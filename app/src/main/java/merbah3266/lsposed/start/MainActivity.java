package merbah3266.lsposed.start;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

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

        // كل عمليات الكشف الحساسة تتم عبر root
        boolean vectorActive = isVectorActiveAsRoot();

        updateLauncherIdentity(vectorActive);
        executeBroadcast(vectorActive);

        finish();
    }

    /**
     * الكشف عن Vector بصلاحيات root.
     *
     * الشرطان:
     * 1. مجلد /data/adb/modules/zygisk_vector موجود.
     * 2. لا يوجد ملف disable بداخله.
     */
    private boolean isVectorActiveAsRoot() {
        Process process = null;

        try {
            process = Runtime.getRuntime().exec("su");

            OutputStream os = process.getOutputStream();

            String command =
                    "if [ -d '" + VECTOR_MODULE + "' ] && " +
                    "[ ! -e '" + VECTOR_MODULE + "/disable' ]; then " +
                    "exit 0; " +
                    "else " +
                    "exit 1; " +
                    "fi\n";

            os.write(command.getBytes("UTF-8"));
            os.flush();
            os.close();

            int exitCode = process.waitFor();

            Log.d(TAG,
                    "Vector root detection exit code: " + exitCode);

            return exitCode == 0;

        } catch (Exception e) {
            Log.e(TAG,
                    "Vector root detection failed", e);
            return false;

        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * تبديل أيقونة/Launcher Alias حسب حالة Vector.
     */
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

            Log.d(TAG, "Vector detected: using VectorAlias");

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

            Log.d(TAG, "Vector not detected: using DefaultAlias");
        }
    }

    /**
     * إرسال Secret Code المناسب عبر root.
     */
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
                Log.e(TAG,
                        "am broadcast exited with code: " +
                        exitCode);
            } else {
                Log.d(TAG,
                        "Secret code broadcast sent: " +
                        secretCode);
            }

        } catch (Exception e) {

            Log.e(TAG,
                    "Broadcast error", e);

        } finally {

            if (suProcess != null) {
                suProcess.destroy();
            }
        }
    }
}