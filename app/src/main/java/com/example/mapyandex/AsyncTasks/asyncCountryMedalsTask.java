package com.example.mapyandex.AsyncTasks;

import static com.example.mapyandex.MainActivity.FlagImage;
import static com.example.mapyandex.MainActivity.bottomSheetBehavior;
import static com.example.mapyandex.MainActivity.buttonlayout;
import static com.example.mapyandex.MainActivity.clearTableView;
import static com.example.mapyandex.MainActivity.country;
import static com.example.mapyandex.MainActivity.drawSimpleBitmap;
import static com.example.mapyandex.MainActivity.geocoder;
import static com.example.mapyandex.MainActivity.getAllMedals;
import static com.example.mapyandex.MainActivity.main_country;
import static com.example.mapyandex.MainActivity.mapView;
import static com.example.mapyandex.MainActivity.season;
import static com.example.mapyandex.MainActivity.setStateOfBottomSheet;
import static com.example.mapyandex.MainActivity.tblayoutl;
import static com.example.mapyandex.MainActivity.thread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.runtime.image.ImageProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class asyncCountryMedalsTask extends AsyncTask<String, Void, Map<String, ArrayList>> {
    Context context;

    public asyncCountryMedalsTask(MainActivity mainActivity) {
        context = mainActivity;
    }
    private java.util.Map<String, ArrayList> GetCountryMedals(OlymParsing op, String country) {
        return op.getMedalsOfOneCountry(country);
    }

    @Override
    protected java.util.Map<String, ArrayList> doInBackground(String... parameter) {
        OlymParsing op = new OlymParsing();
        String str = String.valueOf(country);
        Map<String, ArrayList> resp = GetCountryMedals(op, str);
        if (resp != null) {
            return resp;
        }
        thread.cancel(false);
        return null;
    }

    @Override
    protected void onPostExecute(java.util.Map<String, ArrayList> result) {
        super.onPostExecute(result);

        clearTableView(tblayoutl);
        Button back_but = new Button(context);
        back_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearTableView(tblayoutl);
                clearTableView(buttonlayout);
                getAllMedals(season);
                setStateOfBottomSheet(bottomSheetBehavior);
            }
        });
        back_but.setText("назад");
        buttonlayout.addView(back_but);
        for (String place: result.keySet()) {
            TableRow row_of_country = new TableRow(context);
            TableLayout tbl_of_all_content = new TableLayout(context);
            if (place.equals("src") && result.get(place).size() > 1){
                Log.e("res", result.get(place).toString());
//                try {
//                    FlagImage = new ImageView(context);
//                    FlagImage.setTag(String.valueOf(result.get(place).get(0)));
//                    NocImage = new ImageView(context);
//                    NocImage.setTag(String.valueOf(result.get(place).get(1)));
//                    CoatImage = new ImageView(context);
//                    CoatImage.setTag(String.valueOf(result.get(place).get(2)));
//                }catch (Exception IndexOutOfBoundsException){}
            }
            ArrayList<String> array = result.get(place);
            if (array.size() > 5) {
                TextView ct = new TextView(context);
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
                            mapView.getMap().getMapObjects().addPlacemark(target_part,
                                    ImageProvider.fromBitmap(drawSimpleBitmap(main_country,FlagImage)));
                            List<Point> list_of_points = new ArrayList<>();
                            list_of_points.add(target);
                            list_of_points.add(target_part);
                            Polyline POLYLINE = new Polyline(list_of_points);
                            mapView.getMap().getMapObjects().addPolyline(POLYLINE);
                            mapView.getMap().move(
                                    new CameraPosition(target, 4.5f, 3.0f, 1.0f),
                                    new Animation(Animation.Type.LINEAR, 3),
                                    null);
                            setStateOfBottomSheet(bottomSheetBehavior);
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