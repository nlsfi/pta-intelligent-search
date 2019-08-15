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
"""

# Download new municipality excel from
# https://www.kuntaliitto.fi/asiantuntijapalvelut/johtaminen-ja-kehittaminen/kuntaliitokset
# and modify the file name constant accordingly
MUNICIPALITY_UNIONS_EXCEL = "Kuntajakoselvitykset2005-2019_130519_0.xlsx"

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


def get_municipality_unions():
    # Please make sure if replacing file that the data scheme is similar
    cols = ['year', 'nmbr',
            'municipalities',
            'status',
            'commit_year', 'nmbr_decreased']

    df = pd.read_excel(MUNICIPALITY_UNIONS_EXCEL, sheet_name="Sheet3", header=1, encoding="utf-8")

    df.columns = cols
    df = df[df.status == 1]
    df = df[df.commit_year <= datetime.datetime.now().year]

    def municipality_splitter(line):
        parts = line.replace(" ja ", ",").replace(" ", "").replace("\xa0", "").split(",")
        return parts if parts else None

    # Remove all inside parenthesis
    df['municipalities'] = df.apply(
        lambda row: re.compile(r'\([^)]*\)').sub("", row.municipalities), axis=1)

    df['municipalities'] = df.apply(
        lambda row: [
            municipality for municipality in municipality_splitter(row.municipalities) if municipality
        ], axis=1)

    df["municiality"] = df.apply(lambda row: row.municipalities[0], axis=1)
    df["synonyms"] = df.apply(lambda row: set(row.municipalities[1:]), axis=1)

    # Multiple unions could happen into same municipality
    df = df[["municiality", "synonyms"]].groupby(df.municiality, as_index=True).agg(lambda x: reduce(set.union, x))

    municipalities = set(list(df.index))
    muns_to_remove = set()

    # Municipalities might join to others as well
    def synonyms_from_other_synonyms(row):
        intersection = municipalities & row.synonyms
        synonyms = row.synonyms
        for municipality in intersection:
            muns_to_remove.add(municipality)
            synonyms.add(municipality)
            synonyms = synonyms | df.loc[municipality].synonyms
        return list(synonyms)

    df['synonyms'] = df.apply(synonyms_from_other_synonyms, axis=1)

    return df.drop(muns_to_remove)


def create_df(fname, municipality_unions):
    df = gpd.read_file(fname)[ORIGINAL_RELEVANT_COLUMNS].to_crs(epsg="4326")
    df = df.assign(**df.bounds)
    df['envelope'] = df.apply(
        lambda row: '[ {:.2f}, {:.2f}, {:.2f}, {:.2f} ]'.format(row.minx, row.miny, row.maxx, row.maxy), axis=1)

    df['synonyms_fi'] = '[]'

    def get_synonyms(row):
        if row.nimi in municipality_unions.index:
            return str(municipality_unions.loc[row.nimi].synonyms)
        return '[]'

    df['synonyms_fi'] = df.apply(get_synonyms, axis=1)
    df['synonyms_sv'] = '[]'
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


if __name__ == '__main__':
    download_datasets()
