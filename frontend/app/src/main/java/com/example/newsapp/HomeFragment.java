package com.example.newsapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements LocationListener {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private LocationManager locationManager;
    private String provider;
    private View view;
    private RecyclerView recyclerView;
    private List<Card> cardList;
    private Context context;

    public HomeFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home_fragment, container, false);
        context = view.getContext();
        Utility.setProgressBar(view, true);
        recyclerView = (RecyclerView) view.findViewById(R.id.homeFragmentCard);
        recyclerView.setLayoutManager(new LinearLayoutManager(context) {
        });
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        Utility util = new Utility();
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        provider = locationManager.getBestProvider(new Criteria(), false);
        checkLocationPermission();
        String url = "http://10.0.2.2:8080/home_fragment/";
        util.implementSwipe(url, context, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            provider = locationManager.getBestProvider(new Criteria(), false);
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        }
        if (cardList != null)
            recyclerView.setAdapter(new CardAdapter(cardList, "MainActivity", "listLayout", null));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Gd", "G");
                    provider = locationManager.getBestProvider(new Criteria(), false);
                    locationManager.requestLocationUpdates(provider, 400, 1, this);
                }
            }
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)
                callCardsApi();
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Please give location permission")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String cityName = addresses.get(0).getLocality();
            String stateName = addresses.get(0).getAdminArea();
            callWeatherApi(cityName, stateName);
            callCardsApi();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void callWeatherApi(final String cityName, final String stateName) {
        String weatherApiKey = "58a560f4f6e4f4e84965da2f9c7f5f90";
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&units=metric&appid=" + weatherApiKey;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String temperature = response.getJSONObject("main").getString("temp");
                            double d = Double.parseDouble(temperature);
                            int temp = ((int) Math.round(d));
                            String summary = response.getJSONArray("weather").getJSONObject(0).getString("main");
                            ((TextView) view.findViewById(R.id.weatherTemperature)).setText((temp) + "\u00B0" + "C");
                            ((TextView) view.findViewById(R.id.weatherCity)).setText(cityName);
                            ((TextView) view.findViewById(R.id.weatherState)).setText(stateName);
                            ((TextView) view.findViewById(R.id.weatherClimate)).setText(summary);
                            ImageView weatherImage = (ImageView) view.findViewById(R.id.weatherImage);
                            String uri = "";
                            switch (summary.toLowerCase()) {
                                case "clouds":
                                    uri = "@drawable/cloudy_weather";
                                    break;
                                case "clear":
                                    uri = "@drawable/clear_weather";
                                    break;
                                case "snow":
                                    uri = "@drawable/snowy_weather";
                                    break;
                                case "rain":
                                    uri = "@drawable/rainy_weather";
                                    break;
                                case "drizzle":
                                    uri = "@drawable/rainy_weather";
                                    break;
                                case "thunderstorm":
                                    uri = "@drawable/thunder_weather";
                                    break;
                                default:
                                    uri = "@drawable/sunny_weather";
                            }
                            int imageResource = getResources().getIdentifier(uri, null, getActivity().getPackageName());
                            Drawable res = getResources().getDrawable(imageResource);
                            weatherImage.setImageDrawable(res);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("MYERROR", error.toString());
                    }
                });
        queue.add(jsonObjectRequest);
    }

    public void callCardsApi() {
        Utility util = new Utility();
        String url = "http://10.0.2.2:8080/home_fragment/";
        util.getHomeFragmentData(url, context, new Utility.CallBack() {
            @Override
            public void dataLoaded(List<Card> cards) {
                cardList = cards;
                recyclerView.setAdapter(new CardAdapter(cards, "MainActivity", "listLayout", null));
                Utility.setProgressBar(view, false);
            }

            @Override
            public void dataError(String msg) {
                Log.d("ErrorMessage", msg);
            }
        });
    }

}











