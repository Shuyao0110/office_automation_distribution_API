package com.HelpTools;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class ReadDatabaseConfigurationFile {

	//首先程序获取程序的执行路径，也就是你打包后的jar存放路径，然后找到该路径下的config.properties文件读取，就可以了
	public static final String rootPath = System.getProperty("user.dir").replace("\\", "/");
	//配置文件的文件名
	public static final String fileName = "MDM_ESB_OA_config.properties";
	
	public static Properties ReadDatabaseConfiguration(){

		Properties p=new Properties();
        //获取文件里边的值
        FileReader f;
		try {
			File dir = new File(rootPath);
			String tempdirPath = "";
			//返回该用户目录的上三级目录
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
