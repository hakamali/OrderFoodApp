package com.example.lenghia.orderfoodapp.Interface;

import android.support.v7.widget.RecyclerView;

public interface RecyclerViewItemTouchHelperListener {
    void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
}
