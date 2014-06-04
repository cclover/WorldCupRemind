package com.cc.worldcupremind.logic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.cc.worldcupremind.common.DataOperateHelper;
import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.model.GroupStatistics;
import com.cc.worldcupremind.model.MatchDate;
import com.cc.worldcupremind.model.MatchStage;
import com.cc.worldcupremind.model.MatchStatus;
import com.cc.worldcupremind.model.MatchesModel;
import com.cc.worldcupremind.model.PlayerStatistics;

/**
 * This class will help to load all matches data from data file.
 * 
 * We Save the match data in *.json files. When the APP launch at first time, will copy the data files
 * from ASSET to PRIVATE FILES folder. Can update the data from FTP server.
 */
class MatchDataHelper {
	
	private static final String TAG = "MatchDataHelper";
	
	/** Data files name */
	private static final String DATA_VERSION_FILE = "version.json";
	private static final String DATA_MATCHES_FILE = "matches.json";
	private static final String DATA_REMIND_FILE = "remind.json";
	private static final String DATA_STATISTICS_FILE = "statistics.json";
	private static final String FILE_ENCODE_FORMAT = "UTF-8";
	
	/** matches.json format */
	private static final String JSON_MATCHES_DATA_VERSION = "Version";	/* Double */
	private static final String JSON_TEAMS_COUNT = "TeamsCount";		/* Int */
	private static final String JSON_MATCHES_COUNT = "MatchesCount";	/* Int */
	private static final String JSON_MATCHES_LIST = "Matches";			/* Array */
	private static final String JSON_MATCHES_FILED_NO = "NO";			/* Int */
	private static final String JSON_MATCHES_FILED_STAGE = "Stage";		/* Enum */
	private static final String JSON_MATCHES_FILED_GROUP = "Group";		/* String */
	private static final String JSON_MATCHES_FILED_TIME= "Time";		/* Date */
	private static final String JSON_MATCHES_FILED_TEAM_1 = "Team1";	/* String */
	private static final String JSON_MATCHES_FILED_TEAM_2 = "Team2";	/* String */
	private static final String JSON_MATCHES_FILED_STATUS= "Status";	/* Eunm*/
	private static final String JSON_MATCHES_FILED_SCORE_1= "Score1";	/* Int */
	private static final String JSON_MATCHES_FILED_SCORE_2= "Score2";	/* Int */
	
	/** remind.json format */
	private static final String JSON_REMIND_LIST = "Remind";			/* Array */
	private static final String JSON_REMIND_MATCH_NO = "no";			/* Int */
	
	/** statistics.json format */
	private static final String JSON_STATISTICS_DATA_VERSION = "version";
	private static final String JSON_STATISTICS_ARRAY_GROUP = "groups";
	private static final String JSON_STATISTICS_FILED_TEAM = "team";
	private static final String JSON_STATISTICS_FILED_GROUP = "group";
	private static final String JSON_STATISTICS_FILED_WIN = "w";
	private static final String JSON_STATISTICS_FILED_DRAW = "d";
	private static final String JSON_STATISTICS_FILED_LOSE = "l";
	private static final String JSON_STATISTICS_FILED_GF = "gf";
	private static final String JSON_STATISTICS_FILED_GA = "ga";
	private static final String JSON_STATISTICS_FILED_POINT = "pts";
	private static final String JSON_STATISTICS_FILED_POS = "pos";
	private static final String JSON_STATISTICS_ARRAY_GOAL = "goal";
	private static final String JSON_STATISTICS_ARRAY_ASS = "assist";
	private static final String JSON_STATISTICS_PLAYER_NAME= "name";
	private static final String JSON_STATISTICS_PLAYER_TEAM= "team";
	private static final String JSON_STATISTICS_PLAYER_COUNT= "count";
	
	/** version.json format */
	private static final String JSON_VERSION_MATCHES = "matchesVer";		/* Double */
	private static final String JSON_VERSION_STATISTICS = "statisticsVer";	/* Double */
	
	
	/** MatchesModel list */
	private SparseArray<MatchesModel> matchesList;
	
	/** Remind list */
	private SparseArray<MatchesModel> remindList;
	
	/** Remind list need be cancel*/
	private ArrayList<Integer> remindCancelList;
	
	private ArrayList<GroupStatistics> groupStatisticsList;
	
	private ArrayList<PlayerStatistics> goalStatisticsList;
	
	private ArrayList<PlayerStatistics> assistStatisticsList;
	
	/** matche.json files version*/
	private double dataMatchesVersion;
	
	/** statistics.json file version*/
	private double dataStatisticsVersion;
	
	/** Applicaiont Context */
	private Context context;
	
	/** Matches Count*/
	private int matchesCount;
	
	/** Teams Count */
	private int teamsCount;
	
	/** Return value of updateAllDataFiles*/
	public enum UPDATE_RET{
		
		RET_CHECK_UPDATE_ERROR,
		RET_NO_NEED_UPDATE,
		RET_UPDATE_ERROR,
		RET_OK,
	}
	
	/**
	 * Construct
	 */
	public MatchDataHelper(Context context){
		
		this.dataMatchesVersion = 0;
		this.matchesList = new SparseArray<MatchesModel>();
		this.remindList = new SparseArray<MatchesModel>();
		this.remindCancelList = new ArrayList<Integer>();
		this.groupStatisticsList = new ArrayList<GroupStatistics>();
		this.goalStatisticsList = new ArrayList<PlayerStatistics>();
		this.assistStatisticsList = new ArrayList<PlayerStatistics>();
		this.context = context;
		matchesCount = 0;
		teamsCount = 0;
	}
	
