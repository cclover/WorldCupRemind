package com.cc.worldcupremind.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;

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

import com.cc.worldcupremind.common.DataOperateHelper;
import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.model.MatchStage;
import com.cc.worldcupremind.model.MatchStatus;
import com.cc.worldcupremind.model.MatchesModel;

/*
 * This class will help to load all matches data from data file.
 * 
 * We Save the match data in *.json files. When the APP launch at first time, will copy the data files
 * from ASSET to PRIVATE FILES folder. Can update the data from FTP server.
 */
public class MatchDataHelper {
	
	private static final String TAG = "MatchDataHelper";
	
	/* Data files name */
	private static final String DATA_VERSION_FILE = "version.txt";
	private static final String DATA_MATCHES_FILE = "matches.json";
	private static final String FILE_ENCODE_FORMAT = "UTF-8";
	
	/* matches.json format */
	private static final String JSON_MATCHES_LIST = "Matches";	
	private static final String JSON_MATCHES_DATA_VERSION = "Version";	/* Double */
	private static final String JSON_MATCHES_FILED_NO = "NO";			/* Int */
	private static final String JSON_MATCHES_FILED_STAGE = "Stage";		/* Enum */
	private static final String JSON_MATCHES_FILED_GROUP = "Group";		/* String */
	private static final String JSON_MATCHES_FILED_MONTH = "Month";		/* String */
	private static final String JSON_MATCHES_FILED_DAY = "Day";			/* String */
	private static final String JSON_MATCHES_FILED_WEEK = "Week";		/* Int */
	private static final String JSON_MATCHES_FILED_HOUR = "Hour";		/* String */
	private static final String JSON_MATCHES_FILED_TEAM_1 = "Team1";	/* String */
	private static final String JSON_MATCHES_FILED_TEAM_2 = "Team2";	/* String */
	private static final String JSON_MATCHES_FILED_STATUS= "Status";	/* Eunm*/
	private static final String JSON_MATCHES_FILED_SCORE_1= "Score1";	/* Int */
	private static final String JSON_MATCHES_FILED_SCORE_2= "Score2";	/* Int */
	
	/* Matches list */
	private SparseArray<MatchesModel> matchesList;
	
	/* matche.json files version*/
	private double dataMatchesVersion;
	
	/*
	 * Construct
	 */
	public MatchDataHelper(){
		matchesList = null;
		dataMatchesVersion = 0;
	}
	
	/*
	 * Get the matches data list
	 * 
	 * @param context
	 * The @Context object
	 * 
	 * @return
	 * return the @MatchesModel list
	 */
	public SparseArray<MatchesModel> getMatchesList(Context context){
		
		LogHelper.d(TAG, "getMatchesList()");
		
		if(matchesList == null){
			LogHelper.d(TAG, "Create the matches list");
			matchesList = new SparseArray<MatchesModel>();
			
			if(DataOperateHelper.isLocalFileExist(context, DATA_MATCHES_FILE)){
				if(!loadMatchesDataFromLocal(context)){
					LogHelper.e(TAG, "getMatchesList from local failed");
					return null;
				}
			} else {
				if(!loadMatchesDataFromAsset(context)){
					LogHelper.e(TAG, "getMatchesList from asset failed");
					return null;
				}
			}
		} else {
			LogHelper.d(TAG, "No need to load from file");
		}
		LogHelper.d(TAG, "Return the matchesList size:" + String.valueOf(matchesList.size()));
		return matchesList;
	}
	
