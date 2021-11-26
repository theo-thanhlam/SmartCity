/*
    Name:Thanh Phat Lam N01335598
    Course: CENG322-RND
    Purpose: Report Air Quality of the selected area
    Last updated: Nov 14 2021
*/


package ca.chesm.it.smartcity.View.FragMent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import ca.chesm.it.smartcity.R;
import de.hdodenhof.circleimageview.CircleImageView;


public class AirQualityFragment extends Fragment {

    View v;

    //AQI report components
    LinearLayout aqi_layout;
    TextView location, aqi_quality, aqi_condition;
    CircleImageView aqi_conditionemotion;

    //Pollutant components
    TextView pollutants_pm25, pollutants_co, pollutants_o3;

    //Daily graph components
    RadioButton rb_aqi, rb_co, rb_co2, rb_o3, rb_pm25, rb_so2, rb_no2;
    BarChart dailygraph;

    //Daily Graph data
    List<Float> o3daily = new ArrayList<>(), pm10daily,pm25daily;
    List<String> dateSupport;

    //API value
    public String aqivalue, pm25value, covalue, o3value;
    final String TOKEN = "335c6bfed754d30c7a80d76cd33f15a76c0f15c1";
    private String city_name = "toronto";
    private String url = "https://api.waqi.info/feed/" + city_name + "/?token=" + TOKEN;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_air_quality, container, false);
        getID();
        getAQIdata();
        //barChartEntry(dailygraph);

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void barChartEntry(BarChart graph) {
        List<BarEntry> entries = new ArrayList<>();
        for(int i=0;i<o3daily.size();i++){
            entries.add(new BarEntry(i, o3daily.get(i)));
        }
        for(float value:o3daily){
            Log.d("o3 daily:", String.valueOf(value));
        }
        BarDataSet set = new BarDataSet(entries, "BarDataSet");
        BarData data = new BarData(set);
        data.setBarWidth(0.5f);
        data.setValueTextSize(20f);
        graph.setData(data);
        graph.setFitBars(true);
        graph.invalidate();
    }


    public void getID() {
        location = v.findViewById(R.id.AQ_currentlocation);
        aqi_quality = v.findViewById(R.id.AQ_quality);
        aqi_condition = v.findViewById(R.id.AQ_condition);
        aqi_conditionemotion = v.findViewById(R.id.AQ_conditionemotion);
        aqi_layout = v.findViewById(R.id.aqilayout);

        pollutants_pm25 = v.findViewById(R.id.AQ_pm25);
        pollutants_co = v.findViewById(R.id.AQ_co);
        pollutants_o3 = v.findViewById(R.id.AQ_o3);

        rb_aqi = v.findViewById(R.id.rb_aqi);
        rb_co = v.findViewById(R.id.rb_co);
        rb_co2 = v.findViewById(R.id.rb_co2);
        rb_o3 = v.findViewById(R.id.rb_o3);
        rb_pm25 = v.findViewById(R.id.rb_pm25);
        rb_so2 = v.findViewById(R.id.rb_so2);
        rb_no2 = v.findViewById(R.id.rb_no2);
        dailygraph = v.findViewById(R.id.AQ_dailygraph);


    }


    private void getAQIdata() {
        new ReadJSONFeed().execute(url);

    }

    private void setPollutantsTextView() {

        aqi_quality.setText(aqivalue);
        pollutants_pm25.setText(pm25value);
        pollutants_co.setText(covalue);
        pollutants_o3.setText(o3value);
    }

    public String aqiConditionCheck() {
        try {
            int value = Integer.parseInt(aqivalue);
            if (value <= 50) {
                return "Good";
            } else if (value >= 51 && value <= 100) {
                return "Moderate";
            } else if (value > 100) {
                return "Unhealthy";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Not in range";

    }

    private void setAQITextView() {
        String aqi_result = aqiConditionCheck();
        aqi_condition.setText(aqi_result);
        switch (aqi_result) {
            case "Good":
                aqi_conditionemotion.setImageDrawable(getResources().getDrawable(R.drawable.aq_happy));
                aqi_layout.setBackgroundColor(getResources().getColor(R.color.aqi_good));
                break;
            case "Moderate":
                aqi_conditionemotion.setImageDrawable(getResources().getDrawable(R.drawable.aq_worry));
                aqi_layout.setBackgroundColor(getResources().getColor(R.color.aqi_normal));
                break;
            case "Unhealthy":
                aqi_conditionemotion.setImageDrawable(getResources().getDrawable(R.drawable.aq_angry));
                aqi_layout.setBackgroundColor(getResources().getColor(R.color.aqi_bad));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + aqi_result);
        }
    }

    private class ReadJSONFeed extends AsyncTask<String, Void, String> {
        private String readJSON(String address) {
            URL url = null;
            StringBuilder sb = new StringBuilder();
            HttpsURLConnection urlConnection = null;
            try {
                //Get URL
                url = new URL(address);
                //Open URL
                urlConnection = (HttpsURLConnection) url.openConnection();
                InputStream content = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
            return sb.toString();

        }

        @Override
        protected String doInBackground(String... urls) {
            return readJSON(urls[0]);
        }


        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject dataObject = new JSONObject(result).getJSONObject("data");

                getAQIvalue(dataObject);
                getPollutantsvalue(dataObject);
                getDailyGraphvalue(dataObject);
                setPollutantsTextView();
                setAQITextView();
                barChartEntry(dailygraph);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }


        }

        private void getAQIvalue(JSONObject dataObject) {
            try {

                aqivalue = dataObject.getString("aqi");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void getPollutantsvalue(JSONObject dataObject) {
            try {
                dataObject  =dataObject.getJSONObject("iaqi");
                pm25value = dataObject.getJSONObject("pm25").getString("v");
                covalue = dataObject.getJSONObject("co").getString("v");
                o3value = dataObject.getJSONObject("o3").getString("v");
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
        }

        private void getDailyGraphvalue(JSONObject dataObject) {
            try {
                dateSupport = new ArrayList<>();
                //o3daily = new ArrayList<>();
                JSONArray o3Array = dataObject.getJSONObject("forecast").getJSONObject("daily").getJSONArray("o3");

                for(int i=0;i<o3Array.length();i++){
                    JSONObject temp = o3Array.getJSONObject(i);
                    String date = temp.getString("day");
                    String o3dailyValue = temp.getString("avg");
                    dateSupport.add(date);
                    o3daily.add(Float.parseFloat(o3dailyValue));
                }

            } catch (JSONException e){
                e.printStackTrace();
            }

        }


    }
}





