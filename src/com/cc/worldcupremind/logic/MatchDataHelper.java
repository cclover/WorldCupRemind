package com.cc.worldcupremind.logic;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.pm.PackageInfo;
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
import com.cc.worldcupremind.model.PlayerStatistics.STATISTICS_TYPE;

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
	private static final String DATA_SECOND_STAGE_PIC = "secondstage.png";
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
	private static final String JSON_MATCHES_FILED_EXT= "ext";			/* String */
	private static final String JSON_NEWS_URL = "newsURL";				/* String */
	private static final String JSON_MATCHES_STAGE = "stage";			/* Int */
	
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
	private static final String JSON_STATISTICS_PLAYER_POS= "pos";
	private static final String JSON_STATISTICS_PLAYER_ENG_NAME= "eng";
	private static final String JSON_STATISTICS_PLAYER_EXT= "ext";
	
	/** version.json format */
	private static final String JSON_VERSION_MATCHES = "matchesVer";		/* Double */
	private static final String JSON_VERSION_STATISTICS = "statisticsVer";	/* Double */
	private static final String JSON_VERSION_APP_VERSION = "appVersion";
	private static final String JSON_VERSION_APP_APK = "appURL";
	private static final String JSON_VERSION_APP_EXT = "ext";
	
	
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
	
	/** News URL */
	private String newsURL;
	
	/** Match stage */
	private MatchStage matchStage;
	
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
	 * @return the dataMatchesVersion
	 */
	public double getDataMatchesVersion() {
		return dataMatchesVersion;
	}
	
	/**
	 * @return the newsURL
	 */
	public String getNewsURL() {
		return newsURL;
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
	 * @return the groupStatisticsList
	 */
	public ArrayList<GroupStatistics> getGroupStatisticsList() {
		return groupStatisticsList;
	}

	/**
	 * @return the goalStatisticsList
	 */
	public ArrayList<PlayerStatistics> getGoalStatisticsList() {
		return goalStatisticsList;
	}

	/**
	 * @return the assistStatisticsList
	 */
	public ArrayList<PlayerStatistics> getAssistStatisticsList() {
		return assistStatisticsList;
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
			ret = loadDataFromLocal(DATA_MATCHES_FILE);
		} else {
			ret = loadDataFromAsset(DATA_MATCHES_FILE, true);
		}
		
		if(ret){
			LogHelper.d(TAG, "Load the matches data size:" + String.valueOf(matchesList.size()));
		} else {
			LogHelper.w(TAG, "Load the matches data failed");
		}
		return ret;
	}
	

	/**
	 * Get the matches statistics lists
	 * 
	 * @return
	 * True if successes, False if fail. 
	 */
	public Boolean loadStatisticsData(){
		
		LogHelper.d(TAG, "loadStatisticsData()");
		groupStatisticsList.clear();
		goalStatisticsList.clear();
		assistStatisticsList.clear();
		Boolean ret = false;
		if(DataOperateHelper.isLocalFileExist(context, DATA_STATISTICS_FILE)){
			ret = loadDataFromLocal(DATA_STATISTICS_FILE);
		} else {
			ret = loadDataFromAsset(DATA_STATISTICS_FILE, true);
		}
		
		if(ret){
			LogHelper.d(TAG, "Load the Statistics data success!");
		} else {
			LogHelper.w(TAG, "Load the Statistics data failed!");
		}
		return ret;
	}
	
	/**
	 * Check the data version
	 * 
	 * @return
	 * Return the need update file name list, return null when error
	 */
	public ArrayList<String> checkNewVersion(){
		
		LogHelper.d(TAG, "checkNewVersion()");
		ArrayList<String> updateFileList = new ArrayList<String>();
		
		//Get Download file stream
//		InputStream verStream = DataOperateHelper.loadFileFromFTPNetwork(DATA_VERSION_FILE);
		
		HttpURLConnection conn = DataOperateHelper.conectHTTPServer(DATA_VERSION_FILE);
		if(conn == null){
			LogHelper.w(TAG, "Fail to connect server");
			return null;
		}
		InputStream verStream = DataOperateHelper.loadFileFromHTTPNetwork(conn);
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
		}finally{
			DataOperateHelper.disconnectHTTPServer(conn);
		}
		
		if(ver == null){
			LogHelper.w(TAG, "covertStream2String filed");
			return null;
		}
		
		//Check the version
		LogHelper.d(TAG, "Network version is :" + ver);
		try {
			JSONObject jsonObject = new JSONObject(ver);
			double newMatchesVer = jsonObject.getDouble(JSON_VERSION_MATCHES);
			double newStatisticsVer = jsonObject.getDouble(JSON_VERSION_STATISTICS);
			double appVersion = jsonObject.getDouble(JSON_VERSION_APP_VERSION);
			
			//Check APP version
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);  
			if(appVersion > Double.parseDouble(info.versionName)){
				LogHelper.d(TAG, "App need be updated, version:" + appVersion);
				String appURL = jsonObject.getString(JSON_VERSION_APP_APK);
				updateFileList.add(appURL);
				return updateFileList;
			}
			
			//Check data version
			if(newMatchesVer > dataMatchesVersion){
				LogHelper.d(TAG, "Matches data need be updated, version:" + dataMatchesVersion);
				updateFileList.add(DATA_MATCHES_FILE);
			}
			if(newStatisticsVer > dataStatisticsVersion){
				LogHelper.d(TAG, "Statisstics data need be updated, version:" + dataStatisticsVersion);
				updateFileList.add(DATA_STATISTICS_FILE);
			}
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
	 * Update the all data files from network
	 * 
	 * @return
	 * return true if update success, otherwise false.
	 */
	public Boolean updateAllDataFiles(ArrayList<String> updateList){
		
		LogHelper.d(TAG, "updateAllData()");

		// Check update.
		LogHelper.d(TAG, "Check version file");
		if(updateList == null){
			LogHelper.w(TAG, "Fail to check the new version");
			return false;
		} else if(updateList.size() == 0){
			 LogHelper.d(TAG, "current is latest version");
			 return false;
		}
		 
		// Update each data
		LogHelper.d(TAG, "Start to update files");
		for(String updateFile : updateList){
			
			LogHelper.d(TAG, "update the file:" + updateFile);
			Boolean ret = false; 
			if(updateFile.equals(DATA_MATCHES_FILE)){
				ret = loadDataFromNetwork(DATA_MATCHES_FILE);
			} else if(updateFile.equals(DATA_STATISTICS_FILE)){
				ret = loadDataFromNetwork(DATA_STATISTICS_FILE);
			}

			if(!ret){
				LogHelper.w(TAG, "update failed!");
				return false;
			}
		}
		if(matchStage != MatchStage.STAGE_GROUP){
			LogHelper.d(TAG, "update the second stage pic");
			donwloadSecondStagePic();
		}else{
			LogHelper.d(TAG, "Current in GROUP stage, no need update the pic");
		}
		LogHelper.d(TAG, "all update success!");
		return true;
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
		if(!loadDataFromLocal(DATA_REMIND_FILE)){
			Log.w(TAG, "Load remind data failed");
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
	
	public Boolean deleteRemindData(ArrayList<Integer> deleteList){
		
		LogHelper.d(TAG, "deleteRemindData()");
		
		// renew the remind list
		if(deleteList == null){
			LogHelper.w(TAG, "The delete remind list is null");
			return false;
		}
		
		//Renew the remind data
		for(int matchNo : deleteList){
			remindList.remove(matchNo);
			MatchesModel model = matchesList.get(matchNo);
			model.setIsRemind(false);
			LogHelper.d(TAG, "Remove matchNo from remindList and modify matchesmodel flag" + String.valueOf(matchNo));
		}
		
		//create new remind matchNo list
		ArrayList<Integer> newList = new ArrayList<Integer>();
		for(int i = 0; i < remindList.size(); i++){
			newList.add(remindList.keyAt(i));
		}
		
		//Save new rmind list
		LogHelper.d(TAG, "Save the newList size" + String.valueOf(newList.size()));
		if(!saveRemindData(newList)){
			LogHelper.w(TAG, "Fail to save the remind data");
			return false;
		}
		
		return true;
	}
	
	public Boolean removeData(){
		
		LogHelper.d(TAG, "donwloadSecondStagePic()");
//		DataOperateHelper.deleteLoaclFile(context, DATA_REMIND_FILE);
		Boolean ret1 = DataOperateHelper.deleteLoaclFile(context, DATA_MATCHES_FILE);
		Boolean ret2 = DataOperateHelper.deleteLoaclFile(context, DATA_STATISTICS_FILE);
		Boolean ret3 = DataOperateHelper.deleteLoaclFile(context, DATA_SECOND_STAGE_PIC);
		return ret1 && ret2 && ret3;
	}
	
	private Boolean donwloadSecondStagePic(){
		
		LogHelper.d(TAG, "donwloadSecondStagePic()");	
		//InputStream picInStream = DataOperateHelper.loadFileFromFTPNetwork(DATA_SECOND_STAGE_PIC);
		
		HttpURLConnection conn = DataOperateHelper.conectHTTPServer(DATA_SECOND_STAGE_PIC);
		if(conn == null){
			LogHelper.w(TAG, "Fail to donwloadSecondStagePic");
			return false;
		}
		InputStream picInStream = DataOperateHelper.loadFileFromHTTPNetwork(conn);
		
		if(picInStream == null){
			LogHelper.w(TAG, "Fail to donwloadSecondStagePic");
			return false;
		}
		
		if(!DataOperateHelper.saveStream2LocalFile(context, picInStream, DATA_SECOND_STAGE_PIC)){
			LogHelper.w(TAG, "Save secondstage pic failed");
			return false;
		}
		
		DataOperateHelper.disconnectHTTPServer(conn);
		
		LogHelper.d(TAG, "Save secondstage pic");
		return true;
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
			DataOperateHelper.deleteLoaclFile(context, DATA_REMIND_FILE);
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
	 * Load the Data from asset file and copy to private folder
	 * 
 	 * @param fileName
	 * The data file want to be load
	 * 
	 * @return 
	 * True if successes, False if fail. 
	 */
	private Boolean loadDataFromAsset(String fileName, Boolean saveToLocal){
		
		LogHelper.d(TAG, "loadDataFromAsset():" + fileName);
		
		//Load data from asset
		InputStream dataStream = DataOperateHelper.loadFileFromAsset(context, fileName);
		if(dataStream == null){
			Log.w(TAG, "loadFileFromAsset failed:" + fileName);
			return false;
		}
		
		// Convert the stream to string
		String dataString = DataOperateHelper.covertStream2String(dataStream, FILE_ENCODE_FORMAT);
		try {
			dataStream.close(); //close stream
		} catch (IOException e) {
			LogHelper.e(TAG, e);
			return false;
		}
		
		if(dataString == null){
			Log.w(TAG, "covertStream2String failed:" + fileName);
			return false;
		}
		
		// Parse the data
		if(!parseData(fileName, dataString)){
			Log.w(TAG, "parseData failed:" + fileName);
			return false;
		}
		
		//Save asset file to local file
		if(saveToLocal){
			
			LogHelper.d(TAG, "Save file to local:" + fileName);
			if(!DataOperateHelper.saveData2LocalFile(context, dataString, fileName)){
				Log.w(TAG, "saveData2LocalFile failed:" + fileName);
				return false;
			}
		}
		
		LogHelper.d(TAG, "loadDataFromAsset Successed!:" + fileName);
		return true;
	}
	
	/**
	 * Load the data from private folder
	 * 
	 * @param fileName
	 * The data file want to be load
	 * 
	 * @return 
	 * True if successes, False if fail. 
	 */
	private Boolean loadDataFromLocal(String fileName){
		
		LogHelper.d(TAG, "loadDataFromLocal():" + fileName);
		
		//Load data from local file
		InputStream dataStream = DataOperateHelper.loadFileFromLocal(context, fileName);
		if(dataStream == null){
			Log.w(TAG, "loadFileFromLocal failed:" + fileName);
			return false;
		}
		
		// Convert the stream to string
		String dataString = DataOperateHelper.covertStream2String(dataStream, FILE_ENCODE_FORMAT);
		try {
			dataStream.close(); //close stream
		} catch (IOException e) {
			LogHelper.e(TAG, e);
			return false;
		}
		
		if(dataString == null){
			Log.w(TAG, "covertStream2String failed:" + fileName);
			return false;
		}
		
		// Parse the data
		if(!parseData(fileName, dataString)){
			Log.w(TAG, "parseData failed:" + fileName);
			return false;
		}
		
		LogHelper.d(TAG, "loadDataFromLocal Successed!:" + fileName);
		return true;
	}
	
	/**
	 * Load the data from network
	 * 
	 * @param fileName
	 * The data file want to be load
	 * 
	 * @return 
	 * True if successes, False if fail. 
	 */
	private Boolean loadDataFromNetwork(String fileName){
		
		LogHelper.d(TAG, "loadDataFromNetwork():" + fileName);
		
		//Load data from network
//		InputStream matchesStream = DataOperateHelper.loadFileFromFTPNetwork(fileName);
		
		HttpURLConnection conn = DataOperateHelper.conectHTTPServer(fileName);
		if(conn == null){
			LogHelper.w(TAG, "loadFileFromNetwork failed:" + fileName);
			return false;
		}
		InputStream matchesStream = DataOperateHelper.loadFileFromHTTPNetwork(conn);
		if(matchesStream == null){
			LogHelper.w(TAG, "loadFileFromNetwork failed:" + fileName);
			return false;
		}
		
		// Convert the stream to string
		LogHelper.d(TAG, "Download success!:" + fileName);
		String matchesString = DataOperateHelper.covertStream2String(matchesStream, FILE_ENCODE_FORMAT);
		try {
			matchesStream.close(); //close
		} catch (IOException e) {
			LogHelper.e(TAG, e);
			return false;
		}finally{
			DataOperateHelper.disconnectHTTPServer(conn);
		}
		if(matchesString == null){
			Log.w(TAG, "covertStream2String failed:" + fileName);
			return false;
		}
		
		// Parse the data
		if(!parseData(fileName, matchesString)){
			Log.w(TAG, "parseData failed:" + fileName);
			return false;
		}

		//Save network file to local file
		if(!DataOperateHelper.saveData2LocalFile(context, matchesString, fileName)){
			Log.w(TAG, "save data failed:" + fileName);
			return false;
		}
		
		LogHelper.d(TAG, "loadDataFromNetwork():" + fileName);
		return true;
	}
	

	
	/**
	 * Parse the data
	 * 
	 * @param fileName
	 * The file which need to be parsed
	 * 
	 * @param dataString
	 * The file content
	 * 
	 * @return
	 * True if successes, False if fail. 
	 * 
	 */
	private Boolean parseData(String fileName, String dataString){
		
		LogHelper.d(TAG, "parseData():" + fileName);
		
		// Check the string
		if(dataString == null || dataString.length() == 0){
			LogHelper.w(TAG, "The dataString is empty:" + fileName);
			return false;
		}
		
		// Invoke the parse
		if(fileName.equals(DATA_MATCHES_FILE)){
			return parseMatchesData(dataString);
		}else if(fileName.equals(DATA_STATISTICS_FILE)){
			return parseStatisticsData(dataString);
		}else if(fileName.equals(DATA_REMIND_FILE)){
			return parseRemindData(dataString);
		}
		return false;
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
		double tmpVersion = 0;
		matchesList.clear();
		
		// Convert the json string to match object
		JSONTokener jsonParser  = new JSONTokener(matchesString);
		try {
			
			JSONObject rootObj  = (JSONObject) jsonParser.nextValue();
			
			//parse data version
			tmpVersion = rootObj.getDouble(JSON_MATCHES_DATA_VERSION);
			matchesCount = rootObj.getInt(JSON_MATCHES_COUNT);
			teamsCount = rootObj.getInt(JSON_TEAMS_COUNT);
			newsURL = rootObj.optString(JSON_NEWS_URL);
			matchStage = MatchStage.valueOf(rootObj.getInt(JSON_MATCHES_STAGE));
	    	LogHelper.d(TAG, "The match data version is:" + String.valueOf(tmpVersion));
	    	
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
			return false;
		} catch (Exception ex){
			LogHelper.w(TAG, "Parse the matches.json failed");
			LogHelper.e(TAG, ex);
			return false;
		}
		
		LogHelper.d(TAG, "parseMatchData successed! matchdata size" + String.valueOf(matchesList.size()));
		dataMatchesVersion = tmpVersion; //if parse failed. The version will not change.
		return true;
	}

	
	/**
	 * Parse the Statistics Data from json format
	 * 
	 * @return
	 * True if successes, False if fail. 
	 * 
	 */
	private Boolean parseStatisticsData(String statisticsString){
		
		LogHelper.d(TAG, "parseStatisticsData()");
		double tmpVersion = 0;
		groupStatisticsList.clear();
		goalStatisticsList.clear();
		assistStatisticsList.clear();
		
		// Convert the json string to match object
		JSONTokener jsonParser  = new JSONTokener(statisticsString);
		try {
			
			JSONObject rootObj  = (JSONObject) jsonParser.nextValue();
			
			//parse data version
			tmpVersion = rootObj.getDouble(JSON_STATISTICS_DATA_VERSION);
	    	LogHelper.d(TAG, "The statistics data version is:" + String.valueOf(tmpVersion));
	    	
	    	//parse statistics group data
	    	LogHelper.d(TAG, "parse group statistics");
			JSONArray groupArray = rootObj.getJSONArray(JSON_STATISTICS_ARRAY_GROUP);
			for(int i = 0; i < groupArray.length(); i++){  
				JSONObject teamObj = groupArray.getJSONObject(i);
				String teamCode = teamObj.getString(JSON_STATISTICS_FILED_TEAM);
				String group = teamObj.getString(JSON_STATISTICS_FILED_GROUP);
				int win = teamObj.getInt(JSON_STATISTICS_FILED_WIN);
				int draw = teamObj.getInt(JSON_STATISTICS_FILED_DRAW);
				int lose = teamObj.getInt(JSON_STATISTICS_FILED_LOSE);
				int gf = teamObj.getInt(JSON_STATISTICS_FILED_GF);
				int ga = teamObj.getInt(JSON_STATISTICS_FILED_GA);
				int pts = teamObj.getInt(JSON_STATISTICS_FILED_POINT);
				int pos = teamObj.getInt(JSON_STATISTICS_FILED_POS);
				
				GroupStatistics team = new GroupStatistics(teamCode, group, win, draw, lose, gf, ga, pts, pos);
				groupStatisticsList.add(team);
			}
			
			//parse goal data
			JSONArray goalArray = rootObj.getJSONArray(JSON_STATISTICS_ARRAY_GOAL);
			for(int i = 0; i < goalArray.length(); i++){  
				JSONObject goalObj = goalArray.getJSONObject(i);
				String name = goalObj.getString(JSON_STATISTICS_PLAYER_NAME);
				String team = goalObj.getString(JSON_STATISTICS_PLAYER_TEAM);
				int count = goalObj.getInt(JSON_STATISTICS_PLAYER_COUNT);
				int pos = goalObj.getInt(JSON_STATISTICS_PLAYER_POS);
				String endName = goalObj.getString(JSON_STATISTICS_PLAYER_ENG_NAME);
				String ext = goalObj.getString(JSON_STATISTICS_PLAYER_EXT);
				PlayerStatistics player = new PlayerStatistics(pos, endName, name, team, count, STATISTICS_TYPE.STATISTICS_GOAL);
				player.setExt(ext);
				goalStatisticsList.add(player);
			}
			
			//parse assist data
			JSONArray assistArray = rootObj.getJSONArray(JSON_STATISTICS_ARRAY_ASS);
			for(int i = 0; i < assistArray.length(); i++){  
				JSONObject assistObj = assistArray.getJSONObject(i);
				String name = assistObj.getString(JSON_STATISTICS_PLAYER_NAME);
				String team = assistObj.getString(JSON_STATISTICS_PLAYER_TEAM);
				int count = assistObj.getInt(JSON_STATISTICS_PLAYER_COUNT);
				int pos = assistObj.getInt(JSON_STATISTICS_PLAYER_POS);
				String endName = assistObj.getString(JSON_STATISTICS_PLAYER_ENG_NAME);
				PlayerStatistics player = new PlayerStatistics(pos, endName, name, team, count, STATISTICS_TYPE.STATISTICS_ASSIST);
				assistStatisticsList.add(player);
			}
			
		} catch (JSONException e) {
			LogHelper.w(TAG, "Parse the statistics.json failed");
			LogHelper.e(TAG, e);
			return false;
		} catch (ClassCastException ex){ //If string format error, will throw ClassCastException
			LogHelper.w(TAG, "Parse the statistics.json failed");
			LogHelper.e(TAG, ex);
			return false;
		} catch (Exception ex){
			LogHelper.w(TAG, "Parse the statistics.json failed");
			LogHelper.e(TAG, ex);
			return false;
		}
		
		LogHelper.d(TAG, "parseStatisticsData successed!" + String.valueOf(matchesList.size()));
		LogHelper.d(TAG, "goalStatisticsList size:" + String.valueOf(goalStatisticsList.size()));
		LogHelper.d(TAG, "assistStatisticsList size:" + String.valueOf(assistStatisticsList.size()));
		dataStatisticsVersion = tmpVersion; //if parse failed. The version will not change.
		return true;
	}

	
	
	/**
	 * Parse the Remind Data from json format
	 * 
	 * @return
	 * True if successes, False if fail. 
	 * 
	 */
	private Boolean parseRemindData(String remindString){
		
		LogHelper.d(TAG, "parseRemindData()");
		
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
		
		LogHelper.d(TAG, "parseRemindData successed! remindList size:" + String.valueOf(remindList.size()));
		return true;
	}
}
