#include <endian.h>
#include <errno.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ncurses.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>

#include "includes/utils.h"

int handle_games(int sock) {
    char msg[6];
    int r = recv_n_bytes(sock, msg, 6);
    if (r != 6)
        die("ERR1", -1);
    if(strncmp("GAMES ", msg, 6)) {
        return -1;
    }

    uint8_t nbGames;
    r = recv_n_bytes(sock, &nbGames, 1);
    if (r != 1)
        return -1;
    char suffix[4];
    r = recv_n_bytes(sock, suffix, 3);
    if (r != 3)
        return -1;
    if (strncmp("***", suffix, 3))
        return -1;
    fprintf(stderr, "> GAMES [%d]***\n", nbGames);

    return nbGames;
}

int handle_ogame(int sock, int nbGames, game garray[]) {
    for(int i=0; i<nbGames; i++) {
        char msg[6];
        int r = recv_n_bytes(sock, msg, 6);
        if (r != 6)
            die("ERR1", -1);
        if(strncmp("OGAME ", msg, 6))
            die("ERR2", -1);

        uint8_t id;
        r = recv_n_bytes(sock, &id, 1);
        if (r != 1)
            die("ERR3", -1);
        
        char space;
        r = recv_n_bytes(sock, &space, 1);
        if (space != ' ')
            die("ERR4", -1);

        uint8_t nbPlayers;
        r = recv_n_bytes(sock, &nbPlayers, 1);
        if (r != 1)
            die("ERR5", -1);

        char suffix[4];
        r = recv_n_bytes(sock, suffix, 3);
        if (r != 3)
            die("ERR6", -1);
        if (strncmp("***", suffix, 3))
            die("ERR7", -1);

        fprintf(stderr, "> OGAME [%d] [%d]***\n", id, nbPlayers);

        garray[i].gameId = id;
        garray[i].nbPlayers = nbPlayers;
    }
    return 0;
}

int send_newpl(int sock, char *username, char *udpport) {
    if (send(sock, "NEWPL ", 6, 0) < 0)
        return -1; //TODO ERROR HANDLING
    if (send(sock, username, 8, 0) < 0)
        return -1; //TODO ERROR HANDLING
    send(sock, " ", 1, 0);
    if (send(sock, udpport, 4, 0) < 0)
        return -1; //TODO ERROR HANDLING
    if (send(sock, "***", 3, 0) < 0)
        return -1; //TODO ERROR HANDLING
    fprintf(stderr, "< NEWPL %s %s***\n", username, udpport);
    return 0;
}

int send_regis(int sock, char *username, char *udpport, uint8_t gameid) {
    if (send(sock, "REGIS ", 6, 0) < 0)
        return -1; //TODO ERROR HANDLING
    if (send(sock, username, 8, 0) < 0)
        return -1; //TODO ERROR HANDLING
    send(sock, " ", 1, 0);
    if (send(sock, udpport, 4, 0) < 0)
        return -1; //TODO ERROR HANDLING
    send(sock, " ", 1, 0);
    if (send(sock, &gameid, 1, 0) < 0)
        return -1;
    if (send(sock, "***", 3, 0) < 0)
        return -1; //TODO ERROR HANDLING
    fprintf(stderr, "< REGIS %s %s [%d]***\n", username, udpport, gameid);
    return 0;
}

int getgamesize(int sock, uint8_t gamenumber, labsize *lbsize) {
    if (send(sock, "SIZE? ", 6, 0) < 0)
        return -1;
    if (send(sock, &gamenumber, 1, 0) < 0)
        return -1; 
    if (send(sock, "***", 3, 0) < 0)
        return -1;

    fprintf(stderr, "< SIZE? [%d]***\n", gamenumber);
    
    char response[16];
    if (recv_n_bytes(sock, response, 5) <= 0)
        return -1;
    if (!strncmp(response, "DUNNO", 5))
        return 1;
    if (strncmp(response, "SIZE!", 5))
        return -1;
    if (recv_n_bytes(sock, response+5, 11) <= 0)
        return -1;
    uint8_t m = (uint8_t)response[6];
    if (m != gamenumber)
        return 2;
    
    memcpy(&lbsize->height, response+8, 2);
    memcpy(&lbsize->width, response+11, 2);

    fprintf(stderr, "> SIZE! [%d] [%d] [%d]***\n", gamenumber, lbsize->height, lbsize->width);

    // lbsize->height = be16toh(lbsize->height);
    // lbsize->width = be16toh(lbsize->width);
    // printw("SIZE! %d %x %x***", m, lbsize->height, lbsize->width);
    return 0;
}

