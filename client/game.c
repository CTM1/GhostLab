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
#include <pthread.h>

#include "includes/protocol.h"

pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;

char **ghostsAndPlayers;

typedef struct {
    WINDOW *topwindow;
    WINDOW *gamewindow;
    WINDOW *playerlistwindow;
    WINDOW *chatwindow;
    WINDOW *inputwindow;
} game_windows;

typedef struct {
    int socket;
    char *playerName;
    game_windows *gmw;
    welcome *welco;
    int **lab;
    int gwsizex;
    int gwsizey;
    position_score *pos;
} playerRefreshThreadArgs;

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
            } else if (evalx > 0 && evaly > 0 && evalx < labwidth && evaly < labheight && ghostsAndPlayers[evaly][evalx] != 0) {
                ret[cj][ci] = ghostsAndPlayers[evaly][evalx];
            } else {
                if (evalx < 0 || evaly < 0 || evalx >= labwidth || evaly >= labheight || labyrinth[evaly][evalx]) {
                    ret[cj][ci] = ' ';
                }  
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

void free_game_view(char **render, int height) {
    for (int i=0; i<height; i++)
        free(render[i]);
    free(render);
}

void refresh_lab_view(int **lab, game_windows *gw, welcome *welco, position_score *pos, int gwsizex, int gwsizey) {
    char **render = get_game_view(lab, welco->width, welco->height, gwsizex-1, gwsizey-2, pos->x, pos->y);
    for(int i=0; i<gwsizey-2; i++) {
        mvwprintw(gw->gamewindow, 1+i, 1, "%s", render[i]);
    }
    free_game_view(render, gwsizey-2);
    wrefresh(gw->gamewindow);
}

//WARNING : ONLY WORKS FOR 1 LENGTH MOVEMENTS FOR NOW
void refresh_lab_from_movement(int **lab, position_score *pos, position_score *prevpos, int movdir) {
    // fprintf(stderr, "Prev (%d, %d)\nNew (%d, %d)\nMove %d\n\n", prevpos->x, prevpos->y, pos->x, pos->y, movdir);
    if (prevpos->x == pos->x && prevpos->y == pos->y) {
        switch (movdir) {
            case 0: //LEFT
                lab[pos->y][pos->x - 1] = 0;
                break;
            case 1: //RIGHT
                lab[pos->y][pos->x + 1] = 0;
                break;
            case 2: //UP
                lab[pos->y-1][pos->x] = 0;
                break;
            case 3: //DOWN
                lab[pos->y+1][pos->x] = 0;
                break;
        }
    }
}

void do_move(int sock, char *movetype, int movedir, int **lab, position_score *pos, position_score *prevpos) {
    pthread_mutex_lock(&lock);
    sendmov(sock, movetype, 1);
    memcpy(prevpos, pos, sizeof(position_score));
    get_move_response(sock, pos);
    pthread_mutex_unlock(&lock);
    refresh_lab_from_movement(lab, pos, prevpos, movedir);
}

void clear_ghosts_and_players(welcome *welco) {
    for (int i=0; i<welco->height; i++) {
        ghostsAndPlayers[i] = malloc(welco->width * sizeof(char));
        for(int j=0; j<welco->width; j++) {
            ghostsAndPlayers[i][j] = 0;
        }
    }
}


//THREAD FUNCTION
void *player_refresh(void *arg) {
    playerRefreshThreadArgs *prta;
    prta = (playerRefreshThreadArgs*)arg;
    glist *gl = malloc(sizeof(glist));
    while(1) {
        pthread_mutex_lock(&lock);
        clear_ghosts_and_players(prta->welco);
        get_glist(prta->socket, gl);
        for(int i=0; i<gl->nplayers; i++) {    
            int x = gl->pos_scores[i]->x;
            int y = gl->pos_scores[i]->y;
            int score = gl->pos_scores[i]->score;
            if (strncmp(gl->usernames[i], prta->playerName, 8))
                ghostsAndPlayers[y][x] = gl->usernames[i][0];
            else
                wattron(prta->gmw->playerlistwindow, A_BOLD);
            mvwprintw(prta->gmw->playerlistwindow, 1+i, 1, "%s (%d,%d) : %d  ",
            gl->usernames[i], x, y, score);
            wattroff(prta->gmw->playerlistwindow, A_BOLD);
            wrefresh(prta->gmw->playerlistwindow);
        }
        refresh_lab_view(prta->lab, prta->gmw, prta->welco, prta->pos, prta->gwsizex, prta->gwsizey);
        pthread_mutex_unlock(&lock);
        usleep(500000);
    }
}

void maingame(int sock, char *connip, char *connport, welcome *welco, char *plname) {
    int row, col;
    getmaxyx(stdscr, row, col);
    int gwsizex = (2*col)/3-2;
    int gwsizey = row-4-4;

    int **lab = malloc(welco->height * sizeof(int*));
    for (int i=0; i<welco->height; i++) {
        lab[i] = malloc(welco->width * sizeof(int));
        for(int j=0; j<welco->width; j++) {
            lab[i][j] = 1;
        }
    }

    ghostsAndPlayers = malloc(welco->height * sizeof(char*));
    clear_ghosts_and_players(welco);

    position_score *prevpos = malloc(sizeof(position_score));
    position_score *pos = malloc(sizeof(position_score));
    pthread_mutex_lock(&lock);
    int r = handle_posit(sock, pos);
    pthread_mutex_unlock(&lock);
    

    game_windows *gw = draw_game_windows(row, col, connip, connport, welco->gameId);

    pthread_t player_refresh_thread;
    playerRefreshThreadArgs *prta = malloc(sizeof(playerRefreshThreadArgs));
    prta->socket = sock;
    prta->gmw = gw;
    prta->playerName = plname;
    prta->welco = welco;
    prta->lab = lab;
    prta->gwsizex = gwsizex;
    prta->gwsizey = gwsizey;
    prta->pos = pos;
    r = pthread_create(&player_refresh_thread, NULL, player_refresh, (void*)prta);
    if (r != 0) {
        return;
    }

    refresh_lab_view(lab, gw, welco, pos, gwsizex, gwsizey);

    while(true) {
        int key = getch();
        switch (key) {
            case KEY_LEFT:
                do_move(sock, "LEMOV", 0, lab, pos, prevpos);
                break;
            case KEY_RIGHT:
                do_move(sock, "RIMOV", 1, lab, pos, prevpos);
                break;
            case KEY_UP:
                do_move(sock, "UPMOV", 2, lab, pos, prevpos);
                break;
            case KEY_DOWN:
                do_move(sock, "DOMOV", 3, lab, pos, prevpos);
                break;
        }
        refresh_lab_view(lab, gw, welco, pos, gwsizex, gwsizey);
    } 
}