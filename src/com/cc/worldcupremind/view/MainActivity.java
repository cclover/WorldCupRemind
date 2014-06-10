package com.cc.worldcupremind.view;

import java.util.ArrayList;
import java.util.Locale;

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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
	GroupFragment mGroupFragment;
	MenuItem remindItem;
	MenuItem confirmItem;
	MenuItem cancelItem;
	Boolean isSetAlarm = false;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		LogHelper.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
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
						if(position == 0){
							setMeunStatus();
						}else{
							if(remindItem != null){
								remindItem.setVisible(false);
							}
							if(confirmItem != null){
								confirmItem.setVisible(false);
							}
							if(cancelItem != null){
								cancelItem.setVisible(false);
							}
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
		controller.InitData(this);
	}
	
	
	private void setMeunStatus(){
		if(!isSetAlarm){
			remindItem.setVisible(true);
			confirmItem.setVisible(false);
			cancelItem.setVisible(false);
		}else{
			remindItem.setVisible(false);
			confirmItem.setVisible(true);
			cancelItem.setVisible(true);
		}
	}

	@Override
	protected void onDestroy() 
	{
		LogHelper.d(TAG, "onDestroy");
		super.onDestroy();
		controller.removeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		LogHelper.d(TAG, "onCreateOptionsMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		remindItem = menu.findItem(R.id.action_remind);
		confirmItem = menu.findItem(R.id.action_confirm);
		cancelItem = menu.findItem(R.id.action_cancel);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_update) {
			if(!controller.updateData()){
				LogHelper.d(TAG, "Can't update");
			}
			return true;
		}else if(id == R.id.action_remind){
			
			isSetAlarm = true;
			setMeunStatus();
			matchFragment.setAlarmMode(true);
		}else if(id == R.id.action_confirm){
			
			isSetAlarm = false;
			setMeunStatus();
			ArrayList<Integer> remindList = matchFragment.getRemindList();
			for(int i : remindList){
				LogHelper.d(TAG, "Set Match no:" + String.valueOf(i));
			}
			if(!controller.setMatchRemind(remindList)){
				LogHelper.w(TAG, "Can't setMatchRemind");
			}
			matchFragment.setAlarmMode(false);
		}else if(id == R.id.action_cancel){
			
			isSetAlarm = false;
			setMeunStatus();
			matchFragment.setAlarmMode(false);
		}else if(id == R.id.action_about){
			
			//TODO: TEST
			Intent t = new Intent(this, AlarmActivity.class);
			startActivity(t);
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

	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			matchFragment = new MatchesFragment();
			mGroupFragment = new GroupFragment();
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			if(position == 0){
				return matchFragment;
			}else if(position ==1){
			    return mGroupFragment;
			}
			return PlaceholderFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.tab_mathces).toUpperCase(l);
			case 1:
				return getString(R.string.tab_group).toUpperCase(l);
			case 2:
				return getString(R.string.tab_statistics).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			TextView textView = (TextView) rootView
					.findViewById(R.id.section_label);
			textView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}

	
	@Override
	public void onInitDone(Boolean isSuccess) {
		
		LogHelper.d(TAG, String.format("onInitDone result is %s", isSuccess?"true":"false"));
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				matchFragment.setData(controller.getMatchesData());	
				mGroupFragment.setData(controller.getGroupStaticsData());	
			}
		});
	}

	@Override
	public void onUpdateDone(Boolean haveNewVersion, Boolean isSuccess) {

		LogHelper.d(TAG, String.format("isSuccess is %s", isSuccess?"true":"false"));
		LogHelper.d(TAG, String.format("haveNewVersion is %s", haveNewVersion?"true":"false"));
		
		if(isSuccess && haveNewVersion){
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					matchFragment.setData(controller.getMatchesData());	
					mGroupFragment.setData(controller.getGroupStaticsData());	
				}
			});
		}
	}


	@Override
	public void onSetRemindDone(Boolean isSuccess) {
		
		LogHelper.d(TAG, String.format("onSetRemindDone is %s", isSuccess?"true":"false"));
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
		
		LogHelper.d(TAG, "onTimezoneChanged");
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				matchFragment.refreshData();
			}
		});
	}


	@Override
	public void onLocalChanged() {
		
		//When language changed , the activaty will be destory and create again
		//But can't show the data. If we set  android:configChanges = "locale | layoutDirection" and refresh 
		//The action bar language will not change
		LogHelper.d(TAG, "onLocalChanged");
		finish();
	}

}
