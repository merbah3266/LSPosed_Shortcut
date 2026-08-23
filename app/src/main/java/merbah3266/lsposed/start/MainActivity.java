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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends Activity {

    private static final String VECTOR_MODULE =
            "/data/adb/modules/zygisk_vector";

    private static final String LSPOSED_MODULE =
            "/data/adb/modules/zygisk_lsposed";

    private static final String VECTOR_ALIAS =
            "merbah3266.lsposed.start.VectorAlias";

    private static final String DEFAULT_ALIAS =
            "merbah3266.lsposed.start.DefaultAlias";

    private static final String VECTOR_SECRET_CODE = "832867";
    private static final String LSPOSED_SECRET_CODE = "5776733";

    private static final String PREFS_NAME = "qstile";
    private static final String KEY_TILE_MODE = "tile_mode";

    public static final int TILE_DEFAULT = 0;
    public static final int TILE_VECTOR = 1;
    public static final int TILE_LSPOSED = 2;

    private static final long SU_TIMEOUT_SECONDS = 3;

    private static final int RESULT_DEFAULT = 0;
    private static final int RESULT_VECTOR = 1;
    private static final int RESULT_LSPOSED = 2;

    private static final int RESULT_ROOT_FAILED = 10;
    private static final int RESULT_BROADCAST_FAILED = 11;
    private static final int RESULT_TIMEOUT = 12;
    private static final int RESULT_UNKNOWN = 13;

    private static final String STAGE_START = "بدء جلسة root";
    private static final String STAGE_ROOT = "التحقق من صلاحيات root";
    private static final String STAGE_VECTOR = "فحص Vector";
    private static final String STAGE_LSPOSED = "فحص LSPosed";
    private static final String STAGE_BROADCAST_VECTOR = "إرسال أمر Vector";
    private static final String STAGE_BROADCAST_LSPOSED = "إرسال أمر LSPosed";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private static final class RootResult {
        int result;
        String stage;

        RootResult(int result, String stage) {
            this.result = result;
            this.stage = stage;
        }
    }

    private RootResult checkAndLaunch() {
        Process process = null;

        AtomicReference<String> currentStage =
                new AtomicReference<>(STAGE_START);

        try {
            String action = Build.VERSION.SDK_INT >= 29
                    ? "android.telephony.action.SECRET_CODE"
                    : "android.provider.Telephony.SECRET_CODE";

            String command =
                    "echo STAGE:ROOT; " +

                    "if [ \"$(id -u)\" != \"0\" ]; then " +
                        "echo RESULT:ROOT_FAILED; " +
                        "exit " + RESULT_ROOT_FAILED + "; " +
                    "fi; " +

                    "echo STAGE:VECTOR; " +
                    "VECTOR=0; " +
                    "if [ -d '" + VECTOR_MODULE + "' ] && " +
                    "[ ! -e '" + VECTOR_MODULE + "/disable' ]; then " +
                        "VECTOR=1; " +
                    "fi; " +

                    "echo STAGE:LSPOSED; " +
                    "LSPOSED=0; " +
                    "if [ -d '" + LSPOSED_MODULE + "' ] && " +
                    "[ ! -e '" + LSPOSED_MODULE + "/disable' ]; then " +
                        "LSPOSED=1; " +
                    "fi; " +

                    "if [ \"$VECTOR\" = \"1\" ]; then " +
                        "echo STAGE:BROADCAST_VECTOR; " +
                        "am broadcast --user 0 " +
                        "-a " + action + " " +
                        "-d android_secret_code://" +
                        VECTOR_SECRET_CODE +
                        " >/dev/null 2>&1; " +
                        "STATUS=$?; " +
                        "if [ \"$STATUS\" != \"0\" ]; then " +
                            "echo RESULT:BROADCAST_FAILED; " +
                            "exit " + RESULT_BROADCAST_FAILED + "; " +
                        "fi; " +
                        "echo RESULT:VECTOR; " +
                        "exit 0; " +

                    "elif [ \"$LSPOSED\" = \"1\" ]; then " +
                        "echo STAGE:BROADCAST_LSPOSED; " +
                        "am broadcast --user 0 " +
                        "-a " + action + " " +
                        "-d android_secret_code://" +
                        LSPOSED_SECRET_CODE +
                        " >/dev/null 2>&1; " +
                        "STATUS=$?; " +
                        "if [ \"$STATUS\" != \"0\" ]; then " +
                            "echo RESULT:BROADCAST_FAILED; " +
                            "exit " + RESULT_BROADCAST_FAILED + "; " +
                        "fi; " +
                        "echo RESULT:LSPOSED; " +
                        "exit 0; " +

                    "else " +
                        "echo RESULT:DEFAULT; " +
                        "exit 0; " +
                    "fi";

            process = new ProcessBuilder(
                    "su",
                    "-c",
                    command
            ).redirectErrorStream(true).start();

            Process finalProcess = process;

            Thread outputReader = new Thread(() -> {
                try {
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            finalProcess.getInputStream()
                                    )
                            );

                    String line;

                    while ((line = reader.readLine()) != null) {
                        switch (line) {
                            case "STAGE:ROOT":
                                currentStage.set(STAGE_ROOT);
                                break;

                            case "STAGE:VECTOR":
                                currentStage.set(STAGE_VECTOR);
                                break;

                            case "STAGE:LSPOSED":
                                currentStage.set(STAGE_LSPOSED);
                                break;

                            case "STAGE:BROADCAST_VECTOR":
                                currentStage.set(STAGE_BROADCAST_VECTOR);
                                break;

                            case "STAGE:BROADCAST_LSPOSED":
                                currentStage.set(STAGE_BROADCAST_LSPOSED);
                                break;
                        }
                    }
                } catch (Exception ignored) {
                }
            });

            outputReader.start();

            boolean finished = process.waitFor(
                    SU_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
            );

            if (!finished) {
                String stage = currentStage.get();

                process.destroyForcibly();

                return new RootResult(
                        RESULT_TIMEOUT,
                        stage
                );
            }

            outputReader.join(200);

            int exitCode = process.exitValue();

            if (exitCode == RESULT_ROOT_FAILED) {
                return new RootResult(
                        RESULT_ROOT_FAILED,
                        STAGE_ROOT
                );
            }

            if (exitCode == RESULT_BROADCAST_FAILED) {
                return new RootResult(
                        RESULT_BROADCAST_FAILED,
                        currentStage.get()
                );
            }

            if (exitCode == 0) {
                if (currentStage.get().equals(STAGE_BROADCAST_VECTOR)) {
                    return new RootResult(
                            RESULT_VECTOR,
                            currentStage.get()
                    );
                }

                if (currentStage.get().equals(STAGE_BROADCAST_LSPOSED)) {
                    return new RootResult(
                            RESULT_LSPOSED,
                            currentStage.get()
                    );
                }

                return new RootResult(
                        RESULT_DEFAULT,
                        currentStage.get()
                );
            }

            return new RootResult(
                    RESULT_UNKNOWN,
                    currentStage.get()
            );

        } catch (Exception e) {
            return new RootResult(
                    RESULT_UNKNOWN,
                    currentStage.get()
            );

        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    public static void saveTileMode(Context context, int mode) {
        context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        ).edit()
                .putInt(KEY_TILE_MODE, mode)
                .apply();
    }

    public static int getTileMode(Context context) {
        return context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        ).getInt(KEY_TILE_MODE, TILE_DEFAULT);
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
            RootResult result = checkAndLaunch();

            runOnUiThread(() -> {
                switch (result.result) {

                    case RESULT_VECTOR:
                        saveTileMode(this, TILE_VECTOR);
                        updateLauncherIdentity(true);
                        refreshQuickSettingsTile(this);
                        finish();
                        break;

                    case RESULT_LSPOSED:
                        saveTileMode(this, TILE_LSPOSED);
                        updateLauncherIdentity(false);
                        refreshQuickSettingsTile(this);
                        finish();
                        break;

                    case RESULT_DEFAULT:
                        saveTileMode(this, TILE_DEFAULT);
                        refreshQuickSettingsTile(this);
                        finish();
                        break;

                    case RESULT_TIMEOUT:
                        showError(
                                "E354 — انتهت المهلة أثناء " +
                                result.stage
                        );
                        finish();
                        break;

                    case RESULT_ROOT_FAILED:
                        showError(
                                "E351 — فشل الحصول على صلاحيات root"
                        );
                        finish();
                        break;

                    case RESULT_BROADCAST_FAILED:
                        showError(
                                "E353 — فشل إرسال الأمر أثناء " +
                                result.stage
                        );
                        finish();
                        break;

                    default:
                        showError(
                                "E355 — خطأ غير معروف أثناء " +
                                result.stage
                        );
                        finish();
                        break;
                }
            });
        });
    }

    private void showError(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
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