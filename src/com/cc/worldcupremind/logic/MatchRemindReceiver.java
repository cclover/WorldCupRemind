package com.cc.worldcupremind.logic;

import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.model.MatchDate;
import com.cc.worldcupremind.view.AlarmActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class MatchRemindReceiver extends BroadcastReceiver {

	private static final String TAG = "MatchRemindReceiver";
	private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";  
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(intent == null || intent.getAction() == null){
			LogHelper.w(TAG, "Receive intent(action) is null");
			return;
		}
		
		if(intent.getAction().equals(ACTION_BOOT)){
			LogHelper.d(TAG, "Receive BOOT_COMPLETED..Load data and set alarm");
			MatchDataController.getInstance().InitData(context);
			
		} else if(intent.getAction().equals(MatchRemindHelper.ACTION_ALARM)){
			LogHelper.d(TAG, "Receive the match alarm");
			
			if(intent.getExtras() == null){
				LogHelper.w(TAG, "Invalid intent");
				return;
			}
			
			//Init data
			if(!MatchDataController.getInstance().isDataInit()){
				LogHelper.d(TAG, "Init Data First");
				MatchDataController.getInstance().InitDataAsync(context);
			}
			
			//Show remind info
			int matchNo = intent.getExtras().getInt(MatchRemindHelper.REMIND_MATCHES_NO);
			String team1 =  intent.getExtras().getString(MatchRemindHelper.REMIND_MATCHES_TEAM_1);
			String team2 =  intent.getExtras().getString(MatchRemindHelper.REMIND_MATCHES_TEAM_2);
			String time = intent.getExtras().getString(MatchRemindHelper.REMIND_MATCHES_TIME);
			LogHelper.d(TAG, String.format("The remind:[%d][%s VS %s][%s]", 
					matchNo,team1,team2,time));
			
			//check
			if(matchNo <= 0 || team1.length() != 3|| team2.length() !=3 || time.length() == 0){
				LogHelper.w(TAG, "Invalid alarm intent");
				return;
			}
			
			MatchDate date = new MatchDate(context, time);
			if(date.isStart()){
				LogHelper.w(TAG, "Match is start!");
				return;
			}
			
			//Start alarm activity
			intent.setClass(context, AlarmActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY );
			context.startActivity(intent);
		}
	}
}
