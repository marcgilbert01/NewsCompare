package marc.newscompare.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.List;

import marc.newscompare.api.Article;

public class NewsRecorderService extends Service {

    static final String DB_SUB_DIR  = "/database";
    static final String IMG_ARTICLES_SUB_DIR = "/articlesImages";

    NewsRecorderThread newsRecorderThread;
    NewsRecorderBinder newsRecorderBinder;


    public NewsRecorderService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        return  newsRecorderBinder;
    }



    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if( newsRecorderThread==null || newsRecorderThread.getState()== Thread.State.TERMINATED ){

            NewsRecorderThread newsRecorderThread = new NewsRecorderThread(getApplicationContext());
            newsRecorderThread.setPriority(Thread.MIN_PRIORITY);
            newsRecorderThread.start();
            newsRecorderBinder = new NewsRecorderBinder(getApplicationContext());
        }
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        newsRecorderThread.stopRecorder();

    }












}
