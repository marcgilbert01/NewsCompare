package marc.newscompare.Service;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import marc.newscompare.api.Article;
import marc.newscompare.api.ArticlesLoader;
import marc.newscompare.dao.NewsDb;
import yahooapi.contentAnalysis.YahooContentAnalysisApi;

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

            // FOR EACH NEWSPAPER
            for (Article.NewsPaper newsPaper : Article.NewsPaper.values()) {

                // GET EXISTING ARTICLES FROM DB
                List<Article> existingArticles = newsDb.getArticles( 0L , newsPaper , false);
                // GET NEW ARTICLES FROM RSS FEED
                ArticlesLoader articlesLoader = newsPaper.getArticlesLoader();
                List<Article> newArticles = articlesLoader.getNewArticles( existingArticles );
                // SAVE NEW ARTICLES TO DB
                newsDb.saveArticles( newArticles );
            }


            // GET KEYWORDS FROM ARTICLES
            List<Article> articlesWithNoKeywords = newsDb.getArticlesWithNoKeywords();
            YahooContentAnalysisApi yahooContentAnalysis = new YahooContentAnalysisApi();
            for(Article article : articlesWithNoKeywords  ){

                // GET KEYWORDS FROM YAHOO API
                String[] keywords = yahooContentAnalysis.getKeywords( article.getDescription() );
                // IF NO KEYWORDS CREATE EMPTY ONE
                if( keywords==null ) {
                    keywords = new String[]{""};
                }
                List<String> keywordList = new ArrayList<String>(Arrays.asList(keywords));
                article.setKeywords(keywordList);
                // SAVE KEYWORDS ( NOT WORKING WHEN TOO MANY SELECT )
                List<Article> articlesWithKeywords = new ArrayList<>();
                articlesWithKeywords.add(article);
                newsDb.saveKeywords(articlesWithKeywords);
                // DELETE OLDER ARTICLES (OLDER THAN ONE WEEK)
                Long now = System.currentTimeMillis();
                Long aWeekAgo = now - ( 7 * 24 * 3600 * 1000 );
                newsDb.deleteArticles( aWeekAgo );
            }

            // SLEEP FOR 1 HOUR
            try {
                sleep( 60*3600*1000 );
                //sleep( 10000 );

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    
    public void stopRecorder(){

        exit = true;
        this.interrupt();
    }

}
