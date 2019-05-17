package com.example.lenghia.orderfoodapp.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.lenghia.orderfoodapp.Interface.ItemClickListener;
import com.example.lenghia.orderfoodapp.R;

public class FavoritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView food_name,food_price;
    public ImageView food_image, fav_image, share_image,quick_cart;

    public RelativeLayout view_background;
    public LinearLayout view_foreground;


    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FavoritesViewHolder(View itemView) {
        super(itemView);
        food_name = itemView.findViewById(R.id.food_name);
        food_price = itemView.findViewById(R.id.food_price);
        food_image = itemView.findViewById(R.id.food_image);
        fav_image = itemView.findViewById(R.id.fav);
        quick_cart = itemView.findViewById(R.id.btn_quick_cart);
        share_image = itemView.findViewById(R.id.share_image);

        view_background = itemView.findViewById(R.id.view_background);
        view_foreground = itemView.findViewById(R.id.view_foreground);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);
    }
}

