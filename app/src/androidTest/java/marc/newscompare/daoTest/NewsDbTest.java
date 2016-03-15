package marc.newscompare.daoTest;

import android.test.AndroidTestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;

import marc.newscompare.NewsApiTest.ArticleTest;
import marc.newscompare.api.Article;
import marc.newscompare.dao.NewsDb;

/**
 * Created by gilbertm on 14/03/2016.
 */
public class NewsDbTest extends AndroidTestCase{


    public void testSaveArticles(){

        // DELETE DATABASE
        File dbFile = new File(NewsDb.DATA_DIRECTORY+"/news.db");
        if( dbFile.exists() ) {
            dbFile.delete();
        }

        // PREPARE DUMMY ARTICLES
        List<Article> articles = new ArrayList<Article>();
        int nbArticlesToAdd = 5;
        Long now = System.currentTimeMillis();
        for(int a=0 ; a<nbArticlesToAdd ; a++  ){

            Article article = new Article();
            article.setTitle(" tilte " + a);
            article.setDescription("description " + a);
            article.setText("text " + a);
            article.setAuthor("author " + a);
            article.setImagesFileName("file" + a + ".jpg,file2_" + a + ".jpg");
            article.setNewsPaper(Article.NewsPaper.THE_TELEGRAPHE );
            article.setDate( now );

            //article.setBitmaps( ArticleTest.createDummyBitmaps() );
            //article.setKeywords(new String[]{"first keyword", "second keyword", "third keyword"});

            articles.add(article);
        }

        // SAVE ARTICLES TO DB
        NewsDb newsDb = new NewsDb( getContext() );
        newsDb.saveArticles(articles);

        // GET ARTICLES FROM DB
        List<Article> articlesFromDb = newsDb.getArticles( 0L ,null);
        assertNotNull(articlesFromDb);
        assertTrue( articlesFromDb.size() == nbArticlesToAdd );
        for( int a=0 ; a<articlesFromDb.size() ; a++ ){

            assertTrue( articlesFromDb.get(a).getId() == a+1 );

            assertTrue( articlesFromDb.get(a).getTitle().equals( " tilte " + a ) );

            assertTrue( articlesFromDb.get(a).getDescription().equals("description " + a ) );

            assertTrue( articlesFromDb.get(a).getText().equals( "text " + a ) );

            assertTrue( articlesFromDb.get(a).getAuthor().equals( "author " + a ));

            assertTrue( articlesFromDb.get(a).getImagesFileName().equals("file"+a+".jpg,file2_"+a+".jpg") );

            assertTrue( articlesFromDb.get(a).getDate()==now);


        }







    }





}
