import os

import geopandas as gpd
from osgeo import ogr
from shapely.ops import unary_union

ORIGINAL_RELEVANT_COLUMNS = ['nimi', 'namn', 'geometry']
RELEVANT_COLUMNS = ['nimi', 'namn', 'synonyms_fi', 'synonyms_sv', 'synonyms_en', 'envelope', 'geometry']

# LAyers in 1: 1 000 000 contain english names as well
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


def create_df(fname):
    df = gpd.read_file(fname)[ORIGINAL_RELEVANT_COLUMNS].to_crs(epsg="4326")
    df = df.assign(**df.bounds)
    df['envelope'] = df.apply(
        lambda row: '[ {:.2f}, {:.2f}, {:.2f}, {:.2f} ]'.format(row.minx, row.miny, row.maxx, row.maxy), axis=1)
    df['synonyms_fi'] = ""
    df['synonyms_sv'] = ""
    df['synonyms_en'] = ""
    df['bbox'] = df.envelope
    return df


def save_to_databases(out_dir, save_complete_geometries):
    for fname in LAYERS.keys():
        df = create_df(fname)
        if save_complete_geometries:
            df[RELEVANT_COLUMNS].to_file(os.path.join(out_dir, "well_known_locations_{}".format(fname)),
                                         driver='GeoJSON')
        df['geometry'] = df['bbox']
        df[RELEVANT_COLUMNS].to_file(os.path.join(out_dir, "well_known_location_bboxes_{}".format(fname)),
                                     driver='GeoJSON')


def save_whole_finland(out_dir):
    empirically_found_buffer_that_gets_envelope_as_one_geom = 0.0248712
    df = gpd.read_file(os.path.join(out_dir, "well_known_location_bboxes_regions.json"))
    union = unary_union([row for row in df.buffer(empirically_found_buffer_that_gets_envelope_as_one_geom).envelope])
    df = gpd.geodataframe.GeoDataFrame({
        'nimi': "Suomi",
        'namn': "Finland",
        'synonyms_fi': "[Kansallinen, Valtakunnallinen]",
        'synonyms_sv': "[National, Landsomfattande]",
        'synonyms_en': "[National, Nationwide]",
        'geometry': union
    }, index=[0])
    df = df.assign(**df.bounds)
    df['envelope'] = df.apply(
        lambda row: '[ {:.2f}, {:.2f}, {:.2f}, {:.2f} ]'.format(row.minx, row.miny, row.maxx, row.maxy), axis=1)
    df['geometry'] = df.envelope
    df[RELEVANT_COLUMNS].to_file(os.path.join(out_dir, "well_known_location_bboxes_countries.json"), driver='GeoJSON')


def download_datasets():
    out_dir = "../pta-intelligent-search-elasticsearch/src/main/resources/data"
    download_from_wfs()
    save_to_databases(out_dir, save_complete_geometries=False)
    save_whole_finland(out_dir)


if __name__ == '__main__':
    download_datasets()
