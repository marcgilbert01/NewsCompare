package marc.newscompare.NewsApiTest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.test.AndroidTestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

import marc.newscompare.api.Article;

/**
 * Created by gilbertm on 14/03/2016.
 */
public class ArticleTest extends AndroidTestCase{


    public void testSaveImages(){



        Article article = new Article();
        article.setTitle("test Article.saveimages() ");
        article.setBitmaps( createDummyBitmaps() );

        String[] imagesNames = Article.saveImages(article);

        assertNotNull(imagesNames);

        assertTrue(imagesNames.length==4);

        // RETRIEVE BITMAPS
        for( int i=0 ; i<imagesNames.length ; i++ ){

            File imageFile = new File(imagesNames[i]);

            assertTrue( imageFile.exists() );

            try {

                Bitmap bitmapFromFile = BitmapFactory.decodeStream(new FileInputStream(imageFile));
                int pixelsFromFile[] = new int[ bitmapFromFile.getWidth() * bitmapFromFile.getHeight()  ];
                bitmapFromFile.getPixels( pixelsFromFile, 0, bitmapFromFile.getWidth(), 0, 0, bitmapFromFile.getWidth(), bitmapFromFile.getHeight() );

                Bitmap bitmapFromArticle = article.getBitmaps()[i];
                int pixelsFromArticle[] = new int[ bitmapFromArticle.getWidth() * bitmapFromArticle.getHeight() ];
                bitmapFromArticle.getPixels(pixelsFromArticle, 0, bitmapFromArticle.getWidth(), 0, 0, bitmapFromArticle.getWidth(), bitmapFromArticle.getHeight());

                assertTrue(Arrays.equals(pixelsFromFile, pixelsFromArticle));


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



}
