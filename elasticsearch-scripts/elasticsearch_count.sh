#!/bin/bash

curl -s -XGET 'localhost:9200/catalog/_count?pretty' | jq .count

