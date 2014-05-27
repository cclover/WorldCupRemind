package com.cc.worldcupremind.logic;

import java.util.ArrayList;
import java.util.HashMap;

import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.logic.MatchDataHelper.UPDATE_RET;
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

				// Check version
				UPDATE_RET ret = dataHelper.updateAllData();
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
}
