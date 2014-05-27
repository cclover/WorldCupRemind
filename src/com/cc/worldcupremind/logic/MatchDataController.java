package com.cc.worldcupremind.logic;

import java.util.ArrayList;
import java.util.HashMap;

import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.model.MatchesModel;

import android.content.Context;
import android.util.SparseArray;


public class MatchDataController implements MatchDataListener{
	
	private static final String TAG = "MatchDataController";
	
	private Boolean isDataInitDone = false;
	private MatchDataHelper dataHelper = null;	
	private ArrayList<MatchDataListener> linsterList = new ArrayList<MatchDataListener>();
	private Context context = null;
	
	
	public void setListener(MatchDataListener listener){
		if(listener != null && !linsterList.contains(listener)){
			linsterList.add(listener);
			LogHelper.d(TAG, "Add listener " + listener.toString());
		}
	}
	
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
	public void InitData(Context context){
		
		//Init
		this.context = context;
		isDataInitDone = false;
		dataHelper = new MatchDataHelper(this.context);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				// Load the necessary data
				if(!dataHelper.loadMatchesData() || !dataHelper.loadNationalData()){
					
					LogHelper.w(TAG, "Fail to init the data!");
					onInitDone(false);
					return;
				}
				
				//Load the remind filed
				dataHelper.loadRemindData();
		
				isDataInitDone = true;
				onInitDone(true);
			}
		}).start();
	}

	/*
	 * Check update from network, receive result from @MatchDataListener
	 * 
	 * @return true if can check update.
	 * 
	 */
	public Boolean checkUpdate(){
		
		LogHelper.d(TAG, "checkUpdate()");
		
		if(!isDataInitDone){
			LogHelper.w(TAG, "Please invoke dataInit first");
			return false;
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Boolean ret = dataHelper.checkNewVersion();
				if(ret == null){
					onCheckUpdateDone(false, false);
				} else {
					onCheckUpdateDone(true, ret);
				}
			}
		}).start();
		
		return true;
	}
	
	
	/*
	 * Update data from network, receive result from @MatchDataListener
	 * 
	 * @return true if can update.
	 * 
	 */
	public Boolean updateData(){
		
		if(!isDataInitDone){
			return false;
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Boolean ret = dataHelper.updateMatchesData();
				LogHelper.d(TAG, String.format("updateData result is %s", ret?"true":"false"));
				onUpdateDone(ret);
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
	public void onCheckUpdateDone(Boolean isSuccess, Boolean haveNewVersion) {

		if(linsterList != null && linsterList.size() > 0){
			for (MatchDataListener listener : linsterList) {
				listener.onCheckUpdateDone(isSuccess, haveNewVersion);
			}
		}
	}

	@Override
	public void onUpdateDone(Boolean isSuccess) {
		
		if(linsterList != null && linsterList.size() > 0){
			for (MatchDataListener listener : linsterList) {
				listener.onUpdateDone(isSuccess);
			}
		}
	}
}
