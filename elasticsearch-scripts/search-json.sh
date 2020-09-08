#!/bin/bash

curl -XGET "http://localhost:9200/pta/metadata/_search?size=1&pretty" -H 'Content-Type: application/json' -d @$1

