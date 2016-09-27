# coding:utf-8
TOKENURL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="
MESGEURL = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token="
CORPID = "your corpid"
SECRET = "your secret"

TOUSER = "@all" #  @all - all users  ; "UserID1|UserID2|UserID3",
PARTYID = ""     #  " PartyID1 | PartyID2 ",
TAGID = ""   #  " TagID1 | TagID2 ",
AGENTID = 3   # your app's id

# loging settings
import logging
import logging.handlers
format = '%(asctime)s %(levelname)s %(message)s'
logFileName = r'output.log'
formatter = logging.Formatter(format)
wxLogger = logging.getLogger("infoLog")
wxLogger.setLevel(logging.INFO)
infoHandler = logging.handlers.RotatingFileHandler(
    logFileName, 'a', 1024*1024, 1,encoding = "UTF-8")
infoHandler.setLevel(logging.INFO)
infoHandler.setFormatter(formatter)
wxLogger.addHandler(infoHandler)
