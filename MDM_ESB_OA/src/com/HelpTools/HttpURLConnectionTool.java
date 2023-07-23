package com.HelpTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONObject;

public class HttpURLConnectionTool {


	public static String startGet(String path,HashMap<String, String> mData){
        BufferedReader in = null;        
        StringBuilder result = new StringBuilder(); 
        try {
            //GET����ֱ�������Ӻ���ƴ���������
            String mPath = path + "?";
            for(String key:mData.keySet()){
                mPath += key + "=" + mData.get(key) + "&";
            }
            URL url = new URL(mPath);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            //Get������ҪDoOutPut
            conn.setDoOutput(false);
            conn.setDoInput(true);
            //�������ӳ�ʱʱ��Ͷ�ȡ��ʱʱ��
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //���ӷ�����  
            conn.connect();  
            // ȡ������������ʹ��Reader��ȡ  
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //�ر�������
        finally{
            try{
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result.toString();
    }

	 
	
	public static String startPost(String path,JSONObject jsonData){
        OutputStreamWriter out = null;
        BufferedReader in = null;        
        StringBuilder result = new StringBuilder(); 
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            // ����POST�����������Ϊtrue
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //�������ӳ�ʱʱ��Ͷ�ȡ��ʱʱ��
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");


            out = new OutputStreamWriter(conn.getOutputStream());  
            // POST���������д��������
            
//            for(String key:mData.keySet()){
//                out.write(key + "=" + mData.get(key) + "&");  
//            }
            out.write(jsonData.toString());
            out.flush();  
            out.close();
            // ȡ������������ʹ��Reader��ȡ  
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //�ر��������������
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result.toString();
    }
	
	
}
