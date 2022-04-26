#include <stdint.h>
#include <stdio.h>
#include <ncurses.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <errno.h>

#include "includes/lobby.h"
#include "includes/protocol.h"



struct gamelist_windows {
    WINDOW *topwindow;
    WINDOW *gameswindow;
    WINDOW *buttonswindow;
};

void freewindows(struct gamelist_windows *gmw) {
    delwin(gmw->topwindow);
    delwin(gmw->gameswindow);
    delwin(gmw->buttonswindow);
    free(gmw);
}

struct gamelist_windows * draw_gamelist_windows(int row, int col, char *connip, char *connport) {
    struct gamelist_windows *gmw = malloc(sizeof(struct gamelist_windows));
    WINDOW *topwindow = newwin(5, col, 0, 0);
    box(topwindow, 0, 0);
    mvwprintw(topwindow, 2, 4, "GhostLab -- OCB client -- v0.1");
    char connmsg[50];
    sprintf(connmsg, "Connected to %s:%s", connip, connport);
    mvwprintw(topwindow, 2, col-strlen(connmsg)-5, "%s", connmsg);
    wrefresh(topwindow);

    WINDOW *gameswindow = newwin(row-4, col/2, 4, 0);
    box(gameswindow, 0, 0);
    mvwaddch(gameswindow, 0, 0, ACS_LTEE);
    wrefresh(gameswindow);

    WINDOW *buttonswindow = newwin(row-4, (col/2)+1, 4, (col/2)-1);
    box(buttonswindow, 0, 0);
    mvwaddch(buttonswindow, 0, 0, ACS_TTEE);
    mvwaddch(buttonswindow, row-5, 0, ACS_BTEE);
    mvwaddch(buttonswindow, 0, (col/2), ACS_RTEE);
    char *options[] = {"* Create a game", "* Quit"};
    for(int i=0; i<2; i++) {
        mvwprintw(buttonswindow, 2+i, 2, "%s", options[i]);
    }
    wrefresh(buttonswindow);

    gmw->topwindow = topwindow;
    gmw->gameswindow = gameswindow;
    gmw->buttonswindow = buttonswindow;
    return gmw;
}

void changeHighlight(struct gamelist_windows *gmw, int winselected, int gameselected, int optselected, int nbGames) {
    for (int i=0; i<nbGames; i++) {
        if (i==gameselected && winselected == 0)
            mvwchgat(gmw->gameswindow, 2+i, 2, 30, A_REVERSE, 0, NULL);
        else
            mvwchgat(gmw->gameswindow, 2+i, 2, 30, A_NORMAL, 0, NULL);
    }
    for (int i=0; i<2; i++) {
        if (i==optselected && winselected == 1)
            mvwchgat(gmw->buttonswindow, 2+i, 2, 15, A_REVERSE, 0, NULL);
        else
            mvwchgat(gmw->buttonswindow, 2+i, 2, 15, A_NORMAL, 0, NULL);
    }
}

void askUsernameAndPort(struct gamelist_windows *gmw, char *username, char *port) {
    echo();
    curs_set(1);
    mvwprintw(gmw->buttonswindow, 5, 2, "Pseudo : ");
    mvwprintw(gmw->buttonswindow, 6, 2, "Port   : ");
    wmove(gmw->buttonswindow, 5, 11);
    wgetstr(gmw->buttonswindow, username);
    wmove(gmw->buttonswindow, 6, 11);
    wgetstr(gmw->buttonswindow, port);
    noecho();
    curs_set(0);
}

void gamelist(int sock, char *ip, char *port) {
    int row, col;
    getmaxyx(stdscr, row, col);

    curs_set(0);

    int nbGames = handle_games(sock);

    unsigned int selectedWindow = 0;
    unsigned int selectedGame = 0;
    unsigned int selectedButton = 0;

    struct gamelist_windows *gmw = draw_gamelist_windows(row, col, ip, port);
    // mvwprintw(gmw->topwindow, 0, 0, "Window %d, button %d", selectedWindow, selectedButton);
    bool gamelistempty = false;
    game gamelist[nbGames];
    if (nbGames<0)
        printw("ERROR");
    if (nbGames == 0) {
        mvwprintw(gmw->gameswindow, 2, 2, "There are no games available.");
        gamelistempty = true;
        nbGames = 1;
    } else {
        if (handle_ogame(sock, nbGames, gamelist) < 0)
            printw("ERROR 2");
        for(int i=0; i<nbGames; i++)
            mvwprintw(gmw->gameswindow, 2+i, 2, "GAME %d (%d/256 players)", gamelist[i].gameId, gamelist[i].nbPlayers);
    }

    changeHighlight(gmw, selectedWindow, selectedGame, selectedButton, nbGames);

    while(true) {
        wrefresh(gmw->gameswindow);
        wrefresh(gmw->buttonswindow);  
        
        int key = getch();

        switch(key) {
            case KEY_LEFT:
            case KEY_RIGHT:
                selectedWindow = posmod((selectedWindow+1), 2);
                break;
            case KEY_UP:
                if (selectedWindow == 1) {
                    selectedButton = posmod((selectedButton-1), 2);
                } else {
                    selectedGame = posmod(selectedGame-1, nbGames);
                }
                break;
            case KEY_DOWN:
                if (selectedWindow == 1) {
                    selectedButton = posmod((selectedButton+1), 2);
                } else {
                    selectedGame = posmod(selectedGame+1, nbGames);
                }
                break;
            case 10:
                if (selectedWindow == 0 && !gamelistempty) {
                    printf("%d %d\n", selectedGame, gamelist[selectedGame].gameId);
                    char username[9];
                    char udpport[5];
                    memset(username, 0, 9);
                    askUsernameAndPort(gmw, username, udpport);
                    format_username(username);
                    send_regis(sock, username, udpport, gamelist[selectedGame].gameId);
                    char response[11];
                    int r;
                    if ((r = recv_n_bytes(sock, response, 10)) < 0)
                        return; //TODO ERROR HANDLING
                    if (!strncmp(response, "REGOK ", 6)) {
                        uint8_t gameId;
                        gameId = (uint8_t)response[6];
                        erase();
                        refresh();
                        lobby(sock, ip, port, gameId);
                        gmw = draw_gamelist_windows(row, col, ip, port);
                    } else {
                        printw("ERRORRRRWWWW");
                    }
                }
                if (selectedWindow == 1) {
                    if (selectedButton == 1) {
                        close(sock);
                        freewindows(gmw);
                        endwin();
                        exit(0);
                    }
                    if (selectedButton == 0) {
                        char username[9];
                        char udpport[5];
                        memset(username, 0, 9);
                        askUsernameAndPort(gmw, username, udpport);
                        format_username(username);
                        send_newpl(sock, username, udpport);
                        char response[11];
                        int r;
                        if ((r = recv_n_bytes(sock, response, 10)) < 0)
                            return; //TODO ERROR HANDLING
                        if (!strncmp(response, "REGOK ", 6)) {
                            uint8_t gameId;
                            gameId = (uint8_t)response[6];
                            erase();
                            refresh();
                            lobby(sock, ip, port, gameId);
                            gmw = draw_gamelist_windows(row, col, ip, port);
                        } else {
                            printw("ERRORRRRWWWW");
                        }
                        
                    }
                }
        }
        changeHighlight(gmw, selectedWindow, selectedGame, selectedButton, nbGames);
    }
    
    

    
}