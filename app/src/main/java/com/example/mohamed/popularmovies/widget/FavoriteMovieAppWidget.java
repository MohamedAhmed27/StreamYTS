package com.example.mohamed.popularmovies.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.example.mohamed.popularmovies.R;
import com.example.mohamed.popularmovies.model.movie;

import java.util.ArrayList;

/**
 * Implementation of App Widget functionality.
 */
public class FavoriteMovieAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, ArrayList<String>favMovies) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.favorite_movie_app_widget);

        String movies="";
        for(int i=0;i<favMovies.size();i++)
        {
            movies+=favMovies.get(i)+"\n";
        }

        views.setTextViewText(R.id.favorites_movies_widget_text, movies);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

    }
    public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                    int[] appWidgetIds, ArrayList<String>favMovies) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId,favMovies);
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
}

