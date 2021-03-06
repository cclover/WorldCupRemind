package com.cc.worldcupremind.logic;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cc.worldcupremind.common.DataOperateHelper;
import com.cc.worldcupremind.common.ImageCreator;
import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.common.ResourceHelper;
import com.cc.worldcupremind.model.GroupStatistics;
import com.cc.worldcupremind.model.MatchStage;
import com.cc.worldcupremind.model.MatchesModel;
import com.cc.worldcupremind.model.PlayerStatistics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseArray;


public class MatchDataController extends BroadcastReceiver implements MatchDataListener{
	
	private static final String TAG = "MatchDataController";
	private static final String REMIND_STATUS = "remindstatus";
	private static final String PRE_APP_VERSION = "appversiom";
	private static final String PRE_VIDEO_ALERT = "videoalert";
	private static final String PRE_UPDATE_SERVER = "updateserver";
	private static final String PRE_IMAGE_VERSION = "imageversion";
	private static final String PRE_FILE_NAME = "data.xml";
	private static final int THREAD_POOL_SIZE = 2;
	private static MatchDataController instance = new MatchDataController();
	private Boolean isDataInitDone;
	private Boolean isInUpdateProcess;
	private MatchDataHelper dataHelper;	
	private ResourceHelper resourceHelper;
	private MatchDataListener matchListener;
	private Context context;
	private Object lockObj;
	private ExecutorService threadPool;
	private ExecutorService remindThread;
	private ExecutorService updateThread;
	private ExecutorService initThread;
	private String updateInfo;

	public static final int UPDATE_SERVER_ID_1 = 0;
	public static final int UPDATE_SERVER_ID_2 = 1;
	
	/**
	 * Get the @MatchDataController object
	 * 
	 * @return the @MatchDataController single instance
	 */
	public static MatchDataController getInstance(){
		return instance;
	}
	
	/**
	 * Construct
	 */
	private MatchDataController(){
		isDataInitDone = false;
		isInUpdateProcess = false;
		dataHelper = null;
		resourceHelper = null;
		context = null;
		matchListener = null;
		lockObj = new Object();
		remindThread = Executors.newSingleThreadExecutor();
		updateThread = Executors.newSingleThreadExecutor();
		initThread = Executors.newSingleThreadExecutor();
		threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	}
	
	/**
	 * Get team info url
	 */
	public String getTeamURL(String teamCode){
		return dataHelper.getTeamURL(teamCode);
	}
	
	/**
	 * @return the updateInfo
	 */
	public String getUpdateInfo() {
		return updateInfo;
	}
	
	/**
	 * 
	 * @return true if data init
	 */
	public Boolean isDataInit(){
		return isDataInitDone;
	}
	
	/**
	 * set @MatchDataListener listener
	 * 
	 * @param listener
	 * The @MatchDataListener object
	 */
	public void setListener(MatchDataListener listener){
		if(listener != null){
			matchListener = listener;
			LogHelper.d(TAG, "Add listener " + listener.toString());
		}
	}
	
	/**
	 * remove the @MatchDataListener listener
	 * 
	 * @param listener
	 * The @MatchDataListener object
	 */
	public void removeListener(MatchDataListener listener){
		if(listener != null){
			LogHelper.d(TAG, "Remove the listener " + listener.toString());
			matchListener = null;
		}
	}
	
	
	public double getDataVersion(){
		return dataHelper.getDataMatchesVersion();
	}
	
	public String getNewsURL(){
		return dataHelper.getNewsURL();
	}
	

	public MatchStage getMatchStage() {
		return dataHelper.getMatchStage();
	}
	
	public ResourceHelper gerResourceHelper(){
		return resourceHelper;
	}
	
	/**
	 * Get the Matches info @MatchesModel object
	 * 
	 * @return @SparseArray<MatchesModel>
	 */
	public SparseArray<MatchesModel> getMatchesData() {
		return dataHelper.getMatchesList();
	}
	
	/**
	 * Get the GroupStatics info @GroupStatistics object
	 * 
	 * @return @ArrayList<GroupStatistics>
	 */
	public ArrayList<GroupStatistics> getGroupStaticsData() {
	    return dataHelper.getGroupStatisticsList();
	}
	
	
	/**
	 * Get the Goal Statics info @PlayerStatistics object
	 * 
	 * @return @ArrayList<PlayerStatistics>
	 */
	public ArrayList<PlayerStatistics> getGoalStaticsData(){
		return dataHelper.getGoalStatisticsList();
	}
	
	/**
	 * Get the Assist Statics info @PlayerStatistics object
	 * 
	 * @return @ArrayList<PlayerStatistics>
	 */
	public ArrayList<PlayerStatistics> getAssistStaticsData(){
		return dataHelper.getAssistStatisticsList();
	}

