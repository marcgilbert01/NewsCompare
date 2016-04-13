package marc.newscompare.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;
import java.util.List;

import marc.newscompare.api.Article;
import marc.newscompare.api.ArticlesLoader;
import marc.newscompare.dao.NewsDb;

public class NewsRecorderService extends Service {

    static final String DB_SUB_DIR  = "/database";
    static final String IMG_ARTICLES_SUB_DIR = "/articlesImages";

    NewsRecorderThread newsRecorderThread;
    NewsRecorderBinder newsRecorderBinder;
    NewsDb newsDbForThread;
    NewsDb newsDbForBinder;

    public NewsRecorderService() {
    }


    @Override
    public IBinder onBind(Intent intent) {

        return  newsRecorderBinder;
    }



    @Override
    public void onCreate() {
        super.onCreate();

        //newsDbForThread = new NewsDb( getApplicationContext() , new File( getApplicationContext().getCacheDir()+NewsRecorderService.DB_SUB_DIR ) );
        newsDbForThread = new NewsDb( getApplicationContext() , new File( Environment.getExternalStorageDirectory()+NewsRecorderService.DB_SUB_DIR ) );

        //newsDbForBinder = new NewsDb( getApplicationContext() , new File( getApplicationContext().getCacheDir()+NewsRecorderService.DB_SUB_DIR ) );
        newsDbForBinder = new NewsDb( getApplicationContext() , new File( Environment.getExternalStorageDirectory()+NewsRecorderService.DB_SUB_DIR ) );

        ArticlesLoader.setImageDirectory( new File(getApplicationContext().getCacheDir() + NewsRecorderService.IMG_ARTICLES_SUB_DIR));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if( newsRecorderThread==null || newsRecorderThread.getState()== Thread.State.TERMINATED ){

            NewsRecorderThread newsRecorderThread = new NewsRecorderThread( getApplicationContext() , newsDbForThread );
            newsRecorderThread.setPriority(Thread.MIN_PRIORITY);
            newsRecorderThread.start();
            newsRecorderBinder = new NewsRecorderBinder( getApplicationContext() , newsDbForBinder , newsRecorderThread );
        }
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        newsRecorderThread.stopRecorder();

    }












}
