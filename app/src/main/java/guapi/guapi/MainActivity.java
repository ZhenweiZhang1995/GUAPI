package guapi.guapi;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
    SQLiteDatabase db = null;

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
                Intent myIntent = new Intent(MainActivity.this,MapsActivityCurrentPlace.class);
                startActivity(myIntent);
            }
        });

        db = openOrCreateDatabase("MyDatabase", Context.MODE_PRIVATE,null);
        resetDB();



    }

    public void resetDB(){
        db.execSQL("DROP TABLE IF EXISTS Landmarks");
        db.execSQL("CREATE TABLE IF NOT EXISTS Landmarks (ID TEXT,Name TEXT, Location TEXT, Description TEXT)");
        db.execSQL("INSERT INTO Landmarks VALUES('cam','Carolina Alumni Memorial','35.907284, -79.045378','The Carolina Alumni Memorial in Memory of Those Lost in Military Service, on Cameron Avenue between Phillips and Memorial halls, was dedicated in April 2007. The names of 684 known alumni who perished are listed in the memorial’s bronze Book of Names with pull-out panels. They also are listed on the General Alumni Association’s website, grouped alphabetically by conflict, from the U.S. Civil War to the 1990-91 Persian Gulf War.')");
        db.execSQL("INSERT INTO Landmarks VALUES('dp','Davie Poplar','35.913092, -79.051660','This large tree marks the spot where, as legend has it, Revolutionary War General William R. Davie selected the site for the University. Actually, a six-man committee from the University’s first governing board chose the site on December 3, 1792.')");
        db.execSQL("INSERT INTO Landmarks VALUES('ft','Forest Theatre','35.913715, -79.044944','The Forest Theatre is in Battle Park, where outdoor drama was first performed in 1916 to celebrate the 300th anniversary of Shakespeare’s death. W.C. Coker, faculty botanist who had developed the Arboretum nearby, chose the location.')");
        db.execSQL("INSERT INTO Landmarks VALUES('ca','Coker Arboretum','35.913823, -79.048991','In 1903, Dr. William Chambers Coker, the university’s first professor of botany, began developing a five-acre boggy pasture into an outdoor university classroom for the study of trees, shrubs, and vines native to North Carolina. Beginning in the 1920s and continuing through the 1940s, Dr. Coker added many East Asian trees and shrubs. ')");
        db.execSQL("INSERT INTO Landmarks VALUES('mbt','Morehead-Patterson Bell Tower','35.908874, -79.049238','Each hour of the day the Morehead-Patterson Bell Tower rings to remind students and faculty of the generosity of two families associated with the university since its earliest days.Seniors traditionally have the opportunity to climb the tower’s steps and savor the view a few days prior to May commencement.')");
        db.execSQL("INSERT INTO Landmarks VALUES('oe','Old East','35.912593, -79.050869','The first building constructed to house America’s first state university. The cornerstone was laid on October 12, 1793.\n" +
                "\n" +
                "Nearly a century later, October 12 was declared Carolina’s birthday, or as folks on campus refer to it, University Day. The building was declared a national Historic Landmark in 1966.\n" +
                "\n" +
                "Today, a renovated Old East houses men and women students as a residence hall.')");
        db.execSQL("INSERT INTO Landmarks VALUES('ow','Old Well','35.912360, -79.051219','At the heart of campus stands the visual symbol of the University of North Carolina at Chapel Hill. For many years the Old Well served as the sole water supply for Old East and Old West dormitories.')");
        db.execSQL("INSERT INTO Landmarks VALUES('pt','Playmakers Theater','35.916313, -79.053548','The most beautiful building on the Carolina campus, to many tastes, is this Greek Revival temple considered to be one of the masterworks of New York architect Alexander Jackson Davis. He designed the building as an unlikely combination library and ballroom; later it was used for agricultural chemistry and law. For many years, it was the theatre of the Carolina Playmakers, who were largely responsible for developing folk drama in the United States. Instead of the acanthus leaves that usually ornament Corinthian capitals, Davis substituted wheat and Indian corn, in response to the aggressive Americanism then present in the country.')");
           }
}
