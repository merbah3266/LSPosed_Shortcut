package merbah3266.lsposed.start;

import android.os.Bundle;
import android.widget.Toast;
import java.io.File;
import java.io.OutputStream;
import android.app.Activity;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isRooted()) {
            executeBroadcast();
        } else {
            Toast.makeText(this, "root is required", Toast.LENGTH_LONG).show();
        }
        finish();
    }
    private boolean isRooted() {
        String[] paths = {
            "/system/xbin/su",
            "/system/bin/su",
            "/debug_ramdisk/su"
        };

        for (String path : paths) {
            File file = new File(path);
            if (file.exists() && file.canExecute()) {
                return true;
            }
        }
        return false;
    }
    private void executeBroadcast() {
        int sdkVersion = android.os.Build.VERSION.SDK_INT;
        String action = "android.telephony.action.SECRET_CODE";
        String data = "android_secret_code://5776733";
        try {
            Process suProcess = Runtime.getRuntime().exec("su");
            OutputStream outputStream = suProcess.getOutputStream();

            String command = sdkVersion >= 29 ?
                    "am broadcast -a android.telephony.action.SECRET_CODE -d android_secret_code://5776733" :
                    "am broadcast -a android.provider.Telephony.SECRET_CODE -d android_secret_code://5776733";

  
            outputStream.write((command + "\n").getBytes());
            outputStream.flush();
            outputStream.close();
            suProcess.waitFor();
            suProcess.destroy();

        } catch (Exception e) {
            android.util.Log.e("RootBroadcast", "error: " + e.getMessage());
            Toast.makeText(this, "error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}