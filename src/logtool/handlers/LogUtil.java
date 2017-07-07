package logtool.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

public class LogUtil {
	
	/*
	 * Utility method that replaces the default log file name suffixing it with the server name.
	 * 
	 */
	
	public String createCustomLogFileName(String logFileName, String serverType, String server, String cluster){
		String customLogName = "";
		Properties envprop = new Properties();
		InputStream inputStream = null;
		inputStream = getClass().getResourceAsStream( "/"+serverType+".properties" );
		try {
			envprop.load(inputStream);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Enumeration<Object> keys = envprop.keys();
		String envType = "";
		while(keys.hasMoreElements()){
			envType = keys.nextElement().toString();
			String servers = envprop.getProperty(envType);
			if(servers.contains(server)){
				System.out.println("Server is present");
				break;
			}
		}
		
		if(logFileName.endsWith(".log")){
			String logFirstName = logFileName.substring(0, logFileName.length()-4);
			customLogName = logFirstName+"_"+envType+"_"+cluster+"_"+server+".log";
			System.out.println("Custom Log File Name is ::"+customLogName);
		}
		else{
			customLogName = logFileName+"_"+envType+"_"+server;
		}
		return customLogName;
	}
}
