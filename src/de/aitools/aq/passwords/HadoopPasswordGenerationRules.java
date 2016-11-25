package de.aitools.aq.passwords;

import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Function;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Class for applying {@link PasswordGenerationRules} on Hadoop.
 * 
 * Run without arguments to get the usage message.
 *
 * @author johannes.kiesel@uni-weimar.de
 * @version $Date: 2016/11/18 12:05:41 $
 *
 */
public class HadoopPasswordGenerationRules implements Tool {

  protected static final String CONFIGURATION_KEY =
      "password-generation-rules-config";

  protected static final String CONF_MIN_PASSWORD_LENGTH =
      "min-password-length";

  protected static final String CONF_MAX_PASSWORD_LENGTH =
      "max-password-length";

  private static final String CONFIGURATION_SEPARATOR =
      "_";

  private Configuration configuration;

  public HadoopPasswordGenerationRules() {
    this.configuration = new Configuration();
  }

  @Override
  public Configuration getConf() {
    return this.configuration;
  }

  @Override
  public void setConf(final Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public int run(final String[] args) throws Exception {
    if (args.length < 8 || args.length > 9) {
      printHelp(System.err);
      System.exit(1);
    }

    try {
      PasswordGenerationRules.create(args, 4);
    } catch (final IllegalArgumentException e) {
      System.err.println(e.getMessage());
      printHelp(System.err);
      System.exit(1);
    }

    final Path inputPath = new Path(args[0]);
    final Path outputPath = new Path(args[1]);
    final int minPasswordLength = Integer.parseInt(args[2]);
    final int maxPasswordLength = Integer.parseInt(args[3]);
    final StringBuilder configBuilder = new StringBuilder();
    for (int a = 4; a < args.length; ++a) {
      if (a > 4) {
        configBuilder.append(CONFIGURATION_SEPARATOR);
      }
      configBuilder.append(args[a]);
    }
    final String configuration = configBuilder.toString();

    final Configuration jobConf = new Configuration();
    jobConf.set(CONFIGURATION_KEY, configuration);
    jobConf.setInt(CONF_MIN_PASSWORD_LENGTH, minPasswordLength);
    jobConf.setInt(CONF_MAX_PASSWORD_LENGTH, maxPasswordLength);
    final Job job = Job.getInstance(
        jobConf, "password generator " + configuration);
    job.setJarByClass(HadoopPasswordGenerationRules.class);
    job.setMapperClass(PasswordGenerationRuleMapper.class);
    job.setNumReduceTasks(0);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    LazyOutputFormat.setOutputFormatClass(job, TextOutputFormat.class);

    FileInputFormat.addInputPath(job, inputPath);
    FileOutputFormat.setOutputPath(job, outputPath);

    // Run it
    return job.waitForCompletion(true) ? 0 : 1;
  }
  
  public static void printHelp(final PrintStream out) {
    out.println("Usage:");
    out.print("  <input> <output> ");
    PasswordGenerationRules.printParameters(out);
    out.println("Where:");
    out.println("  <input>");
    out.println("    Files or directory of files with one input string per");
    out.println("    line.");
    out.println("  <output>");
    out.println("    Output directory which will contain files with one");
    out.println("    password per line. The file names will contain");
    out.println("    \"length-<length>\" and the files will contain only");
    out.println("    passwords with <length> characters.");
    out.println("  <min-password-length>");
    out.println("    Minimum password length to consider the password.");
    out.println("  <max-password-length>");
    out.println("    Maximum password length to consider the password.");
    PasswordGenerationRules.printParametersHelp(out);
  }

  public static void main(final String[] args) throws Exception {
    System.exit(ToolRunner.run(
        new Configuration(), new HadoopPasswordGenerationRules(), args));
  }

  public static class PasswordGenerationRuleMapper
  extends Mapper<LongWritable, Text, Text, Text> {
    
    private static final String COUNTER_GROUP = "passwords";

    private Function<String, String> rule;
    
    private int minLength;
    
    private int maxLength;

    private MultipleOutputs<Text, Text> output;

    private int cntr;

    public PasswordGenerationRuleMapper() {
      this.rule = null;
      this.minLength = 0;
      this.maxLength = 0;
      this.output = null;
      this.cntr = 0;
    }

    @Override
    protected void setup(final Context context)
    throws IOException, InterruptedException {
      final Configuration conf = context.getConfiguration();
      final String configuration = conf.get(CONFIGURATION_KEY);
      
      final String[] args = configuration.split(CONFIGURATION_SEPARATOR);
      this.rule = PasswordGenerationRules.create(args, 0);
      this.minLength = conf.getInt(CONF_MIN_PASSWORD_LENGTH, 1);
      this.maxLength = conf.getInt(CONF_MAX_PASSWORD_LENGTH, Integer.MAX_VALUE);
      this.output = new MultipleOutputs<Text, Text>(context);
    }

    @Override
    public void map(
        final LongWritable key, final Text value, final Context context)
    throws IOException, InterruptedException {
      final String password = this.rule.apply(value.toString());
      final int length = password.replaceAll("\\s", "").length();
      if (length >= this.minLength && length <= this.maxLength) {
        final String baseOutputPath = "length-" + length;
        context.getCounter(COUNTER_GROUP, baseOutputPath).increment(1);
        this.output.write(new Text(password), null, baseOutputPath);
      }

      ++this.cntr;
      if (this.cntr % 1000 == 0) {
        context.progress();
        // Make sure that the mapper is not killed while working
      }
    }

    @Override
    protected void cleanup(final Context context)
    throws IOException, InterruptedException {
      this.output.close();
    }
  }

}
