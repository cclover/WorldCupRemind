package com.cc.worldcupremind.view;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.logic.MatchDataController;
import com.cc.worldcupremind.model.GroupStatistics;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class GroupFragment extends ListFragment {

	private ArrayList<GroupStatistics> mGroupStaticsList;
	private GropStaticsListAdapter mAdapter;
	private LayoutInflater mInflater;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mInflater = LayoutInflater.from(getActivity());
		mAdapter = new GropStaticsListAdapter();
		setListAdapter(mAdapter);
		this.getListView().setSelector(new ColorDrawable(Color.TRANSPARENT)); 
	}

	public void setData(ArrayList<GroupStatistics> groupStaticsData) {
		mGroupStaticsList = groupStaticsData;
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Button footView = new Button(getActivity());
		footView.setText(getResources().getString(R.string.str_konckout_match));
		footView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),
						KonckoutMatchActivity.class);
				startActivity(intent);
			}
		});
		getListView().addFooterView(footView);
	}

	class GropStaticsListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (mGroupStaticsList == null) {
				return 0;
			}
			return mGroupStaticsList.size();
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
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.group_statics_item,
						parent, false);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();
			if (holder == null) {
				holder = new ViewHolder();
				holder.initViews(convertView);
				convertView.setTag(holder);
			}
			holder.setupViews(position);
			return convertView;
		}

		class ViewHolder {
			LinearLayout title;
			ImageView flag;
			TextView key_group;
			TextView key_w;
			TextView key_d;
			TextView key_l;
			TextView key_gf;
			TextView key_ga;
			TextView key_pts;
			TextView value_team;
			TextView value_w;
			TextView value_d;
			TextView value_l;
			TextView value_gf;
			TextView value_ga;
			TextView value_pts;
			ImageView star;

			void setupViews(int position) {
				GroupStatistics item = getTeamInfo(position);
				value_team.setText(MatchDataController.getInstance()
						.getTeamNationalName(item.getTeamCode()));

				Drawable drawable = MatchDataController.getInstance()
						.getTeamNationalFlag(item.getTeamCode());
				flag.setImageDrawable(drawable);
				value_w.setText(String.valueOf(item.getWinCount()));
				value_d.setText(String.valueOf(item.getDrawCount()));
				value_l.setText(String.valueOf(item.getLoseCount()));
				value_gf.setText(String.valueOf(item.getGoalFor()));
				value_ga.setText(String.valueOf(item.getGoalAgainst()));
				value_pts.setText(String.valueOf(item.getPoint()));
				if (item.getTeamGroup().equals(
						mGroupStaticsList.get(position > 1 ? position - 1 : 0)
								.getTeamGroup())) {
					title.setVisibility(View.GONE);
				} else {
					title.setVisibility(View.VISIBLE);
					key_group.setText(String.format(
							getResources().getString(R.string.str_stage_group),
							item.getTeamGroup()));
				}
				if (position == 0) {
					title.setVisibility(View.VISIBLE);
					key_group.setText(String.format(
							getResources().getString(R.string.str_stage_group),
							item.getTeamGroup()));
				}
				if(item.getPosition() <= 2){
					star.setVisibility(View.VISIBLE);
				}else{
					star.setVisibility(View.INVISIBLE);
				}
			}

			public void initViews(View convertView) {
				key_group = (TextView) convertView.findViewById(R.id.key_group);
				flag = (ImageView) convertView.findViewById(R.id.flag);
				key_w = (TextView) convertView.findViewById(R.id.key_w);
				key_d = (TextView) convertView.findViewById(R.id.key_d);
				key_l = (TextView) convertView.findViewById(R.id.key_l);
				key_gf = (TextView) convertView.findViewById(R.id.key_gf);
				key_ga = (TextView) convertView.findViewById(R.id.key_ga);
				key_pts = (TextView) convertView.findViewById(R.id.key_pts);
				value_team = (TextView) convertView.findViewById(R.id.value_team);
				value_w = (TextView) convertView.findViewById(R.id.value_w);
				value_d = (TextView) convertView.findViewById(R.id.value_d);
				value_l = (TextView) convertView.findViewById(R.id.value_l);
				value_gf = (TextView) convertView.findViewById(R.id.value_gf);
				value_ga = (TextView) convertView.findViewById(R.id.value_ga);
				value_pts = (TextView) convertView.findViewById(R.id.value_pts);
				title = (LinearLayout) convertView.findViewById(R.id.title);
				star = (ImageView) convertView.findViewById(R.id.star);
			}

			/**
			 * Get the team by position
			 */
			private GroupStatistics getTeamInfo(int pos) {
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

	}

}
