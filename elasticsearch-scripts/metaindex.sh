#!/bin/bash

curl -XPUT 'localhost:9200/pta' -H 'Content-Type: application/json' -d'
{
  "mappings": {
    "metadata": {
      "properties": {
        "abstract_uri":           { "type": "keyword" },
        "keywords_uri":           { "type": "keyword" },
        "annotated_keywords_uri": { "type": "keyword" },
        "abstract_maui_uri":      { "type": "keyword" },
        "keywords":               { "type": "keyword" },
        "keywordsInspire":        { "type": "keyword" },
        "topicCategories":        { "type": "keyword" },
        "distributionFormats":    { "type": "keyword" }
      }
    }
  }
}'

