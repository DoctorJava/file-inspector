package com.websecuritylab.tools.fileinspector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellResponse;
import com.websecuritylab.tools.fileinspector.FileUtil.REPORT_TYPE;
import com.websecuritylab.tools.fileinspector.model.PowerShellSearchResult;
import com.websecuritylab.tools.fileinspector.model.PowerShellSearchResult_ObjectMatches;
import com.websecuritylab.tools.fileinspector.model.Report;


public class Main {
	
	private enum SOURCE_TYPE { A, C, S }			// [A]rchive file (WAR/EAR/JAR), [C]LASS files, [S]OURCE files.
	private enum AUDIT_DIRECTORY { Y, N, T }		// [Y]es (directory), [N]o (single file), [T]emp (previously extracted temp diretory).
	
	public enum FIND_EXT { jar, java, war }
    private static final Logger logger = LoggerFactory.getLogger( Main.class );  
	private static final String PROPS_FILE = "file-inspector.props";
	private static final String SYNTAX = "java -jar file-inspector.jar ";
	private static final String RUN_DECOMPILE = "java -jar lib/%s  %s --outputdir %s";				// Synax for CFR: java -jar lib/<CFR>.jar <FILES> --outputdir <OUTPUT_DIR>
	private static final String TEMP_DIR = "fileinspector";
	
	private static Properties props = new Properties();		
	private static String cfrJar = props.getProperty(CliOptions.CFR_JAR);
	private static String outFolder = "out/";			
	private static String outJsonPath=null;
	private static String outHtmlSummaryPath=null;
	private static String outHtmlDetailPath=null;

