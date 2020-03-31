package densoftinfotechio;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

public class InternetUtils {
	private static InternetUtils mInstance; 
	private static Context context;

	public InternetUtils(Context con){
		context = con;
	}
	
	public static synchronized InternetUtils getInstance(Context con){
		if(mInstance==null){
			mInstance = new InternetUtils(con);
		}
		return mInstance;
	}
	
	public boolean available(){
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		return (info!=null && info.isConnected()); 
	}

	public static NetworkInfo getNetworkInfo(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo();
	}

	/**
	 * Check if there is any connectivity
	 * @param context
	 * @return
	 */
	public static boolean isConnected(Context context){
		NetworkInfo info = InternetUtils.getNetworkInfo(context);
		return (info != null && info.isConnected());
	}

	/* Check if there is any connectivity to a Wifi network
	 */
	public static boolean isConnectedWifi(Context context){
		NetworkInfo info = InternetUtils.getNetworkInfo(context);
		return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
	}

	/*Check if there is any connectivity to a mobile network

	 */
	public static boolean isConnectedMobile(Context context){
		NetworkInfo info = InternetUtils.getNetworkInfo(context);
		return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
	}

	/* Check if there is fast connectivity
	 */
	public static boolean isConnectedSpeed(Context context){
		NetworkInfo info = InternetUtils.getNetworkInfo(context);
		return (info != null && info.isConnected() && InternetUtils.isConnectionWeak(info.getType(),info.getSubtype()));
	}

	/*Check if the connection is fast
	 */
	public static boolean isConnectionWeak(int type, int subType){
		if(type== ConnectivityManager.TYPE_WIFI){
			return true;
		}else if(type== ConnectivityManager.TYPE_MOBILE){
			Log.d("type ", type + " " + subType);

			switch(subType){
				case TelephonyManager.NETWORK_TYPE_1xRTT:
				case TelephonyManager.NETWORK_TYPE_EDGE:
					return false; // ~ 50-100 kbps
				case TelephonyManager.NETWORK_TYPE_GPRS:
					return false; // ~ 100 kbps
				case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
					return false; // ~ 1-2 Mbps
				case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
					return false; // ~ 5 Mbps
				case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
					return false; // ~ 10-20 Mbps
				case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
					return false; // ~ 10+ Mbps


				case TelephonyManager.NETWORK_TYPE_CDMA:
					return false; // ~ 14-64 kbps
				case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
					return false; // ~25 kbps
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					return true; // ~ 400-1000 kbps
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					return true; // ~ 600-1400 kbps
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					return true; // ~ 2-14 Mbps
				case TelephonyManager.NETWORK_TYPE_HSPA:
					return true; // ~ 700-1700 kbps
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					return true; // ~ 1-23 Mbps
				case TelephonyManager.NETWORK_TYPE_UMTS:
					return true; // ~ 400-7000 kbps
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				default:
					return false;
			}
		}else{
			return false;
		}
	}

	public void network(){
		NetworkInfo info = InternetUtils.getNetworkInfo(context);
		if(info.getType() == ConnectivityManager.TYPE_WIFI){
			// do something
		} else if(info.getType() == ConnectivityManager.TYPE_MOBILE){
			// check NetworkInfo subtype
			if(info.getSubtype() == TelephonyManager.NETWORK_TYPE_GPRS){
				// Bandwidth between 100 kbps and below
			} else if(info.getSubtype() == TelephonyManager.NETWORK_TYPE_EDGE){
				// Bandwidth between 50-100 kbps
			} else if(info.getSubtype() == TelephonyManager.NETWORK_TYPE_EVDO_0){
				// Bandwidth between 400-1000 kbps
			} else if(info.getSubtype() == TelephonyManager.NETWORK_TYPE_EVDO_A){
				// Bandwidth between 600-1400 kbps
			}

			// Other list of various subtypes you can check for and their bandwidth limits
			// TelephonyManager.NETWORK_TYPE_1xRTT       ~ 50-100 kbps
			// TelephonyManager.NETWORK_TYPE_CDMA        ~ 14-64 kbps
			// TelephonyManager.NETWORK_TYPE_HSDPA       ~ 2-14 Mbps
			// TelephonyManager.NETWORK_TYPE_HSPA        ~ 700-1700 kbps
			// TelephonyManager.NETWORK_TYPE_HSUPA       ~ 1-23 Mbps
			// TelephonyManager.NETWORK_TYPE_UMTS        ~ 400-7000 kbps
			// TelephonyManager.NETWORK_TYPE_UNKNOWN     ~ Unknown

		}

	}

}
