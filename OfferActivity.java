package com.zion.uniride.util; 
import android.Manifest; 
import android.content.Intent; 
import android.content.pm.PackageManager; 
import android.location.Address; 
import android.location.Geocoder; 
import android.location.Location; 
import android.os.Bundle; 
import android.util.Log; 
import android.widget.AdapterView; 
import android.widget.ArrayAdapter; 
import android.widget.AutoCompleteTextView; 
import android.widget.Button; 
import android.widget.DatePicker; 
import android.widget.EditText; 
import android.widget.TimePicker; 
import android.widget.Toast; 
import androidx.annotation.NonNull; 
import androidx.appcompat.app.AppCompatActivity; 
import androidx.core.app.ActivityCompat; 
import androidx.core.content.ContextCompat; 
import com.google.android.gms.location.FusedLocationProviderClient; 
import com.google.android.gms.location.LocationServices; 
import com.google.android.gms.maps.CameraUpdateFactory; 
import com.google.android.gms.maps.GoogleMap; 
import com.google.android.gms.maps.OnMapReadyCallback; 
import com.google.android.gms.maps.SupportMapFragment; 
import com.google.android.gms.maps.model.LatLng; 
38 
import com.google.android.gms.maps.model.MarkerOptions; 
import com.google.android.libraries.places.api.Places; 
import com.google.android.libraries.places.api.model.AutocompletePrediction; 
import com.google.android.libraries.places.api.model.Place; 
import com.google.android.libraries.places.api.net.FetchPlaceRequest; 
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest; 
import com.google.android.libraries.places.api.net.PlacesClient; 
import com.google.firebase.firestore.FirebaseFirestore; 
import com.zion.uniride.R; 
import java.io.IOException; 
import java.util.ArrayList; 
import java.util.Arrays; 
import java.util.Calendar; 
import java.util.HashMap; 
import java.util.List; 
import java.util.Locale; 
import java.util.Map; 
public class OfferRideActivity extends AppCompatActivity implements OnMapReadyCallback { 
private Button offerButton; 
private AutoCompleteTextView departureLocationEditText, destinationLocationEditText; 
private EditText availableSeatsEditText; 
private DatePicker datePicker; 
private TimePicker timePicker; 
private GoogleMap map; 
private PlacesClient placesClient; 
private FusedLocationProviderClient fusedLocationClient; 
private FirebaseFirestore db = FirebaseFirestore.getInstance(); 
private List<String> suggestionsList; 
private DatePicker departureDatePicker; 
private TimePicker departureTimePicker; 
   
 
@Override 
    protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.activity_offer_ride); 
        initializeViews(); 
        setupListeners(); 
    } 
    private void initializeViews() { 
        SupportMapFragment mapFragment = (SupportMapFragment) 
getSupportFragmentManager().findFragmentById(R.id.map_container); 
        assert mapFragment != null; 
        mapFragment.getMapAsync(this); 
        placesClient = Places.createClient(this); 
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this); 
 
        offerButton = findViewById(R.id.offer_button); 
        departureLocationEditText = findViewById(R.id.departure_location_edit_text); 
        destinationLocationEditText = findViewById(R.id.destination_location_edit_text); 
        availableSeatsEditText = findViewById(R.id.available_seats_edit_text); 
        departureDatePicker = findViewById(R.id.date_picker); 
        departureTimePicker = findViewById(R.id.time_picker); 
    } 
 
    private void setupListeners() { 
        offerButton.setOnClickListener(v -> onOfferButtonClick()); 
        departureLocationEditText.setOnItemClickListener((parent, view, position, id) -> { 
            AutocompletePrediction selectedPrediction = (AutocompletePrediction) 
parent.getItemAtPosition(position); 
            onLocationSuggestionSelected(parent, position, selectedPrediction); 
        }); 

  destinationLocationEditText.setOnItemClickListener((parent, view, position, id) -> { 
            AutocompletePrediction selectedPrediction = (AutocompletePrediction) 
parent.getItemAtPosition(position); 
            onLocationSuggestionSelected(parent, position, selectedPrediction); 
        }); 
    } 
    private void onOfferButtonClick() { 
        String departureLocation = departureLocationEditText.getText().toString().trim(); 
        String destinationLocation = destinationLocationEditText.getText().toString().trim(); 
        int availableSeats = parseSeatsInput(availableSeatsEditText.getText().toString().trim()); 
        Calendar date = getSelectedDate(); 
        LatLng departureLatLng = getLatLngFromAddress(departureLocation); 
        LatLng destinationLatLng = getLatLngFromAddress(destinationLocation); 
        if (departureLatLng == null || destinationLatLng == null) { 
            showToast("Please enter valid locations"); 
            return; 
        } 
        Map<String, Object> rideOffer = createRideOfferData(departureLocation, destinationLocation, 
availableSeats, date, departureLatLng, destinationLatLng); 
        writeRideOfferToFirestore(rideOffer); 
    } 
    private void onLocationSuggestionSelected(AdapterView<?> parent, int position, 
AutocompletePrediction selectedPrediction) { 
        // Extract place ID and address from selected suggestion 
        String placeId = selectedPrediction.getPlaceId(); 
        String address = selectedPrediction.getFullText(null).toString(); 
        // You can use the placeId or address for further actions, such as fetching details from Places 
API 
        // For example, you might want to get more details about the selected place: 
        fetchPlaceDetails(placeId); 
    } 

 
    private void fetchPlaceDetails(String placeId) { 
        // Use the Places API to fetch details about the selected place using placeId 
        List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME, 
Place.Field.ADDRESS); // Create a list of fields 
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields) 
                .build(); 
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> { 
            Place place = response.getPlace(); 
            LatLng location = place.getLatLng(); 
            String name = place.getName(); 
            String address = place.getAddress(); 
            // Perform actions with the fetched details, such as updating UI or storing in the database 
            // For example, you might want to update the location in your UI: 
            updateLocationInUI(location, name, address); 
        }).addOnFailureListener((exception) -> { 
            Log.e("OfferRideActivity", "Error fetching place details", exception); 
            Toast.makeText(OfferRideActivity.this, "Error fetching place details. Please try again.", 
Toast.LENGTH_SHORT).show(); 
        }); 
    } 
    private void updateLocationInUI(LatLng location, String name, String address) { 
        // Update your UI with the selected location details 
        // For example, you might want to set the selected location in the corresponding EditText field 
        departureLocationEditText.setText(address) 
        // You can also update the map if needed 
        map.clear(); 
        map.addMarker(new MarkerOptions().position(location).title(name).snippet(address)); 
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f)); 
    } 
    private int parseSeatsInput(String seatsInput) { 

 
        try { 
            return Integer.parseInt(seatsInput); 
        } catch (NumberFormatException e) { 
            showToast("Invalid number of seats"); 
            return 0; 
        } 
    } 
    private Calendar getSelectedDate() { 
        Calendar date = Calendar.getInstance(); 
        date.set(departureDatePicker.getYear(), departureDatePicker.getMonth(), 
departureDatePicker.getDayOfMonth()); 
        Calendar time = Calendar.getInstance(); 
        time.set(Calendar.HOUR_OF_DAY, departureTimePicker.getHour()); 
        time.set(Calendar.MINUTE, departureTimePicker.getMinute()); 
        date.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY)); 
        date.set(Calendar.MINUTE, time.get(Calendar.MINUTE)); 
        return date; 
    } 
    private Map<String, Object> createRideOfferData(String departureLocation, String 
destinationLocation, int availableSeats, Calendar date, LatLng departureLatLng, LatLng 
destinationLatLng) { 
        Map<String, Object> rideOffer = new HashMap<>(); 
        rideOffer.put("departureLocation", departureLocation); 
        rideOffer.put("destinationLocation", destinationLocation); 
        rideOffer.put("availableSeats", availableSeats); 
        rideOffer.put("departureDate", date.getTimeInMillis()); 
        rideOffer.put("departureLatLng", departureLatLng); 
        rideOffer.put("destinationLatLng", destinationLatLng);        // ... (add other fields as needed) 
        return rideOffer; 
    } 
 

 
    private void writeRideOfferToFirestore(Map<String, Object> rideOffer) { 
        db.collection("rideOffers").add(rideOffer) 
                .addOnSuccessListener(documentReference -> { 
                    showToast("Ride offer created successfully!"); 
                    finish(); 
                    startRideMatchingService(); 
                    navigateToOffersActivity(); 
                }) 
                .addOnFailureListener(e -> showToast("Failed to create ride offer")); 
    } 
    private void startRideMatchingService() { 
        Intent intent = new Intent(OfferRideActivity.this, RideMatchingService.class); 
        startService(intent); 
    } 
    private void navigateToOffersActivity() { 
        Intent intent = new Intent(OfferRideActivity.this, OffersActivity.class); 
        startActivity(intent); 
    } 
    private void showToast(String message) { 
        Toast.makeText(OfferRideActivity.this, message, Toast.LENGTH_SHORT).show(); 
    } 
    @Override 
    public void onMapReady(@NonNull GoogleMap googleMap) { 
        map = googleMap; 
 
        // Enable map features and set onMapClickListener 
        if (ActivityCompat.checkSelfPermission(this, 
Manifest.permission.ACCESS_FINE_LOCATION) != 
PackageManager.PERMISSION_GRANTED 
                && ActivityCompat.checkSelfPermission(this,  

 
Manifest.permission.ACCESS_COARSE_LOCATION) !=  
PackageManager.PERMISSION_GRANTED){            // TODO: Consider calling 
            //    ActivityCompat#requestPermissions 
            // here to request the missing permissions, and then overriding 
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions, 
            //                                          int[] grantResults) 
            // to handle the case where the user grants the permission. See the documentation 
            // for ActivityCompat#requestPermissions for more details. 
            return; 
        } 
        map.setMyLocationEnabled(true); 
        map.getUiSettings().setMyLocationButtonEnabled(true); 
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() { 
            @Override 
            public void onMapClick(@NonNull LatLng latLng) { 
                // Handle map click to set location (adjust logic as needed) 
                if (departureLocationEditText.hasFocus()) { 
                    departureLocationEditText.setText(getAddressFromLatLng(latLng)); 
                } else if (destinationLocationEditText.hasFocus()) { 
                    destinationLocationEditText.setText(getAddressFromLatLng(latLng)); 
                } 
                map.clear(); 
                map.addMarker(new MarkerOptions().position(latLng)); 
            } 
        }); 
        getCurrentLocation(); 
    } private LatLng getLatLngFromAddress(String address) { 
        try { 
             
 

List<Address> addresses = new Geocoder(this, Locale.getDefault()).getFromLocationName(address, 
1); 
            if (addresses.size() > 0) { 
                return new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()); 
            } 
        } catch (IOException e) { 
            e.printStackTrace(); 
        } 
        return null; 
    } 
    private String getAddressFromLatLng(LatLng latLng) { 
        try { 
            List<Address> addresses = new Geocoder(this, 
Locale.getDefault()).getFromLocation(latLng.latitude, latLng.longitude, 1); 
            if (addresses.size() > 0) { 
                Address address = addresses.get(0); 
                StringBuilder addressBuilder = new StringBuilder(); 
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) { 
                    addressBuilder.append(address.getAddressLine(i)).append(" "); 
                } 
                return addressBuilder.toString(); 
            } 
        } catch (IOException e) { 
            e.printStackTrace(); 
        } 
        return null; 
    } 
    private void fetchLocationSuggestions(String query) { 
        if (placesClient == null) { 
            // Handle missing PlacesClient initialization 

 
            Toast.makeText(OfferRideActivity.this, "Error: Location services not available.", 
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(OfferRideActivity.this, 
android.R.layout.simple_dropdown_item_1line, suggestionsList); 
                    // Set the adapter to the appropriate EditText (pickupLocationEditText or 
dropoffLocationEditText) 
                    departureLocationEditText.setAdapter(adapter); 
                    departureLocationEditText.setOnItemClickListener((parent, view, position, id) -> { 
                        AutocompletePrediction selectedPrediction = 
response.getAutocompletePredictions().get(position); // Get the AutocompletePrediction object 
                        onLocationSuggestionSelected(parent, position, selectedPrediction); 
                        // Extract place ID and address from selected suggestion 
                        String placeId = selectedPrediction.getPlaceId(); 
                        String address = selectedPrediction.getFullText(null).toString(); 
                        // ... (use place ID or address for further actions) 
                    }); 
                }) 
                .addOnFailureListener(exception -> { 
                    Log.e("GeocodingAdapter", "Error fetching location suggestions", exception); 
                    Toast.makeText(OfferRideActivity.this, "Error fetching suggestions. Please try again.", 
Toast.LENGTH_SHORT).show(); 
                }); 
    } 
    private void getCurrentLocation() { 
        // ... (implement using fusedLocationClient) 
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
                                    String addressText = currentAddress.getAddressLine(0); // Or format address  
                                    departureLocationEditText.setText(addressText); // Set address text in the 
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
                            Toast.makeText(OfferRideActivity.this, "Unable to retrieve current location.", 
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
} 
