package com.example.mapyandex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapyandex.AsyncTasks.asyncAllMedalsTask;
import com.example.mapyandex.AsyncTasks.asyncCountryMedalsTask;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.ScreenPoint;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.layers.GeoObjectTapEvent;
import com.yandex.mapkit.layers.GeoObjectTapListener;
import com.yandex.mapkit.map.GeoObjectSelectionMetadata;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.mapview.MapView;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements GeoObjectTapListener, InputListener {
    public static MapView mapView;
    public static String season;
    public static BottomSheetBehavior bottomSheetBehavior;
    public static String country;
    public static String main_country;
    public static TableLayout tblayoutl;
    public static TableLayout buttonlayout;
    public static ScrollView sc;
    public static Geocoder geocoder;
    public static MainActivity ma;
    private static TextView t_x_message;
    public static Drawable img_of_search;

    public static ImageView FlagImage;
    public static ImageView NocImage;
    public static ImageView CoatImage;

    public static AsyncTask<String, Void, java.util.Map<String, ArrayList>> thread;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String MAPKIT_API_KEY = "26047576-121f-4108-a0be-a1c7e90cfde7"; // !!!! не забыть убрать в файл в гитигноре
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_main);
        sc = findViewById(R.id.scrollViewOfTbl);
        ma = MainActivity.this;
        t_x_message = new TextView(ma);
        t_x_message.setVisibility(View.INVISIBLE);

        tblayoutl = (TableLayout) findViewById(R.id.medalLayout);
        buttonlayout = (TableLayout) findViewById(R.id.buttonlayout);
        super.onCreate(savedInstanceState);
        getAllMedals("summer");
        @SuppressLint("UseCompatLoadingForDrawables") Drawable d = getResources().getDrawable(R.drawable.search);
        Bitmap bitmap1 = ((BitmapDrawable) d).getBitmap();
        img_of_search = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap1, 20, 20, true));

        mapView = (MapView) findViewById(R.id.mapview);
        geocoder = new Geocoder(this, Locale.getDefault());
        mapView.getMap().addTapListener(this);
        mapView.getMap().addInputListener(this);
        mapView.setZoomFocusPoint(new ScreenPoint(2.f, 2.f));

        LinearLayout llBottomSheet = (LinearLayout) findViewById(R.id.bottomSheet);

        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        bottomSheetBehavior.setPeekHeight(170);
        bottomSheetBehavior.setHideable(false);

    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    public boolean onObjectTap(@NonNull GeoObjectTapEvent geoObjectTapEvent) {
        final GeoObjectSelectionMetadata selectionMetadata = geoObjectTapEvent
                .getGeoObject()
                .getMetadataContainer()
                .getItem(GeoObjectSelectionMetadata.class);

        if (selectionMetadata != null) {
            mapView.getMap().selectGeoObject(selectionMetadata.getId(), selectionMetadata.getLayerId());
        }

        return selectionMetadata != null;
    }

    @Override
    public void onMapTap(@NonNull Map map, @NonNull Point point) {
        if (t_x_message.getVisibility() == View.VISIBLE && checkConnection()){
            t_x_message.setVisibility(View.INVISIBLE);
            getAllMedals("summer");
        }
    }

    @Override
    public void onMapLongTap(@NonNull Map map, @NonNull Point point) {
    }

    public static void getAllMedals(String season_str){
        season = season_str;
        if (checkConnection()) thread = new asyncAllMedalsTask(ma).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public static void getMedalsOfCountry(){
        if (checkConnection()) thread = new asyncCountryMedalsTask(ma).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void clearTableView(ViewGroup tblview){
        for (View i : getAllChildren(tblview)) {
            ((ViewGroup) tblview).removeView(i);
        }
    }

    public static boolean checkConnection(){
        if (hasConnection()){
            return true;
        } else {
            Toast toast = Toast.makeText(ma,
                    "ИНТЕРНЕТ КОНЭКШН ЭРРОР", Toast.LENGTH_SHORT);
            toast.show();
            t_x_message.setText(" Ткните для перезагрузки");
            t_x_message.setTextSize(30);
            t_x_message.setGravity(View.TEXT_ALIGNMENT_CENTER);
            mapView.addView(t_x_message);
            return false;
        }
    }
    public static ArrayList<View> getAllChildren(View v) {
        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            return viewArrayList;
        }
        ArrayList<View> result = new ArrayList<View>();
        ViewGroup viewGroup = (ViewGroup) v;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));
            result.addAll(viewArrayList);
        }
        return result;
    }

    public static void setStateOfBottomSheet(BottomSheetBehavior bottomSheetBehavior){
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        }
    }

    public static Bitmap drawSimpleBitmap(String text) {
        int picSize = 600;
        Bitmap bitmap = Bitmap.createBitmap(picSize, picSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setTextSize(60);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, picSize / 2,
                picSize / 2 - ((paint.descent() + paint.ascent()) / 2), paint);
        return bitmap;
    }
    public static Bitmap drawSimpleBitmap(String text, ImageView imv) {
        int picSize = 650;
        Bitmap bitmap = Bitmap.createBitmap(picSize, picSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setTextSize(60);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, picSize / 2,
                picSize / 2 - ((paint.descent() + paint.ascent()) / 2), paint);
        return bitmap;
    }

    public static boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager) ma.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) return true;

        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) return true;

        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected()) return true;
        return false;
    }

}