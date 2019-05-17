package com.example.lenghia.orderfoodapp.Model;

import java.util.List;

/**
 * Created by LE NGHIA on 1/19/2018.
 */

public class Request {

    private String phone;
    private String name;
    private String address;
    private String total;
    private String comment;
    public String paymentMethod;
    private String status;
    private String paymentState;
    private String latLng;
    private List<Order> foods; // list of food order

    public Request() {
    }


    public Request(String phone, String name, String address, String total, String comment, String paymentMethod, String status, String paymentState, String latLng, List<Order> foods) {
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.total = total;
        this.comment = comment;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paymentState = paymentState;
        this.latLng = latLng;
        this.foods = foods;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(String paymentState) {
        this.paymentState = paymentState;
    }

    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    public List<Order> getFoods() {
        return foods;
    }

    public void setFoods(List<Order> foods) {
        this.foods = foods;
    }
}
