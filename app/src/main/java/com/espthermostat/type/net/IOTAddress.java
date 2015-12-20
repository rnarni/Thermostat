package com.espthermostat.type.net;

import java.net.InetAddress;

import com.espthermostat.object.IEspObject;

// it is used to process the message got from the IOT Device
// "I'm Plug.98:fe:34:77:ce:00 192.168.4.1"
public class IOTAddress implements IEspObject
{
    private String mSSID;
    
    /**
     * the device's bssid
     */
    private String mBSSID;
    
    /**
     * the device's InetAddress(IP Address)
     */
    private InetAddress mInetAddress;
    
    /**
     * the device type enum, @see EspDeviceType
     */
    
    /**
     * the router of the device(if the device is mesh device) null(if the device isn't mesh device)
     */
    private String mRouter;
    
    /**
     * whether the device is mesh device or not
     */
    private boolean mIsMeshDevice;
    
    public IOTAddress(String BSSID, InetAddress inetAddress)
    {
        this(BSSID, inetAddress, null, false);
    }
    
    public IOTAddress(String BSSID, InetAddress inetAddress, boolean isMeshDevice)
    {
        this(BSSID, inetAddress, null, isMeshDevice);
    }
    
    public IOTAddress(String BSSID, InetAddress inetAddress, String router, boolean isMeshDevice)
    {
        this.mBSSID = BSSID;
        this.mInetAddress = inetAddress;
        this.mRouter = router;
        this.mIsMeshDevice = isMeshDevice;
    }
    
    
    public void setIsMeshDevice(boolean isMeshDevice)
    {
        this.mIsMeshDevice = isMeshDevice;
    }
    
    public boolean isMeshDevice()
    {
        return mIsMeshDevice;
    }
    
    public void setRouter(String router)
    {
        this.mRouter = router;
    }
    
    public String getRouter()
    {
        return this.mRouter;
    }
    
    public void setBSSID(String BSSID)
    {
        this.mBSSID = BSSID;
    }
    
    public String getBSSID()
    {
        return mBSSID;
    }
    
    public void setInetAddr(InetAddress inetAddress)
    {
        this.mInetAddress = inetAddress;
    }
    
    public InetAddress getInetAddress()
    {
        return mInetAddress;
    }
    
    public void setSSID(String ssid)
    {
        mSSID = ssid;
    }
    
    public String getSSID()
    {
        return mSSID;
    }
    
   
  
    @Override
    public int hashCode()
    {
        return mBSSID.hashCode();
    }
}
