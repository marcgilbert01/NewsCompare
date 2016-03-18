package marc.newscompare.daoTest;

import android.content.res.AssetManager;
import android.test.InstrumentationTestCase;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import marc.newscompare.api.Article;
import marc.newscompare.api.TheGuardianArticlesLoader;
import marc.newscompare.dao.NewsDb;

/**
 * Created by gilbertm on 14/03/2016.
 */
public class NewsDbTest extends InstrumentationTestCase {



    public void testSaveArticles() {
        // DELETE DATABASE
        File dbFile = new File(NewsDb.DATA_DIRECTORY + "/news.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }
        // PREPARE DUMMY ARTICLES
        List<Article> articles = new ArrayList<Article>();
        int nbArticlesToAdd = 5;
        Long now = System.currentTimeMillis();
        for (int a = 0; a < nbArticlesToAdd; a++) {
            Article article = new Article();
            article.setTitle(" tilte " + a);
            article.setDescription("description'quote" + a);
            article.setText("text " + a);
            article.setAuthor("author " + a);
            article.setImagesFileNameStr("file" + a + ".jpg,file2_" + a + ".jpg");
            article.setNewsPaper(Article.NewsPaper.THE_GUARDIAN);
            article.setDate(now);
            //article.setBitmaps( ArticleTest.createDummyBitmaps() );
            //article.setKeywords(new String[]{"first keyword", "second keyword", "third keyword"});
            articles.add(article);
        }
        // SAVE ARTICLES TO DB
        NewsDb newsDb = new NewsDb(getInstrumentation().getContext());
        newsDb.saveArticles(articles);
        // GET ARTICLES FROM DB
        List<Article> articlesFromDb = newsDb.getArticles(0L, null , false);
        assertNotNull(articlesFromDb);
        assertTrue(articlesFromDb.size() == nbArticlesToAdd);
        for (int a = 0; a < articlesFromDb.size(); a++) {
            assertTrue(articlesFromDb.get(a).getId() == a + 1);
            assertTrue(articlesFromDb.get(a).getTitle().equals(" tilte " + a));
            assertTrue(articlesFromDb.get(a).getDescription().equals("description'quote" + a));
            assertTrue(articlesFromDb.get(a).getText().equals("text " + a));
            assertTrue(articlesFromDb.get(a).getAuthor().equals("author " + a));
            assertTrue(articlesFromDb.get(a).getImagesFileNameStr().equals("file" + a + ".jpg,file2_" + a + ".jpg"));
            assertTrue(articlesFromDb.get(a).getDate() == now);
        }
    }




    public void testSaveArticleFromTheGuardian() {

        // DELETE DATABASE
        File dbFile = new File(NewsDb.DATA_DIRECTORY + "/news.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }

        // GET XML TEST FILES
        AssetManager assetManager = getInstrumentation().getContext().getAssets();
        String xml = null;
        try {
            InputStream is = assetManager.open("rssfeedtext_TheGuardian");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            xml = new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // GET THE GUARDIAN ARTICLES
        TheGuardianArticlesLoader theGuardianArticlesLoader = new TheGuardianArticlesLoader();
        List<Article> articles = null;
        try {
            articles = theGuardianArticlesLoader.parseNewArticles(xml, null);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // SAVE TO DB
        NewsDb newsDb = new NewsDb( getInstrumentation().getContext() );

        newsDb.saveArticles(articles);

        List<Article> theGuardianArticles  = newsDb.getArticles( 0L , Article.NewsPaper.THE_GUARDIAN , false);

        assertNotNull(theGuardianArticles);

        assertTrue( theGuardianArticles.size()==156 );

        for(int a=0 ; a<theGuardianArticles.size() ; a++  ){

            Article orgArticle = articles.get(a);
            Article dbArticle  = theGuardianArticles.get(a);

            assertTrue( orgArticle.getTitle().equals(dbArticle.getTitle())   );

            assertTrue( orgArticle.getDescription().equals(dbArticle.getDescription()) );

            //assertTrue( orgArticle.getText().equals(dbArticle.getText()));

            assertTrue( orgArticle.getAuthor().equals(dbArticle.getAuthor()));

            assertTrue( orgArticle.getDate() == dbArticle.getDate() );


        }

    }




    public void testSaveKeywords(){

        // DELETE DATABASE
        File dbFile = new File(NewsDb.DATA_DIRECTORY + "/news.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }

        // PREPARE DUMMY ARTICLES
        List<Article> articles = new ArrayList<Article>();
        int nbArticlesToAdd = 5;
        Long now = System.currentTimeMillis();
        for (int a = 0; a < nbArticlesToAdd; a++) {
            Article article = new Article();
            article.setTitle(" tilte " + a);
            article.setDescription("description'quote" + a);
            article.setText("text " + a);
            article.setAuthor("author " + a);
            article.setImagesFileNameStr("file" + a + ".jpg,file2_" + a + ".jpg");
            article.setNewsPaper(Article.NewsPaper.THE_GUARDIAN);
            article.setDate(now);
            //article.setBitmaps( ArticleTest.createDummyBitmaps() );
            articles.add(article);
        }

        // SAVE KEYWRODS TO KEYWORDS TABLE
        NewsDb newsDb = new NewsDb(getInstrumentation().getContext());
        newsDb.saveArticles(articles);

        List<Article> articlesFromDb = newsDb.getArticles(0L, null , false);

        for(int a=0 ; a<articlesFromDb.size() ; a++ ) {
            List<String> keywords = new ArrayList<>();
            keywords.add("first keyword " + a);
            keywords.add("second keyword " + a);
            keywords.add("third keyword " + a);
            articlesFromDb.get(a).setKeywords(keywords);
        }

        newsDb.saveKeywords(articlesFromDb);

        List<Article> articlesFromDbIncludeKeywords = newsDb.getArticles(0L,null,true);

        assertNotNull(articlesFromDbIncludeKeywords);

        assertEquals(articlesFromDbIncludeKeywords.size(), nbArticlesToAdd);

        for(int a=0 ; a<articlesFromDbIncludeKeywords.size() ; a++   ){

            Article articleFromDbIncludeKeywords = articlesFromDbIncludeKeywords.get(a);
            Article articleFromDb = articlesFromDb.get(a);

            assertEquals( articleFromDbIncludeKeywords.getTitle() ,
                    articleFromDb.getTitle() );

            assertEquals( articleFromDbIncludeKeywords.getDescription() ,
                    articleFromDb.getDescription() );

            assertEquals( articleFromDbIncludeKeywords.getText() ,
                    articleFromDb.getText() );

            assertEquals( articleFromDbIncludeKeywords.getImagesFileNameStr() ,
                    articleFromDb.getImagesFileNameStr() );

            assertEquals( articleFromDbIncludeKeywords.getDate() ,
                    articleFromDb.getDate() );

            assertEquals( articleFromDbIncludeKeywords.getKeywords().get(0) ,
                          articleFromDb.getKeywords().get(0) );
            assertEquals( articleFromDbIncludeKeywords.getKeywords().get(1) ,
                          articleFromDb.getKeywords().get(1) );
            assertEquals( articleFromDbIncludeKeywords.getKeywords().get(2) ,
                          articleFromDb.getKeywords().get(2) );

            //assertEquals( articleFromDbIncludeKeywords , articlesFromDb );




        }


    }



    public void testGetArticlesWithNoKeywords(){

        // DELETE DATABASE
        File dbFile = new File(NewsDb.DATA_DIRECTORY + "/news.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }

        // PREPARE DUMMY ARTICLES
        List<Article> articles = new ArrayList<Article>();
        int nbArticlesToAdd = 5;
        Long now = System.currentTimeMillis();
        for (int a = 0; a < nbArticlesToAdd; a++) {
            Article article = new Article();
            article.setTitle(" tilte " + a);
            article.setDescription("description'quote" + a);
            article.setText("text " + a);
            article.setAuthor("author " + a);
            article.setImagesFileNameStr("file" + a + ".jpg,file2_" + a + ".jpg");
            article.setNewsPaper(Article.NewsPaper.THE_GUARDIAN);
            article.setDate(now);
            //article.setBitmaps( ArticleTest.createDummyBitmaps() );
            //article.setKeywords(new String[]{"first keyword", "second keyword", "third keyword"});
            articles.add(article);
        }

        // SAVE DUMMY ARTICLES TO DB
        NewsDb newsDb = new NewsDb( getInstrumentation().getContext() );
        newsDb.saveArticles( articles );

    }




}