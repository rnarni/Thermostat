package com.espthermostat.util;

public class EspStrings
{
    public static final class DB
    {
        /**
         * the local database's db name
         */
        public static final String DB_NAME = "device-db";
    }
    
    public static final class Action
    {
        /**
         * when some devices are added into @see IEspDeviceCache. after receiving the broadcast, the IUser should do
         * something relying on @see IEspDeviceCacheHandler, and isStateMachine=true
         * 
         */
        public static final String DEVICES_ARRIVE_STATEMACHINE = "DEVICES_ARRIVE_STATEMACHINE";
        
        /**
         * when some devices are added into @see IEspDeviceCache. after receiving the broadcast, the IUser should do
         * something relying on @see IEspDeviceCacheHandler, and isStateMachine=false
         * 
         */
        public static final String DEVICES_ARRIVE_PULLREFRESH = "DEVICES_ARRIVE_PULLREFRESH";
    }
    
    public static final class Key
    {
        /**
         * The Intent value name of extra account email that RegisterActivity return to LoginActivity when register
         * success
         */
        public static final String REGISTER_NAME_EMAIL = "register_name_email";
        
        /**
         * The Intent value name of extra account password that RegisterActivity return to LoginActivity when register
         * success
         */
        public static final String REGISTER_NAME_PASSWORD = "register_name_password";
        
        /**
         * The settings SharedPreferences xml name
         */
        public static final String SETTINGS_NAME = "settings";
        
        /**
         * The system config SharedPreferences xml name
         */
        public static final String SYSTEM_CONFIG = "system_config";
        
        /**
         * The key of shared value whether the device support https, the default value is true
         */
        public static final String HTTPS_SUPPORT = "is_https_support";
        
        /**
         * The key of shared value auto refresh device
         */
        public static final String SETTINGS_KEY_DEVICE_AUTO_REFRESH = "settings_device_auto_refresh";
        
        /**
         * The key of shared value auto configure device
         */
        public static final String SETTINGS_KEY_DEVICE_AUTO_CONFIGURE = "settings_device_auto_configure";
        
        /**
         * The Intent value name of extra Device Key
         */
        public static final String DEVICE_KEY_KEY = "device_key";
        
        /**
         * The Intent value name of extra Device Timer ID
         */
        public static final String DEVICE_TIMER_ID_KEY = "device_timer_id";
        
        public static final String DEVICE_TIMER_BUNDLE_KEY = "device_timer_bundle";
        
        public static final String DEVICE_TIMER_PLUGS_VALUE_KEY = "plugs_value";
        
        /**
         * The XML for save skip mac user name
         */
        public static final String MAC_USER = "mac_user";
        
        /**
         * The key of shared value skip mac user name
         */
        public static final String MAC_USER_NAME_KEY = "mac_user_name";
    }
}