	/**
	 * Init the necessary data, receive result from @MatchDataListener
	 * Must invoke this must at first.
	 *
	 */
	public void InitData(Context appContext, Boolean needCallbak){
		
		LogHelper.i(TAG, "Init Data");
		
		if(isDataInitDone){
			LogHelper.d(TAG, "Data had init done!");
			if(needCallbak){
				
				initThread.execute(new Runnable() {

					@Override
					public void run() {
						onInitDone(true);
					}
				});
			}
			return;
		}
		
		synchronized (lockObj) {
			if(context == null){
				context = appContext.getApplicationContext();
				dataHelper = new MatchDataHelper(context);
				resourceHelper = new ResourceHelper(context);
				IntentFilter filter = new IntentFilter();
				filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
				filter.addAction(Intent.ACTION_LOCALE_CHANGED);
				context.registerReceiver(this, filter);
			}
		}

		initThread.execute(new Runnable() {
			
			@Override
			public void run() {
				
				if(isDataInitDone){
					LogHelper.d(TAG, "Data had init done!");
					return;
				}
				
				// Load the necessary data
				LogHelper.d(TAG, "Load matches data from file");
				if(!dataHelper.loadMatchesData() || !dataHelper.loadStatisticsData()){
					
					LogHelper.w(TAG, "Fail to init the data!");
					onInitDone(false);
					return;
				}
				
				// Load resource id
				LogHelper.d(TAG, "Load resource id");
				if(!resourceHelper.Init(dataHelper.getTeamsCount())){
					
					LogHelper.w(TAG, "Fail to init the resource!");
					onInitDone(false);
					return;
				}
				
				//Load the remind filed
				LogHelper.d(TAG, "Load remind data");
				if(dataHelper.loadRemindData()){
					LogHelper.i(TAG, "Set remind alarm");
					MatchRemindHelper.setAlarm(context, dataHelper.getRemindList(), dataHelper.getRemindCancelList());
				}
				
				//make image
				makeSecondStageImage();
				
				//Init done
				LogHelper.d(TAG, "Init Data Done!");
				isDataInitDone = true;
				onInitDone(true);
		
			}
		});
	}

	
	
	public Boolean InitDataSync(Context appContext){
		
		LogHelper.i(TAG, "Init Data Sync");
		if(isDataInitDone){
			LogHelper.d(TAG, "Data had init done!");
			return false;
		}
		
		// Load the necessary data
		LogHelper.d(TAG, "Load matches data from file");
		if(!dataHelper.loadMatchesData() || !dataHelper.loadStatisticsData()){
			
			LogHelper.w(TAG, "Fail to init the data!");
			return false;
		}
		
		// Load resource id
		LogHelper.d(TAG, "Load resource id");
		if(!resourceHelper.Init(dataHelper.getTeamsCount())){
			
			LogHelper.w(TAG, "Fail to init the resource!");
			return false;
		}
		
		//Load the remind filed
		LogHelper.d(TAG, "Load remind data");
		if(dataHelper.loadRemindData()){
			LogHelper.i(TAG, "Set remind alarm");
			MatchRemindHelper.setAlarm(context, dataHelper.getRemindList(), dataHelper.getRemindCancelList());
		}
		
		//make image
		makeSecondStageImage();
		
		//Init done
		LogHelper.d(TAG, "Init Data Done!");
		isDataInitDone = true;
		
		return true;
	}
	
