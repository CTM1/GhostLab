#include <ncurses.h>

#include "utils.h"

int send_games(int sock);
int handle_games(int sock);
int handle_ogame(int sock, int nbGames, game garray[]);
int send_newpl(int sock, char *username, char *udpport);
int send_regis(int sock, char *username, char *udpport, uint8_t gameid);
int getgamesize(int sock, uint8_t gamenumber, labsize *lbsize);
int getplayerlist(int sock, uint8_t gamenumber, playerlist *pl);
int unreg(int sock, uint8_t gameId);