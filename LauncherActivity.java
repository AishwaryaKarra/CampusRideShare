package com.zion.uniride; 
import android.content.Intent; 
import android.os.Bundle; 
import androidx.appcompat.app.AppCompatActivity; 
import com.zion.uniride.util.SessionManager; 
public class LauncherActivity extends AppCompatActivity { 
    @Override 
    protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        SessionManager sessionManager = SessionManager.getInstance(this); 
        Intent intent; 
        if (sessionManager.isUserLoggedIn()) { 
            // User is logged in, launch HomeActivity 
            intent = new Intent(this, HomeActivity.class); 
        } else { 
            // User is not logged in, launch LoginActivity 
            intent = new Intent(this, LoginActivity.class); 
        } 
        // Add intent flags 
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | 
Intent.FLAG_ACTIVITY_NEW_TASK); 
 
        startActivity(intent); 
        finish(); 
    } 
} 
