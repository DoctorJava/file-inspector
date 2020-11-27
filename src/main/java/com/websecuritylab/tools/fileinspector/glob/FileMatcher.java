package com.websecuritylab.tools.fileinspector.glob;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;


public class FileMatcher {
	private Path _rootDir;
	private PathMatcher _includeMatcher = null;				// If remains null, then the include matcher is not run
	private PathMatcher _excludeMatcher = null;				// If remains null, then the exclude matcher is not run
	
//	public FileMatcher(String rootPath, List<String> includeGlobs, List<String>  excludeGlobs) {
//		_rootDir = Paths.get(rootPath);
//		//System.out.println("GGGGGonna CONSTRUCT FileMatcher with ("+ (includeGlobs==null)+") incString: " + includeGlobs);
//		String incString = String.join(",", includeGlobs);
//		//System.out.println("CONSTRUCTING FileMatcher with ("+ (incString==null)+") incString: " + incString);
//		
//		setPatterns(incString, String.join(",", excludeGlobs));
//	}	
	
	public FileMatcher(String rootPath, String includeGlob, String excludeGlob) {
		_rootDir = Paths.get(rootPath);
		setPatterns(includeGlob, excludeGlob);
	}	
	
	public FileMatcher(String includePattern, String excludePattern) {
		setPatterns(includePattern, excludePattern);
	}		
	
	private void setPatterns(String includePatterns, String excludePatterns) {
		if ( includePatterns != null && includePatterns.length() > 0 ) _includeMatcher = FileSystems.getDefault().getPathMatcher("glob:{" + includePatterns + "}");
		if ( excludePatterns != null && excludePatterns.length() > 0 ) _excludeMatcher = FileSystems.getDefault().getPathMatcher("glob:{" + excludePatterns + "}");
	}


	private boolean isIncluded(Path path) {
		if ( _includeMatcher == null) {			// If not not specified, include everything that is not specifically excluded
			if ( _excludeMatcher == null || !_excludeMatcher.matches(path)) {
				System.out.println("INCLUDING: " + path);
				return true;
			}
		}
		else {
			if (_includeMatcher.matches(path)) {
				if ( _excludeMatcher == null || !_excludeMatcher.matches(path)) {
					System.out.println("INCLUDING: " + path);
					return true;
				}
			}
		}	
		return false;
	}
	
	private boolean isExcluded(Path path) {
		if ( _includeMatcher == null) {			// If not not specified, include everything that is not specifically excluded
			if ( _excludeMatcher != null && _excludeMatcher.matches(path)) {
				System.out.println("EXCLUDING: " + path);
				return true;						
			}
		}
		else {
			if (_includeMatcher.matches(path)) {
				if ( _excludeMatcher != null && _excludeMatcher.matches(path)) {
					System.out.println("EXCLUDING: " + path);
					return true;						
				}
			}
		}		
		return false;
	}
	
	public Boolean includesFile(String aFilePath) {
		return isIncluded(Paths.get(aFilePath));
	}
	public boolean excludesFile(String aFilePath) {
		return isExcluded(Paths.get(aFilePath));
	}

	public MatchReport getMatchReport() throws IOException {
		List<String> includedFiles = new ArrayList<>();
		List<String> excludedFiles = new ArrayList<>();
		List<String> ignoredFiles = new ArrayList<>();

		Files.walkFileTree(_rootDir, new SimpleFileVisitor<Path>() {
			
			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
			
//				if ( _includeMatcher == null) {			// If not not specified, include everything that is not specifically excluded
//					if ( _excludeMatcher != null && _excludeMatcher.matches(path)) {
//						System.out.println("EXCLUDING: " + path);
//						excludedFiles.add(path.toString());						
//					}
//					else {
//						System.out.println("INCLUDING: " + path);
//						includedFiles.add(path.toString());
//					}
//				}
//				else {
//					if (_includeMatcher.matches(path)) {
//						if ( _excludeMatcher != null && _excludeMatcher.matches(path)) {
//							System.out.println("EXCLUDING: " + path);
//							excludedFiles.add(path.toString());						
//						}
//						else {
//							System.out.println("INCLUDING: " + path);
//							includedFiles.add(path.toString());
//						}
//					}
//					else {
//						System.out.println("IGNORING: " + path);
//						ignoredFiles.add(path.toString());
//					}
//				}
				
				if ( isIncluded(path) ) includedFiles.add(path.toString());
				else if ( isExcluded(path) ) excludedFiles.add(path.toString());
				else ignoredFiles.add(path.toString());

				return FileVisitResult.CONTINUE;
			}

//			@Override
//			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
//				return FileVisitResult.CONTINUE;
//			}
		});
		
		return new MatchReport(includedFiles,excludedFiles, ignoredFiles);
	}

}
