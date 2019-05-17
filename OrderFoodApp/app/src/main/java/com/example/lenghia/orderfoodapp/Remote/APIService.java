package com.example.lenghia.orderfoodapp.Remote;

import com.example.lenghia.orderfoodapp.Model.DataMessage;
import com.example.lenghia.orderfoodapp.Model.MyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAADxbOPII:APA91bFlzDmwbLgj8hXPF66Co2LSSqLBJ2vzBQOuMZJDRqEmslRyiDCSjvwzeY7Me__Vf4yDR01qSe0ewlVXftoHMV44YEK-wYN3oE4BG75ehsmFkHTipN4rOtJPXLUQQU75fQ9lftUz"
            }

    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification (@Body DataMessage body);
}
