//package org.cornelldti.density.density.util;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.util.Log;
//import android.view.View;
//import android.widget.Toast;
//
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.JsonArrayRequest;
//import com.android.volley.toolbox.JsonObjectRequest;
//import com.android.volley.toolbox.Volley;
//import com.google.android.gms.iid.InstanceID;
//
//import org.cornelldti.density.density.FacilitiesListAdapter;
//import org.cornelldti.density.density.Facility;
//import org.cornelldti.density.density.FacilityPage;
//import org.cornelldti.density.density.MainActivity;
//import org.cornelldti.density.density.R;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//import androidx.arch.core.util.Function;
//import androidx.fragment.app.FragmentTransaction;
//
//public class APIRequest
//{
//
//    private RequestQueue queue;
//
//    private static final String TOKEN_REQUEST_ENDPOINT = "https://us-central1-campus-density-backend.cloudfunctions.net/authv1";
//    private static final String FACILITY_LIST_ENDPOINT = "https://us-central1-campus-density-backend.cloudfunctions.net/facilityList";
//    private static final String FACILITY_INFO_ENDPOINT = "https://us-central1-campus-density-backend.cloudfunctions.net/facilityInfo";
//    private static final String HOW_DENSE_ENDPOINT = "https://us-central1-campus-density-backend.cloudfunctions.net/howDense";
//
//    private Context context;
//    private SharedPreferences pref;
//
//    public APIRequest(Context context, SharedPreferences pref)
//    {
//        this.context = context;
//        this.pref = pref;
//        queue = Volley.newRequestQueue(context);
//    }
//
//    private void requestToken() {
//        final String instanceId = InstanceID.getInstance(context).getId();
//        JSONObject requestBody = new JSONObject();
//        try {
//            requestBody.put("platform", "android");
//            requestBody.put("receipt", "");
//            requestBody.put("instanceId", instanceId);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        JsonObjectRequest tokenRequest = new JsonObjectRequest
//                (Request.Method.PUT, TOKEN_REQUEST_ENDPOINT, requestBody, new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            String token = response.getString("token");
//                            Log.d("TOKEN", token);
//                            pref.edit().putString("auth_token", token).commit();
//                            fetchFacilities(false, (success) -> null);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(context, "Please check internet connection", Toast.LENGTH_LONG).show();
//                        Log.d("ERROR MESSAGE", error.toString());
//                    }
//                }) {
//            @Override
//            public Map<String, String> getHeaders() {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Authorization", "Bearer " + context.getString(R.string.auth_key));
//                headers.put("x-api-key", instanceId);
//                return headers;
//            }
//        };
//        queue.add(tokenRequest);
//    }
//
//    private void fetchFacilities(final boolean refresh, Function<Boolean, Void> success) {
//        JsonArrayRequest facilityListRequest = new JsonArrayRequest
//                (Request.Method.GET, FACILITY_LIST_ENDPOINT, null, new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        try {
//                            ArrayList<Facility> f = new ArrayList<Facility>();
//                            for (int i = 0; i < response.length(); i++) {
//                                JSONObject facility = response.getJSONObject(i);
//                                f.add(new Facility(facility.getString("displayName"), facility.getString("id")));
//                            }
//                            fetchFacilityInfo(f, refresh, success);
//                        } catch (JSONException e) {
//                            success.apply(false);
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(context, "Please check internet connection", Toast.LENGTH_LONG).show();
//                        Log.d("ERROR MESSAGE", error.toString());
//                        success.apply(false);
//                    }
//                }) {
//            @Override
//            public Map<String, String> getHeaders() {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Authorization", "Bearer " + context.getString(R.string.auth_key));
//                headers.put("x-api-key", pref.getString("auth_token", ""));
//                return headers;
//            }
//        };
//        queue.add(facilityListRequest);
//    }
//
//    private void fetchFacilityInfo(final ArrayList<Facility> list, final boolean refresh, Function<Boolean, Void> success) {
//        JsonArrayRequest facilityInfoRequest = new JsonArrayRequest
//                (Request.Method.GET, FACILITY_INFO_ENDPOINT, null, new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        Log.d("RESP", response.toString());
//                        try {
//                            ArrayList<Facility> f_list = list;
//                            for (int i = 0; i < f_list.size(); i++) {
//                                for (int x = 0; x < response.length(); x++) {
//                                    JSONObject obj = response.getJSONObject(x);
//                                    if (obj.getString("id").equals(f_list.get(i).getId())) {
//                                        Facility f = f_list.get(i);
//                                        if (obj.has("campusLocation")) {
//                                            f.setLocation(obj.getString("campusLocation"));
//                                        }
//
//                                        if (obj.has("description")) {
//                                            f.setDescription(obj.getString("description"));
//                                        }
//
//                                        if (obj.has("closingAt")) {
//                                            f.setClosingAt(obj.getLong("closingAt"));
//                                        }
//
//                                        f_list.set(i, f);
//                                    }
//                                }
//                            }
//                            fetchFacilityOccupancy(f_list, refresh, success);
//                        } catch (JSONException e) {
//                            success.apply(false);
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(context, "Please check internet connection", Toast.LENGTH_LONG).show();
//                        Log.d("ERROR MESSAGE", error.toString());
//                        success.apply(false);
//                    }
//                }) {
//            @Override
//            public Map<String, String> getHeaders() {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Authorization", "Bearer " + context.getString(R.string.auth_key));
//                headers.put("x-api-key", pref.getString("auth_token", ""));
//                return headers;
//            }
//        };
//        queue.add(facilityInfoRequest);
//    }
//
//    private void fetchFacilityOccupancy(final ArrayList<Facility> list, final boolean refresh, Function<Boolean, Void> success) {
//        JsonArrayRequest facilityOccupancyRequest = new JsonArrayRequest
//                (Request.Method.GET, HOW_DENSE_ENDPOINT, null, new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        try {
//                            ArrayList<Facility> f_list = list;
//                            for (int i = 0; i < f_list.size(); i++) {
//                                for (int x = 0; x < response.length(); x++) {
//                                    JSONObject obj = response.getJSONObject(x);
//                                    if (obj.getString("id")
//                                            .equals(f_list.get(i).getId())) {
//                                        f_list.set(i, f_list.get(i).setOccupancyRating(obj.getInt("density")));
//                                    }
//                                }
//                            }
//
//                            allFacilities = f_list;
//
////                            if (!refresh) {
////                                MainActivity.this.adapter = new FacilitiesListAdapter(MainActivity.this.allFacilities);
////                                MainActivity.this.adapter.setOnItemClickListener(new FacilitiesListAdapter.ClickListener() {
////                                    @Override
////                                    public void onItemClick(int position, View v) {
////                                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
////                                        FacilityPage dialog = FacilityPage.newInstance(adapter.getDataSet().get(position));
////                                        dialog.show(ft, "facility page");
////                                    }
////                                });
////
////                                MainActivity.this.facilities.setAdapter(adapter);
////                                MainActivity.this.spinner.setVisibility(View.GONE);
////                                MainActivity.this.facilities.setVisibility(View.VISIBLE);
////                                setChipOnClickListener();
////                                setOnRefreshListener();
////                            } else {
////                                MainActivity.this.adapter.setDataSet(allFacilities);
////                                success.apply(true);
////                                switch (filterChips.getCheckedChipId()) {
////                                    case R.id.all:
////                                        adapter.showAllLocations();
////                                        break;
////
////                                    case R.id.north:
////                                        adapter.filterFacilitiesByLocation(Facility.CampusLocation.NORTH);
////                                        break;
////
////                                    case R.id.west:
////                                        adapter.filterFacilitiesByLocation(Facility.CampusLocation.WEST);
////                                        break;
////
////                                    case R.id.central:
////                                        adapter.filterFacilitiesByLocation(Facility.CampusLocation.CENTRAL);
////                                        break;
////
////                                    case -1:
////                                        all.setChecked(true);
////                                }
////                            }
//
//                        } catch (JSONException e) {
//                            success.apply(false);
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        success.apply(false);
//                        Toast.makeText(context, "Please check internet connection", Toast.LENGTH_LONG).show();
//                        Log.d("ERROR MESSAGE", error.toString());
//                    }
//                }) {
//            @Override
//            public Map<String, String> getHeaders() {
//                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Authorization", "Bearer " + context.getString(R.string.auth_key));
//                headers.put("x-api-key", pref.getString("auth_token", ""));
//                return headers;
//            }
//        };
//        queue.add(facilityOccupancyRequest);
//    }
//
//}
