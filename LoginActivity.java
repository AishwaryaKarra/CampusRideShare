package com.zion.uniride; 
import android.content.Intent; 
import android.os.Bundle; 
import android.text.InputType; 
import android.util.Log; 
import android.view.MotionEvent; 
import android.widget.Button; 
import android.widget.EditText; 
import android.widget.TextView; 
import android.widget.Toast; 
import androidx.appcompat.app.AppCompatActivity; 
import androidx.core.content.ContextCompat; 
import com.google.firebase.FirebaseNetworkException; 
import com.google.firebase.auth.FirebaseAuth; 
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException; 
import com.zion.uniride.util.SessionManager; 
public class LoginActivity extends AppCompatActivity { 
private Button loginButton; 
private EditText emailEditText; 
private EditText passwordEditText; 
private FirebaseAuth mAuth; 
private static final String TAG = "com.zion.uniride.LoginActivity"; 

 
    @Override 
    protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        setTheme(R.style.AppTheme1); 
        setContentView(R.layout.activity_login); 
        mAuth = FirebaseAuth.getInstance(); 
        loginButton = findViewById(R.id.login_button); 
        emailEditText = findViewById(R.id.email_edit_text); 
        passwordEditText = findViewById(R.id.password_edit_text); 
        // Password visibility toggle (implementation omitted for brevity) 
        // Login button click listener 
        loginButton.setOnClickListener(v -> { 
            String email = emailEditText.getText().toString().trim(); 
            String password = passwordEditText.getText().toString().trim(); 
            // Check for empty email or password before proceeding 
            if (email.isEmpty()) { 
                Toast.makeText(LoginActivity.this, "Please enter your email address.", 
Toast.LENGTH_SHORT).show(); 
                return; 
            } else if (password.isEmpty()) { 
                Toast.makeText(LoginActivity.this, "Please enter your password.", 
Toast.LENGTH_SHORT).show(); 
                return; 
            } 
            loginButton.setEnabled(false); 
            // Login with Firebase Auth and handle success/failure 
            mAuth.signInWithEmailAndPassword(email, password) 
                    .addOnCompleteListener(this, task -> { 
                        loginButton.setEnabled(true); 
                        if (task.isSuccessful()) { 
 
 
    // Check if user is logged in 
                            if (mAuth.getCurrentUser() != null) { 
                                // User is logged in, update session manager and launch HomeActivity 
                                SessionManager.getInstance(LoginActivity.this).setUserLoggedIn(true); 
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class); 
                                startActivity(intent); 
                            } else { 
                                // Handle unexpected error or missing user information 
                                Log.w(TAG, "signInWithEmailAndPassword: unexpected null user"); 
                                Toast.makeText(LoginActivity.this, "An unexpected error occurred. Please try 
again.", Toast.LENGTH_SHORT).show(); 
                            } 
                        } else { 
                            // Handle login failure with specific error messages 
                            Exception e = task.getException(); 
                            if (e instanceof FirebaseAuthInvalidCredentialsException) { 
                                Toast.makeText(LoginActivity.this, "Invalid email or password. Please try 
again.", Toast.LENGTH_SHORT).show(); 
                            } else if (e instanceof FirebaseNetworkException) { 
                                Toast.makeText(LoginActivity.this, "Network error. Please try again later.", 
Toast.LENGTH_SHORT).show(); 
                            } else { 
                                Log.w(TAG, "signInWithEmailAndPassword:failure", e); 
                                Toast.makeText(LoginActivity.this, "An unexpected error occurred. Please try 
again.", Toast.LENGTH_SHORT).show(); 
                            } 
                        } 
                    }); 
        }); 
 
        // Add forgot password link (implementation omitted for brevity)
 
        // Add "New User" text field 
        TextView newUserTextView = findViewById(R.id.new_user_text_view); 
        newUserTextView.setOnClickListener(v -> { 
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class); // Assuming you 
have "RegistrationActivity.java" created 
            startActivity(intent); 
        }); 
    } 
} 
