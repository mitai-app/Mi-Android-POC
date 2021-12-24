package nyc.vonley.mi.di.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.text.format.Formatter
import android.util.Log
import java.lang.ref.WeakReference
import java.net.InetAddress

class ClientSync constructor(context: Context)  {

    companion object {
        const val TAG = "client"
    }

    private val mContextRef: WeakReference<Context> = WeakReference<Context>(context);

    fun doInBg() {
        /*val asyncJob = async {
            //Some operation
        }
        //Pause here until the async block is finished.
        asyncJob.await()
*/
        Log.d(TAG, "Let's sniff the network");

        try {
            val context = mContextRef.get();

            if (context != null) {

                val cm =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
                val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

                val connectionInfo = wm.connectionInfo as WifiInfo
                val ipAddress = connectionInfo.ipAddress;
                val ipString = Formatter.formatIpAddress(ipAddress);


                Log.d(TAG, "activeNetwork: $activeNetwork");
                Log.d(TAG, "ipString: $ipString");

                val prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
                Log.d(TAG, "prefix: $prefix");

                for (i in 0 until 255) {
                    val testIp = "prefix$i";

                    val address = InetAddress.getByName(testIp);
                    val reachable = address.isReachable(1000);
                    val hostName = address.canonicalHostName;

                    if (reachable)
                        Log.i(
                            TAG,
                            "Host: ($hostName : $testIp) is reachable!"
                        );
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Well that's not good.", t);
        }

    }


}


