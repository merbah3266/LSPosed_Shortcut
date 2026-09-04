package merbah3266.lsposed.start;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
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
    private Button saveButton;

    private boolean initialHiddenState;

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

        initialHiddenState = currentlyHidden;

        setContentView(buildLayout(currentlyHidden));

        window.getDecorView().post(() -> {

            int screenWidth =
                    getResources()
                            .getDisplayMetrics()
                            .widthPixels;

            int width =
                    Math.min(
                            (int) (screenWidth * 0.90f),
                            dp(420)
                    );

            window.setLayout(
                    width,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        });
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

        root.setElevation(dp(6));

        if (android.os.Build.VERSION.SDK_INT >= 28) {
            root.setOutlineAmbientShadowColor(Color.BLACK);
            root.setOutlineSpotShadowColor(Color.BLACK);
        }

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
                dp(16),
                dp(12)
        );

        settingRow.setBackground(
                createSettingRowBackground()
        );

        TextView settingTitle = new TextView(this);
        settingTitle.setText("Hide icon");
        settingTitle.setTextSize(16);
        settingTitle.setTextColor(primaryTextColor);
        settingTitle.setCompoundDrawablesWithIntrinsicBounds(
                new EyeOffDrawable(primaryTextColor, dp(18)),
                null,
                null,
                null
        );
        settingTitle.setCompoundDrawablePadding(dp(10));

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

        hideIconSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        updateSaveButtonState(isChecked)
        );

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

        this.saveButton = saveButton;

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

        saveButton.setEnabled(false);
        saveButton.setAlpha(0.4f);

        LinearLayout.LayoutParams cancelParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dp(40)
                );

        buttonRow.addView(
                cancelButton,
                cancelParams
        );

        LinearLayout.LayoutParams saveParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dp(40)
                );

        saveParams.setMarginStart(dp(4));

        buttonRow.addView(
                saveButton,
                saveParams
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
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setIncludeFontPadding(false);
        button.setPadding(
                dp(10),
                0,
                dp(10),
                0
        );
        button.setGravity(
                Gravity.CENTER
        );
        button.setBackground(
                createButtonRipple()
        );
        button.setStateListAnimator(null);

        return button;
    }

    private RippleDrawable createButtonRipple() {

        GradientDrawable mask =
                new GradientDrawable();

        mask.setColor(Color.WHITE);
        mask.setCornerRadius(dp(20));

        int rippleColor =
                Color.argb(
                        80,
                        Color.red(accentColor),
                        Color.green(accentColor),
                        Color.blue(accentColor)
                );

        return new RippleDrawable(
                ColorStateList.valueOf(rippleColor),
                null,
                mask
        );
    }

    private GradientDrawable createSettingRowBackground() {

        GradientDrawable drawable =
                createRoundedBackground(
                        settingColor,
                        dp(16)
                );

        int strokeColor =
                Color.argb(
                        30,
                        Color.red(primaryTextColor),
                        Color.green(primaryTextColor),
                        Color.blue(primaryTextColor)
                );

        drawable.setStroke(
                dp(1),
                strokeColor
        );

        return drawable;
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

    private void updateSaveButtonState(boolean isChecked) {

        boolean changed =
                isChecked != initialHiddenState;

        saveButton.setEnabled(changed);
        saveButton.setAlpha(
                changed ? 1f : 0.4f
        );
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

    private static final class EyeOffDrawable extends Drawable {

        private static final String SVG_PATH_DATA =
                "M3.99989 4L19.9999 20" +
                "M16.4999 16.7559C15.1473 17.4845 13.6185 17.9999 11.9999 17.9999" +
                "C8.46924 17.9999 5.36624 15.5478 3.5868 13.7788" +
                "C3.1171 13.3119 2.88229 13.0784 2.7328 12.6201" +
                "C2.62619 12.2933 2.62616 11.7066 2.7328 11.3797" +
                "C2.88233 10.9215 3.11763 10.6875 3.58827 10.2197" +
                "C4.48515 9.32821 5.71801 8.26359 7.17219 7.42676" +
                "M19.4999 14.6335C19.8329 14.3405 20.138 14.0523 20.4117 13.7803" +
                "L20.4146 13.7772" +
                "C20.8832 13.3114 21.1182 13.0779 21.2674 12.6206" +
                "C21.374 12.2938 21.3738 11.7068 21.2672 11.38" +
                "C21.1178 10.9219 20.8827 10.6877 20.4133 10.2211" +
                "C18.6338 8.45208 15.5305 6 11.9999 6" +
                "C11.6624 6 11.3288 6.02241 10.9999 6.06448" +
                "M13.3228 13.5C12.9702 13.8112 12.5071 14 11.9999 14" +
                "C10.8953 14 9.99989 13.1046 9.99989 12" +
                "C9.99989 11.4605 10.2135 10.9711 10.5608 10.6113";

        private static final float STROKE_WIDTH = 0.9f;

        private final Path path;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int size;

        EyeOffDrawable(int color, int size) {

            this.size = size;
            this.path = parseSvgPath(SVG_PATH_DATA);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(STROKE_WIDTH);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(color);
        }

        private static Path parseSvgPath(String data) {

            Path result = new Path();

            java.util.regex.Matcher tokenMatcher =
                    java.util.regex.Pattern
                            .compile("[MLC]|-?\\d+\\.?\\d*")
                            .matcher(data);

            java.util.List<String> tokens =
                    new java.util.ArrayList<>();

            while (tokenMatcher.find()) {
                tokens.add(tokenMatcher.group());
            }

            char command = ' ';
            java.util.List<Float> args =
                    new java.util.ArrayList<>();

            for (String token : tokens) {

                if (token.equals("M")
                        || token.equals("L")
                        || token.equals("C")) {

                    command = token.charAt(0);
                    args.clear();
                    continue;
                }

                args.add(Float.parseFloat(token));

                int required =
                        command == 'C' ? 6 : 2;

                if (args.size() == required) {

                    applySegment(result, command, args);
                    args.clear();
                }
            }

            return result;
        }

        private static void applySegment(
                Path path,
                char command,
                java.util.List<Float> a
        ) {

            switch (command) {

                case 'M':
                    path.moveTo(a.get(0), a.get(1));
                    break;

                case 'L':
                    path.lineTo(a.get(0), a.get(1));
                    break;

                case 'C':
                    path.cubicTo(
                            a.get(0), a.get(1),
                            a.get(2), a.get(3),
                            a.get(4), a.get(5)
                    );
                    break;
            }
        }

        @Override
        public void draw(Canvas canvas) {

            android.graphics.Rect bounds = getBounds();

            float scale = bounds.width() / 24f;

            paint.setStrokeWidth(STROKE_WIDTH * scale);

            canvas.save();
            canvas.translate(bounds.left, bounds.top);
            canvas.scale(scale, scale);
            canvas.drawPath(path, paint);
            canvas.restore();
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public int getIntrinsicWidth() {
            return size;
        }

        @Override
        public int getIntrinsicHeight() {
            return size;
        }
    }
}