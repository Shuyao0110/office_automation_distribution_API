package com.HelpTools;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class ReadDatabaseConfigurationFile {

	//���ȳ����ȡ�����ִ��·����Ҳ�����������jar���·����Ȼ���ҵ���·���µ�config.properties�ļ���ȡ���Ϳ�����
	public static final String rootPath = System.getProperty("user.dir").replace("\\", "/");
	//�����ļ����ļ���
	public static final String fileName = "MDM_ESB_OA_config.properties";
	
	public static Properties ReadDatabaseConfiguration(){

		Properties p=new Properties();
        //��ȡ�ļ���ߵ�ֵ
        FileReader f;
		try {
			File dir = new File(rootPath);
			String tempdirPath = "";
			//���ظ��û�Ŀ¼��������Ŀ¼
			for(int i =0;i<3;i++){
				tempdirPath = dir.getParent().toString();
				dir = new File(tempdirPath);
			}

			String inPath =  tempdirPath+"/config/"+fileName;
			f = new FileReader(inPath);
	        p.load(f);
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return p;
       
	}
}