int getplayerlist(int sock, uint8_t gamenumber, playerlist *pl) {
    if (send(sock, "LIST? ", 6, 0) < 0)
        return -1;
    if (send(sock, &gamenumber, 1, 0) < 0)
        return -1; 
    if (send(sock, "***", 3, 0) < 0)
        return -1;

    fprintf(stderr, "< LIST? [%d]***\n", gamenumber);

    char response[12];
    if (recv_n_bytes(sock, response, 5) <= 0)
        return -1;
    if (!strncmp(response, "DUNNO", 5))
        return 1;
    if (strncmp(response, "LIST!", 5))
        return -1;
    if (recv_n_bytes(sock, response+5, 7) <= 0)
        return -1;
    uint8_t m = (uint8_t)response[6];
    fprintf(stderr, "> LIST! [%d]***\n", m);
    if (m != gamenumber)
        return 2;
    pl->nplayers = (uint8_t)response[8];
    pl->idList = malloc(sizeof(char*) * pl->nplayers);
    for (int i=0; i<pl->nplayers; i++) {
        char playr[18];
        if (recv_n_bytes(sock, playr, 17) <= 0)
            return -1;
        playr[17] = 0;
        if (strncmp(playr, "PLAYR", 5))
            return -1;
        pl->idList[i] = malloc(9);
        memcpy(pl->idList[i], playr+6, 8);
        pl->idList[i][8] = 0;
        fprintf(stderr, "> PLAYR %s***\n", pl->idList[i]);
    }
    return 0;
}

int send_games(int sock) {
    fprintf(stderr, "< GAME?***\n");
    return send(sock, "GAME?***", 8, 0);
}

int unreg(int sock, uint8_t gameId) {
    if (send(sock, "UNREG***", 8, 0) < 0)
        return -1;

    fprintf(stderr, "< UNREG***\n");

    char response[10];

    if (recv_n_bytes(sock, response, 6) < 0)
        return -1;

    if (!strncmp(response, "UNROK", 5)) {
        if (recv_n_bytes(sock, response+6, 4) < 0)
            return -1;
        uint8_t receivedGameId = (uint8_t)response[6];
        fprintf(stderr, "> UNROK [%d]***\n", receivedGameId);
        if (receivedGameId != gameId)
            return 1;
        return 0;
    }

    
    return 1;
}

int send_start(int sock) {
    return send(sock, "START***", 8, 0);
    fprintf(stderr, "< START***\n");
}

int wait_welcome(int sock, welcome *w) {
    char msg[16];
    if (recv_n_bytes(sock, msg, 6) < 0)
        return -1;
    if (!strncmp(msg, "WELCO ", 6)) {
        if (recv_n_bytes(sock, msg+6, 10) < 0)
            return -1;
        w->gameId = (uint8_t)msg[6];
        memcpy(&w->height, msg+8, 2);
        memcpy(&w->width, msg+11, 2);
        w->nbGhosts = (uint8_t)msg[14];

        char recvip[16];
        recv_n_bytes(sock, recvip, 16);

        for (int i = 0; i < 16; i++) {
            if (recvip[i] == '#' || recvip[i] == ' ') {
                w->ip[i] = '\0';
                break;
            } else w->ip[i] = recvip[i];
        }

        char port[5];
        if (recv_n_bytes(sock, port, 4) < 0)
            return -1;
        port[4] = 0;
        w->port = atoi(port);
        
        char tail[3];
        if (recv_n_bytes(sock, tail, 3) < 0)
            return -1;

        printf("%s\n",tail);
        if (strncmp(tail, "***", 3))
            return 2;

        fprintf(stderr, "> WELCO [%d] [%d] [%d] [%d] %s%s***\n", w->gameId, w->height, w->width, w->nbGhosts, recvip, port);
        return 0;
    }
    return 1;
}

void fill_pos_from_payload(char *payload, position_score *pos, int xstartindex, int ystartindex) {
    char posX_str[4];
    char posY_str[4];
    memcpy(posX_str, payload+xstartindex, 3);
    posX_str[3] = 0;
    memcpy(posY_str, payload+ystartindex, 3);
    posY_str[3] = 0;

    int posX = atoi(posX_str);
    int posY = atoi(posY_str);
    pos->x = posX;
    pos->y = posY;
}

void fill_score_from_payload(char *payload, position_score *pos, int scorestartindex) {
    char score_str[5];
    memcpy(score_str, payload+scorestartindex, 4);
    score_str[4] = 0;

    int score = atoi(score_str);
    pos->score = score;
}

int handle_posit(int sock, position_score *pos) {
    pos->score = -1;
    char msg[26];
    if (recv_n_bytes(sock, msg, 6) < 0)
        return -1;
    if (!strncmp(msg, "POSIT ", 6)) {
        // fprintf(stderr, "GOT POSIT\n");
        if (recv_n_bytes(sock, msg+6, 19) < 0)
            return -1;
        fill_pos_from_payload(msg, pos, 15, 19);
        // fprintf(stderr, "x=%d, y=%d\n", pos->x, pos->y);
        msg[25] = 0;
        fprintf(stderr, "> %s\n", msg);
        return 0;
    }
    return 1;
}

