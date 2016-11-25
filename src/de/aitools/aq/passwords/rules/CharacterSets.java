package de.aitools.aq.passwords.rules;

import java.text.Normalizer;
import java.util.function.Function;

/**
 * Utility class for converting strings between character sets.
 * 
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date: 2016/11/18 12:05:41 $
 */
public class CharacterSets {

  /**
   * Flag for using the {@link #CHARSET_ASCII_CONVERTER}.
   */
  public static final String CHARSET_ASCII =
      "ascii";

  /**
   * Flag for using the {@link #CHARSET_LOWERCASE_LETTERS_CONVERTER}.
   */
  public static final String CHARSET_LOWERCASE_LETTERS =
      "lowercase-letters";

  /**
   * Function that calls {@link String#toLowerCase()}.
   */
  public static final Function<String, String> LOWERCASE_MAPPER =
      input -> input == null ? null : input.toLowerCase();

  /**
   * Function that calls {@link #stripNonLettersOrSpaces(String)}.
   */
  public static final Function<String, String> STRIP_NON_LETTERS_OR_SPACES =
      input -> CharacterSets.stripNonLettersOrSpaces(input);

  /**
   * Function that calls {@link #stripNonAscii(String)}.
   */
  public static final Function<String, String> STRIP_NON_ASCII =
      input -> CharacterSets.stripNonAscii(input);

  /**
   * Function that calls {@link #stripControlChars(String)}.
   */
  public static final Function<String, String> STRIP_CONTROL_CHARS =
      input -> CharacterSets.stripControlChars(input);

  /**
   * Function that calls {@link #stripCombiningMarks(String)}.
   */
  public static final Function<String, String> STRIP_COMBINING_MARKS =
      input -> CharacterSets.stripCombiningMarks(input);

  /**
   * Function that calls {@link #canonicalDecomposition(String)}.
   */
  public static final Function<String, String> CANONICAL_DECOMPOSITION =
      input -> CharacterSets.canonicalDecomposition(input);

  /**
   * Function that calls {@link #compatibilityDecomposition(String)}.
   */
  public static final Function<String, String> COMPATIBILITY_DECOMPOSITION =
      input -> CharacterSets.compatibilityDecomposition(input);


  /**
   * Function that calls {@link #asciiDictionaryMapping(String)}.
   */
  public static final Function<String, String> ASCII_DICTIONARY_MAPPING =
      input -> CharacterSets.asciiDictionaryMapping(input);

  /**
   * Function that converts input strings to 7-bit visible US-ASCII using
   * {@link #asciiDictionaryMapping(String)}, unicode decomposition, and
   * removing what remains (including control characters).
   * Returns <tt>null</tt> if the input is also <tt>null</tt>.
   */
  public static final Function<String, String>
  CHARSET_ASCII_CONVERTER =
      ASCII_DICTIONARY_MAPPING
        .andThen(COMPATIBILITY_DECOMPOSITION)
        .andThen(CANONICAL_DECOMPOSITION)
        .andThen(STRIP_NON_ASCII)
        .andThen(STRIP_CONTROL_CHARS);

  /**
   * Function that converts input strings to ones with only the 26 lowercase
   * latin letters and space characters using
   * {@link #asciiDictionaryMapping(String)}, unicode decomposition, conversion
   * to lowercase, and removing what remains (including control characters).
   * Returns <tt>null</tt> if the input is also <tt>null</tt>.
   */
  public static final Function<String, String>
  CHARSET_LOWERCASE_LETTERS_CONVERTER =
      ASCII_DICTIONARY_MAPPING
      .andThen(COMPATIBILITY_DECOMPOSITION)
      .andThen(CANONICAL_DECOMPOSITION)
      .andThen(STRIP_NON_LETTERS_OR_SPACES)
      .andThen(LOWERCASE_MAPPER);

  private CharacterSets() { }

  /**
   * Gets the character set converter for given configuration string.
   * @param configuration One of {@link #CHARSET_ASCII} and
   * {@link #CHARSET_LOWERCASE_LETTERS}
   * @return The converter
   * @throws NullPointerException If the configuration is <tt>null</tt>
   * @throws IllegalArgumentException If the configuration has none of the
   * values mentioned above
   */
  public static Function<String, String> getConverter(
      final String configuration)
  throws NullPointerException, IllegalArgumentException {
    if (configuration == null) { throw new NullPointerException(); }
    switch (configuration) {
    case CHARSET_ASCII:
      return CHARSET_ASCII_CONVERTER;
    case CHARSET_LOWERCASE_LETTERS:
      return CHARSET_LOWERCASE_LETTERS_CONVERTER;
    }
    throw new IllegalArgumentException(
        "No valid character set:" + configuration);
  }

