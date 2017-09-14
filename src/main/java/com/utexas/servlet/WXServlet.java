package com.utexas.servlet;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.utexas.utils.CheckUtil;
import com.utexas.utils.MessageUtil;
;

/**
 * Servlet implementation class WXServlet
 */
@WebServlet("/wxs")
public class WXServlet extends HttpServlet {


	static Set<String> participants = new HashSet<>();// holds all participants

	private static final long serialVersionUID = 1L;

	
	/**
	 * @throws IOException
	 * @see HttpServlet#HttpServlet()
	 */
	public WXServlet() throws IOException {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// verify that the message is actually from the Tencent Server
		String signature = request.getParameter("signature");
		String timestamp = request.getParameter("timestamp");
		String nonce = request.getParameter("nonce");
		String echostr = request.getParameter("echostr");
		if (CheckUtil.checkSignature(signature, timestamp, nonce)) {
			 PrintWriter out = response.getWriter();
			 out.print(echostr);
			 out.close();
			return;
		}
	}

	
	
	
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doGet(request, response);
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		
		response.getWriter().write("Got message!");
        
        Map<String, String> map = null;
        try{
        	map = MessageUtil.parseXml(request);
        }catch(Exception e){
            System.err.println("Cannot parse xml");
        }

        //OpenID of participant
        String openID = map.get("FromUserName");
        //name of dev
        String dev = map.get("ToUserName");
        //time string
        long time = new java.util.Date().getTime()/1000;
        String createtime = Long.toString(time);
        //message to send back
        String respMessage = "";
        
        
        //if a text type message
        if(map.get("MsgType").equals("text")) {
        	
        	//invalid message
        	if(map.get("Content") == null) return;
        	
        	//Reset participant pool
        	if(map.get("Content").equals("Leon重置")) {
        		Writer fw = new OutputStreamWriter(new FileOutputStream("C:\\Users\\leond\\Google Drive\\CSSA\\抽奖程序\\a.txt"), StandardCharsets.UTF_8);
        		fw.write("");
        		participants.clear();
            	//close the file reader here
            	fw.close();
	        	respMessage = MessageUtil.generateXmlMsg(dev, openID, createtime, "已重置");
        	}
        	
        	//if the user wants to participate
        	else if(map.get("Content").matches("^.{2,10}要抽奖$")) {
                //name of participant
                String participant = map.get("Content").split("要抽奖")[0];
                
                //check if the name is already in use

                
        		//if the user participates for the first time
        		if(!participants.contains(openID)) {
                    List<String> existingNames = Files.readAllLines(Paths.get("C:\\Users\\leond\\Google Drive\\CSSA\\抽奖程序\\", "a.txt"), StandardCharsets.UTF_8);
            		if(existingNames.contains(participant)) {
    		        	respMessage = MessageUtil.generateXmlMsg(dev, openID, createtime, "这个昵称已经有人使用了，请换一个");
            		}
            		
	        		Writer fw = new OutputStreamWriter(new FileOutputStream("C:\\Users\\leond\\Google Drive\\CSSA\\抽奖程序\\a.txt", true), StandardCharsets.UTF_8);
		        	participants.add(openID);
		        	fw.write(participant + "\n");
		        	//close the file reader here
		        	fw.close();
		        	respMessage = MessageUtil.generateXmlMsg(dev, openID, createtime, "谢谢参与抽奖！");
		        }


		        //if the participant has already participated
		        else {
		        	respMessage = MessageUtil.generateXmlMsg(dev, openID, createtime, "想抽两次的不是好孩子");
		        }
        	}
        	
        	//not the right message
            else {
	            String user_msg = URLEncoder.encode(map.get("Content"), "utf-8");
	            String toReply = "";
	            
	            
				try {
					toReply = MessageUtil.getAIResponse(user_msg);
		            toReply += "\n\n\n\n\n\n\n(上面纯属玩笑，是一个临时的AI。本公众号抽奖测试中，请发送 你的名字 + “要抽奖”。\n名字 2 - 10 个字，抽奖时使用。\n\n比如 “王力宏要抽奖”（不带引号）";
				} catch (Exception e) {
					System.err.println("Error fetching AI Response");
				}
				
	        	respMessage = MessageUtil.generateXmlMsg(dev, openID, createtime, toReply);
	        	//respMessage = MessageUtil.generateXmlMsg(dev, openID, createtime, "抽奖测试中。\n\n请发送 你的名字 + “要抽奖”。\n昵称 2 - 10 个字，抽奖时使用。\n\n比如 “王力宏要抽奖”（不带引号）");
            }
        }
        
        //if not a text type message
        else {
        	respMessage = MessageUtil.generateXmlMsg(dev, openID, createtime, "欢迎来到 UTCSSA！");
        }
        
        //reply the user
    	PrintWriter out = response.getWriter();
    	out.print(respMessage);
    	out.close();
	}
	
	

}
