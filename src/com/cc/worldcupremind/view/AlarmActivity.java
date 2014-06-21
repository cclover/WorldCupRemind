/**
 * 
 */
package com.cc.worldcupremind.view;

import java.io.IOException;
import java.util.ArrayList;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.logic.MatchDataController;
import com.cc.worldcupremind.logic.MatchRemindHelper;
import com.cc.worldcupremind.model.MatchDate;
import com.cc.worldcupremind.model.MatchStage;
import com.cc.worldcupremind.model.MatchStatus;
import com.cc.worldcupremind.model.MatchesModel;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author cc
 *
 */
public class AlarmActivity extends Activity {

	private static final String TAG = "AlarmActivity";
	private Button btnClose = null;
	private ListView listView = null;
	private Vibrator vibrator = null;
	private MediaPlayer alarmPlayer = null;
	private ArrayList<MatchesModel> alarmList = null;
	private AlarmAdpater adpater = null;
	private LayoutInflater mInflater = null;
	private Resources resource = null;
	private MatchDataController controller = null;
	private Context context = null;
	private NotificationManager nm = null;
	private AudioManager audioManager = null;
	private PowerManager.WakeLock mWakelock = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogHelper.d(TAG, "onCreate");
		
		//Switch to Theme_Wallpaper_NoTitleBar in lock screen. Need invoke  setTheme before super.onCreate on ANDROID 2.3.3
		KeyguardManager manager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		if(manager.inKeyguardRestrictedInputMode()){
			LogHelper.d(TAG, "Is lock screen. Change the style");
			setTheme(android.R.style.Theme_Wallpaper_NoTitleBar);
		}
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		//Turn on screen. On ANDROID 2.3.3, FLAG_TURN_SCREEN_ON DO NOT WORK.
		if(Build.VERSION.SDK_INT < 10){
			PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE); 
			mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "worldcup_alarm"); 
			mWakelock.acquire();
		}
		setContentView(R.layout.activity_alarm);
	    audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		
		//Set listview
		listView = (ListView)findViewById(R.id.listAlarm);
		alarmList = new ArrayList<MatchesModel>();
		adpater = new AlarmAdpater();
		listView.setAdapter(adpater);
		mInflater = LayoutInflater.from(this);
		resource = getResources();
		controller = MatchDataController.getInstance();
		context = this;
		nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);     
		
		//Set button
		btnClose = (Button)findViewById(R.id.btnAlarmClose);
		btnClose.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LogHelper.w(TAG, "onClick");
				stopMusicAndVibrator();
				finish();
			}
		});
		
		//Get intent
		Intent intent = getIntent();
		if(!parseIntent(intent)){
			LogHelper.w(TAG, "parse intent failed");
			finish();
		}
		
		//Show notification
		showNotification();
		
		//Play music
		playMusicAndVibrator();
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if(!parseIntent(intent)){
			LogHelper.w(TAG, "parse intent failed");
			finish();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogHelper.d(TAG, "onDestroy");
		if(mWakelock != null && mWakelock.isHeld()){
			mWakelock.release();
		}
		stopMusicAndVibrator();
		
	    if(audioManager != null){
		    audioManager.abandonAudioFocus(null);
	    }
	    
		if(nm != null){
			nm.cancel(R.string.app_name);
			deleteRemind();
		}
	}
	
	private void deleteRemind(){
		ArrayList<Integer> cancelList = new ArrayList<Integer>();
		for(int i = 0; i < alarmList.size(); i++){
			int no = alarmList.get(i).getMatchNo();
			cancelList.add(no);
			LogHelper.d(TAG, "Delete the alarm:" + String.valueOf(no));
		}
		controller.deleteMatchRemind(cancelList);
	}
	
	private void showNotification(){
		
		//Create intent 
		Intent i = new Intent(this, AlarmActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);          
		PendingIntent contentIntent = PendingIntent.getActivity(this,R.string.app_name, i, PendingIntent.FLAG_ONE_SHOT);
		
		//Create notification
		NotificationCompat.Builder bulider = new NotificationCompat.Builder(getApplicationContext());
		bulider.setContentTitle(getResources().getString(R.string.str_alram_remind));
		bulider.setContentText(getResources().getString(R.string.str_alram_notify));
		bulider.setContentIntent(contentIntent);
		bulider.setSmallIcon(R.drawable.ic_launcher);
		Notification notify = bulider.build();
		notify.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;  
		
		//show notification
		nm.notify(R.string.app_name, notify);
	}
	
	private void playMusicAndVibrator(){
		
		LogHelper.d(TAG, "playMusicAndVibrator");
	    //Set audio 
	    setVolumeControlStream(AudioManager.STREAM_ALARM); 
	    if(audioManager != null){
		    audioManager.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
	    }

	    //Create media player
	    alarmPlayer = new MediaPlayer();
    	alarmPlayer.setLooping(true);
	    alarmPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
	    
	    //Get music 
	    AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.alarm_ring); 
	    try { 
	    	alarmPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength()); 
	    	file.close(); 
	    	alarmPlayer.prepare(); 
	    } catch (IOException e) { 
	    	LogHelper.e(TAG, e);
	    	alarmPlayer = null; 
			finish();
	    } 
	    alarmPlayer.start();
	    
		//Vibrator
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE); 
		long[] pattern = {500, 500, 500};          
	    vibrator.vibrate(pattern,0);
	}
	
	private void stopMusicAndVibrator(){
		
		LogHelper.d(TAG, "stopMusicAndVibrator");
		if(vibrator != null){
			vibrator.cancel();
			vibrator = null;
		}
		
		if(alarmPlayer != null && alarmPlayer.isPlaying()){
			alarmPlayer.stop();
			alarmPlayer.release();
			alarmPlayer = null;
		}
	}
	
	private Boolean parseIntent(Intent intent){
		
		LogHelper.d(TAG, "parseIntent");
		if(intent != null && intent.getExtras() != null){
			try{
				int matchNo = intent.getExtras().getInt(MatchRemindHelper.REMIND_MATCHES_NO);
				String team1 =  intent.getExtras().getString(MatchRemindHelper.REMIND_MATCHES_TEAM_1);
				String team2 =  intent.getExtras().getString(MatchRemindHelper.REMIND_MATCHES_TEAM_2);
				String time = intent.getExtras().getString(MatchRemindHelper.REMIND_MATCHES_TIME);
			    int stage = intent.getExtras().getInt(MatchRemindHelper.REMIND_MATCHES_STAGE);
			    String group = intent.getExtras().getString(MatchRemindHelper.REMIND_MATCHES_GROUP);
				LogHelper.i(TAG, String.format("The remind:[%d][%s VS %s][%s]", 
						matchNo,team1,team2,time));
			    MatchesModel model = new MatchesModel(
			    		matchNo,
			    		MatchStage.valueOf(stage),
			    		group,
			    		new MatchDate(context, time),
			    		team1,team2,MatchStatus.MATCH_STATUS_WAIT_START,0,0,true);
			    alarmList.add(model);
				adpater.refresh();
			}catch(Exception ex){
				LogHelper.e(TAG, ex);
				return false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK)
        {  
			return true;
        }
		return false;
	}
	
	
	//Fix issue: click white area close the activity API LEVEL < 11
	//IF API LEVEL > 11 setFinishOnTouchOutside(false);  
	@Override  
    public boolean onTouchEvent(MotionEvent event) {  
        if (event.getAction() == MotionEvent.ACTION_DOWN && isOutOfBounds(this, event)) {  
            return true;  
        }  
        return super.onTouchEvent(event);  
    }  
  
    private boolean isOutOfBounds(Activity context, MotionEvent event) {  
        final int x = (int) event.getX();  
        final int y = (int) event.getY();  
        final int slop = ViewConfiguration.get(context).getScaledWindowTouchSlop();  
        final View decorView = context.getWindow().getDecorView();  
        return (x < -slop) || (y < -slop)|| (x > (decorView.getWidth() + slop))|| (y > (decorView.getHeight() + slop));  
    }  
    
	class AlarmAdpater extends BaseAdapter{
		

		public void refresh(){
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			if(alarmList == null)
				return 0;
			return alarmList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.alarm_item, null);
			     holder = new ViewHolder();
			     holder.group = (TextView)convertView.findViewById(R.id.txtGroup);
			     holder.flag1 = (ImageView)convertView.findViewById(R.id.imgFlag1);
			     holder.team1 = (TextView)convertView.findViewById(R.id.txtTeam1);
			     holder.score = (TextView)convertView.findViewById(R.id.txtScore);
			     holder.team2 = (TextView)convertView.findViewById(R.id.txtTeam2);
			     holder.flag2 = (ImageView)convertView.findViewById(R.id.imgFlag2);
			     convertView.setTag(holder);
			} else {
			     holder = (ViewHolder)convertView.getTag();
			}
			
			//Set value
			MatchesModel model = alarmList.get(position);
			if(model.getMatchStage() == MatchStage.STAGE_GROUP){
				holder.group.setText(String.format(resource.getString(R.string.str_stage_group),  model.getGroupName()));
			}else{
				holder.group.setText(resource.getString(model.getMatchStage().getStringResourceID()));
			}

			holder.team1.setText(controller.getTeamNationalName(model.getTeam1Code()));
			Drawable drawable1= controller.getTeamNationalFlag(model.getTeam1Code());
			if(drawable1 != null){
				holder.flag1.setImageDrawable(drawable1);
			}
			holder.team2.setText(controller.getTeamNationalName(model.getTeam2Code()));
			Drawable drawable2 = controller.getTeamNationalFlag(model.getTeam2Code());
			if(drawable2 != null){
				holder.flag2.setImageDrawable(drawable2);
			}
			if(model.getMatchStatus() == MatchStatus.MATCH_STATUS_WAIT_START){
				holder.score.setText(model.getMatchTime().getTimeString());
			}else{
				holder.score.setText(String.format("%d:%d", model.getTeam1Score(), model.getTeam2Score()));
			}
			return convertView;
		}
		
	
		class ViewHolder{
			TextView group;
			ImageView flag1;
			TextView team1;
			TextView score;
			TextView team2;
			ImageView flag2;
		}
	}

}
