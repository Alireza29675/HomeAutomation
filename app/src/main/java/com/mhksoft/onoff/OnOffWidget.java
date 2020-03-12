package com.mhksoft.onoff;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link OnOffWidgetConfigureActivity OnOffWidgetConfigureActivity}
 */
public class OnOffWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.on_off_widget);
        views.setInt(R.id.one_tv, "setBackgroundResource", OnOffWidgetConfigureActivity.loadButtonState(context, appWidgetId, "one"));
        views.setInt(R.id.two_tv, "setBackgroundResource", OnOffWidgetConfigureActivity.loadButtonState(context, appWidgetId, "two"));
        views.setInt(R.id.three_tv, "setBackgroundResource", OnOffWidgetConfigureActivity.loadButtonState(context, appWidgetId, "three"));
        views.setInt(R.id.four_tv, "setBackgroundResource", OnOffWidgetConfigureActivity.loadButtonState(context, appWidgetId, "four"));

        views.setOnClickPendingIntent(R.id.one_tv, getPendingSelfIntent(context, appWidgetId, "one", 1));
        views.setOnClickPendingIntent(R.id.two_tv, getPendingSelfIntent(context, appWidgetId, "two", 2));
        views.setOnClickPendingIntent(R.id.three_tv, getPendingSelfIntent(context, appWidgetId, "three", 3));
        views.setOnClickPendingIntent(R.id.four_tv, getPendingSelfIntent(context, appWidgetId, "four", 4));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static PendingIntent getPendingSelfIntent(Context context, int appWidgetId, String tag, int index) {
        Intent intent = new Intent(context, OnOffWidget.class);
        intent.setAction("OnTextViewClick");
        intent.putExtra("AppWidgetId", appWidgetId);
        intent.putExtra("Tag", tag);
        return PendingIntent.getBroadcast(context, appWidgetId + index, intent, 0);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            OnOffWidgetConfigureActivity.deleteURLPref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals("OnTextViewClick")) {
            final int appWidgetId = intent.getIntExtra("AppWidgetId", 0);
            final String tag = intent.getStringExtra("Tag");
            final String baseURL = OnOffWidgetConfigureActivity.loadURLPref(context, appWidgetId);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            retrofit.create(ApiService.class).toggle(tag).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful())
                        if (response.code() == 200) {
                            Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
                            OnOffWidgetConfigureActivity.toggleButtonState(context, appWidgetId, tag);
                            updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
                        }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

