package com.websecuritylab.tools.fileinspector;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
    private static final Logger logger = LoggerFactory.getLogger( Util.class );  
	final static int BUFFER = 2048;
	
	public static final String[] ignoreExt = new String[] { "gif", "png", "jpg", "jpeg", "svg", "css", "scss"};
	public static final Set<String> IGNORE_EXT = new HashSet<>(Arrays.asList(ignoreExt));
	
	public static final String HR_START = ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>";
	public static final String HR_END 	= "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<";
	
	public static String withSlashStar(String path) {
		if ( path.endsWith("*") ) return path;
		else if ( path.endsWith("/") ) return path + "*";
		else if ( path.endsWith("\\") ) return path + "*";
		else  return path + "/*"; 
	}


	
    private static final int BUFFER_SIZE = 4096;

    
    public static void unjar(String jarFilePath, File tempDir) throws IOException {
    	//String destDirectory = tempDir.getAbsolutePath();
    	String destDirectory = tempDir.getCanonicalPath();
            File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        logger.info("Unjaring file: " + jarFilePath + " to temp directory: " + tempDir);

    }
 

    
	public static void zipDir(String zipFileName, String dir) throws Exception {
		File dirObj = new File(dir);
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName));
		logger.info("Creating : " + zipFileName);
		addDir(dirObj, zos);
		zos.finish();
		//out.close();
	}

    private static void addDir(File dirObj, ZipOutputStream out) throws IOException {
        File[] files = dirObj.listFiles();
        byte[] tmpBuf = new byte[1024];

        for (int i = 0; i < files.length; i++) {
          if (files[i].isDirectory()) {
            addDir(files[i], out);
            continue;
          }
          
          //FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
          FileInputStream in = new FileInputStream(files[i].getCanonicalPath());
               
          // String filePath = files[i].getPath();				// Has short DOS filenames where users/scott.forbes  becomes users/SCOTT~1.FOR which does not work in PowerShell
          String filePath = files[i].getCanonicalPath();
          if ( filePath.contains(":")) filePath = filePath.substring( filePath.indexOf(":") + 1);  // Strip drive from Windows Path (D:/) because it is illegal for zip file
          if ( filePath.startsWith("/") || filePath.startsWith("\\") ) filePath = filePath.substring(1);  // Strip drive from Windows Path (D:/) because it is illegal for zip file

          logger.info(" Adding: " + filePath);
          out.putNextEntry(new ZipEntry(filePath));
          
          int len;
          while ((len = in.read(tmpBuf)) > 0) {
            out.write(tmpBuf, 0, len);
          }
          out.closeEntry();
          in.close();
        }
      }
    
    //public void unzip(String zipFilePath, String destDirectory) throws IOException {
    public static void unzip(String zipFilePath, File tempDir) throws IOException {
    	//String destDirectory = tempDir.getAbsolutePath();
    	String destDirectory = tempDir.getCanonicalPath();
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        logger.info("Unzipping file: " + zipFilePath + " to temp directory: " + tempDir);
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();  	// Error extracting file: C:\Users\scott\AppData\Local\Temp\fileinspector\META-INF/MANIFEST.MF
            //String filePath = destDirectory + "/" + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
    	try (
    			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));)
    	{
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }   		
    	}catch ( Exception e) {
    		logger.error("EEEEEEEEEEEEError extracting file: " + filePath + " with error: " + e.getMessage());
    	}

    }
	
	public static File createTempDir(String dirStr) throws IOException {
		String tempDirStr = File.createTempFile("temp-file", "tmp").getParent()+"/" + dirStr;
		File tempDir  = new File(tempDirStr);
		if (tempDir.exists()) {
			//emptyDir(tempDir);
			deleteDir(tempDir);
		} else {
			boolean createdTempFolder = tempDir.mkdirs();
			if (!createdTempFolder) throw new IOException();
			//logger.info("Created Temp Directory: "+tempDir.getAbsolutePath());			
			logger.info("Created Temp Directory: "+tempDir.getCanonicalPath());			
		}
		//System.out.println("GOt TEMP Path: " + tempDir.getPath());						// Has DOS short filenames that do not work with PowerShell
		//System.out.println("GOt TEMP CanonicalPath: " + tempDir.getCanonicalPath());    // Has long filenames that work with PowerShell
		//System.out.println("GOt TEMP AbsolutePath: " + tempDir.getAbsolutePath());		// Has DOS short filenames that do not work with PowerShell
		return tempDir;
	}

	public static boolean  deleteDir(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDir(file);
			}
		}
		return directoryToBeDeleted.delete();
	}
	
	public static String runCommand(boolean isLinux, String cmd, boolean isVerbose) throws IOException {
		StringBuffer output = new StringBuffer();
		logger.info("Running command: " + cmd);
        ProcessBuilder processBuilder = new ProcessBuilder();
        if ( isVerbose ) {
            System.out.println(HR_START);
            System.out.println("Running "+ ( isLinux ? "LINUX" : "WINDOWS" ) +" command: ");
            System.out.println();
            System.out.println(cmd);
            System.out.println();       	
        }
        //
        // This code HUNG on Windows 
        //
//       if ( isLinux )
//        	processBuilder.command("sh", "-c", cmd);
//        else
//        	processBuilder.command("cmd.exe", "/c", cmd);			// Windows
//
//        try {
//
//            Process process = processBuilder.start();
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
//                output.append(line);
//            }
//
//            int exitCode = process.waitFor();
//            logger.debug("\nExited with error code : " + exitCode);
//            if ( isVerbose ) System.out.println("\nExited with error code : " + exitCode);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
		final File tmp = File.createTempFile("fileAuditOut", null);
		try {
			tmp.deleteOnExit();

			if (isLinux)
				processBuilder.command("sh", "-c", cmd).redirectErrorStream(true).redirectOutput(tmp);
			else
				processBuilder.command("cmd.exe", "/c", cmd).redirectErrorStream(true).redirectOutput(tmp); // Windows

			final Process process = processBuilder.start();
			final int exitCode = process.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tmp)));
			String line = "";
			while ((line = reader.readLine()) != null) {
				//if (isVerbose) System.out.println(line);
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
	
	public static String readFile(String filePath) throws Exception 
	{ 
	    return new String(Files.readAllBytes(Paths.get(filePath))); 
	} 
	
	public static String getSeparatedStringFromFile(String filePath, String sep, String cmt) throws IOException {
		if ( filePath.length() == 0 ) return null;
		List<String> itemList =  readNonCommentLines(filePath, cmt);
		return String.join(sep,  itemList);
	}
	
	public static List<String> readNonCommentLines(String filePath, String c) throws IOException
	{
		if ( filePath.length() == 0 ) return null;
		List<String> lines = new ArrayList<>();
		try ( FileReader fr = new FileReader(filePath);
			  BufferedReader br = new BufferedReader(fr);){
			String line; 
			while ((line = br.readLine()) != null) {
				if ( line.length() == 0 ) continue;														// Ignore empty lines
				if ( !line.startsWith(c) ) {
					if ( line.contains(c) ) lines.add(line.substring(0,line.indexOf(c)).trim());
					else lines.add(line);
				}
			}
		} 
		return lines;			
	}
}
