package com.websecuritylab.tools.fileinspector;
import org.apache.commons.cli.*;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;

public class CliOptions {

    public static final String HELP = "help";
    //public static final String CREATE = "create";
    public static final String INTERACTIVE = "interactive";
    public static final String PROP_FILE = "prop-file";
    //public static final String REPORT_JSON = "report-json";
    
    public static final String CFR_JAR = "cfr-jar";
    public static final String SOURCE_TYPE = "source-type";
    
    public static final String AUDIT_DIRECTORY = "audit-directory";
	public static final String AUDIT_DIR_PATH = "audit-dir-path";
    public static final String AUDIT_FILE = "audit-file";
	public static final String TEMP_DIR_PATH = "temp-dir-path";
 
    public static final String HAS_REGEX_FILE = "has-regex-file";
    public static final String REGEX_FILE = "regex-file";
    public static final String REGEX_STRING = "regex-string";
    
    
	//public static final String SOURCE_DIR = "source-directory";
    //public static final String SOURCE_FILE = "source-file";
    public static final String APP_NAME = "app-name";
//    public static final String CLASSPATH = "classpath";
//    public static final String SUBPACKAGES = "subpackages";
   
    public static final String KEEP_TEMP = "keep-temp";
	public static final String IS_LINUX = "is-linux";
    
    public static final String VERBOSE = "verbose";
    private static final PrintStream OUT = System.out;
    
    private CliOptions() {
    }

    public static final Options getOptions() {
        final Option helpOption = Option.builder("h")
                .required(false)
                .hasArg(false)
                .longOpt(HELP)
                .desc("Print help.")
                .build();
        final Option inputOption = Option.builder("i")
                .required(false)
                .hasArg(false)
                .longOpt(INTERACTIVE)
                .desc("Interactive input prompting for options to be used.  Entries will be saved in the DEFAULT properties file.")
                .build();
        final Option propFileOption = Option.builder("p")
                .required(false)
                .hasArg()
                .longOpt(PROP_FILE)
                .desc("Load options from specified properties file.")
                .build();
        final Option appNameOption = Option.builder("n")
                .required(false)
                .hasArg()
                .longOpt(APP_NAME)
                .desc("Required: Application Name that is appended to the HTML/JSON report names.")
                .build();         
        final Option cfrJarOption = Option.builder("j")
                .required(false)
                .hasArg()
                .longOpt(CFR_JAR)
                .desc("CFR Java Decompiler jar file name ( Ex: cfr.jar )")
                .build();
        
        
        final Option sourceTypeOption = Option.builder("t")
                .required(false)
                .hasArg()
                .longOpt(SOURCE_TYPE)
                .desc("Required: Type of java files to be scanned ([A]rchive file (WAR/EAR/JAR), [C]LASS files, [S]OURCE files.)")
                .build();         

        
        
        final Option auditDirectoryOption = Option.builder("d")
                .required(false)
                .hasArg()
                .longOpt(AUDIT_DIRECTORY)
                .desc("Required: Audit all files in a directory? ( [Y]es, [N]o )")
                .build();         
        final Option auditDirPathOption = Option.builder("p")
                .required(false)
                .hasArg()
                .longOpt(AUDIT_DIR_PATH)
                .desc("Required: Path to directory of file(s).")
                .build();         
        final Option auditFileOption = Option.builder("f")
                .required(false)
                .hasArg()
                .longOpt(AUDIT_FILE)
                .desc("Required: Name of the file to be audited.")
                .build(); 
        final Option tempDirPathOption = Option.builder("z")
                .required(false)
                .hasArg()
                .longOpt(TEMP_DIR_PATH)
                .desc("Required: Path to temp directory where file(s) were decompiled.")
                .build();         
        
        
        
        final Option hasRegexFileOption = Option.builder("r")
                .required(false)
                .hasArg(false)
                .longOpt(HAS_REGEX_FILE)
                .desc("Required: Load file with Regex search expression.  ")
                .build();         
        final Option regexFileOption = Option.builder("x")
                .required(false)
                .hasArg()
                .longOpt(REGEX_FILE)
                .desc("Required: Path to the file to with RegEx search patterns.")
                .build();         
        final Option regexStringOption = Option.builder("s")
                .required(false)
                .hasArg()
                .longOpt(REGEX_STRING)
                .desc("Required: Regex search expression.  For simple strings use | separator)")
                .build();         
        
        final Option keeptempOption = Option.builder("k")
                .required(false)
                .hasArg(false)
                .longOpt(KEEP_TEMP)
                .desc("Keep temporary extracted *.class and *.java files.")
                .build();    
        final Option isLinuxOption = Option.builder("l")
                .required(false)
                .hasArg(false)
                .longOpt(IS_LINUX)
                .desc("Running on a Linux operating system.")
                .build();    
        final Option verboseOption = Option.builder("v")
                .required(false)
                .hasArg(false)
                .longOpt(VERBOSE)
                .desc("Includes all the compliant/non-compliant file paths in the output.")
                .build();
        final Options options = new Options();
        options.addOption(appNameOption);     
        options.addOption(helpOption);
        options.addOption(inputOption);
        options.addOption(propFileOption);
        options.addOption(cfrJarOption);      
        options.addOption(sourceTypeOption);
        
        options.addOption(auditDirectoryOption);
        options.addOption(auditDirPathOption);
        options.addOption(auditFileOption);
        options.addOption(tempDirPathOption);
                
        options.addOption(regexFileOption);
        options.addOption(hasRegexFileOption);      
        options.addOption(regexStringOption);
                
        options.addOption(keeptempOption);
        options.addOption(isLinuxOption);
        options.addOption(verboseOption);
        return options;
    }
    
    public static final void printUsage(final String applicationName) {
        final PrintWriter writer = new PrintWriter(OUT);
        final HelpFormatter usageFormatter = new HelpFormatter();
        usageFormatter.printUsage(writer, 80, applicationName, getOptions());
        writer.flush();
        //writer.close();
    }

    public static final void printHelp(final String applicationName) {
        final HelpFormatter formatter = new HelpFormatter();
        final String syntax = applicationName;
        final String usageHeader = "NetDoc Java network connection documentation tool.";
        String usageFooter = "Examples: \n";
        usageFooter += "    java -jar file-inspector.jar -i [-k, -l, -v]\n";
        usageFooter += "    java -jar file-inspector.jar -p <YOUR_PROP_FILE> [-k, -l, -v]\n";
        usageFooter += "    java -jar file-inspector.jar -r <SAVED_JSON_REPORT_FROM_DOCLET> -n MyAppName\n";
        usageFooter += "See http://jakartaee.net/tools\n";
        formatter.printHelp(syntax, usageHeader, getOptions(), usageFooter);
    }

    public static final CommandLine generateCommandLine(final String[] commandLineArguments) {
        final CommandLineParser cmdLineParser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = cmdLineParser.parse(getOptions(), commandLineArguments);
        } catch (ParseException parseException) {
            OUT.println("ERROR: Unable to parse command-line arguments "
                    + Arrays.toString(commandLineArguments) + " due to: "
                    + parseException);
        }
        return commandLine;
    }
}
