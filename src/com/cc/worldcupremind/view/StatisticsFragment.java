package com.cc.worldcupremind.view;

import java.util.ArrayList;
import java.util.Locale;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.logic.MatchDataController;
import com.cc.worldcupremind.model.PlayerStatistics;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StatisticsFragment extends ListFragment {

	private static final String TAG = "StatisticsFragment";
	private ArrayList<PlayerStatistics> mGoalStaticsList;
	private ArrayList<PlayerStatistics> mAssistStaticsList;
	private PlayerStaticsListAdapter mAdapter;
	private LayoutInflater mInflater;
	private MatchDataController controller;
	private Resources resource;
	private static final int TYPE_GOGAL_TITLE = 0;
	private static final int TYPE_GOGAL = 1;
	private static final int TYPE_ASSIST_TITLE = 2;
	private static final int TYPE_ASSIST =3;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mInflater = LayoutInflater.from(getActivity());
		mAdapter = new PlayerStaticsListAdapter();
		controller = MatchDataController.getInstance();
		resource = getActivity().getResources();
		setListAdapter(mAdapter);
		this.getListView().setSelector(new ColorDrawable(Color.TRANSPARENT)); 
	}

	public void setData(ArrayList<PlayerStatistics> goalStaticsData, 
			ArrayList<PlayerStatistics> assistStaticsData) {
		mGoalStaticsList = goalStaticsData;
		mAssistStaticsList = assistStaticsData;
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}
	
	class PlayerStaticsListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			int goal = 0;
			int assist = 0;
			if(mGoalStaticsList != null){
				goal = mGoalStaticsList.size();
				if(goal > 0){
					goal++;
				}
					
			}
			if(mAssistStaticsList != null){
				assist = mAssistStaticsList.size();
				if(assist > 0){
					assist++;
				}
			}
			return goal + assist;
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
	        if(position == 0){
	        	return TYPE_GOGAL_TITLE;
	        }else if(position > 0 && position <= mGoalStaticsList.size()){
	        	return TYPE_GOGAL;
	        }else if(position ==  mGoalStaticsList.size() + 1){
	        	return TYPE_ASSIST_TITLE;
	        }else if(position > mGoalStaticsList.size() + 1){
	        	return TYPE_ASSIST;
	        }
	        return -1;
	    }
		
	    private Boolean isChinese(){
	    	
	    	Locale l = Locale.getDefault();  
	    	String language = l.getLanguage();  
	    	String country = l.getCountry().toLowerCase();  
	    	return language.equals("zh") && country.equals("cn");
	    }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LogHelper.d(TAG, "position" + String.valueOf(position));
			int type = getItemViewType(position);
			switch (type) {
			case TYPE_GOGAL_TITLE:
			case TYPE_ASSIST_TITLE:
				{
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
					if(type == TYPE_GOGAL_TITLE){
						holder.txtTypeName.setText(resource.getString(R.string.str_player_goal));
					}else if(type == TYPE_ASSIST_TITLE){
						holder.txtTypeName.setText(resource.getString(R.string.str_player_assist));
					}
				}
				break;
			case TYPE_GOGAL:
			case TYPE_ASSIST:
				{
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
					if(type == TYPE_GOGAL){
						PlayerStatistics goal = mGoalStaticsList.get(position-1);
						if(goal.getPosition() == 1){
							holder.imgFlag.setVisibility(View.VISIBLE);
						}else{
							holder.imgFlag.setVisibility(View.INVISIBLE);
						}
						holder.txtPalyerPos.setText(String.valueOf(goal.getPosition()));
						if(isChinese()){
							holder.txtPalyName.setText(goal.getPlayerName());
						}else{
							holder.txtPalyName.setText(goal.getPlayerEngName());
						}
						holder.txtCount.setText(String.valueOf(goal.getCount()));
						holder.txtTeamName.setText(controller.getTeamNationalName(goal.getPlayerTeamCode()));
						Drawable drawable= controller.getTeamNationalFlag(goal.getPlayerTeamCode());
						if(drawable != null){
							holder.imgTeamFlag.setImageDrawable(drawable);
							LogHelper.d(TAG, "DRAW FLAGE");
						}
					}else if(type == TYPE_ASSIST){
						PlayerStatistics assist = mAssistStaticsList.get(position-2-mGoalStaticsList.size());
						if(assist.getPosition() == 1){
							holder.imgFlag.setVisibility(View.VISIBLE);
						}else{
							holder.imgFlag.setVisibility(View.INVISIBLE);
						}
						holder.txtPalyerPos.setText(String.valueOf(assist.getPosition()));
						if(isChinese()){
							holder.txtPalyName.setText(assist.getPlayerName());
						}else{
							holder.txtPalyName.setText(assist.getPlayerEngName());
						}
						holder.txtCount.setText(String.valueOf(assist.getCount()));
						holder.txtTeamName.setText(controller.getTeamNationalName(assist.getPlayerTeamCode()));
						Drawable drawable= controller.getTeamNationalFlag(assist.getPlayerTeamCode());
						if(drawable != null){
							holder.imgTeamFlag.setImageDrawable(drawable);
						}
					}
				}
				break;
			default:
				return null;
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
