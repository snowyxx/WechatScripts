### Usage:

- set ACCOUNTTYPE with one of ["service"  "subscribe"  "enterprise"] in config.properties
- set up your detail in config.properties
	- MsgTemplateId:  your template id of OPENTM207112010
- usage
	- java -cp .;lib/* Messager <@all> <Your [orther messga]>
	- java -cp .;lib/* Messager <tagNames> <alarmurl> <severity> <alertType> <alertDate> <device> <monitorGroup> <Your [orther messga]>
		> tagNames : "tagname" or "tagename1|tagname2|tagname3". 

---

JSON lib

https://github.com/stleary/JSON-java


httpclient 4.5

http://hc.apache.org/httpcomponents-client-4.5.x/index.html
