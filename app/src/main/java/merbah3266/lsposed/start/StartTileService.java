package merbah3266.lsposed.start;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class StartTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivityAndCollapse(intent);

        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }

        if (isVectorActive()) {
            tile.setLabel("Vector");
            tile.setIcon(Icon.createWithResource(
                    this,
                    R.drawable.ic_victory_monochrome
            ));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("Launch \"Vector\"");
            }
        } else {
            tile.setLabel("LSPosed");
            tile.setIcon(Icon.createWithResource(
                    this,
                    R.drawable.ic_launcher_foreground
            ));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("Launch \"LSPosed\"");
            }
        }

        tile.setState(Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    private boolean isVectorActive() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                    "su",
                    "-c",
                    "[ -d /data/adb/modules/zygisk_vector ] && " +
                    "[ ! -e /data/adb/modules/zygisk_vector/disable ]"
            });

            int exitCode = process.waitFor();
            return exitCode == 0;

        } catch (Exception e) {
            return false;
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