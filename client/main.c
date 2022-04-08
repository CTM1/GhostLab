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

#include "logo.h"

int connect_server(char *ip, char *portstr) {
    struct sockaddr_in saddrin;
    struct in_addr inaddr;
    int port;
    int sock;

    errno = 0;
    port = (int)strtol(portstr, (char **)NULL, 10);
    if (errno != 0) {
        return -1;
    }
    if (inet_pton(AF_INET, ip, &inaddr) == 0)
        return -2;
    
    saddrin.sin_family = AF_INET;
    saddrin.sin_port = htons(port);
    saddrin.sin_addr = inaddr;

    sock = socket(AF_INET, SOCK_STREAM, 0);
    
    if(connect(sock, (struct sockaddr*)&saddrin, sizeof(saddrin)) < 0)
        return -3;

    return sock;    
}

int main(int argc, char **argv) {
    initscr();
    keypad(stdscr, true);
    
    int row, col;

    getmaxyx(stdscr, row, col);

    // printw("%d, %d", col, row);
    for(int i=0; i<6; i++) {
        mvprintw((row/4)+i, (col-60)/2, "%s", logolines[i]);
    }
    refresh();

    char iptxt[] =   "Server's IP address : ";
    char porttxt[] = "Server port         : ";
    mvprintw(2*(row/3), ((col-strlen(iptxt)-15)/2), "%s", iptxt);
    mvprintw(2*(row/3)+1, ((col-strlen(porttxt)-15)/2), "%s", porttxt);
    move(2*(row/3), (col/2)+4);
    refresh();
    char ip[16];
    getstr(ip);
    move(2*(row/3)+1, (col/2)+4);
    char port[6];
    getstr(port);
    noecho();
    
    getch();
    endwin();
    
}