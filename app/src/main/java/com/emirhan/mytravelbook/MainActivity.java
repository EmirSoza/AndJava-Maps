package com.emirhan.mytravelbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    static ArrayList<String> names = new ArrayList<>();
    static  ArrayList<LatLng> locations = new ArrayList<>();
    static ArrayAdapter adapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_place, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add_place){
            //intent to map
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);

        try {

            MapsActivity.database = this.openOrCreateDatabase("places", MODE_PRIVATE,null);

            Cursor cursor = MapsActivity.database.rawQuery("SELECT * FROM places",null);
            int nameIx = cursor.getColumnIndex("name");

            int l1Ix = cursor.getColumnIndex("lat");

            int l2Ix = cursor.getColumnIndex("long");


            while(cursor.moveToNext()){
                String locName = cursor.getString(nameIx);

                Double locl1 = Double.parseDouble(cursor.getString(l1Ix));

                Double locl2 = Double.parseDouble(cursor.getString(l2Ix));

                LatLng location = new LatLng(locl1,locl2);
                names.add(locName);
                locations.add(location);




            }
            cursor.close();


        }catch (Exception e){
            e.printStackTrace();
        }
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, names);
        listView.setAdapter(adapter);
    }


}