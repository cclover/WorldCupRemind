package com.cc.worldcupremind.logic;

import java.util.ArrayList;

import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.common.ResourceHelper;
import com.cc.worldcupremind.logic.MatchDataHelper.UPDATE_RET;
import com.cc.worldcupremind.model.GroupStatistics;
import com.cc.worldcupremind.model.MatchesModel;
import com.cc.worldcupremind.model.PlayerStatistics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;


public class MatchDataController extends BroadcastReceiver implements MatchDataListener{
	
	private static final String TAG = "MatchDataController";
	private static MatchDataController instance = new MatchDataController();
	private Boolean isDataInitDone;
	private MatchDataHelper dataHelper;	
	private ResourceHelper resourceHelper;
	private ArrayList<MatchDataListener> listenerList;
	private Context context;
	private Object lockObj;
	
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
		dataHelper = null;
		resourceHelper = null;
		context = null;
		listenerList = new ArrayList<MatchDataListener>();
		lockObj = new Object();
	}
	
	/**
	 * set @MatchDataListener listener
	 * 
	 * @param listener
	 * The @MatchDataListener object
	 */
	public void setListener(MatchDataListener listener){
		if(listener != null && !listenerList.contains(listener)){
			listenerList.add(listener);
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
		if(listener != null && listenerList.contains(listener)){
			listenerList.remove(listener);
			LogHelper.d(TAG, "Remove the listener " + listener.toString());
		}
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
	 * Get the GroupStatics info @MatchesModel object
	 * 
	 * @return ArrayList<GroupStatistics>
	 */
	public ArrayList<GroupStatistics> getGroupStaticsData() {
	    return dataHelper.getGroupStatisticsList();
	}

	/**
	 * Init the necessary data, receive result from @MatchDataListener
	 * Must invoke this must at first.
	 *
	 */
	public void InitData(Context appContext){
		
		LogHelper.d(TAG, "Init Data");
		
		if(isDataInitDone){
			LogHelper.d(TAG, "Data had init done!");
			onInitDone(true);
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

		new Thread(new Runnable() {
			
			@Override
			public synchronized void run() {
				
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
					LogHelper.d(TAG, "Set remind alarm");
					MatchRemindHelper.setAlarm(context, dataHelper.getRemindList(), dataHelper.getRemindCancelList());
				}
				
				//Init done
				LogHelper.d(TAG, "Init Data Done!");
				isDataInitDone = true;
				onInitDone(true);
			}
		}).start();
	}

	
	/**
	 * Update data from network, receive result from @MatchDataListener
	 * 
	 * @return true if can update.
	 * 
	 */
	public Boolean updateData(){
		
		LogHelper.d(TAG, "updateData");
		
		if(!isDataInitDone){
			LogHelper.w(TAG, "Please init data first");
			return false;
		}
		
		new Thread(new Runnable() {
			
			@Override
			public synchronized void run() {

				// Check version
				UPDATE_RET ret = dataHelper.updateAllDataFiles();
				if(ret ==  UPDATE_RET.RET_CHECK_UPDATE_ERROR){
					LogHelper.w(TAG, "Check version failed");
					onUpdateDone(false,false);
				} else if (ret == UPDATE_RET.RET_NO_NEED_UPDATE){
					LogHelper.d(TAG, "Current date is latest version");
					onUpdateDone(false,true);
				} else if(ret == UPDATE_RET.RET_UPDATE_ERROR){
					LogHelper.d(TAG, String.format("updateData data failed"));
					onUpdateDone(true, false);
				} else if(ret == UPDATE_RET.RET_OK){
					LogHelper.d(TAG, String.format("updateData data success"));
					onUpdateDone(true, true);
				}
			}
		}).start();
		
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
		
		LogHelper.d(TAG, "setMatchRemind");
		
		if(!isDataInitDone){
			LogHelper.w(TAG, "Please init data first");
			return false;
		}
		
		final ArrayList<Integer> newList = matchesList;
		new Thread(new Runnable() {
			
			@Override
			public synchronized void run() {
				
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
		}).start();
	
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
			onTimezoneChanged();
		}else if(intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)){
			onLocalChanged();
		}
	}
	
	
	@Override
	public void onInitDone(Boolean isSuccess) {

		if(listenerList != null && listenerList.size() > 0){
			for (MatchDataListener listener : listenerList) {
				listener.onInitDone(isSuccess);
			}
		}
	}

	@Override
	public void onUpdateDone(Boolean haveNewVersion, Boolean isSuccess) {

		if(listenerList != null && listenerList.size() > 0){
			for (MatchDataListener listener : listenerList) {
				listener.onUpdateDone(haveNewVersion, isSuccess);
			}
		}
	}

	@Override
	public void onSetRemindDone(Boolean isSuccess) {

		if(listenerList != null && listenerList.size() > 0){
			for (MatchDataListener listener : listenerList) {
				listener.onSetRemindDone(isSuccess);
			}
		}
	}

	@Override
	public void onTimezoneChanged() {
		
		if(listenerList != null && listenerList.size() > 0){
			for (MatchDataListener listener : listenerList) {
				listener.onTimezoneChanged();
			}
		}
		
	}

	@Override
	public void onLocalChanged() {

		for (MatchDataListener listener : listenerList) {
			listener.onLocalChanged();
		}
	}

}
