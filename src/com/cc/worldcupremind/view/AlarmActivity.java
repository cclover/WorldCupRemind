/**
 * 
 */
package com.cc.worldcupremind.view;

import java.io.IOException;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.common.LogHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		setContentView(R.layout.activity_alarm);
		
		listView = (ListView)findViewById(R.id.listAlarm);
		
		btnClose = (Button)findViewById(R.id.btnAlarmClose);
		btnClose.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		playMusicAndVibrator();
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(vibrator != null){
			vibrator.cancel();
		}
		
		if(alarmPlayer != null){
			alarmPlayer.stop();
		}
	}
	
	private void playMusicAndVibrator(){
		
	    //play music
	    alarmPlayer = new MediaPlayer();
	    
	    setVolumeControlStream(AudioManager.STREAM_ALARM); 
	    AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	    if(audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0){
	    	alarmPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
	    	alarmPlayer.setLooping(true);
	    }

	    AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.alarm_ring); 
	    try { 
	    	alarmPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength()); 
	    	file.close(); 
//	    	alarmPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME); 
	    	alarmPlayer.prepare(); 
	    } catch (IOException e) { 
	    	LogHelper.e(TAG, e);
	    	alarmPlayer = null; 
			finish();
	    } 
	    alarmPlayer.start();
	    
		//Vibrator
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE); 
		long[] pattern = {0, 500, 500};          
	    vibrator.vibrate(pattern,0);
	}
}
