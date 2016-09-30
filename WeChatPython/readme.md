
Python 2.7

- set ACCOUNTTYPE with one of ["service"  "subscribe"  "enterprise"] in config.properties
- set up your detail in config.properties
	- MsgTemplateId:  your template id of OPENTM207112010
	- ToTags: the tag you have allociated to your users
- usage
	- python wxmsg.py <Your [orther messga]>
	- python wxmsg.py <alarmurl> <severity> <alertType> <alertDate> <device> <monitorGroup> <Your [orther messga]>
