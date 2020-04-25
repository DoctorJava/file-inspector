const servlets = [
        {
            "hasWebXml": false,
            "urlPatterns": [
                
            ],
            "className": "SingleWebXmlServlet",
            "packageName": "net.jakartaee.tools.sample.servlets",
            "methods": [
                "GET",
                "POST"
            ]
        },
        {
            "hasWebXml": false,
            "urlPatterns": [
                {
                    "config": "Annotation",
                    "path": "[\"/singleAnnot\"]"
                }
            ],
            "className": "SingleAnnotationServlet",
            "packageName": "net.jakartaee.tools.sample.servlets",
            "methods": [
                "GET"
            ]
        },
        {
            "hasWebXml": false,
            "urlPatterns": [
                
            ],
            "className": "MultiWebXmlServlet",
            "packageName": "net.jakartaee.tools.sample.servlets",
            "methods": [
                "POST"
            ]
        },
        {
            "hasWebXml": false,
            "urlPatterns": [
                {
                    "config": "Annotation",
                    "path": "[\"/multiAnnot1\", \"/multiAnnot2\"]"
                }
            ],
            "className": "MultipleAnnotationServlet",
            "packageName": "net.jakartaee.tools.sample.servlets",
            "methods": [
                "GET",
                "POST"
            ]
        }
	];

const services = [
	{
		"urlPatterns": [
			{
				"config": "Annotation",
				"path": "/book"
			}
		],
		"type": "REST",
		"className": "SingleUrlRestService",
		"packageName": "net.jakartaee.tools.sample.services",
		"methods": [
			{
				"verb": "GET",
				"params": [
					
				],
				"method": "getBooks"
			},
			{
				"verb": "GET",
				"params": [
					"id"
				],
				"method": "getBookById"
			}
		]
	},
	{
		"urlPatterns": [
			{
				"config": "Annotation",
				"path": "/{a:path1|path2}"
			}
		],
		"type": "REST",
		"className": "MultipleUrlRestService",
		"packageName": "net.jakartaee.tools.sample.services",
		"methods": [
			{
				"verb": "GET",
				"params": [
					
				],
				"method": "getBooks"
			},
			{
				"verb": "GET",
				"params": [
					"id"
				],
				"method": "getBookById"
			}
		]
	}
]

const connections = [
        {
            "hasUrlConnection": true,
            "hasServerSockets": false,
            "className": "SampleUrlConnection",
            "hasSockets": false,
            "packageName": "net.jakartaee.tools.sample.netconnections"
        },
        {
            "hasUrlConnection": true,
            "hasServerSockets": true,
            "className": "SampleServerSocket",
            "hasSockets": true,
            "packageName": "net.jakartaee.tools.sample.netconnections"
        },
        {
            "hasUrlConnection": true,
            "hasServerSockets": false,
            "className": "SampleClientSocket",
            "hasSockets": true,
            "packageName": "net.jakartaee.tools.sample.netconnections"
		}
	]

const sockets = [
        {
            "hasWebXml": false,
            "className": "SampleWebSocket",
            "packageName": "net.jakartaee.tools.sample.sockets",
            "endpoints": [
                {
                    "type": "Server",
                    "path": "[@javax.websocket.server.ServerEndpoint(\"/endpoint\")]"
                }
            ]
        }
    ]