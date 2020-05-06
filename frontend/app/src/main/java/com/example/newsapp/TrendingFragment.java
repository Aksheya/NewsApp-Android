package com.example.newsapp;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TrendingFragment extends Fragment {
    private RequestQueue queue;

    public TrendingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final View view = inflater.inflate(R.layout.trending_fragment, container, false);
        final EditText editText1 = (EditText) view.findViewById(R.id.trendingSearchText);

        makeVolleyRequest("",view);
        editText1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText1.getWindowToken(), 0);
                    makeVolleyRequest(editText1.getText().toString(), view);
                }
                return false;
            }
        });
        return view;
    }

    public void makeVolleyRequest(String keyword, View view){
        final String kWord = keyword;
        final View chartView = view;
        queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        String url="https://android-newapp-aks05.wl.r.appspot.com/google_trends?keyword="+ keyword;
        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ArrayList<Entry> entryValues = new ArrayList<>();
                            JSONArray timeArray = response.getJSONObject("default").getJSONArray("timelineData");
                            for(int i=0;i<timeArray.length();i++){
                                int timelineData = (int)timeArray.getJSONObject(i).getJSONArray("value").get(0);
                                entryValues.add(new Entry(i,timelineData));
                            }
                            prepareChart(entryValues, kWord, chartView);

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

        queue.add(jsonArrayRequest);

    }

    public void prepareChart(ArrayList<Entry> entries,String keyword, View view){
        LineDataSet dataSet = new LineDataSet(entries, "Trending chart for " + (keyword.equals("") ? "coronavirus" : keyword));
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSet.setColors(ColorTemplate.rgb("#7B7CBA"));
        dataSet.setValueTextColor(ColorTemplate.rgb("#7B7CBA"));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            dataSet.setCircleColor(getActivity().getColor(R.color.colorPrimary));
            dataSet.setCircleHoleColor(getActivity().getColor(R.color.colorPrimary));
        }
        dataSets.add(dataSet);
        dataSet.setValueTextSize(10);
        dataSet.setDrawHighlightIndicators(false);
        LineData data = new LineData(dataSets);
        LineChart mLineChart = view.findViewById(R.id.goolge_trends_chart);
        Legend legend = mLineChart.getLegend();
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(20);
        legend.setFormSize(17);
        mLineChart.getXAxis().setDrawGridLines(false);
        mLineChart.getAxisLeft().setDrawGridLines(false);
        mLineChart.getAxisRight().setAxisLineColor(getResources().getColor(R.color.grey));
        mLineChart.getAxisRight().setAxisLineWidth(1f);
        mLineChart.getXAxis().setTextSize(12);
        mLineChart.getXAxis().setAxisLineWidth(0.5f);
        mLineChart.getAxisRight().setDrawGridLines(false);
        mLineChart.getAxisLeft().setDrawAxisLine(false);
        mLineChart.getAxisRight().setTextSize(12);
        mLineChart.getAxisLeft().setTextSize(12);
        mLineChart.setData(data);
        mLineChart.invalidate();
        dataSet.setCircleColor(ColorTemplate.rgb("#7B7CBA"));
        mLineChart.setExtraOffsets(0, 10, 0, 10);
    }
}
