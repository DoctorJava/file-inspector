package com.websecuritylab.tools.fileinspector.model;

import java.util.List;

public class MatchCount {
	private int SHOW = 6;
	private String SEP1 = "--->";
	private String SEP2 = "<---";

    public String pattern;
    public Integer count;
    
	public MatchCount(String pattern, Integer count) {

		int length = pattern.length();
		if (pattern.startsWith("\"")) length -= 2; 				// Don't count starting ( and presumably ending) quote characters
		if ( pattern.length() > 12 ) pattern = pattern.substring(0,SHOW) + SEP1 + length + SEP2 + pattern.substring(pattern.length()-SHOW);
		//System.out.println("Adding Truncated match: " + pattern);
		
		this.pattern = pattern;

		this.count = count;
	}

    
    

}
