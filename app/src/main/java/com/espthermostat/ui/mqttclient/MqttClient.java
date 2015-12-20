package com.espthermostat.ui.mqttclient;

import java.util.Timer;
import java.util.TimerTask;

import com.espthermostat.R;
import com.espthermostat.login.LoginActivity;
import com.espthermostat.service.mqttclient.MQTTservice;
import com.espthermostat.ui.SettingsActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MqttClient extends Activity
{
	private Messenger service = null;
	private final Messenger serviceHandler = new Messenger(new ServiceHandler());
	private IntentFilter intentFilter = null;
	private PushReceiver pushReceiver;
	private String mqttTopic_sub = "/sola/espthermostat/app18fe34a486e1";
	private String mqttTopic_pub = "/sola/espthermostat/dev18fe34a486e1";
	CountDownTimer timer1;
	private int mode_val = 0, fan_mode_val = 0;
	int timer_counter = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thermostat_main);
    	intentFilter = new IntentFilter();
    	intentFilter.addAction("com.example.MQTT.PushReceived");
    	pushReceiver = new PushReceiver();
    	registerReceiver(pushReceiver, intentFilter, null, null);
        
        startService(new Intent(this, MQTTservice.class));	
		addCoolIncButtonListener();
		addCoolDecButtonListener();
		addHeatIncButtonListener();
		addHeatDecButtonListener();
		
		addAutoModeSelButtonListener();
		addCoolModeSelButtonListener();
		addHeatModeSelButtonListener();
		addOffModeSelButtonListener();
		
		addAutoFanModeSelButtonListener();
		addLoFanModeSelButtonListener();
		addHiFanModeSelButtonListener();
		
        timer1 = new CountDownTimer(100,100){

            @Override
            public void onTick(long millisUntilFinished) {
                Log.v("GVK", "Countdown Progress "+ timer_counter);
                timer_counter++;
                //mProgressBar.setProgress(i);

            }

            @Override
            public void onFinish() {
            //Do what you want 
            	Log.v("GVK", "Countdown finished");
	    		TextView result = (TextView) findViewById(R.id.textResultStatus);
	    		result.setText("Syncing...");
	    		TextView cool_temp_view = (TextView)findViewById(R.id.cool_temp);
	    		String cool_temp = cool_temp_view.getText().toString();
	    		TextView heat_temp_view = (TextView)findViewById(R.id.heat_temp);
	    		String heat_temp = heat_temp_view.getText().toString();	    		
	    		
	    		Bundle data = new Bundle();
	    		data.putCharSequence(MQTTservice.TOPIC, mqttTopic_pub);
	    		data.putCharSequence(MQTTservice.MESSAGE, "REQ#2#"+(char)Integer.parseInt(cool_temp)+"#"+(char)Integer.parseInt(heat_temp)+"#"+mode_val+"#"+fan_mode_val);
	    		Message msg = Message.obtain(null, MQTTservice.PUBLISH);
	    		msg.setData(data);
	    		msg.replyTo = serviceHandler;
	    		try
	    		{
	    			service.send(msg);
	    			result.setText("Sync Success.");
	    		}
	    		catch (RemoteException e)
	    		{
	    			result.setText("Sync failed: "+e.getMessage());
	    			e.printStackTrace();
	    		}
	    		
          	
            	timer_counter++;
                //mProgressBar.setProgress(i);
            }
        };
    }
    
    @Override
	protected void onStart()
    {
    	super.onStart();
    	bindService(new Intent(this, MQTTservice.class), serviceConnection, 0);
    }
    
    @Override
 	protected void onStop()
     {
    	super.onStop();
    	unbindService(serviceConnection);
     }
    
	 @Override
	protected void onResume()
	{
		 super.onResume();
		registerReceiver(pushReceiver, intentFilter);
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		unregisterReceiver(pushReceiver);
	}
		
	public class PushReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent i)
		{
			String topic = i.getStringExtra(MQTTservice.TOPIC);
			String message = i.getStringExtra(MQTTservice.MESSAGE);
			//Toast.makeText(context, "Push message received - " + topic + ":" + message, Toast.LENGTH_LONG).show();
			Log.v("GVK", "After toast:"+message);
			//Message parsing
			if(message.startsWith("RESP#"))
			{
				String msg_part[] = message.split("#");
				if(msg_part[1].equals("1") || msg_part[1].equals("2"))
				{
					((TextView)findViewById(R.id.cur_temp)).setText(msg_part[2]);
					((TextView)findViewById(R.id.out_temp)).setText("68");
					((TextView)findViewById(R.id.cur_pres)).setText(msg_part[4]);
					((TextView)findViewById(R.id.cur_hum)).setText(msg_part[5]);
					((TextView)findViewById(R.id.cool_temp)).setText(msg_part[6]);
					((TextView)findViewById(R.id.heat_temp)).setText(msg_part[7]);
		    		mode_val = Integer.parseInt(msg_part[8]);
		    		selectMode();
		    		fan_mode_val = Integer.parseInt(msg_part[9]);
		    		selectFanMode();		    		
				}
			}
			
		}
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
        	Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
		@Override
		public void onServiceConnected(ComponentName arg0, IBinder binder)
		{
			service = new Messenger(binder);
			Bundle data = new Bundle();

			//data.putSerializable(MQTTservice.CLASSNAME, MainActivity.class);
			data.putCharSequence(MQTTservice.INTENTNAME, "com.example.MQTT.PushReceived");
			Message msg = Message.obtain(null, MQTTservice.REGISTER);
			msg.setData(data);
			msg.replyTo = serviceHandler;
			try
			{
				service.send(msg);
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
			
    			Bundle data1 = new Bundle();
    			data1.putCharSequence(MQTTservice.TOPIC, mqttTopic_sub);
    			Message msg1 = Message.obtain(null, MQTTservice.SUBSCRIBE);
    			msg1.setData(data1);
    			msg1.replyTo = serviceHandler;
    			try
    			{
    				service.send(msg1);
    			}
    			catch (RemoteException e)
    			{
    				e.printStackTrace();
    			}

    			
	    		Bundle data2 = new Bundle();
	    		data2.putCharSequence(MQTTservice.TOPIC, mqttTopic_pub);
	    		data2.putCharSequence(MQTTservice.MESSAGE, "REQ#1#1#");
	    		Message msg2 = Message.obtain(null, MQTTservice.PUBLISH);
	    		msg2.setData(data2);
	    		msg2.replyTo = serviceHandler;
	    		try
	    		{
	    			service.send(msg2);
	    		}
	    		catch (RemoteException e)
	    		{
	    			e.printStackTrace();
	    		}    			

		}

		@Override
		public void onServiceDisconnected(ComponentName arg0)
		{
		}
    };

	private void addCoolIncButtonListener()
	{	
	    Button subscribeButton = (Button) findViewById(R.id.inc_cool_temp);
	    subscribeButton.setOnClickListener(new OnClickListener()
	    {
	    	@Override
			public void onClick(View arg0)
	    	{
	    		TextView cool_temp_view = (TextView)findViewById(R.id.cool_temp);
	    		String cool_temp = cool_temp_view.getText().toString();
	    		cool_temp_view.setText((Integer.parseInt(cool_temp)+1)+"");
	    		timer_counter = 0;
	    		timer1.cancel();
	    		timer1.start();
				
			}
		}); 
	}

	private void addCoolDecButtonListener()
	{	
	    Button subscribeButton = (Button) findViewById(R.id.dec_cool_temp);
	    subscribeButton.setOnClickListener(new OnClickListener()
	    {
	    	@Override
			public void onClick(View arg0)
	    	{
	    		TextView cool_temp_view = (TextView)findViewById(R.id.cool_temp);
	    		String cool_temp = cool_temp_view.getText().toString();
	    		cool_temp_view.setText((Integer.parseInt(cool_temp)-1)+"");
	    		timer_counter = 0;
	    		timer1.cancel();
	    		timer1.start();	    		
			}
		}); 
	}

	private void addHeatIncButtonListener()
	{	
	    Button subscribeButton = (Button) findViewById(R.id.inc_heat_temp);
	    subscribeButton.setOnClickListener(new OnClickListener()
	    {
	    	@Override
			public void onClick(View arg0)
	    	{
	    		TextView heat_temp_view = (TextView)findViewById(R.id.heat_temp);
	    		String heat_temp = heat_temp_view.getText().toString();
	    		heat_temp_view.setText((Integer.parseInt(heat_temp)+1)+"");
	    		timer_counter = 0;
	    		timer1.cancel();
	    		timer1.start();
				
			}
		}); 
	}

	private void addHeatDecButtonListener()
	{	
	    Button subscribeButton = (Button) findViewById(R.id.dec_heat_temp);
	    subscribeButton.setOnClickListener(new OnClickListener()
	    {
	    	@Override
			public void onClick(View arg0)
	    	{
	    		TextView heat_temp_view = (TextView)findViewById(R.id.heat_temp);
	    		String heat_temp = heat_temp_view.getText().toString();
	    		heat_temp_view.setText((Integer.parseInt(heat_temp)-1)+"");
	    		timer_counter = 0;
	    		timer1.cancel();
	    		timer1.start();	    		
			}
		}); 
	}
	
	private void addAutoModeSelButtonListener()
	{
	    Button subscribeButton = (Button) findViewById(R.id.auto_mode);
	    subscribeButton.setOnClickListener(new OnClickListener()
	    {
	    	@Override
			public void onClick(View arg0)
	    	{
	    		mode_val = 1;
	    		selectMode();
	    		timer_counter = 0;
	    		timer1.cancel();
	    		timer1.start();	    		
			}
		}); 		
	}

	private void addCoolModeSelButtonListener()
	{
	    Button subscribeButton = (Button) findViewById(R.id.cool_mode);
	    subscribeButton.setOnClickListener(new OnClickListener()
	    {
	    	@Override
			public void onClick(View arg0)
	    	{
	    		mode_val = 2;
	    		selectMode();
	    		timer_counter = 0;
	    		timer1.cancel();
	    		timer1.start();	    		
			}
		}); 		
	}

	private void addHeatModeSelButtonListener()
	{
	    Button subscribeButton = (Button) findViewById(R.id.heat_mode);
	    subscribeButton.setOnClickListener(new OnClickListener()
	    {
	    	@Override
			public void onClick(View arg0)
	    	{
	    		mode_val = 3;
	    		selectMode();
	    		timer_counter = 0;
	    		timer1.cancel();
	    		timer1.start();	    		
			}
		}); 		
	}
	private void addOffModeSelButtonListener()
	{
	    Button subscribeButton = (Button) findViewById(R.id.off_mode);
	    subscribeButton.setOnClickListener(new OnClickListener()
	    {
	    	@Override
			public void onClick(View arg0)
	    	{
	    		mode_val = 0;
	    		selectMode();
	    		timer_counter = 0;
	    		timer1.cancel();
	    		timer1.start();	    		
			}
		}); 		
	}	
	
	private void addAutoFanModeSelButtonListener()
	{
	    Button subscribeButton = (Button) findViewById(R.id.fan_auto);
	    subscribeButton.setOnClickListener(new OnClickListener()
	    {
	    	@Override
			public void onClick(View arg0)
	    	{
	    		fan_mode_val = 1;
	    		selectFanMode();
	    		timer_counter = 0;
	    		timer1.cancel();
	    		timer1.start();	    		
			}
		}); 		
	}
	private void addLoFanModeSelButtonListener()
	{
	    Button subscribeButton = (Button) findViewById(R.id.fan_lo);
	    subscribeButton.setOnClickListener(new OnClickListener()
	    {
	    	@Override
			public void onClick(View arg0)
	    	{
	    		fan_mode_val = 2;
	    		selectFanMode();
	    		timer_counter = 0;
	    		timer1.cancel();
	    		timer1.start();	    		
			}
		}); 		
	}
	private void addHiFanModeSelButtonListener()
	{
	    Button subscribeButton = (Button) findViewById(R.id.fan_hi);
	    subscribeButton.setOnClickListener(new OnClickListener()
	    {
	    	@Override
			public void onClick(View arg0)
	    	{
	    		fan_mode_val = 3;
	    		selectFanMode();
	    		timer_counter = 0;
	    		timer1.cancel();
	    		timer1.start();	    		
			}
		}); 		
	}	
	
	private void selectMode()
	{
		Button autoModeButton = (Button) findViewById(R.id.auto_mode);
		autoModeButton.setBackgroundResource(R.drawable.auto_mode);
		Button coolModeButton = (Button) findViewById(R.id.cool_mode);
		coolModeButton.setBackgroundResource(R.drawable.cool_mode);
		Button heatModeButton = (Button) findViewById(R.id.heat_mode);
		heatModeButton.setBackgroundResource(R.drawable.heat_mode);
		Button offModeButton = (Button) findViewById(R.id.off_mode);
		offModeButton.setBackgroundResource(R.drawable.off_mode);		
		if(mode_val==1)  //Auto Mode
		{
			autoModeButton.setBackgroundResource(R.drawable.auto_mode_selected);
		}
		if(mode_val==2)  //Cool Mode
		{
			coolModeButton.setBackgroundResource(R.drawable.cool_mode_selected);
		}
		if(mode_val==3)  //Heat Mode
		{
			heatModeButton.setBackgroundResource(R.drawable.heat_mode_selected);
		}
		if(mode_val==0)  //Off Mode
		{
			offModeButton.setBackgroundResource(R.drawable.off_mode_selected);
		}		
	}

	private void selectFanMode()
	{
		Button autoFanModeButton = (Button) findViewById(R.id.fan_auto);
		autoFanModeButton.setBackgroundResource(R.drawable.auto_fanmode);
		Button loFanModeButton = (Button) findViewById(R.id.fan_lo);
		loFanModeButton.setBackgroundResource(R.drawable.low_fanmode);
		Button hiFanModeButton = (Button) findViewById(R.id.fan_hi);
		hiFanModeButton.setBackgroundResource(R.drawable.hi_fanmode);
		if(fan_mode_val==1)  //Auto Mode
		{
			autoFanModeButton.setBackgroundResource(R.drawable.auto_fanmode_selected);
		}
		if(fan_mode_val==2)  //Low Mode
		{
			loFanModeButton.setBackgroundResource(R.drawable.low_fanmode_selected);
		}
		if(fan_mode_val==3)  //Hi Mode
		{
			hiFanModeButton.setBackgroundResource(R.drawable.hi_fanmode_selected);
		}
	}
	
	class ServiceHandler extends Handler
	{
	    @Override
	    public void handleMessage(Message msg)
	    {
	    	String status_type = "";
		   	 switch (msg.what)
		   	 {
		   	 case MQTTservice.SUBSCRIBE:
		   		status_type = "SUBSCRIBE";
		   		break;
		     case MQTTservice.PUBLISH:
		    	 status_type = "PUBLISH";
		    	 break;
		     case MQTTservice.REGISTER:
		    	 status_type = "REGISTER";
		    	 break;
	    	 default:
	    		 super.handleMessage(msg);
	    		 return;
		   	 }
	   	 
	  		 Bundle b = msg.getData();
	  		 if (b != null)
	  		 {
	  			 TextView result = (TextView) findViewById(R.id.textResultStatus);
	  			 Boolean status = b.getBoolean(MQTTservice.STATUS);
	  			 if (status == false)
	  			 {
	  				result.setText(status_type+":"+"Failed");
	  			 }
	  			 else
	  			 {
	  				result.setText(status_type+":"+"Success");
	  			 }
	  		 }
	    }
	}

    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true); // exist app

    }
}
