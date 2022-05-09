#include <stdint.h>
#include <stdint.h>

#ifndef UTILS_H
#define UTILS_H

#define die(msg, code) { printw("%s", msg); return code; }

typedef struct {
    uint8_t gameId;
    uint8_t nbPlayers;
} game;

typedef struct {
    uint16_t width;
    uint16_t height;
} labsize;

typedef struct {
    uint8_t nplayers;
    char **idList;
} playerlist;

typedef struct {
    uint8_t gameId;
    uint16_t height;
    uint16_t width;
    uint8_t nbGhosts;
    char ip[16];
    int port;
} welcome;

typedef struct {
    int x;
    int y;
    int score;
} position_score;
typedef struct {
    uint8_t nplayers;
    char **usernames;
    position_score **pos_scores;
} glist;

int posmod(int i, int n);
int divroundup(int a, int b);
int recv_n_bytes(int sock, void *buffer, int n);
void format_username(char *username);

extern char *logolines[];

#endif