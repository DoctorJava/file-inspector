package com.websecuritylab.tools.fileinspector.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Report {
	public String app;
						// Use Maps to ensure unique paths
	private Map<String, FileSummary> fileSummaryMap = new HashMap<>();
	private Map<String, List<FileDetail>> fileDetailMap = new HashMap<>();
	
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
		if ( fKey.equals(mKey)) {
			System.out.println("Ignoring filename match: " + mKey );
			return;
		}
	    if ( fileSummaryMap.containsKey( fKey )) {
	    	// System.out.println("Already got: " + r.Filename);
	    	fileSummaryMap.get(fKey).addMatch(r);

	    	fileDetailMap.get(fKey).add(new FileDetail(r.Filename,r.Path));
	    }else {
	    	//System.out.println("Got new: " + r.Filename);
	    	
	    	FileSummary fs = new FileSummary(r.Filename,r.Path);
	    	fs.addMatch(r);
	    	fileSummaryMap.put(fKey, fs);
    	
	    	FileDetail fm = new FileDetail(r.Filename,r.Path);
	    	fm.addMatch(r);
	    	List<FileDetail> fmList = new ArrayList<>();
	    	fmList.add(fm);
	    	fileDetailMap.put(fKey, fmList);
	    }
	}

	public List<FileSummary> getSummary() {
		return new ArrayList<FileSummary>(fileSummaryMap.values());
	}

	public List<List <FileDetail>> getDetails() {
		return new ArrayList<List <FileDetail>>(fileDetailMap.values());
	}


	
	
}
