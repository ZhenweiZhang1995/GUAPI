package guapi.guapi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;

public class MapPanorama extends AppCompatActivity
        implements GoogleMap.OnMarkerDragListener, StreetViewPanorama.OnStreetViewPanoramaChangeListener {

    private static final String MARKER_POSITION_KEY = "MarkerPosition";

    private LatLng LOC;

    private StreetViewPanorama mStreetViewPanorama;

    private Marker mMarker;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_panorama);


        String rm = getIntent().getStringExtra("Marker");

        System.out.println(rm);

        switch (rm) {
            case "UNC Student Store":  LOC = new LatLng(35.90980520000001, -79.04834340000002);
                break;
            case "Carolina Alumni Memorial":  LOC = new LatLng(35.907284, -79.045378);
                break;
            case "Davie Poplar":  LOC = new LatLng(35.913092, -79.051660);
                break;
            case "Forest Theatre":  LOC = new LatLng(35.913715, -79.044944);
                break;
            case "Coker Arboretum":  LOC = new LatLng(35.913823, -79.048991);
                break;
            case "Morehead-Patterson Bell Tower":  LOC = new LatLng(35.908874, -79.049238);
                break;
            case "Old East":  LOC = new LatLng(35.912593, -79.050869);
                break;
            case "Old Well":  LOC = new LatLng(35.912360, -79.051219);
                break;
            case "Playmakers Theater":  LOC = new LatLng(35.916313, -79.053548);
                break;

        }


        final LatLng markerPosition;
        if (savedInstanceState == null) {
            markerPosition = LOC;
        } else {
            markerPosition = savedInstanceState.getParcelable(MARKER_POSITION_KEY);
        }

        SupportStreetViewPanoramaFragment streetViewPanoramaFragment =
                (SupportStreetViewPanoramaFragment)
                        getSupportFragmentManager().findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(
                new OnStreetViewPanoramaReadyCallback() {
                    @Override
                    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
                        mStreetViewPanorama = panorama;
                        mStreetViewPanorama.setOnStreetViewPanoramaChangeListener(
                                MapPanorama.this);

                        if (savedInstanceState == null) {
                            mStreetViewPanorama.setPosition(LOC);
                        }
                    }
                });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                map.setOnMarkerDragListener(MapPanorama.this);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LOC, 14));
                // Creates a draggable marker. Long press to drag.
                mMarker = map.addMarker(new MarkerOptions()
                        .position(markerPosition)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman))
                        .draggable(true));
            }
        });


    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MARKER_POSITION_KEY, mMarker.getPosition());
    }

    @Override
    public void onStreetViewPanoramaChange(StreetViewPanoramaLocation location) {
        if (location != null) {
            mMarker.setPosition(location.position);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mStreetViewPanorama.setPosition(marker.getPosition(), 150);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }



}
