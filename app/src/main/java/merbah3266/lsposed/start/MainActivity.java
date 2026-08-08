package merbah3266.lsposed.start;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.OutputStream;

public class MainActivity extends Activity {

    private static final String TAG = "RootBroadcast";

    private static final String VECTOR_MODULE =
            "/data/adb/modules/zygisk_vector";

    private static final String VECTOR_ALIAS =
            "merbah3266.lsposed.start.VectorAlias";

    private static final String DEFAULT_ALIAS =
            "merbah3266.lsposed.start.DefaultAlias";

    private static final String VECTOR_SECRET_CODE = "832867";
    private static final String LSPOSED_SECRET_CODE = "5776733";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isRooted()) {
            finish();
            return;
        }

        boolean vectorActive = isVectorActive();

        updateLauncherIdentity(vectorActive);
        executeBroadcast(vectorActive);

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

    private boolean isVectorActive() {
        File module = new File(VECTOR_MODULE);

        if (!module.isDirectory()) {
            return false;
        }

        return !new File(module, "disable").exists();
    }

    private void updateLauncherIdentity(boolean vectorActive) {
        PackageManager pm = getPackageManager();

        ComponentName vectorAlias =
                new ComponentName(this, VECTOR_ALIAS);

        ComponentName defaultAlias =
                new ComponentName(this, DEFAULT_ALIAS);

        if (vectorActive) {
            pm.setComponentEnabledSetting(
                    vectorAlias,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
            );

            pm.setComponentEnabledSetting(
                    defaultAlias,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
            );
        } else {
            pm.setComponentEnabledSetting(
                    vectorAlias,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
            );

            pm.setComponentEnabledSetting(
                    defaultAlias,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
            );
        }
    }

    private void executeBroadcast(boolean vectorActive) {
        String secretCode = vectorActive
                ? VECTOR_SECRET_CODE
                : LSPOSED_SECRET_CODE;

        String action;

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            action = "android.telephony.action.SECRET_CODE";
        } else {
            action = "android.provider.Telephony.SECRET_CODE";
        }

        String command =
                "am broadcast -a " +
                action +
                " -d android_secret_code://" +
                secretCode;

        Process suProcess = null;

        try {
            suProcess = Runtime.getRuntime().exec("su");

            OutputStream outputStream =
                    suProcess.getOutputStream();

            outputStream.write(
                    (command + "\n").getBytes()
            );

            outputStream.flush();
            outputStream.close();

            suProcess.waitFor();

        } catch (Exception e) {
            Log.e(TAG, "Broadcast error", e);
        } finally {
            if (suProcess != null) {
                suProcess.destroy();
            }
        }
    }
}