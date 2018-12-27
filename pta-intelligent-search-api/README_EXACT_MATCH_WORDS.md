# Exact Match Words

The API uses a list of words that need to be matched exactly as-is or not at all. This list is provided as a plain text file with one word per row. This file is loaded at runtime and changes to the file require restarting the API.

When the user uses a query term that matches exactly (not case sensitive) a word in this list, the free text matching will be done as a term query instead of a fuzzy search. 

Note that this list only affects the fuzzy matching of words within text. The words in the configured list are translated into ontological terms (if a term for it exists) and matched against the index as normal.

The file is loaded from
1. The file path pointed to by the system environment property EXACT_MATCH_FILE
2. The file "exact_match_words.txt" in the current directory of the JVM process
3. The resource "/exact_match_words.txt" from classpath (included in the repository)

Empty rows, leading and trailing white space and any commented text (content after #) will be ignored. The file encoding should be UTF-8.

