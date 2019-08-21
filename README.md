# PTA Intelligent Search

This code provides an intelligent search for the Finnish Geospatial Platform. The codebase provides the following components:

* **harvester**: a command line tool to harvest metadata records from a CSW and index them into an elasticsearch instance
* **api**: REST based search API (spring boot web)

Libraries
* **document-processor**: A library to extract information from XML and transform it
* **elasticsearch**: contains named constants used both by the harvester and API
* **metadata-annotation**: document processor components that does automatic annotation (using maui) as well as matches words to ontologies using stemmers. These are used to index textual content as well-defined terms in the used ontology 
* **metadata-extractor**: document processor configuration for extracting data from CSW metadata responses
* **qa**: project for end-to-end elasticsearch tests (not done yet)


## Installation for development

### 1. Install voikko

The harvester uses the voikko library for analysing Finnish language. This is a native library with a Java wrapper, so your system must have voikko installed. Preferably version 4.1.1.

On ubuntu/debian based systems, this should be as easy as:

```
apt-get install libvoikko1 voikko-fi
```

### 2. Start elasticsearch

For the system to work, you need to run elasticsearch in localhost port 9200. The easiest way to do this is to use docker. To create and start the container run:

```
docker pull docker.elastic.co/elasticsearch/elasticsearch-oss:6.2.4
docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch-oss:6.2.4
```

If you stop this container (or reboot your computer), you no longer need to create a new container. Instead you can just start it via `docker start [container id]`

### 3. Harvesting data

To populate elasticsearch with data, you must first ensure that the index is set up correctly. 

```
cd /where/you/cloned/pta-intelligent-search/elasticsearch-script
./metaindex.sh
```

If you need to modify the index, you will probably need to first remove the old index via `./metaclean.sh` and then create the index via `./metaindex.sh`. This directory also contains some other handy scripts.

Once the index is set up, run the harvester class in pta-intelligent-search-harvester: `fi.maanmittauslaitos.pta.search.Harvester`. Usually the easiest way to do this is to run the class in an IDE where you have the all the projects.

This process harvests all records from the Finnish National Catalogue (paikkatietohakemisto.fi) and exits once done.

### 4. Run the search API

Run the class `fi.maanmittauslaitos.pta.search.api.Application` from the project pta-intelligent-search-api. Usually the easiest way to do this is to run the class in an IDE where you have the all the projects.

Open a browser window with the URL http://localhost:8080

## Testing

In project pta-intelligent-search-qa there are integration tests that depend on data generated in projects pta-intelligent-search-harvester and pta-intelligent-search-api
by using exec-maven-plugin. In order to run the tests, those projects should be compiled and packaged first. The simplest way verify that all is working is to run `mvn clean install` at the root of the project.

## Updating spatial regions

Databases (geoJSON files) containing the bounding boxes of Finnish municipalities, sub regions and regions 
are built using Statistical units data and Municipality name data.

Statistical Units, Statistics Finland The material was downloaded from Statistics 
Finland's interface service on 9 August 2019 with the licence [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/deed.en). 
Municipalities, Population Register Centre, the material was downloaded from Avoindata.fi on August 20 2019 with
the licence CC BY 4.0 (https://creativecommons.org/licenses/by/4.0/deed.en)

To update the databases with the newest data sets:
 * Make sure that you have Python3 environment with all packages installed from [requirements.txt](elasticsearch-data-processing-scripts/requirements.txt)
 * Download the newest municipality union data:
    * Download Excel file from [Kuntaliitto](https://www.kuntaliitto.fi/asiantuntijapalvelut/johtaminen-ja-kehittaminen/kuntaliitokset) and save it 
    to the project folder elasticsearch-data-processing-scripts
    * Modify the MUNICIPALITY_UNIONS_EXCEL constant from file `dataLoader.py` to match the new file name
* Download the newest municipality name data:
    * Download *Voimassa olevat Suomen kunnat* and *Lakanneet Suomen kunnat* csv files from [Avoindata.fi](https://www.avoindata.fi/data/fi/dataset/kunnat)
    to the project folder elasticsearch-data-processing-scripts
    * Modify the EXISTING_MUNICIPALITIES_CSV and DISCONTINUED_MUNICIPALITIES_CSV constants from file `dataLoader.py` to match the new file names
 * Run the Python script `dataLoader.py` from the project elasticsearch-data-processing-scripts and build the project.



 