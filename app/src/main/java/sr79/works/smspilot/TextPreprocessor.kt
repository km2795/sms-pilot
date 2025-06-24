package sr79.works.smspilot

import org.tartarus.snowball.ext.porterStemmer

object TextPreprocessor {
    /**
     * Utility to perform word stemming and/or lemmatization along with general cleaning.
     * @param stem   Whether to apply stemming
     * @param lemma  Whether to apply lemmatization (not implemented, placeholder)
     * @param words  Input string or list of words
     * @param delim  Delimiter if input is a string
     * @return       Processed words as a string (if input was string) or list
     */
    fun processWord(stem: Boolean, lemma: Boolean, words: Any?, delim: String?): Any {
        if (words == null) return ""
        if (words is String) {
            val str = words
            if (str.trim { it <= ' ' }.isEmpty()) return ""
            val splitWords =
                if (delim != null) str.split(delim.toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray() else arrayOf(str)
            val processed = processList(stem, lemma, splitWords)
            return java.lang.String.join("", processed)
        } else if (words is List<*>) {
            val wordList = words as List<String>
            return processList(stem, lemma, wordList.toTypedArray<String>())
        }
        return ""
    }

    private fun processList(stem: Boolean, lemma: Boolean, words: Array<String>): List<String> {
        val processedWords: MutableList<String> = ArrayList()
        val stemmer = porterStemmer()
        for (word in words) {
            if (word == null || word.trim { it <= ' ' }.isEmpty()) continue
            var processed = word
            if (stem) {
                stemmer.current = processed
                stemmer.stem()
                processed = stemmer.current
            }
            // Lemmatization placeholder (Java has no built-in lemmatizer)
            if (lemma) {
                // processed = lemmatize(processed);
            }
            processedWords.add(processed)
        }
        return processedWords
    } // Placeholder for lemmatization
    // private static String lemmatize(String word) {
    //     // Implement or use a library if needed
    //     return word;
    // }
}