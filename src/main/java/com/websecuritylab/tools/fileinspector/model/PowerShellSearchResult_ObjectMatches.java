package com.websecuritylab.tools.fileinspector.model;

import java.util.List;

public class PowerShellSearchResult_ObjectMatches {
	
    public Boolean IgnoreCase;
    public Integer LineNumber;
    public String Line;
    public String Filename;
    public String Path;
    public String Pattern;
    public Context Context;
    public List<PowerShellSearch_ObjectMatch> Matches;
    
    
    public PowerShellSearchResult_ObjectMatches() {};
    
//	[ {
//	  "IgnoreCase" : true,
//	  "LineNumber" : 67,
//	  "Line" : "            cipher = new BlowfishEngine();",
//	  "Filename" : "BlockCipherSpec.java",
//	  "Path" : "C:\\Users\\scott\\AppData\\Local\\Temp\\fileauditor\\decompiled\\org\\cryptacular\\spec\\BlockCipherSpec.java",
//	  "Pattern" : "rijndael|blowfish",
//	  "Context" : null,
//  "Matches": [
//  {
//      "Groups": "AnotherString",
//      "Success": true,
//      "Name": "0",
//      "Captures": "AnotherString",
//      "Index": 27,
//      "Length": 13,
//      "Value": "AnotherString"
//  }
//	} ]
}
