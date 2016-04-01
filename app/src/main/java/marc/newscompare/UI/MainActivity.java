package marc.newscompare.UI;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import marc.marcviews.OpenRightRecyclerView;
import marc.marcviews.OpenRightView;
import marc.newscompare.R;
import marc.newscompare.Service.NewsRecorderBinder;
import marc.newscompare.Service.NewsRecorderService;
import marc.newscompare.api.Article;

public class MainActivity extends AppCompatActivity implements NewsRecorderBinder.OnArticlesReadyListener{


    OpenRightView openRightViewArticles;
    ArticlesOpenRightViewAdapter articlesOpenRightViewAdapter;
    NewsRecorderBinder newsRecorderBinder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // GET OPEN RIGHT VIEW (RECYCLER VIEW)
        openRightViewArticles = (OpenRightView) findViewById(R.id.openRightViewArticles);

        // START SERVICE
        Intent intent = new Intent(this, NewsRecorderService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);



    }



    @Override
    protected void onStart() {
        super.onStart();

        // CONNECT TO SERVICE TO GET ARTICLES
        Intent intent = new Intent(this, NewsRecorderService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            // GET BINDER
            newsRecorderBinder = (NewsRecorderBinder) service;
            newsRecorderBinder.loadArticlesWithMatchingArticles( null , MainActivity.this );

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    @Override
    public void OnArticlesReady(List<Article> articles) {

        articlesOpenRightViewAdapter = new ArticlesOpenRightViewAdapter(articles);
        openRightViewArticles.setAdapter(articlesOpenRightViewAdapter);

    }



}
