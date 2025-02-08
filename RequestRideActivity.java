package com.zion.uniride.util; 
import android.Manifest; 
import android.content.Intent; 
import android.content.pm.PackageManager; 
import android.location.Address; 
import android.location.Geocoder; 
import android.location.Location; 
import android.os.Bundle; 
import android.text.Editable; 
import android.text.TextWatcher; 
import android.util.Log; 
import android.widget.ArrayAdapter; 
import android.widget.AutoCompleteTextView; 
import android.widget.Button; 
import android.widget.TextView; 
import android.widget.Toast; 
import androidx.annotation.NonNull; 
import androidx.appcompat.app.AppCompatActivity; 
import androidx.core.app.ActivityCompat; 
import androidx.core.content.ContextCompat; 
import com.google.android.gms.location.FusedLocationProviderClient; 
import com.google.android.gms.location.LocationServices; 
import com.google.android.gms.maps.GoogleMap; 
import com.google.android.gms.maps.OnMapReadyCallback; 
28 
 
import com.google.android.gms.maps.SupportMapFragment; 
import com.google.android.gms.maps.model.MarkerOptions; 
import com.google.android.libraries.places.api.Places; 
import com.google.android.libraries.places.api.model.AutocompletePrediction; 
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest; 
import com.google.android.libraries.places.api.net.PlacesClient; 
import com.google.firebase.auth.FirebaseAuth; 
import com.google.firebase.firestore.FirebaseFirestore; 
import com.zion.uniride.R; 
import java.io.IOException; 
import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.List; 
import java.util.Locale; 
import java.util.Map; 
import java.util.Objects; 
public class RequestRideActivity extends AppCompatActivity implements OnMapReadyCallback { 
 
    private AutoCompleteTextView pickupLocationEditText, dropoffLocationEditText; 
    private FusedLocationProviderClient fusedLocationClient; 
    private GoogleMap map; 
    private PlacesClient placesClient; 
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); 
    private final FirebaseAuth auth = FirebaseAuth.getInstance(); 
    private List<String> suggestionsList; 
    private static final int REQUEST_LOCATION_PERMISSION = 101; 
 
    @Override 
    protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 

 
  setContentView(R.layout.activity_request_ride); 
        placesClient = Places.createClient(this); 
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this); 
        Button requestButton = findViewById(R.id.request_button); 
        TextView estimatedFareTextView = findViewById(R.id.estimated_fare_text_view); 
 
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(); 
        getSupportFragmentManager().beginTransaction() 
                .add(R.id.map_container, mapFragment) 
                .commit(); 
        mapFragment.getMapAsync(this); 
 
        pickupLocationEditText = findViewById(R.id.pickup_location_edit_text); 
        dropoffLocationEditText = findViewById(R.id.dropoff_location_edit_text); 
 
        pickupLocationEditText.addTextChangedListener(new TextWatcher() { 
            @Override 
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { } 
 
            @Override 
            public void onTextChanged(CharSequence s, int start, int before, int count) { 
                fetchLocationSuggestions(s.toString()); 
            } 
 
            @Override 
            public void afterTextChanged(Editable s) { } 
        }); 
 
        dropoffLocationEditText.addTextChangedListener(new TextWatcher() { 
            

 
 @Override 
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { } 
 
            @Override 
            public void onTextChanged(CharSequence s, int start, int before, int count) { 
                fetchLocationSuggestions(s.toString()); 
            } 
 
            @Override 
            public void afterTextChanged(Editable s) { } 
        }); 
 
        requestButton.setOnClickListener(v -> { 
            String pickupLocationText = pickupLocationEditText.getText().toString(); 
            String dropoffLocationText = dropoffLocationEditText.getText().toString(); 
 
            if (pickupLocationText.isEmpty()) { 
                showToast("Please enter a pickup location"); 
                return; 
            } 
 
            if (dropoffLocationText.isEmpty()) { 
                showToast("Please enter a dropoff location"); 
                return; 
            } 
 
            // Calculate or fetch fare (optional) 
            // double estimatedFare = calculateFare(pickupLocationText, dropoffLocationText); 
            // estimatedFareTextView.setText("Estimated Fare: " + estimatedFare); 
 
   
 // Send ride request to backend server 
            Map<String, Object> rideRequest = new HashMap<>(); 
            rideRequest.put("userId", Objects.requireNonNull(auth.getCurrentUser()).getUid()); 
            rideRequest.put("pickupLocation", pickupLocationText); 
            rideRequest.put("dropoffLocation", dropoffLocationText); 
 
            sendRideRequest(rideRequest); 
        }); 
 
        // Get current location with permission check 
        handleLocationPermission(); 
    } 
    private void sendRideRequest(Map<String, Object> rideRequest) { 
        db.collection("rideRequests") 
                .add(rideRequest) 
                .addOnSuccessListener(documentReference -> { 
                    showToast("Request sent successfully!"); 
                    startRideMatchingService(); 
                    navigateToOffersActivity(); 
                }) 
                .addOnFailureListener(e -> showToast("Failed to send request: " + e.getMessage())); 
    } 
    private void requestLocationPermission() { 
        ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, 
Manifest.permission.ACCESS_COARSE_LOCATION}, 
                REQUEST_LOCATION_PERMISSION); 
    } 
 
    private void startRideMatchingService() { 
 
 
Intent intent = new Intent(RequestRideActivity.this, RideMatchingService.class); 
        startService(intent); 
    } 
    private void navigateToOffersActivity() { 
        Intent intent = new Intent(RequestRideActivity.this, OffersActivity.class); 
        startActivity(intent); 
    } 
    private void showToast(String message) { 
        Toast.makeText(RequestRideActivity.this, message, Toast.LENGTH_SHORT).show(); 
    } 
    // ... (other methods) 
