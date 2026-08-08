package merbah3266.lsposed.start;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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

                String action = android.os.Build.VERSION.SDK_INT >= 29
                        ? "android.telephony.action.SECRET_CODE"
                        : "android.provider.Telephony.SECRET_CODE";

                String command =
                        "if [ -d '" + VECTOR_MODULE + "' ] && " +
                        "[ ! -e '" + VECTOR_MODULE + "/disable' ]; then " +
                        "  am broadcast --user 0 -a " + action + " -d android_secret_code://" + VECTOR_SECRET_CODE + " > /dev/null 2>&1; " +
                        "  echo 'VECTOR_ACTIVE'; " +
                        "else " +
                        "  am broadcast --user 0 -a " + action + " -d android_secret_code://" + LSPOSED_SECRET_CODE + " > /dev/null 2>&1; " +
                        "  echo 'VECTOR_INACTIVE'; " +
                        "fi\nexit\n";

                os.write(command.getBytes("UTF-8"));
                os.flush();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if ("VECTOR_ACTIVE".equals(line.trim())) {
                        vectorActive = true;
                        hasRoot = true;
                    } else if ("VECTOR_INACTIVE".equals(line.trim())) {
                        vectorActive = false;
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
                    Toast.makeText(this, "Root access unavailable", Toast.LENGTH_SHORT).show();
                } else {
                    updateLauncherIdentity(finalVectorActive);
                }
                
                finish();
            });
        });
    }

    private void updateLauncherIdentity(boolean vectorActive) {
        PackageManager pm = getPackageManager();

        ComponentName vectorAlias = new ComponentName(this, VECTOR_ALIAS);
        ComponentName defaultAlias = new ComponentName(this, DEFAULT_ALIAS);

        int currentVectorState = pm.getComponentEnabledSetting(vectorAlias);
        int currentDefaultState = pm.getComponentEnabledSetting(defaultAlias);

        if (vectorActive) {
            if (currentVectorState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                pm.setComponentEnabledSetting(
                        vectorAlias,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                );
            }
            if (currentDefaultState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                pm.setComponentEnabledSetting(
                        defaultAlias,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                );
            }
        } else {
            if (currentVectorState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                pm.setComponentEnabledSetting(
                        vectorAlias,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                );
            }
            if (currentDefaultState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
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
