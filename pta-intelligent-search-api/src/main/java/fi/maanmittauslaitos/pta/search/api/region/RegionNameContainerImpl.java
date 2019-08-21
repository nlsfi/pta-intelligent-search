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
import java.util.stream.Stream;

import static fi.maanmittauslaitos.pta.search.api.Language.*;
import static java.util.stream.Collectors.*;

public class RegionNameContainerImpl implements RegionNameContainer {

	private static Logger logger = Logger.getLogger(RegionNameContainerImpl.class);

	private final Map<Language, Stemmer> stemmers;
	private Map<RegionType, List<String>> regionNamesByRegionType;
	private Map<Language, Map<String, String>> stemmedRegionNames;
	private String countryResource;
	private String regionResource;
	private String subRegionResource;
	private String municipalityResource;

	public RegionNameContainerImpl(String countryResource, String regionResource, String subRegionResource, String municipalityResource, Map<Language, Stemmer> stemmers) {
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
				.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getRegionNamesByLang().get(FI)));


		this.stemmedRegionNames = regionNamesByRegionTypeAndLanguage.values().stream()
				.flatMap(regionResource -> Stream.concat(regionResource.getRegionNamesByLang().entrySet().stream(),
						regionResource.getSynonymsByRegionName().values().stream().flatMap(languageListMap -> languageListMap.entrySet().stream())))
				.collect(groupingBy(Map.Entry::getKey))//
				.entrySet().stream()//
				.collect(toMap(Map.Entry::getKey, languageRegionsEntry -> languageRegionsEntry.getValue().stream().map(Map.Entry::getValue).flatMap(Collection::stream)
						.distinct()//
						.collect(toMap(region -> stemmers.get(languageRegionsEntry.getKey()).stem(region), region -> regionNameTranslator.get(languageRegionsEntry.getKey()).get(region), (p, q) -> p))));
	}

	private Map<Language, Map<String, String>> getRegionNameTranslator(List<RegionResource> regionResources) {
		Map<Language, Map<String, String>> toFinnishRegionNameMap = new HashMap<>();

		// Build dictionary from other languages to finnish
		regionResources.stream()//
				.map(RegionResource::getRegionNamesByLang)//
				.forEach(languageListMap -> languageListMap.forEach((lang, regionNames) -> {
					for (int i = 0; i < regionNames.size(); i++) {
						toFinnishRegionNameMap.computeIfAbsent(lang, ignored -> new HashMap<>()).putIfAbsent(regionNames.get(i), languageListMap.get(FI).get(i));
					}
				}));

		// Build dictionary from all synonyms to finnish base region name
		regionResources.stream()
				.map(RegionResource::getSynonymsByRegionName)
				.collect(toList())
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
				String swedishRegionName = properties.get("namn").textValue();
				if (finnishRegionName.toLowerCase().equals("lahti")) {
					int i = 2;
				}


				try {
					List<String> symsFi = listReader.readValue(properties.get("synonyms_fi"));
					List<String> symsEn = listReader.readValue(properties.get("synonyms_en"));
					List<String> symsSv = listReader.readValue(properties.get("synonyms_sv"));
					synonymsByRegionName.put(finnishRegionName, ImmutableMap.of(
							FI, symsFi,
							EN, Stream.of(symsFi, symsSv, symsEn, Collections.singletonList(swedishRegionName))
									.flatMap(Collection::stream).distinct().collect(toList()),
							SV, symsSv
					));
				} catch (IOException e) {
					logger.error("Could not read synonyms", e);
				}

				regionNamesByLang.computeIfAbsent(FI, lang -> new ArrayList<>()).add(finnishRegionName);
				regionNamesByLang.computeIfAbsent(EN, lang -> new ArrayList<>()).add(finnishRegionName);
				regionNamesByLang.computeIfAbsent(SV, lang -> new ArrayList<>()).add(swedishRegionName);
			});
		} catch (IOException e) {
			logger.error("Could not read resource file as json " + resource, e);
		}
		assert !regionNamesByLang.isEmpty();
		return RegionResource.create(regionNamesByLang, synonymsByRegionName);
	}


	@Override
	public Map<RegionType, List<String>> getRegionNamesByRegionType() {
		return regionNamesByRegionType;
	}

	@Override
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

		Map<Language, List<String>> getRegionNamesByLang() {
			return regionNamesByLang;
		}

		Map<String, Map<Language, List<String>>> getSynonymsByRegionName() {
			return synonymsByRegionName;
		}
	}
}
