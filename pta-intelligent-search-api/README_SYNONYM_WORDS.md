# Synonym Words

The API is contains a list of words with synonyms. This list is provided as a csv file. Each row contains a list of words with equivalent meaning separated by commas. The list is primarily meant for synonyms that are not part of any official vocabulary, but are widely used and recognized e.g. organization names and abbreviations.

When the user uses a query term that has one or more synonyms, the synonym words are added as part of the query.

The file is loaded from
1. The file path pointed to by the system environment property SYNONYM_WORDS_FILE
2. The file "synonyms.csv" in the current directory of the JVM process
3. The resource "/synonyms.csv" from classpath (included in the repository)

Empty rows and leading and trailing white space will be ignored. The file encoding should be UTF-8.

