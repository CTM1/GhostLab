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
#include <fcntl.h>

#include "includes/protocol.h"

pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;

char **player_grid;
char **ghost_grid;
int gameOver = 0;

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
    glist *gl;
} playerRefreshThreadArgs;

typedef struct {
    char hint_char;
    int x;
    int y;
    int timeout;
} hint_thread_args;

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

void draw_game_windows_borders(int row, int col, char *connip, char *connport, uint8_t gameId) {
    WINDOW *topwindow = newwin(5, col, 0, 0);
    box(topwindow, 0, 0);
    mvwprintw(topwindow, 2, 4, "GhostLab -- OCB client -- v0.1 | Game %d", gameId);
    char connmsg[50];
    sprintf(connmsg, "Connected to %s:%s", connip, connport);
    mvwprintw(topwindow, 2, col-strlen(connmsg)-5, "%s", connmsg);
    wrefresh(topwindow);

    WINDOW *playerlistwindowborders = newwin((row-4)/2 - 1, col/3+1, 4, (2*col/3)-1);
    box(playerlistwindowborders, 0, 0);
    mvwaddch(playerlistwindowborders, 0, col/3, ACS_RTEE);
    wrefresh(playerlistwindowborders);

    WINDOW *chatwindowborders = newwin((row-4)/2+2, col/3+1, 4+(row-4)/2 - 2, (2*col/3)-1);
    box(chatwindowborders, 0, 0);
    mvwaddch(chatwindowborders, 0, col/3, ACS_RTEE);
    wrefresh(chatwindowborders);
    

    WINDOW *inputwindowborders = newwin(5, (2*col/3), row-5, 0);
    box(inputwindowborders, 0, 0);
    mvwaddch(inputwindowborders, 4, (2*col/3)-1, ACS_BTEE);
    wrefresh(inputwindowborders);

    WINDOW *gamewinborders = newwin(row-4-4, (2*col)/3, 4, 0);
    box(gamewinborders, 0, 0);
    mvwaddch(gamewinborders, 0, 0, ACS_LTEE);
    mvwaddch(gamewinborders, 0, (2*col)/3-1, ACS_TTEE);
    mvwaddch(gamewinborders, (row-4)/2-2, (2*col)/3-1, ACS_LTEE);
    mvwaddch(gamewinborders, row-4-4-1, (2*col)/3-1, ACS_RTEE);
    mvwaddch(gamewinborders, row-4-4-1, 0, ACS_LTEE);
    wrefresh(gamewinborders);

    
    
}

