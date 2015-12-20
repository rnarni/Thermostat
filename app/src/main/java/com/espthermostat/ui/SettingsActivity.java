package com.espthermostat.ui;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.espthermostat.R;
import com.espthermostat.base.api.EspBaseApiUtil;
import com.espthermostat.esptouch.EsptouchTask;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity implements OnClickListener, OnCheckedChangeListener,
    OnItemSelectedListener
{
    private static final String SSID_PASSWORD = "ssid_password";
    
    private SharedPreferences mShared;
    private TextView mCurrentSsidTV;
    private Spinner mConfigureSP;
    private EditText mPasswordET;
    private CheckBox mShowPasswordCB;
    private Button mDeletePasswordBtn;
    private Button mConfirmBtn;
    private Button mRefreshBtn;
    
    private List<String> mWifiList;
    private List<String> mThermostatList;
    
    private String mCurrentSSID;
    private ListView listView ;
    
    String wifis[];

    WifiScanReceiver wifiReciever;
    WifiManager wifi;
    
    private ProgressDialog mDialog;
    
   
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.settings_activity);

        
        
        
        
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.ssid_list);
        mThermostatList = new ArrayList<String>();
        mThermostatList.add("No Devices Found");

        // Assign adapter to ListView
        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, mThermostatList)); 
        
        // ListView Item Click Listener
        listView.setOnItemClickListener(new OnItemClickListener() {

              @Override
              public void onItemClick(AdapterView<?> parent, View view,
                 int position, long id) {
                
               // ListView Clicked item index
               int itemPosition     = position;
               
               // ListView Clicked item value
               String  itemValue    = (String) listView.getItemAtPosition(position);
                  Log.d("GVK",itemValue);
                  (new ConnectThermostatWifi(SettingsActivity.this, itemValue, "")).execute();
             
              }

         }); 
            
            
        wifi=(WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Scanning Thermostats...");
        mDialog.setCanceledOnTouchOutside(false);
        //mDialog.setOnCancelListener(this);
        mDialog.show();        
        wifi.startScan();
        
        mShared = getSharedPreferences(SSID_PASSWORD, Context.MODE_PRIVATE);
        
        mCurrentSsidTV = (TextView)findViewById(R.id.esptouch_current_ssid);
        mConfigureSP = (Spinner)findViewById(R.id.esptouch_configure_wifi);
        mPasswordET = (EditText)findViewById(R.id.esptouch_pwd);
        mShowPasswordCB = (CheckBox)findViewById(R.id.esptouch_show_pwd);
        mDeletePasswordBtn = (Button)findViewById(R.id.esptouch_delete_pwd);
        mConfirmBtn = (Button)findViewById(R.id.esptouch_confirm);
        mRefreshBtn = (Button)findViewById(R.id.refresh);
        mShowPasswordCB.setOnCheckedChangeListener(this);
        mDeletePasswordBtn.setOnClickListener(this);
        mConfirmBtn.setOnClickListener(this);
        mRefreshBtn.setOnClickListener(this);
        
        mWifiList = new ArrayList<String>();
        //mWifiAdapter =
          //  new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mWifiList);
        //mConfigureSP.setAdapter(mWifiAdapter);
        mConfigureSP.setOnItemSelectedListener(this);
        
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //unregisterReceiver(wifiReciever);
    }
    
    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
     }
    protected void onPause() {
        unregisterReceiver(wifiReciever);
        super.onPause();
     }
    
   
    private void updateCurrentConnectionInfo()
    {
        mCurrentSSID = EspBaseApiUtil.getWifiConnectedSsid();
        if (mCurrentSSID == null)
        {
            mCurrentSSID = "";
        }
        mCurrentSsidTV.setText(getString(R.string.esp_esptouch_current_ssid, mCurrentSSID));
        
        if (!TextUtils.isEmpty(mCurrentSSID))
        {
            for (int i = 0; i < mWifiList.size(); i++)
            {
                String ssid = mWifiList.get(i);
                if (ssid.equals(mCurrentSSID))
                {
                    mConfigureSP.setSelection(i);
                    break;
                }
            }
        }
        else
        {
            mPasswordET.setText("");
        }
    }
    
    private class ConfigureTask extends AsyncTask<String, Void, Boolean> implements OnCancelListener
    {
        private Activity mActivity;
        
        private ProgressDialog mDialog;
        
        private EsptouchTask mTask;
        
        private final String mSsid;
        
        private final String mPassword;
        
        private boolean mCancelled;
        
        public ConfigureTask(Activity activity, String apSsid, String password)
        {
            mActivity = activity;
            
            mCancelled = false;
            mSsid = apSsid;
            mPassword = password;
        }
        
        @Override
        protected void onPreExecute()
        {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage(getString(R.string.esp_esptouch_configure_message, mSsid));
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setOnCancelListener(this);
            mDialog.show();
        }
        
        @Override
        protected Boolean doInBackground(String... params)
        {
	    	 int server_port = 65506;
	    	 DatagramSocket s;
			try {
				s = new DatagramSocket();
				InetAddress local = InetAddress.getByName("192.168.4.1");
				String config = "SOLA#1#"+mSsid+"#"+mPassword;
				byte[] message = config.getBytes();
				DatagramPacket p = new DatagramPacket(message, message.length,local,server_port);
				s.send(p);
				s.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
        }
        
        @Override
        protected void onPostExecute(Boolean result)
        {
            mDialog.dismiss();
            
            int toastMsg;
            if (result)
            {
                toastMsg = R.string.esp_esptouch_result_suc;
            }
            else if (mCancelled)
            {
                toastMsg = R.string.esp_esptouch_result_cancel;
            }
            else if (mCurrentSSID.equals(mSsid))
            {
                toastMsg = R.string.esp_esptouch_result_failed;
            }
            else
            {
                toastMsg = R.string.esp_esptouch_result_over;
            }
            
            Toast.makeText(mActivity, toastMsg, Toast.LENGTH_LONG).show();
        }
        
        @Override
        public void onCancel(DialogInterface dialog)
        {
            if (mTask != null)
            {
                mCancelled = true;
                mTask.interrupt();
            }
        }
    }

 
    private class ConnectThermostatWifi extends AsyncTask<String, Void, Boolean>
    {
        private Activity mActivity;
        
        private ProgressDialog mDialog;
        
       private String mSsid;
        private boolean mCancelled;
        
        public ConnectThermostatWifi(Activity activity, String apSsid, String password)
        {
            mActivity = activity;
            mCancelled = false;
            mSsid = apSsid;
        }
        
        @Override
        protected void onPreExecute()
        {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage("Connecting to "+mSsid);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
        }
        
        @Override
        protected Boolean doInBackground(String... params)
        {
        	WifiConfiguration conf = new WifiConfiguration();
        	conf.SSID = "\"" + mSsid + "\"";
        	//For WEP
        	/*
        	conf.wepKeys[0] = "\"" + "12345678" + "\""; 
        	conf.wepTxKeyIndex = 0;
        	conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        	conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        	*/
        	//For WPA
        	conf.preSharedKey = "\""+ "12345678" +"\"";
        	WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE); 
        	int netId = wifiManager.addNetwork(conf);
        	wifiManager.disconnect();
        	wifiManager.enableNetwork(netId, true);
        	wifiManager.reconnect();
        	return true;
        }
        
        @Override
        protected void onPostExecute(Boolean result)
        {
            mDialog.dismiss();
            
            int toastMsg;
            if (result)
            {
                toastMsg = R.string.esp_esptouch_result_suc;
            }
            else if (mCancelled)
            {
                toastMsg = R.string.esp_esptouch_result_cancel;
            }
            else if (mCurrentSSID.equals(mSsid))
            {
                toastMsg = R.string.esp_esptouch_result_failed;
            }
            else
            {
                toastMsg = R.string.esp_esptouch_result_over;
            }
            
            Toast.makeText(mActivity, toastMsg, Toast.LENGTH_LONG).show();
        }
        
    }
    
    
    @Override
    public void onClick(View v)
    {
        updateCurrentConnectionInfo();
        if (v == mConfirmBtn)
        {
            if (!TextUtils.isEmpty(mCurrentSSID))
            {
                String ssid = mConfigureSP.getSelectedItem().toString();
                String password = mPasswordET.getText().toString();
                mShared.edit().putString(ssid, password).commit();
                
                new ConfigureTask(this, ssid, password).execute();
            }
            else
            {
                Toast.makeText(this, R.string.esp_esptouch_connection_hint, Toast.LENGTH_LONG).show();
            }
        }
        else if (v == mDeletePasswordBtn)
        {
            String selectionSSID = mConfigureSP.getSelectedItem().toString();
            if (!TextUtils.isEmpty(selectionSSID))
            {
                mShared.edit().remove(selectionSSID).commit();
                mPasswordET.setText("");
            }
        }
        else if (v == mRefreshBtn)
        {

            mDialog.show();        	
        	wifi.startScan();
        }
    }
    
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (buttonView == mShowPasswordCB)
        {
            if (isChecked)
            {
                mPasswordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else
            {
                mPasswordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }
    }
    
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        String ssid = mWifiList.get(position);
        String password = mShared.getString(ssid, "");
        mPasswordET.setText(password);
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }
    
    private class WifiScanReceiver extends BroadcastReceiver{
        public void onReceive(Context c, Intent intent) {
        	mDialog.dismiss(); 
           List<ScanResult> wifiScanList = wifi.getScanResults();
           wifis = new String[wifiScanList.size()];
           mWifiList.clear();
           mThermostatList.clear();
           for(int i = 0; i < wifiScanList.size(); i++){
              wifis[i] = wifiScanList.get(i).SSID;
              mWifiList.add(wifiScanList.get(i).SSID);
              if(wifiScanList.get(i).SSID.startsWith("THERMOSTAT_"))
            	  mThermostatList.add(wifiScanList.get(i).SSID);
           }
           if(mThermostatList.size()==0)
        	   mThermostatList.add("No Devices Found");
           listView.setAdapter(new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,mThermostatList));
           mConfigureSP.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, android.R.id.text1, mWifiList));
        }
     }

}





