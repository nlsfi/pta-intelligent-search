#!/bin/bash

curl -XPUT 'localhost:9200/pta' -H 'Content-Type: application/json' -d @../pta-intelligent-search-qa/src/test/resources/fi/maanmittauslaitos/pta/search/integration/index.json
