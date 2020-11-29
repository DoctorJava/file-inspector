package com.websecuritylab.tools.fileinspector;
import org.apache.commons.cli.*;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;

public class CliOptions {

    public static final String HELP = "help";
    //public static final String CREATE = "create";
    public static final String PROMPT_PROPS = "prompt-props";
    public static final String PROPS_FILE = "props-file";
    //public static final String REPORT_JSON = "report-json";
    
    public static final String CFR_JAR = "cfr-jar";
    public static final String SOURCE_TYPE = "source-type";
    
    //public static final String AUDIT_DIRECTORY = "audit-directory";
	public static final String ROOT_DIR_PATH = "root-dir-path";
    //public static final String AUDIT_FILE = "audit-file";
	public static final String TEMP_DIR_PATH = "temp-dir-path";
 
    //public static final String HAS_REGEX_FILE = "has-regex-file";
    public static final String SEARCH_PATTERN = "search-pattern";
    public static final String SEARCH_PATTERN_FILE = "search-pattern-file";
    
    public static final String INCLUDE_GLOB = "include-glob";
    public static final String INCLUDE_GLOB_FILE = "include-glob-file";
    public static final String EXCLUDE_GLOB = "exclude-glob";
    public static final String EXCLUDE_GLOB_FILE = "exclude-glob-file";
    
	//public static final String SOURCE_DIR = "source-directory";
    //public static final String SOURCE_FILE = "source-file";
    public static final String APP_NAME = "app-name";
//    public static final String CLASSPATH = "classpath";
//    public static final String SUBPACKAGES = "subpackages";
   
    public static final String KEEP_TEMP = "keep-temp";
    
    public static final String MORE_VERBOSE = "more-verbose";
    public static final String VERBOSE = "verbose";
    private static final PrintStream OUT = System.out;

	// public static final String IS_LINUX = "is-linux";				// Linux is no longer supported with the PowerShell based searching.  But it might be in the future
//    final Option isLinuxOption = Option.builder("l")
//            .required(false)
//            .hasArg(false)
//            .longOpt(IS_LINUX)
//            .desc("Running on a Linux operating system.")
//            .build(); 
    // options.addOption(isLinuxOption);
   
    private CliOptions() {
    }

