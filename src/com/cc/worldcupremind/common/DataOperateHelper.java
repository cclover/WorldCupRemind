package com.cc.worldcupremind.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import android.content.Context;
import android.content.res.AssetManager;

/*
 * This is a data tool class. Responsible for check read, write and download data file.
 * Covert between stream and string and so on.
 */
public class DataOperateHelper {
	
	private static final String TAG = "DataOperateHelper";
	
	/* FTP Server info*/
	private static final String FTP_SERVER_URL = "cclover.free3v.net";
	private static final int FTP_SERVER_PORT = 21;
	private static final String FTP_USER_NAME = "cclover";
	private static final String FTP_USER_PASSWORD = "12345cc";
	private static final int NETWORK_TIMEOUT = 20*1000;

	
	/*
	 * Check the file is exist in local private files folder or not
	 * 
	 * @param context
	 * The @Context object
	 * 
	 * @param fileName
	 * The file name
	 * 
	 * @return
	 * Ture if file exist, otherwise false
	 */
	public static Boolean isLocalFileExist(Context context, String fileName){
		File localFile = context.getFileStreamPath(fileName);
		if (localFile != null && localFile.exists()){
			return true;
		}
		return false;
	}
	
	/*
	 * Convert the Stream to String
	 * 
	 * @param dataStream
	 * The stream need convert
	 * 
	 * @PARAM format
	 * The stream format
	 * 
	 * @return
	 * Return NULL if fail.
	 * 
	 */
	public static String covertStream2String(InputStream dataStream, String format){
		
		LogHelper.d(TAG, "covertStream2String()");
		
		//Convert the file stream to string
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(dataStream, format));
		} catch (UnsupportedEncodingException e1) {
			LogHelper.e(TAG, e1);
			return null;
		}
		
		//Read from stream
		StringBuilder matchesString = new StringBuilder();    
		String line = null;    
		try {  
			while ((line = reader.readLine()) != null) {
				matchesString.append(line);    
			}
			return matchesString.toString().replaceAll( "\\s", ""); //remove newline char,space char
		} catch (IOException e) {    
			LogHelper.e(TAG, e);
			return null;
		} 
	}
	
	
	/*
	 * Save the String to local file in PRIVATE FILES folder
	 * 
	 * @param context
	 * Context
	 * 
	 * @param dataString
	 * The string want to save
	 * 
	 * @param fileName
	 * The local file name
	 */
	public static Boolean saveData2LocalFile(Context context, String dataString, String fileName){

		LogHelper.d(TAG, "saveStreamToLocalFile()");
		
		File localFile = context.getFileStreamPath(fileName);
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(localFile);
			fileWriter.write(dataString);
		} catch (IOException e) {
			LogHelper.e(TAG, e);
			return false;
		} finally {
			try {
				if(fileWriter != null){
					fileWriter.close();
				}
			} catch (IOException e) {
				LogHelper.e(TAG, e);
				return false;
			}
		}
		
		LogHelper.d(TAG, "saveStreamToLocalFile Successed!");
		return true;
	}
	
	/*
	 * Load the data file from Asset folder
	 * 
	 * @param context
	 * The @Context object
	 * 
	 * @param file
	 * File name on FTP Server or HTTP URL
	 * 
	 * @return 
	 * The file input stream if successes. return NULL if fail.
	 */
	public static InputStream loadFileFromAsset(Context context, String fileName){
		
		LogHelper.d(TAG, "loadFileFromAsset:" + fileName);
		AssetManager assetMrg = context.getAssets();
		try {
			return assetMrg.open(fileName);
		} catch (IOException e) {
			LogHelper.e(TAG, e);
			return null;
		}
	}
	
	
	public static InputStream loadFileFromLocal(Context context, String fileName){
		
		LogHelper.d(TAG, "loadFileFromLocal:" + fileName);
		
		try {
			return context.openFileInput(fileName);
		} catch (FileNotFoundException e) {
			LogHelper.e(TAG, e);
			return null;
		}
	}

	/*
	 * Download the data file from network (HTTP)
	 * 
	 * @param fileList
	 * file URL list want to donwload on HTTP Server
	 * 
	 * @return 
	 * The file input stream List if successes. return NULL if fail.
	 */
	public static InputStream loadFileFromHTTPNetwork(String fileURL){
		
		LogHelper.d(TAG, "loadFileFromNetwork: HTTP" + fileURL);
			
		//download from http server
		InputStream istream = null;
    	URL url = null;
    	HttpURLConnection httpConn = null;
    	try {
    			//Get update file stream
				url = new URL(fileURL);
				httpConn = (HttpURLConnection)url.openConnection();
			    HttpURLConnection.setFollowRedirects(true);
			    httpConn.setConnectTimeout(NETWORK_TIMEOUT);
			    httpConn.setReadTimeout(NETWORK_TIMEOUT);
	 		    httpConn.setRequestMethod("GET");
			    httpConn.setRequestProperty("User-Agent", "Mozilla/4.0(compatible;MSIE 6.0;Windows 2000)");
			    istream = httpConn.getInputStream();
			    return istream;

		}  catch (MalformedURLException e) {
			LogHelper.e(TAG, e);
		} catch (IOException e) {
			LogHelper.e(TAG, e);
		} finally {
			if(httpConn != null){
				httpConn.disconnect();
			}
		}
		return null;
	}

	
	/*
	 * Download the data file from network (FTP)
	 * 
	 * @param fileName
	 * file name want to donwload on FTP Server
	 * 
	 * @return 
	 * The file input stream if successes. return NULL if fail.
	 */
	public static InputStream loadFileFromFTPNetwork(String fileName){
		
		LogHelper.d(TAG, "loadFileFromFTPNetwork:" + fileName);
		
		//Connect to FTP, each FTPClient can only donwload one file....
		FTPClient ftpClient =  new FTPClient(); 
		try {	
			
			//Connect
			ftpClient.setConnectTimeout(NETWORK_TIMEOUT);
			ftpClient.setDataTimeout(NETWORK_TIMEOUT);
			ftpClient.connect(FTP_SERVER_URL, FTP_SERVER_PORT);
			ftpClient.login(FTP_USER_NAME, FTP_USER_PASSWORD);
			
			//Check reply code
			LogHelper.d(TAG, "Get Reply from FTP Server");
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply))
			{
				LogHelper.w(TAG, "Login FTP Server failed");
				ftpClient.disconnect();
				return null;
			}
			LogHelper.d(TAG, "Connect to FTP Server Success");
			
			// Download
			LogHelper.d(TAG, "Start to download:" + fileName);
			InputStream inStream = ftpClient.retrieveFileStream(fileName);
			if(inStream == null){
				LogHelper.d(TAG, "download files is null(FTP)");
			}else{
				LogHelper.d(TAG, "download files success(FTP)");
			}
			return inStream;
		} catch (SocketException e1) {
			LogHelper.e(TAG, e1);
		} catch (IOException e1) {
			LogHelper.e(TAG, e1);
		} finally {
			if(ftpClient != null){
				try {
					ftpClient.disconnect();
				} catch (IOException e) {
					LogHelper.e(TAG, e);
				}
			}
		}
		return null;
	}	
}