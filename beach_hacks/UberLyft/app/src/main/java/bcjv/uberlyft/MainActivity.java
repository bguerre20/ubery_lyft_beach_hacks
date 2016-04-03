package bcjv.uberlyft;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;

public class MainActivity extends ActionBarActivity {

    TextView typeTView;
    TextView priceTView;
    TextView durationTView;
    TextView etaUView;

    TextView typeLView;
    TextView priceLView;
    TextView durationLView;
    TextView etaLView;

    EditText start;
    EditText end;

    String startText;
    String endText;

    //37.7772&start_lng=-122.4233&end_lat=37.7972&end_lng=-122.4533

    public String sLat = "37.7772";
    public String sLng = "-122.4233";
    public String eLat = "37.7972";
    public String eLng = "-122.4533";

    String type = "";
    String price = "";
    String duration = "";
    String etaU = "";

    String typeL = "";
    String priceL = "";
    String durationL = "";
    String etaL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        typeTView = (TextView)findViewById(R.id.typeV);
        priceTView = (TextView)findViewById(R.id.priceV);
        durationTView = (TextView)findViewById(R.id.durationV);
        etaUView = (TextView)findViewById(R.id.UetaV);

        typeLView = (TextView)findViewById(R.id.LtypeV);
        priceLView = (TextView)findViewById(R.id.LpriceV);
        durationLView = (TextView)findViewById(R.id.LdurationV);
        etaLView = (TextView)findViewById(R.id.LetaV);

        start = (EditText)findViewById(R.id.editTextStart);
        end = (EditText)findViewById(R.id.editTextEnd);

