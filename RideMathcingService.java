package com.zion.uniride.util; 
import static android.content.ContentValues.TAG; 
import android.content.Intent; 
import android.util.Log; 
import androidx.lifecycle.LifecycleService; 
import com.google.android.gms.tasks.Tasks; 
import com.google.firebase.firestore.FirebaseFirestore; 

 
import com.google.firebase.firestore.GeoPoint; 
import com.google.firebase.firestore.QuerySnapshot; 
import com.google.firebase.firestore.DocumentSnapshot; 
import java.io.Serializable; 
import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.List; 
import java.util.Map; 
import java.util.Objects; 
import java.util.concurrent.TimeUnit; 
import java.util.stream.Collectors; 
import io.reactivex.BackpressureStrategy; 
import io.reactivex.Flowable; 
import io.reactivex.schedulers.Schedulers; 
public class RideMatchingService extends LifecycleService { 
    private FirebaseFirestore firestore; 
    private static final long MATCHING_INTERVAL = 15; // Minutes 
    private static final long FLEXIBLE_TIME_WINDOW = 10; // Minutes (adjust as needed) 
    private static final double MAX_DISTANCE = 5.0; // Kilometers (adjust as needed) 
    private SharedViewModel viewModel; // Get a reference to the ViewModel 
    @Override 
    public void onCreate() { 
        super.onCreate(); 
        firestore = FirebaseFirestore.getInstance(); 
        startMatching(); 
    } 
    private void startMatching() { 
        Flowable<QuerySnapshot> requestQuery = Flowable.create(emitter -> { 
            try { 
 

                QuerySnapshot querySnapshot = Tasks.await(firestore.collection("rideRequests") 
                        .whereEqualTo("status", "pending") 
                        .get()); 
                emitter.onNext(querySnapshot); 
                emitter.onComplete(); 
            } catch (Exception e) { 
                emitter.onError(e); 
            } 
        }, BackpressureStrategy.BUFFER); 
        requestQuery.observeOn(Schedulers.io()) // You might want to observe on a different thread 
                .subscribe(querySnapshot -> { 
                    if (querySnapshot != null && !querySnapshot.getMetadata().isFromCache()) { 
                        List<HashMap<String, Object>> requestsList = 
querySnapshot.getDocuments().stream() 
                                .map(document -> new 
HashMap<>(Objects.requireNonNull(document.getData()))) 
                                .collect(Collectors.toList()); 
                        for (HashMap<String, Object> requestData : requestsList) { 
                            // Extract relevant request details (e.g., pickupLocation, dropoffLocation, 
requestTime, numPassengers) 
                            // ... 
                            // Find matching offers 
                            findMatchingOffers(requestData); 
                        } 
                    } else { 
                        // Handle task failure 
                    } 
                }, throwable -> { 
                    // Handle task failure 
                }); 
        // Repeat matching periodically 

        scheduleMatching(); 
    } 
    private void findMatchingOffers(HashMap<String, Object> requestData) { 
        // Extract relevant request details 
        GeoPoint pickupLocation = (GeoPoint) requestData.get("pickupLocation"); 
        long requestTime = (long) requestData.get("requestTime"); 
        int numPassengers = (int) requestData.get("numPassengers"); 
        // Retrieve potential matching offers 
        firestore.collection("rideOffers") 
                .whereEqualTo("status", "available") 
                .whereGreaterThanOrEqualTo("departureTime", requestTime - 
FLEXIBLE_TIME_WINDOW) 
                .whereLessThanOrEqualTo("departureTime", requestTime + 
FLEXIBLE_TIME_WINDOW) 
                .whereGreaterThanOrEqualTo("availableSeats", numPassengers) 
                .get() 
                .addOnSuccessListener(querySnapshot -> { 
                    List<HashMap<String, Object>> offersList = new ArrayList<>(); 
                    for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) { 
                        Map<String, Object> data = documentSnapshot.getData(); 
                        offersList.add((HashMap<String, Object>) data); 
                    } 
                    // Filter offers based on distance 
                    offersList = filterOffersByDistance(offersList, pickupLocation, MAX_DISTANCE); 
                    // Rank filtered offers based on criteria 
                    offersList = rankOffers(offersList, pickupLocation, requestTime); 
                    viewModel.setOffersList(offersList); 
                    // Pass the list of filtered and ranked offers to the UI component 
                    passAvailableOffersToUI(offersList); 
                }) 
      
 
 .addOnFailureListener(e -> { 
                    // Handle query failure 
                    Log.e(TAG, "Failed to retrieve offers: ", e); 
                    // Inform the user about the failure or take appropriate actions 
                }); 
    } 
    private void passAvailableOffersToUI(List<HashMap<String, Object>> offersList) { 
        Intent intent = new Intent(this, CardDisplayActivity.class); 
        intent.putExtra("offersList", (Serializable) offersList); // Assuming offersList is Serializable 
        startActivity(intent); 
    } 
    private void scheduleMatching() { 
        new android.os.Handler().postDelayed(this::startMatching, 
TimeUnit.MINUTES.toMillis(MATCHING_INTERVAL)); 
    } 
    // ... other methods for ranking, notifications, etc. 
    private List<HashMap<String, Object>> filterOffersByDistance(List<HashMap<String, Object>> 
offersList, GeoPoint pickupLocation, double maxDistance) { 
        return offersList.stream() 
                .filter(offer -> { 
                    GeoPoint offerPickupLocation = (GeoPoint) offer.get("pickupLocation"); 
                    return GeoUtils.calculateDistanceInKilometers(offerPickupLocation, pickupLocation) 
<= maxDistance; 
                }) 
                .collect(Collectors.toList()); 
    } 
    private List<HashMap<String, Object>> rankOffers(List<HashMap<String, Object>> offersList, 
GeoPoint pickupLocation, long requestTime) { 
        // Assign scores based on distance, time alignment, and other criteria 
         

 
// Example: 
        offersList.sort((offer1, offer2) -> { 
            double distance1 = GeoUtils.calculateDistanceInKilometers((GeoPoint) 
offer1.get("pickupLocation"), pickupLocation); 
            double distance2 = GeoUtils.calculateDistanceInKilometers((GeoPoint) 
offer2.get("pickupLocation"), pickupLocation); 
            // Add more factors for ranking as needed 
            return Double.compare(distance1, distance2); 
        }); 
        return offersList; 
    } 
    private HashMap<String, Object> selectBestMatch(List<HashMap<String, Object>> 
rankedOffers) { 
        // Choose the offer with the highest score (or apply other selection criteria) 
        if (!rankedOffers.isEmpty()) { 
            return rankedOffers.get(0); 
        } else { 
            return null; 
        } 
    } 
    private void processMatch(HashMap<String, Object> requestData, HashMap<String, Object> 
bestMatch) { 
        // Send notifications, update statuses, or perform other actions 
        // Example: 
        String requestId = (String) requestData.get("id"); 
        String offerId = (String) bestMatch.get("id"); 
        // ... update ride statuses in Firestore 
        // ... send notifications to users 
    } 
}
