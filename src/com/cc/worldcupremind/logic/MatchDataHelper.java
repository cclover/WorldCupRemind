package com.cc.worldcupremind.logic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
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
class MatchDataHelper {
	
	private static final String TAG = "MatchDataHelper";
	
	/* Data files name */
	private static final String DATA_VERSION_FILE = "version.txt";
	private static final String DATA_MATCHES_FILE = "matches.json";
	private static final String DATA_NATIONAL_FILE = "national.json";
	private static final String DATA_REMIND_FILE = "remind.json";
	private static final String FILE_ENCODE_FORMAT = "UTF-8";
	
	/* matches.json format */
	private static final String JSON_MATCHES_DATA_VERSION = "Version";	/* Double */
	private static final String JSON_MATCHES_LIST = "Matches";			/* Array */
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
	
	/* national.json format */
	private static final String JSON_NATIONAL_LIST = "National";		/* Array */
	private static final String JSON_NATIONAL_ID = "id";				/* String */
	private static final String JSON_NATIONAL_NAME = "name";			/* String */
	
	/* remind.json format */
	private static final String JSON_REMIND_LIST = "Remind";			/* Array */
	private static final String JSON_REMIND_MATCH_NO = "no";			/* Int */
	
	/* MatchesModel list */
	private SparseArray<MatchesModel> matchesList;
	
	/* National name string map*/
	private HashMap<String, String> nationalMap;
	
	/* Remind list */
	private SparseArray<MatchesModel> remindList;
	
	private ArrayList<Integer> remindCancelList;
	

	/* matche.json files version*/
	private double dataMatchesVersion;
	
	private Context context;
	
	
	public enum UPDATE_RET{

		RET_CHECK_UPDATE_ERROR,
		RET_NO_NEED_UPDATE,
		RET_UPDATE_ERROR,
		RET_OK,
	}
	
	/*
	 * Construct
	 */
	public MatchDataHelper(Context context){
		
		this.dataMatchesVersion = 0;
		this.matchesList = new SparseArray<MatchesModel>();
		this.nationalMap = new HashMap<String, String>();
		this.remindList = new SparseArray<MatchesModel>();
		this.remindCancelList = new ArrayList<Integer>();
		this.context = context;
	}
	
	public SparseArray<MatchesModel> getMatchesList() {
		return matchesList;
	}

	public HashMap<String, String> getNationalMap() {
		return nationalMap;
	}

	public SparseArray<MatchesModel> getRemindList() {
		return remindList;
	}
	
	public ArrayList<Integer> getRemindCancelList() {
		return remindCancelList;
	}
	
	/*
	 * Get the matches data list
	 * 
	 * @return
	 * True if successes, False if fail. 
	 */
	public Boolean loadMatchesData(){
		
		LogHelper.d(TAG, "loadMatchesData()");
		
		Boolean ret = false;
		if(DataOperateHelper.isLocalFileExist(context, DATA_MATCHES_FILE)){
			ret = loadMatchesDataFromLocal();
		} else {
			ret = loadMatchesDataFromAsset();
		}
		
		if(ret){
			LogHelper.d(TAG, "Load the matches data size:" + String.valueOf(matchesList.size()));
		} else {
			LogHelper.w(TAG, "Load the matches data failed");
		}
		return ret;
	}
	

	/*
	 * Update the all data files from network
	 * 
	 * @return
	 * return @UPDATE_RESUT
	 */
	public UPDATE_RET updateAllDataFiles(){
		
		LogHelper.d(TAG, "updateAllData()");

		// Check update.
		LogHelper.d(TAG, "Check version file");
		ArrayList<String> updateList = checkNewVersion();
		if(updateList == null){
			LogHelper.w(TAG, "Fail to check the new version");
			return UPDATE_RET.RET_CHECK_UPDATE_ERROR;
		} else if(updateList.size() == 0){
			 LogHelper.d(TAG, "current is latest version");
			 return UPDATE_RET.RET_NO_NEED_UPDATE;
		}
		 
		// Update each data
		LogHelper.d(TAG, "Start to update files");
		for(String updateFile : updateList){
			
			LogHelper.d(TAG, "update the file:" + updateFile);
			Boolean ret = false; 
			if(updateFile.equals(DATA_MATCHES_FILE)){
				ret = loadMatchDataFromNetwork();
			}
			
			//TODO: update other files
			
			if(!ret){
				LogHelper.w(TAG, "update failed!");
				return UPDATE_RET.RET_UPDATE_ERROR;
			}
		}
		
		LogHelper.d(TAG, "all update success!");
		return UPDATE_RET.RET_OK;
	}
	
