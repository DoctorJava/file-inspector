package com.websecuritylab.tools.fileinspector;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.websecuritylab.tools.fileinspector.Main.FIND_EXT;


public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger( FileUtil.class );  
    
	private static final String SYNTAX = "java -jar file-inspector.jar ";
	private static final String FINISH_MSG = "Finished.";

	public enum REPORT_TYPE { summary, details }			


	public static Collection<File> listFilesByExt(File dir, FIND_EXT fe) {
    	String ext = fe.toString();		
	    Set<File> fileTree = new HashSet<File>();
	    if(dir==null||dir.listFiles()==null){
	        return fileTree;
	    }
	    for (File entry : dir.listFiles()) {
	        if (entry.isFile()) {
	        	String name = entry.getName();
	        	if ( ext.equals(name.substring( name.lastIndexOf(".")+1).toLowerCase() )) {
	        		fileTree.add(entry);
	        	}
	        }
	        else fileTree.addAll(listFilesByExt(entry, fe));
	    }
	    return fileTree;
	}
	
	public static boolean fileFolderExists(String path) {
		File someFile = new File(path);
		return someFile.exists();
	}
	
	public static void outputJsonReport(String json, String info) throws IOException {
		String OUT_JSON = "out/file-inspector-report_"+info+".json";
		
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_JSON))){
		    //writer.write(JsonOutput.prettyPrint(json)); 
		    writer.write(json); 
		    System.out.println("Output JSON file: " + OUT_JSON);
		}
	}
	public static void outputHtmlReport(REPORT_TYPE type, String json, String info) throws IOException {
		String OUT_HTML_SINGLE = "out/file-inspector_"+ type +"_" + info + ".html";
		//String OUT_HTML_ONLY = "out/file-inspector-report_"+info+"_only.html";
	
			
	//	try(BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_HTML_SINGLE))){
	//	    writer.write(Util.convertJsToHtml( Util.convertJsonToJs(json), false )); 
	//	    System.out.println("Output HTML file: " + OUT_HTML_ONLY);
	//	}
	//	
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(OUT_HTML_SINGLE))){
		    writer.write(convertJsToSummaryHtml( convertSummaryJsonToJs(json), true )); 
		    System.out.println("Output HTML file: " + OUT_HTML_SINGLE);
		}		
	}
	public static void abort(String message) {
		System.err.println(message);
		CliOptions.printUsage(SYNTAX);
		CliOptions.printHelp(SYNTAX);
		System.exit(-1);
	}
	
	public static void finish() {
		logger.debug(FINISH_MSG);
		System.exit(0);
	}
	
	
	public static String convertSummaryJsonToJs(String json) {			// TODO: This doesn't work with JSON that has <CR> because comma isn't replaced with semi-colon
		String bracketStr = json.replace("\"app\":", " const app = ")
								.replace("\"summary\":", " const summary = ")
							    .replace("\"details\":", " const details = ")
							    .replace(", const", "; const");								// Replace commas with semi-colon between the objects.  Semicolon necessary if no line breaks
		System.out.println("Converted JSON------------------");
		System.out.println(json);
		System.out.println("To JS ----------------------__--");
		System.out.println(bracketStr);
		
		return bracketStr.substring(1, bracketStr.length() - 1);							// Need to remove the first and last brackets {}
		
	}
	
	public static String convertNetDocJsonToJs(String json) {			// TODO: This doesn't work with JSON that has <CR> because comma isn't replaced with semi-colon
		String bracketStr = json.replace("\"connections\":", " const connections = ")
								.replace("\"servlets\":", " const servlets = ")
							    .replace("\"services\":", " const services = ")
							    .replace("\"sockets\":", " const sockets = ")
							    .replace("\"info\":", " const info = ")
							    .replace(", const", "; const");								// Replace commas with semi-colon between the objects.  Semicolon necessary if no line breaks
		return bracketStr.substring(1, bracketStr.length() - 1);							// Need to remove the first and last brackets {}
		
	}
	
//	public static String convertJsToHtml(String js, boolean isSingleFile) {
//		final String before = "<!DOCTYPE html><html><title>Net Doc</title> <script type=\"text/javascript\" src=\"js/templates/servlet.js\"></script> <script type=\"text/javascript\" src=\"js/templates/service.js\"></script> <script type=\"text/javascript\" src=\"js/templates/connection.js\"></script> <script type=\"text/javascript\" src=\"js/templates/socket.js\"></script> <link rel=\"stylesheet\" href=\"fileinspector.css\">";
//		final String after = "<body><h1><center>Net Doc Report</center></h1><div id=\"app\"></div> <script>document.getElementById(\"app\").innerHTML=`<h1>Servlets</h1><ul>${servlets.map(servletTemplate).join(\"\")}</ul><h1>Web Services</h1><ul>${services.map(serviceTemplate).join(\"\")}</ul><h1>Net Connections</h1><ul>${connections.map(connectionTemplate).join(\"\")}</ul><h1>Web Sockets</h1><ul>${sockets.map(socketTemplate).join(\"\")}</ul>`;</script></body>";
//		return before + "<script>" + js + "</script>" + after;
//	}
	public static String convertJsToNetDocHtml(String js, boolean isSingleFile) {
		String returnStr = "<!DOCTYPE html><html>";
		final String head = getNetDocHTMLHead(js);
		final String body = "<body><h1><center>Net Doc Report</center></h1><div id=\"app\"></div> <script>document.getElementById(\"app\").innerHTML=`<h1>Servlets</h1><ul>${servlets.map(servletTemplate).join(\"\")}</ul><h1>Web Services</h1><ul>${services.map(serviceTemplate).join(\"\")}</ul><h1>Net Connections</h1><ul>${connections.map(connectionTemplate).join(\"\")}</ul><h1>Web Sockets</h1><ul>${sockets.map(socketTemplate).join(\"\")}</ul>`;</script></body>";
		returnStr += head + body + "</html>";
		return returnStr;
	}
	public static String convertJsToSummaryHtml(String js, boolean isSingleFile) {
		String returnStr = "<!DOCTYPE html><html>";
		final String head = getSummaryHTMLHead(js);
		final String body = "<body><h1><center>File Inspector</center></h1><div id=\"app\"></div> <script>document.getElementById(\"app\").innerHTML=`<h1>Summary</h1><ul>${summary.map(summaryTemplate).join(\"\")}</ul>`;</script></body>";
		returnStr += head + body + "</html>";
		return returnStr;
	}
	private static String getNetDocHTMLHead(String js) {
		String returnStr = "<head>";
		returnStr += "<title>File Inspector</title> ";
		returnStr += "<script type=\"text/javascript\" src=\"js/templates/servlet.js\"></script>";
		returnStr += "<script type=\"text/javascript\" src=\"js/templates/service.js\"></script>";
		returnStr += "<script type=\"text/javascript\" src=\"js/templates/connection.js\"></script> ";
		returnStr += "<script type=\"text/javascript\" src=\"js/templates/socket.js\"></script>";
		returnStr += "<link rel=\"stylesheet\" href=\"fileinspector.css\">";
		returnStr += "<script>" + js + "</script>";
		return returnStr + "</head>";
	}
	private static String getSummaryHTMLHead(String js) {
		String returnStr = "<head>";
		returnStr += "<title>File Inspector</title> ";
		returnStr += "<script type=\"text/javascript\" src=\"js/templates/summary.js\"></script>";
		returnStr += "<link rel=\"stylesheet\" href=\"fileinspector.css\">";
		returnStr += "<script>" + js + "</script>";
		return returnStr + "</head>";
	}
}

