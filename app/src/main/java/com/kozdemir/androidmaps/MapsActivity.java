package com.kozdemir.androidmaps;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kozdemir.androidmaps.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

//uzun tiklayinca cagirilan metod
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //onMapLongClickListener sinifina bulundugumuz MapsActivity bagliyoruz, yoksa hata verir
        mMap.setOnMapLongClickListener(this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // System.out.println("Location: " + location.toString());

                /*
                //kullanicinin yerini macker olarak gösterelim
                //ilk once kullanicinin konumunu Latitude ve Longitude olarak alalim
                mMap.clear(); //haritada secilen eski noktalar temizleniyor
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                */

                // Adres bilgisini lokasyona göre almak
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                try {
                    List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addressList != null && addressList.size() > 0) {
                        System.out.println("Adress: " + addressList.get(0).toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        };

        //izinleri alirken kontrol yapiyoruz, burada locasyonun iznini kontrol ediyoruz
        //API 23 oncesi ve sonrasi icin ayri kontrol
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1); //izin varsa requestQode = 1


        } else {
            //location
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            //son bilinen konumu getir, son bilinen konum yoksa null döner ***
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            //son bilinen konum varsa calistir
            if (lastLocation != null) {
                LatLng userLastLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(userLastLocation).title("Your Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLocation, 15));

            }


        }

/*
        // latitude, longitude
        LatLng eiffel = new LatLng(48.8573937, 2.2940337);
        mMap.addMarker(new MarkerOptions().position(eiffel).title("Eiffel Tower"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eiffel, 15));
*/
    }


    //ilk defa izin verildiginde
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {

        if (grantResults.length > 0) {
            if (requestCode == 1) { // izin verimis ise
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //Haritaya uzun tiklayinca calisiyor, hangi enlem boylama basiyorsan
    // LatLng ve Latlng degerlerini verir ve adres bilgisini aliyoruz
    @Override
    public void onMapLongClick(LatLng latLng) {

        mMap.clear(); //devamli uzun tiklanabilecegi icin sadece bir yerin adresini gösteriyoru, digerlerini siliyoruz

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        String address="";

        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addressList != null && addressList.size() > 0) {
               //adresin cadde/sokak numarasini verir
                if (addressList.get(0).getThoroughfare() != null) {
                    address +=addressList.get(0).getThoroughfare();

                    //adresin numarasini verir
                    if(addressList.get(0).getSubThoroughfare()!=null){
                        address +=" "+addressList.get(0).getThoroughfare();
                    }
                }


            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        //aldigimiz adresi marker gösteriyoruz
        mMap.addMarker(new MarkerOptions().position(latLng).title(address));


    }
}