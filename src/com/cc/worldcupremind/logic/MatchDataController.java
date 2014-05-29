package com.cc.worldcupremind.logic;

import java.util.ArrayList;

import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.logic.MatchDataHelper.UPDATE_RET;
import com.cc.worldcupremind.model.MatchesModel;

import android.content.Context;
import android.util.SparseArray;


public class MatchDataController implements MatchDataListener{
	
	private static final String TAG = "MatchDataController";
	
	private static MatchDataController instance = new MatchDataController();
	private Boolean isDataInitDone;
	private MatchDataHelper dataHelper = null;	
	private ArrayList<MatchDataListener> linsterList = null;
	private Context context = null;
	private Object lockObj = null;;
	
	public static MatchDataController getInstance(){
		return instance;
	}
	
	private MatchDataController(){
		isDataInitDone = false;
		linsterList = new ArrayList<MatchDataListener>();
		lockObj = new Object();
	}
	
	/*
	 * set @MatchDataListener listener
	 * 
	 * @param listener
	 * The @MatchDataListener object
	 */
	public void setListener(MatchDataListener listener){
		if(listener != null && !linsterList.contains(listener)){
			linsterList.add(listener);
			LogHelper.d(TAG, "Add listener " + listener.toString());
		}
	}
	
	/*
	 * remove the @MatchDataListener listener
	 * 
	 * @param listener
	 * The @MatchDataListener object
	 */
	public void removeListener(MatchDataListener listener){
		if(listener != null && linsterList.contains(listener)){
			linsterList.remove(listener);
			LogHelper.d(TAG, "Remove the listener " + listener.toString());
		}
	}
	
	/*
	 * Get the Matches info @MatchesModel object
	 * 
	 * @return @SparseArray<MatchesModel>
	 */
	public SparseArray<MatchesModel> getMatchesData() {
		return dataHelper.getMatchesList();
	}

	/*
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
				LogHelper.d(TAG, "Load necessary data from file");
				if(!dataHelper.loadMatchesData() || !dataHelper.loadNationalData()){
					
					LogHelper.w(TAG, "Fail to init the data!");
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

	
	/*
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

	
	
	public Boolean setMatchRemind(ArrayList<Integer> matchesList){
		
		LogHelper.d(TAG, "updateData");
		
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
	
	
	@Override
	public void onInitDone(Boolean isSuccess) {

		if(linsterList != null && linsterList.size() > 0){
			for (MatchDataListener listener : linsterList) {
				listener.onInitDone(isSuccess);
			}
		}
	}

	@Override
	public void onUpdateDone(Boolean haveNewVersion, Boolean isSuccess) {

		if(linsterList != null && linsterList.size() > 0){
			for (MatchDataListener listener : linsterList) {
				listener.onUpdateDone(haveNewVersion, isSuccess);
			}
		}
	}

	@Override
	public void onSetRemindDone(Boolean isSuccess) {

		if(linsterList != null && linsterList.size() > 0){
			for (MatchDataListener listener : linsterList) {
				listener.onSetRemindDone(isSuccess);
			}
		}
	}
}
