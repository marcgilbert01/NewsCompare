package marc.newscompare.NewsApiTest;

import android.content.res.AssetManager;
import android.test.InstrumentationTestCase;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import marc.newscompare.api.Article;
import marc.newscompare.api.TheDailyMailArticlesLoader;
import marc.newscompare.api.TheGuardianArticlesLoader;

/**
 * Created by gilbertm on 16/03/2016.
 */
public class TheDailyMailArticlesLoaderTest extends InstrumentationTestCase{



    public void testGetArticlesFromRss(){


        AssetManager assetManager = getInstrumentation().getContext().getAssets();
        // GET TEMPLATE FILE
        String xml = null;
        try {
            InputStream is = assetManager.open("rssfeedtext_TheDailyMail");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            xml = new String(buffer);
        }catch(Exception e){
            e.printStackTrace();
        }

        //
        TheDailyMailArticlesLoader theDailyMailArticlesLoader = new TheDailyMailArticlesLoader();

        try {
            List<Article> articles = theDailyMailArticlesLoader.parseNewArticles(xml , null);

            assertTrue( articles.size() == 162 );

            for(int a=0; a<50; a++ ){
                articles.remove(a);
            }

            assertTrue( articles.size() == 112 );

            List<Article> newArticles = theDailyMailArticlesLoader.parseNewArticles( xml , articles );

            assertEquals( newArticles.size() , 50 );



        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }





}
