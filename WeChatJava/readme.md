### Usage:

- set ACCOUNTTYPE with one of ["service" "enterprise"] \(could not be  "subscribe" \)in config.properties
- set up your detail in conf.properties
	- **MsgTemplateId**:  your template id of OPENTM207112010
- command:
	- __Enterprise Account__ 
    
        `java -cp .;lib/* Messager <@all> <departmentNames> <tagNames> <Your [orther messga]>`
        
        > <@all> : to all users associted to this application. If it secified, followed departmentNames and tagNames will not work, otherwise, just give "".
        
        > departmentNames : "departmentname" or "departmentename1|departmentname2|departmentname3". 
        
        > tagNames : "tagname" or "tagename1|tagname2|tagname3". 
    
	- __Service/Subscribe Account__ 
    
        `java -cp .;lib/* Messager <tagNames> <alarmurl> <severity> <alertType> <alertDate> <device> <monitorGroup> <Your [orther messga]>`
		
        > tagNames : "tagname" or "tagename1|tagname2|tagname3". 
        
        > __If you change the tag name or add/remove users from a tag, delete st.properties file to get update now. otherwise, it only get updage very 4 hours.__

---

JSON lib

https://github.com/stleary/JSON-java


httpclient 4.5

http://hc.apache.org/httpcomponents-client-4.5.x/index.html

### related video

https://pan.baidu.com/s/1gflARGR
https://v.qq.com/x/page/e052186znfd.html