package com.example.enkay.cifar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.enkay.cifar.Helper.BitmapHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Main2Activity extends AppCompatActivity {

    TextView address;
    TextView city;
    TextView country;
    TextView cropName;
    TextView price;
    ImageView cropImage;
    Button sendButton;
    String text_key="mytext";

    LocationManager locationManager;
    LocationListener locationListener;

    FirebaseDatabase database;
    FirebaseUser firebaseUser;
    DatabaseReference ref;
    Crop crop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        address= (TextView)findViewById(R.id.address);
        city=(TextView)findViewById(R.id.city);
        country=(TextView)findViewById(R.id.country);
        cropName=(TextView)findViewById(R.id.cropname);
        cropImage=(ImageView)findViewById(R.id.cropImage);
        sendButton=(Button)findViewById(R.id.sendButton);
        price=(TextView)findViewById(R.id.price);

        database= FirebaseDatabase.getInstance();
        ref = database.getReference("Crop");
        //crop= new Crop();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        Intent intent=getIntent();
        String name= intent.getStringExtra(text_key);
        cropName.setText(name);
        if(name=="cotton"){
            price.setText("Rs 4500/100 kg");
        }else if(name=="rice"){
            price.setText("Rs 3300/100 kg");
        }else{
            price.setText("Rs 275/100 kg");
        }
        cropImage.setImageBitmap(BitmapHelper.getInstance().getBitmap());

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnInsert();
            }
        });


        locationManager=(LocationManager)this.getSystemService(LOCATION_SERVICE);
        boolean isGPSEnabled= locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);

        if (isGPSEnabled) {

            locationListener=new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double longitude=location.getLongitude();
                    double latitute=location.getLatitude();

                    try {

                        Geocoder geocoder=new Geocoder(Main2Activity.this, Locale.getDefault());
                        List<Address> addressList=geocoder.getFromLocation(latitute,longitude,1);

                        address.setText(addressList.get(0).getAddressLine(0));
                        city.setText(addressList.get(0).getAdminArea());
                        country.setText(addressList.get(0).getCountryName());

                    }catch (IOException e){
                        e.printStackTrace();
                    }

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

        }else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
                address.setText("Getting Location");
                city.setText("Getting Location");
                country.setText("Getting Location");
            }
        }else{
            address.setText("Access denied");
            city.setText("Access denied");
            country.setText("Access denied");
        }

    }

    public void btnInsert(){
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //getValues();
                Toast.makeText(Main2Activity.this,"Data send to server",Toast.LENGTH_LONG).show();
                String cropname=cropName.getText().toString();
                String add= address.getText().toString();
                String City=city.getText().toString();
                String Country=country.getText().toString();
                String userId = ref.push().getKey();
                String Price=price.getText().toString();
                Crop crop=new Crop(userId,cropname,add,City,Country,Price);
                ref.child(userId).setValue(crop);
                //ref.push().setValue(crop);
                getValues();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getValues(){
        crop.setCropName(cropName.getText().toString());
        crop.setAddress(address.getText().toString());
        crop.setCity(city.getText().toString());
        crop.setCountry(country.getText().toString());
        crop.setPrice(price.getText().toString());

    }
}
