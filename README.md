# GhostLab 10

# Compilation et exécution

## Serveur

On peut compiler le serveur avec `make server` et l'exécuter avec `make runserver`, il démarrera sur le port 1337.
Alternativement, on peut le lancer sur le port que l'on souhaite avec
`java -cp classes ghostlab.MainServer [port]`.


> Le mode verbose peut être activé en mettant une valeur quelconque à la variable d'environnemment `VERBOSE`.

## Client

Également avec `make client` et `make runclient`. L'utilisateur pourra ensuite rentrer l'IP et le port du serveur dans l'interface graphique du terminal.

> Le client écrit son mode verbose sur sa sortie `stderr`. Faire `make runclient` lance le client en redirigeant `stderr` vers `/dev/null`. Si on veut pouvoir consulter la sortie verbose, on peut faire `make runclientverbose` qui écrira `stderr` dans le fichier `client.log` dans le répertoire courant.

Alternativement, on peut lancer le client manuellement après l'avoir compilé avec `bin/client 2>[nom du fichier ou rediriger le verbose]`

# Utilisation du client

L'utilisation est explicite jusqu'a arriver dans une partie. A ce moment là, les contrôles sont:
- les flèches directionnelles pour se déplacer
- la touche m pour envoyer un message global
- la touche p pour envoyer un message privé
- la touche q pour quitter la partie

Une fois la touche p pressée, l'interface demandera d'abord de rentrer l'identifiant du joueur destinataire, puis le message.

# Architecture

## Serveur

Dans `MainServer`, une fois le client connecté, un thread avec le service `ClientHandler` est créé, il lui est assigné le socket TCP du client.

Chaque message est representé par une classe. Les messages que peut recevoir le serveur implémentent MenuMessage ou GameMessage, selon s'ils doivent être reçus avant ou après `START`. Ceux que le serveur peut envoyer implémentent ServerMessage.

Les classes implémentant MenuMessage et GameMessage ont une fabrique statique `parse(BufferedReader)` qui se charge de parser la requête et de stocker les informations, ainsi qu'une méthode méthode `executeRequest` qui se charge de traiter la requête.

Pour parser les requêtes du client, dans `parseMainMenuRequests`, nous lisons les 5 premiers caractères d'une requête, et nous créons l'objet Message correspondant par réflection, et nous appelons `executeRequest`,  l'objet message se chargera de changer les paramètres nécessaires.

Pour envoyer des messages au client, les classes implémentant ServerMessage ont une méthode `send(OutputStream)` qui se chargent d'envoyer les informations nécéssaires, passées à son constructeur.

Une fois une partie commencée, un thread `PlayerHandler` est assigné à chaque client par le `GameServer` correspondant à la partie. Celui ci parse les requêtes du client correspondant au jeu courant jusqu'à ce que celui-ci quitte ou la partie se finisse. Sa connection est ensuite terminée.

Pour le multicast, une classe  `MulticastGameServer` s'occupe de communiquer aux clients d'une partie.


## Client

Le client est écrit en C et utilise ncurses pour afficher une interface graphique dans le terminal. Il y a 6 fichiers c, chacuns accompagnés de leur headers dans le dossier `includes/`.

- `main.c` : le point de démarrage du programme. Demande une IP et un port pour se connecter au server. Appelle ensuite la fonction `gamelist` de `gamelist.c`.

- `gamelist.c` : se charge du menu principal du jeu. Elle permet de récupérer les parties disponibles, de pouvoir en créér de nouvelles ou de rejoindre une partie en attente de démarrage. Quand un joueur rejoint une partie (avec `NEWPL` ou `REGIS`), la fonction `lobby` de lobby.c est appelée.

- `lobby.c` : se charge du lobby d'une partie. Permet de rafraîchir la liste des joueurs connectés, de voir la taille du labyrinthe, de quitter le lobby (et retourner au menu principal) ou de se déclarer comme prêt. Une fois prêt, le client attends un message `WELCO`. Quand il l'a reçu, la fonction `maingame` de `game.c` est appelée.

- `maingame.c` : se charge de gérer la partie en cours. Gère l'affichage du labyrinthe, du chat et de la liste des joueurs. Une fois la partie finie, une fenêtre annoncant le vainqueur ainsi que son score s'ouvre. Il faut ensuite appuyer sur entrée pour quitter le jeu.
Les sockets UDP et multicast sont non bloquants, et sont vérifiés a chaque tour de la boucle principale. Un thread séparé s'occupe d'envoyer un message `GLIS?` toutes les secondes, afin de rafraîchir la liste des joueurs ainsi que l'affichage des joueurs dans le labyrinthe. L'accès au socket TCP est protégé par un mutex.

- `protocol.c` : ici résident une très grande majorité des fonctions qui gèrent l'envoi et la réception des messages du protocole.

- `utils.c` : fichier utilitaire comportant des fonctions utiles à plusieurs endroits dans le jeu.