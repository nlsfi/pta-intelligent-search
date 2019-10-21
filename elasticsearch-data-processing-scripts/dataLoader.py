#!/usr/bin/env python
# -*- coding: utf-8 -*-

import datetime
import os
import re
from functools import reduce

import geopandas as gpd
import pandas as pd
from osgeo import ogr
from shapely.ops import unary_union

"""
Statistical Units, Statistics Finland The material was downloaded from Statistics Finland's
interface service on 9 August 2019 with the licence CC BY 4.0 (https://creativecommons.org/licenses/by/4.0/deed.en).

Municipalities, Population Register Centre, the material was downloaded from Avoindata.fi on August 20 2019 with
the licence CC BY 4.0 (https://creativecommons.org/licenses/by/4.0/deed.en)

Official Statistics of Finland (OSF): Municipal elections [e-publication].
ISSN=2323-1114. Helsinki: Statistics Finland [referred: 21.10.2019].
Access method: http://www.tilastokeskus.fi/til/kvaa/kvaa_2017-03-31_luo_001_fi.html
"""

# Download new municipality excel from
# https://www.kuntaliitto.fi/asiantuntijapalvelut/johtaminen-ja-kehittaminen/kuntaliitokset
# and modify the file name constant accordingly
MUNICIPALITY_UNIONS_EXCEL = "Kuntajakoselvitykset2005-2019_130519_0.xlsx"

MUNICIPALITY_UNION_STATS_URL = "http://www.tilastokeskus.fi/til/kvaa/kvaa_2017-03-31_luo_001_fi.html"

# Download the newest municipality information csv files from
# https://www.avoindata.fi/data/fi/dataset/kunnat
EXISTING_MUNICIPALITIES_CSV = "kuntaluettelo-laajat-tiedot-2018-01-01.csv"
DISCONTINUED_MUNICIPALITIES_CSV = "lakanneet-kunnat.csv"

ORIGINAL_RELEVANT_COLUMNS = ['nimi', 'namn', 'geometry']
RELEVANT_COLUMNS = ['nimi', 'namn', 'synonyms_fi', 'synonyms_sv', 'synonyms_en', 'envelope', 'geometry']

# Layers in 1: 1 000 000 contain english names as well
LAYERS = {
    'regions.json': 'tilastointialueet:maakunta4500k',
    'subregions.json': 'tilastointialueet:seutukunta4500k',
    'municipalities.json': 'tilastointialueet:kunta4500k'
}

TK_BASE_URL = "http://geo.stat.fi/geoserver/tilastointialueet/ows"


def download_layers_to_disk(layer, name):
    print("Downloading layer", name)
    driver = ogr.GetDriverByName("WFS")
    wfs = driver.Open("WFS:" + TK_BASE_URL)
    layer = wfs.GetLayerByName(layer)

    dr = ogr.GetDriverByName('GeoJSON')
    ds = dr.CreateDataSource(name)
    ds.CopyLayer(layer, 'local_copy')


def download_from_wfs():
    for name, layer in LAYERS.items():
        download_layers_to_disk(layer, name)


def get_municipality_names_dict():
    cols = ["KUNTANIMIFI", "KUNTANIMISV"]
    df = pd.concat([(pd.read_csv(EXISTING_MUNICIPALITIES_CSV, delimiter=";", encoding="utf-8")[cols]),
                    (pd.read_csv(DISCONTINUED_MUNICIPALITIES_CSV, delimiter=";", encoding="Windows-1252")[cols])])
    df.set_index(cols[0], inplace=True)
    return df.to_dict()[cols[1]]


def get_municipality_unions2():
    cols = ["synonym", "new", "date"]
    municipality = "municipality"
    synonyms = "synonyms"
    df = pd.read_html(MUNICIPALITY_UNION_STATS_URL, header=0, parse_dates=True)[0].dropna()[
        ["Lakkautettu kunta", "Uuden tai laajentuvan kunnan nimi", "Ajankohta"]]
    df.columns = cols

    def clean_cell(line):
        parts = line.split(" ")
        return " ".join(parts[1:]).replace(", osa", "").replace(" (osa)", "").replace(". osa", "")

    df[municipality] = df.apply(lambda row: clean_cell(row[1]) if len(row[1]) < 100 else None, axis=1)
    df[synonyms] = df.apply(lambda row: {clean_cell(row[0])}, axis=1)
    df[synonyms] = df.apply(lambda row: row[synonyms] if list(row[synonyms])[0] != row[municipality] else None, axis=1)
    return df[[municipality, synonyms]].dropna()


