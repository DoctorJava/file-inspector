    // "connections": [
    //     {
    //         "hasUrlConnection": true,
    //         "hasServerSockets": false,
    //         "className": "SampleUrlConnection",
    //         "hasSockets": false,
    //         "packageName": "net.jakartaee.tools.sample.netconnections"
    //     },
    //     {
    //         "hasUrlConnection": true,
    //         "hasServerSockets": true,
    //         "className": "SampleServerSocket",
    //         "hasSockets": true,
    //         "packageName": "net.jakartaee.tools.sample.netconnections"
    //     },
    //     {
    //         "hasUrlConnection": true,
    //         "hasServerSockets": false,
    //         "className": "SampleClientSocket",
    //         "hasSockets": true,
    //         "packageName": "net.jakartaee.tools.sample.netconnections"
    //     }
    // ],


function showConnectionMethods(methods){
	// TODO: 
	// This threw an erro when method.params was null.  Not sure how to add IF_NULL statement
	// ${methods.map(method => ` ${method.verb} ( ${method.params.map(param => `${param} `).join("")} ), `).join("")}	


	if ( methods ){
		return `
			<p><strong>Methods: </strong>
					${methods.map(method => ` ${method.verb} ), `).join("")}		
			</p>
		`;		
	}

}

function showConnectionPatterns(patterns){
	if ( patterns ){
		return `
			<p><strong>URL Patterns: </strong>
				${patterns.map(pattern => ` ${pattern.path} `).join("")}		
			</p>
		`;
	}

}

function connectionTemplate(connection) {
	if ( connection ) {
		return `
			<li>
				<h2>${connection.className}</h2>
				<p><strong>Package: </strong>${connection.packageName}</p>
				<p><strong>Types:</strong>
					${connection.hasUrlConnection ?  'UrlConnection, ' : ''}
					${connection.hasSockets ?  'Sockets, ' : ''}
					${connection.hasServerSockets ?  'Server Sockets, ' : ''}
				</p>
			</li>
			<hr/> 
		`;
	}

}