# coding:utf-8

ACCOUNTTYPE = "subscribe" # value could be one of ["service"  "subscribe"  "enterprise"]

# enterprise account
CORPID = "Your enterprise account's corpid"
SECRET = "Your enterprise account's corpid secret"
TOUSER = "@all"  #  @all - all users  ; "UserID1|UserID2|UserID3",    # Enterprise account
PARTYID = ""      #  " PartyID1 | PartyID2 ",                          # Enterprise account
TAGID = ""    #  " TagID1 | TagID2 ",                                  # enterprise/subscribe account
AGENTID = 3   # app's id                                              # Enterprise account

# service, subscribe account
AppID = "Your subscribe/service account's app id" 
AppSecret = "Your subscribe/service account's secret"
ToTags = ['ME测试']  # the tag you have allociate to your users
MsgTemplateId = 'your template id of OPENTM207112010'


# loging settings
import logging
import logging.handlers
format = '%(asctime)s %(levelname)s %(message)s'
logFileName = r'output.log'
formatter = logging.Formatter(format)
wxLogger = logging.getLogger("infoLog")
wxLogger.setLevel(logging.INFO)
infoHandler = logging.handlers.RotatingFileHandler(
    logFileName, 'a', 1024*1024, 1)
infoHandler.setLevel(logging.INFO)
infoHandler.setFormatter(formatter)
wxLogger.addHandler(infoHandler)
