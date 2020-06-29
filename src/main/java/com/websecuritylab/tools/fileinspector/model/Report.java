package com.websecuritylab.tools.fileinspector.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Report {

	
	public String app;
						// Use Maps to ensure unique paths
	private Map<String, FileSummary> fileSummaryMap = new HashMap<>();
	private Map<String, FileDetail> fileDetailMap = new HashMap<>();
	
						// Use Lists in JSON for readability and converting to JS objects
	//private List<FileSummary> summary;			
	//private List<List <FileDetail>> details;
	
	public Report(String app) {
		this.app = app;
	}
	
	public void addFileMatch(PowerShellSearchResult r) {
//		if ( fileDetails.containsKey(r.Filename)) {
//			
//		}else {
//			FileMatches fm = new FileMatches();
//			fileDetails.put( r.Filename, new FileMatches())
//		}
//		fileDetails.put(fm.filename, fm.matches);
		
		String fKey = r.Path;		
		String mKey = r.Matches.get(0);
		//String mKey = r.Matches.get(0);
		//if ( mKey.length() > 12 ) mKey = mKey.substring(0,SHOW) + SEP + mKey.length() + SEP + mKey.substring(mKey.length()-SHOW);
		//System.out.println("Adding Truncated match: " + mKey);

		if ( fKey.equals(mKey)) {
			// System.out.println("Ignoring filename match: " + mKey );
			return;
		}
	    if ( fileSummaryMap.containsKey( fKey )) {
	    	fileSummaryMap.get(fKey).addMatch(r);
	    }else {
	    	FileSummary fs = new FileSummary(r.Filename,r.Path);
	    	fs.addMatch(r);
	    	fileSummaryMap.put(fKey, fs);
	    }
	    
	    if ( fileDetailMap.containsKey( fKey )) {
	    	fileDetailMap.get(fKey).addMatch(r);
	    }else {
	    	FileDetail fd = new FileDetail(r.Filename,r.Path);
	    	fd.addMatch(r);
	    	fileDetailMap.put(fKey, fd);
	    }
	}

	public List<FileSummary> getSummary() {
		return new ArrayList<FileSummary>(fileSummaryMap.values());
	}

	public List<FileDetail> getDetail() {
		return new ArrayList<FileDetail>(fileDetailMap.values());
	}


	
	
}
