package logtool.handlers;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class UnixHelper {

	public UnixHelper() {
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * Method used by the Connect button on the screen. We validate the user credentials to 
	 * connect to the server. 
	 */
	
	public void connectToServer(String server, String userName, String password) throws Exception{
		Session session = null;
		
		try{
			// TODO Auto-generated method stub
			JSch jsch = new JSch();
			session = jsch.getSession(userName,server,22);
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
		}
		catch(JSchException e){
			throw new Exception("Connection Exception");
		}
		finally{
			session.disconnect();
		}
	}
	
	/*
	 * Get the log file list from the server. This list is displayed in the log dropdown in the UI
	 * 
	 */
	
	public String[] getLogFilesList(String server, String userName, String password, String cluster) throws Exception{
		Session session = null;
		Channel channel = null;
		ChannelSftp sftpChannel = null;
		try{
			JSch jsch = new JSch();
			session = jsch.getSession(userName,server,22);
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			sftpChannel = (ChannelSftp)channel;
			sftpChannel.cd("/app/"+cluster+"/logs");
			ArrayList<String> logList = new ArrayList<String>();
			Vector<LsEntry> entries = sftpChannel.ls("*.*");
			for (LsEntry entry : entries) {
			    if(entry.getFilename().toLowerCase().contains("log") && !entry.getFilename().toLowerCase().contains("bootstrap") &&
			    		!entry.getFilename().toLowerCase().startsWith("dt_") && !entry.getFilename().toLowerCase().startsWith("native_") && 
			    		!entry.getFilename().toLowerCase().endsWith(".owner")) {
			    	logList.add(entry.getFilename());
			    }
			}
			Collections.sort(logList, new Comparator<String>() {

				@Override
				public int compare(String object1, String object2) {
					return object2.compareTo(object1);
				}
				
			});
			String[] logFileArray = new String[logList.size()+1];
			logFileArray[0]="All";
			for(int i=0;i<logList.size();i++){
				logFileArray[i+1]=logList.get(i);
			}
			return logFileArray;
		}
		catch(JSchException e){
			throw new Exception("Connection Exception");
		}
		finally{
			sftpChannel.disconnect();
			session.disconnect();
		}
	}
	
	/*
	 * This method gets the list of batch components on a batch server.
	 * 
	 */
	
	
	public String[] getBatchComponentList(String server, String userName, String password) throws Exception{
		Session session = null;
		Channel channel = null;
		ChannelSftp sftpChannel = null;
		try{
			JSch jsch = new JSch();
			session = jsch.getSession(userName,server,22);
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			sftpChannel = (ChannelSftp)channel;
			ArrayList<String> batchCompList = new ArrayList<String>();
			Vector<LsEntry> entries = sftpChannel.ls("/batch/");
			for (LsEntry entry : entries) {
				if(entry.getAttrs().isDir() && !".".equals(entry.getFilename().trim()) 
						&& !"..".equals(entry.getFilename().trim())){
					batchCompList.add(entry.getFilename());
				}
			}
			Collections.sort(batchCompList);
			String[] batchCompArray = new String[batchCompList.size()];
			for(int i=0;i<batchCompList.size();i++){
				batchCompArray[i]=batchCompList.get(i);
			}
			return batchCompArray;
		}
		catch(JSchException e){
			throw new Exception("Connection Exception");
		}
		finally{
			sftpChannel.disconnect();
			session.disconnect();
		}
	}
	
	
	/*
	 * Based on the batch component selected, we fetch the list of batch jobs from the batch server
	 * 
	 */
	
	
	public String[] getBatchJobList(String server, String userName, String password, String batchComp) throws Exception{
		Session session = null;
		Channel channel = null;
		ChannelSftp sftpChannel = null;
		try{
			JSch jsch = new JSch();
			session = jsch.getSession(userName,server,22);
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			sftpChannel = (ChannelSftp)channel;
			ArrayList<String> batchJobList = new ArrayList<String>();
			Vector<LsEntry> entries = sftpChannel.ls("/batch/"+batchComp+"/logs");
			for (LsEntry entry : entries) {
				if(entry.getAttrs().isDir() && !".".equals(entry.getFilename().trim()) 
						&& !"..".equals(entry.getFilename().trim())){
					batchJobList.add(entry.getFilename());
				}
			}
			Collections.sort(batchJobList);
			String[] batchJobArray = new String[batchJobList.size()];
			for(int i=0;i<batchJobList.size();i++){
				batchJobArray[i]=batchJobList.get(i);
			}
			return batchJobArray;
		}
		catch(JSchException e){
			throw new Exception("Connection Exception");
		}
		finally{
			sftpChannel.disconnect();
			session.disconnect();
		}
	}
	
	/*
	 * This method fetches the batch log list based on the batch component and batch job
	 * 
	 */
	
	
	public String[] getBatchJobLogList(String server, String userName, String password, String batchComp, String batchJob) throws Exception{
		Session session = null;
		Channel channel = null;
		ChannelSftp sftpChannel = null;
		try{
			JSch jsch = new JSch();
			session = jsch.getSession(userName,server,22);
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			sftpChannel = (ChannelSftp)channel;
			ArrayList<String> batchJobLogList = new ArrayList<String>();
			Vector<LsEntry> entries = sftpChannel.ls("/batch/"+batchComp+"/logs/"+batchJob);
			for (LsEntry entry : entries) {
				if(entry.getFilename().toLowerCase().contains("log") && !".".equals(entry.getFilename().trim()) 
						&& !"..".equals(entry.getFilename().trim())){
					batchJobLogList.add(entry.getFilename());
				}
			}
			Collections.sort(batchJobLogList, new Comparator<String>() {

				@Override
				public int compare(String object1, String object2) {
					return object2.compareTo(object1);
				}
				
			});
			String[] batchJobLogArray = new String[batchJobLogList.size()+1];
			batchJobLogArray[0]="All";
			for(int i=0;i<batchJobLogList.size();i++){
				batchJobLogArray[i+1]=batchJobLogList.get(i);
			}
			return batchJobLogArray;
		}
		catch(JSchException e){
			throw new Exception("Connection Exception");
		}
		finally{
			sftpChannel.disconnect();
			session.disconnect();
		}
	}
	
	/*
	 * This method sftps the log file from app server to the local folder of the user. 
	 * 
	 */
	
	public void sftpLogFileToLocal(String server, String userName, String password, String cluster, String logFile, String logDownloadPath){
		// Object Declaration.
		JSch jsch = new JSch();
		Session session = null;
		Channel channel = null;
		ChannelSftp sftpChannel = null;

		try {
			session = jsch.getSession(userName, server, 22);
			/*
			 * StrictHostKeyChecking Indicates what to do if the server's host 
			 * key changed or the server is unknown. One of yes (refuse connection), 
			 * ask (ask the user whether to add/change the key) and no 
			 * (always insert the new key).
			 */
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(password);

			session.connect();
			channel = session.openChannel("exec");
			LogUtil logUtil = new LogUtil();
			String customLogFileName = logUtil.createCustomLogFileName(logFile, "appservers",server, cluster);
			String command = "";
      //This command needs to be modified as per the log file access. This command will copy the log file from the log folder to the home directory of the user who is logged in. 
			command = "sudo -u "+cluster+"r"+" cat /app/"+cluster+"/logs/"+logFile+" > /home/"+userName+"/"+customLogFileName;
			((ChannelExec)channel).setCommand(command);
			channel.setInputStream(null);
			((ChannelExec)channel).setErrStream(System.err);
			InputStream in = channel.getInputStream();
			channel.connect();
			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0) {
						break;
					}
					System.out.print(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					if (channel.getExitStatus() == 0) {
						System.out.println("Command executed successully.");
					}
					break;
				}
			}
			channel = session.openChannel("sftp");
			channel.connect();
			sftpChannel = (ChannelSftp)channel;
			File file = new File(logDownloadPath);
			if (!file.exists()) {
				if (file.mkdir()) {
					System.out.println("Directory is created!");
				} else {
					System.out.println("Failed to create directory!");
				}
			}
			sftpChannel.get("/home/"+userName+"/"+customLogFileName, logDownloadPath);
			sftpChannel.rm("/home/"+userName+"/"+customLogFileName);
		} catch(Exception e){
			e.printStackTrace();
		}
		finally{
			sftpChannel.disconnect();
			session.disconnect();
		}
	}
	
	
	/*
	 * This method sftps the log file from batch server to the local folder of the user. 
	 * 
	 */
	
	public void sftpBatchLogFileToLocal(String server, String userName, String password, String batchComp, String batchJob, String batchJobLog, String logDownloadPath){
		// Object Declaration.
		JSch jsch = new JSch();
		Session session = null;
		Channel channel = null;
		ChannelSftp sftpChannel = null;

		try {
			session = jsch.getSession(userName, server, 22);
			/*
			 * StrictHostKeyChecking Indicates what to do if the server's host 
			 * key changed or the server is unknown. One of yes (refuse connection), 
			 * ask (ask the user whether to add/change the key) and no 
			 * (always insert the new key).
			 */
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(password);

			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			sftpChannel = (ChannelSftp)channel;
			File file = new File(logDownloadPath);
			if (!file.exists()) {
				if (file.mkdir()) {
					System.out.println("Directory is created!");
				} else {
					System.out.println("Failed to create directory!");
				}
			}
			sftpChannel.get("/batch/"+batchComp+"/logs/"+batchJob+"/"+batchJobLog, logDownloadPath);
		} catch(Exception e){
			e.printStackTrace();
		}
		finally{
			sftpChannel.disconnect();
			session.disconnect();
		}
	}
	
	public String[] getLogClusterList(String server, String userName, String password) throws Exception{
		Session session = null;
		Channel channel = null;
		ChannelSftp sftpChannel = null;
		try{
			JSch jsch = new JSch();
			session = jsch.getSession(userName,server,22);
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			sftpChannel = (ChannelSftp)channel;
			sftpChannel.cd("/app/");
			ArrayList<String> logList = new ArrayList<String>();
			Vector<LsEntry> entries = sftpChannel.ls("pss*");
			for (LsEntry entry : entries) {
				System.out.println("Long Name::"+entry.getFilename());	
				logList.add(entry.getFilename());
			}
			Collections.sort(logList);
			String[] logFileArray = new String[logList.size()];
			for(int i=0;i<logList.size();i++){
				logFileArray[i]=logList.get(i);
			}
			return logFileArray;
		}
		catch(JSchException e){
			throw new Exception("Connection Exception");
		}
		finally{
			sftpChannel.disconnect();
			session.disconnect();
		}
	}
}
