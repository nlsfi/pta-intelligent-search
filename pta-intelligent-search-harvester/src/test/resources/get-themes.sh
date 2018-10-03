#!/bin/bash

# This script can be used to download new INSPIRE theme RDF file that is
# included in this repository in src/main/resources/inspire-theme.rdf.gz

wget -O inspire-theme.rdf 'http://www.paikkatietohakemisto.fi/geonetwork/srv/eng/thesaurus.download?ref=external.theme.inspire-theme'

