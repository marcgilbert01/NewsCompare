package marc.newscompare.Service;

import android.content.Context;

import java.util.List;

import marc.newscompare.api.Article;
import marc.newscompare.api.ArticlesLoader;
import marc.newscompare.dao.NewsDb;

/**
 * Created by gilbertm on 15/03/2016.
 */
public class NewsRecorderThread extends Thread{

    private Context context;
    private Boolean exit = false;

    public NewsRecorderThread(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        super.run();

        while(exit==false) {

            NewsDb newsDb = new NewsDb(context);

            // FOR EACH
            for (Article.NewsPaper newsPaper : Article.NewsPaper.values()) {

                // GET EXISTING ARTICLE FROM DB
                List<Article> existingArticles = newsDb.getArticles( 0L , newsPaper );
                // GET NEW ARTICLES
                ArticlesLoader articlesLoader = newsPaper.getArticlesLoader();
                List<Article> newArticles = articlesLoader.getNewArticles( existingArticles );
                // SAVE NEW ARTICLES TO DB
                newsDb.saveArticles( newArticles );

            }

        }

    }

    
    public void stopRecorder(){

        exit = true;
        this.interrupt();
    }

}
