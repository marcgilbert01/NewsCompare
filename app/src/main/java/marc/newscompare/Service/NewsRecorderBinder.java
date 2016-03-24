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

    public List<Article> getArticlesWithMatch(Article.NewsPaper mainNewsPaper){

        List<Article> articlesWithMatch = new ArrayList<>();

        List<Article> mainNewsPaperArticles = newsDb.getArticles(0L, mainNewsPaper, true);
        if( mainNewsPaperArticles!=null && mainNewsPaperArticles.size()>0 ){

        }

        return articlesWithMatch;
    }










}
