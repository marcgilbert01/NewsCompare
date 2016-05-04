package marc.newscompare.Service;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import marc.newscompare.api.Article;
import marc.newscompare.api.ArticlesLoader;
import marc.newscompare.api.KeywordsExtractor;
import marc.newscompare.dao.NewsDb;
import yahooapi.contentAnalysis.YahooContentAnalysisApi;

/**
 * Created by gilbertm on 15/03/2016.
 */
public class NewsRecorderThread extends Thread{


    private Context context;
    private Boolean exit = false;
    private NewsDb newsDb;
    private File imageDirectory;

    private NewsRecorderThreadListener newsRecorderThreadListener;
    private Status status = Status.STOPPED;
    public enum Status{

        STOPPED,
        LOADING_ARTICLES,
        EXTRACTING_KEYWORDS,
        COMPARING_ARTICLES,
        SLEEPING;
    }


    public NewsRecorderThread(Context context, NewsDb newsDb , File imageDirectory) {
        this.context = context;
        this.newsDb = newsDb;
        this.imageDirectory = imageDirectory;
    }

    @Override
    public void run() {
        super.run();

        while(exit==false) {

            // FOR EACH NEWSPAPER
            for (Article.NewsPaper newsPaper : Article.NewsPaper.values()) {

                // GET EXISTING ARTICLES FROM DB
                List<Article> existingArticles = newsDb.getArticles( 0L , newsPaper , false);
                // GET NEW ARTICLES FROM RSS FEED
                ArticlesLoader articlesLoader = ArticlesLoader.newInstance( newsPaper , imageDirectory );
                List<Article> newArticles = articlesLoader.getNewArticles( existingArticles );
                // SAVE NEW ARTICLES TO DB
                newsDb.saveArticles( newArticles );
            }

            // GET KEYWORDS FROM ARTICLES
            List<Article> articlesWithNoKeywords = newsDb.getArticlesWithNoKeywords();
            if( articlesWithNoKeywords!=null && articlesWithNoKeywords.size()>0 ) {

                articlesWithNoKeywords = new KeywordsExtractor().extractKeywords(articlesWithNoKeywords);
                newsDb.saveKeywords(articlesWithNoKeywords);
            }

            // COMPARE ALL ARTICLES AND SAVE MATCHING ARTICLES
            List<Article> allArticles = newsDb.getArticles( 0L , null , true );
            for(Article article : allArticles  ){

                if( article.getKeywords()!=null &&
                    article.getKeywords().size()>0 &&
                    !article.getKeywords().get(0).equals("null")
                    ) {

                    // List<Article> matchingArticles = newsDb.getMatchingArticles( article , 2 );
                    List<Article> matchingArticles = new ArrayList<>();
                    for (Article articleToCheck : allArticles) {

                        if (  articleToCheck != article &&
                              articleToCheck.getKeywords() != null &&
                              articleToCheck.getKeywords().size() > 0 &&
                              articleToCheck.getKeywords().get(0).equals(article.getKeywords().get(0))) {
                            // MATCH
                            matchingArticles.add(articleToCheck);
                        }
                    }
                    // IF MATCHING ARTICLES FOUND SAVE
                    if (matchingArticles != null && matchingArticles.size() > 0) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int a = 0; a < matchingArticles.size() - 1; a++) {
                            stringBuilder.append(matchingArticles.get(a).getId() + ",");
                        }
                        stringBuilder.append(matchingArticles.get(matchingArticles.size() - 1).getId());
                        article.setMatchingArticlesIds(stringBuilder.toString());
                        // SAVE TO DB
                        newsDb.updateMatchingArticles(article);
                    }
                }
            }

            // DELETE OLDER ARTICLES (OLDER THAN ONE WEEK)
            ArticlesLoader articlesLoader = ArticlesLoader.newInstance( Article.NewsPaper.THE_GUARDIAN , imageDirectory );
            Long now = System.currentTimeMillis();
            Long aWeekAgo = now - (7 * 24 * 3600 * 1000);
            newsDb.deleteArticles(aWeekAgo);
            articlesLoader.deletesImages(aWeekAgo);

            // LOAD IMAGES
            List<Article> articlesWithoutImageFileName = newsDb.getArticlesWithoutImageFileName();
            for(Article articleWithoutImage : articlesWithoutImageFileName ){
                articleWithoutImage = articlesLoader.saveArticleImages(articleWithoutImage);
                newsDb.updateImagesFileNames(articleWithoutImage);
            }

            // SLEEP FOR 1 HOUR
            try {
                sleep( 3600000 );
                //sleep( 30000 );
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
