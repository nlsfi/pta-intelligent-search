package fi.maanmittauslaitos.pta.search.api.region;

import fi.maanmittauslaitos.pta.search.api.Language;

import java.util.Map;

public class RegionNameSearchResult {
    public static final RegionNameSearchResult NO_REGION_FOUND = new RegionNameSearchResult(false, "", "");
    private boolean hasRegionName;
    private String regionalQueryPart;
    private String parsedRegion;


    public static RegionNameSearchResult executeSearch(String queryTerm, String stemmedQueryTerm, RegionNameContainer regionNameContainer, Language lang) {
        Map<String, String> stemmedRegionNames = regionNameContainer.getStemmedRegionNames().get(lang);
        if (stemmedRegionNames.containsKey(stemmedQueryTerm)) {
            return new RegionNameSearchResult(true, queryTerm, stemmedRegionNames.get(stemmedQueryTerm));
        }
        return NO_REGION_FOUND;
    }


    private RegionNameSearchResult(boolean hasRegionName, String regionalQueryPart, String parsedRegion) {
        this.hasRegionName = hasRegionName;
        this.regionalQueryPart = regionalQueryPart;
        this.parsedRegion = parsedRegion;
    }

    public boolean hasRegionName() {
        return hasRegionName;
    }

    public String getRegionalQueryPart() {
        return regionalQueryPart;
    }

    public String getParsedRegion() {
        return parsedRegion;
    }
}
