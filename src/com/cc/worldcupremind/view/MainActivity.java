package com.cc.worldcupremind.view;

import java.util.Timer;
import java.util.TimerTask;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.logic.MatchDataController;
import com.cc.worldcupremind.logic.MatchDataListener;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements
		ActionBar.TabListener , MatchDataListener{

	private static final String TAG = "MainActivity";

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	MatchDataController controller;
	MatchesFragment matchFragment;
	GroupFragment groupFragment;
	StatisticsFragment statisticsFragment;
	NewsFragment newsFragment;
	MenuItem remindItem;
	MenuItem remindFlagItem;
	MenuItem secondStageItem;
	MenuItem statisticsItem;
	MenuItem newsItem;
	Boolean isExit;
	ActionBar actionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LogHelper.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		isExit = false;

		// Set up the action bar.
		actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(4);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
						//set menu visible in different page
						if(position == 0){
							if(controller.isRemindEnable()){
								remindItem.setVisible(true);
								if(!matchFragment.hasFragmentShown()){
									LogHelper.d(TAG, "First show matchFragment!");
									matchFragment.showFragment();
									if(matchFragment.isDataInit()){
										LogHelper.d(TAG, "Data init done! show matchFragment!");
										matchFragment.setData(controller.getMatchesData());
									}
								}
							}
						}else{
							if(remindItem != null){
								remindItem.setVisible(false);
							}
						}
						if(position == 1){
							secondStageItem.setVisible(true);
							if(!groupFragment.hasFragmentShown()){
								LogHelper.d(TAG, "First show groupFragment! Load data to show!");
								groupFragment.showFragment();
								if(groupFragment.isDataInit()){
									LogHelper.d(TAG, "Data init done! show groupFragment!");
									groupFragment.setData(controller.getGroupStaticsData());
								}
							}
						}else{
							secondStageItem.setVisible(false);
						}
						if(position == 2){
							statisticsItem.setVisible(true);
							if(!statisticsFragment.hasFragmentShown()){
								LogHelper.d(TAG, "First show statisticsFragment! Load data to show!");
								statisticsFragment.showFragment();
								if(statisticsFragment.isDataInit()){
									LogHelper.d(TAG, "Data init done! show statisticsFragment!");
									statisticsFragment.setData(controller.getGoalStaticsData(), controller.getAssistStaticsData());
								}
							}
						}else{
							statisticsItem.setVisible(false);
						}
						if(position == 3){
							newsItem.setVisible(true);
							newsFragment.showData();
						}else{
							newsItem.setVisible(false);
						}
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		LogHelper.d(TAG, "Load data!");
		controller = MatchDataController.getInstance();
		controller.setListener(this);
		controller.InitData(this, true);
	}
	
	@Override
	protected void onResume() {
		LogHelper.d(TAG, "onResume");
		super.onResume();
	}	

	@Override
	protected void onDestroy() 
	{
		LogHelper.d(TAG, "onDestroy");
		super.onDestroy();
		controller.removeListener(this);
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK)
        {  
            exitBy2Click();
        }
		return false;
	}

	private void exitBy2Click() {
		Timer tExit = null;
		if (isExit == false) {
			isExit = true; 
			showToast(R.string.app_exit);
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false; // cancel exit
				}
			}, 2000); //cancel

		} else {
			finish();//exit
//			System.exit(0);
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		LogHelper.d(TAG, "onCreateOptionsMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		remindItem = menu.findItem(R.id.action_remind);
		remindFlagItem = menu.findItem(R.id.action_remind_flag);
		secondStageItem = menu.findItem(R.id.action_second);
		statisticsItem = menu.findItem(R.id.action_statistics);
		newsItem = menu.findItem(R.id.action_refresh);
		
		//init remind flag item
		if(controller.isRemindEnable()){
			remindFlagItem.setTitle(R.string.menu_remind_disable);
			remindItem.setVisible(true);
		}else{
			remindFlagItem.setTitle(R.string.menu_remind_enable);
			remindItem.setVisible(false);
		}
		
		LogHelper.d(TAG, "onCreateOptionsMenu DONE");
	
		/**
		 * NOTE: AFTER launch the activity(ViewPage + ActionBar(NAVIGATION_MODE_TABS))
		 * The page will show after onCreateOptionsMenu return. 
		 * 
		 * If we load the first tab date in onCreate, it will increase the activity launch (visible)time.
		 * So we can wait the view change to visible , delay load the first tab data.
		 * 
		 * We can use another thread to send message, then the onCreateOptionsMenu can return as soon as possible.
		 * But use thread may spent many time, so we just send message delay.
		 */				
		LogHelper.d(TAG, "Delay show the matchFragment after activity visible");
		DelayLoadHandler handler = new DelayLoadHandler(Looper.myLooper());
		handler.sendEmptyMessageDelayed(0, 10);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		if(id == R.id.action_remind){ //Set alarm
			if(matchFragment != null){
				matchFragment.setAlarmMode(true);
			}
		}else if(id == R.id.action_second){ //Show second stage
			Intent intent = new Intent(this, KonckoutMatchActivity.class);
			startActivity(intent);
		}else if(id == R.id.action_statistics){ //Switch the goal/assist
			if(statisticsFragment != null){
				if(statisticsFragment.setGoalAssistList()){
					statisticsItem.setTitle(R.string.menu_assist);
					statisticsItem.setIcon(R.drawable.ic_action_assist);
				}else{
					statisticsItem.setTitle(R.string.menu_goal);
					statisticsItem.setIcon(R.drawable.ic_action_goal);
				}
			}
		}else if(id == R.id.action_refresh){
			if(newsFragment != null){
				newsFragment.refresh();
			}
		}else if (id == R.id.action_update) { //update local data
			showToast(R.string.str_update_check_start);
			if(!controller.updateData()){
				LogHelper.w(TAG, "Can't update");
				showToast(R.string.str_update_check_fail);
			}
		}else if(id == R.id.action_reset){ //reset loacl data
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.menu_reset);
			builder.setMessage(R.string.str_update_data_reset);
			builder.setIcon(R.drawable.ic_alerts_warning);
			builder.setPositiveButton(android.R.string.ok, new OnClickListener() {	
				@Override
				public void onClick(DialogInterface dialog, int which) {
					LogHelper.d(TAG, "Reset data");
					controller.resetData();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, null);
			builder.show();
		}else if(id == R.id.action_about){ //Show about dialog
			new AboutDialog(this).show();
		}else if(id == R.id.action_remind_flag){ //Set remind flag
			if(controller.isRemindEnable()){
				if(controller.setRemindEnabl(false)){
					remindFlagItem.setTitle(R.string.menu_remind_enable);
					showToast(R.string.str_remind_disable);
					remindItem.setVisible(false);
				}else{
					showToast(R.string.str_remind_fail);
				}
			}else{
				if(controller.setRemindEnabl(true)){
					remindFlagItem.setTitle(R.string.menu_remind_disable);
					showToast(R.string.str_remind_enable);
					if(actionBar.getSelectedNavigationIndex() == 0){
						remindItem.setVisible(true);
					}
				}else{
					showToast(R.string.str_remind_fail);
				}
			}
			if(matchFragment != null){
				matchFragment.refresh();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	
	 class DelayLoadHandler extends Handler{    
	     
		 public DelayLoadHandler(Looper looper){    
		     super(looper);    
		 }    
		     
		 public DelayLoadHandler(){    
		     super();    
		 }    
		 
		 @Override    
		 public void handleMessage(Message msg) {    
		     super.handleMessage(msg);    
		     
		     LogHelper.d(TAG, "Delay laod the matchFragment data!");
				matchFragment.showFragment();
				if(matchFragment.isDataInit()){
					LogHelper.d(TAG, "Data init done! show matchFragment!");
					matchFragment.setData(controller.getMatchesData());
				}
		 }   
	 }
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			matchFragment = new MatchesFragment();
			groupFragment = new GroupFragment();
			statisticsFragment = new StatisticsFragment();
			newsFragment = new NewsFragment();
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			if(position == 0){
				return matchFragment;
			}else if(position == 1){
			    return groupFragment;
			}else if(position == 2){
				return statisticsFragment;
			}else if(position == 3){
				return newsFragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.tab_mathces);
			case 1:
				return getString(R.string.tab_group);
			case 2:
				return getString(R.string.tab_statistics);
			case 3:
				return getString(R.string.tab_news);
			}
			return null;
		}
	}
	
	
	/**
	 * If the fragment has visited, load data and show
	 * If the fragment has not visited, will load and show in ViewPage.setOnPageChangeListener
	 */
	private void loadDataIfFragmentShown(){
		if(matchFragment != null && matchFragment.hasFragmentShown()){
			LogHelper.d(TAG, "matchFragment has shown before data (init/update/reset), Load data and show!");
			matchFragment.setData(controller.getMatchesData());
		}
		if(groupFragment != null && groupFragment.hasFragmentShown()){
			LogHelper.d(TAG, "groupFragment has shown before data (init/update/reset), Load data and show!");
			groupFragment.setData(controller.getGroupStaticsData());
		}
		if(statisticsFragment != null && statisticsFragment.hasFragmentShown()){
			LogHelper.d(TAG, "statisticsFragment has shown before data (init/update/reset), Load data and show!");
			statisticsFragment.setData(controller.getGoalStaticsData(), controller.getAssistStaticsData());
		}
	}
	
	@Override
	public void onInitDone(Boolean isSuccess) {
		
		LogHelper.i(TAG, String.format("onInitDone result is %sb", isSuccess));
		final Boolean ret = isSuccess;
		
		LogHelper.d(TAG, "Tell fragment Data Init DONE");
		if(ret){
			if(matchFragment != null){
				matchFragment.setDataInit();
			}
			if(groupFragment != null){
				groupFragment.setDataInit();
			}
			if(statisticsFragment != null){
				statisticsFragment.setDataInit();
			}
		}

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(ret){
					LogHelper.d(TAG, "Init done!");
					loadDataIfFragmentShown();
				}else{
					LogHelper.d(TAG, "Init fail!");
					showToast(R.string.data_fail);
				}
			}
		});
	}

	@Override
	public void onUpdateDone(int status, String appURL) {

		LogHelper.i(TAG, String.format("The update status is %d", status));
		
		final int sta = status;
		final String url = appURL;
		final Context tmpContext = this;
		
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				
				if(sta == UPDATE_STATE_CHECK_ERROR){
					LogHelper.w(TAG, "UPDATE_STATE_CHECK_ERROR");
					showToastLong(R.string.str_update_check_fail);
				}else if(sta == UPDATE_STATE_CHECK_NONE){
					LogHelper.d(TAG, "UPDATE_STATE_CHECK_NONE");
					showToastLong(R.string.str_update_check_none);
				}else if(sta == UPDATE_STATE_CHECK_NEW_APK){
					LogHelper.d(TAG, "UPDATE_STATE_CHECK_NEW_APK");
					//Show aleart message
					AlertDialog.Builder builder = new AlertDialog.Builder(tmpContext);
					builder.setTitle(R.string.str_update_apk_title);
					builder.setMessage(R.string.str_update_apk_message);
					builder.setIcon(R.drawable.ic_launcher);
					builder.setPositiveButton(R.string.str_update_apk_download, new OnClickListener() {	
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//download and install apk					
							Intent intent = new Intent(Intent.ACTION_VIEW);
		                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		                    intent.setData(Uri.parse(url));
		                    tmpContext.startActivity(intent);
						}
					});
					builder.setNegativeButton(android.R.string.cancel, null);
					builder.show();
				}else if(sta == UPDATE_STATE_UPDATE_START){
					LogHelper.d(TAG, "UPDATE_STATE_UPDATE_START");
					showToast(R.string.str_update_update_start);
				}else if(sta == UPDATE_STATE_UPDATE_ERROR){
					LogHelper.w(TAG, "UPDATE_STATE_UPDATE_ERROR");
					showToast(R.string.str_update_update_fail);
				}else if(sta == UPDATE_STATE_UPDATE_DONE){
					LogHelper.d(TAG, "UPDATE_STATE_UPDATE_DONE");
					loadDataIfFragmentShown();
					showToastLong(String.format(tmpContext.getResources().getString(R.string.str_update_update_done),
							controller.getDataVersion()));
				}
			}
		});
	}


	@Override
	public void onSetRemindDone(Boolean isSuccess) {
		
		LogHelper.i(TAG, String.format("onSetRemindDone is %b", isSuccess));
		if(isSuccess){
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					matchFragment.setData(controller.getMatchesData());
				}
			});
		}
	}


	@Override
	public void onTimezoneChanged() {
		
		LogHelper.i(TAG, "onTimezoneChanged");
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				matchFragment.refresh();
			}
		});
	}


	@Override
	public void onLocalChanged() {
		
		LogHelper.i(TAG, "onLocalChanged");
		//When language changed , the activaty will be destory and create again
		//But can't show the data. If we set  android:configChanges = "locale | layoutDirection" and refresh 
		//The action bar language will not change
		LogHelper.d(TAG, "onLocalChanged");
		finish();
	}
	
	@Override
	public void onResetDone(Boolean issBoolean) {
		
		LogHelper.i(TAG, String.format("onResetDone is %b", issBoolean));
//		controller.InitData(this);
		final Boolean ret = issBoolean;
		final Context tmpContext = this;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(ret){
					loadDataIfFragmentShown();
					AlertDialog.Builder builder = new AlertDialog.Builder(tmpContext);
					builder.setTitle(R.string.menu_reset);
					builder.setMessage(R.string.str_update_data_reset_success);
					builder.setIcon(R.drawable.ic_launcher);
					builder.setPositiveButton(android.R.string.ok, null);
					builder.show();
				}else{
					showToast(R.string.str_update_data_reset_fail);
				}
			}
		});
	}  
	
	private void showToast(int id){

		Toast toast = Toast.makeText(getApplicationContext(),
			     getResources().getString(id), Toast.LENGTH_SHORT);
		toast.show();
	}
	
	private void showToastLong(int id){

		Toast toast = Toast.makeText(getApplicationContext(),
			     getResources().getString(id), Toast.LENGTH_LONG);
		toast.show();
	}
	
	private void showToastLong(String msg){

		Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
		toast.show();
	}

	class AboutDialog extends AlertDialog implements android.view.View.OnClickListener {   

		private Context context;
		private ImageView btnBlog;
		private ImageView btnWechat;
		private TextView txtContent;
		 
	    public AboutDialog(Context context) {   
	        super(context); 
	        this.context = context;
	        final View view = getLayoutInflater().inflate(R.layout.dialog_about, null); 
	        btnBlog = (ImageView)view.findViewById(R.id.btnBlog);
	        btnWechat = (ImageView)view.findViewById(R.id.btnWechat);
	        txtContent = (TextView)view.findViewById(R.id.txtAbout);
	        txtContent.setText(String.format(context.getResources().getString(R.string.str_about_app), controller.getDataVersion()));
	        btnBlog.setOnClickListener(this);
	        btnWechat.setOnClickListener(this);
	        setIcon(R.drawable.ic_launcher);   
	        PackageInfo info = null;
			try {
				info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			} catch (NameNotFoundException e) {
				LogHelper.e(TAG, e);
			} 
			String ver = info != null ? info.versionName : "";
	        setTitle(context.getResources().getString(R.string.app_name) + ver);    
	        setButton(AlertDialog.BUTTON_NEUTRAL, context.getResources().getString(R.string.str_about_close), (OnClickListener) null);
	        setView(view);   
	    }

		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.btnBlog){
		    	Intent intent = new Intent(Intent.ACTION_VIEW);
		    	intent.setData(Uri.parse("http://blog.csdn.net/cc_net"));
		    	context.startActivity(intent);
				this.cancel();
			}else if(v.getId() == R.id.btnWechat){
			  android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			  clipboard.setText("cc287388");
			  Toast toast = Toast.makeText(context,getResources().getString(R.string.str_about_wechar), Toast.LENGTH_SHORT);
			  toast.show();
			}
		}   
	    
	}
}
