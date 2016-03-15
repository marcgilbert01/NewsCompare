package marc.newscompare.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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

        if( newsRecorderThread==null || newsRecorderThread.getState()== Thread.State.TERMINATED ){

            NewsRecorderThread newsRecorderThread = new NewsRecorderThread(getApplicationContext());
            newsRecorderThread.start();

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        newsRecorderThread.stopRecorder();

    }





}
