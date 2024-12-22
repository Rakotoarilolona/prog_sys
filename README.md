# prog_sys
transfert de fichier

#configuration
  #coté serveur
    ouvrez config.txt
    
    IP_SERVEUR : ip du serveur principale
    PORT_PRINCIPALE_RECUP : port de recuperation du serveur principale
    PORT_PRINCIPALE_PUT : celui de l'upload
    
    ISANY : nombre de sous-serveur
    
    IP_1 : ip du sous-serveur 1
    IP_2 : ip du sous-serveur 1
    ...
    IP_n : ip du sous-serveur n
    
    PORT_1 : port du sous-serveur 1 de get
    ...
    PORT_n : port du sous-serveur n de get
    
    PORT_PUT_1 : port du sous-serveur 1 de put
    ...
    PORT_PUT_5 : port du sous-serveur n de put
    
    PATH_RECUP_1 : lieu de stockage des parties 1 pour le sous-serveur 1
    ...
    PATH_RECUP_n : lieu de stockage des parties 1 pour le sous-serveur n

    IP_CLIENT : ip du client
    PORT_CLIENT : port du client
    
  #coté client

    IP_CLIENT : ip du client
    PORT_CLIENT : port du client
    PATH : lieu de stockage du fichier pour get
    
#utilisation
  #coté serveur
    dans execution= ./run_serv.sh
                    ./run_serv_secondaire.sh
  
  #coté client
    java Client
    choisissez le mode de transfet get/put
    si get=tappez le nom du fichier a telecharger
    si put=tappez le chemin absolu du fichier a uploader