	/**
	 * Update data from network, receive result from @MatchDataListener
	 * 
	 * @return true if can update.
	 * 
	 */
	public Boolean updateData(){
		
		LogHelper.i(TAG, "updateData");
		
		if(!isDataInitDone){
			LogHelper.w(TAG, "Please init data first");
			return false;
		}
		
		if(isInUpdateProcess){
			LogHelper.d(TAG, "isInUpdateProcess");
			return true;
		}
		isInUpdateProcess = true;
		updateThread.execute(new Runnable() {
			
			@Override
			public void run() {

				ArrayList<String> updateList = dataHelper.checkNewVersion();
				if(updateList == null){
					LogHelper.w(TAG, "Check version failed");
					onUpdateDone(UPDATE_STATE_CHECK_ERROR, null);
				}else if(updateList.size() == 0){
					LogHelper.d(TAG, "Current date is latest version");
					onUpdateDone(UPDATE_STATE_CHECK_NONE, null);
				}else{
	
					//Check
					String url = updateList.get(0);
					if(url.contains("http") || url.contains("https")){
						LogHelper.i(TAG, "Have new APK version!!!!");
						updateInfo = updateList.get(1);
						onUpdateDone(UPDATE_STATE_CHECK_NEW_APK, url);
						isInUpdateProcess = false;
						return;
					}
						
					LogHelper.i(TAG, "Have new DATA version!!!!");
					onUpdateDone(UPDATE_STATE_UPDATE_START, null);
					if(!dataHelper.updateAllDataFiles(updateList)){
						LogHelper.w(TAG, "Update Data failed!");
						onUpdateDone(UPDATE_STATE_UPDATE_ERROR, null);
					}else{
						LogHelper.d(TAG, "Update Data success!");
						onUpdateDone(UPDATE_STATE_UPDATE_DONE, null);
						//update image
						makeSecondStageImage();
					}
				}
				isInUpdateProcess = false;
			}
		});
		
		return true;
	}

	
	/**
	 * Set the alarm for the match
	 * 
	 * @param matchesList
	 * Match NO list
	 * 
	 * @return true if set success.
	 * 
	 */
	public Boolean setMatchRemind(ArrayList<Integer> matchesList){
		
		LogHelper.i(TAG, "setMatchRemind");
		
		if(!isDataInitDone){
			LogHelper.w(TAG, "Please init data first");
			return false;
		}
		
		final ArrayList<Integer> newList = matchesList;
		remindThread.execute(new Runnable() {
			
			@Override
			public void run() {
				
				if(!dataHelper.setRemindData(newList)){
					LogHelper.w(TAG, "Fail to set the remind data");
					onSetRemindDone(false);
					return;
				}
				
				//Set alarm
				try{
					MatchRemindHelper.setAlarm(context, dataHelper.getRemindList(), dataHelper.getRemindCancelList());
					onSetRemindDone(true);
				}catch (Exception e) {
					LogHelper.e(TAG, e);
					onSetRemindDone(false);
				}
			}
		});
	
		return true;
	}

	
	/**
	 * Delete the alarm 
	 * 
	 * @param deleteList
	 * Match NO list
	 * 
	 * @return true if delete success.
	 */
	public Boolean deleteMatchRemind(ArrayList<Integer> deleteList){
		
		LogHelper.i(TAG, "deleteMatchRemind");
		
		if(!isDataInitDone){
			LogHelper.w(TAG, "Please init data first");
			return false;
		}
		
		final ArrayList<Integer> delList = deleteList;
		remindThread.execute(new Runnable() {
			
			@Override
			public void run() {
				
				if(!dataHelper.deleteRemindData(delList)){
					LogHelper.w(TAG, "Fail to delete the remind data");
					onSetRemindDone(false);
					return;
				}
				onSetRemindDone(true);
			}
		});
	
		return true;
	}
	
	
	/**
	 * Delete the local files and reload from asset
	 * @return
	 */
	public Boolean resetData(){
		
		LogHelper.i(TAG, "resetData");
		threadPool.execute(new Runnable() {
			
			@Override
			public void run() {
				if(!dataHelper.removeData()){
					Log.d(TAG, "Fail to reset data");
					onResetDone(false);
					return;
				}
				setVideoAlert(true);
				setImageVersion(0);
				isDataInitDone = false;
				InitDataSync(context);
				onResetDone(true);
			}
		});
		return true;
	}
	
