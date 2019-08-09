#!/bin/bash

curl -XPUT 'localhost:9200/pta' -H 'Content-Type: application/json' -d @../pta-intelligent-search-elasticsearch/src/main/resources/index.json
