#!/bin/sh -e

QUICKSTART_FOLDER=quickstart

me=`basename "$0"`

if [ ! -f gradlew ]; then
    echo "Please run this script from the project root using : ${QUICKSTART_FOLDER}/${me}"
    exit 1
fi

COMPOSE_CONFIG_FILE_OPTION="-f ${QUICKSTART_FOLDER}/docker-compose.yml"
EXPLORER_SERVICE=explorer

# Displays services list with port mapping
docker-compose ${COMPOSE_CONFIG_FILE_OPTION} ps

# Get individual port mapping for exposed services
rpcMapping=`docker-compose ${COMPOSE_CONFIG_FILE_OPTION} port rpcnode 8545`
wsMapping=`docker-compose ${COMPOSE_CONFIG_FILE_OPTION} port rpcnode 8546`
explorerMapping=`docker-compose ${COMPOSE_CONFIG_FILE_OPTION} port explorer 3000`

#Check if we run in a tty before using exec and otherwise set $TERM as it fails if not set.
if [ ! -t 1 ] ;
then
  export TERM=xterm-color
fi

# replaces the mix explorer rpc endpoint by ours
`docker-compose ${COMPOSE_CONFIG_FILE_OPTION} exec -T ${EXPLORER_SERVICE} /bin/sed -i \
"s/fallbackUrlPlaceHolder/http:\/\/localhost:${rpcMapping##*:}/g" \
src/constants/index.js`

`docker-compose ${COMPOSE_CONFIG_FILE_OPTION} exec -T ${EXPLORER_SERVICE} /bin/sed -i \
"s/rpcHostPlaceHolder/http:\/\/localhost:${rpcMapping##*:}/g" \
src/components/App.js`

# Displays links to exposed services
ORANGE='\033[0;33m'
CYAN='\033[0;36m'
BOLD=$(tput bold)
NORMAL=$(tput sgr0)

echo "${CYAN}****************************************************************"
echo "JSON-RPC ${BOLD}HTTP${NORMAL}${CYAN} service endpoint      : ${ORANGE}http://localhost:${rpcMapping##*:}${CYAN}   *"
echo "JSON-RPC ${BOLD}WebSocket${NORMAL}${CYAN} service endpoint : ${ORANGE}http://localhost:${wsMapping##*:}${CYAN}   *"

while [ "$(curl -m 1 -s -o /dev/null -w ''%{http_code}'' http://localhost:${explorerMapping##*:})" != "200" ]
do
  dots=$dots"."
  printf "${CYAN} Block explorer is starting, please wait ${ORANGE}$dots${NORMAL}\\r"
  sleep 1
done

echo "${CYAN}Web block explorer address          : ${ORANGE}http://localhost:${explorerMapping##*:}${CYAN}   *                                                                             "
echo "****************************************************************${NORMAL}"