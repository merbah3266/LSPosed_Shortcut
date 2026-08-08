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

    private static final String[] SU_PATHS = {
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/su/xbin/su",
            "/vendor/bin/su",
            "/vendor/xbin/su",
            "/product/bin/su",
            "/data/adb/ksu/bin/su",
            "/data/adb/magisk/su",
            "/debug_ramdisk/su"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isRootAvailable()) {
            finish();
            return;
        }

        boolean vectorActive = isVectorActive();

        updateLauncherIdentity(vectorActive);
        executeBroadcast(vectorActive);

        finish();
    }

    private boolean isRootAvailable() {
        for (String path : SU_PATHS) {
            File su = new File(path);

            if (su.isFile() && su.canExecute()) {
                return true;
            }
        }

        try {
            Process process = Runtime.getRuntime().exec(
                    new String[]{"su", "-c", "id"}
            );

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "su execution check failed", e);
        }

        return false;
    }

    private boolean isVectorActive() {
        File module = new File(VECTOR_MODULE);

        return module.isDirectory()
                && !new File(module, "disable").exists()
                && !new File(module, "remove").exists();
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
                    (command + "\n").getBytes("UTF-8")
            );

            outputStream.flush();
            outputStream.close();

            int exitCode = suProcess.waitFor();

            if (exitCode != 0) {
                Log.e(TAG, "su exited with code: " + exitCode);
            }

        } catch (Exception e) {
            Log.e(TAG, "Broadcast error", e);
        } finally {
            if (suProcess != null) {
                suProcess.destroy();
            }
        }
    }
}