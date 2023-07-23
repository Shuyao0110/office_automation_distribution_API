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
        //��������ͷ
		String mdmToken = OAconfig.getProperty(tokenName);
        connection.setRequestProperty("mdmtoken", mdmToken);
        connection.setRequestProperty("tenantid", "tenant");
        connection.setRequestProperty("Content-Type", "application/json");
        //���Ͳ���
      
        connection.connect(); 
		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8"); 

		//body����������put��JSONObject��
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
		//�жϸ���֯�Ƿ�Ϊ��˾
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
	        //��������ͷ  		
	        connection.setRequestProperty("Content-Type", "application/json");
	        //���Ͳ���         
	        connection.connect(); 
	   		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8"); 

	   		//body����������put��JSONObject��
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
			//�������ݳɰܱ�ʶ
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
				if(sg_gs.equals(yes))//�����֯�ǹ�˾
				{
					
					//���칫˾����λ������
					String subcompanycode = deptJsonObject.getString("code");//��λ���
					String subcompanyname = deptJsonObject.getString("name");//��λ����
					String updatedatetime = deptJsonObject.getString("ts");//����ʱ��
					//�ϼ���λ���
					JSONObject pk_code_entity = deptJsonObject.getJSONObject("pk_code_entity");
					String supsubcomcode = pk_code_entity.getString("code");
					if(supsubcomcode.equals("1"))//�ж��ϼ���λ�Ƿ�Ϊ�����عɼ���
					{
						supsubcomcode = "0";//���ǣ����ϼ���λ�����Ϊ0
					}
					//��λ״̬
					int canceled = 0;//�õ�λΪ����״̬
					int dr=deptJsonObject.getInt("dr");
					if(dr==1||deptJsonObject.getString("org_status_@code").toString().equals(no))
					{
						canceled = 1;//�õ�λ״̬Ϊ���
					}
					
					JSONObject company = new JSONObject();
					company.put("subcompanycode", subcompanycode);
					company.put("subcompanyname", subcompanyname);
					company.put("supsubcomcode", supsubcomcode);
					company.put("canceled", canceled);
					company.put("updatedatetime", updatedatetime);
					
					//��company�����OA�ӿڵĲ�����
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
					
				}else if(sg_gs.equals(no))//�����֯���ǹ�˾
				{
					//����OA��λ��ѯ�ӿڣ��жϸ���֮֯ǰ�Ƿ�Ϊ��˾
					
					//����֮֯ǰ�ǹ�˾������OA��˾�ӿڸ�Ϊ����
					
					//���첿������
					String departmentcode = deptJsonObject.getString("code");//���ű��
					String departmentname = deptJsonObject.getString("name");//��������
					String supdepcode = "";//������֯���ű��
					String subcompanycode = "";//������֯��λ���
					String departmentheadcode = deptJsonObject.getString("principal_code");//���Ÿ����˹���
					String departmentleadercode = deptJsonObject.getString("chargeleader_code");//�ֹ��쵼����
					int canceled = 0;//�õ�λΪ����״̬
					String updatedatetime = deptJsonObject.getString("ts");//����ʱ��
					int dr=deptJsonObject.getInt("dr");
					if(dr==1||deptJsonObject.getString("org_status_@code").toString().equals(no))
					{
						canceled = 1;//�õ�λ״̬Ϊ���
					}
					
					JSONObject pk_code_entity = deptJsonObject.getJSONObject("pk_code_entity");
					String pk_sg_gs = pk_code_entity.getString("sg_gs");
					
					if(pk_sg_gs.equals(no))//����ϼ���֯���ǹ�˾
					{
						supdepcode = pk_code_entity.getString("code");//���������ϼ���֯��ż�ΪOA���ϼ����ű��
						//�����ϼ����������ݱ����������λ
						String subdeptMdmCode = pk_code_entity.getString("mdm_code");
						subcompanycode = SearchCompany(subdeptMdmCode,"MDMDeptToken", "DeptModel", "SystemCode-dept");
						
					}else if(pk_sg_gs.equals(yes)){//����ϼ���֯�ǹ�˾
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
					
					//��װ����������ϵͳ����
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