	/*
	 * Check the data version
	 * 
	 * @return
	 * Have new version return true, otherwise return false
	 */
	public Boolean checkNewVersion(){
		
		LogHelper.d(TAG, "haveNewVersion()");
		
		//Download file
		InputStream verStream = DataOperateHelper.loadFileFromNetwork(DATA_VERSION_FILE);
		if(verStream == null){
			LogHelper.w(TAG, "Download version file filed");
			return false;
		}
		
		//Get the version
		String ver = DataOperateHelper.covertStream2String(verStream, FILE_ENCODE_FORMAT);
		if(ver == null){
			LogHelper.w(TAG, "covertStream2String filed");
			return false;
		}
		
		LogHelper.d(TAG, "Local version is:" + String.valueOf(dataMatchesVersion) + " Network version is :" + ver);
		try {
			JSONObject jsonObject = new JSONObject(ver);
			double newVer = jsonObject.optDouble(JSON_MATCHES_DATA_VERSION);
			return (newVer > dataMatchesVersion);
		} catch (JSONException e1) {
			LogHelper.e(TAG, e1);
		}
		return false;
	}
	
	
	public SparseArray<MatchesModel> updateMatchesData(Context context){
		
		LogHelper.d(TAG, "getMatchesList()");
		loadMatchDataFromNetwork(context);
		return getMatchesList(context);
	}
	
	
	/*
	 * Load the Matches Data from asset file when first launch, and copy to private folder
	 * 
	 * @return 
	 * True if successes, False if fail. 
	 */
	private Boolean loadMatchesDataFromAsset(Context context){
		
		LogHelper.d(TAG, "loadMatchDataFromAsset()");
		
		//Load data from asset
		InputStream matchesStream = DataOperateHelper.loadFileFromAsset(context, DATA_MATCHES_FILE);
		if(matchesStream == null){
			Log.w(TAG, "loadFileFromAsset failed");
			return false;
		}
		
		// Convert the stream to string
		String matchesString = DataOperateHelper.covertStream2String(matchesStream, FILE_ENCODE_FORMAT);
		if(matchesString == null){
			Log.w(TAG, "covertStream2String failed");
			return false;
		}
		
		// Parse the data
		if(!parseMatchesData(context, matchesString)){
			Log.w(TAG, "parseMatchData failed");
			return false;
		}
		
		//Save asset file to local file
		if(!DataOperateHelper.saveData2LocalFile(context, matchesString, DATA_MATCHES_FILE)){
			Log.w(TAG, "saveData2LocalFile failed");
			return false;
		}
		
		//Close the input stream
		if(matchesStream != null){
			try {
				matchesStream.close();
			} catch (IOException e) {
				LogHelper.e(TAG, e.getMessage());
				return false;
			}
		}
		
		LogHelper.d(TAG, "loadMatchDataFromAsset Successed!");
		return true;
	}
	
	/*
	 * Load the Matches data from private folder
	 * 
	 * @return 
	 * True if successes, False if fail. 
	 */
	private Boolean loadMatchesDataFromLocal(Context context){
		
		LogHelper.d(TAG, "loadMatchDataFromLocal()");
		
		//Load data from local file
		InputStream matchesStream = DataOperateHelper.loadFileFromLocal(context, DATA_MATCHES_FILE);
		if(matchesStream == null){
			Log.w(TAG, "loadFileFromLocal failed");
			return false;
		}
		
		// Convert the stream to string
		String matchesString = DataOperateHelper.covertStream2String(matchesStream, FILE_ENCODE_FORMAT);
		if(matchesString == null){
			Log.w(TAG, "covertStream2String failed");
			return false;
		}
		
		// Parse the data
		if(!parseMatchesData(context, matchesString)){
			Log.w(TAG, "parseMatchData failed");
			return false;
		}
		
		//Close the input stream
		if(matchesStream != null){
			try {
				matchesStream.close();
			} catch (IOException e) {
				LogHelper.e(TAG, e);
				return false;
			}
		}
		
		LogHelper.d(TAG, "loadMatchDataFromLocal Successed!");
		return true;
	}
	
