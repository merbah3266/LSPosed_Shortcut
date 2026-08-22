package merbah3266.lsposed.start;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    private static final String VECTOR_MODULE =
            "/data/adb/modules/zygisk_vector";

    private static final String VECTOR_ALIAS =
            "merbah3266.lsposed.start.VectorAlias";

    private static final String DEFAULT_ALIAS =
            "merbah3266.lsposed.start.DefaultAlias";

    private static final String VECTOR_SECRET_CODE = "832867";
    private static final String LSPOSED_SECRET_CODE = "5776733";

    private static final String PREFS_NAME = "qstile";
    private static final String KEY_VECTOR_ACTIVE = "vector_active";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static boolean isVectorActive() {
        Process process = null;

        try {
            process = new ProcessBuilder(
                    "su",
                    "-c",
                    "[ -d '" + VECTOR_MODULE + "' ] && " +
                    "[ ! -e '" + VECTOR_MODULE + "/disable' ]"
            ).start();

            return process.waitFor(10, TimeUnit.SECONDS)
                    && process.exitValue() == 0;

        } catch (Exception e) {
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    public static void saveVectorState(Context context, boolean vectorActive) {
        context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        ).edit()
                .putBoolean(KEY_VECTOR_ACTIVE, vectorActive)
                .apply();
    }

    public static boolean getSavedVectorState(Context context) {
        return context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        ).getBoolean(KEY_VECTOR_ACTIVE, false);
    }

    public static void refreshQuickSettingsTile(Context context) {
        TileService.requestListeningState(
                context,
                new ComponentName(
                        context,
                        StartTileService.class
                )
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        executor.execute(() -> {
            boolean hasRoot = false;
            boolean vectorActive = false;

            Process process = null;

            try {
                ProcessBuilder pb = new ProcessBuilder("su");
                pb.redirectErrorStream(true);
                process = pb.start();

                OutputStream os = process.getOutputStream();

                String action = Build.VERSION.SDK_INT >= 29
                        ? "android.telephony.action.SECRET_CODE"
                        : "android.provider.Telephony.SECRET_CODE";

                vectorActive = isVectorActive();

                String secretCode = vectorActive
                        ? VECTOR_SECRET_CODE
                        : LSPOSED_SECRET_CODE;

                String command =
                        "am broadcast --user 0 -a " + action +
                        " -d android_secret_code://" + secretCode +
                        " > /dev/null 2>&1\n" +
                        "echo ROOT_OK\n" +
                        "exit\n";

                os.write(command.getBytes("UTF-8"));
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                );

                String line;

                while ((line = reader.readLine()) != null) {
                    if ("ROOT_OK".equals(line.trim())) {
                        hasRoot = true;
                    }
                }

                if (!process.waitFor(10, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    hasRoot = false;
                }

            } catch (Exception e) {
                hasRoot = false;
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }

            boolean finalHasRoot = hasRoot;
            boolean finalVectorActive = vectorActive;

            runOnUiThread(() -> {
                if (!finalHasRoot) {
                    Toast.makeText(
                            this,
                            "Root access unavailable",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    saveVectorState(this, finalVectorActive);
                    updateLauncherIdentity(finalVectorActive);
                    refreshQuickSettingsTile(this);
                }

                finish();
            });
        });
    }

    private void updateLauncherIdentity(boolean vectorActive) {
        PackageManager pm = getPackageManager();

        ComponentName vectorAlias =
                new ComponentName(this, VECTOR_ALIAS);

        ComponentName defaultAlias =
                new ComponentName(this, DEFAULT_ALIAS);

        int currentVectorState =
                pm.getComponentEnabledSetting(vectorAlias);

        int currentDefaultState =
                pm.getComponentEnabledSetting(defaultAlias);

        if (vectorActive) {
            if (currentVectorState !=
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {

                pm.setComponentEnabledSetting(
                        vectorAlias,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                );
            }

            if (currentDefaultState !=
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {

                pm.setComponentEnabledSetting(
                        defaultAlias,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                );
            }

        } else {
            if (currentVectorState !=
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {

                pm.setComponentEnabledSetting(
                        vectorAlias,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                );
            }

            if (currentDefaultState !=
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {

                pm.setComponentEnabledSetting(
                        defaultAlias,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                );
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}