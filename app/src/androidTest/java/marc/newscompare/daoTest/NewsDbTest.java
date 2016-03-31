package marc.newscompare.daoTest;

import android.content.res.AssetManager;
import android.os.Environment;
import android.test.InstrumentationTestCase;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import marc.newscompare.api.Article;
import marc.newscompare.api.ArticlesLoader;
import marc.newscompare.api.TheDailyMailArticlesLoader;
import marc.newscompare.api.TheGuardianArticlesLoader;
import marc.newscompare.dao.NewsDb;

/**
 * Created by gilbertm on 14/03/2016.
 */
public class NewsDbTest extends InstrumentationTestCase {


    static final File DB_DIRECTORY = new File(Environment.getExternalStorageDirectory() + "/newsCompare"  );
    static final File IMG_DIRECTORY = new File(Environment.getExternalStorageDirectory() + "/newsCompare/images"  );

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // DELETE DATABASE
        File dbFile = new File( DB_DIRECTORY + "/news.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }

        ArticlesLoader.setImgDirectory( IMG_DIRECTORY.toString() );
        // DELETE IMAGES DIRECTORY
        try {
            FileUtils.deleteDirectory( new File( ArticlesLoader.getImgDirectory() ) );
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public void testSaveArticles() {

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
            article.setMatchingArticlesIds("51 " + a + ",52 " + a + ",53 " + a);
            article.setDate(now);
            //article.setBitmaps( ArticleTest.createDummyBitmaps() );
            //article.setKeywords(new String[]{"first keyword", "second keyword", "third keyword"});
            articles.add(article);
        }
        // SAVE ARTICLES TO DB
        NewsDb newsDb = new NewsDb( getInstrumentation().getContext() , DB_DIRECTORY );
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
            assertTrue(articlesFromDb.get(a).getMatchingArticlesIds().equals("51 " + a + ",52 "+a+",53 "+a));

            assertTrue(articlesFromDb.get(a).getDate() == now);
        }
    }




    public void testSaveArticleFromTheGuardian() {

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
        NewsDb newsDb = new NewsDb( getInstrumentation().getContext(), DB_DIRECTORY);

        newsDb.saveArticles(articles);

        List<Article> theGuardianArticles  = newsDb.getArticles( 0L , Article.NewsPaper.THE_GUARDIAN , false);

        assertNotNull(theGuardianArticles);

        assertTrue( theGuardianArticles.size()==156 );

        for(int a=0 ; a<theGuardianArticles.size() ; a++  ){

            Article orgArticle = articles.get(a);
            Article dbArticle  = theGuardianArticles.get(a);

            assertTrue( orgArticle.getTitle().equals(dbArticle.getTitle())   );

            assertTrue( orgArticle.getDescription().equals(dbArticle.getDescription()) );

            assertTrue( orgArticle.getAuthor().equals(dbArticle.getAuthor()));

            assertTrue( orgArticle.getDate() == dbArticle.getDate() );

        }

    }


    public void testSaveKeywords(){

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

        // SAVE ARTICLES WITHOUT KEYWORDS
        NewsDb newsDb = new NewsDb(getInstrumentation().getContext() , DB_DIRECTORY );
        newsDb.saveArticles(articles);

        // RETRIEVE ARTICLES WITHOUT KEYWORDS
        List<Article> articlesFromDb = newsDb.getArticles(0L, null , false);

        // ADD KEYWORDS TO ARTICLES
        for(int a=0 ; a<articlesFromDb.size() ; a++ ) {
            List<String> keywords = new ArrayList<>();
            keywords.add("first keyword " + a);
            keywords.add("second keyword " + a);
            keywords.add("third keyword " + a);
            articlesFromDb.get(a).setKeywords(keywords);
        }

        // SAVE KEYWORDS WITH ARTICLES
        newsDb.saveKeywords(articlesFromDb);

        // GET ARTICLES FRIOM DB WITH KEYWORDS
        List<Article> articlesFromDbIncludeKeywords = newsDb.getArticles(0L,null,true);

        // CHECK
        assertNotNull(articlesFromDbIncludeKeywords);
        assertEquals(articlesFromDbIncludeKeywords.size(), nbArticlesToAdd);
        for(int a=0 ; a<articlesFromDbIncludeKeywords.size() ; a++   ){

            Article articleFromDbIncludeKeywords = articlesFromDbIncludeKeywords.get(a);
            Article articleFromDb = articlesFromDb.get(a);

            assertEquals( articleFromDb.getTitle() , articleFromDbIncludeKeywords.getTitle() );

            assertEquals( articleFromDb.getDescription() , articleFromDbIncludeKeywords.getDescription() );

            assertEquals( articleFromDb.getText() , articleFromDbIncludeKeywords.getText() );

            assertEquals( articleFromDb.getImagesFileNameStr() , articleFromDbIncludeKeywords.getImagesFileNameStr() );

            assertEquals(articleFromDb.getDate(), articleFromDbIncludeKeywords.getDate());

            assertEquals(articleFromDb.getKeywords().get(0), articleFromDbIncludeKeywords.getKeywords().get(0));

            assertEquals(articleFromDb.getKeywords().get(1), articleFromDbIncludeKeywords.getKeywords().get(1));

            assertEquals(articleFromDb.getKeywords().get(2), articleFromDbIncludeKeywords.getKeywords().get(2));

        }

    }



