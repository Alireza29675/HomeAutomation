package com.mhksoft.onoff;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

/**
 * The configuration screen for the {@link OnOffWidget OnOffWidget} AppWidget.
 */
public class OnOffWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "com.mhksoft.onoff.OnOffWidget";
    private static final String PREF_PREFIX_KEY = "onoffwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    EditText connectionURLEt;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = OnOffWidgetConfigureActivity.this;
            // When the button is clicked, store the string locally
            String widgetText = connectionURLEt.getText().toString();
            saveURLPref(context, mAppWidgetId, widgetText);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            OnOffWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public OnOffWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveURLPref(Context context, int appWidgetId, String text) {
        Log.e("SaveID", "" + appWidgetId);
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadURLPref(Context context, int appWidgetId) {
        Log.e("LoadID", "" + appWidgetId);
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
    }

    static void deleteURLPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    static int loadButtonState(Context context, int appWidgetId, String tag) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        boolean state = prefs.getBoolean(PREF_PREFIX_KEY + appWidgetId + tag, false);
        if (state)
            return R.drawable.on_background;
        return R.drawable.off_background;
    }

    static void toggleButtonState(Context context, int appWidgetId, String tag) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        boolean state = prefs.getBoolean(PREF_PREFIX_KEY + appWidgetId + tag, false);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_PREFIX_KEY + appWidgetId + tag, !state);
        editor.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setResult(RESULT_CANCELED);

        setContentView(R.layout.on_off_widget_configure);
        connectionURLEt = findViewById(R.id.connectionURL_et);
        findViewById(R.id.save_btn).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        connectionURLEt.setText(loadURLPref(OnOffWidgetConfigureActivity.this, mAppWidgetId));
    }
}

