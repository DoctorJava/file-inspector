  
const servlets = [
        {
            "hasWebXml": false,
            "urlPatterns": [
                
            ],
            "className": "LessonSource",
            "packageName": "org.owasp.webgoat",
            "methods": [
                "POST"
            ]
        },
        {
            "hasWebXml": false,
            "urlPatterns": [
                
            ],
            "className": "HammerHead",
            "packageName": "org.owasp.webgoat",
            "methods": [
                "GET",
                "POST"
            ]
        },
        {
            "hasWebXml": false,
            "urlPatterns": [
                
            ],
            "className": "Catcher",
            "packageName": "org.owasp.webgoat",
            "methods": [
                "POST"
            ]
        },
        {
            "hasWebXml": false,
            "urlPatterns": [
                
            ],
            "className": "Controller",
            "packageName": "org.owasp.webgoat.servlets",
            "methods": [
                "GET",
                "POST"
            ]
        }
    ];
const services = [

]

const connections = [
       {
            "hasUrlConnection": true,
            "hasServerSockets": false,
            "className": "Challenge2Screen",
            "hasSockets": true,
            "packageName": "org.owasp.webgoat.lessons"
        },
        {
            "hasUrlConnection": true,
            "hasServerSockets": false,
            "className": "BlindScript",
            "hasSockets": false,
            "packageName": "org.owasp.webgoat.lessons"
        },
        {
            "hasUrlConnection": true,
            "hasServerSockets": false,
            "className": "AbstractLesson",
            "hasSockets": false,
            "packageName": "org.owasp.webgoat.lessons"
        },
        {
            "hasUrlConnection": true,
            "hasServerSockets": false,
            "className": "Interceptor",
            "hasSockets": true,
            "packageName": "org.owasp.webgoat.util"
        }
]		
	  

const sockets = [
     ]