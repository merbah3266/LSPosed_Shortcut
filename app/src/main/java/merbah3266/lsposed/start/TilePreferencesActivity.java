package merbah3266.lsposed.start;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class TilePreferencesActivity extends Activity {

    private Switch hideIconSwitch;

    private int popupColor;
    private int settingColor;
    private int primaryTextColor;
    private int secondaryTextColor;
    private int accentColor;
    private int switchOffColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        boolean darkMode =
                (getResources().getConfiguration().uiMode
                        & Configuration.UI_MODE_NIGHT_MASK)
                        == Configuration.UI_MODE_NIGHT_YES;

        if (darkMode) {
            popupColor = Color.rgb(30, 30, 30);
            settingColor = Color.rgb(45, 45, 45);
            primaryTextColor = Color.rgb(245, 245, 245);
            secondaryTextColor = Color.rgb(185, 185, 185);
            accentColor = Color.rgb(180, 205, 255);
            switchOffColor = Color.rgb(100, 100, 100);
        } else {
            popupColor = Color.rgb(255, 255, 255);
            settingColor = Color.rgb(245, 245, 245);
            primaryTextColor = Color.rgb(30, 30, 30);
            secondaryTextColor = Color.rgb(95, 95, 95);
            accentColor = Color.rgb(45, 90, 170);
            switchOffColor = Color.rgb(125, 125, 125);
        }

        Window window = getWindow();

        window.setBackgroundDrawableResource(
                android.R.color.transparent
        );

        window.addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
        );

        WindowManager.LayoutParams params =
                window.getAttributes();

        params.dimAmount = 0.55f;
        window.setAttributes(params);

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

        window.setLayout(
                (int) (
                        getResources()
                                .getDisplayMetrics()
                                .widthPixels * 0.90f
                ),
                WindowManager.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout buildLayout(boolean currentlyHidden) {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(
                dp(24),
                dp(22),
                dp(24),
                dp(14)
        );

        root.setBackground(
                createRoundedBackground(
                        popupColor,
                        dp(24)
                )
        );

        TextView title = new TextView(this);
        title.setText("Launcher icon");
        title.setTextSize(22);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        title.setTextColor(primaryTextColor);

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        TextView subtitle = new TextView(this);
        subtitle.setText(
                "Choose whether the app icon is visible."
        );
        subtitle.setTextSize(14);
        subtitle.setTextColor(secondaryTextColor);
        subtitle.setPadding(
                0,
                dp(6),
                0,
                dp(18)
        );

        root.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        LinearLayout settingRow = new LinearLayout(this);
        settingRow.setOrientation(
                LinearLayout.HORIZONTAL
        );
        settingRow.setGravity(
                Gravity.CENTER_VERTICAL
        );
        settingRow.setPadding(
                dp(16),
                dp(12),
                dp(10),
                dp(12)
        );

        settingRow.setBackground(
                createRoundedBackground(
                        settingColor,
                        dp(16)
                )
        );

        TextView settingTitle = new TextView(this);
        settingTitle.setText("Hide icon");
        settingTitle.setTextSize(16);
        settingTitle.setTextColor(primaryTextColor);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                );

        settingRow.addView(
                settingTitle,
                titleParams
        );

        hideIconSwitch = new Switch(this);
        hideIconSwitch.setChecked(currentlyHidden);
        hideIconSwitch.setContentDescription(
                "Hide launcher icon"
        );

        hideIconSwitch.setButtonTintList(
                createSwitchTintList()
        );

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            hideIconSwitch.setThumbTintList(
                    createSwitchThumbTintList()
            );

            hideIconSwitch.setTrackTintList(
                    createSwitchTrackTintList()
            );
        }

        settingRow.addView(
                hideIconSwitch,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        root.addView(
                settingRow,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(
                LinearLayout.HORIZONTAL
        );
        buttonRow.setGravity(
                Gravity.END | Gravity.CENTER_VERTICAL
        );
        buttonRow.setPadding(
                0,
                dp(8),
                0,
                0
        );

        Button cancelButton =
                createButton("Cancel");

        cancelButton.setTextColor(
                secondaryTextColor
        );

        cancelButton.setOnClickListener(
                v -> finish()
        );

        Button saveButton =
                createButton("Save");

        saveButton.setTextColor(
                accentColor
        );

        saveButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        saveButton.setOnClickListener(
                v -> onSave()
        );

        buttonRow.addView(
                cancelButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dp(48)
                )
        );

        buttonRow.addView(
                saveButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dp(48)
                )
        );

        root.addView(
                buttonRow,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        return root;
    }

    private ColorStateList createSwitchTintList() {

        return new ColorStateList(
                new int[][] {
                        new int[] { android.R.attr.state_checked },
                        new int[] {}
                },
                new int[] {
                        accentColor,
                        switchOffColor
                }
        );
    }

    private ColorStateList createSwitchThumbTintList() {

        return new ColorStateList(
                new int[][] {
                        new int[] { android.R.attr.state_checked },
                        new int[] {}
                },
                new int[] {
                        accentColor,
                        Color.rgb(235, 235, 235)
                }
        );
    }

    private ColorStateList createSwitchTrackTintList() {

        int checkedTrackColor;

        if (accentColor == Color.rgb(180, 205, 255)) {
            checkedTrackColor = Color.rgb(95, 120, 170);
        } else {
            checkedTrackColor = Color.rgb(145, 170, 220);
        }

        return new ColorStateList(
                new int[][] {
                        new int[] { android.R.attr.state_checked },
                        new int[] {}
                },
                new int[] {
                        checkedTrackColor,
                        switchOffColor
                }
        );
    }

    private Button createButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(
                dp(16),
                0,
                dp(16),
                0
        );
        button.setGravity(
                Gravity.CENTER
        );
        button.setBackgroundColor(
                Color.TRANSPARENT
        );

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            button.setStateListAnimator(null);
        }

        return button;
    }

    private GradientDrawable createRoundedBackground(
            int color,
            int radius
    ) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(radius);

        return drawable;
    }

    private void onSave() {

        boolean hide =
                hideIconSwitch.isChecked();

        SharedPreferences prefs =
                getSharedPreferences(
                        MainActivity.PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        prefs.edit()
                .putBoolean(
                        MainActivity.KEY_HIDE_LAUNCHER_ICON,
                        hide
                )
                .apply();

        int mode =
                prefs.getInt(
                        MainActivity.KEY_TILE_MODE,
                        MainActivity.TILE_DEFAULT
                );

        MainActivity.updateLauncherIdentity(
                this,
                mode
        );

        Toast.makeText(
                this,
                "Changes saved",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    private int dp(int value) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                value * density
        );
    }
}