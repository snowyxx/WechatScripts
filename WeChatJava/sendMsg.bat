@echo off
rem enterprise account: java -cp .;lib/* Messager <@all> <departmentNames> <tagNames> <Your [orther messga]>
rem service account: java -cp .;lib/* Messager <tagNames> <alarmurl> <severity> <alertType> <alertDate> <device> <monitorGroup> <Your [orther messga]>
rem example: 
rem sendMsg.bat ME测试 www.bing.com "$SEVERITY" "$MONITORTYPE" "$DATE" "$MONITORNAME" "$ATTRIBUTE" $RCAMSG_PLAINTEXT
rem sendMsg.bat ME测试 www.bing.com 停止 应用服务器 1999-1-1 server1 CRM系统 CPU利用率过高 88%
rem sendMsg.bat @all "" "" CPU利用率过高 88%

"..\jre\bin\java" -cp .;lib/* Messager %*