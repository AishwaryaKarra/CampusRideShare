package com.zion.uniride;  // Replace with your package name 
import android.content.Intent; 
import android.os.Bundle; 
import android.view.View; 
import android.widget.Button; 
import android.widget.CheckBox; 
import android.widget.EditText; 
import android.widget.ImageView; 
import android.widget.Toast; 
import androidx.appcompat.app.AppCompatActivity; 
import com.google.firebase.auth.FirebaseAuth; 
public class RegistrationActivity extends AppCompatActivity { 
    private EditText usernameEditText, emailEditText, passwordEditText, phoneNumberEditText; 
    private CheckBox hasLicenseCheckBox; 
    private ImageView licenseImageView; 
    private Button uploadLicenseButton, registerButton; 
    private FirebaseAuth mAuth; 
    @Override 
    protected void onCreate(Bundle savedInstanceState) { 
         

super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_registration); 
        mAuth = FirebaseAuth.getInstance(); 
        // Bind UI elements 
        usernameEditText = findViewById(R.id.editText_username); 
        emailEditText = findViewById(R.id.editText_email); 
        passwordEditText = findViewById(R.id.editText_password); 
        phoneNumberEditText = findViewById(R.id.editText_phone_number); 
        hasLicenseCheckBox = findViewById(R.id.checkBox_has_license); 
        licenseImageView = findViewById(R.id.imageView_license); 
        uploadLicenseButton = findViewById(R.id.button_upload_license); 
        registerButton = findViewById(R.id.button_register); 
 
        // Handle license checkbox visibility 
        hasLicenseCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> { 
            licenseImageView.setVisibility(isChecked ? View.VISIBLE : View.GONE); 
            uploadLicenseButton.setVisibility(isChecked ? View.VISIBLE : View.GONE); 
        }); 
 
        // Register button click listener 
        registerButton.setOnClickListener(v -> { 
            String username = usernameEditText.getText().toString().trim(); 
            String email = emailEditText.getText().toString().trim(); 
            String password = passwordEditText.getText().toString().trim(); 
            String phoneNumber = phoneNumberEditText.getText().toString().trim(); 
 
            // Validate input fields (ensure non-empty) 
            if (validateInput(username, email, password, phoneNumber)) { 
                // Create user with FirebaseAuth 
                

 
 mAuth.createUserWithEmailAndPassword(email, password) 
                        .addOnCompleteListener(this, task -> { 
                            if (task.isSuccessful()) { 
                                // Registration successful 
                                Toast.makeText(RegistrationActivity.this, "Registration successful!", 
Toast.LENGTH_SHORT).show(); 
                                // Redirect to login or other appropriate activity 
                                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class)); 
                                finish(); 
                            } else { 
                                // Registration failed, handle errors 
                                Toast.makeText(RegistrationActivity.this, "Registration failed. " + 
task.getException().getMessage(), Toast.LENGTH_SHORT).show(); 
                            } 
                        }); 
            } 
        }); 
    } 
 
    private boolean validateInput(String username, String email, String password, String 
phoneNumber) { 
        // Separate validation methods for better readability 
 
        if (isEmptyField(username, "Username") || isEmptyField(email, "Email") 
                || isEmptyField(password, "Password") || isEmptyField(phoneNumber, "Phone Number")) 
{ 
            return false; 
        } 
 
        if (!isValidEmail(email)) { 
            showToast("Invalid email format"); 

 
 return false;  
          if (!isStrongPassword(password)) { 
            showToast("Password too weak. Ensure it's at least 8 characters and contains at least one 
digit."); 
            return false; 
        } 
        if (!isValidPhoneNumber(phoneNumber)) { 
            showToast("Invalid phone number format. Please enter a 10-digit number."); 
            return false; 
        } 
        return true;  // All validations passed 
    } 
    private boolean isEmptyField(String value, String fieldName) { 
        if (value.isEmpty()) { 
            showToast("Please enter " + fieldName); 
            return true; 
        } 
        return false; 
    } 
    private boolean isValidEmail(String email) { 
        return email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,}$"); 
    } 
    private boolean isStrongPassword(String password) { 
        return password.length() >= 8 && password.matches(".*[0-9].*"); 
    } 
    private boolean isValidPhoneNumber(String phoneNumber) { 
        return phoneNumber.matches("[0-9]{10}"); 
    } 
    private void showToast(String message) { 
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); 

 
    } 
}