int sendmov(int sock, char *move, int dist) {
    char request[13];
    sprintf(request, "%s %03d***", move, dist);
    if (send(sock, request, 12, 0) < 0)
        return -1;
    fprintf(stderr, "< %s\n", request);
    return 0;
};

int get_move_response(int sock, position_score *pos) {
    pos->score = -1;
    char response[22];
    if (recv_n_bytes(sock, response, 5) < 0)
        return -1;
    if (!strncmp(response, "MOVE!", 5)) {
        if (recv_n_bytes(sock, response+5, 11) < 0)
            return -1;
        fill_pos_from_payload(response, pos, 6, 10);
        char tmp[17];
        memcpy(tmp, response, 16);
        tmp[16] = 0;
        fprintf(stderr, "> %s\n", tmp);
        return 0;
    } else if (!strncmp(response, "MOVEF", 5)) {
        if (recv_n_bytes(sock, response+5, 16) < 0)
            return -1;
        fill_pos_from_payload(response, pos, 6, 10);
        fill_score_from_payload(response, pos, 14);
        response[21] = 0;
        fprintf(stderr, "> %s\n", response);
        return 0;
    } else if (!strncmp(response, "DUNNO", 5)) {
        if (recv_n_bytes(sock, response+5, 3) < 0)
            return -1;
        return 0;
    }
    return 1;
}

int iquit(int sock) {
    if (send(sock, "IQUIT***", 8, 0) < 0)
        return -1;
    fprintf(stderr, "< IQUIT***\n");
    char response[8];
    if (recv_n_bytes(sock, response, 8) < 0)
        return -1;
    if (strncmp(response, "GOBYE***", 8))
        return 1;
    fprintf(stderr, "> GOBYE***\n");
    return 0;
}

int get_glist(int sock, glist *glist) {
    if (send(sock, "GLIS?***", 8, 0) < 0)
        return -1;
    fprintf(stderr, "< GLIS?***\n");
    char response[10];
    int r = recv_n_bytes(sock, response, 10);
    if (r < 0)
        return -1;
    if (strncmp(response, "GLIS!", 5))
        return 2;
    uint8_t nbPlayers = (uint8_t)response[6];
    fprintf(stderr, "> GLIS! [%d]***\n", nbPlayers);
    glist->nplayers = nbPlayers;
    glist->usernames = malloc(nbPlayers * sizeof(char*));
    glist->pos_scores = malloc(nbPlayers * sizeof(position_score*));
    char gplyr_msg[31];
    for (int i=0; i<nbPlayers; i++) {
        if (recv_n_bytes(sock, gplyr_msg, 30) < 0)
            return -1;
        gplyr_msg[30] = 0;
        if (strncmp(gplyr_msg, "GPLYR ", 6))
            return 1;
        glist->usernames[i] = malloc(9);
        memcpy(glist->usernames[i], gplyr_msg+6, 8);
        glist->usernames[i][8] = 0;

        glist->pos_scores[i] = malloc(sizeof(position_score));
        fill_pos_from_payload(gplyr_msg, glist->pos_scores[i], 15, 19);
        fill_score_from_payload(gplyr_msg, glist->pos_scores[i], 23);
        fprintf(stderr, "> %s\n", gplyr_msg);
    }
    return 0;
}

int send_mall(int sock, char *msg, int msglength) {
    char request[9+msglength+1];
    sprintf(request, "MALL? %s***", msg);
    if (send(sock, request, 9+msglength, 0) < 0)
        return -1;
    request[9+msglength] = 0;
    fprintf(stderr, "< %s\n", request);
    char response[9];
    if (recv_n_bytes(sock, response, 8) < 0)
        return -1;
    if (strncmp(response, "MALL!***", 8))
        return 1;
    response[8] = 0;
    fprintf(stderr, "> %s\n", response);
    return 0;
}

int private_msg(int sock, char *playerid, char *msg, int msglength) {
    char request[18+msglength+1];
    sprintf(request, "SEND? %s %s***", playerid, msg);
    if (send(sock, request, 18+msglength, 0) < 0)
        return -1;
    request[18+msglength] = 0;
    fprintf(stderr, "< %s\n", request);
    char response[9];
    if (recv_n_bytes(sock, response, 8) < 0)
        return -1;
    response[8] = 0;
    fprintf(stderr, "> %s\n", response);
    if (!strncmp(response, "NSEND***", 8))
        return 2;
    if (strncmp(response, "SEND!***", 8))
        return 1;
    return 0;
}
