package com.espthermostat.login;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.espthermostat.R;
import com.espthermostat.ui.DeviceListActivity;
import com.espthermostat.ui.mqttclient.MqttClient;
import com.espthermostat.util.Device;
import com.espthermostat.util.DeviceList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoginActivity extends Activity {

    private int a;
    private EditText username;
    private EditText password;
    private Button login;
    private DeviceList deviceList = new DeviceList();
    String status = "";
    String userId = "";
    String access_token = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupVariables();
    }

    public void authenticateLogin(View view) {
        // TODO - pass UID and PWD

        new MyAsyncTask().execute(username.getText() + "$" + password.getText());
        Toast.makeText(getApplicationContext(), "verifying login details ..",
                Toast.LENGTH_SHORT).show();

    }

    private void setupVariables() {
        username = (EditText) findViewById(R.id.usernameET);
        password = (EditText) findViewById(R.id.passwordET);
        login = (Button) findViewById(R.id.loginBtn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }


    // Async task to make http post request for authentication

    private class MyAsyncTask extends AsyncTask<String, Integer, Double> {

        @Override
        protected Double doInBackground(String... params) {
            // TODO Auto-generated method stub

            String paramVal = params[0];
            int delemiterIndex = paramVal.indexOf("$");
            postData(paramVal.substring(0, delemiterIndex), paramVal.substring(delemiterIndex + 1, paramVal.length()));
            return null;
        }

        protected void onPostExecute(Double result) {

            if (status.toString().equals("SUCCESS")) {
                Intent intent = new Intent(LoginActivity.this, DeviceListActivity.class);
                //Create the bundle
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                bundle.putString("access_token", access_token);
                bundle.putParcelable("deviceList", deviceList);
                intent.putExtras(bundle);
                LoginActivity.this.startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Invalid credientials.", Toast.LENGTH_LONG).show();
            }

        }

        protected void onProgressUpdate(Integer... progress) {
            //pb.setProgress(progress[0]);
        }

        public void postData(String email, String pwd) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://api.codinglabs.in/talent-wagon/api/v0/auth/login");
            httppost.setHeader("Content-Type", "application/json");
            try {

                JSONObject holder = new JSONObject();
                holder.put("email", "vijaykumars@nbostech.com");
                holder.put("password", "nbostech");
                holder.put("clientId", "my-client");

                StringEntity se = new StringEntity(holder.toString());
                httppost.setEntity(se);
                httppost.setHeader("Accept", "application/json");
                httppost.setHeader("Content-type", "application/json");


                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity rp = response.getEntity();
                String result = readContent(response);
                System.out.println("done...");
                System.out.println("result..."+result);

                if (result != "" || result != null) {
                    JSONObject json = new JSONObject(result);
                    status = (String) json.get("status");
                    if (status.toString().equals("SUCCESS")) {
                        JSONArray results = json.getJSONArray("results");
                        JSONObject resultObj = results.getJSONObject(0);
                        userId = "" + (Integer) resultObj.get("userId");
                        access_token = (String) resultObj.get("access_token");
                        HttpGet httpGet = new HttpGet("http://api.codinglabs.in/talent-wagon/api/v0/tenants");
                        httpGet.setHeader("Content-Type", "application/json");

                        try {

                            httpGet.setHeader("Accept", "application/json");
                            httpGet.setHeader("Content-type", "application/json");
                            httpGet.setHeader("Authorization", "Bearer " + access_token);


                            // Execute HTTP Post Request
                            response = httpclient.execute(httpGet);
                            result = readContent(response);
                            System.out.println("done...");
                            System.out.println("result..."+result);

                            if (result != "" || result != null) {
                                json = new JSONObject(result);
                                status = (String) json.get("status");
                                if (status.toString().equals("SUCCESS")) {
                                    results = json.getJSONArray("results");
                                    JSONObject resultObj1 = null;
                                    for (int i = 0; i < results.length(); i++) {
                                        resultObj1 = results.getJSONObject(i);
                                        Device device = new Device();
                                        device.setName((String) resultObj1.get("name"));
                                        device.setId((Integer)resultObj1.get("id"));
                                        deviceList.add(device);

                                    }
                                }
                            }

                            // call second activity

                        } catch (ClientProtocolException e) {
                            // TODO Auto-generated catch block
                        } catch (IOException e) {
                            System.out.println("IOException ..." + e.getMessage());

                            // TODO Auto-generated catch block
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                }

                // call second activity

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                System.out.println("IOException ..." + e.getMessage());

                // TODO Auto-generated catch block
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    String readContent(HttpResponse response) {
        String text = "";
        InputStream in = null;

        try {
            in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            text = sb.toString();
        } catch (IllegalStateException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {

                in.close();
            } catch (Exception ex) {
            }
        }

        return text;
    }
}
