package com.websecuritylab.tools.fileinspector.model;

import java.util.Arrays;
import java.util.List;

public class PowerShellSearchResult {
	
    public Boolean IgnoreCase;
    public Integer LineNumber;
    public String Line;
    public String Filename;
    public String Path;
    public String Pattern;
    public Context Context;
    public List<String> Matches;
    
    
    public PowerShellSearchResult() {}


	public PowerShellSearchResult(PowerShellSearchResult_ObjectMatches pr_om) {
		IgnoreCase = pr_om.IgnoreCase;
		LineNumber = pr_om.LineNumber;
		Line = pr_om.Line;
		Filename = pr_om.Filename;
		Path = pr_om.Path;
		Pattern = pr_om.Pattern;
		Context = pr_om.Context;
		//System.out.println("Got Match: " + pr_om.Matches.get(0).Value);
		Matches = Arrays.asList(pr_om.Matches.get(0).Value);
	};
    
    
    
//	[ {
//	  "IgnoreCase" : true,
//	  "LineNumber" : 67,
//	  "Line" : "            cipher = new BlowfishEngine();",
//	  "Filename" : "BlockCipherSpec.java",
//	  "Path" : "C:\\Users\\scott\\AppData\\Local\\Temp\\fileauditor\\decompiled\\org\\cryptacular\\spec\\BlockCipherSpec.java",
//	  "Pattern" : "rijndael|blowfish",
//	  "Context" : null,
//	  "Matches" : [ "Blowfish" ]
//	} ]
}