       // Context context = getApplicationContext();
        //Toast.makeText(context, response.getStatusLine().toString(), Toast.LENGTH_SHORT).show();
    }

    public void clicked(View view) {
        startText = start.getText().toString();
        endText = end.getText().toString();

        new MyOtherTask().execute("");//google lat and long

        new MyTask().execute(""); // uber price stuff

        new MyThirdTask().execute(""); // lyft price stuff

        new MyOtherThirdTask().execute(""); //lyft ETA

        new MyOtherUberTask().execute(""); //uber ETA
    }


    private class MyTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String uberStatusCode = getUberPrice();
            return uberStatusCode;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Context context = getApplicationContext();
            //Toast.makeText(context, result, Toast.LENGTH_SHORT).show();

            priceTView.setText("PRICE: " + price);
            durationTView.setText("DURATION: " + duration);
            typeTView.setText("RIDE TYPE: " + type);
        }
    }

    private class MyOtherTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String status = "-1";
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet();

                String unParsedAddress = startText; //test address
                String parsedAddress = parseAddress(unParsedAddress);
                URI uri = new URI("https://maps.googleapis.com/maps/api/geocode/json?" +
                        "address=" + parsedAddress +
                        "&key=AIzaSyAGTodLDHtOlNFK3Z6H7X5pHxQtzcVu-lw");
                httpGet.setURI(uri);

                HttpResponse httpResponse = httpClient.execute(httpGet);
                getLatLng(httpResponse, 0);
                status = httpResponse.getStatusLine().toString();
                String almostParsed = parseUberJSONResponse(httpResponse);

            }
            catch (Exception e) {
                e.printStackTrace();
            }

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet();

                String unParsedAddress = endText; //test address
                String parsedAddress = parseAddress(unParsedAddress);
                URI uri = new URI("https://maps.googleapis.com/maps/api/geocode/json?" +
                        "address=" + parsedAddress +
                        "&key=AIzaSyAGTodLDHtOlNFK3Z6H7X5pHxQtzcVu-lw");
                httpGet.setURI(uri);

                HttpResponse httpResponse = httpClient.execute(httpGet);
                getLatLng(httpResponse, 1);
                status = httpResponse.getStatusLine().toString();
                String almostParsed = parseUberJSONResponse(httpResponse);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }
    }

    private String getUberPrice() {
        String status = "-1";

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet();

            URI uri = new URI("https://api.uber.com/v1/estimates/price?" +
                    "&server_token=xHire5cK16V9eQgU_MhA-y6vs2OZqHKLTtk0YiBF" +
                    "&start_latitude=" + sLat +
                    "&start_longitude=" + sLng  +
                    "&end_latitude=" + eLat +
                    "&end_longitude="+ eLng);
            httpGet.setURI(uri);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            status = httpResponse.getStatusLine().toString();
            String almostParsed = parseUberJSONResponse(httpResponse);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }

    private String parseUberJSONResponse(HttpResponse hresponse) {
        try {
            String result = EntityUtils.toString(hresponse.getEntity());
            JSONObject estimateDetails = new JSONObject(result);
            JSONArray data = estimateDetails.getJSONArray("prices");
            JSONObject uberXEst = data.getJSONObject(0);
            type = uberXEst.getString("localized_display_name");
            price = uberXEst.getString("estimate");
            String temp = uberXEst.getString("duration");
            duration = Double.parseDouble(temp) / 60 + "min";
            System.out.println("test");
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return "";
    }


    private String getLatLng(HttpResponse address, int which) {

        try {
            String result = EntityUtils.toString(address.getEntity());
            JSONObject topLevelJSON = new JSONObject(result);
            JSONArray data = topLevelJSON.getJSONArray("results");
            JSONObject entireArray = data.getJSONObject(0);
            JSONObject geometry = entireArray.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");
            if (which == 0) {
                sLat = location.getString("lat");
                sLng = location.getString("lng");
            }
            else {
                eLat = location.getString("lat");
                eLng = location.getString("lng");
            }
            System.out.println("test");
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private String parseAddress(String address) {
        return address.replaceAll("\\s+", "+");
    }

    private class MyThirdTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String lyftCode = getLyftPrice();
            //String otherLyftCode = getLyftETA();
            return lyftCode;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            priceLView.setText("PRICE: " + priceL);
            durationLView.setText("DURATION: " + durationL);
            typeLView.setText("RIDE TYPE: " + typeL);



        }
    }

    private String getLyftPrice() {
        String status = "-1";

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet("https://api.lyft.com/v1/cost?" +
                    "start_lat=" + sLat +
                    "&start_lng=" + sLng +
                    "&end_lat=" + eLat +
                    "&end_lng=" + eLng);
            httpGet.addHeader("Authorization", "Bearer gAAAAABXAIYZCGKphx6dYLcCxqRRyUVkeHJGxFJ4Mhx1LTcFuKLleUE4hFhmkrAQgvHgjctIjLLLZMYMbsFf66wSUW7eeneqBPvZXwK_bIAX-31ZvYtAeDE7lSYUSxnc-iD2ieGlJ92GfgLiiAFUaiBFY8p0gsYzUkJ4OLWO0tgSBmp79c88IfZijmg5JyirKpPYNy6sNBUHJ0smZFA2EBl2eQrPEV2q0w==");
            HttpResponse httpresponse = httpClient.execute(httpGet);
            status = httpresponse.getStatusLine().toString();
            parseLyftJSONResponse(httpresponse);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }

    private String getLyftETA() {
        String status2 = "-1";

        try {
            HttpClient httpClient2 = new DefaultHttpClient();
            HttpGet httpGet2 = new HttpGet("https://api.lyft.com/v1/eta?" +
                    "lat=" + sLat +
                    "&lng=" + sLng);
            httpGet2.addHeader("Authorization", "Bearer gAAAAABXAIYZCGKphx6dYLcCxqRRyUVkeHJGxFJ4Mhx1LTcFuKLleUE4hFhmkrAQgvHgjctIjLLLZMYMbsFf66wSUW7eeneqBPvZXwK_bIAX-31ZvYtAeDE7lSYUSxnc-iD2ieGlJ92GfgLiiAFUaiBFY8p0gsYzUkJ4OLWO0tgSBmp79c88IfZijmg5JyirKpPYNy6sNBUHJ0smZFA2EBl2eQrPEV2q0w==");
            HttpResponse httpresponse2 = httpClient2.execute(httpGet2);
            status2 = httpresponse2.getStatusLine().toString();
            parseLyftJSONResponse2(httpresponse2);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return status2;
    }


    private String parseLyftJSONResponse2(HttpResponse hresponse) {
        try {
            String result = EntityUtils.toString(hresponse.getEntity());
            JSONObject estimateDetails = new JSONObject(result);

            JSONArray data = estimateDetails.getJSONArray("eta_estimates");
            JSONObject lyftSpecific = data.getJSONObject(0);
            String temp = lyftSpecific.getString("eta_seconds");
            etaL = Double.parseDouble(temp) / 60 + "min";

            System.out.println("test");
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private String parseLyftJSONResponse(HttpResponse hresponse) {
        try {
            String result = EntityUtils.toString(hresponse.getEntity());
            JSONObject estimateDetails = new JSONObject(result);

            JSONArray data = estimateDetails.getJSONArray("cost_estimates");
            JSONObject lyftSpecific = data.getJSONObject(1);
            typeL = lyftSpecific.getString("display_name");

            String tempMinPrice = lyftSpecific.getString("estimated_cost_cents_min");
            String tempMaxPrice = lyftSpecific.getString("estimated_cost_cents_max");
            priceL = "$" + ((( Double.parseDouble(tempMinPrice) + Double.parseDouble(tempMaxPrice) ) / 2 ) / 100) + "";
            String durSeconds = lyftSpecific.getString("estimated_duration_seconds");
            durationL = (Double.parseDouble(durSeconds) / 60 + "min").substring(0,5);
            System.out.println("test");
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private class MyOtherThirdTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            //String lyftCode = getLyftPrice();
            String otherLyftCode = getLyftETA();
            return otherLyftCode;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            etaLView.setText("ETA: "  + etaL);
        }
    }

    private class MyOtherUberTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            //String lyftCode = getLyftPrice();
            String otherUberCode = getUberEta();
            return otherUberCode;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            etaUView.setText("ETA: "  + etaU);
        }
    }


    private String getUberEta() {
        String status = "-1";

        try {
            HttpClient httpClient3 = new DefaultHttpClient();
            HttpGet httpGet3 = new HttpGet();

            URI uri3 = new URI("https://api.uber.com/v1/estimates/time?" +
                    "&server_token=xHire5cK16V9eQgU_MhA-y6vs2OZqHKLTtk0YiBF" +
                    "&start_latitude=" + sLat +
                    "&start_longitude=" + sLng);
            httpGet3.setURI(uri3);

            HttpResponse httpResponse = httpClient3.execute(httpGet3);
            status = httpResponse.getStatusLine().toString();
            String almostParsed = parseOtherUberJSON(httpResponse);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }

    private String parseOtherUberJSON(HttpResponse hResposne) {
        try {
            String result2 = EntityUtils.toString(hResposne.getEntity());
            JSONObject estimateDetails2 = new JSONObject(result2);
            JSONArray data = estimateDetails2.getJSONArray("times");
            JSONObject uberEst2 = data.getJSONObject(0);
            String temp = uberEst2.getString("estimate");
            etaU = Double.parseDouble(temp) / 60 + " min";
            System.out.println("test UBER EST");
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}


