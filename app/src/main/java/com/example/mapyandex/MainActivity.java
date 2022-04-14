package com.example.mapyandex;

import static android.content.ContentValues.TAG;
import static android.os.FileUtils.copy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.cocosw.bottomsheet.BottomSheet;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.ScreenPoint;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.layers.GeoObjectTapEvent;
import com.yandex.mapkit.layers.GeoObjectTapListener;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.GeoObjectSelectionMetadata;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements GeoObjectTapListener, InputListener {
    private final String MAPKIT_API_KEY = "26047576-121f-4108-a0be-a1c7e90cfde7";
    private MapView mapView;
    private String season;
    private BottomSheetBehavior bottomSheetBehavior;
    private String country;
    private String main_country;
    private TableLayout tblayoutl;
    private TableLayout buttonlayout;
    private Geocoder geocoder;
    private Drawable img_of_search;
    private Bitmap nocImage;
    private String url_of_noc;
    private AsyncTask<String, Void, java.util.Map<String, ArrayList>> thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        @SuppressLint("UseCompatLoadingForDrawables") Drawable d = getResources().getDrawable(R.drawable.search);
        Bitmap bitmap1 = ((BitmapDrawable) d).getBitmap();
        img_of_search = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap1, 20, 20, true));

        mapView = (MapView) findViewById(R.id.mapview);
        getAllMedals("summer");
        geocoder = new Geocoder(this, Locale.getDefault());
        mapView.getMap().addTapListener(this);
        mapView.getMap().addInputListener(this);
        mapView.setZoomFocusPoint(new ScreenPoint(2.f, 2.f));

        LinearLayout llBottomSheet = (LinearLayout) findViewById(R.id.bottomSheet);

        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight(160);
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
        Location loc = new Location(String.valueOf(point));
        mapView.getMap().deselectGeoObject();

        try {
            List<Address> location = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapLongTap(@NonNull Map map, @NonNull Point point) {

    }


    private void getAllMedals(String str){
        season = str;
        thread = new asyncAllMedalsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private void clearTableView(ViewGroup tblview){
        for (View i : getAllChildren(tblview)) {
            ((ViewGroup) tblview).removeView(i);
        }
    }
    private void getMedalsOfCountryt(){
        thread = new asyncCountryMedalsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private ArrayList<View> getAllChildren(View v) {
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

    private void setStateOfBottomSheet(){
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        }
    }
    public Bitmap drawSimpleBitmap(String text) {
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


    private class asyncAllMedalsTask extends AsyncTask<String, Void, java.util.Map<String, ArrayList>> {
        private java.util.Map<String, ArrayList> GetMedals(OlymParsing op, String season) {
            return op.getAllMedals(season);
        }
        @Override
        protected java.util.Map<String, ArrayList> doInBackground(String... parameter) {
            OlymParsing op = new OlymParsing();
            String str = String.valueOf(season);
            return GetMedals(op, str);
        }
        @SuppressLint("ResourceType")
        @Override
        protected void onPostExecute(java.util.Map<String, ArrayList> result) {
            super.onPostExecute(result);
            tblayoutl = (TableLayout) findViewById(R.id.medalLayout);
            buttonlayout = (TableLayout) findViewById(R.id.buttonlayout);

            Button summer_but = new Button(MainActivity.this);
            Button winter_but = new Button(MainActivity.this);
            EditText search = new EditText(MainActivity.this);

            ImageButton find_but = new ImageButton(MainActivity.this);
            find_but.setImageDrawable(img_of_search);
            search.setWidth(450);
            find_but.setVisibility(View.GONE);

            search.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    find_but.setVisibility(View.VISIBLE);
                }
                @Override
                public void afterTextChanged(Editable s) { }
            });
            find_but.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public void onClick(View v) {
                    find_but.setVisibility(View.GONE);
                    String str_for_search = search.getText().toString();
                    ScrollView sc = findViewById(R.id.scrollViewOfTbl);
                    for (View row : getAllChildren(tblayoutl)) {
                        if (row instanceof TextView && ((TextView) row).getText().equals(str_for_search)) {
                            sc.scrollToDescendant(row);
                            row.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.summer_plus));
                        }

                    }
                }
            });

            if (season.equals("summer")){
                summer_but.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.summer_plus));
                tblayoutl.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.summer));
            } else if (season.equals("winter")){
                winter_but.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.teal_700));
                tblayoutl.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.winter));
            }
            summer_but.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearTableView(tblayoutl);
                    clearTableView(buttonlayout);
                    getAllMedals("summer");
                    setStateOfBottomSheet();
                }
            });
            summer_but.setText(R.string.summer);
            winter_but.setText(R.string.winter);
            winter_but.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearTableView(tblayoutl);
                    clearTableView(buttonlayout);
                    getAllMedals("winter");
                    setStateOfBottomSheet();
                }
            });
            TableRow tbl_of_but = new TableRow(MainActivity.this);
            TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1);
            tbl_of_but.setLayoutParams(rowParams);
            tbl_of_but.addView(summer_but);
            tbl_of_but.addView(winter_but);
            tbl_of_but.addView(search);
            tbl_of_but.addView(find_but);
            buttonlayout.addView(tbl_of_but);
            for (String сountry: result.keySet()) {
                TableRow row_of_country = new TableRow(MainActivity.this);
                TableLayout tbl_of_all_content = new TableLayout(MainActivity.this);
                ArrayList<String> array = result.get(сountry);
                if (array.size() > 5) {
                    TextView ct = new TextView(MainActivity.this);
                    TableRow.LayoutParams trLayoutParams = new TableRow.LayoutParams();
                    trLayoutParams.setMargins(7, 7, 7, 7);
                    ct.setText(сountry);
                    ct.setTextSize(25);
                    ct.setPadding(5, 0, 0, 0);
                    row_of_country.addView(ct);

                    ct.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                ct.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.summer_plus));
                                List<Address> for_mark = geocoder.getFromLocationName(String.valueOf(ct.getText()), 1);
                                Point target = new Point(for_mark.get(0).getLatitude(), for_mark.get(0).getLongitude());
                                mapView.getMap().getMapObjects().clear();
                                country = String.valueOf(array.get(1));
                                main_country = String.valueOf(ct.getText());
                                clearTableView(buttonlayout);
                                getMedalsOfCountryt();
                                mapView.getMap().getMapObjects().addPlacemark(target,
                                        ImageProvider.fromBitmap(drawSimpleBitmap(String.valueOf(ct.getText()))));
                                setStateOfBottomSheet();
                                mapView.getMap().move(
                                        new CameraPosition(target, 4.5f, 3.0f, 1.0f),
                                        new Animation(Animation.Type.LINEAR, 3),
                                        null);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    TableRow tbl_of_medals = new TableRow(MainActivity.this);

                    TextView cu_medal = new TextView(MainActivity.this);
                    cu_medal.setText("Золото: " +array.get(2));
                    cu_medal.setTextSize(20);
                    cu_medal.setLayoutParams(trLayoutParams);
                    cu_medal.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.cu));
                    tbl_of_medals.addView(cu_medal);

                    TextView sb_medal = new TextView(MainActivity.this);
                    sb_medal.setText("Серебро: " + array.get(3));
                    sb_medal.setTextSize(20);
                    sb_medal.setLayoutParams(trLayoutParams);
                    sb_medal.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.sb));
                    tbl_of_medals.addView(sb_medal);

                    TextView br_medal = new TextView(MainActivity.this);
                    br_medal.setText("Медь: " + array.get(4));
                    br_medal.setTextSize(20);
                    br_medal.setLayoutParams(trLayoutParams);
                    br_medal.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.br));
                    tbl_of_medals.addView(br_medal);

                    TextView all_medal = new TextView(MainActivity.this);
                    all_medal.setText("Всего: " + array.get(5));
                    all_medal.setTextSize(20);
                    all_medal.setLayoutParams(trLayoutParams);
                    tbl_of_medals.addView(all_medal);

                   tbl_of_all_content.addView(tbl_of_medals);

                }
                tblayoutl.addView(row_of_country);
                tblayoutl.addView(tbl_of_all_content);
            }
        }
    }


    public class asyncCountryMedalsTask extends AsyncTask<String, Void, java.util.Map<String, ArrayList>> {
        private java.util.Map<String, ArrayList> GetCountryMedals(OlymParsing op, String country) {
            return op.getMedalsOfOneCountry(country);
        }

        @Override
        protected java.util.Map<String, ArrayList> doInBackground(String... parameter) {
            OlymParsing op = new OlymParsing();
            String str = String.valueOf(country);
            return GetCountryMedals(op, str);
        }

        @Override
        protected void onPostExecute(java.util.Map<String, ArrayList> result) {
            super.onPostExecute(result);
            tblayoutl = (TableLayout) findViewById(R.id.medalLayout);
            clearTableView(tblayoutl);
            Button back_but = new Button(MainActivity.this);
            back_but.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearTableView(tblayoutl);
                    clearTableView(buttonlayout);
                    getAllMedals(season);
                    setStateOfBottomSheet();
                }
            });
            back_but.setText("назад");
            buttonlayout.addView(back_but);
            for (String place: result.keySet()) {
                TableRow row_of_country = new TableRow(MainActivity.this);
                TableLayout tbl_of_all_content = new TableLayout(MainActivity.this);
                if (place == "src"){
                    url_of_noc = String.valueOf(result.get(place).get(0));
                }
                ArrayList<String> array = result.get(place);
                if (array.size() > 5) {
                    TextView ct = new TextView(MainActivity.this);
                    TableRow.LayoutParams trLayoutParams = new TableRow.LayoutParams();
                    trLayoutParams.setMargins(7, 7, 7, 7);
                    ct.setText(place + " / " + array.get(1));
                    ct.setTextSize(25);
                    ct.setPadding(5, 0, 0, 0);
                    row_of_country.addView(ct);
                    ct.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("NewApi")
                        @RequiresApi(api = Build.VERSION_CODES.Q)
                        @Override
                        public void onClick(View v) {
                            try {
                                List<Address> place_mark = geocoder.getFromLocationName(String.valueOf(ct.getText()), 1);
                                Point target = new Point(place_mark.get(0).getLatitude(), place_mark.get(0).getLongitude());

                                mapView.getMap().getMapObjects().addPlacemark(target,
                                        ImageProvider.fromBitmap(drawSimpleBitmap(String.valueOf(ct.getText()))));

                                List<Address> participators_mark = geocoder.getFromLocationName(main_country, 1);
                                Point target_part = new Point(participators_mark.get(0).getLatitude(), participators_mark.get(0).getLongitude());

                                List<Point> list_of_points = new ArrayList<>();
                                list_of_points.add(target);
                                list_of_points.add(target_part);
                                Polyline POLYLINE = new Polyline(list_of_points);
                                mapView.getMap().getMapObjects().addPolyline(POLYLINE);
                                mapView.getMap().move(
                                        new CameraPosition(target, 4.5f, 3.0f, 1.0f),
                                        new Animation(Animation.Type.LINEAR, 3),
                                        null);
                                setStateOfBottomSheet();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    TableRow tbl_of_medals = new TableRow(MainActivity.this);

                    TextView cu_medal = new TextView(MainActivity.this);
                    cu_medal.setText("Золото: " +array.get(2));
                    cu_medal.setTextSize(20);
                    cu_medal.setLayoutParams(trLayoutParams);
                    cu_medal.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.cu));
                    tbl_of_medals.addView(cu_medal);

                    TextView sb_medal = new TextView(MainActivity.this);
                    sb_medal.setText("Серебро: " + array.get(3));
                    sb_medal.setTextSize(20);
                    sb_medal.setLayoutParams(trLayoutParams);
                    sb_medal.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.sb));
                    tbl_of_medals.addView(sb_medal);

                    TextView br_medal = new TextView(MainActivity.this);
                    br_medal.setText("Медь: " + array.get(4));
                    br_medal.setTextSize(20);
                    br_medal.setLayoutParams(trLayoutParams);
                    br_medal.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.br));
                    tbl_of_medals.addView(br_medal);

                    TextView all_medal = new TextView(MainActivity.this);
                    all_medal.setText("Всего: " + array.get(5));
                    all_medal.setTextSize(20);
                    all_medal.setLayoutParams(trLayoutParams);
                    tbl_of_medals.addView(all_medal);

                    tbl_of_all_content.addView(tbl_of_medals);

                }
                tblayoutl.addView(row_of_country);
                tblayoutl.addView(tbl_of_all_content);
            }
        }
    }

}