  /**
   * Removes all characters that are not letters or space characters
   * (space, tab, newline).
   * @param input The string to process
   * @return The processed string or <tt>null</tt> if input is <tt>null</tt>
   */
  public static String stripNonLettersOrSpaces(final String input)  {
    if (input == null) { return null; }
    return input.replaceAll("[^a-zA-Z \t\n]", "");
  }

  /**
   * Removes all characters with a code above 127.
   * @param input The string to process
   * @return The processed string or <tt>null</tt> if input is <tt>null</tt>
   */
  public static String stripNonAscii(final String input)  {
    if (input == null) { return null; }
    return input.replaceAll("[^\\p{ASCII}]", "");
  }

  /**
   * Removes all control characters.
   * @param input The string to process
   * @return The processed string or <tt>null</tt> if input is <tt>null</tt>
   */
  public static String stripControlChars(final String input) {
    if (input == null) { return null; }
    return input.replaceAll("\\p{Cntrl}", "");
  }

  /**
   * Removes all combining diacritical marks. Combine with
   * {@link #canonicalDecomposition(String)} to remove all diacritics.
   * @param input The string to process
   * @return The processed string or <tt>null</tt> if input is <tt>null</tt>
   */
  public static String stripCombiningMarks(final String input) {
    if (input == null) { return null; }
    return input.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  }

  /**
   * Decomposes letters with accents, dots, bars and so on.
   * The accents are added as combining-mark in front of or after the base
   * letter. You can use {@link #stripCombiningMarks(String)} to remove them.
   * @param input The string to process
   * @return The processed string or <tt>null</tt> if input is <tt>null</tt>
   */
  public static String canonicalDecomposition(final String input) {
    if (input == null) { return null; }
    return Normalizer.normalize(input, Normalizer.Form.NFD);
  }

  /**
   * Replaces combined symbols with their parts.
   * E.g., 'ellipses' to '...'.
   * @param input The string to process
   * @return The processed string or <tt>null</tt> if input is <tt>null</tt>
   */
  public static String compatibilityDecomposition(final String input) {
    if (input == null) { return null; }
    return Normalizer.normalize(input, Normalizer.Form.NFKC);
  }

  /**
   * Converts characters which are not converted by the
   * compatibility and canonical decomposition. Due to the nature of things,
   * the list is incomplete. The dictionary contains rules for the latin1 ASCII
   * supplement block and the 'euro' currency symbol.
   * @param input The string to process
   * @return The processed string or <tt>null</tt> if input is <tt>null</tt>
   */
  public static String asciiDictionaryMapping(final String input) {
    if (input == null) { return null; }
    final StringBuilder output = new StringBuilder(input.length());
    final int numChars = input.length();
    for (int i = 0; i < numChars; ++i)
    {
      final char c = input.charAt(i);
      switch (c)
      {
        // latin1 conversion
        case '\u00A2':
          output.append("Cent");
          break;
        case '\u00A3':
          output.append("Pound");
          break;
        case '\u00A5':
          output.append("Yen");
          break;
        case '\u00A6':
          output.append('|');
          break;
        case '\u00A9':
          output.append('C');
          break;
        case '\u00AB':
          output.append('"');
          break;
        case '\u00AE':
          output.append('R');
          break;
        case '\u00B1':
          output.append("+-");
          break;
        case '\u00B5':
          output.append("mu");
          break;
        case '\u00BB':
          output.append('"');
          break;
        case '\u00BC':
          output.append("1/4");
          break;
        case '\u00BD':
          output.append("1/2");
          break;
        case '\u00BE':
          output.append("3/4");
          break;
        case '\u00C6':
          output.append("AE");
          break;
        case '\u00D0':
          output.append('D');
          break;
        case '\u00D7':
          output.append('x');
          break;
        case '\u00D8':
          output.append('O');
          break;
        case '\u00DE':
          output.append("Th"); // capital thorn
          break;
        case '\u00DF':
          output.append("ss");
          break;
        case '\u00E6':
          output.append("ae");
          break;
        case '\u00F0':
          output.append('d');
          break;
        case '\u00F7':
          output.append('/');
          break;
        case '\u00F8':
          output.append('o');
          break;
        case '\u00FE':
          output.append("th"); // lowercase thorn
          break;
        // Other conversions
        case '\u20AC':
          output.append("Euro");
          break;
        default:
          output.append(c);
          break;
      }
    }
    return output.toString();
  }

}
