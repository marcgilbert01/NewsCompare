package marc.newscompare.NewsApiTest;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.test.AndroidTestCase;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import marc.newscompare.api.Article;
import marc.newscompare.api.ArticlesLoader;
import marc.newscompare.api.TheGuardianArticlesLoader;

/**
 * Created by gilbertm on 15/03/2016.
 */
public class ArticlesLoaderTest extends AndroidTestCase{

    ArticlesLoader articlesLoader;

    @Override
    public void setUp() throws Exception {

        articlesLoader = ArticlesLoader.newInstance( Article.NewsPaper.THE_GUARDIAN ,
        new File( Environment.getExternalStorageDirectory()+"/newsCompare/images" ) );

    }

    @Override
    public void tearDown() throws Exception {

        articlesLoader = null;
        File file = new File( Environment.getExternalStorageDirectory()+"/newsCompare" );
        FileUtils.deleteDirectory(file);

    }

    public void testSaveImage(){

        Bitmap[] dummyBitmaps = createDummyBitmaps();

        String[] imagesNames = new String[dummyBitmaps.length];

        for(int b=0 ; b<dummyBitmaps.length ; b++  ){
            imagesNames[b] = articlesLoader.saveImage( dummyBitmaps[b] , false);
        }

        // RETRIEVE BITMAPS
        for( int i=0 ; i<imagesNames.length ; i++ ){

            File imageFile = new File(imagesNames[i]);

            assertTrue( imageFile.exists() );

            try {

                Bitmap bitmapFromFile = BitmapFactory.decodeStream(new FileInputStream(imageFile));
                int pixelsFromFile[] = new int[ bitmapFromFile.getWidth() * bitmapFromFile.getHeight()  ];
                bitmapFromFile.getPixels( pixelsFromFile, 0, bitmapFromFile.getWidth(), 0, 0, bitmapFromFile.getWidth(), bitmapFromFile.getHeight() );

                Bitmap dummyBitmap = dummyBitmaps[i];
                int pixelsFromArticle[] = new int[ dummyBitmap.getWidth() * dummyBitmap.getHeight() ];
                dummyBitmap.getPixels(pixelsFromArticle, 0, dummyBitmap.getWidth(), 0, 0, dummyBitmap.getWidth(), dummyBitmap.getHeight());
                dummyBitmap = Bitmap.createScaledBitmap( dummyBitmap , 600 , 600 , false);

                assertTrue( dummyBitmap.getWidth()  == bitmapFromFile.getWidth() );
                assertTrue( dummyBitmap.getHeight() == bitmapFromFile.getHeight() );
                //assertTrue( Arrays.equals(pixelsFromFile, pixelsFromArticle) );

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }



    }



    public void testSaveImages(){


        Article article = new Article();
        article.setThumbnailUrl("http://www.casiocd.co.uk/images/cbmslogo.jpg");
        String[] imagesUrls = new String[]{
                    "http://www.casiomedia.co.uk/medialibrary/76511..jpg",
                    "http://www.casiomedia.co.uk/medialibrary/76547..jpg",
                    "http://www.casio.co.uk/media/176531/v-r7000_banner.jpg"
        };
        article.setImagesUrls(imagesUrls);

        article = articlesLoader.saveArticleImages(article);

        assertNotNull( article.getThumbnailFileName() );
        assertTrue( article.getThumbnailFileName().length()>2 );

        File file = new File( article.getThumbnailFileName() );
        assertNotNull(file);
        assertTrue(file.exists());
        try {
            Bitmap bitmap = BitmapFactory.decodeStream( new FileInputStream(file) );
            assertEquals( ArticlesLoader.THUMBNAIL_WIDTH , bitmap.getWidth() );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assertNotNull(article.getImagesFilesNames());
        assertTrue( article.getImagesFilesNames().length>0 );

        for(String imageFileName : article.getImagesFilesNames() ){

            assertNotNull(imageFileName);
            assertTrue(imageFileName.length()>0);

            file = new File(imageFileName);
            assertNotNull(file);
            assertTrue(file.exists());
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream( new FileInputStream(file) );
                assertEquals( ArticlesLoader.IMAGE_WIDTH , bitmap.getWidth() );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }



    }







    static public Bitmap[] createDummyBitmaps(){

        Bitmap bitmap1 = Bitmap.createBitmap( 200 , 200 , Bitmap.Config.ARGB_8888);
        Bitmap bitmap2 = Bitmap.createBitmap( 300 , 300 , Bitmap.Config.ARGB_8888);
        Bitmap bitmap3 = Bitmap.createBitmap( 400 , 400 , Bitmap.Config.ARGB_8888);
        Bitmap bitmap4 = Bitmap.createBitmap( 500 , 500 , Bitmap.Config.ARGB_8888);
        for( int x=0 ; x<50 ; x++ ){

            for(int y=0 ; y<50 ; y++ ){

                bitmap1.setPixel( x , y , Color.BLUE );
                bitmap2.setPixel( bitmap1.getWidth()/2  + x, y, Color.BLUE);
                bitmap3.setPixel( x , bitmap1.getHeight()/2 + y , Color.BLUE );
                bitmap4.setPixel( bitmap1.getWidth()/2 + x , bitmap1.getHeight()/2 + y , Color.BLUE );
            }

        }

        return new Bitmap[]{ bitmap1, bitmap2, bitmap3, bitmap4 };

    }



    public void testFindArticleByTitle(){

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
            article.setNewsPaper(Article.NewsPaper.THE_GUARDIAN);
            article.setMatchingArticlesIds("51 " + a + ",52 " + a + ",53 " + a);
            article.setDate(now);
            //article.setBitmaps( ArticleTest.createDummyBitmaps() );
            //article.setKeywords(new String[]{"first keyword", "second keyword", "third keyword"});
            articles.add(article);
        }


        int position = ArticlesLoader.findArticleByTitle( articles , "title 3" );

        assertEquals( 2 , position );


    }



    public void testDeletesImages(){

        Bitmap[] dummyBitmaps = createDummyBitmaps();

        Article article = new Article();
        article.setThumbnailUrl("http://www.casiocd.co.uk/images/cbmslogo.jpg");
        String[] imagesUrls = new String[]{
                "http://www.casiomedia.co.uk/medialibrary/76511..jpg",
                "http://www.casiomedia.co.uk/medialibrary/76547..jpg",
                "http://www.casio.co.uk/media/176531/v-r7000_banner.jpg"
        };
        article.setImagesUrls(imagesUrls);
        article = articlesLoader.saveArticleImages(article);

        // SLEEP
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Long timeImageSaved = System.currentTimeMillis();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //
        article = new Article();
        article.setThumbnailUrl("http://www.casiocd.co.uk/images/cbmslogo.jpg");
        imagesUrls = new String[]{
                "http://www.casiomedia.co.uk/medialibrary/76511..jpg",
                "http://www.casiomedia.co.uk/medialibrary/76547..jpg",
                "http://www.casio.co.uk/media/176531/v-r7000_banner.jpg"
        };
        article.setImagesUrls(imagesUrls);
        article = articlesLoader.saveArticleImages(article);

        articlesLoader.deletesImages( timeImageSaved );

        File imageDirectory = articlesLoader.getImageDirectory();
        File[] imageFiles = imageDirectory.listFiles();

        assertEquals( 4 , imageFiles.length );



    }




}
