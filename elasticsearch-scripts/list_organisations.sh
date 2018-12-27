#!/bin/bash

curl -XGET "http://localhost:9200/pta/_search?" -H 'Content-Type: application/json' -d' { "size": 0, "aggs": { "o": { "terms": { "size": 999, "field": "organisations.organisationName"} } } }' | jq .aggregations.o.buckets[].key -r

