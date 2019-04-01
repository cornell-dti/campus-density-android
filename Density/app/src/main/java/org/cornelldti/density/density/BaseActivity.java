package org.cornelldti.density.density;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.util.Function;
import kotlin.Unit;

public class BaseActivity extends AppCompatActivity
        implements FirebaseAuth.IdTokenListener, FirebaseAuth.AuthStateListener {

    private transient String idToken;

    private FirebaseAuth auth;

    private ArrayList<Facility> all_facilities; // KEEPS TRACK OF ALL FACILITIES
    protected ArrayList<String> favFacilities; // origin/feat/favorites

    private int facility_occupancy_rating; // KEEPS TRACK OF SELECTED FACILITY'S OCCUPANCY

    private RequestQueue queue;

    private static final String FACILITY_LIST_ENDPOINT = "https://flux-backend-dev.herokuapp.com/v1/facilityList";
    private static final String FACILITY_INFO_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/facilityInfo";
    private static final String HOW_DENSE_ENDPOINT = "https://flux.api.internal.cornelldti.org/v1/howDense";

    public static final String OPERATING_HOURS_ENDPOINT =
            "https://flux.api.internal.cornelldti.org/v1/facilityHours";

    public static final String HISTORICAL_DATA_ENDPOINT =
            "https://flux.api.internal.cornelldti.org/v1/historicalData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);

        // origin/feat/favorites
        favFacilities = new ArrayList<>();
        initFavorites();

        auth = FirebaseAuth.getInstance();
        checkUserSignedIn();
        queue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkUserSignedIn();
    }

    // Invoked whenever ID Token changed!
    @Override
    public void onIdTokenChanged(@NonNull FirebaseAuth auth) {
        if (auth.getCurrentUser() != null) {
            requestToken(auth.getCurrentUser());
        } else {
            signIn();
        }
    }

    // When user is signed out, or lost access.
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
        // TODO DISPLAY ERROR SCREEN AND ATTEMPT TO RE SIGN IN
    }

    protected void updateUI() {
        // add implementation
    }

    public String getIdToken() {
        return idToken;
    }

    public void refreshToken() {
        requestToken(auth.getCurrentUser());
    }

    protected void requestToken(FirebaseUser user) {
        Log.d("checkpoint", "requestToken");
        user.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("checkpoint", "gotToken");
                            idToken = task.getResult().getToken();
                            updateUI();
                        } else {
                            Log.d("AUTH ERROR", "Error obtaining Firebase Auth ID token");
                        }
                    }
                });
    }

    private void checkUserSignedIn() {
        Log.d("checkpoint", "checkUserSignedIn");
//        auth.addIdTokenListener(this);
        auth.addAuthStateListener(this);
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            signIn();
        } else {
            requestToken(user);
        }

    }

    private void signIn() {
        Log.d("checkpoint", "signIn");
        auth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("checkpoint", "signIn = success");
                            Log.d("Firebase", "signInAnonymously:success");
                            FirebaseUser user = auth.getCurrentUser();
                            requestToken(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("checkpoint", "signIn = failure");
                            Log.w("Firebase", "signInAnonymously:failure", task.getException());
                            Toast.makeText(BaseActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // API HANDLING FUNCTIONS HERE

    public void fetchFacilitiesOnResponse(JSONArray response, boolean refresh, Function<Boolean, Void> success) {
        try {
            ArrayList<Facility> f = new ArrayList<Facility>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject facility = response.getJSONObject(i);
                f.add(new Facility(facility.getString("displayName"), facility.getString("id")));
            }
            fetchFacilityInfo(f, refresh, success);
        } catch (JSONException e) {
            success.apply(false);
            e.printStackTrace();
        }
    }

    public void fetchFacilitiesOnError(VolleyError error, Function<Boolean, Void> success) {
        Log.d("ERROR", error.toString());
        success.apply(false);
    }

    public void fetchFacilityOccupancyOnResponse(ArrayList<Facility> list, JSONArray response, boolean refresh, Function<Boolean, Void> success) {
        try {
            ArrayList<Facility> f_list = list;
            for (int i = 0; i < f_list.size(); i++) {
                for (int x = 0; x < response.length(); x++) {
                    JSONObject obj = response.getJSONObject(x);
                    if (obj.getString("id")
                            .equals(f_list.get(i).getId())) {
                        f_list.set(i, f_list.get(i).setOccupancy_rating(obj.getInt("density")));
                    }
                }
            }

            all_facilities = f_list;

        } catch (JSONException e) {
            success.apply(false);
            e.printStackTrace();
        }

    }

    // TODO Function returns a 403 Error Code!
    public void singleFacilityOccupancy(String facId) {
        JsonObjectRequest facilityRequest = new JsonObjectRequest
                (Request.Method.GET, HOW_DENSE_ENDPOINT + "?=" + facId, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("GOTOCCRATING", response.toString());
//                        try {
//                            facility_occupancy_rating = response.getInt("density");
//                        }
//                        catch(JSONException e)
//                        {
//                            e.printStackTrace();
//                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERRORSON", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + getIdToken());
                return headers;
            }
        };
        queue.add(facilityRequest);
    }

    /**
     * @return
     */
    public void fetchFacilities(final boolean refresh, Function<Boolean, Void> success) {
        JsonArrayRequest facilityListRequest = new JsonArrayRequest
                (Request.Method.GET, FACILITY_LIST_ENDPOINT, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("RESP1", response.toString());
                        fetchFacilitiesOnResponse(response, refresh, success);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        fetchFacilitiesOnError(error, success);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + getIdToken());
                return headers;
            }
        };
        queue.add(facilityListRequest);
    }

    public void fetchFacilityInfo(final ArrayList<Facility> list, final boolean refresh, Function<Boolean, Void> success) {
        JsonArrayRequest facilityInfoRequest = new JsonArrayRequest
                (Request.Method.GET, FACILITY_INFO_ENDPOINT, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("RESP2", response.toString());
                        try {
                            ArrayList<Facility> f_list = list;
                            for (int i = 0; i < f_list.size(); i++) {
                                for (int x = 0; x < response.length(); x++) {
                                    JSONObject obj = response.getJSONObject(x);
                                    if (obj.getString("id").equals(f_list.get(i).getId())) {
                                        Facility f = f_list.get(i);
                                        if (obj.has("campusLocation")) {
                                            f.setLocation(obj.getString("campusLocation"));
                                        }

                                        if (obj.has("description")) {
                                            f.setDescription(obj.getString("description"));
                                        }

                                        if (obj.has("closingAt")) {
                                            f.setClosingAt(obj.getLong("closingAt"));
                                        }

                                        f_list.set(i, f);
                                    }
                                }
                            }
                            fetchFacilityOccupancy(f_list, refresh, success);
                        } catch (JSONException e) {
                            success.apply(false);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BaseActivity.this, "Please check internet connection", Toast.LENGTH_LONG).show();
                        Log.d("ERROR MESSAGE", error.toString());
                        success.apply(false);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + getIdToken());
                return headers;
            }
        };
        queue.add(facilityInfoRequest);
    }

    public void fetchFacilityOccupancy(final ArrayList<Facility> list, final boolean refresh, Function<Boolean, Void> success) {
        JsonArrayRequest facilityOccupancyRequest = new JsonArrayRequest
                (Request.Method.GET, HOW_DENSE_ENDPOINT, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("RESP3", response.toString());
                        fetchFacilityOccupancyOnResponse(list, response, refresh, success);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        success.apply(false);
                        Toast.makeText(BaseActivity.this, "Please check internet connection", Toast.LENGTH_LONG).show();
                        Log.d("ERROR MESSAGE", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + getIdToken());
                return headers;
            }
        };
        queue.add(facilityOccupancyRequest);
    }

    public void fetchOperatingHoursOnResponse(JSONArray response, Function<Boolean, Void> success, String day) {
        // OVERRIDE IN FACILITYPAGE
    }

    public void fetchHistoricalJSONOnResponse(JSONArray response, Function<Boolean, Void> success, String day) {
        // OVERRIDE IN FACILITYPAGE
    }


    public void fetchOperatingHours(Function<Boolean, Void> success, String day, Facility facility) {
        JsonArrayRequest operatingHoursRequest = new JsonArrayRequest
                (Request.Method.GET, OPERATING_HOURS_ENDPOINT + "?id=" + facility.getId() + "&startDate=" + getDate(day) + "&endDate=" + getDate(day), null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        fetchOperatingHoursOnResponse(response, success, day);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Toast.makeText(FacilityPage.this, "Please check internet connection", Toast.LENGTH_LONG).show();
                        Log.d("ERROR MESSAGE", error.toString());
                        success.apply(false);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + getIdToken());
                return headers;
            }
        };
        queue.add(operatingHoursRequest);
    }

    public void fetchHistoricalJSON(Function<Boolean, Void> success, String day, Facility facility) {
        fetchOperatingHours(successOp -> null, day, facility);
        JsonArrayRequest historicalDataRequest = new JsonArrayRequest
                (Request.Method.GET, HISTORICAL_DATA_ENDPOINT + "?id=" + facility.getId(), null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        fetchHistoricalJSONOnResponse(response, success, day);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Toast.makeText(FacilityPage.this, "Please check internet connection", Toast.LENGTH_LONG).show();
                        Log.d("ERROR MESSAGE", error.toString());
                        success.apply(false);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + getIdToken());
                return headers;
            }
        };
        queue.add(historicalDataRequest);
    }

    /**
     * GETTER FUNCTION FOR ALL_FACILITIES LIST
     */
    public ArrayList<Facility> getAll_facilities() {
        return all_facilities;
    }

    /**
     * GETTER FUNCTION FOR SELECTED FACILITY'S OCCUPANCY RATING
     */
    public int getFacility_occupancy_rating() {
        return facility_occupancy_rating;
    }

    private String getDate(String day) {
        Calendar current = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yy");
        SimpleDateFormat checkFormat = new SimpleDateFormat("E");

        String dayCheck = checkFormat.format(current.getTime()).toUpperCase();
        while (!dayCheck.equals(day)) {
            current.add(Calendar.DAY_OF_MONTH, 1);
            dayCheck = checkFormat.format(current.getTime()).toUpperCase();
        }

        return format.format(current.getTime());
    }

    // origin/feat/favorites
    protected void toggleFavorite(Facility fac) {
        if (!favFacilities.contains(fac.getId()))
            favFacilities.add(fac.getId());
        else
            favFacilities.remove(fac.getId());
    }

    public ArrayList<String> getFavorites() {
        initFavorites();
        return favFacilities;
    }

    protected void initFavorites() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        favFacilities = new ArrayList<>();
        try {
            JSONArray favArray = new JSONArray(prefs.getString("favorites", "[]"));
            for (int i = 0; i < favArray.length(); i++) {
                String facId = favArray.getString(i);
                favFacilities.add(facId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void saveFavorites() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray favArray = new JSONArray();
        for (String id : favFacilities) {
            favArray.put(id);
        }
        editor.putString("favorites", favArray.toString());
        editor.apply();
    }

}
