package com.cc.worldcupremind.view;

import java.util.ArrayList;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.common.ResourceHelper;
import com.cc.worldcupremind.model.MatchDate;
import com.cc.worldcupremind.model.MatchStage;
import com.cc.worldcupremind.model.MatchStatus;
import com.cc.worldcupremind.model.MatchesModel;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MatchesFragment extends BaseFragment implements View.OnClickListener{
	
	private static final String TAG = "MatchesFragment";
	private static final int ITEM_DAY = 0;
	private static final int ITEM_MATCH = 1;
	private SparseArray<MatchesModel> matchList;
	private ArrayList<MatchesModel> matchDataList;
	private ArrayList<Integer> remindList;
	private Boolean isAlarmMode;
	private LinearLayout remindFooterLayout;
	private Button btnConfitm;
	private Button btnCancel;
			
	public MatchesFragment(){
		
        matchDataList = new ArrayList<MatchesModel>();
        remindList = new ArrayList<Integer>();
        isAlarmMode = false;
        remindFooterLayout = null;
        btnConfitm = null;
        btnCancel = null;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_matches, container, false);
		remindFooterLayout = (LinearLayout)view.findViewById(R.id.remindFooter);
		btnConfitm = (Button)view.findViewById(R.id.btnConfirm);
		btnCancel = (Button)view.findViewById(R.id.btnCancel);
		btnConfitm.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		progressBar = (ProgressBar)view.findViewById(android.R.id.progress);
		super.onCreateView(inflater, container, savedInstanceState); 
        return view;
    }
	
	@Override
	public void onClick(View v) {
		
		if(v.getId() == R.id.btnConfirm){

			if(!controller.setMatchRemind(remindList)){
				LogHelper.w(TAG, "Can't setMatchRemind");
			}
		}else if(v.getId() == R.id.btnCancel){
		}
		setAlarmMode(false);
	}
	

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {  
		super.onListItemClick(l, v, position, id); 

		if(isAlarmMode && matchDataList.get(position).getMatchNo() != 0){
			CheckBox box = (CheckBox)v.findViewById(R.id.chkRemind);
			if(box != null){
				int matchNo = matchDataList.get(position).getMatchNo();
				if(box.isChecked()){
					box.setChecked(false);
					if(remindList.remove((Object)matchNo)){
						LogHelper.d(TAG, "Unselect match " + String.valueOf(matchNo));
					}
				}else{
					box.setChecked(true);
					if(remindList.add(matchNo)){
						LogHelper.d(TAG, "Select match " + String.valueOf(matchNo));
					}
				}
			}
		}
	}
	
	public void setData(SparseArray<MatchesModel> list){
		
		LogHelper.d(TAG, "MatchesFragment::setData");
		matchList = list;
		createMatchesDayMap();
		super.setAdapter();
		super.refresh();
	}
	
	@Override
	public BaseAdapter createAdapter() {
		return new MatchesAdapter();
	}
	
	public void setAlarmMode(Boolean isOn){
		
		LogHelper.d(TAG, "MatchesFragment::setAlarmMode");
		if(isAlarmMode == isOn){
			Log.w(TAG, "setAlarmMode In same mode");
			return;
		}
		isAlarmMode = isOn;
		int startIndex = 0;
		if(isOn){
			// Build the remind list
			remindList.clear();
			for(int i = 0; i < matchDataList.size(); i++){
				MatchesModel model = matchDataList.get(i);
				
				//Skip the ITEM_DAY
				if(model.getMatchNo() > matchList.size()){
					continue;
				}
				
				if(model.getIsRemind()){
					remindList.add(model.getMatchNo());
				}
				
				//Get the first match index which can set the alarm
				if(startIndex == 0 && (!model.getMatchTime().isStart() ||
						model.getMatchStatus() == MatchStatus.MATCH_STATUS_WAIT_START)){
					startIndex = i;
				}
			}
			LogHelper.d(TAG, String.format("Visible index is %d, must index is %d",
					getListView().getFirstVisiblePosition(), startIndex));
		}
		setFootVisibility(isOn);
		super.refresh();
		
		//Scoll the list  position to the start index.
		final int index = startIndex-1;
		if(index > 0 && getListView().getFirstVisiblePosition() < index){
			LogHelper.d(TAG, "Scorll to the startIndex:" + index);
			getListView().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					getListView().setSelected(false);
					getListView().setSelection(index);
					getListView().setSelected(true);
				}
			}, 100);
		}
	}
	
	public void setFootVisibility(Boolean isShow){
		
		LogHelper.d(TAG, "MatchesFragment::setFootVisibility");
		if(isShow){
			remindFooterLayout.setVisibility(View.VISIBLE);
		}else{
			remindFooterLayout.setVisibility(View.GONE);
		}
	}

	
	public ArrayList<Integer> getRemindList() {
		return remindList;
	}
	
	private void createMatchesDayMap(){
		
		LogHelper.d(TAG, "createMatchesDayMap");
		ArrayList<Integer> tmpList = new ArrayList<Integer>();
		ArrayList<Integer> dayIndexList = new ArrayList<Integer>();
		dayIndexList.clear();
		matchDataList.clear();
		int count = 1;
		
		//Get the same day count
		for(int i = 1; i < matchList.size(); i++){
			
			//Tune: if we compare the getMatchTime().getDateString(), on some device, it's cost 5m-8ms (X920E), 2ms-3ms(G7)
			matchDataList.add(matchList.valueAt(i-1));
			MatchDate day = matchList.valueAt(i-1).getMatchTime();
			MatchDate dayNext = matchList.valueAt(i).getMatchTime();
			if(day.isSameDay(dayNext)){
				count++;
			}else{
//				LogHelper.d(TAG, day + "count:" + String.valueOf(count));
				tmpList.add(count);
				count = 1;
			}
		}
//		LogHelper.d(TAG, dayNext + "count:" + String.valueOf(count));
		if(matchList.size() >= 1){
			matchDataList.add(matchList.valueAt(matchList.size()-1));
			tmpList.add(count);
		}
		
		//Count the day index
		for(int j = 0; j < tmpList.size(); j++){
			if(j == 0){
				dayIndexList.add(0);
			}else{
				dayIndexList.add(tmpList.get(j-1) + dayIndexList.get(j-1) + 1);
			}
		}
		
		//renew the matchDataList with day info
		int dyaID = matchList.size();
		for(int k : dayIndexList){
			MatchDate date = matchDataList.get(k).getMatchTime();
			MatchesModel model = new MatchesModel(++dyaID, null, null, date, null, null, null, 0, 0, null);
			matchDataList.add(k, model);
		}
		LogHelper.d(TAG, "matchDataList size = " + String.valueOf(matchDataList.size()));
	}
	

	class MatchesAdapter extends BaseAdapter{
	
		public MatchesAdapter(){
			
		}

		@Override
		public int getCount() {

			if(matchDataList == null){
				return 0;
			}
//			LogHelper.d(TAG, "getCount--" + String.valueOf(matchDataList.size()));
			return matchDataList.size();
		}

		@Override
		public Object getItem(int position) {
			return matchDataList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return matchDataList.get(position).getMatchNo();
		}

		@Override
	    public int getItemViewType(int position) {
	        if(matchDataList.get(position).getMatchNo() > matchList.size()){
	        	return ITEM_DAY;
	        }else{
	        	return ITEM_MATCH;
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
			if(getItemViewType(position) == ITEM_DAY){
				
				TextView txtDay = null;
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.day_item, null);
					txtDay = (TextView)convertView.findViewById(R.id.txtDay);
				    convertView.setTag(txtDay);
				} else {
					txtDay = (TextView)convertView.getTag();
				}
				
				//set value
				txtDay.setText(String.format("%s %s", model.getMatchTime().getDateString(), model.getMatchTime().getWeekdayString()));
				if(model.getMatchTime().isWeekend()){
					convertView.setBackgroundColor(resource.getColor(R.color.lightsalmon));
				}else{
					convertView.setBackgroundColor(resource.getColor(R.color.gainsboro));
				}
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
				     holder.imgRemind = (ImageView)convertView.findViewById(R.id.imgRemind);
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
				Drawable flag = null;
				if(model.getMatchStatus() == MatchStatus.MATCH_STATUS_WAIT_START){
					holder.score.setText(model.getMatchTime().getTimeString());
					holder.score.setTextColor(resource.getColor(R.color.gray));
					flag = resource.getDrawable(R.drawable.ic_match_wait);
				}else{
					holder.score.setText(String.format("%d:%d", model.getTeam1Score(), model.getTeam2Score()));
					holder.score.setTextColor(resource.getColor(R.color.score));
					flag = resource.getDrawable(R.drawable.ic_match_over);
				}
				int px = ResourceHelper.dip2px(context,10);
				flag.setBounds(0, 0, px, px);  
				if(model.getTeam1Score() > model.getTeam2Score()){
					holder.team1.setTextColor(Color.RED);
					holder.team2.setTextColor(Color.BLACK);
				}else if(model.getTeam1Score() < model.getTeam2Score()){
					holder.team1.setTextColor(Color.BLACK);
					holder.team2.setTextColor(Color.RED);
				}else{
					if(model.getMatchStatus() == MatchStatus.MATCH_STATUS_WAIT_START){
						holder.team1.setTextColor(Color.GRAY);
						holder.team2.setTextColor(Color.GRAY);
					}else{
						holder.team1.setTextColor(Color.BLACK);
						holder.team2.setTextColor(Color.BLACK);
					}
				}
				holder.score.setCompoundDrawables(null, flag, null, null);

				//only show the remind image or checkbox when game not start
				if(model.getMatchStatus() != MatchStatus.MATCH_STATUS_WAIT_START || model.getMatchTime().isStart()){
					holder.remind.setVisibility(View.GONE);
					holder.imgRemind.setVisibility(View.GONE);
				}else{
					
					//Set remind image					
					if(model.getIsRemind()){
						if(!isAlarmMode){
							if(controller.isRemindEnable()){
								holder.imgRemind.setBackgroundResource(R.drawable.ic_audio_alarm);
							}else{
								holder.imgRemind.setBackgroundResource(R.drawable.ic_audio_alarm_muted);
							}
							holder.imgRemind.setVisibility(View.VISIBLE);
						}else{
							holder.imgRemind.setVisibility(View.GONE);
						}
					}else{
						holder.imgRemind.setVisibility(View.GONE);
					}
					holder.remind.setChecked(remindList.contains(model.getMatchNo()));
					if(isAlarmMode){
						holder.remind.setVisibility(View.VISIBLE);
					}else{
						holder.remind.setVisibility(View.GONE);
					}
				}
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
			ImageView imgRemind;
		}
	}
}
