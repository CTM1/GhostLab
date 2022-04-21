#include <ncurses.h>

int handle_games(int sock);
int handle_ogame(int sock, int nbGames, WINDOW *gameswindow);
int send_newpl(int sock, char *username, char *udpport);