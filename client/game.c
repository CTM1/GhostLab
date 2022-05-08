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

typedef struct {
    WINDOW *topwindow;
    WINDOW *gamewindow;
    WINDOW *playerlistwindow;
    WINDOW *chatwindow;
    WINDOW *inputwindow;
} game_windows;

void draw_empty_game_window(int row, int col, game_windows *gw) {
    WINDOW *w = newwin(row-4-4, (2*col)/3, 4, 0);
    box(w, 0, 0);
    mvwaddch(w, 0, 0, ACS_LTEE);
    mvwaddch(w, 0, (2*col)/3-1, ACS_TTEE);
    mvwaddch(w, (row-4)/2-2, (2*col)/3-1, ACS_LTEE);
    mvwaddch(w, row-4-4-1, (2*col)/3-1, ACS_RTEE);
    mvwaddch(w, row-4-4-1, 0, ACS_LTEE);
    gw->gamewindow = w;
    wrefresh(w);
}

game_windows * draw_game_windows(int row, int col, char *connip, char *connport, uint8_t gameId) {
    game_windows *gw = malloc(sizeof(game_windows));
    WINDOW *topwindow = newwin(5, col, 0, 0);
    box(topwindow, 0, 0);
    mvwprintw(topwindow, 2, 4, "GhostLab -- OCB client -- v0.1 | Game %d", gameId);
    char connmsg[50];
    sprintf(connmsg, "Connected to %s:%s", connip, connport);
    mvwprintw(topwindow, 2, col-strlen(connmsg)-5, "%s", connmsg);
    wrefresh(topwindow);

    WINDOW *playerlistwindow = newwin((row-4)/2, col/3+1, 4, (2*col/3)-1);
    box(playerlistwindow, 0, 0);
    mvwaddch(playerlistwindow, 0, col/3, ACS_RTEE);
    wrefresh(playerlistwindow);

    WINDOW *chatwindow = newwin((row-4)/2+2, col/3+1, 4+(row-4)/2-2, (2*col/3)-1);
    box(chatwindow, 0, 0);
    mvwaddch(chatwindow, 0, col/3, ACS_RTEE);
    wrefresh(chatwindow);

    WINDOW *inputwindow = newwin(5, (2*col/3), row-5, 0);
    box(inputwindow, 0, 0);
    mvwaddch(inputwindow, 4, (2*col/3)-1, ACS_BTEE);
    wrefresh(inputwindow);

    draw_empty_game_window(row, col, gw);



    gw->topwindow = topwindow;
    gw->playerlistwindow = playerlistwindow;
    gw->chatwindow = chatwindow;
    gw->inputwindow = inputwindow;

    return gw;
}

char ** get_game_view(int **labyrinth, int labwidth, int labheight, int gwsizex, int gwsizey, int posx, int posy) {
    char **ret = malloc(gwsizey * sizeof(char*));
    for (int i=0; i<gwsizey; i++)
        ret[i] = malloc(gwsizex+1 * sizeof(char));
    int gwcenterx = gwsizex/2;
    int gwcentery = gwsizey/2;
    int ci = 0;
    int cj = 0;
    for(int y=0; y<gwsizey; y++) {
        for (int x=0; x<gwsizex; x++) {
            // refresh();
            int evalx = posx-gwcenterx+x;
            int evaly = posy-gwcentery+y;
            // printw("%d %d || ", evalx, evaly);
            if (evalx == posx && evaly == posy) {
                ret[cj][ci] = '@';
            } else {
                if (evalx < 0 || evaly < 0 || evalx >= labwidth || evaly >= labheight || labyrinth[evaly][evalx])
                    ret[cj][ci] = ' ';
                else
                    ret[cj][ci] = '#';
            }
            ci++;
        }
        ret[cj][ci] = 0;
        ci = 0;
        cj++;
    }
    return ret;
}

void maingame(int sock, char *connip, char *connport, welcome *welco) {
    int row, col;
    getmaxyx(stdscr, row, col);
    int gwsizex = (2*col)/3-2;
    int gwsizey = row-4-4;

    int labtestvalues[9][9] =   {
                        {0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 1, 1, 1, 1, 1, 1, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 1, 1, 1, 0, 1, 0},
                        {0, 1, 0, 0, 0, 1, 0, 1, 0},
                        {0, 1, 0, 1, 1, 1, 0, 1, 0},
                        {0, 1, 0, 0, 0, 0, 0, 1, 0},
                        {0, 1, 1, 1, 1, 1, 1, 1, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0}
                    };

    int **lab = malloc(9 * sizeof(int*));
    for (int i=0; i<9; i++)
        lab[i] = malloc(9 * sizeof(int));

    for(int y=0; y<9; y++) {
        for (int x=0; x<9; x++) {
            lab[y][x] = labtestvalues[y][x];
        }
    }

    game_windows *gw = draw_game_windows(row, col, connip, connport, welco->gameId);
    int posX = 4;
    int posY = 5;
    char **render = get_game_view(lab, 9, 9, gwsizex-1, gwsizey-2, 4, 5);
    for(int i=0; i<gwsizey-2; i++) {
        mvwprintw(gw->gamewindow, 1+i, 1, "%s", render[i]);
    }
    wrefresh(gw->gamewindow);

    while(true) {
        int key = getch();
        switch (key) {
            case KEY_LEFT:
                posX -= 1;
                break;
            case KEY_RIGHT:
                posX += 1;
                break;
            case KEY_UP:
                posY -= 1;
                break;
            case KEY_DOWN:
                posY += 1;
        }
        render = get_game_view(lab, 9, 9, gwsizex-1, gwsizey-2, posX, posY);
        for(int i=0; i<gwsizey-2; i++) {
            mvwprintw(gw->gamewindow, 1+i, 1, "%s", render[i]);
        }
        wrefresh(gw->gamewindow);
    }
    
    
    
}