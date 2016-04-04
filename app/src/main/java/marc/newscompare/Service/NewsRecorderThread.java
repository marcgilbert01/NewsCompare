package marc.newscompare.Service;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import marc.newscompare.R;
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
    private NewsDb newsDb;

    private NewsRecorderThreadListener newsRecorderThreadListener;
    private Status status = Status.STOPPED;
    public enum Status{

        STOPPED,
        LOADING_ARTICLES,
        EXTRACTING_KEYWORDS,
        COMPARING_ARTICLES,
        SLEEPING;
    }


    public NewsRecorderThread(Context context, NewsDb newsDb) {
        this.context = context;
        this.newsDb = newsDb;
    }

    @Override
    public void run() {
        super.run();

        while(exit==false) {

            // FOR EACH NEWSPAPER
            if( newsRecorderThreadListener!=null ){
                status = Status.LOADING_ARTICLES;
                newsRecorderThreadListener.onStatusChange( status );
            }
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
            if( newsRecorderThreadListener!=null ){
                status = Status.EXTRACTING_KEYWORDS;
                newsRecorderThreadListener.onStatusChange( status );
            }
            List<Article> articlesWithNoKeywords = newsDb.getArticlesWithNoKeywords();
            if( articlesWithNoKeywords!=null && articlesWithNoKeywords.size()>0 ) {

                YahooContentAnalysisApi yahooContentAnalysis = new YahooContentAnalysisApi();
                for (Article article : articlesWithNoKeywords) {
                    // GET KEYWORDS FROM YAHOO API
                    String[] keywords = yahooContentAnalysis.getKeywords(article.getDescription());
                    // IF NO KEYWORDS CREATE EMPTY ONE
                    if (keywords == null) {
                        keywords = new String[]{""};
                    }
                    List<String> keywordList = new ArrayList<String>(Arrays.asList(keywords));
                    article.setKeywords(keywordList);
                    // SAVE KEYWORDS ( NOT WORKING WHEN TOO MANY SELECT )
                    List<Article> articlesWithKeywords = new ArrayList<>();
                    articlesWithKeywords.add(article);
                    newsDb.saveKeywords(articlesWithKeywords);
                }
            }

            // COMPARE ALL ARTICLES AND SAVE MATCHING ARTICLES
            if( newsRecorderThreadListener!=null ){
                status = Status.COMPARING_ARTICLES;
                newsRecorderThreadListener.onStatusChange( status );
            }
            List<Article> allArticles = newsDb.getArticles(0L,null,true);
            for(Article article : allArticles  ){

                List<Article> matchingArticles = newsDb.getMatchingArticles(article,2);
                // IF MATCHING ARTICLES FOUND SAVE
                if( matchingArticles!=null && matchingArticles.size()>0 ) {

                    StringBuilder stringBuilder = new StringBuilder();
                    for ( int a=0 ; a<matchingArticles.size()-1 ; a++ ) {
                        stringBuilder.append( matchingArticles.get(a).getId()+"," );
                    }
                    stringBuilder.append( matchingArticles.get(matchingArticles.size()-1).getId() );
                    article.setMatchingArticlesIds(stringBuilder.toString());
                    // SAVE TO DB
                    newsDb.updateMatchingArticles(article);
                }
            }

            // DELETE OLDER ARTICLES (OLDER THAN ONE WEEK)
            Long now = System.currentTimeMillis();
            Long aWeekAgo = now - (7 * 24 * 3600 * 1000);
            newsDb.deleteArticles(aWeekAgo);
            ArticlesLoader.deletesImages(aWeekAgo);

            // SLEEP FOR 1 HOUR
            if( newsRecorderThreadListener!=null ){
                status = Status.SLEEPING;
                newsRecorderThreadListener.onStatusChange( status );
            }
            try {
                sleep( 3600*1000 );
                //sleep( 10000 );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if( newsRecorderThreadListener!=null ){
            status = Status.STOPPED;
            newsRecorderThreadListener.onStatusChange( status );
        }

    }

    
    public void stopRecorder(){

        exit = true;
        this.interrupt();
    }


    public interface NewsRecorderThreadListener{

        public void onStatusChange(Status status);

    }

    public void setOnNewsRecorderThreadListener(NewsRecorderThreadListener newsRecorderThreadListener){

        this.newsRecorderThreadListener = newsRecorderThreadListener;
    }



}