	/*
	 * Load the National Data from asset file
	 * 
	 * @return 
	 * True if successes, False if fail. 
	 * 
	 */
	public Boolean loadNationalData(){
		
		LogHelper.d(TAG, "loadNationalData()");
		
		//Load data from asset
		LogHelper.d(TAG, "load National Data From Asset");
		InputStream nationalStream = DataOperateHelper.loadFileFromAsset(context, DATA_NATIONAL_FILE);
		if(nationalStream == null){
			Log.w(TAG, "loadFileFromAsset failed");
			return false;
		}
		
		// Convert the stream to string
		String nationalString = DataOperateHelper.covertStream2String(nationalStream, FILE_ENCODE_FORMAT);
		try {
			nationalStream.close(); //close stream
		} catch (IOException e) {
			LogHelper.e(TAG, e);
			return false;
		}
		if(nationalString == null){
			Log.w(TAG, "covertStream2String failed");
			return false;
		}
		
		// Parse the data
		LogHelper.d(TAG, "Parse National Data From JSON");
		JSONTokener jsonParser  = new JSONTokener(nationalString);
		try {
			JSONObject nationalObj = (JSONObject) jsonParser.nextValue();
			JSONArray nationalArray = nationalObj.getJSONArray(JSON_NATIONAL_LIST);
			for(int i=0; i<nationalArray.length(); i++){  
				JSONObject national = nationalArray.getJSONObject(i);
				String id = national.getString(JSON_NATIONAL_ID);
				String name = national.getString(JSON_NATIONAL_NAME);
				nationalMap.put(id, name);
			}
		} catch (JSONException e1) {
			LogHelper.w(TAG, "Parse the national.json failed");
			LogHelper.e(TAG, e1);
			return false;
		} catch (ClassCastException ex){ //If string format error, will throw ClassCastException
			LogHelper.w(TAG, "Parse the national.json failed");
			LogHelper.e(TAG, ex);
			return false;
		}

		LogHelper.d(TAG, "Load the national data size:" + String.valueOf(nationalMap.size()));
		return true; 
	}

	
	/*
	 * Load the remind data list
	 * 
	 * @return
	 * return the @ArrayList<Integer> remind match number list
	 */
	public Boolean loadRemindData(){
		
		LogHelper.d(TAG, "loadRemindData()");
		remindList.clear();
		remindCancelList.clear();
		
		// Check file
		if(!DataOperateHelper.isLocalFileExist(context, DATA_REMIND_FILE)){
			Log.d(TAG, "remind.json is not exist!");
			return true;
		}
		
		// Load remind data from local file
		InputStream remindStream = DataOperateHelper.loadFileFromLocal(context, DATA_REMIND_FILE);
		if(remindStream == null){
			Log.w(TAG, "Load remind data failed");
			return false;
		}
		
		// Convert the stream to string
		String remindString = DataOperateHelper.covertStream2String(remindStream, FILE_ENCODE_FORMAT);
		try {
			remindStream.close(); //close stream
		} catch (IOException e) {
			LogHelper.e(TAG, e);
			return false;
		}
		
		if(remindString == null){
			Log.w(TAG, "covertStream2String failed");
			return false;
		}
		
		// Parse the remind data
		JSONTokener jsonParser  = new JSONTokener(remindString);
		try {
			JSONObject rootObj  = (JSONObject) jsonParser.nextValue();
			
	    	//parse remind data
			JSONArray remindArray = rootObj.getJSONArray(JSON_REMIND_LIST);
			for(int i = 0; i < remindArray.length(); i++){  
				JSONObject remindObj = remindArray.getJSONObject(i);
				int matchNo = remindObj.getInt(JSON_REMIND_MATCH_NO);
				MatchesModel match =  matchesList.get(matchNo); 
				match.setIsRemind(true); //update the isRemind field
				remindList.put(matchNo, match);
			}
		} catch (JSONException e) {
			LogHelper.w(TAG, "Parse the remind.json failed");
			LogHelper.e(TAG, e);
			return false;
		} catch (ClassCastException ex){ //If string format error, will throw ClassCastException
			LogHelper.w(TAG, "Parse the remind.json failed");
			LogHelper.e(TAG, ex);
			return false;
		}
				
		LogHelper.d(TAG, "Return the remindList size:" + String.valueOf(remindList.size()));
		return true;
	}
	
	
	/*
	 * Update the Remind Data
	 * 
	 * @param newlist 
	 * New remind matches number list 
	 */
	public Boolean setRemindData(ArrayList<Integer> newlist){

		LogHelper.d(TAG, "setRemindData()");
		
		// renew the remind list
		if(newlist == null){
			LogHelper.w(TAG, "The new remind list is null");
			return false;
		}
		
		// save to file first
		if(!saveRemindData(newlist)){
			LogHelper.w(TAG, "Fail to save the remind data");
			return false;
		}
		
		//Construct the cancel list
		LogHelper.d(TAG, "Construct the cancel list");
		remindCancelList.clear();
		for(int i = 0; i < remindList.size(); i++){
			int matchNo = remindList.keyAt(i);
			if(!newlist.contains(matchNo)){
				LogHelper.d(TAG, "Cancel alarm MatchNo:" + String.valueOf(matchNo));
				remindCancelList.add(matchNo);
				
				//Reset the isRemind flag
				MatchesModel match = matchesList.get(matchNo);
				match.setIsRemind(false);
			}
		}
		
		// clear the remind list
		LogHelper.d(TAG, "Renew the remindList and matchesList");
		remindList.clear();
		for(int no : newlist){
			LogHelper.d(TAG, "Set alarm MatchNo:" + String.valueOf(no));
			MatchesModel match = matchesList.get(no);
			if(match == null){
				LogHelper.w(TAG, "The matchNo is not exist:" + String.valueOf(no));
				continue;
			}
			match.setIsRemind(true);
			remindList.put(no, match);
		}
		
		//For test
		LogHelper.d(TAG, "Show Alarm list:");
		for(int i = 0; i < matchesList.size(); i++){
			MatchesModel match = matchesList.valueAt(i);
			if(match.getIsRemind()){
				LogHelper.d(TAG, "alarm MathcNo:" + String.valueOf(match.getMatchNo()));
			}
		}
		return true;
	}
	
