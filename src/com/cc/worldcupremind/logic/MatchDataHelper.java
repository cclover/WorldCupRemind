package com.cc.worldcupremind.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.util.SparseArray;

import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.model.MatchStage;
import com.cc.worldcupremind.model.MatchStatus;
import com.cc.worldcupremind.model.MatchesModel;

public class MatchDataHelper {
	
	private static final String TAG = "MatchDataHelper";
	private static final String NETWORK_VERSION_FILE = "version.txt";
	private static final String NETWORK_DATA_FILE = "matches.json";
	private static final String ASSET_DATA_FILE = "matches.json";
	private static final String LOCAL_DATA_FILE = "matches.json";
	private static final String ENCODE_FORMAT = "UTF-8";
	
	/* json format*/
	private static final String MATCHES_LIST = "Matches";	
	private static final String MATCHES_DATA_VERSION = "Version";	/* Double */
	private static final String MATCHES_FILED_NO = "NO";			/* Int */
	private static final String MATCHES_FILED_STAGE = "Stage";		/* Enum */
	private static final String MATCHES_FILED_GROUP = "Group";		/* String */
	private static final String MATCHES_FILED_MONTH = "Month";		/* String */
	private static final String MATCHES_FILED_DAY = "Day";			/* String */
	private static final String MATCHES_FILED_WEEK = "Week";		/* Int */
	private static final String MATCHES_FILED_HOUR = "Hour";		/* String */
	private static final String MATCHES_FILED_TEAM_1 = "Team1";		/* String */
	private static final String MATCHES_FILED_TEAM_2 = "Team2";		/* String */
	private static final String MATCHES_FILED_STATUS= "Status";		/* Eunm*/
	private static final String MATCHES_FILED_SCORE_1= "Score1";	/* Int */
	private static final String MATCHES_FILED_SCORE_2= "Score2";	/* Int */
	
	private static MatchDataHelper instance = new MatchDataHelper();
	private SparseArray<MatchesModel> matchList;
	private double dataVersion;
	
	private MatchDataHelper(){
		matchList = null;
		dataVersion = 0;
	}
	
	public static MatchDataHelper getInstance(){
		return instance;
	}
	
	public SparseArray<MatchesModel> getMatchesList(Context context){
		
		LogHelper.d(TAG, "getMatchesList()");
		if(matchList == null){
			LogHelper.d(TAG, "Create the matches list");
			matchList = new SparseArray<MatchesModel>();

			//load match data
			File localFile = context.getFileStreamPath(LOCAL_DATA_FILE);
			if (localFile != null && localFile.exists()){
				loadMatchDataFromLocal(context);
			} else {
				loadMatchDataFromAsset(context);
			}
		}
		LogHelper.d(TAG, "List is exist");
		return matchList;
	}
	
	public Boolean haveNewVersion(){
		
		LogHelper.d(TAG, "haveNewVersion()");
		InputStream version = downloadFile(NETWORK_VERSION_FILE);
		if(version == null){
			LogHelper.e(TAG, "download file filed");
			return false;
		}
		String ver = covertStream2String(version);
		LogHelper.d(TAG, "Local version is:" + String.valueOf(dataVersion) + " Network version is :" + ver);
		try {
			JSONObject jsonObject = new JSONObject(ver);
			double newVer = jsonObject.optDouble(MATCHES_DATA_VERSION);
			return (newVer > dataVersion);
		} catch (JSONException e1) {
			LogHelper.d(TAG, e1.getMessage());
			e1.printStackTrace();
		}
		return false;
	}
	
	public SparseArray<MatchesModel> updateMatchData(Context context){
		
		loadMatchDataFromNetwork(context);
		return getMatchesList(context);
	}
	
