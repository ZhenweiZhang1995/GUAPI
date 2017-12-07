package guapi.guapi;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.MapStyleOptions;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MapsActivityCurrentPlace extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        SensorEventListener {

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mWindow;
        private final View mContents;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;

        }

        private void render(Marker marker, View view) {
            int badge;
            if (marker.equals(uss)) {
                badge = R.drawable.student_store;
            } else if (marker.equals(cam)) {
                badge = R.drawable.memorial;
            } else if (marker.equals(dp)) {
                badge = R.drawable.davie;
            } else if (marker.equals(ft)) {
                badge = R.drawable.forest_theater;
            } else if (marker.equals(ca)) {
                badge = R.drawable.coker;
            } else if (marker.equals(pt)){
                badge = R.drawable.playmakers;
            } else if (marker.equals(mbt)){
                badge = R.drawable.bell_tower;
            } else if (marker.equals(oe)){
                badge = R.drawable.old_east;
            } else if (marker.equals(ow)){
                badge = R.drawable.old_well;
            } else{
                // Passing 0 to setImageResource will clear the image view.
                badge = 0;
            }
            ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);

            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            TextView description = ((TextView)view.findViewById(R.id.description));

            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
                titleUi.setText(titleText);

                Cursor c = db.rawQuery("SELECT Description FROM Landmarks WHERE Name = '"+ marker.getTitle().toString()+"'", null);
                c.moveToFirst();
                //set description
                description.setText(c.getString(0));
                //update description
                des=c.getString(0);


            } else {
                titleUi.setText("");
            }


            String snippet = marker.getSnippet();
            TextView snippetUi = view.findViewById(R.id.snippet);
            if (snippet != null && snippet.length() > 12) {
                SpannableString snippetText = new SpannableString(snippet);
                snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }
        }
    }


    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;
    private Marker mSelectedMarker;
    private Spinner spinner1;


    private Marker uss;
    private Marker cam;
    private Marker dp;
    private Marker ft;
    private Marker ca;
    private Marker mbt;
    private Marker oe;
    private Marker ow;
    private Marker pt;

    private Marker mLastSelectedMarker;


    private TextView mCameraTextView;

    private String title;
    SQLiteDatabase db =  null;

    private TextToSpeech tts;
    private TextToSpeech tts2;

    private String des;

    private Button svBtn;

    float light_value = 0;
    SensorManager sm = null;
    Sensor light = null;
    private MapStyleOptions style;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // create database
        db =  openOrCreateDatabase("MyDatabase", Context.MODE_PRIVATE, null);
        resetDB();

        // initialize dropdown box
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner1.setOnItemSelectedListener(new ItemSelectedListener());


        // initialize map
        if (mLastSelectedMarker != null && mLastSelectedMarker.isInfoWindowShown()) {
            // Refresh the info window when the info window's content has changed.
            mLastSelectedMarker.showInfoWindow();
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

<<<<<<< HEAD
        Button button= findViewById(R.id.btn);
=======

        // method for "Navigate" button, pass intent to MapGps.java
        Button button= (Button)findViewById(R.id.btn);
>>>>>>> refs/remotes/origin/master
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if (tts != null) {
                    tts.stop();
                    tts.shutdown();
                }
               // add method
                if(title!=null){
                    Intent myIntent = new Intent(MapsActivityCurrentPlace.this,MapGps.class);

                    myIntent.putExtra("Title",title);
                    //get marker's location
                    myIntent.putExtra("LocationBundle",getLocationByTitle(title));

                    //start the intent
                    startActivity(myIntent);
                }else{
                    Toast.makeText(MapsActivityCurrentPlace.this, "You need to select a marker first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.voice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen();
            }
        });

        svBtn = findViewById(R.id.streetview);
        svBtn.setEnabled(false);


        // light sensor
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        light = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sm.registerListener(this, light, 100000);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        light_value = sensorEvent.values[0];
        if(light_value < -3 ){
            style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_night);
            mMap.setMapStyle(style);
        }else{
            style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_default);
            mMap.setMapStyle(style);
        }

        Log.e("Value","Light sensor value is " + light_value);

    }



    private Bundle getLocationByTitle(String title){

        Bundle locations = new Bundle();
        Cursor c = db.rawQuery("SELECT LocationX,LocationY FROM Landmarks WHERE Name = '"+ title +"'", null);
        c.moveToFirst();
        Double x = c.getDouble(0);
        Double y = c.getDouble(1);
        locations.putParcelable("to_position",new LatLng(x,y));
        return locations;
    }


    // Stop speaking when exit
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if (tts2 != null) {
            tts2.stop();
            tts2.shutdown();
        }
        super.onDestroy();
    }

    public void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void speak2(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts2.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            tts2.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void listen(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        try {
            startActivityForResult(i, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
        }
    }


    // Get user input from voice input
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String inSpeech = res.get(0);
                recognition(inSpeech);
            }
        }
    }


    // voice recognition method to match various user input
    public void recognition(String text){
        Log.e("Speech",""+text);
        //update marker title
        title = text;

        Toast.makeText(this, "You said:\n" + text, Toast.LENGTH_LONG).show();

        if(text.equalsIgnoreCase("UNC Student Store")){
            moveMarker(uss);
        }
        if(text.equalsIgnoreCase("Go to UNC Student Store")){
            navigateTo("UNC Student Store");
        }

        if(text.equalsIgnoreCase("Carolina Alumni Memorial")){
            moveMarker(cam);
        }
        if(text.equalsIgnoreCase("Go to Carolina Alumni Memorial")){
            navigateTo("Carolina Alumni Memorial");
        }

        if(text.equalsIgnoreCase("Davie Poplar")){
            moveMarker(dp);
        }
        if(text.equalsIgnoreCase("Go to Davie Poplar")){
            navigateTo("Davie Poplar");
        }

        if(text.equalsIgnoreCase("Forest Theatre")){
            moveMarker(ft);
        }
        if(text.equalsIgnoreCase("Go to Forest Theatre")){
            navigateTo("Forest Theatre");
        }

        if(text.equalsIgnoreCase("Coker Arboretum")){
            moveMarker(ca);
        }
        if(text.equalsIgnoreCase("Go to Coker Arboretum")){
            navigateTo("Coker Arboretum");
        }

        if(text.equalsIgnoreCase("Bell Tower")){
            moveMarker(mbt);
        }
        if(text.equalsIgnoreCase("Go to Bell Tower")){
            navigateTo("Bell Tower");
        }

        if(text.equalsIgnoreCase("Old East")){
            moveMarker(oe);
        }
        if(text.equalsIgnoreCase("Go to Old East")){
            navigateTo("Old East");
        }

        if(text.equalsIgnoreCase("Old Well")){
            moveMarker(ow);
        }
        if(text.equalsIgnoreCase("Go to Old Well")){
            navigateTo("Old Well");
        }

        if(text.equalsIgnoreCase("Playmakers Theater")){
            moveMarker(pt);
        }
        if(text.equalsIgnoreCase("Go to Playmakers Theater")){
            navigateTo("Playmakers Theater");
        }
    }

    // voice recognition help method for navigation
    public void navigateTo(String text) {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        title = text;
        // add method
        if(title!=null){
            Intent myIntent = new Intent(MapsActivityCurrentPlace.this,MapGps.class);

            myIntent.putExtra("Title",title);
            //get marker's location
            myIntent.putExtra("LocationBundle",getLocationByTitle(title));

            //start the intent
            startActivity(myIntent);
        }else{
            Toast.makeText(MapsActivityCurrentPlace.this, "You need to select a marker first", Toast.LENGTH_SHORT).show();
        }
    }

    // Dropdown box listener
    public class ItemSelectedListener implements AdapterView.OnItemSelectedListener {

        //get strings of first item
        String firstItem = String.valueOf(spinner1.getSelectedItem());

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (firstItem.equals(String.valueOf(spinner1.getSelectedItem()))) {
                // ToDo when first item is selected
            } else {
                String selectedItem = String.valueOf(spinner1.getSelectedItem());
                //update title
                title = selectedItem;
                if(selectedItem.equals("UNC Student Store")){
                    moveMarker(uss);
                }
                if(selectedItem.equals("Carolina Alumni Memorial")){
                    moveMarker(cam);

                }
                if(selectedItem.equals("Davie Poplar")){
                    moveMarker(dp);

                }
                if(selectedItem.equals("Forest Theatre")){
                    moveMarker(ft);

                }
                if(selectedItem.equals("Coker Arboretum")){
                    moveMarker(ca);

                }
                if(selectedItem.equals("Morehead-Patterson Bell Tower")){
                    moveMarker(mbt);

                }
                if(selectedItem.equals("Old East")){
                    moveMarker(oe);

                }
                if(selectedItem.equals("Old Well")){
                    moveMarker(ow);

                }
                if(selectedItem.equals("Playmakers Theater")){
                    moveMarker(pt);
                }
                Toast.makeText(parent.getContext(),
                        "You have selected : " + parent.getItemAtPosition(pos).toString(),
                        Toast.LENGTH_LONG).show();

                // Todo when item is selected by the user
            }
        }


        @Override
        public void onNothingSelected(AdapterView<?> arg) {

        }

    }

    // Switch marker and show info window without actually click on it
    public void moveMarker(Marker marker){
        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 250, null);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 250, null);
        marker.showInfoWindow();

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if (tts2 != null) {
            tts2.stop();
            tts2.shutdown();
        }

        tts2 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts2.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    speak2(des);

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });


    }

    // Add marker into google map
    private Marker addMarker(LatLng point, String title) {
        Marker marker=mMap.addMarker(new MarkerOptions()
                .position(point)
                .title(title));

        return marker;
    }

    // Create marker
    private void setUpMap() {
        mMap.setOnMarkerClickListener(this);
        uss=addMarker(new LatLng(35.90980520000001, -79.04834340000002), "UNC Student Store");
        cam=addMarker(new LatLng(35.907284, -79.045378), "Carolina Alumni Memorial");
        dp=addMarker(new LatLng(35.913092, -79.051660), "Davie Poplar");
        ft=addMarker(new LatLng(35.913715, -79.044944), "Forest Theatre");
        ca=addMarker(new LatLng(35.913823, -79.048991), "Coker Arboretum");
        mbt=addMarker(new LatLng(35.908874, -79.049238), "Morehead-Patterson Bell Tower");
        oe=addMarker(new LatLng(35.912593, -79.050869), "Old East");
        ow=addMarker(new LatLng(35.912360, -79.051219), "Old Well");
        pt=addMarker(new LatLng(35.916313, -79.053548), "Playmakers Theater");



    }

    private static final LatLng UNC = new LatLng(35.90980520000001, -79.04834340000002);

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UNC, 14));
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        // Set listener for marker click event.  See the bottom of this class for its behavior.
        mMap.setOnMarkerClickListener(this);
        // Setting an info window adapter allows us to change the both the contents and look of the
        // info window.
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnMapClickListener(this);
        enableMyLocation();


        setUpMap();

    }

    @Override
    public void onMapClick(LatLng point) {
        //mTapTextView.setText("tapped, point=" + point);

        mSelectedMarker = null;
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if (tts2 != null) {
            tts2.stop();
            tts2.shutdown();
        }

        svBtn.setEnabled(false);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        //mTapTextView.setText("long pressed, point=" + point);
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    boolean isClicked = true;



    @Override
    public boolean onMarkerClick(Marker marker) {

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }


        if (marker.equals(mSelectedMarker)) {
            // The showing info window has already been closed - that's the first thing to happen
            // when any marker is clicked.
            // Return true to indicate we have consumed the event and that we do not want the
            // the default behavior to occur (which is for the camera to move such that the
            // marker is centered and for the marker's info window to open, if it has one).
            mSelectedMarker = null;
            return true;
        }

        mSelectedMarker = marker;

        title=marker.getTitle();

        Toast.makeText(this, marker.getTitle().toString(), Toast.LENGTH_SHORT).show();

        // Return false to indicate that we have not consumed the event and that we wish
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 500, null);
        // for the default behavior to occur.

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    if(des!=null){
                        System.out.println(des);
                        speak(des);
                    }

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });

        svBtn.setEnabled(true);

        svBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MapsActivityCurrentPlace.this, MapPanorama.class);
                myIntent.putExtra("Marker", title);
                startActivity(myIntent);
            }
        });

        return false;
    }

    // initialize database
    public void resetDB(){
        db.execSQL("DROP TABLE IF EXISTS Landmarks");
        db.execSQL("CREATE TABLE IF NOT EXISTS Landmarks (ID TEXT,Name TEXT, LocationX DOUBLE, LocationY DOUBLE, Description TEXT)");
        db.execSQL("INSERT INTO Landmarks VALUES('cam','Carolina Alumni Memorial',35.907284, -79.045378,'The Carolina Alumni Memorial in Memory of Those Lost in Military Service, on Cameron Avenue between Phillips and Memorial halls, was dedicated in April 2007. The names of 684 known alumni who perished are listed in the memorial’s bronze Book of Names with pull-out panels.')");
        db.execSQL("INSERT INTO Landmarks VALUES('dp','Davie Poplar',35.913092, -79.051660,'This large tree marks the spot where, as legend has it, Revolutionary War General William R. Davie selected the site for the University. Actually, a six-man committee from the University’s first governing board chose the site on December 3, 1792.')");
        db.execSQL("INSERT INTO Landmarks VALUES('ft','Forest Theatre',35.913715, -79.044944,'The Forest Theatre is in Battle Park, where outdoor drama was first performed in 1916 to celebrate the 300th anniversary of Shakespeare’s death. W.C. Coker, faculty botanist who had developed the Arboretum nearby, chose the location.')");
        db.execSQL("INSERT INTO Landmarks VALUES('ca','Coker Arboretum',35.913823, -79.048991,'In 1903, Dr. William Chambers Coker, the university’s first professor of botany, began developing a five-acre boggy pasture into an outdoor university classroom for the study of trees, shrubs, and vines native to North Carolina. Beginning in the 1920s and continuing through the 1940s, Dr. Coker added many East Asian trees and shrubs. ')");
        db.execSQL("INSERT INTO Landmarks VALUES('mbt','Morehead-Patterson Bell Tower',35.908874, -79.049238,'Each hour of the day the Morehead-Patterson Bell Tower rings to remind students and faculty of the generosity of two families associated with the university since its earliest days.Seniors traditionally have the opportunity to climb the tower’s steps and savor the view a few days prior to May commencement.')");
        db.execSQL("INSERT INTO Landmarks VALUES('oe','Old East',35.912593, -79.050869,'The first building constructed to house America’s first state university. The cornerstone was laid on October 12, 1793.\n" +
                "\n" +
                "Nearly a century later, October 12 was declared Carolina’s birthday, or as folks on campus refer to it, University Day. The building was declared a national Historic Landmark in 1966.\n" +
                "\n" +
                "Today, a renovated Old East houses men and women students as a residence hall.')");
        db.execSQL("INSERT INTO Landmarks VALUES('ow','Old Well',35.912360, -79.051219,'At the heart of campus stands the visual symbol of the University of North Carolina at Chapel Hill. For many years the Old Well served as the sole water supply for Old East and Old West dormitories.')");
        db.execSQL("INSERT INTO Landmarks VALUES('pt','Playmakers Theater',35.916313, -79.053548,'The most beautiful building on the Carolina campus, to many tastes, is this Greek Revival temple considered to be one of the masterworks of New York architect Alexander Jackson Davis. He designed the building as an unlikely combination library and ballroom; later it was used for agricultural chemistry and law. For many years, it was the theatre of the Carolina Playmakers, who were largely responsible for developing folk drama in the United States.')");
        db.execSQL("INSERT INTO Landmarks VALUES('uss','UNC Student Store',35.90980520000001, -79.04834340000002,'One of the most modern campus stores in the United States, this $1.5 million contemporary structure provides textbooks, fiction and nonfiction books, computers and software, and many other items for the UNC-Chapel Hill campus.')");
    }

}
