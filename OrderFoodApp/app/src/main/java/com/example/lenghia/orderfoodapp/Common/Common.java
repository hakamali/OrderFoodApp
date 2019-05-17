package com.example.lenghia.orderfoodapp.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;

import com.example.lenghia.orderfoodapp.Model.Request;
import com.example.lenghia.orderfoodapp.Model.User;
import com.example.lenghia.orderfoodapp.Remote.APIService;
import com.example.lenghia.orderfoodapp.Remote.GoogleRetrofitClient;
import com.example.lenghia.orderfoodapp.Remote.IGoogleService;
import com.example.lenghia.orderfoodapp.Remote.RetrofitClient;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by LE NGHIA on 1/15/2018.
 */

public class Common {
    public static User currentUser;

    public static String PHONE_TEXT = "userPhone";

    public static String topicName = "News";

    public static String currentKey;

    public static final String SHIPPER_INFO_TABLE = "ShippingOrders";

    public static final String INTENT_FOOD_ID = "FoodId";

    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";

    public static final String BASE_URL = "https://fcm.googleapis.com/";
    public static final String GOOGLE_API_URL = "https://maps.googleapis.com/";
    public static Request currentRequest;

    public static APIService getFCMService() {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static IGoogleService getGoogleMapAPI() {
        return GoogleRetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    public static String convertCodeToStatus(String code) {
        if (code.equals("0"))
            return "Placed";
        else if (code.equals("1"))
            return "On my way";
        else if(code.equals("2"))
            return "Shipping";
        else
            return "Shipped";
    }

    public static boolean isConnectedInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null)
        {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null)
            {
                for (int i = 0; i < info.length; i++)
                {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }

    public static BigDecimal formatCurrency(String amount, Locale locale) throws ParseException, java.text.ParseException {
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        if (format instanceof DecimalFormat)
            (((DecimalFormat) format)).setParseBigDecimal(true);
        return (BigDecimal) format.parse(amount.replace("[^\\d.,]", ""));
    }
}