    public void testGetArticlesWithNoKeywords(){

        // PREPARE DUMMY ARTICLES
        List<Article> articles = new ArrayList<Article>();
        int nbArticlesToAdd = 6;
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

        // SAVE ARTICLES WITHOUT KEYWORDS
        NewsDb newsDb = new NewsDb(getInstrumentation().getContext() , DB_DIRECTORY);
        newsDb.saveArticles(articles);

        // RETRIEVE ARTICLES WITHOUT KEYWORDS
        List<Article> articlesFromDb = newsDb.getArticles(0L, null , false);

        // ADD KEYWORDS TO SOME OF THE ARTICLES
        for(int a=0 ; a<articlesFromDb.size() ; a++ ) {

            if( a%2 == 0) {
                List<String> keywords = new ArrayList<>();
                keywords.add("first keyword " + a);
                keywords.add("second keyword " + a);
                keywords.add("third keyword " + a);
                articlesFromDb.get(a).setKeywords(keywords);
            }
        }

        // SAVE KEYWORDS WITH ARTICLES
        newsDb.saveKeywords(articlesFromDb);

        // GET ARTICLES WHICH DON'T HAVE KEYWORDS
        List<Article> articlesWithNoKeywords = newsDb.getArticlesWithNoKeywords();

        assertNotNull(articlesWithNoKeywords);

        assertEquals( articlesFromDb.size()/2 , articlesWithNoKeywords.size() );

        int ak = 0;
        for(int a=0 ; a<articlesFromDb.size() ; a++ ){

            if( !(a%2 ==0) ){

                Article articleFromDb = articlesFromDb.get(a);
                Article articleWithNoKeyWords = articlesWithNoKeywords.get(ak);
                assertEquals( articleFromDb.getTitle() , articleWithNoKeyWords.getTitle() );
                ak++;
            }
        }

        // GET ARTICLES WHICH HAVE KEYWORDS
        List<Article> articlesWithKeywords = newsDb.getArticlesWithKeywords();

        assertNotNull(articlesWithKeywords);

        assertEquals(articlesFromDb.size() / 2, articlesWithKeywords.size());

        ak = 0;
        for(int a=0 ; a<articlesFromDb.size() ; a++ ){

            if( !(a%2 ==0) ){

                Article articleFromDb = articlesFromDb.get(a);
                Article articleWithNoKeyWords = articlesWithNoKeywords.get(ak);
                assertEquals( articleFromDb.getTitle() , articleWithNoKeyWords.getTitle() );
                ak++;
            }
        }



    }