	/**
	 * @return the matchesList
	 */
	public SparseArray<MatchesModel> getMatchesList() {
		return matchesList;
	}

	/**
	 * @return the remindList
	 */
	public SparseArray<MatchesModel> getRemindList() {
		return remindList;
	}

	/**
	 * @return the remindCancelList
	 */
	public ArrayList<Integer> getRemindCancelList() {
		return remindCancelList;
	}

	/**
	 * @return the matchesCount
	 */
	public int getMatchesCount() {
		return matchesCount;
	}

	/**
	 * @return the teamsCount
	 */
	public int getTeamsCount() {
		return teamsCount;
	}

	
	/**
	 * Get the matches data list
	 * 
	 * @return
	 * True if successes, False if fail. 
	 */
	public Boolean loadMatchesData(){
		
		LogHelper.d(TAG, "loadMatchesData()");
		matchesList.clear();
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
	

	/**
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
	
	
	/**
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
		} catch (Exception ex) {
			LogHelper.w(TAG, "Parse the remind.json failed");
			LogHelper.e(TAG, ex);
			return false;
		}
				
		LogHelper.d(TAG, "Return the remindList size:" + String.valueOf(remindList.size()));
		return true;
	}
	
	
	/**
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
	
	
	/**
	 * 
	 * @return
	 */
	public Boolean loadStatisticsData(){
		
		LogHelper.d(TAG, "loadStatisticsData()");
		groupStatisticsList.clear();
		goalStatisticsList.clear();
		assistStatisticsList.clear();
		Boolean ret = false;
		if(DataOperateHelper.isLocalFileExist(context, DATA_STATISTICS_FILE)){
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
	
	/**
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
			LogHelper.w(TAG, "create remind.json object failed");
			LogHelper.e(TAG, e);
			return false;
		} catch (Exception ex){
			LogHelper.w(TAG, "create remind.json object failed");
			LogHelper.e(TAG, ex);
			return null;
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
	
	
	/**
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
			double newMatchesVer = jsonObject.getDouble(JSON_VERSION_MATCHES);
			double newStatisticsVer = jsonObject.getDouble(JSON_VERSION_STATISTICS);
			if(newMatchesVer > dataMatchesVersion){
				updateFileList.add(DATA_MATCHES_FILE);
			}
			//TODO: same operate for other file
			return updateFileList;
		} catch (JSONException e1) {
			LogHelper.w(TAG, "Parse the version.json failed");
			LogHelper.e(TAG, e1);
		}catch (ClassCastException ex){ //If string format error, will throw ClassCastException
			LogHelper.w(TAG, "Parse the version.json failed");
			LogHelper.e(TAG, ex);
		} catch (Exception ex){
			LogHelper.w(TAG, "Parse the version.json failed");
			LogHelper.e(TAG, ex);
		}
		return null;
	}
	

	/**
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
	
	/**
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
	
	/**
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
	

	/**
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
		
		double tmpVersion = 0;
		
		// Convert the json string to match object
		JSONTokener jsonParser  = new JSONTokener(matchesString);
		try {
			
			JSONObject rootObj  = (JSONObject) jsonParser.nextValue();
			
			//parse data version
			tmpVersion = rootObj.getDouble(JSON_MATCHES_DATA_VERSION);
			matchesCount = rootObj.getInt(JSON_MATCHES_COUNT);
			teamsCount = rootObj.getInt(JSON_TEAMS_COUNT);
	    	LogHelper.d(TAG, "The match data version is:" + String.valueOf(dataMatchesVersion));
	    	
	    	//parse match data
			JSONArray matchesArray = rootObj.getJSONArray(JSON_MATCHES_LIST);
			for(int i=0; i<matchesArray.length(); i++){  
				JSONObject matchObj = matchesArray.getJSONObject(i);
				int matchNo = matchObj.getInt(JSON_MATCHES_FILED_NO);
				MatchStage stage = MatchStage.valueOf(matchObj.getInt(JSON_MATCHES_FILED_STAGE));
				String group = matchObj.getString(JSON_MATCHES_FILED_GROUP);
				MatchDate time = new MatchDate(context, matchObj.getString(JSON_MATCHES_FILED_TIME));
				String team1 = matchObj.getString(JSON_MATCHES_FILED_TEAM_1);
				String team2 = matchObj.getString(JSON_MATCHES_FILED_TEAM_2);
				MatchStatus status = MatchStatus.valueOf(matchObj.getInt(JSON_MATCHES_FILED_STATUS));
				int team1Score = matchObj.getInt(JSON_MATCHES_FILED_SCORE_1);
				int team2Score = matchObj.getInt(JSON_MATCHES_FILED_SCORE_2);
				MatchesModel matchItem = new MatchesModel(matchNo, stage, group, time,  
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
		} catch (Exception ex){
			LogHelper.w(TAG, "Parse the matches.json failed");
			LogHelper.e(TAG, ex);
			return null;
		}
		
		LogHelper.d(TAG, "parseMatchData successed!" + String.valueOf(matchesList.size()));
		dataMatchesVersion = tmpVersion; //if parse failed. The version will not change.
		return true;
	}
}
