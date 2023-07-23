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

import org.json.JSONObject;
import org.json.JSONArray;

import com.HelpTools.ReadDatabaseConfigurationFile;
import com.sanhua.search;
import com.ufida.eip.core.MessageContext;
import com.ufida.eip.exception.EIPException;
import com.ufida.eip.java.IContextProcessor;

import commonj.sdo.DataObject;

public class personAPI implements IContextProcessor {
	
//	首次根据mdmCode查询
	public static JSONObject selectOrganizationOfPerson(String systemCode, String mdType, ArrayList<String> codes) throws Exception{
		Properties OAconfig =
				ReadDatabaseConfigurationFile.ReadDatabaseConfiguration();
		String url = OAconfig.getProperty("MDMUrl");
    	String mdmToken = OAconfig.getProperty("MDMPersonToken");
    	String tenantId = "tenant";
    	     
    	JSONObject queryBody = new JSONObject();
    	queryBody.put("systemCode", systemCode);
    	queryBody.put("gdCode", mdType);
    	queryBody.put("codes", codes);
    	queryBody.put("pagable", false);
    	queryBody.put("pageIndex", 1);
    	queryBody.put("pageSize", 30000);
    	
    	//System.out.println(queryBody.toString());
		URL uri = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
        connection.setRequestMethod("POST");
        connection.setReadTimeout(50000);
        connection.setConnectTimeout(100000);
        connection.setDoInput(true); 
		connection.setDoOutput(true); 
		connection.setRequestProperty("mdmtoken", mdmToken);
		connection.setRequestProperty("tenantid", tenantId);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.connect(); 
//      发送
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8"); 
        writer.write(queryBody.toString()); 
		writer.flush();
//		接收
		InputStream is = connection.getInputStream(); 
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); 
		String strRead;
		StringBuffer sbf = new StringBuffer(); 
		while ((strRead = reader.readLine()) != null) {
			System.out.println(strRead);
			sbf.append(strRead); 
			sbf.append("\r\n"); 
		}
		reader.close();
		connection.disconnect();
		JSONObject results = new JSONObject(sbf.toString());
		JSONArray resultData = results.getJSONArray("data");
		System.out.println(resultData.toString());
		JSONObject result = new JSONObject();
		for(int i = 0; i < resultData.length(); i++){
			JSONObject resultData0 = resultData.getJSONObject(i);
			JSONObject oneResult = new JSONObject();
			String oneMdmCode = resultData0.getString("mdm_code");
			oneResult.put("sg_gs", resultData0.getString("sg_gs"));
			oneResult.put("code", resultData0.getString("code"));
			oneResult.put("pk_code_name", resultData0.getString("pk_code_name"));
			result.put(oneMdmCode, oneResult);
		}

    	return result;
    }
	
//	根据组织编号查询
	public static JSONObject companyFinder(String systemCode, String mdType, JSONObject jo) throws Exception{
    	if (jo.getString("sg_gs").equals("001")){
			return jo;
    	}
    	Properties OAconfig =
				ReadDatabaseConfigurationFile.ReadDatabaseConfiguration();
		String url = OAconfig.getProperty("MDMConditionUrl");
    	String mdmToken = OAconfig.getProperty("MDMPersonToken");
    	String tenantId = "tenant";
    	JSONObject condition = new JSONObject();
    	ArrayList<String> codeCondition = new ArrayList<String>();
    	codeCondition.add("and");
    	codeCondition.add("=");
    	codeCondition.add("'"+jo.getString("pk_code_name")+"'");
    	condition.put("code",codeCondition);

    	JSONObject queryBody = new JSONObject();
    	queryBody.put("systemCode", systemCode);
    	queryBody.put("gdCode", mdType);
    	queryBody.put("conditions", condition);
    	queryBody.put("pagable", false);
    	queryBody.put("pageIndex", 1);
    	queryBody.put("pageSize", 30000);

		URL uri = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
        connection.setRequestMethod("POST");
        connection.setReadTimeout(50000);
        connection.setConnectTimeout(100000);
        connection.setDoInput(true); 
		connection.setDoOutput(true); 
		connection.setRequestProperty("mdmtoken", mdmToken);
		connection.setRequestProperty("tenantid", tenantId);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.connect(); 

        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8"); 
        writer.write(queryBody.toString()); 
		writer.flush();
		
		InputStream is = connection.getInputStream(); 
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); 
		String strRead;
		StringBuffer sbf = new StringBuffer(); 
		while ((strRead = reader.readLine()) != null) {
			System.out.println(strRead);
			sbf.append(strRead); 
			sbf.append("\r\n"); 
		}
		reader.close();
		connection.disconnect();
		
		JSONObject results = new JSONObject(sbf.toString());
		JSONArray resultDataArray = results.getJSONArray("data");
		JSONObject resultData = resultDataArray.getJSONObject(0);
		JSONObject result = new JSONObject();
		
		result.put("sg_gs", resultData.getString("sg_gs"));
		result.put("code", resultData.getString("code"));
		result.put("pk_code_name", resultData.getString("pk_code_name"));
    	System.out.println(result.toString());
		return companyFinder(systemCode, mdType, result);
    }
	
	
