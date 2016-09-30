@echo off
rem java -cp .;lib/* Messager <@all> <Your [orther messga]>
rem java -cp .;lib/* Messager <tagNames> <alarmurl> <severity> <alertType> <alertDate> <device> <monitorGroup> <Your [orther messga]>
"..\jre\bin\java" -cp .;lib/* Messager %*