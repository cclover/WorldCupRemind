package com.cc.worldcupremind.view;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.common.LogHelper;
import com.cc.worldcupremind.logic.MatchDataController;
import com.cc.worldcupremind.model.GroupStatistics;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class GroupFragment extends BaseFragment {

	private static final String TAG = "GroupFragment";
	private ArrayList<GroupStatistics> mGroupStaticsList;
	private static final int ITEM_TYPE_TITLE = 0;
	private static final int ITEM_TYPE_TEAM = 1;	
	private static final int GROUP_TEAM_COUNT = 4;
	private static final int TITLE_SPAN = GROUP_TEAM_COUNT + 1;

	public GroupFragment(){
		mGroupStaticsList = null;
	}
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_group, container, false);
		progressBar = (ProgressBar)view.findViewById(android.R.id.progress);
		super.onCreateView(inflater, container, savedInstanceState); 
        return view;
    }
	
	public void setData(ArrayList<GroupStatistics> groupStaticsData) {
		
		LogHelper.d(TAG, "GroupFragment::setData");
		mGroupStaticsList = groupStaticsData;
		super.setAdapter();
		super.refresh();
	}
	
	@Override
	public BaseAdapter createAdapter() {
		return new GropStaticsListAdapter();
	}
	
	class GropStaticsListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (mGroupStaticsList == null) {
				return 0;
			}
			return mGroupStaticsList.size() + mGroupStaticsList.size()/GROUP_TEAM_COUNT;
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
		public int getViewTypeCount() {
			return 2;
		}
		
		@Override
		public int getItemViewType(int position) {
			if((position) % TITLE_SPAN == 0){
				return ITEM_TYPE_TITLE;
			}
			return ITEM_TYPE_TEAM;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LogHelper.d(TAG, "POSITION:"+position);
			if(getItemViewType(position) == ITEM_TYPE_TITLE){
				LogHelper.d(TAG, "ITEM_TYPE_TITLE");
				//Set title holder
				TextView txtGroup = null;
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.group_title_item, null);
					txtGroup = (TextView)convertView.findViewById(R.id.key_group);
					convertView.setTag(txtGroup);
				}else{
					txtGroup = (TextView)convertView.getTag();
				}
				
				//Set title value
				LogHelper.d(TAG, "ITEM_TYPE_TITLE SET VALUE");
				txtGroup.setText(String.format(getResources().getString(R.string.str_stage_group),GetTitleGroupCode(position)));
			}else{
				LogHelper.d(TAG, "ITEM_TYPE_TEAM");
				//Set team holder
				ViewHolderTeam holder = null;
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.group_statics_item, null);
					holder = new ViewHolderTeam();
					holder.flag = (ImageView)convertView.findViewById(R.id.flag);
					holder.value_team = (TextView)convertView.findViewById(R.id.value_team);
					holder.star = (ImageView)convertView.findViewById(R.id.star);
					holder.value_w = (TextView)convertView.findViewById(R.id.value_w);
					holder.value_d = (TextView)convertView.findViewById(R.id.value_d);
					holder.value_l = (TextView)convertView.findViewById(R.id.value_l);
					holder.value_gf = (TextView)convertView.findViewById(R.id.value_gf);
					holder.value_ga = (TextView)convertView.findViewById(R.id.value_ga);
					holder.value_pts = (TextView)convertView.findViewById(R.id.value_pts);
					convertView.setTag(holder);
				}else{
					holder = (ViewHolderTeam) convertView.getTag();
				}
				
				//Set value
				LogHelper.d(TAG, "ITEM_TYPE_TEAM SETVALUE");
				GroupStatistics item = getTeamInfo(position);
				Drawable drawable = controller.getTeamNationalFlag(item.getTeamCode());
				holder.flag.setImageDrawable(drawable);
				holder.value_team.setText(controller.getTeamNationalName(item.getTeamCode()));
				if(item.getPosition() <= 2 && item.getPoint() > 0){
					holder.star.setVisibility(View.VISIBLE);
				}else{
					holder.star.setVisibility(View.INVISIBLE);
				}
				holder.value_pts.setText(String.valueOf(item.getPoint()));
				holder.value_w.setText(String.valueOf(item.getWinCount()));
				holder.value_d.setText(String.valueOf(item.getDrawCount()));
				holder.value_l.setText(String.valueOf(item.getLoseCount()));
				holder.value_gf.setText(String.valueOf(item.getGoalFor()));
				holder.value_ga.setText(String.valueOf(item.getGoalAgainst()));
				LogHelper.d(TAG, "ITEM_TYPE_TEAM SETVALUEDONE");
			}
			return convertView;
		}
		
		
		/**
		 * Get tht tilte group name by pos
		 */
		private String GetTitleGroupCode(int pos){
			
			int index = pos / TITLE_SPAN;
			switch(index){
				case 0:
					return "A";
				case 1:
					return "B";
				case 2:
					return "C";
				case 3:
					return "D";
				case 4:
					return "E";
				case 5:
					return "F";
				case 6:
					return "G";
				case 7:
					return "H";
				default:
					return "";
			}
		}
		
		/**
		 * Get the team by position
		 */
		private GroupStatistics getTeamInfo(int position) {
			
			int pos = position - (position/TITLE_SPAN +1) ; //pos in mGroupStaticsList
			
			int p = 0;
			int start = 0;
			int end = 0;
			if (pos >= 0 && pos <= 3) {
				start = 0;
				end = 3;
				p = pos + 1;
			} else if (pos >= 4 && pos <= 7) {
				start = 4;
				end = 7;
				p = pos - 3;
			} else if (pos >= 8 && pos <= 11) {
				start = 8;
				end = 11;
				p = pos - 7;
			} else if (pos >= 12 && pos <= 15) {
				start = 12;
				end = 15;
				p = pos - 11;
			} else if (pos >= 16 && pos <= 19) {
				start = 16;
				end = 19;
				p = pos - 15;
			} else if (pos >= 20 && pos <= 23) {
				start = 20;
				end = 23;
				p = pos - 19;
			} else if (pos >= 24 && pos <= 27) {
				start = 24;
				end = 27;
				p = pos - 23;
			} else if (pos >= 28 && pos <= 31) {
				start = 28;
				end = 31;
				p = pos - 27;
			}

			for (int i = start; i <= end; i++) {
				GroupStatistics item = mGroupStaticsList.get(i);
				if (item.getPosition() == p) {
					return item;
				}
			}
			return null;
		}
	}
		
	
	class ViewHolderTeam{
		ImageView flag;
		TextView value_team;
		ImageView star;
		TextView value_w;
		TextView value_d;
		TextView value_l;
		TextView value_gf;
		TextView value_ga;
		TextView value_pts;
	}
}
