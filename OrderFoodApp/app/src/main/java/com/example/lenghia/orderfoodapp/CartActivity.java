package com.example.lenghia.orderfoodapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenghia.orderfoodapp.Common.Common;
import com.example.lenghia.orderfoodapp.Common.Config;
import com.example.lenghia.orderfoodapp.Databases.Database;
import com.example.lenghia.orderfoodapp.Helper.RecyclerItemTouchHelper;
import com.example.lenghia.orderfoodapp.Interface.RecyclerViewItemTouchHelperListener;
import com.example.lenghia.orderfoodapp.Model.DataMessage;
import com.example.lenghia.orderfoodapp.Model.MyResponse;
import com.example.lenghia.orderfoodapp.Model.Order;
import com.example.lenghia.orderfoodapp.Model.Request;
import com.example.lenghia.orderfoodapp.Model.Token;
import com.example.lenghia.orderfoodapp.Model.User;
import com.example.lenghia.orderfoodapp.Remote.APIService;
import com.example.lenghia.orderfoodapp.Remote.IGoogleService;
import com.example.lenghia.orderfoodapp.ViewHolder.CartAdapter;
import com.example.lenghia.orderfoodapp.ViewHolder.CartViewHolder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CartActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, RecyclerViewItemTouchHelperListener {
    private static final int PAYPAL_REQUEST_CODE = 9998;
    private static final int PLAY_SERVICE_REQUEST = 9997;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RelativeLayout rootLayout;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView txtTotalPrice;
    Button btnPlace;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    Place shippingAddress;

    //paypal payment
    static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);

    String address, comment;

    IGoogleService mGoogleMapService;
    APIService mService;

    //location
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mlastlocation;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT_INTERVAL = 10;

    private static final int LOCATION_REQUEST_CODE = 9999;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath).build());

        //runtime permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, LOCATION_REQUEST_CODE);
        } else {
            if (checkPlayService()) {
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        setContentView(R.layout.activity_cart);
        initialViews();
        Intent servicePaypal = new Intent(this, PayPalService.class);
        servicePaypal.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(servicePaypal);

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        mGoogleMapService = Common.getGoogleMapAPI();

//        if (mlastlocation != null) {
//            lat = mlastlocation.getLatitude();
//            lng = mlastlocation.getLongitude();
//        }

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cart.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText(CartActivity.this, "Your cart is empty !!!", Toast.LENGTH_SHORT).show();
            }
        });
        loadListFood();

        mService = Common.getFCMService();
    }

    @SuppressLint("RestrictedApi")
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT_INTERVAL);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayService()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
        }
    }

    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_REQUEST).show();
            else {
                Toast.makeText(this, "This device is not supported !!!", Toast.LENGTH_SHORT).show();
            }
            return false;
        }

        return true;
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CartActivity.this);
        alertDialog.setTitle("One more step !");
        alertDialog.setMessage("Enter your address ");

        final LayoutInflater inflater = this.getLayoutInflater();
        final View order_address_comment = inflater.inflate(R.layout.order_address_comment, null);

        //final MaterialEditText edtAddress = order_address_comment.findViewById(R.id.edtAddress);

        final PlaceAutocompleteFragment placeAddress = (PlaceAutocompleteFragment) getFragmentManager()
                .findFragmentById(R.id.place_autocomplete_fragment);

        placeAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);

        ((EditText) placeAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setHint(" Enter your address ? ");

        ((EditText) placeAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(14);

        placeAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress = place;
                //Toast.makeText(CartActivity.this, "data !", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Status status) {
                Log.e("error", status.getStatusMessage());
            }
        });

        final MaterialEditText edtComment = order_address_comment.findViewById(R.id.edtComment);

        final RadioButton rdiShipToAddress = order_address_comment.findViewById(R.id.rdiShipToAddress);
        final RadioButton rdiHomeAdress = order_address_comment.findViewById(R.id.rdiHomeAddress);
        final RadioButton rdiCOD = order_address_comment.findViewById(R.id.rdiCOD);
        final RadioButton rdiPaypal = order_address_comment.findViewById(R.id.rdiPaypal);
        final RadioButton rdiBalance = order_address_comment.findViewById(R.id.rdiEaiItBalance);

        rdiHomeAdress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (Common.currentUser.getHomeAddress() != null ||
                            !TextUtils.isEmpty(Common.currentUser.getHomeAddress()))
                    {
                        address = Common.currentUser.getHomeAddress();
                        ((EditText) placeAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setText(address);

                    }
                    else
                        Toast.makeText(CartActivity.this, "Please update your Home Address !!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rdiShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //ship to this address
                if (isChecked) {
                    mGoogleMapService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false",
                            mlastlocation.getLatitude(), mlastlocation.getLongitude()))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response.body().toString());

                                        JSONArray resultsArray = jsonObject.getJSONArray("results");

                                        if (resultsArray.length() > 0) {
                                            JSONObject firstObject = resultsArray.getJSONObject(0);
                                            address = firstObject.getString("formatted_address");
                                            //Log.d("address", address);
                                            //set this address to placeAddress
                                            ((EditText) placeAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setText(address);
                                            Toast.makeText(CartActivity.this, "" + address, Toast.LENGTH_SHORT).show();
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Log.d("error", t.getMessage());
                                }
                            });
                }
            }
        });

        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //check condition
                //if user selects address from place fragment, just use it
                //if user selects Ship to this address , get address from location and using
                //if user selects Home address, get Home address from profile and using
                if (!rdiShipToAddress.isChecked() && !rdiHomeAdress.isChecked()) {
                    //if both radio buttons it not selected
                    if (shippingAddress != null)
                        address = shippingAddress.getAddress().toString();
                    else
                    {
                        Toast.makeText(CartActivity.this, "Please enter address or select option address !!!", Toast.LENGTH_SHORT).show();

                        //fix crash fragment
                        getFragmentManager().beginTransaction().remove(getFragmentManager()
                                .findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();
                        return;
                    }
                }

                if (TextUtils.isEmpty(address)) {
                    Toast.makeText(CartActivity.this, "Please enter address or select option address !!!", Toast.LENGTH_SHORT).show();

                    //fix crash fragment
                    getFragmentManager().beginTransaction().remove(getFragmentManager()
                            .findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                }

                comment = edtComment.getText().toString();

                //check payment
                if (!rdiCOD.isChecked() && !rdiPaypal.isChecked() && !rdiBalance.isChecked()) {
                    Toast.makeText(CartActivity.this, "Please select Payment option !!!", Toast.LENGTH_SHORT).show();

                    //fix crash fragment
                    getFragmentManager().beginTransaction().remove(getFragmentManager()
                            .findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                } else if (rdiPaypal.isChecked()) {
                    String formatAmount = txtTotalPrice.getText().toString()
                            .replace("$", "")
                            .replace(",", "");

                    //show paypal to payment
                    PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(formatAmount)
                            , "USD"
                            , "Eat it App Order"
                            , PayPalPayment.PAYMENT_INTENT_SALE);

                    Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                    intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                    intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
                    startActivityForResult(intent, PAYPAL_REQUEST_CODE);

                } else if (rdiCOD.isChecked())
                {
                    Request request = new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            address,
                            txtTotalPrice.getText().toString(),
                            comment,
                            "COD",
                            "0",
                            "Unpaid",
                            String.format("%s,%s", mlastlocation.getLatitude(), mlastlocation.getLongitude()),
                            cart
                    );
                    //Submit to Firebase
                    String order_number = String.valueOf(System.currentTimeMillis());
                    requests.child(order_number).setValue(request);
                    //Delete cart
                    new Database(CartActivity.this).cleanCart(Common.currentUser.getPhone());

                    sendNotificationOrder(order_number);

                    Toast.makeText(CartActivity.this, "Thank You, Order Place ", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (rdiBalance.isChecked()) {
                    double amount = 0.0;
                    try {
                        amount = Common.formatCurrency(txtTotalPrice.getText().toString(), Locale.US).doubleValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (Double.parseDouble(Common.currentUser.getBalance().toString()) >= amount) {
                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                txtTotalPrice.getText().toString(),
                                comment,
                                "EatIt Balance",
                                "0",
                                "Paid",
                                String.format("%s,%s", mlastlocation.getLatitude(), mlastlocation.getLongitude()),
                                cart
                        );
                        //Submit to Firebase
                        final String order_number = String.valueOf(System.currentTimeMillis());
                        requests.child(order_number).setValue(request);
                        //Delete cart
                        new Database(CartActivity.this).cleanCart(Common.currentUser.getPhone());

                        double balance = Double.parseDouble(Common.currentUser.getBalance().toString());
                        Map<String, Object> update_balance = new HashMap<>();
                        update_balance.put("balance", balance);

                        FirebaseDatabase.getInstance()
                                .getReference("User")
                                .child(Common.currentUser.getPhone())
                                .updateChildren(update_balance)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                            FirebaseDatabase.getInstance()
                                                    .getReference("User")
                                                    .child(Common.currentUser.getPhone())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Common.currentUser = dataSnapshot.getValue(User.class);
                                                            //
                                                            sendNotificationOrder(order_number);
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                    }
                                });
                    } else {
                        Toast.makeText(CartActivity.this, "Your balance not enough, please choose other payment !!!", Toast.LENGTH_SHORT).show();
                    }
                }


                //remove fragment
                getFragmentManager().beginTransaction().remove(getFragmentManager()
                        .findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();

            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                //remove fragment
                getFragmentManager().beginTransaction().remove(getFragmentManager()
                        .findFragmentById(R.id.place_autocomplete_fragment))
                        .commit();
            }
        });
        alertDialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PAYPAL_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null)
                {
                    try {
                        String paymentDetail = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetail);

                                Request request = new Request(
                                        Common.currentUser.getPhone(),
                                        Common.currentUser.getName(),
                                        address,
                                        txtTotalPrice.getText().toString(),
                                        comment,
                                        "Paypal",
                                        "0",
                                        jsonObject.getJSONObject("response").getString("state"),
                                        "100.100",
                                        //String.format("%s,%s", shippingAddress.getLatLng().latitude, shippingAddress.getLatLng().longitude),
                                        cart);

                                //Submit to Firebase
                                String order_number = String.valueOf(System.currentTimeMillis());
                                requests.child(order_number).setValue(request);

                                Log.d("phone", request.getFoods().toString());

                                //Delete cart
                                new Database(CartActivity.this).cleanCart(Common.currentUser.getPhone());

                                sendNotificationOrder(order_number);

                                Toast.makeText(CartActivity.this, "Payment successfully, Thank you for ordering ! ", Toast.LENGTH_SHORT).show();
                                finish();

                    } catch (JSONException j) {
                        j.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(this, "Payment cancel !!!", Toast.LENGTH_SHORT).show();
            else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
                Toast.makeText(this, "Invalid payment !!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotificationOrder(final String order_number) {
        final DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens_Client");
        final Query data = tokens.orderByChild("serverToken").equalTo(true);// get all true node

        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Token serverToken = postSnapshot.getValue(Token.class);
                    // Toast.makeText(CartActivity.this, "data !", Toast.LENGTH_SHORT).show();
//
//                    Notification notification = new Notification("LTN Dev", " You have new order " + order_number);
//                    Sender content = new Sender(serverToken.getToken(), notification);

                    Map<String, String> dataSent = new HashMap<>();
                    dataSent.put("title", "LTN Dev");
                    dataSent.put("message", " New order " + order_number);
                    DataMessage dataMessage = new DataMessage(serverToken.getToken(), dataSent);

                    String test = new Gson().toJson(dataMessage);
                    Log.d("Content", test);

                    mService.sendNotification(dataMessage)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.body().success == 1) {
                                        Toast.makeText(CartActivity.this, "Thank you, Order Place", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else
                                        Toast.makeText(CartActivity.this, "Failed !", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.d("error", t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("error", databaseError.getMessage());
            }
        });
    }

    private void loadListFood() {
        cart = new Database(this).getCarts(Common.currentUser.getPhone());
//        adapter.notifyDataSetChanged();
        adapter = new CartAdapter(cart, this);
        recyclerView.setAdapter(adapter);

        //Calculate total price
        int total = 0;
        for (Order order : cart)
            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {
        //delete item at List<Order> by position
        cart.remove(position);
        //delete all old data from sqlite
        new Database(this).cleanCart(Common.currentUser.getPhone());
        //update new data from List<Order> from sqlite
        for (Order item : cart)
            new Database(this).addToCart(item);
        //refresh
        loadListFood();
    }

    private void initialViews() {
        recyclerView = findViewById(R.id.listCart);
        rootLayout = findViewById(R.id.rootLayout);
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        txtTotalPrice = findViewById(R.id.total);
        btnPlace = findViewById(R.id.btnPlaceOrder);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mlastlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mlastlocation != null) {
            //Log.d("location","Your location " + mlastlocation.getLongitude() + "," + mlastlocation.getLatitude());
            //Toast.makeText(this, ""+ mlastlocation.getLatitude() + "," + mlastlocation.getLongitude(), Toast.LENGTH_SHORT).show();
        } else {
            Log.d("location fail ", "Could not get locaton ! ");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mlastlocation = location;
        displayLocation();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder) {
            String name = ((CartAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            final Order deleteItem = ((CartAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);

            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(), Common.currentUser.getPhone());

            //update
            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for (Order item : orders)
                total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("en", "US");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

            txtTotalPrice.setText(fmt.format(total));

            Snackbar snackbar = Snackbar.make(rootLayout, name + " removed from cart ! ", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem, deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);
                    //update
                    int total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for (Order item : orders)
                        total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
                    Locale locale = new Locale("en", "US");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

                    txtTotalPrice.setText(fmt.format(total));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