// Implement other methods like fetchLocationSuggestions and getCurrentLocation as needed 
    @Override 
    public void onMapReady(@NonNull GoogleMap googleMap) { 
        map = googleMap; 
        getCurrentLocation(); 
        // Enable map features like zoom controls and location button 
        map.getUiSettings().setZoomControlsEnabled(true); 
        map.getUiSettings().setMyLocationButtonEnabled(true); 
        // Implement map-related functionalities here if needed 
        map.setOnMapClickListener(latLng -> { 
            // 1. Clear any existing markers (optional) 
            map.clear(); 
            // 2. Add a marker at the clicked location 
            map.addMarker(new MarkerOptions().position(latLng).title("Selected Location")); 
            // 3. Convert coordinates to address using Geocoder (optional) 
            Geocoder geocoder = new Geocoder(RequestRideActivity.this, Locale.getDefault()); 
            try { 
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 
1); 

 
assert addresses != null; 
                addresses.size();// Update address text field or display it in a dialog 
// Handle case where no address is found 
            } catch (IOException e) { 
                e.printStackTrace(); 
                // Handle Geocoder error 
            } 
 
            // 4. Update other UI elements or perform actions as needed 
            // Example: Update text fields for pickup/dropoff location, calculate estimated fare, etc. 
        }); 
    } 
    private void getCurrentLocation() { 
        if (ContextCompat.checkSelfPermission(this, 
Manifest.permission.ACCESS_FINE_LOCATION) == 
PackageManager.PERMISSION_GRANTED) { 
            fusedLocationClient.getLastLocation() 
                    .addOnCompleteListener(this, task -> { 
                        if (task.isSuccessful() && task.getResult() != null) { 
                            Location location = task.getResult(); 
                            double latitude = location.getLatitude(); 
                            double longitude = location.getLongitude(); 
                            // ... (map update code) 
                            // Get address from coordinates using Geocoder 
                            Geocoder geocoder = new Geocoder(this, Locale.getDefault()); 
                            try { 
                                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1); 
                                if (addresses != null && !addresses.isEmpty()) { 
                                    Address currentAddress = addresses.get(0); 
                                    String addressText = currentAddress.getAddressLine(0); // Or format address as needed 

 
 pickupLocationEditText.setText(addressText); // Set address text in the field 
                                } else { 
                                    Toast.makeText(this, "Unable to retrieve address for current location.", 
Toast.LENGTH_SHORT).show(); 
                                } 
                            } catch (IOException e) { 
                                e.printStackTrace(); 
                                Toast.makeText(this, "Error fetching address", 
Toast.LENGTH_SHORT).show(); 
                            } 
                        } else { 
                            // Handle Location not found error 
                            Toast.makeText(RequestRideActivity.this, "Unable to retrieve current location.", 
Toast.LENGTH_SHORT).show(); 
                        } 
                    }); 
        } else { 
            int REQUEST_LOCATION_PERMISSION = 101; 
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, 
Manifest.permission.ACCESS_COARSE_LOCATION}, 
                    REQUEST_LOCATION_PERMISSION); 
        } 
    } 
    private void fetchLocationSuggestions(String query) { 
        if (placesClient == null) { 
            // Handle missing PlacesClient initialization 
            Toast.makeText(RequestRideActivity.this, "Error: Location services not available.", 
Toast.LENGTH_SHORT).show(); 
            return; 
        } 

        if (query.isEmpty() || query.length() < 3) { 
            // Handle empty or short query 
            return; 
        } 
        String userInput = query.toLowerCase(); 
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder() 
                .setQuery(userInput) 
                .build(); 
        placesClient.findAutocompletePredictions(request) 
                .addOnSuccessListener(response -> { 
                    suggestionsList = new ArrayList<>(); // Initialize the list 
 
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) { 
                        suggestionsList.add(prediction.getFullText(null).toString()); 
                    } 
 
                    // Create an AutoCompleteTextView adapter 
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
android.R.layout.simple_dropdown_item_1line, suggestionsList); 
 
                    // Set the adapter to the appropriate EditText (pickupLocationEditText or 
dropoffLocationEditText) 
                    pickupLocationEditText.setAdapter(adapter); 
                    pickupLocationEditText.setOnItemClickListener((parent, view, position, id) -> { 
                        String selectedSuggestion = (String) parent.getItemAtPosition(position); 
                        AutocompletePrediction selectedPrediction = 
response.getAutocompletePredictions().get(position); 
                        // Extract place ID and address from selected suggestion 
                        String placeId = selectedPrediction.getPlaceId(); 
                        String address = selectedPrediction.getFullText(null).toString(); 
 

 
                       // ... (use place ID or address for further actions) 
                    }); 
                }) 
                .addOnFailureListener(exception -> { 
                    Log.e("GeocodingAdapter", "Error fetching location suggestions", exception); 
                    Toast.makeText(RequestRideActivity.this, "Error fetching suggestions. Please try 
again.", Toast.LENGTH_SHORT).show(); 
                }); 
    } 
    private void requestRide(String pickupLocationText, String dropoffLocationText) { 
        // Your existing logic for sending ride request to backend server 
        // ... 
        // Add other logic specific to ride request handling as needed 
    } 
    private void handleLocationPermission() { 
        if (ContextCompat.checkSelfPermission(this, 
Manifest.permission.ACCESS_FINE_LOCATION) == 
PackageManager.PERMISSION_GRANTED) { 
            getCurrentLocation(); 
        } else { 
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, 
Manifest.permission.ACCESS_COARSE_LOCATION}, 
                    REQUEST_LOCATION_PERMISSION); 
        } 
    } 
    private void handleLocationSuggestionsError(Exception exception) { 
        Log.e("GeocodingAdapter", "Error fetching location suggestions", exception); 
        Toast.makeText(RequestRideActivity.this, "Error fetching suggestions. Please try again.", 
Toast.LENGTH_SHORT).show(); 
    } 
 

}
