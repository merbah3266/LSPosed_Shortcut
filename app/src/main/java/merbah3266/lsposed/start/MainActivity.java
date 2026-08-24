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
import java.io.IOException;
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

    private static final String MAIN_ACTIVITY =
            "merbah3266.lsposed.start.MainActivity";

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

    private static final String STAGE_START =
            "starting root session";

    private static final String STAGE_ROOT =
            "checking root permission";

    private static final String STAGE_VECTOR =
            "checking Vector";

    private static final String STAGE_LSPOSED =
            "checking LSPosed";

    private static final String STAGE_BROADCAST_VECTOR =
            "sending Vector command";

    private static final String STAGE_BROADCAST_LSPOSED =
            "sending LSPosed command";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private static final class RootResult {

        final int result;
        final String stage;

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
                    "[ \"$(id -u)\" != \"0\" ] && exit "
                    + RESULT_ROOT_FAILED + "; " +

                    "echo STAGE:VECTOR; " +
                    "if [ -d '" + VECTOR_MODULE + "' ] && " +
                    "[ ! -e '" + VECTOR_MODULE + "/disable' ]; then " +

                    "echo STAGE:BROADCAST_VECTOR; " +

                    "am broadcast --user 0 -a " + action +
                    " -d android_secret_code://" +
                    VECTOR_SECRET_CODE +
                    " >/dev/null 2>&1 && exit " +
                    RESULT_VECTOR + "; " +

                    "exit " + RESULT_BROADCAST_FAILED + "; " +
                    "fi; " +

                    "echo STAGE:LSPOSED; " +

                    "if [ -d '" + LSPOSED_MODULE + "' ] && " +
                    "[ ! -e '" + LSPOSED_MODULE + "/disable' ]; then " +

                    "echo STAGE:BROADCAST_LSPOSED; " +

                    "am broadcast --user 0 -a " + action +
                    " -d android_secret_code://" +
                    LSPOSED_SECRET_CODE +
                    " >/dev/null 2>&1 && exit " +
                    RESULT_LSPOSED + "; " +

                    "exit " + RESULT_BROADCAST_FAILED + "; " +
                    "fi; " +

                    "exit " + RESULT_DEFAULT;

            process = new ProcessBuilder(
                    "su",
                    "-c",
                    command
            )
                    .redirectErrorStream(true)
                    .start();

            Process finalProcess = process;

            Thread outputReader = new Thread(() -> {

                try (
                        BufferedReader reader =
                                new BufferedReader(
                                        new InputStreamReader(
                                                finalProcess
                                                        .getInputStream()
                                        )
                                )
                ) {

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
                                currentStage.set(
                                        STAGE_BROADCAST_VECTOR
                                );
                                break;

                            case "STAGE:BROADCAST_LSPOSED":
                                currentStage.set(
                                        STAGE_BROADCAST_LSPOSED
                                );
                                break;
                        }
                    }

                } catch (Exception ignored) {
                }

            });

            outputReader.setDaemon(true);
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

            int exitCode = process.exitValue();
            String stage = currentStage.get();

            if (exitCode == RESULT_VECTOR) {
                return new RootResult(
                        RESULT_VECTOR,
                        stage
                );
            }

            if (exitCode == RESULT_LSPOSED) {
                return new RootResult(
                        RESULT_LSPOSED,
                        stage
                );
            }

            if (exitCode == RESULT_DEFAULT) {
                return new RootResult(
                        RESULT_DEFAULT,
                        stage
                );
            }

            if (exitCode == RESULT_ROOT_FAILED) {
                return new RootResult(
                        RESULT_ROOT_FAILED,
                        STAGE_ROOT
                );
            }

            if (exitCode == RESULT_BROADCAST_FAILED) {
                return new RootResult(
                        RESULT_BROADCAST_FAILED,
                        stage
                );
            }

            if (STAGE_ROOT.equals(stage)) {
                return new RootResult(
                        RESULT_ROOT_FAILED,
                        stage
                );
            }

            return new RootResult(
                    RESULT_UNKNOWN,
                    stage
            );

        } catch (IOException e) {

            return new RootResult(
                    RESULT_ROOT_FAILED,
                    STAGE_ROOT
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

    private String getReadableStage(String stage) {

        switch (stage) {

            case STAGE_ROOT:
                return "checking root permission";

            case STAGE_VECTOR:
                return "checking Vector";

            case STAGE_LSPOSED:
                return "checking LSPosed";

            case STAGE_BROADCAST_VECTOR:
                return "sending Vector command";

            case STAGE_BROADCAST_LSPOSED:
                return "sending LSPosed command";

            default:
                return "initializing";
        }
    }

    public static void saveTileMode(
            Context context,
            int mode
    ) {

        context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        )
                .edit()
                .putInt(KEY_TILE_MODE, mode)
                .apply();
    }

    public static int getTileMode(Context context) {

        return context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        )
                .getInt(
                        KEY_TILE_MODE,
                        TILE_DEFAULT
                );
    }

    public static void refreshQuickSettingsTile(
            Context context
    ) {

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

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                String readableStage =
                        getReadableStage(result.stage);

                switch (result.result) {

                    case RESULT_VECTOR:

                        saveTileMode(
                                this,
                                TILE_VECTOR
                        );

                        updateLauncherIdentity(
                                TILE_VECTOR
                        );

                        refreshQuickSettingsTile(this);

                        finish();
                        break;

                    case RESULT_LSPOSED:

                        saveTileMode(
                                this,
                                TILE_LSPOSED
                        );

                        updateLauncherIdentity(
                                TILE_LSPOSED
                        );

                        refreshQuickSettingsTile(this);

                        finish();
                        break;

                    default:

                        saveTileMode(
                                this,
                                TILE_DEFAULT
                        );

                        updateLauncherIdentity(
                                TILE_DEFAULT
                        );

                        refreshQuickSettingsTile(this);

                        if (result.result ==
                                RESULT_ROOT_FAILED) {

                            showError(
                                    "Root permission failed or denied"
                            );

                        } else if (
                                result.result ==
                                        RESULT_BROADCAST_FAILED
                        ) {

                            showError(
                                    "Broadcast failed during: "
                                            + readableStage
                            );

                        } else if (
                                result.result ==
                                        RESULT_TIMEOUT
                        ) {

                            showError(
                                    "Timeout during: "
                                            + readableStage
                            );

                        } else if (
                                result.result != RESULT_DEFAULT
                        ) {

                            showError(
                                    "Unknown error during: "
                                            + readableStage
                            );
                        }

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
                Toast.LENGTH_LONG
        ).show();
    }

    private void updateLauncherIdentity(int mode) {

        PackageManager pm = getPackageManager();

        ComponentName mainActivity =
                new ComponentName(
                        this,
                        MAIN_ACTIVITY
                );

        ComponentName vectorAlias =
                new ComponentName(
                        this,
                        VECTOR_ALIAS
                );

        ComponentName defaultAlias =
                new ComponentName(
                        this,
                        DEFAULT_ALIAS
                );

        int mainState =
                mode == TILE_DEFAULT
                        ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        int vectorState =
                mode == TILE_VECTOR
                        ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        int defaultState =
                mode == TILE_LSPOSED
                        ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        pm.setComponentEnabledSetting(
                mainActivity,
                mainState,
                PackageManager.DONT_KILL_APP
        );

        pm.setComponentEnabledSetting(
                vectorAlias,
                vectorState,
                PackageManager.DONT_KILL_APP
        );

        pm.setComponentEnabledSetting(
                defaultAlias,
                defaultState,
                PackageManager.DONT_KILL_APP
        );
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdownNow();
    }
}