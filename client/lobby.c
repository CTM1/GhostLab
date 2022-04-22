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

typedef struct {
    WINDOW *topwindow;
    WINDOW *lobbywindow;
} lobby_windows;

lobby_windows * draw_lobby_windows(int row, int col, char *connip, char *connport) {
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
    mvwaddch(lobbywindow, 0, 0, ACS_LTEE);
    mvwaddch(lobbywindow, 0, col-1, ACS_RTEE);
    wrefresh(lobbywindow);

    lbw->topwindow = topwindow;
    lbw->lobbywindow = lobbywindow;
    return lbw;
}

void lobby(int socket, char *connip, char *connport, uint8_t gameId) {
    int row, col;
    getmaxyx(stdscr, row, col);
    lobby_windows *lbw = draw_lobby_windows(row, col, connip, connport);
    mvwprintw(lbw->lobbywindow, 2, 2, "Game %d lobby", gameId);
    wrefresh(lbw->lobbywindow);
    getch();
    erase();
    refresh();
}