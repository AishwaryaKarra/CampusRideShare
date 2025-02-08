package com.zion.uniride; 
import android.content.Intent; 
import android.os.Bundle; 
import android.view.View; 
import android.widget.Button; 
import androidx.appcompat.app.AppCompatActivity; 
import com.zion.uniride.util.OfferRideActivity; 
import com.zion.uniride.util.RequestRideActivity; 
import com.zion.uniride.util.SessionManager; 
public class HomeActivity extends AppCompatActivity { 
    private Button requestRideButton; 
    private Button offerRideButton; 
    @Override 
    protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_home); 
        requestRideButton = findViewById(R.id.request_ride_button); 
        offerRideButton = findViewById(R.id.offer_ride_button); 
        if (SessionManager.getInstance(this).isUserLoggedIn()) { 
            // User is logged in, proceed with HomeActivity features 
            requestRideButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, 
RequestRideActivity.class))); 
            offerRideButton.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, 
OfferRideActivity.class))); 
        } else { 
            // User is not logged in, redirect to LoginActivity 
 
 
startActivity(new Intent(HomeActivity.this, LoginActivity.class)); 
finish(); 
} 
} 
} 
