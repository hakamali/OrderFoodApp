package com.example.lenghia.orderfoodapp.ViewHolder;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenghia.orderfoodapp.Interface.ItemClickListener;
import com.example.lenghia.orderfoodapp.R;

/**
 * Created by LE NGHIA on 1/19/2018.
 */

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtOrderId, txtOrderStatus, txtOrderPhone, txtOrderAddress;
    public FloatingActionButton fav_delete;
    private ItemClickListener itemClickListener;

    Context context;

    public OrderViewHolder(View itemView) {
        super(itemView);
        txtOrderAddress = itemView.findViewById(R.id.order_address);
        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtOrderPhone = itemView.findViewById(R.id.order_phone);
       // fav_delete = itemView.findViewById(R.id.fav_delete);

        itemView.setOnClickListener(this);
       // fav_delete.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);

    }

    private void removeCurrentItem() {
        getAdapterPosition();
        Toast.makeText(context, "This item will be delete !", Toast.LENGTH_SHORT).show();
    }
}
