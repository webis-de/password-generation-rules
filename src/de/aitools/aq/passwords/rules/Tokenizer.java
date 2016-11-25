package de.aitools.aq.passwords.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import com.ibm.icu.text.BreakIterator;

/**
 * A function that tokenizes strings.
 * 
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date: 2016/11/18 12:05:41 $
 */
public class Tokenizer implements Function<String, List<String>> {

  private final Locale locale;

  /**
   * Create a new {@link Tokenizer} for English.
   */
  public Tokenizer() {
    this(Locale.ENGLISH);
  }

  /**
   * Create a new {@link Tokenizer} for given locale.
   * @param locale The locale to use
   */
  public Tokenizer(final Locale locale) {
    if (locale == null) { throw new NullPointerException(); }
    this.locale = locale;
  }

  @Override
  public List<String> apply(final String input) {
    if (input == null) { return null; }
    final List<String> tokens = new ArrayList<>();
    
    final BreakIterator iterator = BreakIterator.getWordInstance(locale);
    iterator.setText(input);

    int begin = iterator.first();
    int end = iterator.next();
    while (end != BreakIterator.DONE) {
      final String token = input.substring(begin, end).trim();
      if (!token.isEmpty()) {
        tokens.add(token);
      }
      begin = end;
      end = iterator.next();
    }

    return tokens;
  }

}
