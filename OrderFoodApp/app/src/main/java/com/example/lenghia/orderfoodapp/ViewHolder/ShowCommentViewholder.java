package com.example.lenghia.orderfoodapp.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.lenghia.orderfoodapp.R;

public class ShowCommentViewholder extends RecyclerView.ViewHolder {

    public TextView txtUerPhone, txtShowComment;
    public RatingBar ratingBarComment;

    public ShowCommentViewholder(View itemView) {
        super(itemView);
        txtUerPhone = itemView.findViewById(R.id.txtUserPhone);
        txtShowComment = itemView.findViewById(R.id.txtComment);
        ratingBarComment = itemView.findViewById(R.id.ratingBarComment);
    }
}
