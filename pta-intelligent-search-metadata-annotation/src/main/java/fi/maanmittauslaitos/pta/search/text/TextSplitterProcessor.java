package fi.maanmittauslaitos.pta.search.text;

import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TextSplitterProcessor implements TextProcessor {

	private static final String PATTERN = "(?U)(?!-\\w)\\W+";
	private static final String HYPHEN = "-";
	private final Stemmer stemmer;
	private final boolean joinHyphened;
	private final Collection<String> wellKnownPostfixes;

	private TextSplitterProcessor(Stemmer stemmer, Collection<String> wellKnownPostfixes, boolean joinHyphened) {
		this.stemmer = stemmer;
		this.wellKnownPostfixes = wellKnownPostfixes;
		this.joinHyphened = joinHyphened;
	}

	public static TextSplitterProcessor create(Stemmer stemmer, Collection<String> wellKnownPostfixes, boolean joinHyphened) {
		return new TextSplitterProcessor(stemmer, wellKnownPostfixes, joinHyphened);
	}

	@Override
	public List<String> process(List<String> input) {
		List<String> ret = new ArrayList<>();

		for (String str : input) {
			String[] words = str.split(PATTERN);

			for (String w : words) {
				if (w == null || w.length() == 0) continue;

				if (w.contains(HYPHEN)) {
					handleHyphened(ret, w);
				}

				ret.add(w);
			}
		}

		return ret;
	}

	private void handleHyphened(List<String> ret, String w) {
		String[] wordParts = w.split(HYPHEN);
		if (wellKnownPostfixes.contains(stemmer.stem(wordParts[wordParts.length - 1]))) {
			ret.addAll(Arrays.asList(wordParts));
		}

		if (joinHyphened) {
			ret.add(String.join("", wordParts));
		}
	}
}