    public static final Options getOptions() {
        
        final Option helpOption = Option.builder("h")
                .required(false)
                .hasArg(false)
                .longOpt(HELP)
                .desc("Print help.")
                .build();

        final Option includeGlobOption = Option.builder("i")
                .required(false)
                .hasArg()
                .longOpt(INCLUDE_GLOB)
                .desc("Required: Globs for files that are to be INCLUDED.  By default ALL files under rood directory are included.)")
                .build();         
        final Option includeGlobFileOption = Option.builder("if")
                .required(false)
                .hasArg()
                .longOpt(INCLUDE_GLOB_FILE)
                .desc("Required: Path to the FILE with Globs for files that are to be INCLUDED.  By default ALL files under rood directory are included.")
                .build(); 
        final Option excludeGlobOption = Option.builder("x")
                .required(false)
                .hasArg()
                .longOpt(INCLUDE_GLOB)
                .desc("Required: Globs for files that are to be EXCLUDED.  By default NO files under rood directory are excluded.)")
                .build();         
        final Option excludeGlobFileOption = Option.builder("xf")
                .required(false)
                .hasArg()
                .longOpt(INCLUDE_GLOB_FILE)
                .desc("Required: Path to the FILE with Globs for files that are to be EXCLUDED.  By default NO files under rood directory are excluded.")
                .build(); 
        
        final Option inputOption = Option.builder("p")
                .required(false)
                .hasArg(false)
                .longOpt(PROMPT_PROPS)
                .desc("PROMPT for input options to be used.  Entries will be saved in the DEFAULT properties file.")
                .build();
        final Option propFileOption = Option.builder("pf")
                .required(false)
                .hasArg()
                .longOpt(PROPS_FILE)
                .desc("Load options from specified properties FILE.")
                .build();
        final Option appNameOption = Option.builder("n")
                .required(false)
                .hasArg()
                .longOpt(APP_NAME)
                .desc("Required: Application NAME that is appended to the HTML/JSON report names.")
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
                .desc("Required: TYPE of  files to be scanned ([A]rchive file (WAR/EAR/JAR), [C]LASS files, [S]OURCE files.)")
                .build();         

        
        //
        // TODO: handle using the TEMP file directory withough knowing the absolute PATH
        //
        
//        final Option auditDirectoryOption = Option.builder("d")
//                .required(false)
//                .hasArg()
//                .longOpt(AUDIT_DIRECTORY)
//                .desc("Required: Audit all files in a directory? ( [Y]es, [N]o )")
//                .build();         
        final Option auditDirPathOption = Option.builder("r")
                .required(false)
                .hasArg()
                .longOpt(ROOT_DIR_PATH)
                .desc("Required: Path to directory of file(s).")
                .build();         
//        final Option auditFileOption = Option.builder("f")
//                .required(false)
//                .hasArg()
//                .longOpt(AUDIT_FILE)
//                .desc("Required: Name of the file to be audited.")
//                .build(); 
        final Option tempDirPathOption = Option.builder("z")
                .required(false)
                .hasArg()
                .longOpt(TEMP_DIR_PATH)
                .desc("Required: Path to temp directory where file(s) were decompiled.")
                .build();         
        
        
        
//        final Option hasRegexFileOption = Option.builder("r")
//                .required(false)
//                .hasArg(false)
//                .longOpt(HAS_REGEX_FILE)
//                .desc("Required: Load file with Regex search expression.  ")
//                .build();         
        
        final Option searchPatternOption = Option.builder("s")
                .required(false)
                .hasArg()
                .longOpt(SEARCH_PATTERN)
                .desc("SEARCH expression.  For simple strings use | separator without any SPACES)")
                .build();         
        final Option searchPatternFileOption = Option.builder("sf")
                .required(false)
                .hasArg()
                .longOpt(SEARCH_PATTERN_FILE)
                .desc("Path to the FILE with SEARCH patterns.")
                .build();       
        
        
        
        
        final Option keeptempOption = Option.builder("k")
                .required(false)
                .hasArg(false)
                .longOpt(KEEP_TEMP)
                .desc("Keep temporary extracted *.class and *.java files.")
                .build();    
   
        final Option verboseOption = Option.builder("v")
                .required(false)
                .hasArg(false)
                .longOpt(VERBOSE)
                .desc("Shows INCLUDED files in the CLI output.")
                .build();
        final Option moreVerboseOption = Option.builder("vv")
                .required(false)
                .hasArg(false)
                .longOpt(MORE_VERBOSE)
                .desc("Shows INCLUDED and EXCLUDED files in the CLI output.")
                .build();
        
        
        final Options options = new Options();
        options.addOption(appNameOption);     
        options.addOption(helpOption);
        options.addOption(inputOption);
        options.addOption(propFileOption);
        options.addOption(cfrJarOption);      
        options.addOption(sourceTypeOption);
        
       //options.addOption(auditDirectoryOption);
        options.addOption(auditDirPathOption);
        //options.addOption(auditFileOption);
        options.addOption(tempDirPathOption);
                
        options.addOption(searchPatternOption);
        options.addOption(searchPatternFileOption);
        options.addOption(includeGlobOption);
        options.addOption(includeGlobFileOption);
        options.addOption(excludeGlobOption);
        options.addOption(excludeGlobFileOption);
                
        options.addOption(keeptempOption);
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
        final String usageHeader = "FileInspector Java network connection documentation tool.";
        String usageFooter = "Examples: \n";
        usageFooter += "    java -jar file-inspector.jar -p [-k, -l, -v]\n";
        usageFooter += "    java -jar file-inspector.jar -pf <YOUR_PROP_FILE> [-k, -l, -v]\n";
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
