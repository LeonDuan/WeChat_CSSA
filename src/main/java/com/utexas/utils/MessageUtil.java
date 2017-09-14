//A WeChat XML Parser found online
//We will only be using 文本 type messages
//has the potential of expanding into something more useful

package com.utexas.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;


public class MessageUtil {  

    /** 
     * 返回消息类型：文本 
     */  
    public static final String RESP_MESSAGE_TYPE_TEXT = "text";  

    /** 
     * 返回消息类型：音乐 
     */  
    public static final String RESP_MESSAGE_TYPE_MUSIC = "music";  

    /** 
     * 返回消息类型：图文 
     */  
    public static final String RESP_MESSAGE_TYPE_NEWS = "news";  

    /** 
     * 请求消息类型：文本 
     */  
    public static final String REQ_MESSAGE_TYPE_TEXT = "text";  

    /** 
     * 请求消息类型：图片 
     */  
    public static final String REQ_MESSAGE_TYPE_IMAGE = "image";  

    /** 
     * 请求消息类型：链接 
     */  
    public static final String REQ_MESSAGE_TYPE_LINK = "link";  

    /** 
     * 请求消息类型：地理位置 
     */  
    public static final String REQ_MESSAGE_TYPE_LOCATION = "location";  

    /** 
     * 请求消息类型：音频 
     */  
    public static final String REQ_MESSAGE_TYPE_VOICE = "voice";  

    /** 
     * 请求消息类型：推送 
     */  
    public static final String REQ_MESSAGE_TYPE_EVENT = "event";  

    /** 
     * 事件类型：subscribe(订阅) 
     */  
    public static final String EVENT_TYPE_SUBSCRIBE = "subscribe";  

    /** 
     * 事件类型：unsubscribe(取消订阅) 
     */  
    public static final String EVENT_TYPE_UNSUBSCRIBE = "unsubscribe";  

    /** 
     * 事件类型：CLICK(自定义菜单点击事件) 
     */  
    public static final String EVENT_TYPE_CLICK = "CLICK";  
    
    
    
    public static String generateXmlMsg(String from, String to, String createTime, String content) {
    	String respMessage = "<xml>"
    			+ "<ToUserName><![CDATA[" + to + "]]></ToUserName>"
    			+ "<FromUserName><![CDATA["+ from + "]]></FromUserName>"
    			+ "<CreateTime>" + createTime + "</CreateTime>"+"<MsgType><![CDATA[text]]></MsgType>"
    			+ "<Content><![CDATA[" + content + "]]></Content>"
    			+ "</xml>";
    	return respMessage;
    };

    /**
     * @Description: 解析微信发来的请求（XML） 
     * @param @param request
     * @param @return
     * @param @throws Exception   
     * @author dapengniao
     * @date 2016 年 3 月 7 日 上午 10:04:02
     */
    public static Map<String, String> parseXml(HttpServletRequest request) throws Exception {  
        // 将解析结果存储在 HashMap 中   
        Map<String, String> map = new HashMap<String, String>();  

        // 从 request 中取得输入流   
        InputStream inputStream = request.getInputStream();  
        // 读取输入流   
        SAXReader reader = new SAXReader();  
        Document document = reader.read(inputStream);  
        // 得到 xml 根元素   
        Element root = document.getRootElement();  
        // 得到根元素的所有子节点   
        List<Element> elementList = root.elements();  

        // 遍历所有子节点   
        for (Element e : elementList)  
            map.put(e.getName(), e.getText());  

        // 释放资源   
        inputStream.close();  
        inputStream = null;  

        return map;  
    }  

    @SuppressWarnings("unused")
    private static XStream xstream = new XStream(new XppDriver() {  
        public HierarchicalStreamWriter createWriter(Writer out) {  
            return new PrettyPrintWriter(out) {  
                // 对所有 xml 节点的转换都增加 CDATA 标记   
                boolean cdata = true;  
                @SuppressWarnings("rawtypes")
                public void startNode(String name, Class clazz) {  
                    super.startNode(name, clazz);  
                }  

                protected void writeText(QuickWriter writer, String text) {  
                    if (cdata) {  
                        writer.write("<![CDATA[");  
                        writer.write(text);  
                        writer.write("]]>");  
                    } else {  
                        writer.write(text);  
                    }  
                }  
            };  
        }  
    });  
    
    public static String getAIResponse(String user_msg) throws Exception {
    	String INFO = user_msg;
    	String APIKEY = "6073df611dd741c0a460849a836ebd1e";
        String getURL = "http://www.tuling123.com/openapi/api?key=" + APIKEY + "&info=" + INFO;
        URL getUrl = new URL(getURL);
        HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
        connection.connect();

        // 取得输入流，并使用Reader读取
        BufferedReader reader = new BufferedReader(new InputStreamReader( connection.getInputStream(), "utf-8"));
        StringBuffer sb = new StringBuffer();
        String line = "";
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        // 断开连接
        connection.disconnect();
        
        String toReply = sb.toString().split("\"")[5];
		return toReply;
    }
}