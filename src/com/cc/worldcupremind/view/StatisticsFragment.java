package com.cc.worldcupremind.view;

import java.util.ArrayList;
import java.util.Locale;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.logic.MatchDataController;
import com.cc.worldcupremind.model.PlayerStatistics;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class StatisticsFragment extends ListFragment {

	private static final String TAG = "StatisticsFragment";
	private ArrayList<PlayerStatistics> mGoalStaticsList;
	private ArrayList<PlayerStatistics> mAssistStaticsList;
	private ArrayList<PlayerStatistics> mDataStaticsList;
	private PlayerStaticsListAdapter mAdapter;
	private LayoutInflater mInflater;
	private MatchDataController controller;
	private Resources resource;
	private Button btnSwitch;
	private Boolean isGoal = true;
	private static final int TYPE_TITLE = 0;
	private static final int TYPE_DATA = 1;


	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
		mInflater = LayoutInflater.from(getActivity());
		mAdapter = new PlayerStaticsListAdapter();
		controller = MatchDataController.getInstance();
		resource = getActivity().getResources();
		isGoal = true;
    } 
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_statistics, container, false);
		btnSwitch = (Button)view.findViewById(R.id.btnSwitch);
		btnSwitch.setText(R.string.str_stat_goal);
		btnSwitch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(isGoal){
					mDataStaticsList  = mAssistStaticsList;
					btnSwitch.setText(R.string.str_stat_assist);
					isGoal = false;
				}else{
					mDataStaticsList = mGoalStaticsList;
					btnSwitch.setText(R.string.str_stat_goal);
					isGoal = true;
				}
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
			}
		});
		setListAdapter(mAdapter);   
        return view;
    }
	
	public void setData(ArrayList<PlayerStatistics> goalStaticsData, ArrayList<PlayerStatistics> assistStaticsData) {
		LogHelper.d(TAG, "setData");
		mGoalStaticsList = goalStaticsData;
		mAssistStaticsList = assistStaticsData;
		if(isGoal){
			mDataStaticsList = mGoalStaticsList;
		}else{
			mDataStaticsList = mAssistStaticsList;
		}
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}
	
	class PlayerStaticsListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if(mDataStaticsList != null){
				return mDataStaticsList.size() + 1;
			}
			return 1;
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
			return 2;
		};
		
	    public int getItemViewType(int position) {
	    	if(mGoalStaticsList == null || mGoalStaticsList == null){
	    		return TYPE_TITLE;
	    	}
	        if(position == 0){
	        	return TYPE_TITLE;
	        }else if(position > 0){
	        	return TYPE_DATA;
	        }
	        return TYPE_TITLE;
	    }
		
	    private Boolean isChinese(){
	    	
	    	Locale l = Locale.getDefault();  
	    	String language = l.getLanguage();  
	    	String country = l.getCountry().toLowerCase(l);
	    	return language.equals("zh") && country.equals("cn");
	    }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int type = getItemViewType(position);
			if (type == TYPE_TITLE) {
				
				//Get view
				HolderViewTitle holder = null;
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.statistics_type_item, null);
					holder = new HolderViewTitle();
					holder.imgTitleFlag = (ImageView)convertView.findViewById(R.id.imgStatTitle);
					holder.txtTitleName = (TextView)convertView.findViewById(R.id.txtStatTitleName);
					holder.txtTypeName = (TextView)convertView.findViewById(R.id.txtStatTitleCount);
					holder.txtTemaName = (TextView)convertView.findViewById(R.id.txtStatTitleTeam);
				    convertView.setTag(holder);
				} else {
				     holder = (HolderViewTitle)convertView.getTag();
				}
				
				//Set value
				holder.txtTitleName.setText(resource.getString(R.string.str_player_name));
				holder.txtTemaName.setText(resource.getString(R.string.str_team_name));
				if(isGoal){
					holder.txtTypeName.setText(resource.getString(R.string.str_player_goal));
				}else{
					holder.txtTypeName.setText(resource.getString(R.string.str_player_assist));
				}
			} else {
				
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
				PlayerStatistics data = mDataStaticsList.get(position-1);
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
				holder.txtCount.setText(String.valueOf(data.getCount()));
				holder.txtTeamName.setText(controller.getTeamNationalName(data.getPlayerTeamCode()));
				Drawable drawable= controller.getTeamNationalFlag(data.getPlayerTeamCode());
				if(drawable != null){
					holder.imgTeamFlag.setImageDrawable(drawable);
				}
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
		
		class HolderViewTitle{
			ImageView imgTitleFlag;
			TextView txtTitleName;
			TextView txtTypeName;
			TextView txtTemaName;
		}
	}

}
