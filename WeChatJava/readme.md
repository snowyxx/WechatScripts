### Usage:

- set ACCOUNTTYPE with one of ["service" "enterprise"] \(could not be  "subscribe" \)in config.properties
- set up your detail in config.properties
	- MsgTemplateId:  your template id of OPENTM207112010
- usage
	- __Enterprise Account__ `java -cp .;lib/* Messager <@all> <Your [orther messga]>`
	- __Service/Subscribe Account__ `java -cp .;lib/* Messager <tagNames> <alarmurl> <severity> <alertType> <alertDate> <device> <monitorGroup> <Your [orther messga]>`
		
        > tagNames : "tagname" or "tagename1|tagname2|tagname3". 
        
        > __If you change the tag tagname or add/remove users from a tag, del st.properties to get update now. otherwise, it only get updage very 4 hours.__

---

JSON lib

https://github.com/stleary/JSON-java


httpclient 4.5

http://hc.apache.org/httpcomponents-client-4.5.x/index.html
