#!/usr/bin/evn python
#coding:utf-8
import sys
reload(sys)
sys.setdefaultencoding("utf-8")
from Config import wxLogger
from Config import CORPID
from Config import SECRET
from Config import TOUSER
from Config import PARTYID
from Config import TAGID
from Config import AGENTID
from Config import ACCOUNTTYPE
from Config import AppID
from Config import AppSecret
from Config import ToTags
from Config import MsgTemplateId

import urllib2
import json
import ast
import time

tokenurl_ent = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid={0}&corpsecret={1}"
mesgeurl_ent = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token="

tokenurl_sub = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={0}&secret={1}"
# mesgeurl_sub =  

class MXMessager():
    def getAccessToken(self):
        try:
            with open('at.json') as f:
                resDir = ast.literal_eval(f.read())
                if resDir.has_key('expires_in') and resDir['accountType'] == ACCOUNTTYPE:
                    if time.time() - resDir['time'] < resDir['expires_in'] - 600:
                        return resDir['access_token']
        except Exception, e:
            # wxLogger.info(e)
            pass
        if ACCOUNTTYPE == 'enterprise' and CORPID and SECRET:
            url = tokenurl_ent.format(CORPID, SECRET)
        elif ACCOUNTTYPE in ['service', 'subscribe'] and AppID and AppSecret:
            url = tokenurl_sub.format(AppID, AppSecret)
        try:
            response = urllib2.urlopen(url).read().decode('utf-8')
            resDir = json.loads(response)
            resDir['time'] = time.time()
            resDir['accountType'] = ACCOUNTTYPE
            json.dump(resDir, open('at.json', 'w'))
        except Exception, e:
            resDir = {}
            wxLogger.info('[!]EXCEPTION -- %s' % e)
        if resDir.has_key('access_token'):
            return resDir['access_token']
        else:
            wxLogger.info(resDir)
            wxLogger.info('[!]Can not get ACCESS_TOKEN, Going to exit.')
            sys.exit()

    def sendMesg_ent(self, access_token, content):
        '''
        Send message to Enterprise account
        '''
        url = mesgeurl_ent+access_token
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
        req.add_header('Content-Type','application/json;charset=utf-8')
        data = json.dumps(postdata, ensure_ascii=False)
        wxLogger.info('SEND MESSAGE -- %s' % data)
        response = urllib2.urlopen(req,data)
        return response.read() 
        
    def sendMesg_sub(self, access_token, msgurl, severity, alertType, alertDate, device, monitorGroup,rcaMessage ):
        '''
        Send message to Enterprise account. Using template OPENTM207112010
        Used API: https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1433751277&token=&lang=zh_CN
        Used Message Template: https://mp.weixin.qq.com/advanced/tmplmsg?action=tmpl_preview&t=tmplmsg/preview&id=OPENTM207112010&token=713914582&lang=zh_CN
        '''
        ids = self.getTagIds(access_token, ToTags)
        users = self.getSubTagedUsers(access_token, ids)
        title = "--- 运维告警 ---"
        url = 'https://api.weixin.qq.com/cgi-bin/message/template/send?access_token={0}'.format(access_token)
        if severity.lower() in ['critical', 'error', 'down', '严重', '严重的', '停止', '错误']:
            severitycolor="#FF0000"
        else:
            severitycolor="#173177"
        for user in users:
            try:
                color="#173177"
                data={"first":{"value":title, "color":"#673ab7"},"keyword1":{"value":severity,"color":severitycolor},"keyword2":{"value":alertType,"color":color},"keyword3":{"value":alertDate,"color":color},"keyword4":{"value":device,"color":color},"keyword5":{"value":monitorGroup,"color":color},"remark":{"value":rcaMessage}}
                dict_arr = {'touser': user, 'template_id':MsgTemplateId, 'url':msgurl, 'topcolor':color,'data':data}
                json_template = json.dumps(dict_arr)
                wxLogger.info('SEND MESSAGE -- %s' % json_template)
                response = urllib2.urlopen(url,json_template)
                wxLogger.info(response.read())
            except Exception, e:
                print ' ---- %r' % e
                wxLogger.info('[!] Could not send message to user %r.' % user)
                raise

    def getSubTagedUsers(self, access_token, tagname):
        '''
        Get user id list of the users who taged by the 'tagname'
        Used API: https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140837&token=&lang=zh_CN
        tagname: is a list
        '''
        ids = self.getTagIds(access_token, ToTags)
        if not ids:
            wxLogger.info('[!] Could not get tags id.')
            return None
        url = 'https://api.weixin.qq.com/cgi-bin/user/tag/get?access_token={0}'.format(access_token)
        users =[]
        for id in ids:
            try:
                postdata = {
                  "tagid" : id,
                  "next_openid":""
                  #TOFIX:  if send too many tags realistically, we should pass next_openid 
                }
                        
                req = urllib2.Request(url)
                req.add_header('Content-Type','application/json;charset=utf-8')
                data = json.dumps(postdata)
                response = urllib2.urlopen(req,data).read()
                # {"count":2,"data":{"openid":["osQhiuOAGCM96q6e8gAkTFpHf_60","osQhiuHgVRrK5ciDxVvk17OEU66Q"]},"next_openid":"osQhiuHgVRrK5ciDxVvk17OEU66Q"}
                resDir = json.loads(response)
                users +=  resDir['data']['openid']
            except Exception, e:
                wxLogger.info('[!] Could not get user id.')
        return list(set(users))

    def getTagIds(self, access_token, namelist):
        '''
        Fetch tag id(s) of tag name(s)
        Used API: https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140837&token=&lang=zh_CN
        '''
        try:
            url = 'https://api.weixin.qq.com/cgi-bin/tags/get?access_token={0}'.format(access_token)
            response = urllib2.urlopen(url).read().decode('utf-8')
            # {"tags":[{"id":2,"name":"星标组","count":0},{"id":100,"name":"ME测试","count":1},{"id":102,"name":"ME支持","count":0}]}
            resDir = json.loads(response)
        except Exception, e:
            return None
        taglist = resDir['tags']
        ids = [x['id'] for x in taglist if x['name'] in namelist]
        return ids
        
        
if __name__ == '__main__':
    try:
        if sys.platform == 'win32':
            args = [x.decode('gbk').encode('utf-8') for x in sys.argv]
        else:
            args = sys.argv
        if ACCOUNTTYPE in ["service", "subscribe"]:
            msgurl = args[1]
            severity = args[2]
            alertType = args[3]
            alertDate = args[4]
            device = args[5]
            monitorGroup = args[6]
            rcaMessage = args[7:]
            messager = MXMessager()
            access_token = messager.getAccessToken()
            result = messager.sendMesg_sub(access_token, msgurl, severity, alertType, alertDate, device, monitorGroup,rcaMessage )
            wxLogger.info(result)
        elif ACCOUNTTYPE == 'enterprise':
            msg = ' '.join(args[1:])
            messager = MXMessager()
            access_token = messager.getAccessToken()
            result = messager.sendMesg_ent(access_token, msg)
            wxLogger.info(result)
        else:
            print 'Please setup correct account type.'
            wxLogger.info('[!] Please input correct account type.')
    except Exception, e:
        print 'Please input correct parameters.'
        wxLogger.info(e)