	/*
	 * Load the Matches data from network
	 * 
	 * @param context
	 * The @Context object
	 * 
	 * @return 
	 * True if successes, False if fail. 
	 */
	private Boolean loadMatchDataFromNetwork(Context context){
		
		LogHelper.d(TAG, "loadMatchDataFromNetwork()");
		
		//Load data from network
		InputStream matchesStream = DataOperateHelper.loadFileFromNetwork(DATA_MATCHES_FILE);
		if(matchesStream == null){
			LogHelper.w(TAG, "loadFileFromNetwork failed");
			return false;
		}
		
		// Convert the stream to string
		String matchesString = DataOperateHelper.covertStream2String(matchesStream, FILE_ENCODE_FORMAT);
		if(matchesString == null){
			Log.w(TAG, "covertStream2String failed");
			return false;
		}
		
		// Parse the data
		if(!parseMatchesData(context, matchesString)){
			Log.w(TAG, "parseMatchData failed");
			return false;
		}

		//Save asset file to local file
		if(!DataOperateHelper.saveData2LocalFile(context, matchesString, DATA_MATCHES_FILE)){
			Log.w(TAG, "parseMatchData failed");
			return false;
		}
		
		//Close the input stream
		if(matchesStream != null){
			try {
				matchesStream.close();
			} catch (IOException e) {
				LogHelper.e(TAG, e);
				return false;
			}
		}
		
		LogHelper.d(TAG, "loadMatchDataFromNetwork()");
		return true;
	}
	

	/*
	 * Parse the Data from json format to @MatchesModel object into matchesList
	 * 
	 * @param context
	 * The @Context object
	 * 
	 * @return
	 * True if successes, False if fail. 
	 * 
	 */
	private Boolean parseMatchesData(Context context, String matchesString){
		
		LogHelper.d(TAG, "parseMatchData()");
	
		//Check the string
		if(matchesString == null || matchesString.isEmpty()){
			LogHelper.w(TAG, "The matchesString is empty");
			return false;
		}
		
		// Convert the json string to match object
		JSONTokener jsonParser  = new JSONTokener(matchesString);
		try {
			
			JSONObject matchesObj  = (JSONObject) jsonParser.nextValue();
			
			//parse data version
			dataMatchesVersion = matchesObj.getDouble(JSON_MATCHES_DATA_VERSION);
	    	LogHelper.d(TAG, "The match data version is:" + String.valueOf(dataMatchesVersion));
	    	
	    	//parse match data
			JSONArray matchesArray = matchesObj.getJSONArray(JSON_MATCHES_LIST);
			for(int i=0; i<matchesArray.length(); i++){  
				JSONObject matchObj = matchesArray.getJSONObject(i);
				int matchNo = matchObj.getInt(JSON_MATCHES_FILED_NO);
				MatchStage stage = MatchStage.valueOf(matchObj.getInt(JSON_MATCHES_FILED_STAGE));
				String group = matchObj.getString(JSON_MATCHES_FILED_GROUP);
				int week = matchObj.getInt(JSON_MATCHES_FILED_WEEK);
				String month = matchObj.getString(JSON_MATCHES_FILED_MONTH);
				String day = matchObj.getString(JSON_MATCHES_FILED_DAY);
				String hour = matchObj.getString(JSON_MATCHES_FILED_HOUR);
				String team1 = matchObj.getString(JSON_MATCHES_FILED_TEAM_1);
				String team2 = matchObj.getString(JSON_MATCHES_FILED_TEAM_2);
				MatchStatus status = MatchStatus.valueOf(matchObj.getInt(JSON_MATCHES_FILED_STATUS));
				int team1Score = matchObj.getInt(JSON_MATCHES_FILED_SCORE_1);
				int team2Score = matchObj.getInt(JSON_MATCHES_FILED_SCORE_2);
				MatchesModel matchItem = new MatchesModel(matchNo, stage, group, week, month, day, hour, 
						team1, team2, status, team1Score, team2Score, false);
				matchesList.put(matchNo, matchItem);
			}
		} catch (JSONException e) {
			LogHelper.w(TAG, "Parse the JSON failed");
			LogHelper.e(TAG, e);
			return false;
		} 
		
		LogHelper.d(TAG, "parseMatchData successed!" + String.valueOf(matchesList.size()));
		return true;
	}
}