	/*
	 * Save the Remind Data into local file
	 * 
	 * @return 
	 * True if successes, False if fail. 
	 */
	private Boolean saveRemindData(ArrayList<Integer> list){
		
		LogHelper.d(TAG, "saveRemindData()");
		
		// Check the list
		if(list == null || list.size() == 0){
			
			LogHelper.d(TAG, "No remind data to save");
			return true;
		}
		
		JSONObject rootObj = new JSONObject();
		JSONArray remindArray = new JSONArray();
		// Construct the JSON Object
		try {
			for(int i = 0; i < list.size(); i++){
				JSONObject remindObj = new JSONObject();
				remindObj.put(JSON_REMIND_MATCH_NO, list.get(i));
				remindArray.put(remindObj);
			}
			rootObj.put(JSON_REMIND_LIST, remindArray);
		} catch (JSONException e) {
			LogHelper.e(TAG, e);
			return false;
		}
		
		// Save to file
		String remindString = rootObj.toString();
		if(!DataOperateHelper.saveData2LocalFile(context, remindString, DATA_REMIND_FILE)){
			LogHelper.w(TAG, "Fail to save the remind.json");
			return false;
		}
		
		LogHelper.d(TAG, "saveRemindData Successed!");
		return true;
	}
	
	
	/*
	 * Check the data version
	 * 
	 * @return
	 * Return the need update file name list, return null when error
	 */
	private ArrayList<String> checkNewVersion(){
		
		LogHelper.d(TAG, "checkNewVersion()");
		ArrayList<String> updateFileList = new ArrayList<String>();
		
		//Get Download file stream
		InputStream verStream = DataOperateHelper.loadFileFromFTPNetwork(DATA_VERSION_FILE);
		if(verStream == null){
			LogHelper.w(TAG, "Download version file filed");
			return null;
		}
		
		//Get the version
		LogHelper.d(TAG, "Download success!");
		String ver = DataOperateHelper.covertStream2String(verStream, FILE_ENCODE_FORMAT);
		try {
			verStream.close(); //close stream
		} catch (IOException e) {
			LogHelper.e(TAG, e);
			return null;
		}
		
		if(ver == null){
			LogHelper.w(TAG, "covertStream2String filed");
			return null;
		}
		
		LogHelper.d(TAG, "Local version is:" + String.valueOf(dataMatchesVersion) + " Network version is :" + ver);
		try {
			JSONObject jsonObject = new JSONObject(ver);
			double newMatchesVer = jsonObject.optDouble(JSON_MATCHES_DATA_VERSION);
			if(newMatchesVer > dataMatchesVersion){
				updateFileList.add(DATA_MATCHES_FILE);
			}
			//TODO: same operate for other file
			return updateFileList;
		} catch (JSONException e1) {
			LogHelper.e(TAG, e1);
		}
		return null;
	}
	

