package com.websecuritylab.tools.fileinspector.glob;

import java.util.List;

public class MatchReport {
	private List<String> _includedFiles;
	private List<String> _excludedFiles;
	private List<String> _ignoredFiles;
	
	public MatchReport(List<String> includedFiles, List<String> excludedFiles, List<String> ignoredFiles) {
		_includedFiles = includedFiles;
		_excludedFiles = excludedFiles;
		_ignoredFiles = ignoredFiles;
	}

	public boolean includesFile(String aFilePath) {
		return _includedFiles.contains(aFilePath);
	}
	public boolean excludesFile(String aFilePath) {
		return _excludedFiles.contains(aFilePath);
	}

	
	public List<String> getIncludedFiles() {
		return _includedFiles;
	}

	public List<String> getExcludedFiles() {
		return _excludedFiles;
	}

	public List<String> getIgnoredFiles() {
		return _ignoredFiles;
	}

}
