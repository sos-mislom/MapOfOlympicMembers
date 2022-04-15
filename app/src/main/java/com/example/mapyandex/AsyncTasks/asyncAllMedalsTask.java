package com.example.mapyandex.AsyncTasks;

import static com.example.mapyandex.MainActivity.bottomSheetBehavior;
import static com.example.mapyandex.MainActivity.buttonlayout;
import static com.example.mapyandex.MainActivity.geocoder;
import static com.example.mapyandex.MainActivity.img_of_search;
import static com.example.mapyandex.MainActivity.mapView;
import static com.example.mapyandex.MainActivity.sc;
import static com.example.mapyandex.MainActivity.season;
import static com.example.mapyandex.MainActivity.tblayoutl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.example.mapyandex.MainActivity;
import com.example.mapyandex.Parse.OlymParsing;
import com.example.mapyandex.R;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.runtime.image.ImageProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class asyncAllMedalsTask extends AsyncTask<String, Void, Map<String, ArrayList>> {
    Context context;

    public asyncAllMedalsTask(MainActivity mainActivity) {
        context = mainActivity;
    }

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

        Button summer_but = new Button(context);
        Button winter_but = new Button(context);
        EditText search = new EditText(context);

        ImageButton find_but = new ImageButton(context);
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
                for (View row : MainActivity.getAllChildren(tblayoutl)) {
                    if (row instanceof TextView && ((TextView) row).getText().equals(str_for_search)) {
                        sc.scrollToDescendant(row);
                        if (season == "winter"){
                            row.setBackgroundColor(ContextCompat.getColor(context, R.color.winter_plus));
                        } else if (season == "summer")
                        row.setBackgroundColor(ContextCompat.getColor(context, R.color.summer_plus));
                    }

                }
            }
        });

        if (season.equals("summer")){
            summer_but.setTextColor(ContextCompat.getColor(context, R.color.summer_plus));
            tblayoutl.setBackgroundColor(ContextCompat.getColor(context, R.color.summer));
        } else if (season.equals("winter")){
            winter_but.setTextColor(ContextCompat.getColor(context, R.color.teal_700));
            tblayoutl.setBackgroundColor(ContextCompat.getColor(context, R.color.winter));
        }
        summer_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.clearTableView(tblayoutl);
                MainActivity.clearTableView(buttonlayout);
                MainActivity.getAllMedals("summer");
                MainActivity.setStateOfBottomSheet(bottomSheetBehavior);
            }
        });
        summer_but.setText(R.string.summer);
        winter_but.setText(R.string.winter);
        winter_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.clearTableView(tblayoutl);
                MainActivity.clearTableView(buttonlayout);
                MainActivity.getAllMedals("winter");
                MainActivity.setStateOfBottomSheet(bottomSheetBehavior);
            }
        });
        TableRow tbl_of_but = new TableRow(context);
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
            TableRow row_of_country = new TableRow(context);
            TableLayout tbl_of_all_content = new TableLayout(context);
            ArrayList<String> array = result.get(сountry);
            if (array.size() > 5) {
                TextView ct = new TextView(context);
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
                            ct.setBackgroundColor(ContextCompat.getColor(context, R.color.summer_plus));
                            List<Address> for_mark = geocoder.getFromLocationName(String.valueOf(ct.getText()), 1);
                            Point target = new Point(for_mark.get(0).getLatitude(), for_mark.get(0).getLongitude());
                            mapView.getMap().getMapObjects().clear();
                            MainActivity.country = String.valueOf(array.get(1));
                            MainActivity.main_country = String.valueOf(ct.getText());
                            MainActivity.clearTableView(buttonlayout);
                            MainActivity.getMedalsOfCountry();
                            mapView.getMap().getMapObjects().addPlacemark(target,
                                    ImageProvider.fromBitmap(MainActivity.drawSimpleBitmap(String.valueOf(ct.getText()))));
                            MainActivity.setStateOfBottomSheet(bottomSheetBehavior);
                            mapView.getMap().move(
                                    new CameraPosition(target, 4.5f, 3.0f, 1.0f),
                                    new Animation(Animation.Type.LINEAR, 3),
                                    null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                TableRow tbl_of_medals = new TableRow(context);

                TextView cu_medal = new TextView(context);
                cu_medal.setText("Золото: " +array.get(2));
                cu_medal.setTextSize(20);
                cu_medal.setLayoutParams(trLayoutParams);
                cu_medal.setTextColor(ContextCompat.getColor(context, R.color.cu));
                tbl_of_medals.addView(cu_medal);

                TextView sb_medal = new TextView(context);
                sb_medal.setText("Серебро: " + array.get(3));
                sb_medal.setTextSize(20);
                sb_medal.setLayoutParams(trLayoutParams);
                sb_medal.setTextColor(ContextCompat.getColor(context, R.color.sb));
                tbl_of_medals.addView(sb_medal);

                TextView br_medal = new TextView(context);
                br_medal.setText("Медь: " + array.get(4));
                br_medal.setTextSize(20);
                br_medal.setLayoutParams(trLayoutParams);
                br_medal.setTextColor(ContextCompat.getColor(context, R.color.br));
                tbl_of_medals.addView(br_medal);

                TextView all_medal = new TextView(context);
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