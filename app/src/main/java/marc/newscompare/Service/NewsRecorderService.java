package marc.newscompare.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.List;

import marc.newscompare.api.Article;

public class NewsRecorderService extends Service {


    NewsRecorderThread newsRecorderThread;

    public NewsRecorderService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if( newsRecorderThread==null || newsRecorderThread.getState()== Thread.State.TERMINATED ){

            NewsRecorderThread newsRecorderThread = new NewsRecorderThread(getApplicationContext());
            newsRecorderThread.start();

        }
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        newsRecorderThread.stopRecorder();

    }












}
