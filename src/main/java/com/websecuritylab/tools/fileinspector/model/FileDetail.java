package com.websecuritylab.tools.fileinspector.model;

import java.util.ArrayList;
import java.util.List;

public class FileDetail {
    public String filename;
    public String path;

    public List<Match> matches = new ArrayList<>();

	public FileDetail(String filename, String path) {
		this.filename = filename;
		this.path = path;
	}
	
	public void addMatch(PowerShellSearchResult r) {
		//System.out.println("Got Result: " + r.Context);
		matches.add( new Match(r.LineNumber, r.Line, r.Matches.get(0), r.Context));
	}

}
