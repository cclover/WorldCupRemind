package com.cc.worldcupremind.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.R.id;
import com.cc.worldcupremind.R.layout;
import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.logic.MatchDataController;
import com.cc.worldcupremind.model.MatchDate;
import com.cc.worldcupremind.model.MatchStage;
import com.cc.worldcupremind.model.MatchStatus;
import com.cc.worldcupremind.model.MatchesModel;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Pair;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MatchesFragment extends ListFragment {
	
	private static final String TAG = "MatchesFragment";
	private ListView matchesList;  
	private MatchesAdapter adapter;  
	private Context context;
	private SparseArray<MatchesModel> matchList;
	private ArrayList<MatchesModel> matchDataList;
	private MatchDataController controller;
	private LayoutInflater mInflater;
	private Resources resource;
			
	public MatchesFragment(){
        adapter = new MatchesAdapter();  
        matchDataList = new ArrayList<MatchesModel>();
	}
	
	public void setData(SparseArray<MatchesModel> list){
		matchList = list;
		createMatchesDayMap();
		adapter.refresh();
	}
	
	private void createMatchesDayMap(){
		
		
		ArrayList<Integer> tmpList = new ArrayList<Integer>();
		ArrayList<Integer> dayIndexList = new ArrayList<Integer>();
		dayIndexList.clear();
		int count = 1;
		String dayNext = "";
		
		//Get the same day count
		for(int i = 1; i < matchList.size(); i++){
	
			matchDataList.add(matchList.valueAt(i-1));
			String day = matchList.valueAt(i-1).getMatchTime().getDateString();
			dayNext = matchList.valueAt(i).getMatchTime().getDateString();
			if(day.equals(dayNext)){
				count++;
			}else{
				LogHelper.d(TAG, day + "count:" + String.valueOf(count));
				tmpList.add(count);
				count = 1;
			}
		}
		LogHelper.d(TAG, dayNext + "count:" + String.valueOf(count));
		matchDataList.add(matchList.valueAt(matchList.size()-1));
		tmpList.add(count);
		
		//Count the day index
		for(int j = 0; j < tmpList.size(); j++){
			if(j == 0){
				dayIndexList.add(0);
			}else{
				dayIndexList.add(tmpList.get(j-1) + dayIndexList.get(j-1) + 1);
			}
		}
		
		//renew the matchDataList with day info
		for(int k : dayIndexList){
			MatchDate date = matchDataList.get(k).getMatchTime();
			MatchesModel model = new MatchesModel(0, null, null, date, null, null, null, 0, 0, null);
			matchDataList.add(k, model);
		}

	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matches, container, false);
        matchesList = (ListView) view.findViewById(android.R.id.list);  
        setListAdapter(adapter);    
        return view;
    }
	
	
	@Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        
        controller = MatchDataController.getInstance();
		context = this.getActivity();
		resource = context.getResources();
        mInflater = LayoutInflater.from(context);
    }  

	
	class MatchesAdapter extends BaseAdapter{
		
		public MatchesAdapter(){

		}
		
		public void refresh(){
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {

			if(matchDataList == null)
				return 0;
			return matchDataList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
	    public int getItemViewType(int position) {
	        if(matchDataList.get(position).getMatchNo() == 0){
	        	return 0;
	        }else{
	        	return 1;
	        }
	    }

		@Override
	    public int getViewTypeCount() {
	        return 2;
	    }
	    
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			MatchesModel model = matchDataList.get(position);
			
			//Get item view
			if(model.getMatchNo() == 0){
				
				ViewHolder2 holder = null;
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.day_item, null);
					holder = new ViewHolder2();
				    holder.day = (TextView)convertView.findViewById(R.id.txtDay);
				    convertView.setTag(holder);
				} else {
				     holder = (ViewHolder2)convertView.getTag();
				}
				
				//set value
				holder.day.setText(String.format("%s %s", model.getMatchTime().getDateString(), model.getMatchTime().getWeekdayString()));
				
			}else{
			
				ViewHolder holder = null;
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.matches_item, null);
				     holder = new ViewHolder();
				     holder.group = (TextView)convertView.findViewById(R.id.txtGroup);
				     holder.flag1 = (ImageView)convertView.findViewById(R.id.imgFlag1);
				     holder.team1 = (TextView)convertView.findViewById(R.id.txtTeam1);
				     holder.score = (TextView)convertView.findViewById(R.id.txtScore);
				     holder.team2 = (TextView)convertView.findViewById(R.id.txtTeam2);
				     holder.flag2 = (ImageView)convertView.findViewById(R.id.imgFlag2);
				     holder.remind = (CheckBox)convertView.findViewById(R.id.chkRemind);
				     convertView.setTag(holder);
				} else {
				     holder = (ViewHolder)convertView.getTag();
				}
				
				//Set value
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
				holder.remind.setChecked(model.getIsRemind());
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
			CheckBox remind;
		}
		
		class ViewHolder2{
			TextView day;
		}
		
	}

}
