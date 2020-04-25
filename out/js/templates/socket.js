// sockets = [
//         {
//             "hasWebXml": false,
//             "className": "SampleWebSocket",
//             "packageName": "net.jakartaee.tools.sample.sockets",
//             "endpoints": [
//                 {
//                     "type": "Server",
//                     "path": "[@javax.websocket.server.ServerEndpoint(\"/endpoint\")]"
//                 }
//             ]
//         }
//     ]



function showSocketEndpoints(endpoints){
	if ( !endpoints ) return ``;
	return `
		<p><strong>Endpoints: </strong>
			${endpoints.map(endpoint => ` ${endpoint.path} `).join("")}		
		</p>
	`;
}

function socketTemplate(socket) {
	if ( socket ) {
		return `
			<li>
				<h2>${socket.className}</h2>
				<p><strong>Package: </strong>${socket.packageName}</p>
				${showSocketEndpoints(socket.endpoints)}
			</li>
			<hr/> 
		`;
	}	

}