	/**
	 * Get the Team's National name
	 * 
	 * @param teamCode
	 * Team code
	 * 
	 * @return
	 * The Team's national name
	 * 
	 */
	public String getTeamNationalName(String teamCode){
		
		try {
			return resourceHelper.getStringRescourse(teamCode);
		} catch (Resources.NotFoundException ex){
//			Log.w(TAG, "Not find the team name:" + teamCode);
			return teamCode;
		}
	}
	
	
	/**
	 * Get the Team's National Flag
	 * 
	 * @param resourceID
	 * Resource ID
	 * 
	 * @return
	 * The Team's national flag @Drawable object
	 * 
	 */
	public Drawable getTeamNationalFlag(String teamCode){

		try {
			return resourceHelper.getDrawableRescourse(teamCode);
		} catch (Resources.NotFoundException ex){
//			Log.w(TAG, "Not find the team flag:" + teamCode);
			return null;
		}
	}
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)){
			threadPool.execute(new Runnable() {
				
				@Override
				public void run() {
					onTimezoneChanged();
				}
			});

		}else if(intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)){
			onLocalChanged();
		}
	}
	
	
	/**
	 * 
	 * @param status
	 * set true means remind enable
	 */
	public Boolean setRemindEnabl(Boolean status){
		SharedPreferences share = context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE);   
		if(share != null){
			SharedPreferences.Editor edit = share.edit();  
			edit.putBoolean(REMIND_STATUS, status);
			edit.commit();
			return true;
		}
		return false;
	}
	
	
	/**
	 * 
	 * @return true if remind enable, otherwise return false;
	 */
	public Boolean isRemindEnable(){
		SharedPreferences share =  context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE);
		if(share != null){
			return share.getBoolean(REMIND_STATUS, true);
		}
		return true;
	}
	
	private Boolean isNewVersionLaunch(){
		SharedPreferences share =  context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE);
		if(share != null){
			double version = share.getFloat(PRE_APP_VERSION, 1.0f);
			PackageInfo info;
			try {
				info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			} catch (NameNotFoundException e) {
				LogHelper.e(TAG, e);
				return false;
			}  
			float appVersion = Float.parseFloat(info.versionName);
			if(appVersion > version){
				//new app first launch
				LogHelper.d(TAG, "New version app launch:" + info.versionName);
				SharedPreferences.Editor edit = share.edit();  
				edit.putFloat(PRE_APP_VERSION, appVersion);
				edit.commit();
				return true;
			}
		}
		return false;
	}
	
	
	public Boolean needAlertWhenPlayVideo(){
		SharedPreferences share =  context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE);
		if(share != null){
			return share.getBoolean(PRE_VIDEO_ALERT, true);
		}
		return true;
	}
	
	public Boolean setVideoAlert(Boolean enable){
		SharedPreferences share = context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE);   
		if(share != null){
			SharedPreferences.Editor edit = share.edit();  
			edit.putBoolean(PRE_VIDEO_ALERT, enable);
			edit.commit();
			return true;
		}
		return false;
	}
	
	
	public int getUpdateServerID(){
		SharedPreferences share =  context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE);
		if(share != null){
			return share.getInt(PRE_UPDATE_SERVER, UPDATE_SERVER_ID_1);
		}
		return UPDATE_SERVER_ID_1;
	}
	
	public Boolean setUpdateServerID(int serverID){
		
		LogHelper.d(TAG, "Set update server id:" + serverID);
		SharedPreferences share = context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE);   
		if(share != null){
			SharedPreferences.Editor edit = share.edit();  
			edit.putInt(PRE_UPDATE_SERVER, serverID);
			edit.commit();
			return true;
		}
		return false;
	}
	
	
	private float getImageVersion(){
		SharedPreferences share =  context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE);
		if(share != null){
			return share.getFloat(PRE_IMAGE_VERSION, -1);
		}
		return -1;
	}
	
	private void setImageVersion(float version){
		
		LogHelper.d(TAG, "Set image version:" + version);
		SharedPreferences share = context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE);   
		if(share != null){
			SharedPreferences.Editor edit = share.edit();  
			edit.putFloat(PRE_IMAGE_VERSION, version);
			edit.commit();
		}
	}
	
	
	public void makeSecondStageImage(){
		
		LogHelper.d(TAG, "makeSecondStageImage");
		threadPool.execute(new Runnable() {
			
			@Override
			public void run() {
				double version = getImageVersion();
				double dataVesion = dataHelper.getDataMatchesVersion();
				LogHelper.d(TAG, String.format("Image version %f, data version %f ",version, dataVesion));
				if(version >= dataVesion && DataOperateHelper.isLocalFileExist(context, ImageCreator.DATA_SECOND_STAGE_IMAGE)){
					LogHelper.d(TAG, "No need to update the secondstage image");
					return;
				}
				LogHelper.d(TAG, "Create the secondstage image");
				ImageCreator creator = new ImageCreator(context);
				Intent intent = new Intent(ImageCreator.ACTION_CRATEA_IAMGE_DONE);
				if(creator.createSecondStageImage()){
					setImageVersion((float)dataVesion);
					intent.putExtra(ImageCreator.KEY_CRATEA_IAMGE_DONE, true);
				}else{
					intent.putExtra(ImageCreator.KEY_CRATEA_IAMGE_DONE, false);
				}
				context.sendBroadcast(intent);
			}
		});
	}
	
	
	@Override
	public void onInitDone(Boolean isSuccess) {

		if(matchListener != null ) {
			matchListener.onInitDone(isSuccess);
		}
	}

	@Override
	public void onUpdateDone(int status, String appURL) {

		if(matchListener != null){
			matchListener.onUpdateDone(status, appURL);
		}
	}

	@Override
	public void onSetRemindDone(Boolean isSuccess) {

		if(matchListener != null){
			matchListener.onSetRemindDone(isSuccess);
		}
	}

	@Override
	public void onTimezoneChanged() {
		
		LogHelper.d(TAG, "onTimezoneChanged");
		DataOperateHelper.deleteLoaclFile(context, ImageCreator.DATA_SECOND_STAGE_IMAGE);
		makeSecondStageImage();
		if(matchListener != null){
			matchListener.onTimezoneChanged();
		}
	}

	@Override
	public void onLocalChanged() {

		if(matchListener != null){
			matchListener.onLocalChanged();
		}
	}

	@Override
	public void onResetDone(Boolean issBoolean) {
		
		if(matchListener != null){
			matchListener.onResetDone(issBoolean);
		}
	}

}
