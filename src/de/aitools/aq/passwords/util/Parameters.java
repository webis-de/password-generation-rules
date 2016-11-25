package de.aitools.aq.passwords.util;

/**
 * Utility class for parsing String parameters.
 *
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date: 2016/11/18 12:05:41 $
 *
 */
public class Parameters {
  
  private static final String KEYWORD_FROM_BACK = "last";
  
  private static final String KEYWORD_SELECTION = "every";
  
  private Parameters() {}

  /**
   * Parses a string that specifies an index. Valid strings are
   * 1st, 2nd, 3rd, 4th, ..., or 1stlast, 2ndlast, 3rdlast, 4thlast, ..., or
   * last. Strings with last are mapped to negative values. String without last
   * are mapped to their number - 1 (as int indices start with 0).
   * @param intString The input
   * @return The parsed index
   * @throws NullPointerException If the input is null
   * @throws IllegalArgumentException If the input is not valid
   */
  public static int indexStringToInt(final String intString)
  throws NullPointerException, IllegalArgumentException {
    final boolean isNegative = intString.endsWith(KEYWORD_FROM_BACK);

    if (isNegative && intString.length() == KEYWORD_FROM_BACK.length()) {
      return -1;
    }

    final int suffixStart = isNegative
        ? intString.length() - 2 - KEYWORD_FROM_BACK.length() // stlast, ndlast or rdlast
        : intString.length() - 2; // st, nd or rd
    if (suffixStart < 1
        || !Character.isLetter(intString.charAt(suffixStart))
        || !Character.isLetter(intString.charAt(suffixStart + 1))) {
      throw new IllegalArgumentException(
          "Index strings must end in a two-letter number suffix, such a suffix "
          + "followed by \"last\", or be \"last\", "
          + "but given string does not: " + intString);
    }

    final String numberString = intString.substring(0, suffixStart);
    final int number = Integer.parseInt(numberString);
    if (number < 1) {
      throw new IllegalArgumentException(intString);
    }
    
    if (isNegative) {
      return -number;
    } else {
      return number - 1;
    }
  }

  /**
   * Parses a string that specifies which elements of an enumeration should be
   * used. Valid strings are "every", "every 1st", "every 2nd", ....
   * @param intString The input
   * @return The parsed selection
   * @throws NullPointerException If the input is null
   * @throws IllegalArgumentException If the input is not valid
   */
  public static int selectionStringToInt(final String intString)
  throws NullPointerException, IllegalArgumentException {
    if (!intString.startsWith(KEYWORD_SELECTION)) {
      throw new IllegalArgumentException("Selection strings must start with \""
          + KEYWORD_SELECTION + "\", but given string does not: " + intString);
    }
    
    if (intString.equals(KEYWORD_SELECTION)) {
      return 1;
    } else {
      final String indexString =
          intString.substring(KEYWORD_SELECTION.length()).trim();
      final int index = Parameters.indexStringToInt(indexString);
      if (index < 0) {
        throw new IllegalArgumentException(
            "Selection strings must contain a positive index or be \""
            + KEYWORD_SELECTION + "\", but given string is not: " + intString);
      }
      return index + 1;
    }
    
    
  }

}
