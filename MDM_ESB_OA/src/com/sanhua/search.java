package com.sanhua;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import com.HelpTools.ReadDatabaseConfigurationFile;

public class search {
	//
	public static String SearchCompany(String orgMdmCode, String tokenName, String modelName, String systemCodeName) throws Exception
	{
		Properties OAconfig =
				ReadDatabaseConfigurationFile.ReadDatabaseConfiguration();
		String url =
		OAconfig.getProperty("MDMUrl");
        URL    uri = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
        connection.setRequestMethod("POST");
        connection.setReadTimeout(50000);
        connection.setConnectTimeout(100000);
        connection.setDoInput(true); 
		connection.setDoOutput(true); 
        //设置请求头
		String mdmToken = OAconfig.getProperty(tokenName);
        connection.setRequestProperty("mdmtoken", mdmToken);
        connection.setRequestProperty("tenantid", "tenant");
        connection.setRequestProperty("Content-Type", "application/json");
        //发送参数
      
        connection.connect(); 
		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8"); 

		//body参数在这里put到JSONObject中
		JSONObject parm = new JSONObject();
		String systemCode = OAconfig.getProperty(systemCodeName);
		String gdCode = OAconfig.getProperty(modelName);
		parm.put("systemCode", systemCode);
		parm.put("gdCode", gdCode);
		Collection<String> collection = new ArrayList<String>();
		
		collection.add(orgMdmCode);
		   		 
		parm.put("codes",collection);
		System.out.println(parm.toString());
		
		
		writer.write(parm.toString()); 
		writer.flush();
		InputStream is = connection.getInputStream(); 
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); 
		String strRead;
		StringBuffer sbf = new StringBuffer(); 
		while ((strRead = reader.readLine()) != null) { 
			sbf.append(strRead); 
			sbf.append("\r\n"); 
		}
		reader.close(); 
		connection.disconnect();
		String results = sbf.toString(); 
		
		JSONObject resultObject = new JSONObject(results);
		JSONArray orgJsonArray = new JSONArray(resultObject.get("data").toString());
		JSONObject orgObject = orgJsonArray.getJSONObject(0);
		String sg_gs = orgObject.getString("sg_gs");
		//判断该组织是否为公司
		if(sg_gs.equals("002"))
		{
			String pkOrgMdmCode = orgObject.getString("pk_code");
			return SearchCompany(pkOrgMdmCode,tokenName,modelName, systemCodeName);
		}else{
			return orgObject.getString("code");
		}				
		
	}
}
