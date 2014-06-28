package com.cc.worldcupremind.view;

import java.util.ArrayList;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.common.AdsHelper;
import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.common.ResourceHelper;
import com.cc.worldcupremind.model.MatchDate;
import com.cc.worldcupremind.model.MatchStage;
import com.cc.worldcupremind.model.MatchStatus;
import com.cc.worldcupremind.model.MatchesModel;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
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
		progressBar = (ProgressBar)view.findViewById(R.id.progress_load);
		listView = (ListView)view.findViewById(android.R.id.list);
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
		int titleIndex = 0;
		if(isOn){
			// Build the remind list
			remindList.clear();
			for(int i = 0; i < matchDataList.size(); i++){
				MatchesModel model = matchDataList.get(i);
				
				//Skip the ITEM_DAY
				if(model.getMatchNo() > matchList.size()){
					if(startIndex == 0){
						titleIndex = i;
					}
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
		
		//Scroll the list  position to the start index.
		final int index = titleIndex;
		if(index > 0 && listView != null && 
				listView.getFirstVisiblePosition() < index){
			LogHelper.d(TAG, "Scorll to the startIndex:" + index);
			listView.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					listView.setSelected(false);
					listView.setSelection(index);
					listView.setSelected(true);
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
	
	public void scrollToSuitMatch(){
	
		LogHelper.d(TAG, "scrollToSuitMatch");

		//find the first match
		int pos = 0;
		int titlePos = 0;
		for(int i = 0; i < matchDataList.size(); i++){
			MatchesModel model = matchDataList.get(i);
			//handle the match item
			if(model.getMatchNo() < matchList.size()){
				if(!model.getMatchTime().isStart()){
					pos = i;
					break;
				}
			}else{
				titlePos = i;
			}
		}

		//Set position
		final int index = titlePos;
		if(listView != null){
			listView.postDelayed(new Runnable() {
	
	
				@Override
				public void run() {
					listView.setSelected(false);
					listView.setSelection(index);
					listView.setSelected(true);
				}
			}, 100);
		}else{
			LogHelper.d(TAG, "listView is null");
		}
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
	
		private ItemClickListener listener;
		
		public MatchesAdapter(){
			listener = new ItemClickListener();
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
				     holder.ninetyScore = (TextView)convertView.findViewById(R.id.txtNinetyScore);
				     holder.info = (TextView)convertView.findViewById(R.id.txtInfo);
				     holder.team2 = (TextView)convertView.findViewById(R.id.txtTeam2);
				     holder.flag2 = (ImageView)convertView.findViewById(R.id.imgFlag2);
				     holder.remind = (CheckBox)convertView.findViewById(R.id.chkRemind);
				     holder.imgRemind = (ImageView)convertView.findViewById(R.id.imgRemind);
				     holder.flag1.setOnClickListener(listener);
				     holder.flag2.setOnClickListener(listener);
				     holder.info.setOnClickListener(listener); 
				     convertView.setTag(holder);
				} else {
				     holder = (ViewHolder)convertView.getTag();
				}
				
				//Set value
				//Match Stage Name
				if(model.getMatchStage() == MatchStage.STAGE_GROUP){
					holder.group.setText(String.format(resource.getString(R.string.str_stage_group),  model.getGroupName()));
				}else{
					holder.group.setText(resource.getString(model.getMatchStage().getStringResourceID()));
				}
	
				//National name and flag
				holder.team1.setText(controller.getTeamNationalName(model.getTeam1Code()));
				Drawable drawable1= controller.getTeamNationalFlag(model.getTeam1Code());
				if(drawable1 != null){
					holder.flag1.setImageDrawable(drawable1);
					holder.flag1.setTag(model.getTeam1Code());
				}
				holder.team2.setText(controller.getTeamNationalName(model.getTeam2Code()));
				Drawable drawable2 = controller.getTeamNationalFlag(model.getTeam2Code());
				if(drawable2 != null){
					holder.flag2.setImageDrawable(drawable2);
					holder.flag2.setTag(model.getTeam1Code());
				}
				
				//Match time or score
				if(model.getMatchStatus() == MatchStatus.MATCH_STATUS_WAIT_START){
					holder.score.setText(model.getMatchTime().getTimeString());
					holder.score.setTextColor(resource.getColor(R.color.gray));
				}else{
					holder.score.setText(String.format("%d:%d", model.getTeam1Score(), model.getTeam2Score()));
					holder.score.setTextColor(resource.getColor(R.color.score));
				}
				if(model.getIsPen()){
					holder.ninetyScore.setText(String.format("(%d:%d)", model.getNinetyScore1(), model.getNinetyScore2()));
					holder.ninetyScore.setVisibility(View.VISIBLE);
				}else{
					holder.ninetyScore.setVisibility(View.GONE);
				}
				
				//winner text color
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
				
				//show the news and video info only for current matches item 
			    holder.info.setTag(position);
			    if(controller.getMatchStage().getStageValue() >= model.getMatchStage().getStageValue()
			    		&& model.getExtInfo().length() > 0)
			    {
				    Drawable infoFlag = null;
					if(model.getMatchStatus() == MatchStatus.MATCH_STATUS_OVER){
				    	if(model.getExtType() == MatchesModel.EXT_TYPE_NEWS){ //not set video url
							holder.info.setText(R.string.str_info_match);
							infoFlag = resource.getDrawable(R.drawable.ic_match_over);
				    	}else if(model.getExtType() == MatchesModel.EXT_TYPE_VIDEO){//set video url
							holder.info.setText(R.string.str_info_video);
							infoFlag = resource.getDrawable(R.drawable.ic_match_video);
				    	}
				    	holder.info.setTextColor(resource.getColor(R.color.score));
					}else{
						if(model.getMatchTime().isOver()){//match over but not update data
							holder.info.setText(R.string.str_info_match);
							infoFlag = resource.getDrawable(R.drawable.ic_match_over);
							holder.info.setTextColor(resource.getColor(R.color.score));
						}else if(model.getMatchTime().isPlaying()){ // playing
							holder.info.setText(R.string.str_info_live);
							infoFlag = resource.getDrawable(R.drawable.ic_match_live);
							holder.info.setTextColor(resource.getColor(R.color.score));
						}else if(model.getMatchTime().isPlayingSoon()){
							holder.info.setText(R.string.str_info_news);
							infoFlag = resource.getDrawable(R.drawable.ic_match_before);
							holder.info.setTextColor(resource.getColor(R.color.gray));
						}
					}
					if(infoFlag != null){
						int flagSize = ResourceHelper.dip2px(context,20);
						infoFlag.setBounds(0, 0, flagSize, flagSize);  
						holder.info.setCompoundDrawables(infoFlag, null, null, null);
						holder.info.setVisibility(View.VISIBLE);
					}else{
						holder.info.setVisibility(View.GONE);
					}
					
			    }else{
			    	holder.info.setVisibility(View.GONE);
			    }
				
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
		

		private void openNewsAndVideo(MatchesModel match){
			
			LogHelper.d(TAG, "openNewsAndVideo");
			String url = match.getExtInfo();
			if(url.length() > 0){

				Intent intent = new Intent(Intent.ACTION_VIEW); 
				//show ads
				((MainActivity)getActivity()).showWidgetAds();
				
				//open infi
				if(match.getExtType() == MatchesModel.EXT_TYPE_NEWS){
					//Show news
					intent.setData(Uri.parse(url));
					context.startActivity(intent);
				}else if(match.getExtType() == MatchesModel.EXT_TYPE_VIDEO){
					
					//show video
					intent.setDataAndType(Uri.parse(url), "video/mp4");  //Open video
					try{
						context.startActivity(intent); 
					}catch(ActivityNotFoundException e){
						LogHelper.e(TAG, e);
						//If no app can play it. open the browser to download
						intent.setData(Uri.parse(url));
						context.startActivity(intent);
					}
				}
			}
		}
		
		class ItemClickListener implements View.OnClickListener{

			@Override
			public void onClick(View v) {
				if(v.getId() == R.id.txtInfo){
				
					int pos = Integer.parseInt(v.getTag().toString());
					final MatchesModel match = matchDataList.get(pos);
					if(match == null){
						LogHelper.w(TAG, "match not exist");
						return;
					}
					//check need alert
					if(controller.needAlertWhenPlayVideo() && match.getExtType() == MatchesModel.EXT_TYPE_VIDEO){
						
						//Add check box
						LinearLayout layout = new LinearLayout(context);
						LinearLayout.LayoutParams params 
							= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
						params.leftMargin = ResourceHelper.dip2px(context,10);
						params.gravity = Gravity.CENTER_VERTICAL;
						final CheckBox box = new CheckBox(context);
						box.setText(R.string.str_viedo_checkbox);
						box.setTextColor(resource.getColor(R.color.gray));
						layout.addView(box, params);
						
						//Show alert box
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						builder.setTitle(R.string.str_viedo_alert_title);
						builder.setMessage(R.string.str_viedo_alert_message);
						builder.setIcon(R.drawable.ic_match_video);
						builder.setView(layout);
						builder.setPositiveButton(R.string.str_viedo_button, new OnClickListener() {	
							@Override
							public void onClick(DialogInterface dialog, int which) {
								openNewsAndVideo(match);
								if(box.isChecked()){
									controller.setVideoAlert(false);
								}
							}
						});
						builder.setNegativeButton(android.R.string.cancel, null);
						builder.show();
					}else{
						openNewsAndVideo(match);
					}
				}else if(v.getId() == R.id.imgFlag1 || v.getId() == R.id.imgFlag2){
					
					//Get url
					String teamCode = v.getTag().toString();
					if(teamCode == null || teamCode.length() == 0){
						return;
					}
					
					//Visit url
					String url = controller.getTeamURL(teamCode);
					if(url.length() > 0){
						Intent intent = new Intent(Intent.ACTION_VIEW); 
						intent.setData(Uri.parse(url));
						context.startActivity(intent);
						
						//show ads
						((MainActivity)getActivity()).showWidgetAds();
					}
				}
			}	
		}
		
		class ViewHolder{
			TextView group;
			ImageView flag1;
			TextView team1;
			TextView ninetyScore;
			TextView score;
			TextView info;
			TextView team2;
			ImageView flag2;
			CheckBox remind;
			ImageView imgRemind;
		}
	}
}
