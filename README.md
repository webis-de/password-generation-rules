Password Generation Rules
=========================

Library and command line program to generate a passwords from sentences (so called mnemonic passwords). The input is either a file with one sentence per line or a directory of such files:

    java -cp password-generation-rules-bin.jar de.aitools.aq.passwords.PasswordGenerationRules --help

    java -cp password-generation-rules-bin.jar de.aitools.aq.passwords.PasswordGenerationRules sentences.txt output-passwords.txt lowercase-letters none every 1st

    hadoop jar password-generation-rules-bin.jar de.aitools.aq.passwords.HadoopPasswordGenerationRules --help

    hadoop jar password-generation-rules-bin.jar de.aitools.aq.passwords.HadoopPasswordGenerationRules input output 8 20 lowercase-letters none every 1st

When you use this software, cite it as
<pre>
Johannes Kiesel, Benno Stein, and Stefan Lucks.
A Large-scale Analysis of the Mnemonic Password Advice.
In Proceedings of the 24th Annual Network and Distributed System Security Symposium (NDSS 17),
February 2017. 
</pre>
[[bibtex](http://www.uni-weimar.de/medien/webis/publications/bibentries.php?bibkey=stein_2017a)]


Dependencies (packed into the password-generation-rules-bin.jar)
----------------------------------------------------------------
  - apache-hadoop-2.5.2
  - icu4j-53.1

