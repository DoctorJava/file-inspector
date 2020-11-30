package com.websecuritylab.tools.fileinspector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellNotAvailableException;
import com.profesorfalken.jpowershell.PowerShellResponse;
import com.websecuritylab.tools.fileinspector.FileUtil.REPORT_TYPE;
import com.websecuritylab.tools.fileinspector.model.PowerShellSearchResult;
import com.websecuritylab.tools.fileinspector.model.PowerShellSearchResult_ObjectMatches;
import com.websecuritylab.tools.fileinspector.model.Report;

import com.websecuritylab.tools.fileinspector.glob.FileMatcher;


public class Main {
	
	private enum SOURCE_TYPE { A, C, S }			// [A]rchive file (WAR/EAR/JAR), [C]LASS files, [S]OURCE files.
//	private enum AUDIT_DIRECTORY { Y, N, T }		// [Y]es (directory), [N]o (single file), [T]emp (previously extracted temp diretory).
	
	private static String COMMA = ",";
	private static String PIPE = "|";
	private static String COMMENT = "///";
	
	public enum FIND_EXT { jar, war, ear, java, clazz }
    private static final Logger logger = LoggerFactory.getLogger( Main.class );  
	private static final String PROPS_FILE = "file-inspector.props";
	private static final String SYNTAX = "java -jar file-inspector.jar ";
	private static final String RUN_DECOMPILE = "java -jar lib/%s  %s --outputdir %s";				// Synax for CFR: java -jar lib/<CFR>.jar <FILES> --outputdir <OUTPUT_DIR>
	private static final String TEMP_DIR = "fileinspector";
	
	private static Properties props = new Properties();		
	//private static String cfrJar = props.getProperty(CliOptions.CFR_JAR);
	private static String outFolder = "out/";			
	private static String outJsonPath=null;
	private static String outHtmlSummaryPath=null;
	private static String outHtmlDetailPath=null;
	
	private static String searchPatterns;
	private static boolean searchPatternsFileOnCommandLine = false;

	private static String includeGlobs;
	private static boolean includeGlobFileOnCommandLine = false;
	private static String excludeGlobs;
	private static boolean excludeGlobFileOnCommandLine = false;
	
	private static boolean isVerbose = false;
	private static boolean isMoreVerbose = false;