	private void loadMatchDataFromAsset(Context context){
		
		LogHelper.d(TAG, "loadMatchDataFromAsset()");
		
		//Load data from asset
		InputStream matchesStream = null;
		AssetManager assetMrg = context.getAssets();
		try {
			matchesStream = assetMrg.open(ASSET_DATA_FILE);
		} catch (IOException e) {
			LogHelper.e(TAG, e.getMessage());
			return;
		}
		
		// Convert the stream to string
		String matchesString = covertStream2String(matchesStream);
		
		//parse the data
		if(!parseMatchData(context, matchesString)){
			Log.e(TAG, "parseMatchData failed");
			return;
		}
		
		//Save asset file to local file
		saveStreamToLocalFile(context, matchesString);
		
		//Close the input stream
		if(matchesStream != null){
			try {
				matchesStream.close();
			} catch (IOException e) {
				LogHelper.e(TAG, e.getMessage());
				return;
			}
		}
		
		LogHelper.d(TAG, "loadMatchDataFromAsset Successed!");
	}
	
	private void loadMatchDataFromLocal(Context context){
		
		LogHelper.d(TAG, "loadMatchDataFromLocal()");
		
		//Load data from local file
		InputStream matchesStream = null;
		try {
			matchesStream = context.openFileInput(LOCAL_DATA_FILE);
		} catch (FileNotFoundException e) {
			LogHelper.e(TAG, e.getMessage());
			return;
		}
		
		// Convert the stream to string
		String matchesString = covertStream2String(matchesStream);
		
		//parse the data
		if(!parseMatchData(context, matchesString)){
			Log.e(TAG, "parseMatchData failed");
			return;
		}
		
		//Close the input stream
		if(matchesStream != null){
			try {
				matchesStream.close();
			} catch (IOException e) {
				LogHelper.e(TAG, e.getMessage());
				return;
			}
		}
		
		LogHelper.d(TAG, "loadMatchDataFromLocal Successed!");
	}
	
	private Boolean loadMatchDataFromNetwork(Context context){
		
		LogHelper.d(TAG, "loadMatchDataFromNetwork()");
		
		//Load data from network
		InputStream matchesStream = downloadFile(NETWORK_DATA_FILE);
		if(matchesStream == null){
			LogHelper.e(TAG, "download file filed");
			return false;
		}
		
		// Convert the stream to string
		String matchesString = covertStream2String(matchesStream);
		
		//parse the data
		if(!parseMatchData(context, matchesString)){
			Log.e(TAG, "parseMatchData failed");
			return false;
		}
		
		//Save the file to local file
		saveStreamToLocalFile(context, matchesString);
		
		//Close the input stream
		if(matchesStream != null){
			try {
				matchesStream.close();
			} catch (IOException e) {
				LogHelper.e(TAG, e.getMessage());
				return false;
			}
		}
		
		LogHelper.d(TAG, "loadMatchDataFromNetwork()");
		return true;
	}
	
