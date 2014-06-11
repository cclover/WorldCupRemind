package com.cc.worldcupremind.view;

import java.util.ArrayList;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.logic.MatchDataController;
import com.cc.worldcupremind.model.MatchDate;
import com.cc.worldcupremind.model.MatchStage;
import com.cc.worldcupremind.model.MatchStatus;
import com.cc.worldcupremind.model.MatchesModel;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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
import android.widget.TextView;

public class MatchesFragment extends ListFragment implements View.OnClickListener{
	
	private static final String TAG = "MatchesFragment";
	private MatchesAdapter adapter;  
	private Context context;
	private SparseArray<MatchesModel> matchList;
	private ArrayList<MatchesModel> matchDataList;
	private ArrayList<Integer> remindList;
	private MatchDataController controller;
	private LayoutInflater mInflater;
	private Resources resource;
	private Boolean isAlarmMode;
	
	private LinearLayout remindFooterLayout;
	private Button btnConfitm;
	private Button btnCancel;
			
	public MatchesFragment(){
        matchDataList = new ArrayList<MatchesModel>();
        adapter = new MatchesAdapter();  
        remindList = new ArrayList<Integer>();
        isAlarmMode = false;
	}
	
	public void setData(SparseArray<MatchesModel> list){
		LogHelper.d(TAG, "setData()");
		matchList = list;
		createMatchesDayMap();
		adapter.refresh();
	}
	
	public void setAlarmMode(Boolean isOn){
		if(isAlarmMode == isOn){
			Log.w(TAG, "setAlarmMode In same mode");
			return;
		}
		isAlarmMode = isOn;
		if(isOn){
			remindList.clear();
			for(int i = 0; i < matchList.size(); i++){
				MatchesModel model = matchList.valueAt(i);
				if(model.getIsRemind()){
					remindList.add(model.getMatchNo());
				}
			}
		}
		setFootVisibility(isOn);
		adapter.refresh();
	}
	
	public void refresh(){
		adapter.refresh();
	}
	
	public void refreshData(){
		createMatchesDayMap();
		adapter.refresh();
	}
	
	public void setFootVisibility(Boolean isShow){
		if(isShow){
			remindFooterLayout.setVisibility(View.VISIBLE);
		}else{
			remindFooterLayout.setVisibility(View.GONE);
		}
	}
	
	/**
	 * @return the remindList
	 */
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
		String dayNext = "";
		
		//Get the same day count
		for(int i = 1; i < matchList.size(); i++){
	
			matchDataList.add(matchList.valueAt(i-1));
			String day = matchList.valueAt(i-1).getMatchTime().getDateString();
			dayNext = matchList.valueAt(i).getMatchTime().getDateString();
			if(day.equals(dayNext)){
				count++;
			}else{
//				LogHelper.d(TAG, day + "count:" + String.valueOf(count));
				tmpList.add(count);
				count = 1;
			}
		}
//		LogHelper.d(TAG, dayNext + "count:" + String.valueOf(count));
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
		LogHelper.d(TAG, "matchDataList size = " + String.valueOf(matchDataList.size()));
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LogHelper.d(TAG, "MatchFragment onCreateView");
		View view = inflater.inflate(R.layout.fragment_matches, container, false);
		remindFooterLayout = (LinearLayout)view.findViewById(R.id.remindFooter);
		btnConfitm = (Button)view.findViewById(R.id.btnConfirm);
		btnConfitm.setOnClickListener(this);
		btnCancel = (Button)view.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(this);
		setListAdapter(adapter);    
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
    public void onCreate(Bundle savedInstanceState) {  
		
		LogHelper.d(TAG, "MatchFragment onCreate");
        super.onCreate(savedInstanceState);  
        
        controller = MatchDataController.getInstance();
		context = this.getActivity();
		resource = context.getResources();
        mInflater = LayoutInflater.from(context);
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
						LogHelper.d(TAG, "Unselect mathc " + String.valueOf(matchNo));
					}
				}else{
					box.setChecked(true);
					if(remindList.add(matchNo)){
						LogHelper.d(TAG, "Select mathc " + String.valueOf(matchNo));
					}
				}
			}
		}
	}
	
	class MatchesAdapter extends BaseAdapter{
	
		public MatchesAdapter(){
			
		}
		
		public void refresh(){
			notifyDataSetChanged();
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
			return null;
		}

		@Override
		public long getItemId(int position) {
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
				if(model.getMatchStatus() == MatchStatus.MATCH_STATUS_WAIT_START){
					holder.score.setText(model.getMatchTime().getTimeString());
				}else{
					holder.score.setText(String.format("%d:%d", model.getTeam1Score(), model.getTeam2Score()));
				}
				
				//only show the remind image or checkbox when game not start
				if(model.getMatchStatus() != MatchStatus.MATCH_STATUS_WAIT_START || model.getMatchTime().isStart()){
					holder.remind.setVisibility(View.GONE);
					holder.imgRemind.setVisibility(View.GONE);
				}else{
					if(model.getIsRemind()){
						if(!isAlarmMode){
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
		
		class ViewHolder2{
			TextView day;
		}
		
	}

}
