package com.udacity.webcrawler;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.stream.Collectors;



final class WordCounts {


  static Map<String, Integer> sort(Map<String, Integer> wordCounts, int popularWordCount) {


    return wordCounts.entrySet()
            .stream()
            .sorted(new WordCountComparator())
            .limit(popularWordCount)
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new));
  }

  /**
   * A {@link Comparator} that sorts word count pairs correctly:
   *
   * <p>
   * <ol>
   *   <li>First sorting by word count, ranking more frequent words higher.</li>
   *   <li>Then sorting by word length, ranking longer words higher.</li>
   *   <li>Finally, breaking ties using alphabetical order.</li>
   * </ol>
   */
  private static final class WordCountComparator implements Comparator<Map.Entry<String, Integer>> {
    @Override
    public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
      if (!a.getValue().equals(b.getValue())) {
        return b.getValue() - a.getValue();
      }
      if (a.getKey().length() != b.getKey().length()) {
        return b.getKey().length() - a.getKey().length();
      }
      return a.getKey().compareTo(b.getKey());
    }
  }

  private WordCounts() {
    // This class cannot be instantiated
  }
}