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

#define die(msg, code) { printw("%s", msg); return code; }

struct gamelist_windows {
    WINDOW *topwindow;
    WINDOW *gameswindow;
};

struct gamelist_windows * draw_windows(int row, int col, char *connip, char *connport) {
    struct gamelist_windows *gmw = malloc(sizeof(struct gamelist_windows));
    WINDOW *topwindow = newwin(5, col, 0, 0);
    box(topwindow, 0, 0);
    mvwprintw(topwindow, 2, 4, "GhostLab -- OCB client -- v0.1");
    char connmsg[50];
    sprintf(connmsg, "Connected to %s:%s", connip, connport);
    mvwprintw(topwindow, 2, col-strlen(connmsg)-5, "%s", connmsg);
    wrefresh(topwindow);

    WINDOW *gameswindow = newwin(row-4, col, 4, 0);
    box(gameswindow, 0, 0);
    mvwaddch(gameswindow, 0, 0, ACS_LTEE);
    mvwaddch(gameswindow, 0, col-1, ACS_RTEE);
    wrefresh(gameswindow);

    gmw->topwindow = topwindow;
    gmw->gameswindow = gameswindow;
    return gmw;
}

int handle_games(int sock) {
    char msg[6];
    int r = recv(sock, msg, 6, 0);
    if (r != 6)
        die("ERR1", -1);
    if(strncmp("GAMES ", msg, 6)) {
        return -1;
    }

    uint8_t nbGames;
    r = recv(sock, &nbGames, 1, 0);
    if (r != 1)
        return -1;
    char suffix[4];
    r = recv(sock, suffix, 3, 0);
    if (r != 3)
        return -1;
    if (strncmp("***", suffix, 3))
        return -1;

    return nbGames;
}

int handle_ogame(int sock, int nbGames, WINDOW *gameswindow) {
    //TODO finish this shit
    for(int i=0; i<nbGames; i++) {
        char msg[6];
        int r = recv(sock, msg, 6, 0);
        if (r != 6)
            die("ERR1", -1);
        if(strncmp("OGAME ", msg, 6))
            die("ERR2", -1);

        uint8_t id;
        r = recv(sock, &id, 1, 0);
        if (r != 1)
            die("ERR3", -1);
        
        char space;
        r = recv(sock, &space, 1, 0);
        if (space != ' ')
            die("ERR4", -1);

        uint8_t nbPlayers;
        r = recv(sock, &nbPlayers, 1, 0);
        if (r != 1)
            die("ERR5", -1);

        char suffix[4];
        r = recv(sock, suffix, 3, 0);
        if (r != 3)
            die("ERR6", -1);
        if (strncmp("***", suffix, 3))
            die("ERR7", -1);

        mvwprintw(gameswindow, 2+i, 2, "GAME %d (%d/256 players)", id, nbPlayers);
    }
    wrefresh(gameswindow);
}

void gamelist(int sock, char *ip, char *port) {
    int row, col;
    getmaxyx(stdscr, row, col);

    curs_set(0);

    struct gamelist_windows *gmw = draw_windows(row, col, ip, port);
    

    int nbGames = handle_games(sock);
    if (nbGames<0)
        printw("ERROR");
    if (nbGames == 0) {
        mvwprintw(gmw->gameswindow, 2, 2, "There are no games available.");
    } else {
        if (handle_ogame(sock, nbGames, gmw->gameswindow) < 0)
            printw("ERROR 2");
    }

    wrefresh(gmw->gameswindow);
    

    getch();
}