//	在岗位表里查询岗位编号，得到上级岗位编号
	public static String managerCodeFinder(ArrayList<String> mdmCodes) throws Exception{
		Properties OAconfig =
				ReadDatabaseConfigurationFile.ReadDatabaseConfiguration();
		String url = OAconfig.getProperty("MDMUrl");
    	String mdmToken = OAconfig.getProperty("MDMPersonToken");
    	String systenCode = OAconfig.getProperty("SystemCode-person");
    	String gdCode = OAconfig.getProperty("PositionModel");
    	String tenantId = "tenant";
    	
//    	JSONObject condition = new JSONObject();
//    	ArrayList<String> codeCondition = new ArrayList<String>();
//    	codeCondition.add("and");
//    	codeCondition.add("=");
//    	codeCondition.add(pk_post_name);
//    	condition.put("code",codeCondition);

    	JSONObject queryBody = new JSONObject();
    	queryBody.put("systemCode", systenCode);
    	queryBody.put("gdCode",gdCode);
    	queryBody.put("codes", mdmCodes);
    	queryBody.put("pagable", false);
    	queryBody.put("pageIndex", 1);
    	queryBody.put("pageSize", 30000);

		URL uri = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
        connection.setRequestMethod("POST");
        connection.setReadTimeout(50000);
        connection.setConnectTimeout(100000);
        connection.setDoInput(true); 
		connection.setDoOutput(true); 
		connection.setRequestProperty("mdmtoken", mdmToken);
		connection.setRequestProperty("tenantid", tenantId);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.connect(); 

        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8"); 
        writer.write(queryBody.toString()); 
		writer.flush();
		
		InputStream is = connection.getInputStream(); 
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); 
		String strRead;
		StringBuffer sbf = new StringBuffer(); 
		while ((strRead = reader.readLine()) != null) {
			System.out.println(strRead);
			sbf.append(strRead); 
			sbf.append("\r\n"); 
		}
		reader.close();
		connection.disconnect();
		
		JSONObject results = new JSONObject(sbf.toString());
		JSONArray resultDataArray = results.getJSONArray("data");
		JSONObject resultData = resultDataArray.getJSONObject(0);
		String managerCode = resultData.getString("code");
		return managerCode;
    }
	
