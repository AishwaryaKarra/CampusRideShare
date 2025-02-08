package com.zion.uniride; 
import android.app.Activity; 
import android.app.Application; 
import android.content.Intent; 
import com.google.android.libraries.places.api.Places; 
import com.google.firebase.auth.FirebaseAuth; 
import com.google.firebase.database.FirebaseDatabase; 
public class MyApplication extends Application { 
    private FirebaseAuth mAuth; 
    private FirebaseDatabase mDatabase; 
    @Override 
    public void onCreate() { 
        super.onCreate(); 
        // Initialize Firebase Auth and Database 
        mAuth = FirebaseAuth.getInstance(); 
        mDatabase = FirebaseDatabase.getInstance();
 
        Places.initialize(this,"AIzaSyDhOQB7xqLFgWtWNNk4Sg9izuxBfStpC4s"); 
        // Optional: Add any additional custom initialization logic here 
        // Example: Initialize shared preferences 
        // SharedPreferences prefs = getSharedPreferences("my_app_prefs", MODE_PRIVATE); 
        // Example: Create singleton instance of a service class 
        // MyService.getInstance(this); 
    } 
    public FirebaseAuth getFirebaseAuth() { 
        return mAuth; 
    } 
    public FirebaseDatabase getFirebaseDatabase() { 
        return mDatabase; 
    } 
}
