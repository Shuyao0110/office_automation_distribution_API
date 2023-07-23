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
import com.sanhua.search;
import com.ufida.eip.core.MessageContext;
import com.ufida.eip.exception.EIPException;
import com.ufida.eip.java.IContextProcessor;

import commonj.sdo.DataObject;

public class deptAPI implements IContextProcessor {
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

	public static String PostUrl (String url, JSONObject obj)
	{
		String returnarg = "";
		try{
			URL    uri = new URL(url);
	        HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
	        connection.setRequestMethod("POST");
	        connection.setReadTimeout(50000);
	        connection.setConnectTimeout(100000);
	        connection.setDoInput(true); 
	   		connection.setDoOutput(true); 
	        //设置请求头  		
	        connection.setRequestProperty("Content-Type", "application/json");
	        //发送参数         
	        connection.connect(); 
	   		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8"); 

	   		//body参数在这里put到JSONObject中
	   		writer.write(obj.toString()); 
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
	   		 

	   		System.out.println(sbf.toString());
			 
	   		returnarg=sbf.toString();	   		
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
   		return returnarg;
	}
	@Override
	public String handleMessageContext(MessageContext arg0) throws EIPException {
		// TODO Auto-generated method stub
		
		
		JSONObject deptMdmData = new JSONObject(arg0.getBodyData().toString()); 
		System.out.println(deptMdmData.toString());
		
		try 
		{
			String yes = "001";
			String no = "002";
			String deptMasterData= (String) deptMdmData.get("masterData");			
			JSONArray deptJsonArray = new JSONArray(deptMasterData);
			//本批数据成败标识
			Boolean success = true;
			//
			JSONArray mdMappings = new JSONArray();
			//
			Properties apiConfig =
					ReadDatabaseConfigurationFile.ReadDatabaseConfiguration();
			for(int count = 0;count < deptJsonArray.length();count++)
			{
				JSONObject deptJsonObject = deptJsonArray.getJSONObject(count);
				String sg_gs = deptJsonObject.getString("sg_gs");
				if(sg_gs.equals(yes))//如果组织是公司
				{
					
					//构造公司（单位）数据
					String subcompanycode = deptJsonObject.getString("code");//单位编号
					String subcompanyname = deptJsonObject.getString("name");//单位名称
					String updatedatetime = deptJsonObject.getString("ts");//更新时间
					//上级单位编号
					JSONObject pk_code_entity = deptJsonObject.getJSONObject("pk_code_entity");
					String supsubcomcode = pk_code_entity.getString("code");
					if(supsubcomcode.equals("1"))//判断上级单位是否为三花控股集团
					{
						supsubcomcode = "0";//若是，则将上级单位代码改为0
					}
					//单位状态
					int canceled = 0;//该单位为正常状态
					int dr=deptJsonObject.getInt("dr");
					if(dr==1||deptJsonObject.getString("org_status_@code").toString().equals(no))
					{
						canceled = 1;//该单位状态为封存
					}
					
					JSONObject company = new JSONObject();
					company.put("subcompanycode", subcompanycode);
					company.put("subcompanyname", subcompanyname);
					company.put("supsubcomcode", supsubcomcode);
					company.put("canceled", canceled);
					company.put("updatedatetime", updatedatetime);
					
					//将company数据填到OA接口的参数中
					String url = apiConfig.getProperty("OACompanyUrl");
					String returnarg = PostUrl(url,company);
					
					JSONObject returnObj = new JSONObject(returnarg);
					JSONObject mdMapping = new JSONObject();
					Boolean singleSuccess = returnObj.getString("status").toString().equals("1")? true : false;
					if(!singleSuccess)
					{
						success = false;
					}
					mdMapping.put("mdmCode", deptJsonObject.getString("mdm_code"));
					mdMapping.put("entityCode", "g_org_test");
					mdMapping.put("busiDataId", subcompanycode);
					mdMapping.put("message", returnObj.getString("msg").toString());
					mdMapping.put("success", singleSuccess);
					mdMappings.put(mdMapping);
					
				}else if(sg_gs.equals(no))//如果组织不是公司
				{
					//调用OA单位查询接口，判断该组织之前是否为公司
					
					//若组织之前是公司，调用OA公司接口改为禁用
					
					//构造部门数据
					String departmentcode = deptJsonObject.getString("code");//部门编号
					String departmentname = deptJsonObject.getString("name");//部门名称
					String supdepcode = "";//管理组织部门编号
					String subcompanycode = "";//管理组织单位编号
					String departmentheadcode = deptJsonObject.getString("principal_code");//部门负责人工号
					String departmentleadercode = deptJsonObject.getString("chargeleader_code");//分管领导工号
					int canceled = 0;//该单位为正常状态
					String updatedatetime = deptJsonObject.getString("ts");//更新时间
					int dr=deptJsonObject.getInt("dr");
					if(dr==1||deptJsonObject.getString("org_status_@code").toString().equals(no))
					{
						canceled = 1;//该单位状态为封存
					}
					
					JSONObject pk_code_entity = deptJsonObject.getJSONObject("pk_code_entity");
					String pk_sg_gs = pk_code_entity.getString("sg_gs");
					
					if(pk_sg_gs.equals(no))//如果上级组织不是公司
					{
						supdepcode = pk_code_entity.getString("code");//主数据中上级组织编号即为OA中上级部门编号
						//根据上级部门主数据编码查所属单位
						String subdeptMdmCode = pk_code_entity.getString("mdm_code");
						subcompanycode = SearchCompany(subdeptMdmCode,"MDMDeptToken", "DeptModel", "SystemCode-dept");
						
					}else if(pk_sg_gs.equals(yes)){//如果上级组织是公司
						supdepcode = "0";
						subcompanycode = pk_code_entity.getString("code");
					}
					JSONObject dept = new JSONObject();
					dept.put("departmentcode", departmentcode);
					dept.put("departmentname", departmentname);
					dept.put("supdepcode", supdepcode);
					dept.put("subcompanycode", subcompanycode);
					dept.put("departmentheadcode", departmentheadcode);
					dept.put("departmentleadercode", departmentleadercode);
					dept.put("canceled", canceled);
					dept.put("updatedatetime", updatedatetime);
					String url = apiConfig.getProperty("OADeptUrl");
					String returnarg = PostUrl(url,dept);
					
					//封装返回主数据系统数据
					JSONObject returnObj = new JSONObject(returnarg);
					JSONObject mdMapping = new JSONObject();
					Boolean singleSuccess = returnObj.getString("status").toString().equals("1")? true : false;
					if(!singleSuccess)
					{
						success = false;
					}
					mdMapping.put("mdmCode", deptJsonObject.getString("mdm_code"));
					mdMapping.put("entityCode", "g_org_test");
					mdMapping.put("busiDataId", departmentcode);
					mdMapping.put("message", returnObj.getString("msg").toString());
					mdMapping.put("success", singleSuccess);
					mdMappings.put(mdMapping);
					
				}
				
			}
			
			JSONObject returnData = new JSONObject();
			returnData.put("success", success);
			returnData.put("mdMappings", mdMappings);
			DataObject body = arg0.getBody();
			   
			body.set("deptData",returnData.toString());
			
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
