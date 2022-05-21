# GhostLab 10

# Compilation et exécution

---

## Serveur

On peut compiler le serveur avec `make server` et l'exécuter avec `make runserver`, il démarrera sur le port 1337.

- ### Verbose

> Le mode verbose peut être activé en mettant une valeur quelconque à la variable d'environnemment `VERBOSE`.

## Client

Également avec `make client` et `make runclient`. L'utilisateur pourra ensuite rentrer l'IP et le port du serveur dans l'interface graphique du terminal.

# Architecture

---

## Messages

Chaque message est representé par une classe, l'hiérarchie ressemble à ceci, il existe 2 interfaces, MenuMessage et GameMessage, et 2 classes abstraites, ServerMessage et MovementMessage.



Les deux interfaces ont une méthode `executeRequest`.

## Serveur

Dans `MainServer`, une fois le client connecté, un thread avec le service `ClientHandler` est créé, il lui est assigné le socket TCP du client.

 

Pour parser les requêtes du client, dans `parseMainMenuRequests`, nous lisons les 5 premiers caractères d'une requête, et nous créons l'objet Message correspondant par réflection, et nous appelons `executeRequest`,  l'objet message se chargera de changer les paramètres nécessaires.



Une fois connecté à une partie, un `PlayerHandler` lui est assigné dans un autre thread par le `GameServer` correspondant au lobby du joueur, et parse les requêtes du client correspondant au jeu courant jusqu'à ce que celui-ci quitte ou la partie se finisse. Sa connection est ensuite terminée.



Pour le multicast, une classe  `MulticastGameServer` s'occupe de communiquer aux clients d'une partie.


## Client
