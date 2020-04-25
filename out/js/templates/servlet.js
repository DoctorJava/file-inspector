	// "servlets": [
        // {
        //     "hasWebXml": false,
        //     "urlPatterns": [
                
        //     ],
        //     "className": "SingleWebXmlServlet",
        //     "packageName": "net.jakartaee.tools.sample.servlets",
        //     "methods": [
        //         "GET",
        //         "POST"
        //     ]
        // },
        // {
        //     "hasWebXml": false,
        //     "urlPatterns": [
        //         {
        //             "config": "Annotation",
        //             "path": "[\"/singleAnnot\"]"
        //         }
        //     ],
        //     "className": "SingleAnnotationServlet",
        //     "packageName": "net.jakartaee.tools.sample.servlets",
        //     "methods": [
        //         "GET"
        //     ]
        // },
        // {
        //     "hasWebXml": false,
        //     "urlPatterns": [
                
        //     ],
        //     "className": "MultiWebXmlServlet",
        //     "packageName": "net.jakartaee.tools.sample.servlets",
        //     "methods": [
        //         "POST"
        //     ]
        // },
        // {
        //     "hasWebXml": false,
        //     "urlPatterns": [
        //         {
        //             "config": "Annotation",
        //             "path": "[\"/multiAnnot1\", \"/multiAnnot2\"]"
        //         }
        //     ],
        //     "className": "MultipleAnnotationServlet",
        //     "packageName": "net.jakartaee.tools.sample.servlets",
        //     "methods": [
        //         "GET",
        //         "POST"
        //     ]
        // }
	
function showServletPatterns(patterns){
	if ( !patterns ) return ``;
	return `
		<p><strong>URL Patterns: </strong>
			${patterns.map(pattern => ` ${pattern.path} `).join("")}		
		</p>
	`;
}
function servletTemplate(servlet) {
        if (servlet){
                return `
                        <li>
                                <h2>${servlet.className}</h2>
                                <p><strong>Package: </strong>${servlet.packageName}</p>
                                <p><strong>Methods: </strong>${servlet.methods}</p>
                                ${showServletPatterns(servlet.urlPatterns)}
                        </li>
                        <hr/> 
                `;
        }

}