#!/bin/bash

# Vérifier si le fichier contenant le nombre existe

if [ ! -f ../config/config.txt ]; then
    echo "Le fichier '../config/config.txt' est manquant. Veuillez le créer avec le format : ISANY : <nombre>"
    exit 1
fi

# Lire le nombre depuis le fichier

ISANY=$(grep -oP '(?<=ISANY : )\d+' ../config/config.txt)

# Vérifier si le nombre est valide
if [ -z "$ISANY" ]; then
    echo "Le fichier 'config.txt' est mal formaté. Il doit contenir : ISANY : <nombre>"
    exit 1
fi

# Lancer plusieurs instances de Client en arrière-plan, chaque instance avec un numéro spécifique
cd .. || exit 1

for ((i = 1; i <= ISANY; i++)); do
    java Serveur_secondaire_recup "$i" >/dev/null 2>&1 &
done

echo "Serveurs secondaires de recuperation au nombre de $ISANY démarrés en arrière-plan."