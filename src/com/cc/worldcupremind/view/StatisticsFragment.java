package com.cc.worldcupremind.view;

import java.util.ArrayList;
import java.util.Locale;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.model.PlayerStatistics;
import com.cc.worldcupremind.model.PlayerStatistics.STATISTICS_TYPE;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class StatisticsFragment extends BaseFragment {

	private static final String TAG = "StatisticsFragment";
	private ArrayList<PlayerStatistics> mGoalStaticsList;
	private ArrayList<PlayerStatistics> mAssistStaticsList;
	private ArrayList<PlayerStatistics> mDataStaticsList;
	private TextView txtHeaderType;
	private ImageView imgHeaderFlag;
	private Boolean isGoal;

	
	public StatisticsFragment(){
		
		txtHeaderType = null;
		imgHeaderFlag = null;
		isGoal = true;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_statistics, container, false);
		imgHeaderFlag = (ImageView)view.findViewById(R.id.imgStatTitle);
		txtHeaderType = (TextView)view.findViewById(R.id.txtStatTitleCount);
		imgHeaderFlag.setBackgroundResource(R.drawable.ic_title_goal);
		txtHeaderType.setText(resource.getString(R.string.str_player_goal));
		progressBar = (ProgressBar)view.findViewById(android.R.id.progress);
		super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }
	
	public void setData(ArrayList<PlayerStatistics> goalStaticsData, ArrayList<PlayerStatistics> assistStaticsData) {
		
		LogHelper.d(TAG, "StatisticsFragment::setData");
		mGoalStaticsList = goalStaticsData;
		mAssistStaticsList = assistStaticsData;
		super.setAdapter();
		switchView();
	}
	
	@Override
	public BaseAdapter createAdapter() {
		return new PlayerStaticsListAdapter();
	}
	

	public Boolean setGoalAssistList(){
		
		LogHelper.d(TAG, "StatisticsFragment::setGoalAssistList");
		if(isGoal){
			isGoal = false;
		}else{
			isGoal = true;
		}
		switchView();
		return isGoal;
	}
	
	
	private void switchView(){
		
		LogHelper.d(TAG, "StatisticsFragment::showData");
		if(isGoal){
			mDataStaticsList = mGoalStaticsList;
			if(txtHeaderType != null){
				txtHeaderType.setText(resource.getString(R.string.str_player_goal));
			}
			if(imgHeaderFlag != null){
				imgHeaderFlag.setBackgroundResource(R.drawable.ic_title_goal);
			}
		}else{
			mDataStaticsList = mAssistStaticsList;
			if(txtHeaderType != null){
				txtHeaderType.setText(resource.getString(R.string.str_player_assist));
			}
			if(imgHeaderFlag != null){
				imgHeaderFlag.setBackgroundResource(R.drawable.ic_title_assist);
			}
		}
		super.refresh();
	}
	
		
	class PlayerStaticsListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if(mDataStaticsList != null){
				return mDataStaticsList.size();
			}
			return 0;
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
		public int getViewTypeCount() 
		{
			return 1;
		};
		
	    public int getItemViewType(int position) {
	    	return 0;
	    }
		
	    private Boolean isChinese(){
	    	
	    	Locale l = Locale.getDefault();  
	    	String language = l.getLanguage();  
	    	String country = l.getCountry().toLowerCase(l);
	    	return language.equals("zh") && country.equals("cn");
	    }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			//Get view
			HolderView holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.statistics_item, null);
				holder = new HolderView();
				holder.txtPalyerPos = (TextView)convertView.findViewById(R.id.txtStatPos);
				holder.imgFlag = (ImageView)convertView.findViewById(R.id.imgStatFlag);
				holder.txtPalyName = (TextView)convertView.findViewById(R.id.txtStatName);
				holder.txtCount = (TextView)convertView.findViewById(R.id.txtStatCount);
				holder.txtTeamName = (TextView)convertView.findViewById(R.id.txtStatNatinoal);
				holder.imgTeamFlag = (ImageView)convertView.findViewById(R.id.imgStatNationalFlag);
			    convertView.setTag(holder);
			} else {
			     holder = (HolderView)convertView.getTag();
			}
			
			//Set value
			PlayerStatistics data = mDataStaticsList.get(position);
			if(data.getPosition() == 1 && data.getCount() != 0){
				holder.imgFlag.setVisibility(View.VISIBLE);
			}else{
				holder.imgFlag.setVisibility(View.INVISIBLE);
			}
			holder.txtPalyerPos.setText(String.valueOf(data.getPosition()));
			if(isChinese()){
				holder.txtPalyName.setText(data.getPlayerName());
			}else{
				holder.txtPalyName.setText(data.getPlayerEngName());
			}
			
			int penCount = data.getPenGoalCount();
			if(data.getType() == STATISTICS_TYPE.STATISTICS_GOAL && penCount > 0){
				holder.txtCount.setText(String.format("%d(%d)", data.getCount(), penCount));
			}else{
				holder.txtCount.setText(String.valueOf(data.getCount()));
			}
			holder.txtTeamName.setText(controller.getTeamNationalName(data.getPlayerTeamCode()));
			Drawable drawable= controller.getTeamNationalFlag(data.getPlayerTeamCode());
			if(drawable != null){
				holder.imgTeamFlag.setImageDrawable(drawable);
			}
			
			return convertView;
		}
		
		class HolderView{
			TextView txtPalyerPos;
			ImageView imgFlag;
			TextView txtPalyName;
			TextView txtCount;
			TextView txtTeamName;
			ImageView imgTeamFlag;
		}
	}
}
