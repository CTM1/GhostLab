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

#include "includes/utils.h"
#include "includes/gamelist.h"

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

void display_main_screen(int row, int col) {
    for(int i=0; i<6; i++) {
        mvprintw((row/4)+i, (col-60)/2, "%s", logolines[i]);
    }

    char iptxt[] =   "Server's IP address : ";
    char porttxt[] = "Server port         : ";
    mvprintw(2*(row/3), ((col-strlen(iptxt)-15)/2), "%s", iptxt);
    mvprintw(2*(row/3)+1, ((col-strlen(porttxt)-15)/2), "%s", porttxt);
    refresh();
}

void get_main_screen_input(int row, int col, char *ip, char *port) {
    echo();
    move(2*(row/3), (col/2)+4);
    refresh();
    getstr(ip);
    move(2*(row/3)+1, (col/2)+4);
    getstr(port);
    noecho();
}

void clear_main_screen_input(int row, int col) {
    move(2*(row/3), (col/2)+4);
    clrtoeol();
    move(2*(row/3)+1, (col/2)+4);
    clrtoeol();
}

int main(int argc, char **argv) {
    initscr();
    keypad(stdscr, true);
    
    int row, col;
    getmaxyx(stdscr, row, col);

    int sock = -1;
    char ip[16];
    char port[6];
    display_main_screen(row, col);
    while(sock < 0) {
        get_main_screen_input(row, col, ip, port);
        sock = connect_server(ip, port);
        if (sock < 0) {
            char errormsg[] = "An error occured during server connection";
            mvprintw(row-2, (col-strlen(errormsg))/2, "%s", errormsg);
            clear_main_screen_input(row, col);
            refresh();
        }
    }
    move(row-2, 0);
    erase();
    refresh();
    gamelist(sock, ip, port);
    endwin();
    
}