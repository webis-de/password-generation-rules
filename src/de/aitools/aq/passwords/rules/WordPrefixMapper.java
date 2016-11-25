package de.aitools.aq.passwords.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;


/**
 * A function that replaces word prefixes by single special characters using
 * a case-insensitive dictionary. The dictionary is located at
 * <tt>resources/de/aitools/aq/passwords/rules/word-prefix-map.txt</tt>.
 * <p>
 * Most prefixes are taken from
 * <a href="http://web.archive.org/web/20140817200254/http://blog.codinghorror.com/ascii-pronunciation-rules-for-programmers/">this url</a>.
 * </p>
 * @author matthew.heinz@uni-weimar.de
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date: 2016/11/18 12:05:41 $
 */
public class WordPrefixMapper implements Function<String, String> {
  
  /**
   * Name used to choose this function when several are available.
   */
  public static final String KEY = "word-prefix";

  private static final String MAP_RESSOURCE_NAME =
      "word-prefix-map.txt";

  private static final String MAP_RESSOURCE_MAPPING_SYMBOL = "<-";

  private static WordPrefixMapper INSTANCE = null;

  private List<PrefixMapping> prefixMappings;

  private WordPrefixMapper() {
    this.prefixMappings = new ArrayList<WordPrefixMapper.PrefixMapping>();
    this.readRessources();
  }

  /**
   * Gets the thread-safe instance of the {@link WordPrefixMapper}.
   * @return The singleton instance
   */
  public static WordPrefixMapper get() {
    if (INSTANCE == null) {
      synchronized (MAP_RESSOURCE_NAME) {
        if (INSTANCE == null) {
          INSTANCE = new WordPrefixMapper();
        }
      }
    }
    
    return INSTANCE;
  }

  private void readRessources() {
    try (final Scanner map =
        new Scanner(WordPrefixMapper.class.getResourceAsStream(
            WordPrefixMapper.MAP_RESSOURCE_NAME))) {
      while (map.hasNextLine()) {
        final String line = map.nextLine();
        final String[] splits = line.split("\\s+");

        assert WordPrefixMapper.MAP_RESSOURCE_MAPPING_SYMBOL.equals(
            splits[1]);

        final String symbol = splits[0];

        final List<String> prefixes = new ArrayList<String>(splits.length - 2);
        for (int p = 2; p < splits.length; ++p) {
          prefixes.add(splits[p]);
        }

        final PrefixMapping mapping = new PrefixMapping(symbol, prefixes);
        this.prefixMappings.add(mapping);
      }
    }
  }

  @Override
  public String apply(final String token) {
    if (token == null) { return null; }

    final String lowerCaseToken = token.toLowerCase();
    for (final PrefixMapping mapping : this.prefixMappings) {
      final String prefix = mapping.matchPrefix(lowerCaseToken);
      if (prefix != null) {
        return mapping.getSymbol() + token.substring(prefix.length());
      }
    }
    return token;
  }

  /**
   * Helper class to map the possible symbol names with their symbol.
   */
  private class PrefixMapping {

    private final String symbol;

    private final List<String> prefixes;

    public PrefixMapping(final String symbol, final List<String> prefixes)
    throws NullPointerException, IllegalArgumentException {
      this.prefixes = new ArrayList<String>(prefixes.size());
      if (symbol == null) {
        throw new NullPointerException();
      }
      if (prefixes.isEmpty()) {
        throw new IllegalArgumentException();
      }
      for (String name : prefixes) {
        this.prefixes.add(name.toLowerCase());
      }
      this.symbol = symbol;
    }

    public String getSymbol() {
      return this.symbol;
    }

    public String matchPrefix(final String input) {
      for (final String prefix : this.prefixes) {
        if (input.startsWith(prefix)) {
          return prefix;
        }
      }
      return null;
    }

  }

}
