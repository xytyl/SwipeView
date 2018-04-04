package com.mapleaf.swipeview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by yanxin on 2018/4/2.
 */

public class SwipeRecyclerViewAdapter extends RecyclerView.Adapter<SwipeRecyclerViewAdapter.ViewHolder>{

    private List<String> mDataList;
    private Context mContext;

    public SwipeRecyclerViewAdapter(List<String> dataList) {
        mDataList = dataList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_swipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "delete", Toast.LENGTH_SHORT).show();
                SwipeView.closeMenu(v);
            }
        });
        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SwipeView.closeMenu(v)) {
                    Toast.makeText(mContext, "content", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
//        return mDataList == null ? 0 : mDataList.size();
        return 20;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView delete;
        LinearLayout content;

        public ViewHolder(View itemView) {
            super(itemView);
            delete = itemView.findViewById(R.id.tv_usb_delete);
            content = itemView.findViewById(R.id.ll_usb);
        }
    }
}
