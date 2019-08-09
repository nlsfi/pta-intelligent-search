package fi.maanmittauslaitos.pta.search.api.region;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.ImmutableMap;
import fi.maanmittauslaitos.pta.search.api.Language;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegionNameContainer {

	private static Logger logger = Logger.getLogger(RegionNameContainer.class);

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

		RegionResource countries = readRegionResource(objectMapper, listReader, countryResource);
		RegionResource regions = readRegionResource(objectMapper, listReader, regionResource);
		RegionResource subregions = readRegionResource(objectMapper, listReader, subRegionResource);
		RegionResource municipalities = readRegionResource(objectMapper, listReader, municipalityResource);

		Map<Language, Map<String, String>> regionNameTranslator = getRegionNameTranslator(Arrays.asList(countries, regions, subregions, municipalities));


		ImmutableMap<RegionType, RegionResource> regionNamesByRegionTypeAndLanguage = ImmutableMap.of(
				RegionType.COUNTRY, countries,
				RegionType.REGION, regions,
				RegionType.SUBREGION, subregions,
				RegionType.MUNICIPALITY, municipalities);


		this.regionNamesByRegionType = regionNamesByRegionTypeAndLanguage.entrySet().stream()//
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getRegionNamesByLang().get(Language.FI)));


		this.stemmedRegionNames = regionNamesByRegionTypeAndLanguage.values().stream()
				.flatMap(regionResource -> Stream.concat(regionResource.getRegionNamesByLang().entrySet().stream(),
						regionResource.getSynonymsByRegionName().values().stream().flatMap(languageListMap -> languageListMap.entrySet().stream())))
				.collect(Collectors.groupingBy(Map.Entry::getKey))//
				.entrySet().stream()//
				.collect(Collectors.toMap(Map.Entry::getKey, languageRegionsEntry -> languageRegionsEntry.getValue().stream().map(Map.Entry::getValue).flatMap(Collection::stream)
						.distinct()//
						.collect(Collectors.toMap(region -> stemmers.get(languageRegionsEntry.getKey()).stem(region), region -> regionNameTranslator.get(languageRegionsEntry.getKey()).get(region)))));


	}

	private Map<Language, Map<String, String>> getRegionNameTranslator(List<RegionResource> regionResources) {
		Map<Language, Map<String, String>> toFinnishRegionNameMap = new HashMap<>();

		// Build dictionary from other languages to finnish
		regionResources.stream()//
				.map(RegionResource::getRegionNamesByLang)//
				.forEach(languageListMap -> languageListMap.forEach((lang, regionNames) -> {
					for (int i = 0; i < regionNames.size(); i++) {
						toFinnishRegionNameMap.computeIfAbsent(lang, ignored -> new HashMap<>()).putIfAbsent(regionNames.get(i), languageListMap.get(Language.FI).get(i));
					}
				}));

		// Build dictionary from all synonyms to finnish base region name
		regionResources.stream()
				.map(RegionResource::getSynonymsByRegionName)
				.collect(Collectors.toList())
				.forEach(synonymMap -> synonymMap
						.forEach((regionName, synonymsByLang) ->
								synonymsByLang
										.forEach((lang, synonyms) -> synonyms
												.forEach(synonym -> toFinnishRegionNameMap.computeIfAbsent(lang, ignored -> new HashMap<>()).putIfAbsent(synonym, regionName)))
						));

		return toFinnishRegionNameMap;
	}

	private RegionResource readRegionResource(ObjectMapper objectMapper, ObjectReader listReader, String resource) {
		Map<Language, List<String>> regionNamesByLang = new HashMap<>();
		Map<String, Map<Language, List<String>>> synonymsByRegionName = new HashMap<>();
		try {
			JsonNode jsonFile = objectMapper.readTree(this.getClass().getClassLoader().getResource(resource));
			JsonNode features = jsonFile.get("features");
			features.forEach(feature -> {
				JsonNode properties = feature.get("properties");
				String finnishRegionName = properties.get("nimi").textValue();


				try {
					JsonNode synonyms_fi = properties.get("synonyms_fi");
					synonymsByRegionName.put(finnishRegionName, ImmutableMap.of(Language.FI, listReader.readValue(synonyms_fi),
							Language.EN, listReader.readValue(properties.get("synonyms_en")),
							Language.SV, listReader.readValue(properties.get("synonyms_sv"))));
				} catch (IOException e) {
					logger.error("Could not read synonyms", e);
				}

				regionNamesByLang.computeIfAbsent(Language.FI, lang -> new ArrayList<>()).add(finnishRegionName);
				regionNamesByLang.computeIfAbsent(Language.EN, lang -> new ArrayList<>()).add(finnishRegionName);
				regionNamesByLang.computeIfAbsent(Language.SV, lang -> new ArrayList<>()).add(properties.get("namn").textValue());
			});
		} catch (IOException e) {
			logger.error("Could not read resource file as json " + resource, e);
		}
		assert !regionNamesByLang.isEmpty();
		return RegionResource.create(regionNamesByLang, synonymsByRegionName);
	}


	public Map<RegionType, List<String>> getRegionNamesByRegionType() {
		return regionNamesByRegionType;
	}

	public Map<Language, Map<String, String>> getStemmedRegionNames() {
		return stemmedRegionNames;
	}


	static class RegionResource {
		Map<Language, List<String>> regionNamesByLang;
		Map<String, Map<Language, List<String>>> synonymsByRegionName;

		static RegionResource create(Map<Language, List<String>> regionNamesByLang, Map<String, Map<Language, List<String>>> regionNameSynonymsByLang) {
			return new RegionResource(regionNamesByLang, regionNameSynonymsByLang);
		}

		private RegionResource(Map<Language, List<String>> regionNamesByLang, Map<String, Map<Language, List<String>>> synonymsByRegionName) {
			this.regionNamesByLang = regionNamesByLang;
			this.synonymsByRegionName = synonymsByRegionName;
		}

		public Map<Language, List<String>> getRegionNamesByLang() {
			return regionNamesByLang;
		}

		public Map<String, Map<Language, List<String>>> getSynonymsByRegionName() {
			return synonymsByRegionName;
		}
	}
}
