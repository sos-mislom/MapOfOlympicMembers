package com.example.mapyandex;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements GeoObjectTapListener, InputListener {
    private final String MAPKIT_API_KEY = "26047576-121f-4108-a0be-a1c7e90cfde7";
    private final Point TARGET_LOCATION = new Point(59.936760, 30.314673);

    private MapView mapView;
    private TextView content;
    private String season;
    private String country;

    private AsyncTask<String, Void, java.util.Map<String, ArrayList>> thread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        // Now MapView can be created.
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        mapView = (MapView) findViewById(R.id.mapview);

        // And to show what can be done with it, we move the camera to the center of Saint Petersburg.
        mapView.getMap().move(
                new CameraPosition(TARGET_LOCATION, 13.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 3),
                null);


        mapView.getMap().addTapListener(this);
        mapView.getMap().addInputListener(this);
        getStart();
    }
    private void getStart(){
        season = "winter";
        country = "fra";
        thread = new asyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public Bitmap drawSimpleBitmap(String number) {
        int picSize = 20;
        Bitmap bitmap = Bitmap.createBitmap(picSize, picSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        // отрисовка плейсмарка
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(picSize / 2, picSize / 2, picSize / 2, paint);
        // отрисовка текста
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setTextSize(10);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(number, picSize / 2,
                picSize / 2 - ((paint.descent() + paint.ascent()) / 2), paint);
        return bitmap;
    }
    @Override
    protected void onStop() {
        // Activity onStop call must be passed to both MapView and MapKit instance.
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        // Activity onStart call must be passed to both MapView and MapKit instance.
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
        Log.e("loc", loc.toString());

        mapView.getMap().deselectGeoObject();
    }

    @Override
    public void onMapLongTap(@NonNull Map map, @NonNull Point point) {

    }

    private class asyncTask extends AsyncTask<String, Void, java.util.Map<String, ArrayList>> {

        private java.util.Map<String, ArrayList> GetMedals(OlymParsing op, String season) {
            return op.getAllMedals(season);
        }

        private java.util.Map<String, ArrayList> GetCountryMedals(OlymParsing op, String country) {
            return op.getMedalsOfOneCountry(country);
        }

        @Override
        protected java.util.Map<String, ArrayList> doInBackground(String... parameter) {
            OlymParsing op = new OlymParsing();
            String str = String.valueOf(season);
            String str_c = String.valueOf(country);
            return GetCountryMedals(op, str_c);
        }

        @Override
        protected void onPostExecute(java.util.Map<String, ArrayList> result) {
            super.onPostExecute(result);

        }
    }
}