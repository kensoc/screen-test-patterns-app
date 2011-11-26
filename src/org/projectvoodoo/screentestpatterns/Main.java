/* This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 *
 * Author: François SIMOND aka supercurio
 *
 * Sources:
 * https://github.com/project-voodoo/screen-test-patterns-app
 *
 * Market:
 * https://market.android.com/details?id=org.projectvoodoo.screentestpatterns
 *
 * feedback welcome on twitter/XDA/email whatever
 * contributions are welcome too
 * IRC: Freenode, channel #project-voodoo
 */

package org.projectvoodoo.screentestpatterns;

import org.projectvoodoo.screentestpatterns.Patterns.PatternType;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class Main extends Activity implements OnClickListener, OnItemSelectedListener {

    private static final String TAG = "Voodoo ScreenTestPatterns Main";

    Patterns pattern;

    private ShapeDrawable display;
    private View patternView;
    private Spinner grayscaleLevelsSpinner;
    private Spinner nearBlackLevelsSpinner;
    private Spinner nearWhiteLevelsSpinner;
    private Spinner saturationLevelsSpinner;
    private Spinner patternTypeSpinner;
    private TextView currentPatternInfos;

    private Button setGrayscale;
    private Button setNearWhite;
    private Button setNearBlack;
    private Button setColors;
    private Button setSaturations;

    private Button next;
    private Button prev;

    private Boolean isTablet = false;

    SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // detect tablet screen size:
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if (metrics.widthPixels >= 1280 || metrics.heightPixels >= 1280)
            isTablet = true;

        // instantiate pattern engine
        pattern = new Patterns(this);

        // preference manager
        settings = getSharedPreferences(PatternGeneratorOptions.prefName, MODE_PRIVATE);

        // keep screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (!isTablet)
            requestWindowFeature(Window.FEATURE_NO_TITLE);

        // select layout first
        if (isTablet)
            setContentView(R.layout.main);
        else
            setContentView(R.layout.phone);

        // we will display the stuff here
        patternView = (View) findViewById(R.id.pattern_display);
        display = new ShapeDrawable(new OvalShape());
        display.getPaint().setColor(Color.GRAY);
        patternView.setBackgroundDrawable(display);
        patternView.setOnClickListener(this);

        if (isTablet) {
            // configure spinners
            // For grayscale measurements
            grayscaleLevelsSpinner = (Spinner) findViewById(R.id.spinner_grayscale_levels);
            ArrayAdapter<CharSequence> grayscaleAdapter = ArrayAdapter.createFromResource(this,
                    R.array.grayscale_array, android.R.layout.simple_spinner_item);
            grayscaleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            grayscaleLevelsSpinner.setAdapter(grayscaleAdapter);
            setSpinnerValue(
                    grayscaleLevelsSpinner,
                    Integer.parseInt(settings.getString("grayscale_levels", pattern.grayscaleLevels
                            + "")));
            grayscaleLevelsSpinner.setOnItemSelectedListener(this);

            // For near black measurements
            nearBlackLevelsSpinner = (Spinner) findViewById(R.id.spinner_near_black_levels);
            ArrayAdapter<CharSequence> blackLevelsAdapter = ArrayAdapter.createFromResource(this,
                    R.array.near_black_array, android.R.layout.simple_spinner_item);
            blackLevelsAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            nearBlackLevelsSpinner.setAdapter(blackLevelsAdapter);
            setSpinnerValue(
                    nearBlackLevelsSpinner,
                    Integer.parseInt(settings.getString("near_black_levels",
                            pattern.nearBlackLevels + "")));
            nearBlackLevelsSpinner.setOnItemSelectedListener(this);

            // For near white measurements
            nearWhiteLevelsSpinner = (Spinner) findViewById(R.id.spinner_near_white_levels);
            ArrayAdapter<CharSequence> whiteLevelsAdapter = ArrayAdapter.createFromResource(this,
                    R.array.near_white_array, android.R.layout.simple_spinner_item);
            whiteLevelsAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            nearWhiteLevelsSpinner.setAdapter(whiteLevelsAdapter);
            setSpinnerValue(
                    nearWhiteLevelsSpinner,
                    Integer.parseInt(settings.getString("near_white_levels",
                            pattern.nearWhiteLevels + "")));
            nearWhiteLevelsSpinner.setOnItemSelectedListener(this);

            // For saturation measurements
            saturationLevelsSpinner = (Spinner) findViewById(R.id.spinner_saturation_levels);
            ArrayAdapter<CharSequence> saturationLevelsAdapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.saturations_array, android.R.layout.simple_spinner_item);
            saturationLevelsAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item);
            saturationLevelsSpinner.setAdapter(saturationLevelsAdapter);
            setSpinnerValue(
                    saturationLevelsSpinner,
                    Integer.parseInt(settings.getString("saturations_levels",
                            pattern.saturationLevels + "")));
            saturationLevelsSpinner.setOnItemSelectedListener(this);

            // Buttons
            setGrayscale = (Button) findViewById(R.id.button_grayscale);
            setGrayscale.setOnClickListener(this);
            setNearBlack = (Button) findViewById(R.id.button_near_black);
            setNearBlack.setOnClickListener(this);
            setNearWhite = (Button) findViewById(R.id.button_near_white);
            setNearWhite.setOnClickListener(this);
            setColors = (Button) findViewById(R.id.button_colors);
            setColors.setOnClickListener(this);
            setSaturations = (Button) findViewById(R.id.button_saturations);
            setSaturations.setOnClickListener(this);

        } else {

            patternTypeSpinner = (Spinner) findViewById(R.id.spinner_pattern_type);
            ArrayAdapter<CharSequence> patternTypesAdapter = ArrayAdapter.createFromResource(this,
                    R.array.pattern_types_array, android.R.layout.simple_spinner_item);
            patternTypesAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            patternTypeSpinner.setAdapter(patternTypesAdapter);
            patternTypeSpinner.setOnItemSelectedListener(this);

        }

        // Informs users of the current pattern displayed
        currentPatternInfos = (TextView) findViewById(R.id.current_pattern_info);

        prev = (Button) findViewById(R.id.button_prev);
        prev.setOnClickListener(this);

        next = (Button) findViewById(R.id.button_next);
        next.setOnClickListener(this);

        loadPatternGeneratorConfig();

    }

    @Override
    protected void onResume() {

        loadPatternGeneratorConfig();

        super.onResume();
    }

    private void loadPatternGeneratorConfig() {
        // load pattern generator config from preferences
        pattern.grayscaleLevels = Integer.parseInt(settings.getString("grayscale_levels",
                pattern.grayscaleLevels + ""));
        pattern.nearBlackLevels = Integer.parseInt(settings.getString("near_black_levels",
                pattern.nearBlackLevels + ""));
        pattern.nearWhiteLevels = Integer.parseInt(settings.getString("near_white_levels",
                pattern.nearWhiteLevels + ""));
        pattern.saturationLevels = Integer.parseInt(settings.getString("saturations_levels",
                pattern.saturationLevels + ""));

    }

    @Override
    public void onClick(View v) {
        String tag = v.getTag() + "";

        Log.d("ScreenTestPatterns", "Button pressed: " + tag);

        if (tag.equals("grayscale")) {
            pattern.type = PatternType.GRAYSCALE;
            pattern.step = 0;
            displayPattern();

        } else if (tag.equals("near_black")) {
            pattern.type = PatternType.NEAR_BLACK;
            pattern.step = 0;
            displayPattern();

        } else if (tag.equals("near_white")) {
            pattern.type = PatternType.NEAR_WHITE;
            pattern.step = 0;
            displayPattern();

        } else if (tag.equals("colors")) {
            pattern.type = PatternType.COLORS;
            pattern.step = 0;
            displayPattern();

        } else if (tag.equals("saturations")) {
            pattern.type = PatternType.SATURATIONS;
            pattern.step = 0;
            displayPattern();

        } else if (tag.equals("prev")) {
            pattern.step -= 1;
            displayPattern();

        } else if (tag.equals("next") || tag.equals("pattern_display")) {
            pattern.step += 1;
            displayPattern();
        }
    }

    private void displayPattern() {

        display.getPaint().setColor(pattern.getColor());
        patternView.setBackgroundDrawable(display);
        patternView.invalidate();
        showCurrentPatternInfos();

    }

    private void showCurrentPatternInfos() {
        String text = pattern.type + " ";
        if (pattern.type == PatternType.GRAYSCALE)
            text += "IRE " + (int) ((float) 100 / pattern.grayscaleLevels * pattern.step) + "\n";
        else
            text += "\n";
        text += "R: " + Color.red(pattern.color);
        text += " G: " + Color.green(pattern.color);
        text += " B: " + Color.blue(pattern.color);
        currentPatternInfos.setText(text);
    }

    private void setSpinnerValue(Spinner spinner, int value) {
        int i;
        String item;
        for (i = 0; i < spinner.getAdapter().getCount(); i++) {
            item = (String) spinner.getAdapter().getItem(i);
            try {
                if (Integer.parseInt(item) == value)
                    spinner.setSelection(i);
            } catch (Exception e) {
                // should never happen
                break;
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        try {
            String valueStr = (String) parent.getAdapter().getItem(pos);
            int value = Integer.parseInt(valueStr);
            String tag = parent.getTag() + "";
            Log.d("ScreenTestPatterns", tag + " item: " + valueStr);

            // now save the preference
            SharedPreferences.Editor editor = settings.edit();
            String name = tag + "_levels";

            if (tag.equals("grayscale")) {
                pattern.grayscaleLevels = value;
                pattern.step = 0;
                displayPattern();
                editor.putString(name, valueStr);

            } else if (tag.equals("near_black")) {
                pattern.nearBlackLevels = value;
                pattern.step = 0;
                displayPattern();
                editor.putString(name, valueStr);

            } else if (tag.equals("near_white")) {
                pattern.nearWhiteLevels = value;
                pattern.step = 0;
                displayPattern();
                editor.putString(name, valueStr);

            } else if (tag.equals("saturations")) {
                pattern.saturationLevels = value;
                pattern.step = 0;
                displayPattern();
                editor.putString(name, valueStr);
            }
            editor.commit();

        } catch (Exception e) {

            switch (pos) {
                case 0:
                    pattern.type = PatternType.GRAYSCALE;
                    pattern.step = 0;
                    displayPattern();
                    break;

                case 1:
                    pattern.type = PatternType.COLORS;
                    pattern.step = 0;
                    displayPattern();
                    break;

                case 2:
                    pattern.type = PatternType.SATURATIONS;
                    pattern.step = 0;
                    displayPattern();
                    break;

                case 3:
                    pattern.type = PatternType.NEAR_BLACK;
                    pattern.step = 0;
                    displayPattern();
                    break;

                case 4:
                    pattern.type = PatternType.NEAR_WHITE;
                    pattern.step = 0;
                    displayPattern();
                    break;

                default:
                    break;
            }

            Log.d("ScreenTestPatterns", "Error: Invalid item selection: "
                    + parent.getAdapter().getItem(pos));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isTablet) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.options, menu);
            return true;
        } else
            return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.pattern_options:
                Intent intent = new Intent(this, PatternGeneratorOptions.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
