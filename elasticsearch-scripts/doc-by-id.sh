#!/bin/bash

curl -XGET "http://localhost:9200/catalog/doc/"$1"?pretty"
