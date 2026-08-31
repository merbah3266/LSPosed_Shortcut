package merbah3266.lsposed.start;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class TilePreferencesActivity extends Activity {

    private Switch hideIconSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SharedPreferences prefs =
                getSharedPreferences(
                        MainActivity.PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        boolean currentlyHidden =
                prefs.getBoolean(
                        MainActivity.KEY_HIDE_LAUNCHER_ICON,
                        false
                );

        setContentView(buildLayout(currentlyHidden));
    }

    private LinearLayout buildLayout(boolean currentlyHidden) {

        int padding = dp(24);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(padding, padding, padding, padding);

        TextView title = new TextView(this);
        title.setText("Launcher Icon");
        title.setTextSize(18);
        title.setTextColor(Color.BLACK);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Choose whether the app icon stays visible in the launcher.");
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setPadding(0, dp(8), 0, dp(20));
        root.addView(subtitle);

        LinearLayout switchRow = new LinearLayout(this);
        switchRow.setOrientation(LinearLayout.HORIZONTAL);
        switchRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView switchLabel = new TextView(this);
        switchLabel.setText("Hide launcher icon");
        switchLabel.setTextColor(Color.BLACK);

        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        switchRow.addView(switchLabel, labelParams);

        hideIconSwitch = new Switch(this);
        hideIconSwitch.setChecked(currentlyHidden);
        switchRow.addView(hideIconSwitch);

        root.addView(switchRow);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setGravity(Gravity.END);
        buttonRow.setPadding(0, dp(28), 0, 0);

        Button cancelButton = new Button(this);
        cancelButton.setText("Cancel");
        cancelButton.setOnClickListener(v -> finish());
        buttonRow.addView(cancelButton);

        Button saveButton = new Button(this);
        saveButton.setText("Save Changes");
        saveButton.setOnClickListener(v -> onSave());
        buttonRow.addView(saveButton);

        root.addView(buttonRow);

        return root;
    }

    private void onSave() {

        boolean hide = hideIconSwitch.isChecked();

        SharedPreferences prefs =
                getSharedPreferences(
                        MainActivity.PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        prefs.edit()
                .putBoolean(MainActivity.KEY_HIDE_LAUNCHER_ICON, hide)
                .apply();

        int mode =
                prefs.getInt(
                        MainActivity.KEY_TILE_MODE,
                        MainActivity.TILE_DEFAULT
                );

        MainActivity.updateLauncherIdentity(this, mode);

        finish();
    }

    private int dp(int value) {

        float density = getResources().getDisplayMetrics().density;

        return Math.round(value * density);
    }
}