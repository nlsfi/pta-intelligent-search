#!/bin/bash

#curl 'localhost:9200/pta/48430c98-e372-4a4a-b707-d3feeb484dd1'

ID=$1
if [ "$ID" = "" ]; then
	ID=48430c98-e372-4a4a-b707-d3feeb484dd1
fi

curl "localhost:9200/pta/metadata/$ID" | jq .

