package com.websecuritylab.tools.fileinspector.model;

import java.util.ArrayList;
import java.util.List;

public class FileDetail {
	private int SHOW = 5;
	private String SEP1 = "--->";
	private String SEP2 = "<---";

	public String filename;
    public String path;

    public List<Match> matches = new ArrayList<>();

	public FileDetail(String filename, String path) {
		this.filename = filename;
		this.path = path;
	}
	
	public void addMatch(PowerShellSearchResult r) {
		//System.out.println("Got Result: " + r.Context);
		String pattern = r.Matches.get(0);
		int length = pattern.length();
		if (pattern.startsWith("\"")) length -= 2; 				// Don't count starting ( and presumably ending) quote characters
		if ( pattern.length() > 12 ) pattern = pattern.substring(0,SHOW) + SEP1 + length + SEP2 + pattern.substring(pattern.length()-SHOW);
		//System.out.println("Adding Truncated match: " + pattern);

		matches.add( new Match(r.LineNumber, r.Line, pattern, r.Context));
	}

}
