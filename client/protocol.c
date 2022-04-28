#include <endian.h>
#include <stdint.h>
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
    return 0;
}

int getgamesize(int sock, uint8_t gamenumber, labsize *lbsize) {
    if (send(sock, "SIZE? ", 6, 0) < 0)
        return -1;
    if (send(sock, &gamenumber, 1, 0) < 0)
        return -1; 
    if (send(sock, "***", 3, 0) < 0)
        return -1;
    
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
    }
    return 0;
}

int send_games(int sock) {
    return send(sock, "GAME?***", 8, 0);
}

int unreg(int sock, uint8_t gameId) {
    if (send(sock, "UNREG***", 8, 0) < 0)
        return -1;

    char response[10];

    if (recv_n_bytes(sock, response, 6) < 0)
        return -1;

    if (!strncmp(response, "UNROK", 5)) {
        if (recv_n_bytes(sock, response+6, 4) < 0)
            return -1;
        uint8_t receivedGameId = (uint8_t)response[6];
        if (receivedGameId != gameId)
            return 1;
        return 0;
    }

    
    return 2;
}