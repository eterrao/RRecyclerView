package com.welove520.rrefresh.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.welove520.rrefresh.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/17.
 */

public class SampleAdapter extends RecyclerView.Adapter<SampleHolder> {
    public static final String KEY_ICON = "icon";
    public static final String KEY_COLOR = "color";
    protected List<Map<String, Integer>> mSampleList;

    @Override
    public SampleHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        initData();
        return new SampleHolder(view);
    }

    @Override
    public void onBindViewHolder(SampleHolder holder, int pos) {
        Map<String, Integer> data = mSampleList.get(pos);
        holder.bindData(data);
    }

    @Override
    public int getItemCount() {
        return mSampleList.size();
    }

    private void initData() {
        Map<String, Integer> map;
        mSampleList = new ArrayList<>();

        int[] icons = {
                R.drawable.icon_1,
                R.drawable.icon_2,
                R.drawable.icon_3};

        int[] colors = {
                R.color.saffron,
                R.color.eggplant,
                R.color.sienna};

        for (int i = 0; i < icons.length; i++) {
            map = new HashMap<>();
            map.put(KEY_ICON, icons[i]);
            map.put(KEY_COLOR, colors[i]);
            mSampleList.add(map);
        }
    }
}
