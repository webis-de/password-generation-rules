package de.aitools.aq.passwords.rules;

import java.util.function.Function;

import de.aitools.aq.passwords.util.Parameters;

/**
 * A function that selects characters at specific positions in a string.
 *
 * @author matthew.heinz@uni-weimar.de
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date: 2016/11/18 12:05:41 $
 */
public class CharacterIndicesFilter implements Function<String, String> {

  private final int[] indices;

  private final boolean outputDuplicates;

  private final boolean roundRobin;

  /**
     * Creates a new CharacterFilter.
     * @param indices Indices that point to specific characters in each input
     * with 0 being the first character and -1 the last
     * @param outputDuplicates If it should output the same character (at the
     * same position) several times, if it is specified several times by the
     * indices
     * @param roundRobin If indices are just taken modulo the input length
     * (<tt>true</tt>) or if indices referring to characters before the first
     * or after the last character are ignored (<tt>false</tt>)
     */
  public CharacterIndicesFilter(final int[] indices,
      final boolean outputDuplicates, final boolean roundRobin)
  throws NullPointerException, IllegalArgumentException {
    if (indices == null) {
      throw new NullPointerException();
    }
    this.indices = indices;
    this.outputDuplicates = outputDuplicates;
    this.roundRobin = roundRobin;
  }

  @Override
  public String apply(final String input) throws NullPointerException {
    if (input.isEmpty()) {
      return "";
    }

    final int tokenLength = input.length();
    final boolean[] indexBlacklist = new boolean[tokenLength];
    final char[] filtered = new char[this.indices.length * 2];
    int i = 0;
    for (int index : this.indices) {
      if (this.roundRobin || (index >= -tokenLength && index < tokenLength)) {

        int realIndex = index % tokenLength;
        if (realIndex < 0) {
          realIndex += tokenLength;
        } // % != modulo

        if (!indexBlacklist[realIndex]) {
          filtered[i] = input.charAt(realIndex);
          if (!this.outputDuplicates) {
            indexBlacklist[realIndex] = true;
          }
          ++i;
        }
      }
    }

    if (i == 0) {
      return "";
    } else {
      return new String(filtered, 0, i);
    }
  }

  /**
   * Creates a new filter using a plus-separated list of index strings (see
   * {@link Parameters#indexStringToInt(String)) as indices.
   * @param indicesConfiguration String containing the indices separated by
   * pluses (+)
   * @param outputDuplicates If it should output the same character (at the
   * same position) several times, if it is specified several times by the
   * indices
   * @param roundRobin If indices are just taken modulo the input length
   * (<tt>true</tt>) or if indices referring to characters before the first
   * or after the last character are ignored (<tt>false</tt>)
   * @return The filter
   * @throws NullPointerException if the indices configuration is <tt>null</tt>
   * @throws IllegalArgumentException If the indices configuration is not valid
   */
  public static CharacterIndicesFilter create(
      final String indicesConfiguration,
      final boolean outputDuplicates, final boolean roundRobin)
  throws NullPointerException, IllegalArgumentException {
    final String[] parts = indicesConfiguration.split("\\+");
    final int[] indices = new int[parts.length];
    for (int i = 0; i < parts.length; ++i) {
      indices[i] = Parameters.indexStringToInt(parts[i].trim());
    }
    return new CharacterIndicesFilter(indices, outputDuplicates, roundRobin);
  }

}
