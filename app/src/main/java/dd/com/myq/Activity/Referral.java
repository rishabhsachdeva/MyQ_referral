package dd.com.myq.Activity;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bitly.Bitly;
import com.bitly.Error;
import com.bitly.Response;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;
import com.rosaloves.bitlyj.Url;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.rosaloves.bitlyj.Bitly.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import dd.com.myq.App.Config;
import dd.com.myq.R;
import dd.com.myq.Util.SessionManager;

public class Referral extends AppCompatActivity {
    public static int Total_points ;

    SessionManager currentSession;
    String user_id,link,temp_link;
    String POINTS_REQUEST_URL="http://myish.com:10011/api/users/";

    private TextView points;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referral);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());

        TextView date=(TextView)findViewById(R.id.point_date);
        date.setText("Referral points acquired till "+formattedDate+":");


        currentSession = new SessionManager(this);

        HashMap<String, String> user_details = currentSession.getUserDetails();
        user_id = user_details.get(SessionManager.KEY_UID);
        temp_link = "http://myish.com:10011/referral?userid=" + user_id;


        POINTS_REQUEST_URL = POINTS_REQUEST_URL + user_id;

        link = "http%3A%2F%2Fmyish.com%3A10011%2Freferral%3Fuserid%3D" + user_id;

        Log.d("Bitly_userid=", user_id);

        AsyncHttpClient client = new AsyncHttpClient();
        final String getbitlyurl = "https://api-ssl.bitly.com/v3/shorten?access_token=c995ca3af188cbb77639cb2330f9ed50b92cfc0e&longUrl=http%3A%2F%2Fmyish.com%3A10011%2Freferral%3Fuserid%3D" + user_id;

        client.get(this, getbitlyurl, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject object) {
                try {
                    JSONObject objdata = object.getJSONObject("data");
                    String shorturl = objdata.getString("url");
                    TextView seturl = (TextView) findViewById(R.id.link);
                    seturl.setText(shorturl);
                    //    Toast.makeText(Referral.this, "Link: " + shorturl, Toast.LENGTH_SHORT).show();
                    getlongurl(shorturl);
                    getPoints();
                    Log.d("shorturl=", shorturl);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("exception_get=", String.valueOf(e));

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("erroringet=", String.valueOf(errorResponse));
            }
        });
    }

        public void getlongurl(String shorturl)
    {
        String[] parts = shorturl.split("http://bit.ly/"); // escape .
        String part1 = parts[1];
        Log.d("part1=",part1);

        AsyncHttpClient client = new AsyncHttpClient();
        String getbitlyurl="https://api-ssl.bitly.com/v3/expand?access_token=c995ca3af188cbb77639cb2330f9ed50b92cfc0e&shortUrl=http%3A%2F%2Fbit.ly%2F"+part1;

        client.get(this, getbitlyurl, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject object) {
                try {
                    JSONObject objdata = object.getJSONObject("data");

                    JSONArray infoarray = objdata.getJSONArray("expand");

                    for (int i = 0; i < infoarray.length(); i++) {
                        JSONObject obj;
                        obj = infoarray.getJSONObject(i);
                        String longurl = obj.getString("long_url");

                        Log.d("long url", longurl);
                        Toast.makeText(Referral.this, "Link: " + longurl, Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            });
    }


    public void getPoints(){

        points=(TextView)findViewById(R.id.points);
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(this, POINTS_REQUEST_URL , new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.e("Response Shushant GET: ", response.toString());

                try {
                    Total_points = response.getInt("rewardpoints");

                    Log.e("Points stored before: ", String.valueOf(Total_points));

                    points.setText(String.valueOf(Total_points)+" pts");

            } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}