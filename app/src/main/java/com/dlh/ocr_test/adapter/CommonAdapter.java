package com.dlh.ocr_test.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * @desc: 通用适配器
 * @author: yangjing
 * @time: 2020-03-13
 */
public abstract class CommonAdapter<T> extends BaseAdapter {

    protected final Context mContext;

    private final List<T> mDatas;
    private final LayoutInflater mInflater;
    private final int layoutId;

    public CommonAdapter(Context context, List<T> mDatas, int layoutId) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mDatas = mDatas;
        this.layoutId = layoutId;

    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public T getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.get(mContext, convertView, parent, layoutId, position);
        convert(holder, position, getItem(position));
        return holder.getConvertView();
    }

    public abstract void convert(ViewHolder helper, int position, T item);

}
