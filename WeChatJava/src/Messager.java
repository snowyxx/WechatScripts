import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Messager {
	static String accountType;
	
	String corpid;
	String secret;
	String appid;
	
	String AppID;
	String AppSecret;
	String MsgTemplateId;
	public Messager(String file) throws FileNotFoundException, IOException{
		Properties prop = new Properties();
		prop.load(new FileInputStream(file));
		Messager.accountType=prop.getProperty("ACCOUNTTYPE", "").trim();
		this.corpid = prop.getProperty("corpid", "").trim();
		this.secret = prop.getProperty("secret", "").trim();
		String id = String.valueOf(prop.get("AGENTID"));
		this.appid = id!=null?id:"";
		
		this.AppID = prop.getProperty("AppID", "").trim();
		this.AppSecret = prop.getProperty("AppSecret", "").trim();
		this.MsgTemplateId = prop.getProperty("MsgTemplateId", "").trim();
		System.out.println("[-] New Messager created with account type: " + Messager.accountType);
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length<2){
			System.out.println("[!] Your input was not correct. Please check the readme");
			System.exit(999);
		}
		Messager msg = new Messager("conf.properties");
		String token = msg.fetchToken();
		StringBuffer sb = new StringBuffer();
		if ("service".equals(accountType)){
			String tagNames = args[0];
			String alarmurl = args[1];
			String severity = args[2];
			String alertType = args[3];
			String alertDate = args[4];
			String device = args[5];
			String monitorGroup = args[6];
			for (int i=7;i<args.length;i++){
				sb.append(args[i]+" ");
			}
			String rcaMessage = sb.toString().trim();
			
			String result = msg.sendMsg_sub(token,tagNames, alarmurl,severity,alertType,alertDate,device,monitorGroup,rcaMessage);
			System.out.println("[*] send message to service account result: "+result);
		}else if("enterprise".equals(accountType)){
			String toUser = args[0];
			String toParty = args[1];
			String toTag = args[2];
			for (int i=3;i<args.length;i++){
				sb.append(args[i]+" ");
			}
			String content = sb.toString().trim();
			String result = msg.sendMsg_ent(token, toUser, toParty, toTag, content);
			System.out.println("[*] send message to enterprise account result: "+result);
		}else if("subscribe".equals(accountType)){
			System.out.println("[!] subscribe account type is not supportted.");
		}else{
			System.out.println("[!] Your account type is not correct. It should be one of service, enterprise");
		}
	}
	

	/*
	Send message to Enterprise account. Using template OPENTM207112010
    Used API: https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1433751277&token=&lang=zh_CN
    Used Message Template: https://mp.weixin.qq.com/advanced/tmplmsg?action=tmpl_preview&t=tmplmsg/preview&id=OPENTM207112010&token=713914582&lang=zh_CN
	*/
	private String sendMsg_sub(String token, String tagNames, String alarmurl, String severity,
			String alertType, String alertDate, String device, String monitorGroup, String rcaMessage) throws Exception {
		
		HashSet<String> users = this.fetchServieUserIds(token, tagNames);
		StringBuffer result = new StringBuffer();
		String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="+token;
		String[] critcal = {"critical", "error", "down", "严重", "严重的", "停止", "错误", "服务停止"};
		String title = "--- 来自ME产品的运维告警 ---";
		String color = "#173177";
		String severityColor=color;
		if (Arrays.asList(critcal).contains(severity.toLowerCase())){
			severityColor = "#FF0000";
		}
		System.out.println("[*] Be going to send message to "+users.size()+" users." );
		for (String user : users){
			JSONObject postdata = new JSONObject();
			postdata.put("touser", user);
			postdata.put("url", alarmurl);
			postdata.put("template_id", MsgTemplateId);
			postdata.put("topcolor", color);
			
			JSONObject data = new JSONObject();
			
			JSONObject first = new JSONObject();
			first.put("value", title);
			first.put("color", "#673ab7");
			data.put("first", first);
			
			JSONObject node1 = new JSONObject();
			node1.put("value", severity);
			node1.put("color", severityColor);
			data.put("keyword1", node1);
			
			JSONObject node2 = new JSONObject();
			node2.put("value", alertType);
			node2.put("color", color);
			data.put("keyword2", node2);
			
			JSONObject node3 = new JSONObject();
			node3.put("value", alertDate);
			node3.put("color", color);
			data.put("keyword3", node3);
			
			JSONObject node4 = new JSONObject();
			node4.put("value", device);
			node4.put("color", color);
			data.put("keyword4", node4);
			
			JSONObject node5 = new JSONObject();
			node5.put("value", monitorGroup);
			node5.put("color", color);
			data.put("keyword5", node5);
			
			JSONObject remark = new JSONObject();
			remark.put("value", rcaMessage);
			data.put("remark", remark);
			
			postdata.put("data", data);
			
			String datastr = postdata.toString();
			System.out.println("[-] message template post data is \n"+datastr);
			result.append(postRequestWithString(url, datastr));
		}


		return result.toString();
	}

	/*
	 Get user id list of the users who taged by the 'tagname'
     Used API: https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140837&token=&lang=zh_CN
	*/ 
	private ArrayList<String> getUsers(String token, ArrayList<Integer> ids) throws Exception {
		ArrayList<String> result = new ArrayList<String>();
        String url = "https://api.weixin.qq.com/cgi-bin/user/tag/get?access_token="+token;
        for(int i=0;i<ids.size();i++){
        	JSONObject postdata = new JSONObject();
    		postdata.put("tagid", ids.get(i));
    		postdata.put("next_openid", "");
    		String data = postdata.toString();
    		String postResponse = postRequestWithString(url, data);
    		System.out.println("[-] get taged users API response is:\n"+postResponse);
    		// {"count":2,"data":{"openid":["osQhiuOAGCM96q6e8gAkTFpHf_60","osQhiuHgVRrK5ciDxVvk17OEU66Q"]},"next_openid":"osQhiuHgVRrK5ciDxVvk17OEU66Q"}
    		JSONObject resobj = new JSONObject(postResponse);
    		JSONObject dataobj = (JSONObject) resobj.get("data");
    		JSONArray idsobj = (JSONArray) dataobj.get("openid");
    		Iterator<Object> itor = idsobj.iterator();
    		while(itor.hasNext()){
    			String id = (String) itor.next();
    			if (!result.contains(id)){
    				result.add(id);
    			}
    		}
        }
        return result;
	}

	/*
	 Fetch tag id(s) of tag name(s)
     Used API: https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140837&token=&lang=zh_CN
	 */
	private ArrayList<Integer> getTagIds(String token, String tagNames) throws Exception {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		String names[];
		if (tagNames.indexOf("|")!=-1){
			names = tagNames.split("\\|");
		}else{
			names = new String[1];
			names[0]=tagNames;
		}
		List<String> namelist = Arrays.asList(names);
		System.out.println("[-] Going to get the tag ids of tag names: "+namelist);
		String url = "https://api.weixin.qq.com/cgi-bin/tags/get?access_token="+token;
		String tagResponse = getRequest(url);
		System.out.println("[-] Get tage name id API response:\n"+tagResponse);
		JSONObject jobj = new JSONObject(tagResponse);
		//{"tags":[{"id":2,"name":"星标组","count":0},{"id":100,"name":"ME测试","count":1},{"id":102,"name":"ME支持","count":0}]}
		JSONArray tagArray = jobj.getJSONArray("tags");
		Iterator<Object> iterator = tagArray.iterator();
		while (iterator.hasNext()){
			JSONObject ob = (JSONObject) iterator.next();
			String name = ob.getString("name");
			if (namelist.contains(name)){
				ids.add((Integer) ob.get("id"));
			}
		}
		return ids;
	}
	
	//send mssage API: http://qydev.weixin.qq.com/wiki/index.php?title=%E6%B6%88%E6%81%AF%E7%B1%BB%E5%9E%8B%E5%8F%8A%E6%95%B0%E6%8D%AE%E6%A0%BC%E5%BC%8F
	private String sendMsg_ent(String token, String toUser, String toParty, String toTag, String content) throws Exception {
		String senMessageUrl = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=";
		String url = senMessageUrl+token;
		String toPartyId = "";
		String toTagId = "";
		if (! "".equals(toParty)){
			toPartyId = getPartyIdByName(token, toParty);
			System.out.println("[-] message will send to department ids: "+ toPartyId);
		}
		if (! "".equals(toTag)){
			toTagId = getTagIdByName(token, toTag);
			System.out.println("[-] message will send to tag ids: "+ toTagId);
		}
		JSONObject data = new JSONObject();
		JSONObject contentObj = new JSONObject();
		contentObj.put("content", content);
		data.put("touser", toUser);
		data.put("toparty", toPartyId);
		data.put("totag", toTagId);
		data.put("msgtype", "text");
		data.put("agentid", Integer.valueOf(appid));
		data.put("text", contentObj);
		data.put("safe",0);
		String dataStr = data.toString();
		System.out.println("[-] Enterprise account message sending post data:\n"+dataStr);
		String reslut = postRequestWithString(url, dataStr);
		return reslut;
	}
	
	//get department list API: http://qydev.weixin.qq.com/wiki/index.php?title=%E7%AE%A1%E7%90%86%E9%83%A8%E9%97%A8
	private String getPartyIdByName(String token, String partyName) {
		String url = "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token="+token+"&id=";
		String ids= ""; // the string to return, it'd like id1|id2|id3 or id1
		String names[];
		if (partyName.indexOf("|")!=-1){
			names = partyName.split("\\|");
		}else{
			names = new String[1];
			names[0]=partyName;
		}
		List<String> namelist = new LinkedList<String>(Arrays.asList(names));
		System.out.println("[-] Be going to get the department ids of department names: "+namelist);
		String partyListRes = null;
		try {
			partyListRes = getRequest(url);
		} catch (Exception e) {
			return ids;
		}
		System.out.println("[-] get department list API response:\n"+partyListRes);
		JSONObject jobj = new JSONObject(partyListRes);
		int errcode =jobj.getInt("errcode");
		String errmsg =jobj.getString("errmsg");
		if (0 != errcode){
			System.out.println("[!] Can not get department list from Wechat: "+errmsg);
			return ids;
		}
		JSONArray partyArray = jobj.getJSONArray("department");
		Iterator<Object> iterator = partyArray.iterator();
		while (iterator.hasNext()){
			if(! "".equals(ids)){ids+="|";}
			JSONObject ob = (JSONObject) iterator.next();
			String name = ob.getString("name");
			String id = String.valueOf(ob.getInt("id"));
			if (namelist.contains(name)){
				ids+=id;
				namelist.remove(name);
			}
		}
		if (namelist.size()>0){
			System.out.println("[!] Can not get id for: "+namelist+". Please check if it associated to you application or not!!!");
		}
		return ids;
	}
	
	//get tag list API: http://qydev.weixin.qq.com/wiki/index.php?title=%E7%AE%A1%E7%90%86%E6%A0%87%E7%AD%BE
	private String getTagIdByName(String token, String tagName) {
		String url = "https://qyapi.weixin.qq.com/cgi-bin/tag/list?access_token="+token;
		String ids=""; // the string to return, it'd like id1|id2|id3 or id1
		String names[];
		if (tagName.indexOf("|")!=-1){
			names = tagName.split("\\|");
		}else{
			names = new String[1];
			names[0]=tagName;
		}
		List<String> namelist = new LinkedList<String>(Arrays.asList(names));
		System.out.println("[-] Be going to get the tag ids of department names: "+namelist);
		String tagListRes = null;
		try {
			tagListRes = getRequest(url);
		} catch (Exception e) {
			return ids;
		}
		System.out.println("[-] get department list API response:\n"+tagListRes);
		JSONObject jobj = new JSONObject(tagListRes);
		int errcode =jobj.getInt("errcode");
		String errmsg =jobj.getString("errmsg");
		if (0 !=errcode){
			System.out.println("[!] Can not get tag list from Wechat: "+errmsg);
			return ids;
		}
		JSONArray partyArray = jobj.getJSONArray("taglist");
		Iterator<Object> iterator = partyArray.iterator();
		while (iterator.hasNext()){
			if(! "".equals(ids)){ids+="|";}
			JSONObject ob = (JSONObject) iterator.next();
			String name = ob.getString("tagname");
			String id = String.valueOf(ob.getInt("tagid"));
			if (namelist.contains(name)){
				ids+=id;
				namelist.remove(name);
			}
		}
		if (namelist.size()>0){
			System.out.println("[!] Can not get id for: "+namelist+". Please check if it associated to you application or not!!!");
		}
		return ids;
	}

	private String fetchToken() throws Exception{
		Properties prop = new Properties();
		String token;
		int expires;
		Long time;
		String url = null;
		try{
			prop.load(new FileInputStream("st.properties"));
			token = prop.getProperty("access_token");
			expires = Integer.valueOf(prop.getProperty("expires_in"));
			time = Long.valueOf(prop.getProperty("time"));
			if ((System.currentTimeMillis()/1000 < (time/1000+expires)) && accountType.equals(prop.getProperty("type", ""))){
				return token;
			}
		}catch (IOException e){
			
		}
		if ("enterprise".equalsIgnoreCase(accountType) && (corpid != null) && (secret != null)){
			url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+secret;
		}else if ("service".equalsIgnoreCase(accountType) ||  "subscribe".equalsIgnoreCase(accountType) && (corpid != null) && (secret != null)){
			url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+AppID+"&secret="+AppSecret;
		}else{
			System.out.println("[!] Your account type is not correct. It should be one of service, subscribe,enterprise");
			return null;
		}
		String getTokenRes;
		
		try {
			getTokenRes = getRequest(url);
			//{"access_token":"mi3AX1J9fg9VYCuDhPnciCgqzAVKAU7e6apxm1rE-cR7-sOQQW03yzuoNRCLJr4j","expires_in":7200}
		} catch (Exception e1) {
			return null;
		}
		System.out.println("[-] get token API response:\n"+getTokenRes);
		JSONObject jobj = new JSONObject(getTokenRes);
		token =jobj.getString("access_token");
		expires =jobj.getInt("expires_in");
		time = System.currentTimeMillis();
		prop.setProperty("access_token", token);
		prop.setProperty("expires_in", String.valueOf(expires));
		prop.setProperty("time", String.valueOf(time));
		prop.setProperty("type", accountType);
		prop.store(new FileOutputStream("st.properties"), "store the new access kenston");	
		return token;
	}
	
	private HashSet<String> fetchServieUserIds(String token, String tagNames) throws Exception{
		Properties prop = new Properties();
		Long time;
		String serviceUsers;
		ArrayList<String> users;
		try{
			prop.load(new FileInputStream("st.properties"));
			serviceUsers = prop.getProperty("serviceUsers");
			time = Long.valueOf(prop.getProperty("serviceUsersUpdateTime"));
			if (System.currentTimeMillis()/1000 < (time/1000+3600*4)){
				if (serviceUsers.indexOf("|")!=-1){
					String[] ss = serviceUsers.split("\\|");
					users = new ArrayList<String>(Arrays.asList(ss));
				}else{
					users = new ArrayList<String>();
					users.add(serviceUsers);
				}
				return new HashSet<String>(users);
			}
		}catch (Exception e){
			
		}
		
		ArrayList<Integer> ids = this.getTagIds(token, tagNames);
		users = this.getUsers(token,ids);
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<users.size();i++){
			sb.append(users.get(i)+"|");
		}
		String unames = sb.toString();
		time = System.currentTimeMillis();
		prop.setProperty("serviceUsers", unames);
		prop.setProperty("serviceUsersUpdateTime", String.valueOf(time));
		prop.store(new FileOutputStream("st.properties"), "store the new taged name ids");
		return new HashSet<String>(users);
	}
	
	public static String getRequest(String url) throws Exception{
		String result="";
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try{
			HttpGet httpget = new HttpGet(url);
			System.out.println(httpget.getRequestLine());
			ResponseHandler<String> resHandler = new ResponseHandler<String>(){
				@Override
				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					int status =  response.getStatusLine().getStatusCode();
					if(status >= 200 && status < 300){
						HttpEntity entity = response.getEntity();
						return entity !=null?EntityUtils.toString(entity,"utf-8"):null;
					}else{
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};
			result = httpclient.execute(httpget, resHandler);
		}catch(java.net.UnknownHostException ne){
			System.out.println("[!] Can not connet to WeChat API serever. Please check your internet connection.");
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			httpclient.close();
		}
		return result;
	}
	
	public static String postRequestWithString(String url, String data) throws Exception{
		
		String result="";
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost postRequest = new HttpPost(url);
		StringEntity entity = new StringEntity(data,"utf-8");
		entity.setContentEncoding("UTF-8");    
		entity.setContentType("application/json");    
		postRequest.setEntity(entity);
		
		try{
			System.out.println(postRequest.getRequestLine());
			ResponseHandler<String> resHandler = new ResponseHandler<String>(){
				@Override
				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					int status =  response.getStatusLine().getStatusCode();
					if(status >= 200 && status < 300){
						HttpEntity entity = response.getEntity();
						return entity !=null?EntityUtils.toString(entity,"utf-8"):null;
					}else{
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};
			result = httpclient.execute(postRequest, resHandler);
		}catch(java.net.UnknownHostException ne){
			System.out.println("[!] Can not connet to WeChat API serever. Please check your internet connection.");
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			httpclient.close();
		}
		return result;
	}

}
