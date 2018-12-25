package com.example.mohamed.popularmovies.Activities;

import android.app.ActivityOptions;

import android.appwidget.AppWidgetManager;
import android.content.AsyncTaskLoader;
import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mohamed.popularmovies.Adapters.MovieAdapter;
import com.example.mohamed.popularmovies.R;
import com.example.mohamed.popularmovies.data.MovieContract;
import com.example.mohamed.popularmovies.data.MovieDbHelper;
import com.example.mohamed.popularmovies.model.movie;
import com.example.mohamed.popularmovies.utilities.NetworkUtils;
import com.example.mohamed.popularmovies.utilities.jsonUtil;
import com.example.mohamed.popularmovies.widget.FavoriteMovieAppWidget;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import com.yqritc.recyclerviewflexibledivider.VerticalDividerItemDecoration;

import java.net.URL;
import java.util.ArrayList;

import static com.example.mohamed.popularmovies.data.MovieContract.*;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler,LoaderManager.LoaderCallbacks<Cursor>,NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView mRecyclerView;
    private TextView mErrorMessageDisplay;
    public static final String SORT_BY_POPULATIRY="date_added";
    public static final String SORT_BY_HIGHT_RATE="rating";
    public static final String SORT_BY_FAVORITES="favorites";
    public static final String ADAPTER_POS_TAG="clickedpos";
    public static final String ARRAY_TAG_TO_DEATIL_ACTIVITY="array_List_of_movies";
    public  static ArrayList<movie> movies =new ArrayList<>();
    private ProgressBar mLoadingIndicator;
    private MovieAdapter mMovietAdapter;
    public String jsonMoveResult;
    public static String sortBy=SORT_BY_HIGHT_RATE;
    public static final String SORT_BY_KEY="sortbykey";
    public static final String SAVE_SCROLL_KEY="scroll";
    private SQLiteDatabase mDb;
    public static GridLayoutManager layoutManager;
    private static final int LOADER_ID = 5;
    private Parcelable state;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToogle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mDrawerLayout=findViewById(R.id.drawer);
        mToogle=new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close);
        mDrawerLayout.addDrawerListener(mToogle);
        navigationView=(NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        mToogle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
        bar.setDisplayShowHomeEnabled(true);

        bar.setLogo(R.drawable.ytss);
        bar.setDisplayUseLogoEnabled(true);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        if(this.getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT) {

            layoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        }
        else
        {

            layoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        }



        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(false);
        mMovietAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovietAdapter);
        this.mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this)
                .color(Color.DKGRAY)
                .build());
        this.mRecyclerView.addItemDecoration(new VerticalDividerItemDecoration.Builder(this)
                .color(Color.DKGRAY)
                .build());
        MovieDbHelper dbHelper = new MovieDbHelper(this);
        mDb = dbHelper.getWritableDatabase();

        if(savedInstanceState!=null)
        {

            mRecyclerView.getLayoutManager().onRestoreInstanceState(state);
            sortBy=savedInstanceState.getString(SORT_BY_KEY);

            if(sortBy==SORT_BY_POPULATIRY)
            {
                setTitle(R.string.most_popularM);
                loadMovieData(sortBy);
            }
            else if(sortBy==SORT_BY_HIGHT_RATE)
            {

                setTitle(R.string.Top_ratedM);
                loadMovieData(sortBy);
            }
            else if(sortBy==SORT_BY_FAVORITES)
            {
                setTitle(R.string.favoritesM);

                getSupportLoaderManager().initLoader(LOADER_ID,null,this).forceLoad();
                showMovieDataView();


            }

        }
        else
        {
            loadMovieData(SORT_BY_HIGHT_RATE);

        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, FavoriteMovieAppWidget.class));
        ArrayList<String>favMovies=new ArrayList<>();
        favMovies=getAllFav();
        FavoriteMovieAppWidget.updateWidget(this,appWidgetManager,appWidgetIds,favMovies);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState!=null)
        {
            state=savedInstanceState.getParcelable(SAVE_SCROLL_KEY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId())
        {
            case R.id.top_rated:
                sortBy=SORT_BY_HIGHT_RATE;
                setTitle(R.string.Top_ratedM);
                loadMovieData(SORT_BY_HIGHT_RATE);
                break;
            case R.id.most_popular:
                sortBy=SORT_BY_POPULATIRY;
                setTitle(R.string.most_popularM);
                loadMovieData(SORT_BY_POPULATIRY);
                break;

            case R.id.favorites:
                sortBy=SORT_BY_FAVORITES;
                setTitle(R.string.favoritesM);
                getSupportLoaderManager().initLoader(LOADER_ID,null,MainActivity.this).forceLoad();
                showMovieDataView();
                break;



        }

        if(mToogle.onOptionsItemSelected(item))
        {
            return true;
        }
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();


        if(sortBy==SORT_BY_FAVORITES) {



            getSupportLoaderManager().initLoader(LOADER_ID,null,this).forceLoad();
            showMovieDataView();



        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        MenuItem item=menu.findItem(R.id.menuSearch);
        SearchView searchView=(SearchView)item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                LoadMovieSearchData(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                return false;
            }
        });
        return true;
    }


    private void LoadMovieSearchData(String movieName)
    {
        showMovieDataView();
        new FetchMovie().execute(movieName);
    }
    private void loadMovieData(String sortby) {
        showMovieDataView();

        new FetchMovie().execute(sortby);
    }
    private void showMovieDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }


    private void showErrorMessage() {

        mRecyclerView.setVisibility(View.INVISIBLE);

        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(int adapterPosition) {


        Intent i =new Intent(getApplicationContext(),DetailsActivity.class);
        i.putExtra(ADAPTER_POS_TAG,adapterPosition);

        i.putExtra(ARRAY_TAG_TO_DEATIL_ACTIVITY, movies);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
            startActivity(i, bundle);
        } else {
            startActivity(i);
        }

    }


    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {

        Uri uri= MovieEntry.CONTENT_URI;
        CursorLoader cursorLoader=new CursorLoader(this,uri,null,null,null,null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        movies.clear();
        movie movie;

        ArrayList<String> torrents = new ArrayList<>();
        try {

            while (cursor.moveToNext()) {

                torrents = new ArrayList<>();
                long movieId = cursor.getLong(cursor.getColumnIndex(MovieEntry.Movie_Id));
                String MovieName = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_Name));
                String movieImage = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_Image));
                String MovieOverview = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_Overview));
                String MovieVoteAvarage = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_VoteAvarage));
                String MovieReleaseDate = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_ReleaseDate));
                String MovieTrailer = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_Trailer_ID));
                String MovieBackGroundImage = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_BackGround_Image));
                String Movie720Plink = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_720P_torrent_link));
                String Movie1080Plink = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_1080P_torrent_link));
                if (Movie720Plink != null) {
                    torrents.add(Movie720Plink);
                }
                if (Movie1080Plink != null) {
                    torrents.add(Movie1080Plink);
                }
                movie = new movie(movieId, MovieName, movieImage, MovieBackGroundImage, MovieOverview, MovieVoteAvarage, MovieReleaseDate, torrents, MovieTrailer);
                movies.add(movie);


            }
            mMovietAdapter.setMovieData(movies);
        } finally {
            cursor.close();

        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

       /* if(item.getItemId()==R.id.movieGenre_mystrey)
        {
            Toast.makeText(this,"yaw",Toast.LENGTH_SHORT).show();
        }
*/
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    public  class FetchMovie extends AsyncTask<String, Void, ArrayList<movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<movie> doInBackground(String... params) {


            if (params.length == 0) {
                return null;
            }

            String sortBy = params[0];
            URL MovieUrl=null;
            if(sortBy.equals(SORT_BY_HIGHT_RATE)||sortBy.equals(SORT_BY_POPULATIRY)) {
                MovieUrl = NetworkUtils.buildUrl(sortBy);
            }
            else
            {
                MovieUrl = NetworkUtils.buildUrlForSearch(sortBy);
            }


            try {
                 jsonMoveResult = NetworkUtils
                        .getResponseFromHttpUrl(MovieUrl);

                 movies = jsonUtil.ParseMoviewithJson(jsonMoveResult);


                 return movies;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<movie> movies) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movies != null) {
                showMovieDataView();
                mMovietAdapter.setMovieData(movies);
                if(state!=null)
                {
                    layoutManager.onRestoreInstanceState(state);
                }
            } else {
                showErrorMessage();
            }
        }
    }
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String sort=sortBy;
        outState.putParcelable(SAVE_SCROLL_KEY,layoutManager.onSaveInstanceState());
        outState.putString(SORT_BY_KEY, sort);

    }
  private ArrayList<movie> getAllMovies() {
        movies.clear();
        movie movie;
        Uri uri=MovieEntry.CONTENT_URI;
        Cursor cursor=getContentResolver().query(uri,null,null,null,null);
      ArrayList<String> torrents = new ArrayList<>();
        try {

 while (cursor.moveToNext()) {

            torrents = new ArrayList<>();
            long movieId = cursor.getLong(cursor.getColumnIndex(MovieEntry.Movie_Id));
            String MovieName = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_Name));
            String movieImage = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_Image));
            String MovieOverview = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_Overview));
            String MovieVoteAvarage = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_VoteAvarage));
            String MovieReleaseDate = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_ReleaseDate));
            String MovieTrailer = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_Trailer_ID));
            String MovieBackGroundImage = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_BackGround_Image));
            String Movie720Plink = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_720P_torrent_link));
            String Movie1080Plink = cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_1080P_torrent_link));
            if (Movie720Plink != null) {
                torrents.add(Movie720Plink);
            }
            if (Movie1080Plink != null) {
                torrents.add(Movie1080Plink);
            }
            movie = new movie(movieId, MovieName, movieImage, MovieBackGroundImage, MovieOverview, MovieVoteAvarage, MovieReleaseDate, torrents, MovieTrailer);
            movies.add(movie);

            }
        } finally {
            cursor.close();

        }
            return movies;
    }


    private ArrayList<String> getAllFav() {

        ArrayList<String> favMovies=new ArrayList<>();

        Uri uri=MovieEntry.CONTENT_URI;
        Cursor cursor=getContentResolver().query(uri,null,null,null,null);

        try {
            while (cursor.moveToNext()) {


                String MovieName=cursor.getString(cursor.getColumnIndex(MovieEntry.Movie_Name));
               favMovies.add(MovieName);


            }
        } finally {
            cursor.close();

        }
        return favMovies;
    }



}