    public void testDeleteArticles(){

        // PREPARE DUMMY ARTICLES SET 1
        List<Article> articlesSet1 = new ArrayList<Article>();
        int nbArticlesToAdd = 3;
        Long now = System.currentTimeMillis();
        for (int a = 0; a < nbArticlesToAdd; a++) {
            Article article = new Article();
            article.setTitle("SET1 tilte " + a);
            article.setDescription("description'quote" + a);
            article.setText("text " + a);
            article.setAuthor("author " + a);
            article.setImagesFileNameStr("file" + a + ".jpg,file2_" + a + ".jpg");
            article.setNewsPaper(Article.NewsPaper.THE_GUARDIAN);
            article.setDate(now);
            //article.setBitmaps( ArticleTest.createDummyBitmaps() );
            articlesSet1.add(article);
        }
        // SAVE ARTICLES WITHOUT KEYWORDS
        NewsDb newsDb = new NewsDb( getInstrumentation().getContext() , DB_DIRECTORY );
        newsDb.saveArticles(articlesSet1);
        // RETRIEVE ARTICLES EXCLUDING THE KEYWORDS
        List<Article> articlesFromDbSet1 = newsDb.getArticles(0L, null , false);
        // ADD KEYWORDS TO THE ARTICLES
        for(int a=0 ; a<articlesFromDbSet1.size() ; a++ ) {

           List<String> keywords = new ArrayList<>();
            keywords.add("SET1 first keyword " + a);
            keywords.add("SET1 second keyword " + a);
            keywords.add("SET1 third keyword " + a);
            articlesFromDbSet1.get(a).setKeywords(keywords);

        }
        // SAVE KEYWORDS WITH ARTICLES
        newsDb.saveKeywords(articlesFromDbSet1);

        // SLEEP FOR A SECOND
        Long olderThan = null;
        try {
            Thread.sleep(500);
            olderThan = System.currentTimeMillis();
            Thread.sleep(500);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // PREPARE DUMMY ARTICLES SET 2
        List<Article> articlesSet2 = new ArrayList<Article>();
        now = System.currentTimeMillis();
        for (int a = 0; a < nbArticlesToAdd; a++) {
            Article article = new Article();
            article.setTitle("SET2 tilte " + a);
            article.setDescription("description'quote" + a);
            article.setText("text " + a);
            article.setAuthor("author " + a);
            article.setImagesFileNameStr("file" + a + ".jpg,file2_" + a + ".jpg");
            article.setNewsPaper(Article.NewsPaper.THE_GUARDIAN);
            article.setDate(now);
            //article.setBitmaps( ArticleTest.createDummyBitmaps() );
            articlesSet2.add(article);
        }
        // SAVE ARTICLES WITHOUT KEYWORDS
        newsDb.saveArticles(articlesSet2);
        // RETRIEVE ARTICLES WITHOUT KEYWORDS
        List<Article> articlesFromDbSet2 = newsDb.getArticles(olderThan, null , false);
        // ADD KEYWORDS TO THE ARTICLES
        for(int a=0 ; a<articlesFromDbSet2.size() ; a++ ) {

            List<String> keywords = new ArrayList<>();
            keywords.add("SET2 first keyword " + a);
            keywords.add("SET2 second keyword " + a);
            keywords.add("SET2 third keyword " + a);
            articlesFromDbSet2.get(a).setKeywords(keywords);

        }
        // SAVE KEYWORDS WITH ARTICLES
        newsDb.saveKeywords(articlesFromDbSet2);

        // DELETE ARTICLES FROM SET1
        newsDb.deleteArticles(olderThan);

        // GET ARTICLES
        List<Article> remainingArticles = newsDb.getArticles(0L, null, true);

        assertNotNull(remainingArticles);

        assertEquals( 3 , remainingArticles.size() );

        for(int a=0 ; a<remainingArticles.size() ; a++ ){

            assertEquals( remainingArticles.get(a).getTitle() , articlesFromDbSet2.get(a).getTitle() );

            assertEquals( remainingArticles.get(a).getKeywords() , articlesFromDbSet2.get(a).getKeywords() );

        }
    }










    public void testGetMatchingArticles(){


        // PREPARE DUMMY ARTICLES
        List<Article> articles = new ArrayList<Article>();
        int nbArticlesToAdd = 5;
        Long now = System.currentTimeMillis();
        for (int a = 0; a < nbArticlesToAdd; a++) {
            Article article = new Article();
            article.setTitle("tilte " + a);
            article.setDescription("description'quote" + a);
            article.setText("text " + a);
            article.setAuthor("author " + a);
            article.setImagesFileNameStr("file" + a + ".jpg,file2_" + a + ".jpg");
            article.setNewsPaper(Article.NewsPaper.THE_GUARDIAN);
            article.setDate(now);
            //article.setBitmaps( ArticleTest.createDummyBitmaps() );
            articles.add(article);
        }
        // SAVE ARTICLES WITHOUT KEYWORDS
        NewsDb newsDb = new NewsDb( getInstrumentation().getContext() , DB_DIRECTORY );
        newsDb.saveArticles(articles);
        // RETRIEVE ARTICLES EXCLUDING THE KEYWORDS
        List<Article> articlesFromDb = newsDb.getArticles(0L, null , false);
        // ADD KEYWORDS TO THE ARTICLES
        for(int a=0 ; a<articlesFromDb.size() ; a++ ) {

            List<String> keywords = new ArrayList<>();

            if( a==0 ) {
                keywords.add("dog");
                keywords.add("cat");
                keywords.add("horse");
                keywords.add("chicken");
                keywords.add("cow");
                keywords.add("pig");
            }
            if( a==1 ) {
                keywords.add("elephant");
                keywords.add("dog");
                keywords.add("lion");
                keywords.add("cat");
                keywords.add("monkey");
            }
            if( a==2 ) {
                keywords.add("giraffe");
                keywords.add("dog");
                keywords.add("lion");
                keywords.add("horse");
            }
            if( a==3 ) {
                keywords.add("monkey");
                keywords.add("elephant");
                keywords.add("cat");
                keywords.add("eagle");
                keywords.add("giraffe");
                keywords.add("eagle");
            }
            if( a==4 ) {
                keywords.add("chicken");
                keywords.add("giraffe");
                keywords.add("cat");
            }
            articlesFromDb.get(a).setKeywords(keywords);
        }
        // SAVE KEYWORDS WITH ARTICLES
        newsDb.saveKeywords(articlesFromDb);

        // GET ARTICLES FROM DB
        articlesFromDb = newsDb.getArticles(0L, null, true);

        // CHECK FIRST ARTICLE FOR 2 MATCHING KEYWORDS
        List<Article> matchingArticles = newsDb.getMatchingArticles( articlesFromDb.get(0).getKeywords() , 2 );
        assertNotNull(matchingArticles);
        assertEquals(4, matchingArticles.size());
        // MATCHING ARTICLES SHOULD BE 0 ,1 , 2, 4
        assertEquals( articles.get(0).getTitle() , matchingArticles.get(0).getTitle() );
        assertEquals( articles.get(1).getTitle() , matchingArticles.get(1).getTitle() );
        assertEquals(articles.get(2).getTitle(), matchingArticles.get(2).getTitle());
        assertEquals( articles.get(4).getTitle() , matchingArticles.get(3).getTitle() );

        // CHECK SECOND ARTICLE FOR 3 MATCHING KEYWORDS
        matchingArticles = newsDb.getMatchingArticles( articlesFromDb.get(1).getKeywords() , 3 );
        assertNotNull(matchingArticles);
        assertEquals(2, matchingArticles.size());
        // MATCHING ARTICLES SHOULD BE 1 , 3
        assertEquals(articles.get(1).getTitle(), matchingArticles.get(0).getTitle());
        assertEquals( articles.get(3).getTitle() , matchingArticles.get(1).getTitle() );

    }






    // SHOULD INCLUDE THE IMAGES
    public void testDeleteOldArticlesAndImages(){

        // GET XML TEST FILES FRO THE GUARDIAN
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
        List<Article> theGuardianArticles = null;
        try {
            theGuardianArticles = theGuardianArticlesLoader.parseNewArticles(xml, null);
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
        NewsDb newsDb = new NewsDb( getInstrumentation().getContext() , DB_DIRECTORY );
        newsDb.saveArticles( theGuardianArticles );

        // SLEEP 2 sec
        Long olderThan = System.currentTimeMillis();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // GET XML TEST FILES FRO THE GUARDIAN
        xml = null;
        try {
            InputStream is = assetManager.open("rssfeedtext_TheDailyMail");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            xml = new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // GET THE GUARDIAN ARTICLES
        TheDailyMailArticlesLoader theDailyMailArticlesLoader = new TheDailyMailArticlesLoader();
        List<Article> theDailyMailArticles = null;
        try {
            theDailyMailArticles = theDailyMailArticlesLoader.parseNewArticles(xml,null);
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
        newsDb.saveArticles(theDailyMailArticles);

        List<Article> articlesFromDb = newsDb.getArticles(0L,null,false);

        int totalImages = 0;
        int nbOldImages = 0;
        File imagesDirectory = new File( ArticlesLoader.getImgDirectory() );
        totalImages = imagesDirectory.listFiles().length;
        for( File file : imagesDirectory.listFiles() ){
            if( file.lastModified()<olderThan ){
                nbOldImages++;
            }
        }

        assertNotNull(articlesFromDb);

        assertEquals(theDailyMailArticles.size() + theGuardianArticles.size(), articlesFromDb.size());

        newsDb.deleteArticles(olderThan);

        articlesFromDb = newsDb.getArticles( 0L , null , false );

        assertEquals( theDailyMailArticles.size() , articlesFromDb.size() );

        // CHECK IMAGES
        ArticlesLoader.deletesImages(olderThan);

        assertTrue(totalImages > 0);
        assertTrue(nbOldImages > 0);

        assertEquals(  totalImages-nbOldImages , imagesDirectory.listFiles().length  );


    }



    public void testGetArticlesFromIds(){

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
            article.setMatchingArticlesIds("51 " + a + ",52 " + a + ",53 " + a);
            article.setDate(now);
            //article.setBitmaps( ArticleTest.createDummyBitmaps() );
            //article.setKeywords(new String[]{"first keyword", "second keyword", "third keyword"});
            articles.add(article);
        }
        // SAVE ARTICLES TO DB
        NewsDb newsDb = new NewsDb( getInstrumentation().getContext() , DB_DIRECTORY );
        newsDb.saveArticles(articles);
        // GET ARTICLES FROM DB
        List<Article> articlesFromDb = newsDb.getArticles( new Integer[]{2,3} );
        assertNotNull(articlesFromDb);
        assertEquals(2, articlesFromDb.size());
        // CHECK ARTICLE 2
        assertEquals( articles.get(1).getTitle()  , articlesFromDb.get(0).getTitle() );
        assertEquals( articles.get(1).getDescription() , articlesFromDb.get(0).getDescription() );
        assertEquals(articles.get(1).getText(), articlesFromDb.get(0).getText());
        assertEquals( articles.get(1).getAuthor() , articlesFromDb.get(0).getAuthor() );
        assertEquals( articles.get(1).getImagesFileNameStr() , articlesFromDb.get(0).getImagesFileNameStr() );
        assertEquals( articles.get(1).getNewsPaper() , articlesFromDb.get(0).getNewsPaper() );
        assertEquals(articles.get(1).getDate(), articlesFromDb.get(0).getDate());
        assertEquals(articles.get(1).getMatchingArticlesIds(), articlesFromDb.get(0).getMatchingArticlesIds());
        // CHECK ARTICLE 3
        assertEquals( articles.get(2).getTitle()  , articlesFromDb.get(1).getTitle() );
        assertEquals(articles.get(2).getDescription(), articlesFromDb.get(1).getDescription());
        assertEquals( articles.get(2).getText()   , articlesFromDb.get(1).getText() );
        assertEquals( articles.get(2).getAuthor() , articlesFromDb.get(1).getAuthor() );
        assertEquals(articles.get(2).getImagesFileNameStr(), articlesFromDb.get(1).getImagesFileNameStr());
        assertEquals(articles.get(2).getNewsPaper(), articlesFromDb.get(1).getNewsPaper());
        assertEquals(articles.get(2).getDate(), articlesFromDb.get(1).getDate());
        assertEquals( articles.get(2).getMatchingArticlesIds() , articlesFromDb.get(1).getMatchingArticlesIds() );



    }



    public void testUpdateMatchingArticles(){


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
        NewsDb newsDb = new NewsDb( getInstrumentation().getContext() , DB_DIRECTORY );
        newsDb.saveArticles(articles);
        // GET ARTICLES FROM DB
        List<Article> articlesFromDb = newsDb.getArticles( new Integer[]{2,3} );
        // UPDATE ARTICLE 1 WITH MATCHING ARTICLES
        Article article = articlesFromDb.get(0);
        article.setMatchingArticlesIds("99,88,77,66");
        newsDb.updateMatchingArticles(article);
        // GET UPDATED ARTICLES
        List<Article> updatedArticle = newsDb.getArticles( new Integer[]{article.getId()} );

        assertNotNull(updatedArticle);

        assertEquals( 1 , updatedArticle.size() );

        assertEquals( article.getId() , updatedArticle.get(0).getId() );

        assertEquals( article.getMatchingArticlesIds() , updatedArticle.get(0).getMatchingArticlesIds() );

    }



    public void testGetArticlesWithMatchingArticles(){

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
            if( a==1 || a==3 ) {
                article.setMatchingArticlesIds("4,0");
            }
            else{
                article.setMatchingArticlesIds(null);
            }
            articles.add(article);
        }
        // SAVE ARTICLES TO DB
        NewsDb newsDb = new NewsDb( getInstrumentation().getContext() , DB_DIRECTORY );
        newsDb.saveArticles(articles);
        // GET ARTICLES FROM DB
        List<Article> articlesWithMatchingArticles = newsDb.getArticlesWithMatchingArticles(0L, Article.NewsPaper.THE_GUARDIAN);

        assertNotNull(articlesWithMatchingArticles);

        assertEquals( 2 , articlesWithMatchingArticles.size());

        assertEquals( articles.get(1).getTitle(), articlesWithMatchingArticles.get(0).getTitle() );

        assertEquals( articles.get(3).getTitle() , articlesWithMatchingArticles.get(1).getTitle() );

        articlesWithMatchingArticles = newsDb.getArticlesWithMatchingArticles(0L, Article.NewsPaper.THE_DAILY_MAIL);

        assertNotNull( articlesWithMatchingArticles );

        assertEquals( 0 , articlesWithMatchingArticles.size() );



    }




}