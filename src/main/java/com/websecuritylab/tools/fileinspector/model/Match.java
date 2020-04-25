package com.websecuritylab.tools.fileinspector.model;

import java.util.List;

public class Match {
    public Integer lineNumber;
    public String line;
    public String match;
    public Context context;
    
	public Match(Integer lineNumber, String line, String match, Context context) {
		this.lineNumber = lineNumber;
		this.line = line;
		this.match = match;
		this.context = context;
	}
    
    

}
