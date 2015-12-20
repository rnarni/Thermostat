package com.espthermostat.util;

import java.util.Locale;

/**
 * Util class for mesh
 * 
 * @author afunx
 * 
 */

public class MeshUtil
{
    public static String BLANK_ROUTER = "FFFFFFFF";
    /**
     * Get the port for mesh usage(For mesh require port hex uppercase).
     * 
     * @param port the port
     * @return the port for mesh usage
     */
    public static String getPortForMesh(int port)
    {
        String portHexUppercase = Integer.toHexString(port).toUpperCase(Locale.US);
        int numberOfZero = 4 - portHexUppercase.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfZero; i++)
        {
            sb.append("0");
        }
        sb.append(portHexUppercase);
        return sb.toString();
    }
    
    /**
     * Get the mac address for mesh usage(For mesh require the BSSID uppercase and without colon). It is an inverse
     * method for getRawMacAddress
     * 
     * @param bssid the bssid get from wifi scan
     * @return the mac address for mesh usage
     */
    public static String getMacAddressForMesh(String bssid)
    {
        StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < bssid.length(); i++)
        {
            c = bssid.charAt(i);
            if (c != ':')
            {
                sb.append(c);
            }
        }
        return sb.toString().toUpperCase(Locale.US);
    }
    
    /**
     * Get the raw mac address from macAddress for Mesh. It is an inverse method for getMacAddressForMesh.
     * 
     * @param macAddressForMesh the macAddress for Mesh
     * @return the raw mac address
     */
    public static String getRawMacAddress(String macAddressForMesh)
    {
        StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < macAddressForMesh.length(); i++)
        {
            c = macAddressForMesh.charAt(i);
            sb.append(c);
            if (i % 2 != 0 && i != macAddressForMesh.length() - 1)
            {
                sb.append(':');
            }
        }
        return sb.toString().toLowerCase(Locale.US);
    }
    
    /**
     * Get the host name by mesh ip
     * @param meshIp the mesh ip, e.g. C0A80102
     * @return the ip address, e.g. 192.168.1.2
     */
    public static String getHostNameByMeshIp(String meshIp)
    {
        StringBuilder sb = new StringBuilder();
        int value;
        for (int i = 0; i < meshIp.length(); i += 2)
        {
            value = Integer.parseInt(meshIp.substring(i, i+2), 16);
            sb.append(value);
            sb.append(".");
        }
        return sb.substring(0, sb.length()-1);
    }
    
    /**
     * Get the ip address for mesh usage(For mesh require the ip address hex uppercase without ".".
     * 
     * @param hostname the ip address, e.g. 192.168.1.2
     * @return ip address by hex without ".", e.g. C0A80102
     */
    public static String getIpAddressForMesh(String hostname)
    {
        StringBuilder sb = new StringBuilder();
        String[] segments = hostname.split("\\.");
        int segment;
        String segmentHexStr;
        for (int i = 0; i < segments.length; i++)
        {
            // get the integer
            segment = Integer.parseInt(segments[i]);
            // transform the integer to hex
            segmentHexStr = Integer.toHexString(segment);
            // transform the hex string to uppercase
            segmentHexStr = segmentHexStr.toUpperCase(Locale.US);
            // append segmentHexStr to the sb
            if (segmentHexStr.length() == 1)
            {
                sb.append("0");
                sb.append(segmentHexStr);
            }
            else if (segmentHexStr.length() == 2)
            {
                sb.append(segmentHexStr);
            }
            else
            {
                throw new RuntimeException();
            }
        }
        return sb.toString();
    }
    
    private static char __ascii2Char(int ASCII)
    {
        return (char)ASCII;
    }
    
    /**
     * Transform the raw ASCIIs to String by its value. "," is the separator.
     * 
     * e.g. if ASCIIs = "97", the result is "a". if ASCIIs = "97,98", the result is "ab".
     * 
     * @param ASCIIs the raw ASCIIs require transforming
     * @return the String by its value
     */
    public static String asciis2String(String ASCIIs)
    {
        String[] ASCIIss = ASCIIs.split(",");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ASCIIss.length; i++)
        {
            sb.append((char)__ascii2Char(Integer.parseInt(ASCIIss[i])));
        }
        return sb.toString();
    }
    
    /**
     * Transform the raw ASCIIs to String by its value.
     * 
     * e.g. if ASCII = "97", the result is "a".
     * 
     * @param ASCII the raw ASCII require transforming
     * @return the String by its value
     */
    public static String ascii2String(String ASCII)
    {
        return Character.toString(__ascii2Char(Integer.parseInt(ASCII)));
    }
    
    /**
     * Check whether the phone is connected to a mesh device
     * 
     * @param gateway the phone's gateway
     * @return whether the phone is connected to a mesh device
     */
    public static boolean isConnectedToMeshDevice(String gateway)
    {
        String[] gateways = gateway.split(".");
        if (gateways[0].equals("192") && gateways[1].equals("4") && gateways[2].equals("1"))
            return false;
        else
            return true;
    }
    
    private static void test_getPortForMesh()
    {
        int port1 = 7777;
        String result1 = getPortForMesh(port1);
        
        int port2 = 10;
        String result2 = getPortForMesh(port2);
        
        if (result1.equals("1E61") && result2.equals("000A"))
        {
            System.out.println("test_getPortForMesh() pass");
        }
        else
        {
            System.out.println("test_getPortForMesh() fail");
        }
    }
    
    private static void test_getMacAddressForMesh()
    {
        String bssid = "1a:34:56:38:90:34";
        String result = getMacAddressForMesh(bssid);
        if (result.equals("1A3456389034"))
        {
            System.out.println("test_getMacAddressForMesh() pass");
        }
        else
        {
            System.out.println("test_getMacAddressForMesh() fail");
        }
    }
    
    private static void test_getIpAddressForMesh()
    {
        String ip = "192.168.1.2";
        String result = getIpAddressForMesh(ip);
        if (result.equals("C0A80102"))
        {
            System.out.println("test_getIpAddressForMesh() pass");
        }
        else
        {
            System.out.println("test_getIpAddressForMesh() fail");
        }
    }
    
    private static void test_ascii2String()
    {
        String ascii = "97";
        char resultChar = ascii2String(ascii).charAt(0);
        if (resultChar == 'a')
        {
            System.out.println("test_ascii2String() pass");
        }
        else
        {
            System.out.println("test_ascii2String() fail");
        }
    }
    
    private static void test_getRawMacAddress()
    {
        String macAddressForMesh = "1A3456389034";
        String result = getRawMacAddress(macAddressForMesh);
        if (result.equals("1a:34:56:38:90:34"))
        {
            System.out.println("test_getRawMacAddress() pass");
        }
        else
        {
            System.out.println("test_getRawMacAddress() fail");
        }
    }
    
    private static void test_getHostNameByMeshIp()
    {
        String meshIp = "C0A80102";
        String result = getHostNameByMeshIp(meshIp);
        if(result.equals("192.168.1.2"))
        {
            System.out.println("test_getHostNameByMeshIp() pass");
        }
        else
        {
            System.out.println("test_getHostNameByMeshIp() fail");
        }
    }
    
    public static void main(String args[])
    {
        test_getPortForMesh();
        test_getMacAddressForMesh();
        test_getIpAddressForMesh();
        test_ascii2String();
        test_getRawMacAddress();
        test_getHostNameByMeshIp();
    }
}