game_windows * draw_game_windows(int row, int col) {
    game_windows *gw = malloc(sizeof(game_windows));

    WINDOW *playerlistwindow = newwin((row-4)/2-1 - 2, col/3+1 - 2, 4 + 1, (2*col/3)-1 + 1);
    wrefresh(playerlistwindow);

    WINDOW *chatwindow = newwin((row-4)/2+2 - 2, col/3+1 - 2, 4+(row-4)/2-2 + 1, (2*col/3)-1 + 1);
    wrefresh(chatwindow);

    WINDOW *inputwindow = newwin(5 - 2, (2*col/3) - 2, row-5 + 1, 0 + 1);
    wrefresh(inputwindow);

    WINDOW *gamewin = newwin(row-4-4 - 2, (2*col)/3 - 2, 4 + 1, 0 + 1);
    wrefresh(gamewin);

    gw->playerlistwindow = playerlistwindow;
    gw->chatwindow = chatwindow;
    gw->inputwindow = inputwindow;
    gw->gamewindow = gamewin;

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
            int evalx = posx-gwcenterx+x;
            int evaly = posy-gwcentery+y;
            if (evalx == posx && evaly == posy) {
                ret[cj][ci] = '@';
            } else if (evalx >= 0 && evaly >= 0 && evalx < labwidth && evaly < labheight ) {
                if (player_grid[evaly][evalx] != 0) 
                    ret[cj][ci] = player_grid[evaly][evalx];
                else if (ghost_grid[evaly][evalx] != 0) 
                    ret[cj][ci] = ghost_grid[evaly][evalx];
                else if (labyrinth[evaly][evalx])
                    ret[cj][ci] = ' ';
                else
                    ret[cj][ci] = '#';
            } else {
                ret[cj][ci] = ' ';
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
    char **render = get_game_view(lab, welco->width, welco->height, gwsizex, gwsizey, pos->x, pos->y);
    for(int i=0; i<gwsizey; i++) {
        mvwprintw(gw->gamewindow, i, 0, "%s", render[i]);
    }
    free_game_view(render, gwsizey);
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

void clear_player_grid(welcome *welco) {
    for (int i=0; i<welco->height; i++) {
        player_grid[i] = malloc(welco->width * sizeof(char));
        for(int j=0; j<welco->width; j++) {
            player_grid[i][j] = 0;
        }
    }
}

//THREAD FUNCTION
void *player_refresh(void *arg) {
    playerRefreshThreadArgs *prta;
    prta = (playerRefreshThreadArgs*)arg;
    while(1) {
        pthread_mutex_lock(&lock);
        clear_player_grid(prta->welco);
        get_glist(prta->socket, prta->gl);
        for(int i=0; i<prta->gl->nplayers; i++) {    
            int x = prta->gl->pos_scores[i]->x;
            int y = prta->gl->pos_scores[i]->y;
            int score = prta->gl->pos_scores[i]->score;
            int is_player = !strncmp(prta->gl->usernames[i], prta->playerName, 8);
            if (!is_player)
                player_grid[y][x] = prta->gl->usernames[i][0];
            else
                wattron(prta->gmw->playerlistwindow, A_BOLD);
            mvwprintw(prta->gmw->playerlistwindow, i, 0, "%s (%d,%d) : %d  ",
            prta->gl->usernames[i], x, y, score);
            if (is_player)
                wattroff(prta->gmw->playerlistwindow, A_BOLD);
            wrefresh(prta->gmw->playerlistwindow);
        }
        refresh_lab_view(prta->lab, prta->gmw, prta->welco, prta->pos, prta->gwsizex, prta->gwsizey);
        pthread_mutex_unlock(&lock);
        usleep(500000);
    }
}

//THREAD FUNCTION
void *hint(void *arg) {
    hint_thread_args *hta;
    hta = (hint_thread_args *)arg;

    pthread_mutex_lock(&lock);
    ghost_grid[hta->y][hta->x] = hta->hint_char;
    pthread_mutex_unlock(&lock);
    sleep(hta->timeout);
    pthread_mutex_lock(&lock);
    if (ghost_grid[hta->y][hta->x] == hta->hint_char)
        ghost_grid[hta->y][hta->x] = 0;
    pthread_mutex_unlock(&lock);
    free(hta);
    return NULL;
}

void handle_ghost(int mcsock, char *request) {
    char x_str[4];
    char y_str[4];
    char tail[3];
    memcpy(x_str, request, 3);
    x_str[3] = 0;
    memcpy(y_str, request+4, 3);
    y_str[3] = 0;

    int x = atoi(x_str);
    int y = atoi(y_str);
    
    pthread_t *t = malloc(sizeof(pthread_t));
    hint_thread_args *hta = malloc(sizeof(hint_thread_args));
    hta->hint_char = '?';
    hta->x = x;
    hta->y = y;
    hta->timeout = 3;
    pthread_create(t, NULL, hint, (void*)hta);
    pthread_detach(*t);
    // fprintf(stderr, "> GHOST %s %s+++\n", x_str, y_str);
}

void handle_score(int mcsock, glist *gl, char *request) {
    char player_id[9];
    char score_str[5];
    char x_str[4];
    char y_str[4];
    memcpy(player_id, request, 8);
    player_id[8] = 0;
    memcpy(score_str, request+9, 4);
    score_str[4] = 0;
    memcpy(x_str, request+14, 3);
    x_str[3] = 0;
    memcpy(y_str, request+18, 3);
    y_str[3] = 0;

    int score = atoi(score_str);
    int x = atoi(x_str);
    int y = atoi(y_str);

    pthread_t *t = malloc(sizeof(pthread_t));
    hint_thread_args *hta = malloc(sizeof(hint_thread_args));
    hta->hint_char = '!';
    hta->x = x;
    hta->y = y;
    hta->timeout = 3;
    pthread_create(t, NULL, hint, (void*)hta);
    pthread_detach(*t);
    // fprintf(stderr, "> SCORE %s %s %s %s+++\n", player_id, score_str, x_str, y_str);
}

void handle_messa(int mcsock, char *request, game_windows *gmw) {
    // fprintf(stderr, "GOT MESSA\n");
    char player_id[9];
    memcpy(player_id, request, 8);
    player_id[8] = 0;
    wprintw(gmw->chatwindow, "[%s] ", player_id);
    wrefresh(gmw->chatwindow);
    int c = 9;
    do {
        wprintw(gmw->chatwindow, "%c", request[c]);
        c++;
    } while (c >=200 || !(request[c] == '+' && request[c+1] == '+' && request[c+2] == '+'));
    wprintw(gmw->chatwindow, "\n");
    wrefresh(gmw->chatwindow);
}

void handle_multicast_requests(int mcsock, glist *gl, game_windows *gmw) {
    char request[256];
    int r = recv(mcsock, request, 256, 0);
    if (r > 0) {
        request[255] = 0;
        // fprintf(stderr, "%s\n", request);
        if (!strncmp(request, "GHOST ", 6)) {
            handle_ghost(mcsock, request+6);
        } else if (!strncmp(request, "SCORE ", 6)) {
            handle_score(mcsock, gl, request+6);
        } else if (!strncmp(request, "MESSA ", 6)) {
            handle_messa(mcsock, request+6, gmw);
        } else if (!strncmp(request, "ENDGA ", 6)) {
            gameOver = 1;
        }
    }
}

void maingame(int sock, char *connip, char *connport, welcome *welco, char *plname, int port) {
    int row, col;
    getmaxyx(stdscr, row, col);
    int gwsizex = (2*col)/3 - 2;
    int gwsizey = row-4-4 - 2;
    int r;

    int mcsocket = socket(PF_INET, SOCK_DGRAM, 0);
    // int udpsocket = socket(AF_INET, SOCK_DGRAM, 0);

    fcntl(mcsocket, F_SETFL, O_NONBLOCK);
    // fcntl(udpsocket, F_SETFL, O_NONBLOCK);

    struct sockaddr_in mcaddr;
    memset(&mcaddr, 0, sizeof(mcaddr));
    mcaddr.sin_family = AF_INET;
    mcaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    mcaddr.sin_port = htons(welco->port);

    // struct sockaddr_in udpaddr;
    // memset(&udpaddr, 0, sizeof(udpaddr));
    // udpaddr.sin_family = AF_INET;
    // mcaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    // udpaddr.sin_port = htons(port);

    const int trueFlag = 1;
    setsockopt(mcsocket, SOL_SOCKET, SO_REUSEADDR, &trueFlag, sizeof(int));

    r = bind(mcsocket, (struct sockaddr*) &mcaddr, sizeof(mcaddr));
    // fprintf(stderr, "[*] Bind multicast socket, r=%d (%d)\n", r, errno);
    // r = bind(udpsocket, (struct sockaddr*) &udpaddr, sizeof(udpaddr));

    struct ip_mreq mreq;
    inet_pton(AF_INET, welco->ip, &mreq.imr_multiaddr.s_addr);
    mreq.imr_interface.s_addr=htonl(INADDR_ANY);
    r=setsockopt(mcsocket, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq));

    int **lab = malloc(welco->height * sizeof(int*));
    for (int i=0; i<welco->height; i++) {
        lab[i] = malloc(welco->width * sizeof(int));
        for(int j=0; j<welco->width; j++) {
            lab[i][j] = 1;
        }
    }

    player_grid = malloc(welco->height * sizeof(char*));
    clear_player_grid(welco);

    ghost_grid = malloc(welco->height * sizeof(char*));
    for (int i=0; i<welco->height; i++) {
        ghost_grid[i] = malloc(welco->width * sizeof(char));
        for(int j=0; j<welco->width; j++) {
            ghost_grid[i][j] = 0;
        }
    }


    position_score *prevpos = malloc(sizeof(position_score));
    position_score *pos = malloc(sizeof(position_score));
    pthread_mutex_lock(&lock);
    r = handle_posit(sock, pos);
    pthread_mutex_unlock(&lock);
    
    draw_game_windows_borders(row, col, connip, connport, welco->gameId);
    game_windows *gw = draw_game_windows(row, col);

    glist *gl = malloc(sizeof(glist));

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
    prta->gl = gl;
    r = pthread_create(&player_refresh_thread, NULL, player_refresh, (void*)prta);
    if (r != 0) {
        return;
    }

    refresh_lab_view(lab, gw, welco, pos, gwsizex, gwsizey);

    char tmpbuf[5];

    timeout(500);

    while(true) {
        handle_multicast_requests(mcsocket, gl, gw);
        if (gameOver)
            return;
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
        switch ((char)key) {
            case 'm':
                wmove(gw->inputwindow, 0, 0);
                echo();
                curs_set(1);
                char msg[201];
                wprintw(gw->inputwindow, "chat> ");
                wrefresh(gw->inputwindow);
                wgetnstr(gw->inputwindow, msg, 200);
                msg[200] = 0;
                pthread_mutex_lock(&lock);
                send_mall(sock, msg, strlen(msg));
                pthread_mutex_unlock(&lock);
                curs_set(0);
                noecho();
                wclear(gw->inputwindow);
                wrefresh(gw->inputwindow);
                break;
            case 'p':
                break;
            case 'e':
                break;
        }
        refresh_lab_view(lab, gw, welco, pos, gwsizex, gwsizey);
    } 
}