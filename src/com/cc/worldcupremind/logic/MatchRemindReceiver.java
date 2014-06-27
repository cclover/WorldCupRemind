package com.cc.worldcupremind.logic;

import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.model.MatchDate;
import com.cc.worldcupremind.view.AlarmActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;


public class MatchRemindReceiver extends BroadcastReceiver {

	private static final String TAG = "MatchRemindReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(intent == null || intent.getAction() == null){
			LogHelper.w(TAG, "Receive intent(action) is null");
			return;
		}
		
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			LogHelper.i(TAG, "Receive BOOT_COMPLETED..Load data and set alarm");
			MatchDataController.getInstance().InitData(context, false);
			
		}else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
			
			LogHelper.i(TAG, "Receive CONNECTIVITY_ACTION..Load data and set alarm");
			MatchDataController.getInstance().InitData(context, false);
		}else if(intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)){
			
			LogHelper.d(TAG, "Receive MY_PACKAGE_REPLACED..Load data and set alarm");
			MatchDataController.getInstance().InitData(context, false);
		}else if(intent.getAction().equals(MatchRemindHelper.ACTION_ALARM)){
			LogHelper.i(TAG, "Receive the match alarm");
			
			if(intent.getExtras() == null){
				LogHelper.w(TAG, "Invalid intent");
				return;
			}
			
			//Check remind status
			if(!MatchDataController.getInstance().isRemindEnable()){
				LogHelper.d(TAG, "Remind is disable");
				return;
			}
			
			//Init data
			if(!MatchDataController.getInstance().isDataInit()){
				LogHelper.d(TAG, "Init Data First");
				MatchDataController.getInstance().InitDataSync(context);
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
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //| Intent.FLAG_ACTIVITY_NO_HISTORY can't use this for multi intent on singleinstance acticity
			context.startActivity(intent);
		}
	}
}
