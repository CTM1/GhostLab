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

int handle_games(int sock) {
    char msg[6];
    int r = recv(sock, msg, 6, 0);
    if (r != 6)
        return -1;
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

int handle_ogame(int sock, int nbGames) {
    //TODO finish this shit
    for(int i=0; i<nbGames; i++) {
        char msg[6];
        int r = recv(sock, msg, 6, 0);
        if (r != 6)
            return -1;
        if(strncmp("OGAME ", msg, 6)) {
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
    }
}

void draw_windows(int row, int col, char *connip, char *connport) {
    WINDOW *topwindow = newwin(5, col, 0, 0);
    box(topwindow, 0, 0);
    mvwprintw(topwindow, 2, 4, "GhostLab -- OCB client -- v0.1");
    char connmsg[50];
    sprintf(connmsg, "Connected to %s:%s", connip, connport);
    mvwprintw(topwindow, 2, col-strlen(connmsg)-5, "%s", connmsg);
    wrefresh(topwindow);
}

void gamelist(int sock, char *ip, char *port) {
    int row, col;
    getmaxyx(stdscr, row, col);

    draw_windows(row, col, ip, port);
    

    int nbGames = handle_games(sock);
    if (nbGames<0)
        printw("ERROR");

    getch();
}