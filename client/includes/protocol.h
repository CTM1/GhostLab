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
int send_start(int sock);
int wait_welcome(int sock, welcome *w);
int handle_posit(int sock, position_score *pos);
int sendmov(int sock, char *move, int dist);
int get_move_response(int sock, position_score *pos);
int iquit(int sock);
int get_glist(int sock, glist *glist);
int send_mall(int sock, char *msg, int msglength);
int private_msg(int sock, char playerid[8], char *msg, int msglength);