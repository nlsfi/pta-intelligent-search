#!/bin/bash

N=$1
if [ "$N" = "" ]; then
  N=1
fi

curl -XGET "http://localhost:9200/pta/_search?size="$N"&pretty" -H 'Content-Type: application/json' -d' { }'

