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

public class StartTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();

        Tile tile = getQsTile();

        if (tile == null) {
            return;
        }

        updateTile(MainActivity.getTileMode(this));

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            tile.setActivityLaunchForClick(pendingIntent);
            tile.updateTile();
        }
    }

    @Override
    public void onClick() {
        super.onClick();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT |
                    PendingIntent.FLAG_IMMUTABLE
            );

            startActivityAndCollapse(pendingIntent);
        }
    }

    private void updateTile(int mode) {
        Tile tile = getQsTile();

        if (tile == null) {
            return;
        }

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

        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    @Override
    public void onStopListening() {
        Tile tile = getQsTile();

        if (tile != null) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        }

        super.onStopListening();
    }
}