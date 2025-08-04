package com.ultimafurniture.lynx.adapter;

import android.content.Context;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * @author 一叶丶枫凌
 *         Created on 2018-02-13 14:59
 */
public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {
    private Context mContext;
    protected BaseMultiItemRecyclerAdapter<T> mAdapter;

    public BaseViewHolder(View view) {
        super(view);
    }

    @CallSuper
    public void onCreate(Context context, BaseMultiItemRecyclerAdapter<T> adapter) {
        mContext = context;
        mAdapter = adapter;
    }

    final protected Context getContext() {
        return mContext;
    }

    public abstract void onBindData(int adapterPosition);

    final protected List<T> getData() {
        return mAdapter.getData();
    }

    final protected T getData(int adapterPosition) {
        return (T) mAdapter.getData(adapterPosition);
    }

    /**
     * 留给子类自己回收
     */
    public void onViewRecycled() {
    }

    public void onViewDetachedFromWindow() {
    }
}