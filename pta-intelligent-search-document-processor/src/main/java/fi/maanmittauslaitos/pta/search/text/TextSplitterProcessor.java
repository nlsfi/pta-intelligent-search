package fi.maanmittauslaitos.pta.search.text;

import java.util.ArrayList;
import java.util.List;

public class TextSplitterProcessor implements TextProcessor {

	private static final String PATTERN = "(?U)(?!-\\w)\\W+";
	private static final String HYPHEN = "-";
	private final Boolean joinHyphened;

	public TextSplitterProcessor(Boolean joinHyphened) {
		this.joinHyphened = joinHyphened;
	}

	public TextSplitterProcessor() {
		this.joinHyphened = false;
	}

	@Override
	public List<String> process(List<String> input) {
		return joinHyphened ? processWithJoining(input) : processWithoutJoining(input);
	}

	private List<String> processWithJoining(List<String> input) {
		List<String> ret = new ArrayList<>();

		for (String str : input) {
			String [] words = str.split(PATTERN);

			for (String w : words) {
				if (w == null || w.length() == 0) continue;

				if (w.contains(HYPHEN)) {
					String[] wordParts = w.split(HYPHEN);
					ret.add(String.join("", wordParts));
				}

				ret.add(w);
			}
		}

		return ret;
	}

	private List<String> processWithoutJoining(List<String> input) {
		List<String> ret = new ArrayList<>();

		for (String str : input) {
			String [] words = str.split(PATTERN);

			for (String w : words) {
				if (w == null || w.length() == 0) continue;

				ret.add(w);
			}
		}

		return ret;
	}


}
