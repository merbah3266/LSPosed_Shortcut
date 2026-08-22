package merbah3266.lsposed.start;

import android.app.PendingIntent;
import android.content.Intent;
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

        updateTile(MainActivity.getSavedVectorState(this));

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

    private void updateTile(boolean vectorActive) {
        Tile tile = getQsTile();

        if (tile == null) {
            return;
        }

        if (vectorActive) {
            tile.setLabel("Vector");
            tile.setIcon(
                    Icon.createWithResource(
                            this,
                            R.drawable.ic_victory_monochrome
                    )
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("Launch \"Vector\"");
            }
        } else {
            tile.setLabel("LSPosed");
            tile.setIcon(
                    Icon.createWithResource(
                            this,
                            R.drawable.ic_launcher_foreground
                    )
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("Launch \"LSPosed\"");
            }
        }

        tile.setState(Tile.STATE_INACTIVE);
        tile.updateTile();
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