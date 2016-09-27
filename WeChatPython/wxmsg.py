#!C:\Python27\python.exe
#coding:utf-8
import sys
reload(sys)
sys.setdefaultencoding("utf-8")
from Config import wxLogger
from Config import TOKENURL
from Config import MESGEURL
from Config import CORPID
from Config import SECRET
from Config import TOUSER
from Config import PARTYID
from Config import TAGID
from Config import AGENTID

import urllib2
import json
import ast
import time
class MXMessager():
    def getAccessToken(self):
        try:
            with open('at.json') as f:
                resDir = ast.literal_eval(f.read())
                if resDir.has_key('expires_in'):
                    if time.time() - resDir['time'] < resDir['expires_in'] - 600:
                        return resDir['access_token']
        except Exception, e:
            # wxLogger.info(e)
            pass
        url = TOKENURL+CORPID+'&corpsecret='+SECRET
        try:
            response = urllib2.urlopen(url).read().decode('utf-8')
            resDir =  json.loads(response)
            resDir['time'] = time.time()
            json.dump(resDir, open('at.json', 'w'))
        except Exception, e:
            resDir ={}
            wxLogger.info('[!]EXCEPTION -- %s' % e)
        if resDir.has_key('access_token'):
            return resDir['access_token']
        else:
            wxLogger.info(resDir)
            wxLogger.info('[!]Can not get ACCESS_TOKEN, Going to exit.')
            sys.exit()
   
    def sendMesg(self,acess_token, content):
        url = MESGEURL+acess_token
        postdata = {
           "touser": TOUSER,
           "toparty": PARTYID,
           "totag": TAGID,
           "msgtype": "text",
           "agentid": AGENTID,
           "text": {
               "content": content
           },
           "safe":0
        }
        
        req = urllib2.Request(url)
        # req.add_header('Content-Type','application/json;charset=utf-8')
        data = json.dumps(postdata, ensure_ascii=False)
        wxLogger.info('SEND MESSAGE -- %s' % data)
        response = urllib2.urlopen(req,data)
        return response.read() 
if __name__ == '__main__':
    msg ="No content passed"
    if len(sys.argv)>1:
        msg = ' '.join(sys.argv[1:])
        if sys.platform == 'win32':
            msg = ' '.join(sys.argv[1:]).decode('gbk').encode('utf-8')
    else:
        sys.exit()
    messager = MXMessager()
    access_token = messager.getAccessToken()
    result = messager.sendMesg(access_token, msg)
    wxLogger.info(result)