//	在人员表里用岗位编号查id
	public static String managerIdFinder(String pk_post_name) throws Exception{
		Properties OAconfig =
				ReadDatabaseConfigurationFile.ReadDatabaseConfiguration();
		String url = OAconfig.getProperty("MDMConditionUrl");
    	String mdmToken = OAconfig.getProperty("MDMPersonToken");
    	String systenCode = OAconfig.getProperty("SystemCode-person");
    	String gdCode = OAconfig.getProperty("PersonModel");
    	String tenantId = "tenant";
    	String returnMsg;
    	
    	JSONObject condition = new JSONObject();
    	ArrayList<String> codeCondition = new ArrayList<String>();
    	codeCondition.add("and");
    	codeCondition.add("=");
    	codeCondition.add("'"+pk_post_name+"'");
    	condition.put("pk_post#code",codeCondition);

    	JSONObject queryBody = new JSONObject();
    	queryBody.put("systemCode", systenCode);
    	queryBody.put("gdCode",gdCode);
    	queryBody.put("conditions", condition);
    	queryBody.put("pagable", false);
    	queryBody.put("pageIndex", 1);
    	queryBody.put("pageSize", 30000);
    	System.out.println(queryBody.toString());
    	
		URL uri = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
        connection.setRequestMethod("POST");
        connection.setReadTimeout(50000);
        connection.setConnectTimeout(100000);
        connection.setDoInput(true); 
		connection.setDoOutput(true); 
		connection.setRequestProperty("mdmtoken", mdmToken);
		connection.setRequestProperty("tenantid", tenantId);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.connect(); 

        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8"); 
        writer.write(queryBody.toString()); 
		writer.flush();
		
		InputStream is = connection.getInputStream(); 
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); 
		String strRead;
		StringBuffer sbf = new StringBuffer(); 
		while ((strRead = reader.readLine()) != null) {
			System.out.println(strRead);
			sbf.append(strRead); 
			sbf.append("\r\n"); 
		}
		reader.close();
		connection.disconnect();
		
		JSONObject results = new JSONObject(sbf.toString());
		JSONArray resultDataArray = results.getJSONArray("data");
		if(resultDataArray.length() != 1)
		{
			returnMsg = "Error:"+ pk_post_name;
		}else{
			JSONObject resultData = resultDataArray.getJSONObject(0);
			String managerId = resultData.getString("code");
			returnMsg = managerId;
		}
		
		return returnMsg;
	}
	
	@Override
	public String handleMessageContext(MessageContext arg0) throws EIPException {
		// TODO Auto-generated method stub
//		员工和当前组织编码是多对一关系，发出的codes会在主数据系统被去重，返回值没有重复
//		同一个组织（mdmCode）对应的最小公司是同一个code，所以建立一张存储着组织编码和最小公司的哈希表
//		按员工的当前组织查询最小公司编号
		JSONObject personMdmData = new JSONObject(arg0.getBodyData().toString());  
		JSONArray masterData = new JSONArray(personMdmData.getString("masterData"));
		String systemCode = personMdmData.getString("systemCode");
		String mdType = personMdmData.getString("mdType");
//		按编号查询管理组织
		ArrayList<String> mdm_codes = new ArrayList<String>();
		
		for(int i = 0; i < masterData.length(); i++){
			mdm_codes.add(masterData.getJSONObject(i).getJSONObject("pk_post_entity").getString("pk_org"));
		}
		System.out.println(mdm_codes.toString());		
		try
		{
			JSONObject depDataFromMdm = selectOrganizationOfPerson(systemCode, "g_org_test", mdm_codes);
			System.out.println(depDataFromMdm.toString());
			for (String key : depDataFromMdm.keySet()) {
				JSONObject depData = depDataFromMdm.getJSONObject(key);
				JSONObject subcompany = companyFinder(systemCode, "g_org_test", depData);
				String subcompanycode = subcompany.getString("code");
				
			}
			//返回主数据平台的数据详情
            JSONArray dataReturnToMdm = new JSONArray();
            boolean success = true; 
            int numberOfFail = 0;
            
			// 给OA系统发送请求
			// 设置请求头
            Properties OAconfig =
    				ReadDatabaseConfigurationFile.ReadDatabaseConfiguration();
    		String url = OAconfig.getProperty("OAPersonUrl");
			//String url = "http://172.16.99.52:20600/papi/openapi/api/open-esb/server/webhook/trigger/ZjVkZGU2N2YwOGRhNDRkNWIyNTNmNzUwNGMyZWU2NDc=?access_token=f238d1f2274a4281a0df39cb7577d27e";
			//String access_token = "f238d1f2274a4281a0df39cb7577d27e";
			URL uri = new URL(url);
			
            //逐条发送参数并读取返回结果
            //mdm要求一数据一记录，OA接口传多数据返回一记录 23.7.7
			
			JSONObject returnDataToMdm = new JSONObject();
            for(int i = 0; i < masterData.length(); i++){
            	JSONArray personDataToOA = new JSONArray();
            	JSONObject oneData = masterData.getJSONObject(i);
    			String pk_org = oneData.getJSONObject("pk_post_entity").getString("pk_org");
    			ArrayList<String> mdmCodes = new ArrayList<String>();
    			mdmCodes.add(oneData.getJSONObject("pk_post_entity").getString("pk_post"));
    			String managerCode = managerCodeFinder(mdmCodes);
    			System.out.println(managerCode);
    			String managerId = managerIdFinder(managerCode);
    			
    			JSONObject temp = new JSONObject();
    			System.out.println(depDataFromMdm.getJSONObject(pk_org).toString());
    			temp.put("subcompanycode", depDataFromMdm.getJSONObject(pk_org).getString("pk_code_name"));
    			temp.put("departmentcode", depDataFromMdm.getJSONObject(pk_org).getString("code"));
    			//temp.put("legalsubcompanycode", "123456");
    			//temp.put("legaldepartmentcode", "123456");
    			temp.put("workcode", oneData.getString("code"));
    			temp.put("workcode2", oneData.getString("last_code"));
    			temp.put("lastname", oneData.getString("name"));
    			temp.put("jobtitle", oneData.getString("pk_post_name"));
    			temp.put("managerid", managerId);
    			temp.put("mobile", oneData.getString("t_phone"));
    			temp.put("email", oneData.getString("mail_addr"));
    			temp.put("sex", Integer.toString(Integer.parseInt(oneData.getString("sex"))-1));
    			System.out.println(Integer.parseInt(oneData.getString("peop_status")));
    			temp.put("status", Integer.parseInt(oneData.getString("peop_status")));
    			temp.put("updatedatetime",oneData.getString("ts"));
    			personDataToOA.put(temp);
            	
                HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(50000);
                connection.setConnectTimeout(100000);
                connection.setDoInput(true); 
        		connection.setDoOutput(true); 
        		//设置请求头
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("charset", "UTF-8");
                
                connection.connect();
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8"); 
                writer.write(personDataToOA.toString()); 
        		writer.flush();
        		
        		InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); 
        		String strRead;
        		StringBuffer sbf = new StringBuffer(); 
    			while ((strRead = reader.readLine()) != null) {
    				System.out.println(strRead);
        			sbf.append(strRead); 
        			sbf.append("\r\n"); 
        		}
        		reader.close();
        		JSONObject results = new JSONObject(sbf.toString());

        		String OACode = results.getString("code");
        		String OAStatus = results.getString("status");
        		String OAMsg = results.getString("msg");
        		boolean tempSuccess = true;
        		String tempMessage = "";
        		
        		//返回主数据平台数据详情中的一条记录
        		JSONObject oneResult = new JSONObject();
        		oneResult.put("mdmCode",oneData.getString("mdm_code"));
        		oneResult.put("entityCode", mdType);
        		if(!OACode.equals("200")){
        			tempSuccess = false;
        			tempMessage = String.format("OA接口HTTP连接错误，错误代码为%s", OACode);
        			success = false;
        			numberOfFail++;
        		}
        		else{
        			if(OAStatus.equals("2")){
        				tempSuccess = false;
        				tempMessage = "OA接口接口调用成功，业务发生失败,接口返回消息："+OAMsg;
        				success = false;
        				numberOfFail++;
        			}
        			else{
        				tempMessage = "OA接口接口调用成功，业务发生成功,接口返回消息："+OAMsg;;
        			}
        		}
        		oneResult.put("success", tempSuccess);
        		oneResult.put("message", tempMessage);
        		
        		dataReturnToMdm.put(oneResult);
        		
        		connection.disconnect();
            }
            
            
            JSONObject returnData = new JSONObject();
            if(success){
            	returnData.put("success", success);
            	returnData.put("message", "消费系统消费这批数据成功！");
            	returnData.put("mdMappings", dataReturnToMdm);
            	System.out.println(dataReturnToMdm.toString());
            }
            else{
            	returnData.put("success", success);
            	returnData.put("message", String.format("消费系统消费这批数据失败！共%i条数据分发失败", numberOfFail));
            	returnData.put("mdMappings", dataReturnToMdm);
            }
    		

			DataObject body = arg0.getBody();
			
			body.set("personData",returnData.toString());
            
     
    		// 封装返回给主数据系统的returnData
			

			
		}catch (Exception e) {
			// TODO AutoW-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
