package com.cc.worldcupremind.view;

import java.util.ArrayList;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.logic.MatchDataController;
import com.cc.worldcupremind.model.PlayerStatistics;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StatisticsFragment extends ListFragment {

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
			}
			if(mAssistStaticsList != null){
				assist = mAssistStaticsList.size();
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
	        }else if(position ==  mGoalStaticsList.size() + 2){
	        	return TYPE_ASSIST_TITLE;
	        }else if(position > mGoalStaticsList.size() + 2){
	        	return TYPE_ASSIST;
	        }
	        return -1;
	    }
		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
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
					    holder.type = (TextView)convertView.findViewById(R.id.txtType);
					    convertView.setTag(holder);
					} else {
					     holder = (HolderViewTitle)convertView.getTag();
					}
					
					//Set value
					
				}
				break;
			case TYPE_GOGAL:
			case TYPE_ASSIST:
				{
					//Get view
					HolderView holder = null;
					if (convertView == null) {
						convertView = mInflater.inflate(R.layout.statistics_type_item, null);
						holder = new HolderView();
					    holder.name = (TextView)convertView.findViewById(R.id.txtName);
					    holder.count = (TextView)convertView.findViewById(R.id.txtCount);
					    convertView.setTag(holder);
					} else {
					     holder = (HolderView)convertView.getTag();
					}
					
					//Set value
				}
				break;
			default:
				break;
			}
			return convertView;
		}
		
		class HolderView{
			TextView name;
			TextView count;
		}
		
		class HolderViewTitle{
			TextView type;
		}
		
	}

}
