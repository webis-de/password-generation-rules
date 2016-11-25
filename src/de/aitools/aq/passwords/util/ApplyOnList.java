package de.aitools.aq.passwords.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Function that applies an inner function to each element of a list.
 * 
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date: 2016/11/18 12:05:41 $
 */
public class ApplyOnList<T,R> implements Function<List<T>, List<R>> {
  
  private final Function<T,R> innerFunction;
  
  /**
   * Creates a new function.
   * @param innerFunction The inner function to use on every element of the
   * list.
   * @throws NullPointerException If the inner function is <tt>null</tt>
   */
  public ApplyOnList(final Function<T,R> innerFunction)
  throws NullPointerException {
    if (innerFunction == null) { throw new NullPointerException(); }
    this.innerFunction = innerFunction;
  }

  @Override
  public List<R> apply(final List<T> inputs) {
    final List<R> outputs = new ArrayList<>(inputs.size());
    for (final T input : inputs) {
      outputs.add(this.innerFunction.apply(input));
    }
    return outputs;
  }
  
}
