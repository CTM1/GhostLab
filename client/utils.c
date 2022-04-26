#include <sys/socket.h>

int posmod(int i, int n) {
    return (i % n + n) % n;
}

int recv_n_bytes(int sock, void *buffer, int n) {
    int totalreceived = 0;
    while(totalreceived < n) {
        int nreceived = recv(sock, buffer+totalreceived, n-totalreceived, 0);
        if (nreceived <= 0)
            return nreceived;
        totalreceived += nreceived;
    }
    return totalreceived;
}

void format_username(char *username) {
    for (int i=0; i<8; i++) {
        if (username[i] == 0)
            username[i] = '_';
    }
    username[8] = 0;
}

char logoline1[] = "  ________.__                    __  .____          ___.    ";
char logoline2[] = " /  _____/|  |__   ____  _______/  |_|    |   _____ \\_ |__  ";
char logoline3[] = "/   \\  ___|  |  \\ /  _ \\/  ___/\\   __\\    |   \\__  \\ | __ \\ ";
char logoline4[] = "\\    \\_\\  \\   Y  (  <_> )___ \\  |  | |    |___ / __ \\| \\_\\ \\";
char logoline5[] = " \\______  /___|  /\\____/____  > |__| |_______ (____  /___  /";
char logoline6[] = "        \\/     \\/           \\/               \\/    \\/    \\/ ";

char *logolines[] = {logoline1, logoline2, logoline3, logoline4, logoline5, logoline6};