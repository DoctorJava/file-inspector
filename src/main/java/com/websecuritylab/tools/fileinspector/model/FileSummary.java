package com.websecuritylab.tools.fileinspector.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSummary {
    public String filename;
    public String path;
    private Map<String, MatchCount> matchCountMap = new HashMap<>();
    
    //public List<Match> matches;
	
    public FileSummary(String filename, String path) {
		this.filename = filename;
		this.path = path;
	}
    
	public void addMatch(PowerShellSearchResult r) {
		String mKey = r.Matches.get(0);
    	if ( matchCountMap.containsKey( mKey )) {
    		MatchCount mc = matchCountMap.get(mKey);
    		mc.count += 1;
    		matchCountMap.put(mKey, mc);    		
    	} else {
    		matchCountMap.put(mKey, new MatchCount( mKey, new Integer(1))); 
    	}
	}

	public List<MatchCount> getMatches() {
		return new ArrayList<MatchCount>(matchCountMap.values());
	}
	
	
}
