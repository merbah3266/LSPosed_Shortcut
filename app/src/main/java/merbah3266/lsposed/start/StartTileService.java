package merbah3266.lsposed.start;

import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class StartTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();

        boolean vectorActive =
                MainActivity.getSavedVectorState(this);

        updateTile(vectorActive);
    }

    @Override
    public void onClick() {
        super.onClick();

        MainActivity.refreshQuickSettingsTile(this);

        android.content.Intent intent =
                new android.content.Intent(this, MainActivity.class);

        intent.addFlags(
                android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        );

        startActivityAndCollapse(intent);

        Tile tile = getQsTile();

        if (tile != null) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
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