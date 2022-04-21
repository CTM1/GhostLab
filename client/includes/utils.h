#define die(msg, code) { printw("%s", msg); return code; }

int posmod(int i, int n);
int recv_n_bytes(int sock, void *buffer, int n);
void format_username(char *username);

extern char *logolines[];