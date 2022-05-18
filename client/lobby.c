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

#include "includes/protocol.h"
#include "includes/game.h"


typedef struct {
    WINDOW *topwindow;
    WINDOW *lobbywindow;
} lobby_windows;

lobby_windows * draw_lobby_windows(int row, int col, char *connip, char *connport, uint8_t gameId, labsize lbsize) {
    lobby_windows *lbw = malloc(sizeof(lobby_windows));
    WINDOW *topwindow = newwin(5, col, 0, 0);
    box(topwindow, 0, 0);
    mvwprintw(topwindow, 2, 4, "GhostLab -- OCB client -- v0.1");
    char connmsg[50];
    sprintf(connmsg, "Connected to %s:%s", connip, connport);
    mvwprintw(topwindow, 2, col-strlen(connmsg)-5, "%s", connmsg);
    wrefresh(topwindow);

    WINDOW *lobbywindow = newwin(row-4, col, 4, 0);
    box(lobbywindow, 0, 0);
    char *options[] = {"* Refresh player list", "* Ready to start" , "* Quit lobby"};
    for(int i=0; i<3; i++) {
        mvwprintw(lobbywindow, (row/2-2)+i, col-28, "%s", options[i]);
    }
    mvwaddch(lobbywindow, 0, 0, ACS_LTEE);
    mvwaddch(lobbywindow, 0, col-1, ACS_RTEE);

    mvwprintw(lobbywindow, 2, 2, "Game %d lobby", gameId);
    mvwprintw(lobbywindow, 4, 2, "Labyrinth size : %d*%d", lbsize.width, lbsize.height);
    wrefresh(lobbywindow);

    lbw->topwindow = topwindow;
    lbw->lobbywindow = lobbywindow;
    return lbw;
}

void changeLobbyHighlight(lobby_windows *lbw, int optselected, int row, int col) {
    for (int i=0; i<3; i++) {
        if (i==optselected)
            mvwchgat(lbw->lobbywindow, (row/2-2)+i, col-28, 25, A_REVERSE, 0, NULL);
        else
            mvwchgat(lbw->lobbywindow, (row/2-2)+i, col-28, 25, A_NORMAL, 0, NULL);
    }
}

void refreshPlayerList(lobby_windows *lbw, int socket, uint8_t gameId) {
    playerlist pl;
    int r = getplayerlist(socket, gameId, &pl);
    mvwprintw(lbw->lobbywindow, 5, 2, "%d/255 players in lobby", pl.nplayers);
    for (int i=0; i<pl.nplayers; i++) {
        mvwprintw(lbw->lobbywindow, 6+i, 2, "  - %s", pl.idList[i]);
    }
}

void lobby(int socket, char *connip, char *connport, uint8_t gameId, char *plname, int port) {
    int row, col;
    getmaxyx(stdscr, row, col);
    

    labsize lbsize;
    int r = getgamesize(socket, gameId, &lbsize);

    lobby_windows *lbw = draw_lobby_windows(row, col, connip, connport, gameId, lbsize);
    
    refreshPlayerList(lbw, socket, gameId);

    int selectedOpt = 0;
    bool quitLobby = false;

    while(!quitLobby) {
        changeLobbyHighlight(lbw, selectedOpt, row, col);
        wrefresh(lbw->lobbywindow);
        
        int key = getch();

        switch(key) {
            case KEY_UP:
                selectedOpt = posmod(selectedOpt-1, 3);
                break;
            case KEY_DOWN:
                selectedOpt = posmod(selectedOpt+1, 3);
                break;
            case 10:
                switch (selectedOpt) {
                    case 0:
                        lbw = draw_lobby_windows(row, col, connip, connport, gameId, lbsize);
                        refreshPlayerList(lbw, socket, gameId);
                        break;
                    case 1:
                        r = send_start(socket);
                        wprintw(lbw->lobbywindow, "START, CODE %d\n", r);
                        wrefresh(lbw->lobbywindow);
                        welcome w;
                        r = wait_welcome(socket, &w);
                        wprintw(lbw->lobbywindow, "WELCO, CODE %d\n", r);
                        if (r == 0) {
                            // wprintw(lbw->lobbywindow, "GOT WELCO");
                            // wprintw(lbw->lobbywindow, "gameId : %d\nheight : %d\nwidth : %d\nnbghost : %d\nmcip : %s\nmcport : %d\n",
                            // w.gameId, w.height, w.width, w.nbGhosts, w.ip, w.port);
                            erase();
                            refresh();
                            maingame(socket, connip, connport, &w, plname, port);
                        }
                        wrefresh(lbw->lobbywindow);
                        break;
                    case 2:
                        unreg(socket, gameId);
                        quitLobby = true;
                        break;
                }
                break;
            
        }
    }
    erase();
    refresh();
}