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

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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

/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The
 * "My Location" button uses GMS Location to set the blue dot representing the users location.
 * Permission for {@link android.Manifest.permission#ACCESS_FINE_LOCATION} is requested at run
 * time. If the permission has not been granted, the Activity is finished with an error message.
 */
public class MapsActivityCurrentPlace extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener{

    /** Demonstrates customizing the info window and/or its contents. */
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
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
            // Use the equals() method on a Marker to check for equals.  Do not use ==.
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
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
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
    private TextView mTapTextView;
    private Marker mSelectedMarker;
    private Spinner spinner1;
    private PopupWindow popUpWindow;

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

    private Bundle markerBundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        db =  openOrCreateDatabase("MyDatabase", Context.MODE_PRIVATE, null);

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner1.setOnItemSelectedListener(new ItemSelectedListener());


        if (mLastSelectedMarker != null && mLastSelectedMarker.isInfoWindowShown()) {
            // Refresh the info window when the info window's content has changed.
            mLastSelectedMarker.showInfoWindow();
        }

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button button= (Button)findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               // add method
                if(title!=null){
                    Intent myIntent = new Intent(MapsActivityCurrentPlace.this,MapGps.class);

                    myIntent.putExtra("Title",title);
                    //get marker's location
                    myIntent.putExtra("LocationBundle",getLocationByTitle(title));

                    //start the intent
                    startActivity(myIntent);
                }else{
                }
            }
        });

        findViewById(R.id.voice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen();
            }
        });



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

    public void recognition(String text){
        Log.e("Speech",""+text);
        //update marker title
        title = text;

        if(text.equalsIgnoreCase("UNC Student Store")){
            moveMarker(uss);
        }
        if(text.equalsIgnoreCase("Carolina Alumni Memorial")){
            moveMarker(cam);

        }
        if(text.equalsIgnoreCase("Davie Poplar")){
            moveMarker(dp);

        }
        if(text.equalsIgnoreCase("Forest Theatre")){
            moveMarker(ft);

        }
        if(text.equalsIgnoreCase("Coker Arboretum")){
            moveMarker(ca);

        }
        if(text.equalsIgnoreCase("Morehead-Patterson Bell Tower")){
            moveMarker(mbt);

        }
        if(text.equalsIgnoreCase("Old East")){
            moveMarker(oe);

        }
        if(text.equalsIgnoreCase("Old Well")){
            moveMarker(ow);

        }
        if(text.equalsIgnoreCase("Playmakers Theater")){
            moveMarker(pt);
        }
    }

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

    private Marker addMarker(LatLng point, String title) {
        Marker marker=mMap.addMarker(new MarkerOptions()
                .position(point)
                .title(title));

        return marker;
    }

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
    }

    @Override
    public void onMapLongClick(LatLng point) {
        mTapTextView.setText("long pressed, point=" + point);
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

        return false;
    }
}
