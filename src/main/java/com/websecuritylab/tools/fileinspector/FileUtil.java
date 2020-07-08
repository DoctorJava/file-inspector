package com.websecuritylab.tools.fileinspector;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.websecuritylab.tools.fileinspector.FileUtil.REPORT_TYPE;
import com.websecuritylab.tools.fileinspector.Main.FIND_EXT;


public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger( FileUtil.class );  
    
	private static final String SYNTAX = "java -jar file-inspector.jar ";
	private static final String FINISH_MSG = "Finished.";
	private static final String BASE_FILENAME = "file-inspector-";

	public enum REPORT_TYPE { summary, detail, json }			


	public static Collection<File> listFilesByExt(File dir, FIND_EXT fe) {
    	String ext = fe.toString();
    	if ( "clazz".equals(ext)) ext = "class";			// clazz is used as the enum because 
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
	
//	public static void outputJsonReport(String json, String filepath) throws IOException {
//		
//		try(BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))){
//		    //writer.write(JsonOutput.prettyPrint(json)); 
//		    writer.write(json); 
//		    //System.out.println("Output JSON file: " + OUT_JSON);
//		}
//	}
	public static String outputReport(REPORT_TYPE type, Properties props, String outFolder, String appName, String json) throws IOException {
		String outPath = outFolder + BASE_FILENAME + type +"_" + appName + (type==REPORT_TYPE.json? ".json" : ".html");

		try(BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))){
			switch(type)
			{
				case json:
					writer.write(json); 
					break;
				case summary:
				case detail:
					writer.write(convertJsToHtml( type, props, convertSummaryJsonToJs(json), true )); 
					break;
			}
		}	
		return outPath;
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
							    .replace("\"detail\":", " const detail = ")
							    .replace(", const", "; const");								// Replace commas with semi-colon between the objects.  Semicolon necessary if no line breaks
//		System.out.println("Converted JSON------------------");
//		System.out.println(json);
//		System.out.println("To JS ----------------------__--");
//		System.out.println(bracketStr);
		
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
//	public static String convertJsToSummaryHtml(String js, boolean isSingleFile) {
//		String returnStr = "<!DOCTYPE html><html>";
//		final String head = getSummaryHTMLHead(js);
//		final String body = "<body><h1><center>File Inspector</center></h1><div id=\"app\"></div> <script>document.getElementById(\"app\").innerHTML=`<h1>Summary</h1><ul>${summary.map(summaryTemplate).join(\"\")}</ul><h1>Details</h1><ul>${detail.map(detailTemplate).join(\\\"\\\")}</ul>`;</script></body>";
//		returnStr += head + body + "</html>";
//		return returnStr;
//	}
//	public static String convertJsToDetailHtml(String js, boolean isSingleFile) {
//		String returnStr = "<!DOCTYPE html><html>";
//		final String head = getSummaryHTMLHead(js);
//		final String body = "<body><h1><center>File Inspector</center></h1><div id=\"app\"></div> <script>document.getElementById(\"app\").innerHTML=`<h1>Summary</h1><ul>${summary.map(summaryTemplate).join(\"\")}</ul><h1>Details</h1><ul>${detail.map(detailTemplate).join(\\\"\\\")}</ul>`;</script></body>";
//		returnStr += head + body + "</html>";
//		return returnStr;
//	}
	public static String convertJsToHtml(REPORT_TYPE type, Properties props, String js, boolean isSingleFile) {
		String returnStr = "<!DOCTYPE html><html>";
		final String title = "File Inspector " + (type==REPORT_TYPE.summary? "Summary" : "Detail");
		final String propStr = props.toString().replace(", ", "<li>").replace("{", "<li>").replace("}", "");
		final String head = getHTMLHead(type, js);
		String body = "<body>";
			   body += "<h1><center>"+title+"</center></h1>";
			   body += "<h2>Scan Properties</h2><ul>"+propStr+"</ul><hr/>";
			   body += "<div id=\"app\"></div> ";
			   body += "<script>document.getElementById(\"app\").innerHTML=`";
			   if ( type==REPORT_TYPE.summary ) body += "${tableStart}${summary.map(summaryTemplate).join(\"\")}${tableEnd}";
			   if ( type==REPORT_TYPE.detail ) body += "<ul>${detail.map(detailTemplate).join(\"\")}</ul>";
			   body += "`;</script>";
			   body += "</body>";
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
	private static String getHTMLHead(REPORT_TYPE type, String js) {
		String returnStr = "<head>";
		returnStr += "<title>File Inspector "+(type==REPORT_TYPE.summary? "Summary" : "Details") + "</title> ";
		if ( type==REPORT_TYPE.summary ) returnStr += "<script type=\"text/javascript\" src=\"js/templates/summary.js\"></script>";
		if ( type==REPORT_TYPE.detail ) returnStr += "<script type=\"text/javascript\" src=\"js/templates/detail.js\"></script>";
		returnStr += "<link rel=\"stylesheet\" href=\"fileinspector.css\">";
		returnStr += "<script>" + js + "</script>";
		return returnStr + "</head>";
	}
}

