package fi.maanmittauslaitos.pta.search.api;

import fi.maanmittauslaitos.pta.search.api.language.LanguageDetectionResult;
import fi.maanmittauslaitos.pta.search.api.language.LanguageDetector;
import fi.maanmittauslaitos.pta.search.api.model.SearchQuery;
import fi.maanmittauslaitos.pta.search.api.model.SearchResult;
import fi.maanmittauslaitos.pta.search.api.model.SearchResult.QueryLanguage;
import fi.maanmittauslaitos.pta.search.api.model.SearchResult.QueryLanguageScore;
import fi.maanmittauslaitos.pta.search.api.search.HakuKone;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@OpenAPIDefinition(
		info = @Info(
				title = "PTA Intelligent Search API",
				version = "1.0", description = "", license = @License(name = "", url = ""), contact = @Contact(url = "", name = "", email = "")
		)
)
@Tag(name = "search api", description = "Search for something")
@RestController
public class HakuController {
	
	private static Logger logger = Logger.getLogger(HakuController.class);
	
	@Autowired
	private HakuKone hakukone;
	
	@Autowired
	private LanguageDetector languageDetector;
	
	@Autowired
	@Qualifier("PreferredLanguages")
	private List<Language> languagesInPreferenceOrder;

	@Operation(summary = "Search stuff", description = "Search for things here")
	@ApiResponses(value={
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = SearchResult.class))))
	})
	@RequestMapping(value = "/v1/search", method = RequestMethod.POST)
	public SearchResult hae(@Parameter(description="Request body") @RequestBody SearchQuery pyynto, @Parameter(description="The language of the search terms can be specified using this parameter, if left empty the language will be deduced") @RequestParam("X-CLIENT-LANG") Optional<String> lang) throws IOException
	{
		String defaultLanguageStr = languagesInPreferenceOrder.get(0).toString();
		
		Language languageHint = sanitizeLanguage(lang.orElse(defaultLanguageStr));
		
		Language forcedLanguage = null;
		if (pyynto.getQueryLanguage() != null) {
			forcedLanguage = sanitizeLanguage(pyynto.getQueryLanguage());
		}
		
		Language language;
		
		Tuple<Language, LanguageDetectionResult> detection = deduceLanguage(pyynto, languageHint);
		
		if (forcedLanguage == null ) {
			language = detection.v1();
		} else {
			language = forcedLanguage;
		}
		
		logger.debug("Querying in language: "+language);
		
		SearchResult tulos = hakukone.haku(pyynto, language);
		
		QueryLanguage queryLanguage = createQueryLanguage(language, detection.v1(), detection.v2());
		tulos.setQueryLanguage(queryLanguage);
		
		return tulos;
	}

	private QueryLanguage createQueryLanguage(Language used, Language deduced, LanguageDetectionResult ldr) {
		QueryLanguage ret = new QueryLanguage();
		ret.setUsed(used.toString());
		ret.setDeduced(deduced.toString());
		ret.setScores(new ArrayList<>());
		for (Language lang : ldr.getPotentialLanguages()) {
			QueryLanguageScore qls = new QueryLanguageScore();
			qls.setLanguage(lang.toString());
			qls.setScore(ldr.getScoreForLanguage(lang));
			ret.getScores().add(qls);
		}
		return ret;
	}

	private Tuple<Language, LanguageDetectionResult> deduceLanguage(SearchQuery pyynto, Language languageHint) {
		Language language;
		
		LanguageDetectionResult ldr = languageDetector.detectLanguage(pyynto.getQuery());
		
		List<Language> autoDetectedLanguages = ldr.getTopLanguages();
		
		if (autoDetectedLanguages.size() == 1) {
			language = autoDetectedLanguages.get(0);
			
		} else if (autoDetectedLanguages.size() > 1) {
			
			if (autoDetectedLanguages.contains(languageHint)) {
				// Prefer hinted language
				language = languageHint;
			} else {
				logger.debug("Conflict, multiple equally good languages detected. Query terms: "+pyynto.getQuery()+", language hint: "+languageHint+", detected languages: "+autoDetectedLanguages+". Choosing first language in preference order.");
				language = null;
				for (Language l : languagesInPreferenceOrder) {
					if (autoDetectedLanguages.contains(l)) {
						language = l;
						break;
					}
				}
				if (language == null) {
					logger.warn("None of the detected languages are in the configured language list?! Choosing first language from preference order: "+languagesInPreferenceOrder);
					language = languagesInPreferenceOrder.get(0);
				}
			}
			
			languageHint = autoDetectedLanguages.get(0);

		} else {
			language = languageHint;
		}
		
		return Tuple.tuple(language, ldr);
	}

	private Language sanitizeLanguage(String lang) {
		lang = lang.toUpperCase();
		if (lang.equals("SV")) {
			return Language.SV;
		}
		if (lang.equals("EN")) {
			return Language.EN;
		}
		
		return Language.FI;
	}
	
	
}
