#!/bin/bash

curl -XPUT 'localhost:9200/catalog?pretty' -H 'Content-Type: application/json' -d'
{
    "settings" : {
        "index" : {
        }
    },
    "mappings" : {
        "_default_" : {
            "properties" : {
		"abstract_uri": { "type": "text" }
            }
        }
    }
}
'

