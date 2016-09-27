import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.json.JSONObject;

public class Messager {
	String corpid;
	String secret;
	String appid;
	String getTokenUrl;
	String senMessageUrl = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=";
	public Messager(String file) throws FileNotFoundException, IOException{
		Properties prop = new Properties();
		prop.load(new FileInputStream(file));
		this.corpid = prop.getProperty("corpid").trim();
		this.secret = prop.getProperty("secret").trim();
		this.appid = String.valueOf(prop.get("AGENTID"));
		this.getTokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+secret;
	}
	
	public static void main(String[] args) throws Exception {
//		Usage java Messager toUser content
		Messager msg = new Messager("conf.properties");
		String token = msg.fetchToken();
		String toUser = "@all";
		String content = "java²âÊÔ";
		if (args.length > 2){
			toUser = args[0];
			StringBuffer sb = new StringBuffer();
			for (int i=1;i<args.length;i++){
				sb.append(args[i]+" ");
			}
			content = sb.toString().trim();
		}
		String result = msg.sendMsg(token,toUser, content);
		System.out.println(result);
	}
	private String sendMsg(String token, String toUser, String content) throws Exception {
		String url = senMessageUrl+token;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost postRequest = new HttpPost(url);
//        postdata = {
//                "touser": TOUSER,
//                "toparty": PARTYID,
//                "totag": TAGID,
//                "msgtype": "text",
//                "agentid": AGENTID,
//                "text": {
//                    "content": content
//                },
//                "safe":0
//             }
		JSONObject data = new JSONObject();
		JSONObject contentObj = new JSONObject();
		contentObj.put("content", content);
		data.put("touser", toUser);
		data.put("toparty", "");
		data.put("totag", "");
		data.put("msgtype", "text");
		data.put("agentid", Integer.valueOf(appid));
		data.put("text", contentObj);
		data.put("safe",0);
		String dataStr = data.toString();
		System.out.println(dataStr);
		StringEntity entity = new StringEntity(dataStr,"utf-8");
		entity.setContentEncoding("UTF-8");    
        entity.setContentType("application/json");    
		postRequest.setEntity(entity);
		System.out.println(postRequest.getRequestLine());
		HttpResponse postResponse = httpclient.execute(postRequest);
		String reslut = EntityUtils.toString(postResponse.getEntity());
		return reslut;
	}
	private String fetchToken() throws Exception{
		Properties prop = new Properties();
		String token;
		int expires;
		Long time;
		try{
			prop.load(new FileInputStream("st.properties"));
			token = prop.getProperty("access_token");
			expires = Integer.valueOf(prop.getProperty("expires_in"));
			time = Long.valueOf(prop.getProperty("time"));
			if (System.currentTimeMillis()/1000 < (time/1000+expires)){
				return token;
			}
		}catch (IOException e){
			
		}
		String getTokenRes;
		try {
			getTokenRes = getAccessToken(getTokenUrl);
			//{"access_token":"mi3AX1J9fg9VYCuDhPnciCgqzAVKAU7e6apxm1rE-cR7-sOQQW03yzuoNRCLJr4j","expires_in":7200}
		} catch (Exception e1) {
			return null;
		}
		System.out.println(getTokenRes);
		JSONObject jobj = new JSONObject(getTokenRes);
		token =jobj.getString("access_token");
		expires =jobj.getInt("expires_in");
		time = System.currentTimeMillis();
		prop.setProperty("access_token", token);
		prop.setProperty("expires_in", String.valueOf(expires));
		prop.setProperty("time", String.valueOf(time));
		prop.store(new FileOutputStream("st.properties"), "store the new access kenston");	
		return token;
	}
	public static String getAccessToken(String url) throws Exception{
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
						return entity !=null?EntityUtils.toString(entity):null;
					}else{
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};
			result = httpclient.execute(httpget, resHandler);
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			httpclient.close();
		}
		return result;
	}
	

}
