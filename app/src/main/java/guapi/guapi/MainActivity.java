package guapi.guapi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by jiayi on 26/11/2017.
 */

public class MainActivity extends AppCompatActivity {

    private ImageButton btn_gps;
    private ImageButton btn_tour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        btn_gps = findViewById(R.id.gps);
        btn_tour = findViewById(R.id.tour);

        btn_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start new activity class
                Intent myIntent = new Intent(MainActivity.this,MapsActivity.class);
                startActivity(myIntent);
            }
        });


    }
}
