#include <stdint.h>
#include <stdint.h>

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

int posmod(int i, int n);
int recv_n_bytes(int sock, void *buffer, int n);
void format_username(char *username);

extern char *logolines[];