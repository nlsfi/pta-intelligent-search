package fi.maanmittauslaitos.pta.search.api.region;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.ImmutableMap;
import fi.maanmittauslaitos.pta.search.api.Language;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegionNameContainer {
    private final Map<Language, Stemmer> stemmers;
    private Map<RegionType, List<String>> regionNamesByRegionType;
    private Map<Language, Map<String, String>> stemmedRegionNames;
    private String countryResource;
    private String regionResource;
    private String subRegionResource;
    private String municipalityResource;

    public enum RegionType {
        COUNTRY("country"),
        REGION("region"),
        SUBREGION("subregion"),
        MUNICIPALITY("municipality");

        private final String type;

        RegionType(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }
    }

    public RegionNameContainer(String countryResource, String regionResource, String subRegionResource, String municipalityResource, Map<Language, Stemmer> stemmers) {
        this.countryResource = countryResource;
        this.regionResource = regionResource;
        this.subRegionResource = subRegionResource;
        this.municipalityResource = municipalityResource;
        this.stemmers = stemmers;
    }

    public void init() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectReader listReader = objectMapper.readerFor(new TypeReference<List<String>>() {
        });

        Map<Language, List<String>> countries = readRegionResource(objectMapper, listReader, this.getClass().getClassLoader().getResource(countryResource));
        Map<Language, List<String>> regions = readRegionResource(objectMapper, listReader, this.getClass().getClassLoader().getResource(regionResource));
        Map<Language, List<String>> subregions = readRegionResource(objectMapper, listReader, this.getClass().getClassLoader().getResource(subRegionResource));
        Map<Language, List<String>> municipalities = readRegionResource(objectMapper, listReader, this.getClass().getClassLoader().getResource(municipalityResource));

        Map<Language, Map<String, String>> toFinnishRegionNameMap = new HashMap<>();
        Stream.of(countries, regions, subregions, municipalities).forEach(languageListMap -> languageListMap.forEach((lang, regionNames) -> {
            for (int i = 0; i < regionNames.size(); i++) {
                toFinnishRegionNameMap.computeIfAbsent(lang, asd -> new HashMap<>()).putIfAbsent(regionNames.get(i), languageListMap.get(Language.FI).get(i));
            }
        }));

        ImmutableMap<RegionType, Map<Language, List<String>>> regionNamesByRegionTypeAndLanguage = ImmutableMap.of(
                RegionType.COUNTRY, countries,
                RegionType.REGION, regions,
                RegionType.SUBREGION, subregions,
                RegionType.MUNICIPALITY, municipalities);

        this.regionNamesByRegionType = regionNamesByRegionTypeAndLanguage.entrySet().stream()//
                .collect(Collectors.toMap(Map.Entry::getKey, langRegionsEntry -> langRegionsEntry.getValue().get(Language.FI)));

        this.stemmedRegionNames = regionNamesByRegionTypeAndLanguage.values().stream()
                .flatMap(languageSetMap -> languageSetMap.entrySet().stream())//
                .collect(Collectors.groupingBy(Map.Entry::getKey))//
                .entrySet().stream()//
                .collect(Collectors.toMap(Map.Entry::getKey, languageRegionsEntry -> languageRegionsEntry.getValue().stream().map(Map.Entry::getValue).flatMap(Collection::stream)
                        .distinct()//
                        .collect(Collectors.toMap(region -> stemmers.get(languageRegionsEntry.getKey()).stem(region), region -> toFinnishRegionNameMap.get(languageRegionsEntry.getKey()).get(region)))));
    }

    private Map<Language, List<String>> readRegionResource(ObjectMapper objectMapper, ObjectReader listReader, URL resource) {
        Map<Language, List<String>> map = new HashMap<>();
        try {
            JsonNode jsonFile = objectMapper.readTree(resource);
            JsonNode features = jsonFile.get("features");
            features.forEach(feature -> {
                JsonNode properties = feature.get("properties");
                //TODO: Figure out what to do with the synonyms
                map.computeIfAbsent(Language.FI, lang -> new ArrayList<>()).add(properties.get("nimi").textValue());
                map.computeIfAbsent(Language.EN, lang -> new ArrayList<>()).add(properties.get("nimi").textValue());
                map.computeIfAbsent(Language.SV, lang -> new ArrayList<>()).add(properties.get("namn").textValue());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }


    public Map<RegionType, List<String>> getRegionNamesByRegionType() {
        return regionNamesByRegionType;
    }

    public Map<Language, Map<String, String>> getStemmedRegionNames() {
        return stemmedRegionNames;
    }
}