	private String covertStream2String(InputStream matchesStream){
		//Convert the file stream to string
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(matchesStream, ENCODE_FORMAT));
		} catch (UnsupportedEncodingException e1) {
			LogHelper.e(TAG, e1.getMessage());
			return null;
		}
		StringBuilder matchesString = new StringBuilder();    
		String line = null;    
		try {  
			while ((line = reader.readLine()) != null) {
				matchesString.append(line);    
			}
			return matchesString.toString().replaceAll( "\\s", "");
		} catch (IOException e) {    
			LogHelper.e(TAG, e.getMessage());
			return null;
		} 
	}
	
	private Boolean parseMatchData(Context context, String matchesString){
		
		LogHelper.d(TAG, "parseMatchData()");
	
		if(matchesString == null){
			LogHelper.e(TAG, "Failed to get string from stream");
			return false;
		}
		
		// Convert the json string to match object
		JSONTokener jsonParser  = new JSONTokener(matchesString);
		try {
			
			JSONObject matchesObj  = (JSONObject) jsonParser.nextValue();
			
			//parse data version
			dataVersion = matchesObj.getDouble(MATCHES_DATA_VERSION);
	    	LogHelper.d(TAG, "The match data version is:" + String.valueOf(dataVersion));
	    	
	    	//parse match data
			JSONArray matchesArray = matchesObj.getJSONArray(MATCHES_LIST);
			for(int i=0; i<matchesArray.length(); i++){  
				JSONObject matchObj = matchesArray.getJSONObject(i);
				int matchNo = matchObj.getInt(MATCHES_FILED_NO);
				MatchStage stage = MatchStage.valueOf(matchObj.getInt(MATCHES_FILED_STAGE));
				String group = matchObj.getString(MATCHES_FILED_GROUP);
				int week = matchObj.getInt(MATCHES_FILED_WEEK);
				String month = matchObj.getString(MATCHES_FILED_MONTH);
				String day = matchObj.getString(MATCHES_FILED_DAY);
				String hour = matchObj.getString(MATCHES_FILED_HOUR);
				String team1 = matchObj.getString(MATCHES_FILED_TEAM_1);
				String team2 = matchObj.getString(MATCHES_FILED_TEAM_2);
				MatchStatus status = MatchStatus.valueOf(matchObj.getInt(MATCHES_FILED_STATUS));
				int team1Score = matchObj.getInt(MATCHES_FILED_SCORE_1);
				int team2Score = matchObj.getInt(MATCHES_FILED_SCORE_2);
				MatchesModel matchItem = new MatchesModel(matchNo, stage, group, week, month, day, hour, 
						team1, team2, status, team1Score, team2Score, false);
				matchList.put(matchNo, matchItem);
			}
		} catch (JSONException e) {
			LogHelper.e(TAG, e.getMessage());
			e.printStackTrace();
			return false;
		} 
		
		LogHelper.d(TAG, "parseMatchData successed!" + String.valueOf(matchList.size()));
		return true;
	}
	
	private void saveStreamToLocalFile(Context context, String matchesString){

		LogHelper.d(TAG, "saveStreamToLocalFile()");
		
		File localFile = context.getFileStreamPath(LOCAL_DATA_FILE);
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(localFile);
			fileWriter.write(matchesString);
		} catch (IOException e) {
			LogHelper.e(TAG, e.getMessage());
			e.printStackTrace();
			return;
		} finally {
			try {
				if(fileWriter != null){
					fileWriter.close();
				}
			} catch (IOException e) {
				LogHelper.e(TAG, e.getMessage());
				e.printStackTrace();
			}
		}

		LogHelper.d(TAG, "saveStreamToLocalFile Successed!");
	}

	private InputStream downloadFile(String file){
		
		LogHelper.d(TAG, "downloadFile() :" + file);
		
		//Connect to the FTP Server
		FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect("cclover.free3v.net", 21);
			ftpClient.login("cclover", "12345cc");
			
			//Check reply code
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply))
			{
				ftpClient.disconnect();
				return null;
			}
			
			// List files and downloade
			FTPFile[] files = ftpClient.listFiles();
			for (FTPFile ftpFile : files) {
				if (ftpFile.getName().equals(file)) {
					return ftpClient.retrieveFileStream(ftpFile.getName());
				}
			}
		} catch (SocketException e1) {
			LogHelper.e(TAG, e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e1) {
			LogHelper.e(TAG, e1.getMessage());
			e1.printStackTrace();
		}
		return null;
		
//		InputStream istream = null;
//    	URL url = null;
//    	HttpURLConnection httpConn = null;
//    	try {
//    			//Get update file stream
//				url = new URL(file);
//				httpConn = (HttpURLConnection)url.openConnection();
//			    HttpURLConnection.setFollowRedirects(true);
//	 		    httpConn.setRequestMethod("GET");
//			    httpConn.setRequestProperty("User-Agent", "Mozilla/4.0(compatible;MSIE 6.0;Windows 2000)");
//			    istream = httpConn.getInputStream();
//		    	LogHelper.d(TAG, "downloadFile Successed!");
//			    return istream;       
//		} catch (IOException e) {
//			LogHelper.e(TAG, e.getMessage());
//			return null;
//		}
	}
}
