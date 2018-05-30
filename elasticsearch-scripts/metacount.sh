#!/bin/bash

curl -s -XGET 'localhost:9200/pta/_count?pretty' | jq .count

