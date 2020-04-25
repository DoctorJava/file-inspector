// const summary =  [ {
// 		"filename" : "BlowfishEngine.java",
// 		"path" : "C:\\Users\\scott\\AppData\\Local\\Temp\\fileinspector\\decompiled\\org\\bouncycastle\\crypto\\engines\\BlowfishEngine.java",
// 		"matches" : [ {
// 			"pattern" : "Blowfish",
// 			"count" : 4
// 			} ]
// 		}
// 	]


function showSummaryMatches(matches){
	if ( !matches ) return ``;
	return `
			<td>${matches.map(match => `${match.pattern} | `).join("")}</td>		
			<td align=middle> | ${matches.map(match => `${match.count} | `).join("")}</td>		
	`;
}

var tableStart = `<table width="80%" border=1>
				<tr><th>#</th><th>File</th><th>Pattern</th><th>Count</th><th>Show the Details</th><th>Open File</th></tr>`;

var tableEnd = `</table>`;

var count = 0;

function summaryTemplate(summary) {
	if ( summary ) {
		detailUrl = window.location.href.replace("summary", "detail");
		return `
			<tr>
				<td>${++count}</td>
				<td>${summary.filename}</td>
				${showSummaryMatches(summary.matches)}
				<td align=middle><a href="${detailUrl}#${count}" target="_blank">Details</a></td>
				<td align=middle><a href="file://${summary.path}" target="_blank">File</a></td>
			</tr>
		`;
	}	
}