	public static void main(String[] args) {

		String mainCmd = SYNTAX + String.join(" ", Arrays.asList(args));
		logger.info(mainCmd);

		CommandLine cl = CliOptions.generateCommandLine(args);
		
		String propFile = PROPS_FILE;
		if (cl.getOptionValue(CliOptions.PROPS_FILE) != null ) propFile = cl.getOptionValue(CliOptions.PROPS_FILE);

		

		try ( InputStream fis = new FileInputStream(propFile); ) {
			props.load(fis);
			logger.info("Got prop AUDIT_DIR: " + props.getProperty(CliOptions.ROOT_DIR_PATH));
		} catch (IOException e) {
			//			props.setProperty(CliOptions.AUDIT_DIRECTORY, "N");
			props.setProperty(CliOptions.SOURCE_TYPE, "A");
			props.setProperty(CliOptions.ROOT_DIR_PATH, ".");
			props.setProperty(CliOptions.CFR_JAR, "cfr-0.147.jar");

		}	
		
		// These file names will override the saved prop file values if they are present on the commandline 
		//if (cl.getOptionValue(CliOptions.SEARCH_PATTERN_FILE) != null ) 
		//	props.setProperty( CliOptions.SEARCH_PATTERN_FILE, cl.getOptionValue(CliOptions.SEARCH_PATTERN_FILE));
		
		if (cl.getOptionValue(CliOptions.SEARCH_PATTERN_FILE) != null ) {
			searchPatternsFileOnCommandLine = true;
			props.setProperty( CliOptions.SEARCH_PATTERN_FILE, cl.getOptionValue(CliOptions.SEARCH_PATTERN_FILE));
		}
		
		if (cl.getOptionValue(CliOptions.INCLUDE_GLOB_FILE) != null ) {
			includeGlobFileOnCommandLine = true;
			props.setProperty( CliOptions.INCLUDE_GLOB_FILE, cl.getOptionValue(CliOptions.INCLUDE_GLOB_FILE));
		}
		if (cl.getOptionValue(CliOptions.EXCLUDE_GLOB_FILE) != null ) {
			excludeGlobFileOnCommandLine = true;
			props.setProperty( CliOptions.EXCLUDE_GLOB_FILE, cl.getOptionValue(CliOptions.EXCLUDE_GLOB_FILE));
		}

		boolean isKeepTemp = false;
		boolean isLinux = false;			// Linux is no longer supported with the PowerShell based searching.  But it might be in the future	
		try {
			if (cl.hasOption(CliOptions.HELP)) {
				CliOptions.printHelp(SYNTAX);
				FileUtil.finish();
			} 

			if (cl.hasOption(CliOptions.VERBOSE)) isVerbose = true;
			if (cl.hasOption(CliOptions.MORE_VERBOSE)) isMoreVerbose = true;
			if (cl.hasOption(CliOptions.KEEP_TEMP)) isKeepTemp = true;
			//if (cl.hasOption(CliOptions.IS_LINUX)) isLinux = true;	// Linux is no longer supported with the PowerShell based searching.  But it might be in the future
			//if (cl.hasOption(CliOptions.HAS_REGEX_FILE)) hasRegExFile = true;
			if (cl.getOptionValue(CliOptions.CFR_JAR) != null )  props.setProperty(CliOptions.CFR_JAR, cl.getOptionValue(CliOptions.CFR_JAR));

			if (cl.hasOption(CliOptions.PROMPT_PROPS)) {
				 getPromptInput();
			} 

			// handlePropInput(buf,CliOptions.IS_LINUX, false);
			String dirPath = props.getProperty(CliOptions.ROOT_DIR_PATH);
			if (dirPath == null) {
					FileUtil.abort("Aborting program. The root source directory does not exist:" + dirPath);
			}               

				//String filePath = dirPath + props.getProperty(CliOptions.AUDIT_FILE);
				SOURCE_TYPE sourceType = Enum.valueOf(SOURCE_TYPE.class, props.getProperty(CliOptions.SOURCE_TYPE).toUpperCase());

				Collection<File> files = null;
				//                if ( isDirectory ) {
				File f = new File(dirPath);
				switch(sourceType) {
				case A: 
					files = FileUtil.listFilesByExt(f, FIND_EXT.jar); 
					files.addAll(FileUtil.listFilesByExt(f, FIND_EXT.war)); 
					files.addAll(FileUtil.listFilesByExt(f, FIND_EXT.ear)); 
					break;
				case S: 
					files = FileUtil.listFilesByExt(f, FIND_EXT.java); 
					break;
				case C: 
					files = FileUtil.listFilesByExt(f, FIND_EXT.clazz); 
					break;
				}

				System.out.println("Got Files in Directory: " + files);
				//                 }
				//                else {
				//        			//File f = new File(filePath);
				//                	files = Arrays.asList(f);
				//                }

				String searchPath = dirPath;

				//                if ( auditDirectory != AUDIT_DIRECTORY.T ) {
				if ( sourceType == SOURCE_TYPE.A || sourceType == SOURCE_TYPE.C  ) {
					File tempDir = Util.createTempDir(TEMP_DIR);
					for (File file: files ) {
						searchPath = runDecompile(file, tempDir, isLinux, isKeepTemp);	
						props.setProperty(CliOptions.TEMP_DIR_PATH, searchPath);
					}       		
				}              	
				//                }
				//                else {
				//                	searchPath = props.getProperty(CliOptions.TEMP_DIR_PATH);
				//                }

//				String searchRegEx="";
//				if ( props.getProperty(CliOptions.SEARCH_PATTERN_FILE) != null ) {
//					searchRegEx = Util.getSeparatedStringFromFile(props.getProperty(CliOptions.SEARCH_PATTERN_FILE),PIPE,COMMENT);
////					List<String> lines = Util.readNonCommentLines(props.getProperty(CliOptions.SEARCH_PATTERN_FILE),COMMENT);
////					int i = 0;
////					for (String line : lines) {
////						searchRegEx += line;
////						if (i++ != lines.size() - 1) { searchRegEx += "|"; };				// Add '|' if NOT last line
////					}
//					//searchRegEx = Util.readFile(props.getProperty(CliOptions.REGEX_FILE));
//
//				} else {
//					searchRegEx = props.getProperty(CliOptions.SEARCH_PATTERN);     
//				}




				Report report = searchRecursiveForString(searchPath, searchPatterns, isLinux);

				savePropsFile(propFile);

				if ( report != null ) {
					ObjectMapper mapper = new ObjectMapper();
					//String uglyReport = mapper.writeValueAsString(report);
					String uglyReport = mapper.writeValueAsString(report).replace("script",  "zcript");  		// </script> tags in LINE match breaks the HTML rendering
					String prettyReport = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);				

					String app = props.getProperty(CliOptions.APP_NAME);
					outJsonPath = FileUtil.outputReport(REPORT_TYPE.json, props, outFolder, app, prettyReport);
					outHtmlSummaryPath = FileUtil.outputReport(REPORT_TYPE.summary,  props, outFolder, app, uglyReport);
					outHtmlDetailPath = FileUtil.outputReport(REPORT_TYPE.detail,  props, outFolder, app, uglyReport);

					System.out.println("***************  Output Files  ***********************");
					System.out.println();
					System.out.println(outJsonPath);
					System.out.println(outHtmlSummaryPath);
					System.out.println(outHtmlDetailPath); 	   					
					System.out.println();
					System.out.println("******************************************************");
				}


				//    			for (File file: files ) {
				//    				System.out.println("Got file: " + file.getName());		
				//
				//    				//run(sourceType, isLinux, isKeepTemp, isVerbose);	
				//    			}


				logger.info("Running: " + SYNTAX + " -s " +   props.getProperty(CliOptions.ROOT_DIR_PATH));



		} catch (IOException | PowerShellException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}




	}
	
	private static void getPromptInput() throws IOException {
		try (BufferedReader buf = new BufferedReader(new InputStreamReader(System.in))) {
			String appName = null;

			//            	switch( auditDirectory )
			//    			{
			//    				case T:
			//                        handlePropInput(buf,CliOptions.TEMP_DIR_PATH, true);
			//    					break;


			handlePropInput(buf,CliOptions.SOURCE_TYPE);
			//handlePropInput(buf,CliOptions.ROOT_DIR_PATH, true);				// This was used when handle input added the trailing slash.  Not sure if this really helpful with the PowerShell search approach
			handlePropInput(buf,CliOptions.ROOT_DIR_PATH);
			appName = props.getProperty(CliOptions.ROOT_DIR_PATH);
			int lastSlash = appName.lastIndexOf("/");			// TODO: Handle Windows backslash too?
			if ( lastSlash == appName.length() - 1 ) appName = appName.substring(0,lastSlash);	// Trim last slash 
			lastSlash = appName.lastIndexOf("/");
			if ( lastSlash > 0 ) appName = appName.substring(lastSlash+1);
			props.setProperty(CliOptions.APP_NAME, appName );  

			//if ( props.getProperty(CliOptions.SEARCH_PATTERN_FILE) == null ) handlePropInput(buf,CliOptions.SEARCH_PATTERN);
			if ( !searchPatternsFileOnCommandLine ) {
								// Prompt for EITHER input or filename, remembering which one was previously entered
				if ( hasProp(CliOptions.SEARCH_PATTERN_FILE)) handlePropInputs(buf,CliOptions.SEARCH_PATTERN_FILE, CliOptions.SEARCH_PATTERN);
				else 										  handlePropInputs(buf,CliOptions.SEARCH_PATTERN, CliOptions.SEARCH_PATTERN_FILE);
				
				if (hasProp(CliOptions.SEARCH_PATTERN)) 		  searchPatterns = props.getProperty(CliOptions.SEARCH_PATTERN);		// If empty string, the getProperty returns the String "null" for some reason			
				else if (hasProp(CliOptions.SEARCH_PATTERN_FILE)) searchPatterns = Util.getSeparatedStringFromFile(props.getProperty(CliOptions.SEARCH_PATTERN_FILE),PIPE,COMMENT);
			}

			
			
			if ( !includeGlobFileOnCommandLine ) {
															// Prompt for EITHER input or filename, remembering which one was previously entered
				if ( hasProp(CliOptions.INCLUDE_GLOB_FILE)) handlePropInputs(buf,CliOptions.INCLUDE_GLOB_FILE, CliOptions.INCLUDE_GLOB);
				else 										handlePropInputs(buf,CliOptions.INCLUDE_GLOB, CliOptions.INCLUDE_GLOB_FILE);
					
				if (hasProp(CliOptions.INCLUDE_GLOB)) 			includeGlobs = props.getProperty(CliOptions.INCLUDE_GLOB);		// If empty string, the getProperty returns the String "null" for some reason			
				else if (hasProp(CliOptions.INCLUDE_GLOB_FILE)) includeGlobs = Util.getSeparatedStringFromFile(props.getProperty(CliOptions.INCLUDE_GLOB_FILE),COMMA,COMMENT);
			}
			if ( !excludeGlobFileOnCommandLine ) {
															// Prompt for EITHER input or filename, remembering which one was previously entered
				if ( hasProp(CliOptions.EXCLUDE_GLOB_FILE)) handlePropInputs(buf,CliOptions.EXCLUDE_GLOB_FILE, CliOptions.EXCLUDE_GLOB);
				else										handlePropInputs(buf,CliOptions.EXCLUDE_GLOB, CliOptions.EXCLUDE_GLOB_FILE);
				
				if (hasProp(CliOptions.EXCLUDE_GLOB)) 			excludeGlobs = props.getProperty(CliOptions.EXCLUDE_GLOB);					
				else if (hasProp(CliOptions.EXCLUDE_GLOB_FILE)) excludeGlobs = Util.getSeparatedStringFromFile(props.getProperty(CliOptions.EXCLUDE_GLOB_FILE),COMMA,COMMENT);				
			}

			handlePropInput(buf,CliOptions.APP_NAME);

			
		}
	}
	
	private static boolean hasProp(String key) {
		return ( props.getProperty(key) != null && props.getProperty(key).length() > 0 );
	}
	
	private static void savePropsFile(String propFile) throws IOException {
	    Set<String> keys = props.stringPropertyNames();
	    for (String key : keys) {	  
	    	String val = props.getProperty(key);
	    	if ( val == null || val.length() == 0 ) props.remove(key);
	    }
	
		OutputStream output = new FileOutputStream(propFile);
		props.store(output,  null);
	
		System.out.println("*********  Fileinspector Scanning Properties  ********");
		System.out.println("File: " + propFile);
		System.out.println();
		System.out.println(props.toString().replace(", ", "\n").replace("{", "").replace("}", ""));  
		System.out.println("******************************************************");


	}
	
	private static String runDecompile(File file, File tempDir, boolean isLinux, boolean keepTemp) throws IOException {
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

	private static Report searchRecursiveForString( String rootPath, String searchStr, boolean isLinux) throws IOException, PowerShellException {
	
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
				
		//PowerShellResponse response = PowerShell.executeSingleCommand(cmd);
		PowerShellResponse response = runPowerShell(cmd);
		Report report = new Report("MyApp");
		
		String jsonStr = response.getCommandOutput();
		//System.out.println("Got jsonStr single object: " + jsonStr.startsWith("{"));
		//if (jsonStr.startsWith("{")) jsonStr = "[" + jsonStr + "]";		// Convert to a List of one object because ObjectMapper is looking for a list
		//System.out.println("Got jsonStr: " + jsonStr);
		
		if (jsonStr.length() == 0 ) {
			System.out.println("******************************************************");
			System.out.println();
			System.out.println("     No matches found");
			System.out.println();
			System.out.println("******************************************************");
			return null;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);	// Powershell Select-String returns single object, or a list of objects

		try {
			List<PowerShellSearchResult> psResults = mapper.readValue(jsonStr, new TypeReference<List<PowerShellSearchResult>>(){});
			FileMatcher fm = new FileMatcher(rootPath, includeGlobs, excludeGlobs);
			System.out.println("Using INCLUDE: " + fm.getIncludeGlob());
			System.out.println("Using EXCLUDE: " + fm.getExcludeGlob());
			
			for(PowerShellSearchResult r : psResults) {
				// System.out.println("CCCCCCCCChecking file: " + r.Path);						
				if (fm.includesFile(r.Path)) {
					//System.out.println("CCCCCCcchecking ("+( !fm.excludesFile(r.Path) )+") EXCLUDE file: " + r.Path);						
					if ( !fm.excludesFile(r.Path) ) {
						if (isVerbose || isMoreVerbose) System.out.println("INCLUDING: " + r.Path);						
						report.addFileMatch(r);
					}
					else {
						if (isMoreVerbose) System.out.println("EXCLUDING: " + r.Path);						
					}
				}
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
	
	private static PowerShellResponse runPowerShell(String cmd) throws PowerShellException {
		long startMS = System.currentTimeMillis();

		System.out.println("----------------Running powershell command--------------------");
		System.out.println(cmd);
		//PowerShellResponse response = PowerShell.executeSingleCommand(cmd);
		//PowerShellResponse response = PowerShell.configuration(config).executeSingleCommand(cmd); This DID NOT work.  Need a PS instance=
		//return response;
		
		try (PowerShell powerShell = PowerShell.openSession()) {					// This is used for calling multiple scripts, and setting config
			Map<String, String> config = new HashMap<String, String>();
			config.put("maxWait", "60000");
			PowerShellResponse response = powerShell.configuration(config).executeCommand(cmd);
			
			long endMS = System.currentTimeMillis();
			float elapsed = (endMS - startMS) / 1000F;
			System.out.println("PowerShell scan time: " + elapsed + " seconds");
			System.out.println("--------------------------------------------------------------");

			return response;
		} catch (PowerShellNotAvailableException ex) {
			throw new PowerShellException(ex.getCause());
		}
		
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

    //private static void handlePropInput(BufferedReader buf, String key, boolean hasTrailingSlash) throws IOException {
    private static void handlePropInput(BufferedReader buf, String key) throws IOException {
        System.out.print("Enter the " + key + " (" + props.getProperty(key) + "): ");
        String entry = buf.readLine();
        if (!entry.equals("")) {
            //if (hasTrailingSlash && (!entry.endsWith("/") && !entry.endsWith("\\")) )  props.setProperty( key, entry + "/" );
        	props.setProperty( key, entry);
        }      
    }
    
    private static void handlePropInputs(BufferedReader buf, String key1, String key2) throws IOException {
        System.out.print("Enter the " + key1 + " (" + props.getProperty(key1) + ") or SPACE to CLEAR and/or prompt for: " + key2);
        String entry1 = buf.readLine();
        if (!entry1.equals("")) {
            //if (hasTrailingSlash && (!entry.endsWith("/") && !entry.endsWith("\\")) )  props.setProperty( key, entry + "/" );
        	if ( !entry1.equals(" ") )  {
            	props.setProperty( key1, entry1.trim());			// Converts space into empty string
            	props.setProperty( key2, "");						// ONLY one of the two keys will be valid ( Ex: prompt entry, or file path )
            }
        	else							// SPACE was pressed, prompt for key2
        	{
            	props.setProperty( key1, "");
                System.out.print("Enter the " + key2 + " (" + props.getProperty(key2) + "): ");
                String entry2 = buf.readLine();
                if ( entry2.equals(" ") ) props.setProperty( key2, "");
            	else 
                {
                	props.setProperty( key1, "");					// ONLY one of the two keys will be valid ( Ex: prompt entry, or file path )
                	props.setProperty( key2, entry2.trim());		// Converts space into empty string
                }   
            }
        }
    }
    
//    private static String handleTextInput(BufferedReader buf, String key) throws IOException {
//        System.out.print("Enter the " + key + " : ");
//        String entry = buf.readLine();
//
//        return entry;     
//    }
}