def get_municipality_unions():
    # Please make sure if replacing file that the data scheme is similar
    cols = ['year', 'nmbr',
            'municipalities',
            'status',
            'commit_year', 'nmbr_decreased']

    municipality = "municipality"
    synonyms = "synonyms"
    synonyms_sv = "synonyms_sv"

    df = pd.read_excel(MUNICIPALITY_UNIONS_EXCEL, sheet_name="Sheet3", header=1, encoding="utf-8")

    df.columns = cols
    df = df[df.status == 1]
    df = df[df.commit_year <= datetime.datetime.now().year]

    def municipality_splitter(line):
        parts = line.replace(" ja ", ",").replace(" ", "").replace("\xa0", "").split(",")
        return parts if parts else None

    # Remove all inside parenthesis
    df.municipalities = df.apply(
        lambda row: re.compile(r'\([^)]*\)').sub("", row.municipalities), axis=1)

    df.municipalities = df.apply(
        lambda row: [
            mun for mun in municipality_splitter(row.municipalities) if mun
        ], axis=1)

    df[municipality] = df.apply(lambda row: row.municipalities[0], axis=1)
    df[synonyms] = df.apply(lambda row: set(row.municipalities[1:]), axis=1)

    # Fetch and concat the similar data from another source
    df = df[[municipality, synonyms]].append(get_municipality_unions2(), ignore_index=True)

    # Multiple unions could happen into same municipality
    df = df[[municipality, synonyms]].groupby(df[municipality], as_index=True).agg(lambda x: reduce(set.union, x))

    municipalities = set(list(df.index))
    muns_to_remove = set()

    # Municipalities might join to others as well
    def synonyms_from_other_synonyms(row):
        intersection = municipalities & row.synonyms
        all_synonyms = row.synonyms
        for found_municipality in intersection:
            muns_to_remove.add(found_municipality)
            all_synonyms.add(found_municipality)
            all_synonyms = all_synonyms | df.loc[found_municipality].synonyms
        return list(all_synonyms)

    df[synonyms] = df.apply(synonyms_from_other_synonyms, axis=1)
    df = df.drop(muns_to_remove)
    mun_sv_dict = get_municipality_names_dict()

    df[synonyms_sv] = df.apply(lambda row: [mun_sv_dict.get(val, val) for val in row[synonyms]], axis=1)
    return df


def create_df(fname, municipality_unions):
    df = gpd.read_file(fname)[ORIGINAL_RELEVANT_COLUMNS].to_crs(epsg="4326")
    df = df.assign(**df.bounds)
    df['envelope'] = df.apply(
        lambda row: '[ {:.2f}, {:.2f}, {:.2f}, {:.2f} ]'.format(row.minx, row.miny, row.maxx, row.maxy), axis=1)

    df['synonyms_fi'] = '[]'

    def get_synonyms(row, col):
        if row.nimi in municipality_unions.index:
            return str(municipality_unions.loc[row.nimi][col])
        return '[]'

    df['synonyms_fi'] = df.apply(lambda row: get_synonyms(row, "synonyms"), axis=1)
    df['synonyms_sv'] = df.apply(lambda row: get_synonyms(row, "synonyms_sv"), axis=1)
    df['synonyms_en'] = '[]'
    df['bbox'] = df.envelope
    return df


def save_to_databases(out_dir, municipality_unions, save_complete_geometries):
    for fname in LAYERS.keys():
        df = create_df(fname, municipality_unions)
        if save_complete_geometries:
            df[RELEVANT_COLUMNS].to_file(os.path.join(out_dir, "well_known_locations_{}".format(fname)),
                                         driver='GeoJSON')
        df['geometry'] = df.bbox
        df[RELEVANT_COLUMNS].to_file(os.path.join(out_dir, "well_known_location_bboxes_{}".format(fname)),
                                     driver='GeoJSON')


def save_whole_finland(out_dir):
    empirically_found_buffer_that_gets_envelope_as_one_geom = 0.0248712
    df = gpd.read_file(os.path.join(out_dir, "well_known_location_bboxes_regions.json"))
    union = unary_union([row for row in df.buffer(empirically_found_buffer_that_gets_envelope_as_one_geom).envelope])
    df = gpd.geodataframe.GeoDataFrame({
        'nimi': "Suomi",
        'namn': "Finland",
        'synonyms_fi': '["Kansallinen", "Valtakunnallinen"]',
        'synonyms_sv': '["National", "Landsomfattande"]',
        'synonyms_en': '["National", "Nationwide"]',
        'geometry': union
    }, index=[0])
    df = df.assign(**df.bounds)
    df['envelope'] = df.apply(
        lambda row: '[ {:.2f}, {:.2f}, {:.2f}, {:.2f} ]'.format(row.minx, row.miny, row.maxx, row.maxy), axis=1)
    df['geometry'] = df.envelope
    df[RELEVANT_COLUMNS].to_file(os.path.join(out_dir, "well_known_location_bboxes_countries.json"), driver='GeoJSON')


def download_datasets():
    out_dir = "../pta-intelligent-search-elasticsearch/src/main/resources/data"
    municipality_unions = get_municipality_unions()
    download_from_wfs()
    save_to_databases(out_dir, municipality_unions, save_complete_geometries=False)
    save_whole_finland(out_dir)


def main():
    download_datasets()


if __name__ == '__main__':
    main()