	/*
	 * Load the Matches Data from asset file and copy to private folder
	 * 
	 * @return 
	 * True if successes, False if fail. 
	 */
	private Boolean loadMatchesDataFromAsset(){
		
		LogHelper.d(TAG, "loadMatchDataFromAsset()");
		
		//Load data from asset
		InputStream matchesStream = DataOperateHelper.loadFileFromAsset(context, DATA_MATCHES_FILE);
		if(matchesStream == null){
			Log.w(TAG, "loadFileFromAsset failed");
			return false;
		}
		
		// Convert the stream to string
		String matchesString = DataOperateHelper.covertStream2String(matchesStream, FILE_ENCODE_FORMAT);
		try {
			matchesStream.close(); //close stream
		} catch (IOException e) {
			LogHelper.e(TAG, e);
			return false;
		}
		
		if(matchesString == null){
			Log.w(TAG, "covertStream2String failed");
			return false;
		}
		
		// Parse the data
		if(!parseMatchesData(matchesString)){
			Log.w(TAG, "parseMatchData failed");
			return false;
		}
		
		//Save asset file to local file
		if(!DataOperateHelper.saveData2LocalFile(context, matchesString, DATA_MATCHES_FILE)){
			Log.w(TAG, "saveData2LocalFile failed");
			return false;
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
	private Boolean loadMatchesDataFromLocal(){
		
		LogHelper.d(TAG, "loadMatchDataFromLocal()");
		
		//Load data from local file
		InputStream matchesStream = DataOperateHelper.loadFileFromLocal(context, DATA_MATCHES_FILE);
		if(matchesStream == null){
			Log.w(TAG, "loadFileFromLocal failed");
			return false;
		}
		
		// Convert the stream to string
		String matchesString = DataOperateHelper.covertStream2String(matchesStream, FILE_ENCODE_FORMAT);
		try {
			matchesStream.close(); //close stream
		} catch (IOException e) {
			LogHelper.e(TAG, e);
			return false;
		}
		
		if(matchesString == null){
			Log.w(TAG, "covertStream2String failed");
			return false;
		}
		
		// Parse the data
		if(!parseMatchesData(matchesString)){
			Log.w(TAG, "parseMatchData failed");
			return false;
		}
		
		LogHelper.d(TAG, "loadMatchDataFromLocal Successed!");
		return true;
	}
	
	/*
	 * Load the Matches data from network
	 * 
	 * @return 
	 * True if successes, False if fail. 
	 */
	private Boolean loadMatchDataFromNetwork(){
		
		LogHelper.d(TAG, "loadMatchDataFromNetwork()");
		
		//Load data from network
		InputStream matchesStream = DataOperateHelper.loadFileFromFTPNetwork(DATA_MATCHES_FILE);
		if(matchesStream == null){
			LogHelper.w(TAG, "loadFileFromNetwork failed");
			return false;
		}
		
		// Convert the stream to string
		LogHelper.d(TAG, "Download success!");
		String matchesString = DataOperateHelper.covertStream2String(matchesStream, FILE_ENCODE_FORMAT);
		try {
			matchesStream.close(); //close
		} catch (IOException e) {
			LogHelper.e(TAG, e);
			return false;
		}
		if(matchesString == null){
			Log.w(TAG, "covertStream2String failed");
			return false;
		}
		
		// Parse the data
		if(!parseMatchesData(matchesString)){
			Log.w(TAG, "parseMatchData failed");
			return false;
		}

		//Save asset file to local file
		if(!DataOperateHelper.saveData2LocalFile(context, matchesString, DATA_MATCHES_FILE)){
			Log.w(TAG, "parseMatchData failed");
			return false;
		}
		
		LogHelper.d(TAG, "loadMatchDataFromNetwork()");
		return true;
	}
	

	/*
	 * Parse the Data from json format to @MatchesModel object into matchesList
	 * 
	 * @return
	 * True if successes, False if fail. 
	 * 
	 */
	private Boolean parseMatchesData(String matchesString){
		
		LogHelper.d(TAG, "parseMatchData()");
		
		//Check the string
		if(matchesString == null || matchesString.length() == 0){
			LogHelper.w(TAG, "The matchesString is empty");
			return false;
		}
		
		// Convert the json string to match object
		JSONTokener jsonParser  = new JSONTokener(matchesString);
		try {
			
			JSONObject rootObj  = (JSONObject) jsonParser.nextValue();
			
			//parse data version
			dataMatchesVersion = rootObj.getDouble(JSON_MATCHES_DATA_VERSION);
	    	LogHelper.d(TAG, "The match data version is:" + String.valueOf(dataMatchesVersion));
	    	
	    	//parse match data
			JSONArray matchesArray = rootObj.getJSONArray(JSON_MATCHES_LIST);
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
			LogHelper.w(TAG, "Parse the matches.json failed");
			LogHelper.e(TAG, e);
			return false;
		} catch (ClassCastException ex){ //If string format error, will throw ClassCastException
			LogHelper.w(TAG, "Parse the matches.json failed");
			LogHelper.e(TAG, ex);
			return null;
		}
		
		LogHelper.d(TAG, "parseMatchData successed!" + String.valueOf(matchesList.size()));
		return true;
	}
}
