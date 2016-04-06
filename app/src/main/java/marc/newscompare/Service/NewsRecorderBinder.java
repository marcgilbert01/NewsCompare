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
    NewsRecorderThread newsRecorderThread;

    public NewsRecorderBinder(Context context, NewsDb newsDb, NewsRecorderThread newsRecorderThread) {
        this.context = context;
        this.newsDb = newsDb;
        this.newsRecorderThread = newsRecorderThread;
    }

    public void loadArticlesWithMatchingArticles(Article.NewsPaper mainNewsPaper, final OnArticlesReadyListener onArticlesReadyListener){

        new Thread(){
            @Override
            public void run() {
                super.run();
                List<Article> articlesWithMatchingArticles = new ArrayList<>();
                articlesWithMatchingArticles = newsDb.getArticlesWithMatchingArticles(0L, null);
                articlesWithMatchingArticles = fillArticleWithMatchingArticles(articlesWithMatchingArticles);
                onArticlesReadyListener.OnArticlesReady(articlesWithMatchingArticles);




// TEST
/*
System.out.println( "###### nb Articles with matching articles from method =" + articlesWithMatchingArticles.size() );
List<Article> tmpArticles = newsDb.getArticles(0L,null,false);
List<Article> tmpArticlesWithMatch = new ArrayList<Article>();
for( Article article : tmpArticles ){

    if( article.getMatchingArticlesIds()!=null && article.getMatchingArticlesIds().length()>0 ){
        tmpArticlesWithMatch.add(article);
    }
}
System.out.println( "###### total nb Articles ="+tmpArticles.size()  );
System.out.println( "###### nb Articles with matching articles from loop =" + tmpArticlesWithMatch.size() );
*/


            }
        }.start();

    }

    public interface OnArticlesReadyListener {

        public void OnArticlesReady(List<Article> articles);
    }

    public NewsRecorderThread getNewsRecorderThread() {
        return newsRecorderThread;
    }

    public void setNewsRecorderThread(NewsRecorderThread newsRecorderThread) {
        this.newsRecorderThread = newsRecorderThread;
    }



    public List<Article> fillArticleWithMatchingArticles(List<Article> articles){

        int a = 0;
        while( a<articles.size() ){

            Article article = articles.get(a);
            if( article.getMatchingArticlesIdsAsIntegers()!=null && article.getMatchingArticlesIdsAsIntegers().length>0 ){

                List<Article> matchingArticles = new ArrayList<>();
                // LOOK FOR MATCHING ARTICLES IN THE LIST
                for(Integer articleId : article.getMatchingArticlesIdsAsIntegers() ){

                   for(Article articleToCheck : articles ){

                       if( articleId==articleToCheck.getId() ){
                           matchingArticles.add( articleToCheck );
                       }
                   }
                }
                article.setMatchingArticles(matchingArticles);
                articles.removeAll( matchingArticles );
            }

            a++;
        }


        return  articles;
    }





}
