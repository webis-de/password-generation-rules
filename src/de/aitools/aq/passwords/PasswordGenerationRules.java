package de.aitools.aq.passwords;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import de.aitools.aq.passwords.rules.CharacterIndicesFilter;
import de.aitools.aq.passwords.rules.CharacterSets;
import de.aitools.aq.passwords.rules.EveryNthTokenFilter;
import de.aitools.aq.passwords.rules.TokenReplacements;
import de.aitools.aq.passwords.rules.Tokenizer;
import de.aitools.aq.passwords.util.ApplyOnList;

/**
 * Utility class for composing password generation rules. Contains a main method
 * for applying generation rules to text-files with one sentence per line.
 * @see #create(String)
 * 
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date: 2016/11/18 12:05:41 $
 */
public class PasswordGenerationRules {

  private PasswordGenerationRules() { }

  /**
   * Creates a new password generation rule.
   * @param charset Configuration parameter for the character set (see
   * {@link CharacterSets#getConverter(String)})
   * @param replacement Configuration parameter for the token replacements (see
   * {@link TokenReplacements#getListReplacer(String)})
   * @param nthToken Configuration parameter for selecting the nth token for the
   * password (see {@link EveryNthTokenFilter#create(String)})
   * @param charIndices Configuration parameter for selecting the characters of
   * each token for the password (see
   * {@link CharacterIndicesFilter#create(String, boolean, boolean)},
   * outputDuplicates and roundRobin are false for this version)
   * @param addSpacesBetweenCharacters If spaces should be added between the
   * password characters at the end (as needed for n-gram counting) 
   * @return The rule
   * @throws NullPointerException If one of the parameters is <tt>null</tt>
   * @throws IllegalArgumentException If one of the parameters has an illegal
   * value
   */
  public static Function<String, String> create(
      final String charset,
      final String replacement,
      final String nthToken,
      final String charIndices,
      final boolean addSpacesBetweenCharacters)
  throws NullPointerException, IllegalArgumentException {
     return CharacterSets.getConverter(charset)
       .andThen(new Tokenizer())
       .andThen(TokenReplacements.getListReplacer(replacement))
       .andThen(EveryNthTokenFilter.create(nthToken))
       .andThen(new ApplyOnList<>(CharacterIndicesFilter.create(
           charIndices, false, false)))
       .andThen(input -> join(input, addSpacesBetweenCharacters));
  }

  /**
   * Creates a new password generation rule.
   * @param args The parameters of
   * {@link #create(String, String, String, String, boolean)}, starting at index
   * start
   * @param start The first index in args to treat as parameter
   * @return The rule
   * @throws NullPointerException If one of the parameters is <tt>null</tt>
   * @throws IllegalArgumentException If one of the parameters has an illegal
   * value, or the number of parameters is less than 4 or more than 5
   */
  public static Function<String, String> create(
      final String[] args, final int start)
  throws NullPointerException, IllegalArgumentException {
    final int numArgs = args.length - start;
    if (numArgs < 4 || numArgs > 5) {
      throw new IllegalArgumentException(
          "Invalid number of arguments: " + numArgs);
    }
    
    final String charset = args[start + 0];
    final String replacement = args[start + 1];
    final String nthToken = args[start + 2];
    final String charIndices = args[start + 3];
    final boolean addSpacesBetweenCharacters =
        numArgs == 4 ? false : Boolean.parseBoolean(args[start + 4]);
    
    return PasswordGenerationRules.create(
        charset, replacement, nthToken, charIndices,
        addSpacesBetweenCharacters);
  }

