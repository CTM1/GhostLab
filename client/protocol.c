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

int handle_ogame(int sock, int nbGames, WINDOW *gameswindow) {
    //TODO finish this shit
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

        mvwprintw(gameswindow, 2+i, 2, "GAME %d (%d/256 players)", id, nbPlayers);
    }
    wrefresh(gameswindow);
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