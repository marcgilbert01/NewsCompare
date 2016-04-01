package marc.newscompare.Service;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;

import java.net.BindException;
import java.util.ArrayList;
import java.util.List;

import marc.newscompare.api.Article;
import marc.newscompare.dao.NewsDb;

/**
 * Created by gilbertm on 23/03/2016.
 */
public class NewsRecorderBinder extends Binder {

    Context context;
    NewsDb newsDb;

    public NewsRecorderBinder(Context context) {
        this.context = context;
        newsDb = new NewsDb(context,context.getCacheDir());
    }

    public void loadArticlesWithMatchingArticles(Article.NewsPaper mainNewsPaper, final OnArticlesReadyListener onArticlesReadyListener){


        new Thread(){
            @Override
            public void run() {
                super.run();
                List<Article> articlesWithMatchingArticles = new ArrayList<>();
                articlesWithMatchingArticles = newsDb.getArticlesWithMatchingArticles(0L, null);
                onArticlesReadyListener.OnArticlesReady(articlesWithMatchingArticles);
            }
        }.start();

    }



    interface OnArticlesReadyListener {

        public void OnArticlesReady(List<Article> articles);

    }










}
