@echo off
rem python wxmsg.py <Your [orther messga]>
rem python wxmsg.py <alarmurl> <severity> <alertType> <alertDate> <device> <monitorGroup> <Your [orther messga]>
rem example: sendMsg.bat www.bing.com 停止 应用服务器 1999-1-1 server1 CRM系统 CPU利用率过高 88%
rem example: sendMsg.bat www.bing.com "$SEVERITY" "$MONITORTYPE" "$DATE" "$MONITORNAME" "$ATTRIBUTE" $RCAMSG_PLAINTEXT
c:\Python27\python wxmsg.py %*