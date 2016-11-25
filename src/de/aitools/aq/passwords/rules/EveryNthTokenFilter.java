package de.aitools.aq.passwords.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import de.aitools.aq.passwords.util.Parameters;

/**
 * Function that chooses every nth element of a list.
 * 
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date: 2016/11/18 12:05:41 $
 */
public class EveryNthTokenFilter
implements Function<List<String>, List<String>> {

  private final int n;

  /**
   * Creates a new filter.
   * @param n The selection which elements to choose, starting with the first
   * @throws IllegalArgumentException If n is less than 1
   */
  public EveryNthTokenFilter(final int n) throws IllegalArgumentException {
    if (n < 1) {
      throw new IllegalArgumentException("Non-positive word count: " + n);
    }
    this.n = n;
  }

  @Override
  public List<String> apply(final List<String> inputs) {
    final List<String> outputs = new ArrayList<>((inputs.size()+1) / 2);
    
    int i = 0;
    for (final String input : inputs) {
      if (i % this.n == 0) {
        outputs.add(input);
      }
      ++i;
    }
    
    return outputs;
  }
  
  /**
   * Creates a new filter using given configuration as selection string
   * ({@link Parameters#selectionStringToInt(String)}). 
   * @param configuration The selection string
   * @return The filter
   * @throws NullPointerException If the configuration is <tt>null</tt> 
   * @throws IllegalArgumentException If the configuration is not a valid
   * selection string
   */
  public static Function<List<String>, List<String>> create(
      final String configuration)
  throws NullPointerException, IllegalArgumentException {
    final int selection = Parameters.selectionStringToInt(configuration);
    if (selection == 1) {
      return Function.identity();
    } else {
      return new EveryNthTokenFilter(selection);
    }
  }

}
