package merbah3266.lsposed.start;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

public class StartTileService extends TileService {

    private static final String TAG = "LSPOSED_TILE";
    private static final int LAUNCH_REQUEST_CODE = 1000;

    @Override
    public void onStartListening() {
        super.onStartListening();

        Log.d(TAG, "onStartListening CALLED");

        updateTile(MainActivity.getTileMode(this));
    }

    @Override
    public void onClick() {
        super.onClick();

        Log.d(TAG, "onClick CALLED");

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                LAUNCH_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                Log.d(TAG, "Launching MainActivity using PendingIntent");

                startActivityAndCollapse(pendingIntent);
            } else {
                Log.d(TAG, "Launching MainActivity using Intent");

                startActivityAndCollapse(intent);
            }

            Log.d(TAG, "startActivityAndCollapse CALLED");

        } catch (Exception e) {
            Log.e(TAG, "Failed to launch MainActivity", e);
        }
    }

    private void updateTile(int mode) {
        Tile tile = getQsTile();

        if (tile == null) {
            Log.w(TAG, "getQsTile() returned null");
            return;
        }

        Log.d(TAG, "updateTile mode=" + mode);

        if (mode == MainActivity.TILE_VECTOR) {

            tile.setLabel("Vector");

            tile.setIcon(
                    Icon.createWithResource(
                            this,
                            R.drawable.ic_victory_monochrome
                    )
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("Launch Vector");
            }

        } else if (mode == MainActivity.TILE_LSPOSED) {

            tile.setLabel("LSPosed");

            tile.setIcon(
                    Icon.createWithResource(
                            this,
                            R.drawable.ic_qs_lsposed
                    )
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("Launch LSPosed");
            }

        } else {

            restoreManifestTile(tile);
        }

        tile.setState(Tile.STATE_INACTIVE);
        tile.updateTile();

        Log.d(TAG, "Tile updated");
    }

    private void restoreManifestTile(Tile tile) {
        try {
            PackageManager pm = getPackageManager();

            ComponentName componentName =
                    new ComponentName(this, StartTileService.class);

            ServiceInfo serviceInfo = pm.getServiceInfo(
                    componentName,
                    PackageManager.GET_META_DATA
            );

            CharSequence label = serviceInfo.loadLabel(pm);

            if (label != null) {
                tile.setLabel(label);
            }

            if (serviceInfo.icon != 0) {
                tile.setIcon(
                        Icon.createWithResource(
                                this,
                                serviceInfo.icon
                        )
                );
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle(null);
            }

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to get StartTileService info", e);
        }
    }

    @Override
    public void onStopListening() {
        Log.d(TAG, "onStopListening CALLED");

        Tile tile = getQsTile();

        if (tile != null) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        }

        super.onStopListening();
    }
}