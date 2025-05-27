package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    public static String mHost;
    public static int mPort;
    public static String sHost;
    public static int sPort;
    

    static {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("config.properties"));
            mHost = props.getProperty("monitor.host");
            mPort = Integer.parseInt(props.getProperty("monitor.port"));
            sHost = props.getProperty("servidor.host");
            sPort = Integer.parseInt(props.getProperty("servidor.port"));
            
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

}