  /**
   * Applies a password generation rule to a string and returns the current
   * state of the string after each step.
   * @param charset Configuration parameter for the character set (see
   * {@link CharacterSets#getConverter(String)})
   * @param replacement Configuration parameter for the token replacements (see
   * {@link TokenReplacements#getListReplacer(String)})
   * @param nthToken Configuration parameter for selecting the nth token for the
   * password (see {@link EveryNthTokenFilter#create(String)})
   * @param charIndices Configuration parameter for selecting the characters of
   * each token for the password (see
   * {@link CharacterIndicesFilter#create(String, boolean, boolean)},
   * outputDuplicates and roundRobin are false for this version)
   * @return The string after every steo
   * @throws NullPointerException If one of the parameters is <tt>null</tt>
   * @throws IllegalArgumentException If one of the parameters has an illegal
   * value
   */
  public static List<String> applyInSteps(
      final String mnemonic,
      final String charset,
      final String replacement,
      final String nthToken,
      final String charIndices)
  throws NullPointerException, IllegalArgumentException {
    final List<String> outputs = new ArrayList<>();

    String current = CharacterSets.getConverter(charset).apply(mnemonic);
    List<String> currents = new Tokenizer().apply(current);
    outputs.add(joinSpacesBetweenTokens(currents));
    
    currents = TokenReplacements.getListReplacer(replacement).apply(currents);
    outputs.add(joinSpacesBetweenTokens(currents));
    
    currents = EveryNthTokenFilter.create(nthToken).apply(currents);
    outputs.add(joinSpacesBetweenTokens(currents));
    
    currents = new ApplyOnList<>(CharacterIndicesFilter.create(
        charIndices, false, false)).apply(currents);
    outputs.add(join(currents, false));

    return outputs;
  }
  
  private static String joinSpacesBetweenTokens(final List<String> inputs) {
    final StringBuilder output = new StringBuilder();
    for (final String input : inputs) {
      if (output.length() > 0) {
        output.append(' ');
      }
      output.append(input);
    }
    return output.toString();
  }
  
  private static String join(final List<String> inputs,
      final boolean spacesBetweenCharacters) {
    final StringBuilder output = new StringBuilder();
    for (final String input : inputs) {
      output.append(input);
    }
    
    if (spacesBetweenCharacters) {
      return output.toString().replaceAll("(.)", "$1 ").trim();
    } else {
      return output.toString();
    }
  }
  
  public static void printParameters(final PrintStream out) {
    out.println("<character set> <replacement> <word> <character position> "
        + "[<add spaces between characters>]");
  }

  public static void printParametersHelp(final PrintStream out) {
    out.println("  <character set>");
    out.println("    Specifies the character set to which the input is cast.");
    out.println("    Either '" + CharacterSets.CHARSET_ASCII + "' or '"
        + CharacterSets.CHARSET_LOWERCASE_LETTERS + "'");
    out.println("  <replacement>");
    out.println("    Specifies whether to replace certain character sequences.");
    out.println("    Either '" + TokenReplacements.REPLACE_NONE + "' or '"
        + TokenReplacements.REPLACE_WORD_PREFIXES + "'");
    out.println("  <word>");
    out.println("    Specifies which tokens to take.");
    out.println("    Possible values: 'every', 'every2nd', ...");
    out.println("  <character position>");
    out.println("    Specifies which characters to take from each token.");
    out.println("    Possible values:");
    out.println("      '1st', '2nd', ...");
    out.println("      'last', '2ndlast', ...");
    out.println("      '1st+2nd', ..., '1st+last', ..., '1st+2nd+3rd', ...");
    out.println("  <add spaces between characters>");
    out.println("    Either 'false' (default) or 'true' (add one space ");
    out.println("    between each pair of character of the output passwords)");
  }
  
  public static void printHelp(final PrintStream out) {
    out.println("Usage:");
    out.print("  <input> <output> "); printParameters(out);
    out.println("Where:");
    out.println("  <input>");
    out.println("    A file with one input string per line.");
    out.println("  <output>");
    out.println("    Output file which will contain one output string per line.");
    printParametersHelp(out);
  }
  
  public static void main(final String[] args) throws IOException {
    if (args.length < 6 || args.length > 7) {
      printHelp(System.err);
      System.exit(1);
    }

    Function<String, String> rule = null;
    try {
      rule = PasswordGenerationRules.create(args, 2);
    } catch (final IllegalArgumentException e) {
      System.err.println(e.getMessage());
      printHelp(System.err);
      System.exit(1);
    }

    final Iterator<String> passwords =
        Files.lines(new File(args[0]).toPath()).map(rule).iterator();
    try (final BufferedWriter writer =
        new BufferedWriter(new FileWriter(args[1]))) {
      while (passwords.hasNext()) {
        writer.write(passwords.next());
        writer.write('\n');
      }
    }
  }

}