	public static void main(String[] args) {
		String mainCmd = SYNTAX + String.join(" ", Arrays.asList(args));
		logger.info(mainCmd);
		
		CommandLine cl = CliOptions.generateCommandLine(args);
		String propFile = PROPS_FILE;
		if (cl.getOptionValue(CliOptions.PROP_FILE) != null ) propFile = cl.getOptionValue(CliOptions.PROP_FILE);

		try ( InputStream fis = new FileInputStream(propFile); ) {
			props.load(fis);
			logger.info("Got prop AUDIT_DIR: " + props.getProperty(CliOptions.AUDIT_DIR_PATH));
		} catch (IOException e) {
			props.setProperty(CliOptions.AUDIT_DIRECTORY, "N");
			props.setProperty(CliOptions.SOURCE_TYPE, "A");
			props.setProperty(CliOptions.AUDIT_DIR_PATH, ".");
			props.setProperty(CliOptions.CFR_JAR, "cfr-0.147.jar");
			
		}	
	
		boolean isVerbose = false;
		boolean isKeepTemp = false;
		boolean isLinux = false;
		boolean hasRegExFile = false;
		try (BufferedReader buf = new BufferedReader(new InputStreamReader(System.in))) {
			if (cl.hasOption(CliOptions.HELP)) {
				CliOptions.printHelp(SYNTAX);
				FileUtil.finish();
			} 

			if (cl.hasOption(CliOptions.VERBOSE)) isVerbose = true;
			if (cl.hasOption(CliOptions.KEEP_TEMP)) isKeepTemp = true;
			if (cl.hasOption(CliOptions.IS_LINUX)) isLinux = true;
			if (cl.hasOption(CliOptions.HAS_REGEX_FILE)) hasRegExFile = true;
			if (cl.getOptionValue(CliOptions.CFR_JAR) != null )  props.setProperty(CliOptions.CFR_JAR, cl.getOptionValue(CliOptions.CFR_JAR));

            if (cl.hasOption(CliOptions.INTERACTIVE)) {
            	handlePropInput(buf,CliOptions.AUDIT_DIRECTORY, false);
            	AUDIT_DIRECTORY auditDirectory = Enum.valueOf(AUDIT_DIRECTORY.class, props.getProperty(CliOptions.AUDIT_DIRECTORY).toUpperCase());
            	
            	boolean isDirectory = true;
             	String appName = null;
            	switch( auditDirectory )
    			{
    				case T:
                        handlePropInput(buf,CliOptions.TEMP_DIR_PATH, true);
    					break;
    				case Y:
                		handlePropInput(buf,CliOptions.SOURCE_TYPE, false);
                        handlePropInput(buf,CliOptions.AUDIT_DIR_PATH, true);
                   	    appName = props.getProperty(CliOptions.AUDIT_DIR_PATH);
                    	int lastSlash = appName.lastIndexOf("/");			// TODO: Handle Windows backslash too?
                       	if ( lastSlash == appName.length() - 1 ) appName = appName.substring(0,lastSlash);	// Trim last slash 
                       	lastSlash = appName.lastIndexOf("/");
                    	if ( lastSlash > 0 ) appName = appName.substring(lastSlash+1);
                    	props.setProperty(CliOptions.APP_NAME, appName );              	          	
    					break;
    				case N:
    	            	isDirectory = false;
                		handlePropInput(buf,CliOptions.SOURCE_TYPE, false);
                        handlePropInput(buf,CliOptions.AUDIT_DIR_PATH, true);
                      	handlePropInput(buf,CliOptions.AUDIT_FILE, false);
                      	appName = props.getProperty(CliOptions.AUDIT_FILE);
                      	int lastDot = appName.lastIndexOf(".");
                      	if ( lastDot > 0 ) appName = appName.substring(0, lastDot);
                      	props.setProperty(CliOptions.APP_NAME, appName );
    					break;
    					 
    			}


                if ( hasRegExFile ) handlePropInput(buf,CliOptions.REGEX_FILE, false);
                else handlePropInput(buf,CliOptions.REGEX_STRING, false);
                
                handlePropInput(buf,CliOptions.APP_NAME, false);
                
               // handlePropInput(buf,CliOptions.IS_LINUX, false);
                String dirPath = props.getProperty(CliOptions.AUDIT_DIR_PATH);
                String filePath = dirPath + props.getProperty(CliOptions.AUDIT_FILE);
    			SOURCE_TYPE sourceType = Enum.valueOf(SOURCE_TYPE.class, props.getProperty(CliOptions.SOURCE_TYPE).toUpperCase());
                 
                Collection<File> files = null;
                if ( isDirectory ) {
        			File f = new File(dirPath);
        			files = FileUtil.listFilesByExt(f, FIND_EXT.jar);               	
                 }
                else {
        			File f = new File(filePath);
                	files = Arrays.asList(f);
                }
                
                String searchPath = dirPath;
                
                if ( auditDirectory != AUDIT_DIRECTORY.T ) {
            		if ( sourceType == SOURCE_TYPE.A ) {
            			File tempDir = Util.createTempDir(TEMP_DIR);
            			for (File file: files ) {
              				searchPath = runDecompile(file, tempDir, isLinux, isKeepTemp, isVerbose);	
               				props.setProperty(CliOptions.TEMP_DIR_PATH, searchPath);
            			}       		
            		}              	
                }
                else {
                	searchPath = props.getProperty(CliOptions.TEMP_DIR_PATH);
                }

                String searchRegEx="";
                if ( hasRegExFile ) {
                	List<String> lines = Util.readNonCommentLines(props.getProperty(CliOptions.REGEX_FILE),"///");
                	int i = 0;
                	for (String line : lines) {
                		searchRegEx += line;
                	    if (i++ != lines.size() - 1) { searchRegEx += "|"; };				// Add '|' if NOT last line
                	}
                	//searchRegEx = Util.readFile(props.getProperty(CliOptions.REGEX_FILE));
                	
                } else {
            		searchRegEx = props.getProperty(CliOptions.REGEX_STRING);     
                }
   				Report report = searchRecursiveForString(searchPath, searchRegEx, isLinux, isVerbose);
   				
   				ObjectMapper mapper = new ObjectMapper();
   				String uglyReport = mapper.writeValueAsString(report);  
  				String prettyReport = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);				
  				
  				String app = props.getProperty(CliOptions.APP_NAME);
  				outJsonPath = FileUtil.outputReport(REPORT_TYPE.json, props, outFolder, app, prettyReport);
  				outHtmlSummaryPath = FileUtil.outputReport(REPORT_TYPE.summary,  props, outFolder, app, uglyReport);
  				outHtmlDetailPath = FileUtil.outputReport(REPORT_TYPE.detail,  props, outFolder, app, uglyReport);
 

//    			for (File file: files ) {
//    				System.out.println("Got file: " + file.getName());		
//
//    				//run(sourceType, isLinux, isKeepTemp, isVerbose);	
//    			}
    			
    			
                logger.info("Running: " + SYNTAX + " -s " +   props.getProperty(CliOptions.AUDIT_DIR_PATH));
            } else {
            	if (props.getProperty(CliOptions.AUDIT_DIR_PATH) != null) {
    				if (!FileUtil.fileFolderExists( props.getProperty(CliOptions.AUDIT_DIR_PATH)))
    					FileUtil.abort("Aborting program.  The source directory (" +  props.getProperty(CliOptions.AUDIT_DIR_PATH) + ") does not exist.");
    			} else {
    				FileUtil.abort("Aborting program.  The root source directory (-s) option is required.");
    			}
            }
			
			OutputStream output = new FileOutputStream(PROPS_FILE);
			props.store(output,  null);
			
			System.out.println("---------- Fileinspector Scanning Properties ----------");
			System.out.println("File: " + propFile);
			System.out.println();
			System.out.println(props.toString().replace(", ", "\n").replace("{", "").replace("}", ""));  
			System.out.println("------------------------------------------------");
			System.out.println("See Output Files: ");
			System.out.println(outJsonPath);
			System.out.println(outHtmlSummaryPath);
			

			
		} catch (Exception e) {
			e.printStackTrace();
			//return;
		} 

		
		
		
		
	}
	
	private static String runDecompile(File file, File tempDir, boolean isLinux, boolean keepTemp, boolean isVerbose) throws IOException {
		//String tempPath = tempDir.getAbsolutePath().replace("\\", "/"); // replace windows backslash because either works with the cmd
		String tempPath = tempDir.getCanonicalPath().replace("\\", "/"); // replace windows backslash because either works with the cmd

		String cfrJar = props.getProperty(CliOptions.CFR_JAR);

		String decompiledPath = tempPath + "/decompiled/";

		try {
			String runDecompileA = String.format(RUN_DECOMPILE, cfrJar, file, decompiledPath);
			logger.debug("Running: " + runDecompileA);
			Util.runCommand(isLinux, runDecompileA, isVerbose);

		} finally { // TODO: This doesn't get executed with javadoc command exits with error.
			if (!keepTemp) {
				Util.deleteDir(tempDir);
			}
		}
		
		return decompiledPath;

	}

	private static Report searchRecursiveForString( String rootPath, String searchStr, boolean isLinux, boolean isVerbose) throws IOException {
	
		//String searchStr = "'rijndael|blowfish'";
	
		//System.out.println("!!!!!!!!!!!!!!!!Got RootPath: " + rootPath);
		//String cmd = "Get-ChildItem '"+rootPath+"' -Recurse | Select-String -Pattern '"+searchStr+"' -Context 0,3 | ConvertTo-Json";
		String cmd = "Get-ChildItem '"+rootPath+"' -Recurse | Select-String -Pattern '"+searchStr+"' | ConvertTo-Json";
		
	//	[ {
	//	  "IgnoreCase" : true,
	//	  "LineNumber" : 67,
	//	  "Line" : "            cipher = new BlowfishEngine();",
	//	  "Filename" : "BlockCipherSpec.java",
	//	  "Path" : "C:\\Users\\scott\\AppData\\Local\\Temp\\fileinspector\\decompiled\\org\\cryptacular\\spec\\BlockCipherSpec.java",
	//	  "Pattern" : "rijndael|blowfish",
//        "Context":  {
//            "PreContext":  "        public KeyGen() {",
//            "PostContext":  "        }",
//            "DisplayPreContext":  "        public KeyGen() {",
//            "DisplayPostContext":  "        }"
//        },
	//	  "Matches" : [ "Blowfish" ]
	//	} ]
				
		System.out.println ("----------------Running powershell command--------------------");
		System.out.println (cmd);		
		System.out.println ("--------------------------------------------------------------");
		PowerShellResponse response = PowerShell.executeSingleCommand(cmd);
		
		Report report = new Report("MyApp");
		
		String jsonStr = response.getCommandOutput();
		//System.out.println("Got jsonStr single object: " + jsonStr.startsWith("{"));
		//if (jsonStr.startsWith("{")) jsonStr = "[" + jsonStr + "]";		// Convert to a List of one object because ObjectMapper is looking for a list
		System.out.println("Got jsonStr: " + jsonStr);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);	// Powershell Select-String returns single object, or a list of objects

		try {
			List<PowerShellSearchResult> psResults = mapper.readValue(jsonStr, new TypeReference<List<PowerShellSearchResult>>(){});
			
			for(PowerShellSearchResult r : psResults) {
				report.addFileMatch(r);
			}
						
		} catch (MismatchedInputException e) {
			List<PowerShellSearchResult_ObjectMatches> psResultsObjectMapper = mapper.readValue(jsonStr, new TypeReference<List<PowerShellSearchResult_ObjectMatches>>(){});
			
			List<PowerShellSearchResult> psResults = new ArrayList<>();
			for(PowerShellSearchResult_ObjectMatches rom : psResultsObjectMapper) {
				psResults.add(new PowerShellSearchResult(rom));	
			}
		
			for(PowerShellSearchResult r : psResults) {
				report.addFileMatch(r);
			}
		}
		catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return report;
	}
	
	
	//Get-ChildItem C:/Users/scott/AppData/Local/Temp/fileinspector/decompiled/*.java -Recurse | Select-String -Pattern "parseTrie" | group path | select name
	private static String searchRecursiveForStringOld( String rootPath, String searchText, boolean isLinux, boolean isVerbose) throws IOException {
		String HR_START = ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>";
		String HR_END 	= "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<";
		StringBuffer output = new StringBuffer();
		System.out.println("Searching rootPath: " + rootPath);
		// Get-ChildItem 'C:/Users/scott/AppData/Local/Temp/fileinspector/decompiled/*.java' -Recurse | Select-String -Pattern 'CloneNotSupportedException | parseTrie' | ForEach-Object { ' | ' + $_.lineNumber  + ' | ' + $_.fileName + ' | ' + $_.Line }
		// Get-ChildItem 'C:/Users/scott/AppData/Local/Temp/fileinspector/decompiled/*.java' -Recurse | Select-String -Pattern 'CloneNotSupportedException | parseTrie' | ForEach-Object { ' | ' + $_.lineNumber  + ' | ' + $_.fileName + ' | ' + $_.Line }
		String cmd;
		if (isLinux)
			cmd = "TODO: grep command";
		else
			cmd = "Get-ChildItem " + rootPath + " -Recurse | Select-String -Pattern '" + searchText + "' | ForEach-Object { ' | ' + $_.lineNumber  + ' | ' + $_.fileName + ' | ' + $_.Line }";
			//cmd = "Get-ChildItem " + rootPath + " -Recurse | Select-String -Pattern '" + searchText + "' | group path | select name";
			//cmd = "Get-ChildItem C:/Users/scott/AppData/Local/Temp/fileinspector/decompiled/*.java -Recurse | Select-String -Pattern '" + searchText + "' | group path | select name";
		
		
		logger.info("Running command: " + cmd);
        ProcessBuilder processBuilder = new ProcessBuilder();
        if ( isVerbose ) {
            System.out.println(HR_START);
            System.out.println("Running "+ ( isLinux ? "LINUX" : "WINDOWS" ) +" command: ");
            System.out.println();
            System.out.println(cmd);
            System.out.println();       	
        }
		final File tmp = File.createTempFile("fileAuditOut", null);
		try {
			tmp.deleteOnExit();

			if (isLinux)
				processBuilder.command("sh", "-c", cmd).redirectErrorStream(true).redirectOutput(tmp);
			else
				processBuilder.command("C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe", "-Command", cmd).redirectErrorStream(true).redirectOutput(tmp); // Windows

			final Process process = processBuilder.start();
			final int exitCode = process.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tmp)));
			String line = "";
			while ((line = reader.readLine()) != null) {
				if (isVerbose) System.out.println(line);
				output.append(line);
			}
			reader.close();
			tmp.delete();
			logger.debug("\nExited with error code : " + exitCode);
			if (isVerbose) System.out.println("\nExited with error code : " + exitCode);
		} catch (Exception e) {
			logger.error( "EEEEEEEEEEEEError in runCommand: " + processBuilder.toString() + " with error: " + e.getMessage());
		} finally {
			tmp.delete();
		}
		if (isVerbose) System.out.println(HR_END);

		return output.toString();
		
	}

    private static void handlePropInput(BufferedReader buf, String key, boolean hasTrailingSlash) throws IOException {
        System.out.print("Enter the " + key + " (" + props.getProperty(key) + "): ");
        String entry = buf.readLine();
        if (!entry.equals("")) {
            if (hasTrailingSlash && (!entry.endsWith("/") && !entry.endsWith("\\")) )  props.setProperty( key, entry + "/" );
            else props.setProperty( key, entry);
        }      
    }
    private static String handleTextInput(BufferedReader buf, String key) throws IOException {
        System.out.print("Enter the " + key + " : ");
        String entry = buf.readLine();

        return entry;     
    }
}
