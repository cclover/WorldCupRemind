package com.cc.worldcupremind.view;

import com.cc.worldcupremind.R;
import com.cc.worldcupremind.logic.MatchDataController;
import com.cc.worldcupremind.model.GroupStatistics;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class GroupFragment extends ListFragment {
    private ArrayList<GroupStatistics> mGroupStaticsList;
    private ListAdapter mAdapter;
    private LayoutInflater mInflater;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mInflater = LayoutInflater.from(getActivity());
        mAdapter = new GropStaticsListAdapter();
        setListAdapter(mAdapter);
    }

    public void setData(ArrayList<GroupStatistics> groupStaticsData) {
        mGroupStaticsList = groupStaticsData;
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
                convertView = mInflater.inflate(R.layout.group_statics_item, parent, false);
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
            TextView key_1;
            TextView key_2;
            TextView key_3;
            TextView key_4;
            TextView key_5;
            TextView key_6;
            TextView value_group;
            TextView value_1;
            TextView value_2;
            TextView value_3;
            TextView value_4;
            TextView value_5;
            TextView value_6;

            void setupViews(int position) {
                GroupStatistics item = mGroupStaticsList.get(position);
                value_group.setText(MatchDataController.getInstance().getTeamNationalName(item.getTeamCode()));

                Drawable drawable = MatchDataController.getInstance().getTeamNationalFlag(item.getTeamCode());
                flag.setImageDrawable(drawable);
                value_1.setText(String.valueOf(item.getWinCount()));
                value_2.setText(String.valueOf(item.getDrawCount()));
                value_3.setText(String.valueOf(item.getGoalFor()));
                value_4.setText(String.valueOf(item.getPoint()));
                value_5.setText(String.valueOf(item.getLoseCount()));
                value_6.setText(String.valueOf(item.getPosition()));
                if (item.getTeamGroup().equals(mGroupStaticsList.get(position > 1 ? position - 1 : 0).getTeamGroup())) {
                    title.setVisibility(View.GONE);
                } else {
                    title.setVisibility(View.VISIBLE);
                    key_group.setText(String.format(getResources().getString(R.string.str_stage_group),
                            item.getTeamGroup()));
                }
                if (position == 0) {
                    title.setVisibility(View.VISIBLE);
                    key_group.setText(String.format(getResources().getString(R.string.str_stage_group),
                            item.getTeamGroup()));
                }
            }

            public void initViews(View convertView) {
                key_group = (TextView) convertView.findViewById(R.id.key_group);
                flag = (ImageView) convertView.findViewById(R.id.flag);
                key_1 = (TextView) convertView.findViewById(R.id.key_1);
                key_2 = (TextView) convertView.findViewById(R.id.key_2);
                key_3 = (TextView) convertView.findViewById(R.id.key_3);
                key_4 = (TextView) convertView.findViewById(R.id.key_4);
                key_5 = (TextView) convertView.findViewById(R.id.key_5);
                key_6 = (TextView) convertView.findViewById(R.id.key_6);
                value_group = (TextView) convertView.findViewById(R.id.value_group);
                value_1 = (TextView) convertView.findViewById(R.id.value_1);
                value_2 = (TextView) convertView.findViewById(R.id.value_2);
                value_3 = (TextView) convertView.findViewById(R.id.value_3);
                value_4 = (TextView) convertView.findViewById(R.id.value_4);
                value_5 = (TextView) convertView.findViewById(R.id.value_5);
                value_6 = (TextView) convertView.findViewById(R.id.value_6);
                title = (LinearLayout) convertView.findViewById(R.id.title);
            }
        }

    }

}
