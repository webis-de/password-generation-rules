package de.aitools.aq.passwords.rules;

import java.util.List;
import java.util.function.Function;

import de.aitools.aq.passwords.util.ApplyOnList;

/**
 * Utility class for replacing single tokens in a string.
 * 
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date: 2016/11/18 12:05:41 $
 */
public class TokenReplacements {

  /**
   * Flag for using the identity function.
   */
  public static final String REPLACE_NONE =
      "none";

  /**
   * Flag for using a {@link WordPrefixMapper}.
   */
  public static final String REPLACE_WORD_PREFIXES =
      "word-prefixes";

  /**
   * Function that does no replacement at all.
   */
  public static final Function<List<String>, List<String>>
  REPLACER_IDENTITY = Function.identity();

  /**
   * Function that uses the {@link WordPrefixMapper} on every String.
   */
  public static final Function<List<String>, List<String>>
  REPLACER_WORD_PREFIXES =
  new ApplyOnList<String, String>(WordPrefixMapper.get());
  
  private TokenReplacements() { }

  /**
   * Gets the token replacer for a list of strings for given configuration
   * string.
   * @param configuration One of {@link #REPLACE_NONE} and
   * {@link #REPLACE_WORD_PREFIXES}
   * @return The replacer
   * @throws NullPointerException If the configuration is <tt>null</tt>
   * @throws IllegalArgumentException If the configuration has none of the
   * values mentioned above
   */
  public static Function<List<String>, List<String>> getListReplacer(
      final String configuration)
  throws NullPointerException, IllegalArgumentException {
    if (configuration == null) { throw new NullPointerException(); }
    switch (configuration) {
    case REPLACE_NONE:
      return REPLACER_IDENTITY;
    case REPLACE_WORD_PREFIXES:
      return REPLACER_WORD_PREFIXES;
    }
    throw new IllegalArgumentException(
        "No valid replacement configuration: " + configuration);
  }

}
