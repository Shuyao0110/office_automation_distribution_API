package com.sanhua;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ufida.eip.core.MessageContext;
import com.ufida.eip.exception.EIPException;
import com.ufida.eip.java.IContextProcessor;

import commonj.sdo.DataObject;

public class positionAPI implements IContextProcessor {

	@Override
	public String handleMessageContext(MessageContext arg0) throws EIPException {
		// TODO Auto-generated method stub
		JSONObject positionMdmData = new JSONObject(arg0.getBodyData().toString()); 

		//���ݷ���mdm���͵�json�е�masterData�ֶ���
		JSONArray masterData = new JSONArray(positionMdmData.getString("masterData"));
		JSONArray positionDataToOA = new JSONArray();
		JSONObject mdmCodes =new JSONObject();
		String entityCode = positionMdmData.getString("mdType");
		
		//����jsonArrayȡ��mdm���͵�����
		for(int i = 0; i < masterData.length(); i++){
			JSONObject oneData = masterData.getJSONObject(i);
			JSONObject temp = new JSONObject();
			//�������͸�OA������
			temp.put("departmentcode", oneData.getString("pk_org_name"));
			temp.put("jobtitlecode", oneData.getString("code"));
			temp.put("jobtitlename", oneData.getString("name"));
			mdmCodes.put(Integer.toString(i), oneData.getString("mdm_code"));
			positionDataToOA.put(temp);
			
		}
		
		try 
		{
			//OA�ṩ��url��token
			String url = "http://172.16.99.52:20600/papi/openapi/api/open-esb/server/webhook/trigger/MTIwYWU2OWU0MWY2NDU1NmJjMjMxZGE4YWM1OTY5Y2Y=?access_token=f238d1f2274a4281a0df39cb7577d27e";
			String access_token = "f238d1f2274a4281a0df39cb7577d27e";
			URL uri = new URL(url);

            //����������ƽ̨����������
            JSONArray dataReturnToMdm = new JSONArray();
            boolean success = true; 
            int numberOfFail = 0;
            
            //�������Ͳ�������ȡ���ؽ��
            //mdmҪ��һ����һ��¼��OA�ӿڴ������ݷ���һ��¼ 23.7.7
            for(int i = 0; i < positionDataToOA.length(); i++){
                HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
                connection.setRequestMethod("POST");
                connection.setReadTimeout(50000);
                connection.setConnectTimeout(100000);
                connection.setDoInput(true); 
        		connection.setDoOutput(true); 
        		//��������ͷ
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("charset", "UTF-8");
                
                connection.connect();
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),"UTF-8"); 
                writer.write(positionDataToOA.getJSONObject(i).toString()); 
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
        		//����������ƽ̨���������е�һ����¼
        		JSONObject oneResult = new JSONObject();
        		oneResult.put("mdmCode", mdmCodes.getString(Integer.toString(i)));
        		oneResult.put("entityCode", entityCode);
        		if(!OACode.equals("200")){
        			tempSuccess = false;
        			tempMessage = String.format("OA�ӿ�HTTP���Ӵ��󣬴������Ϊ%s", OACode);
        			success = false;
        			numberOfFail++;
        		}
        		else{
        			if(OAStatus.equals("2")){
        				tempSuccess = false;
        				tempMessage = "OA�ӿڽӿڵ��óɹ���ҵ����ʧ��,�ӿڷ�����Ϣ��"+OAMsg;
        				success = false;
        				numberOfFail++;
        			}
        			else{
        				tempMessage = "OA�ӿڽӿڵ��óɹ���ҵ�����ɹ�,�ӿڷ�����Ϣ��"+OAMsg;;
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
            	returnData.put("message", "����ϵͳ�����������ݳɹ���");
            	returnData.put("mdMappings", dataReturnToMdm);
            	System.out.println(dataReturnToMdm.toString());
            }
            else{
            	returnData.put("success", success);
            	returnData.put("message", String.format("����ϵͳ������������ʧ�ܣ���%i�����ݷַ�ʧ��", numberOfFail));
            	returnData.put("mdMappings", dataReturnToMdm);
            }
    		

			DataObject body = arg0.getBody();
			
			body.set("positionData",returnData.toString());
			
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
