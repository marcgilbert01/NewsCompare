package marc.newscompare.NewsApiTest;

import android.content.res.AssetManager;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import marc.newscompare.api.Article;
import marc.newscompare.api.TheGuardianArticlesLoader;
import marc.newscompare.dao.NewsDb;

/**
 * Created by gilbertm on 10/03/2016.
 */
public class TheGuardianArticlesLoaderTest extends InstrumentationTestCase{


    public void testGetArticlesFromRss(){


            AssetManager assetManager = getInstrumentation().getContext().getAssets();
            // GET TEMPLATE FILE
            String xml = null;
            try {
                InputStream is = assetManager.open("rssfeedtext_TheGuardian");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                xml = new String(buffer);
            }catch(Exception e){
                e.printStackTrace();
            }

            //
            TheGuardianArticlesLoader theGuardianArticlesLoader = new TheGuardianArticlesLoader();

            try {
                List<Article> articles = theGuardianArticlesLoader.parseNewArticles(xml , null);

            assertTrue( articles.size() == 156 );

            for(int a=0; a<50; a++ ){
                articles.remove(a);
            }

            assertTrue( articles.size() == 106 );

            List<Article> newArticles = theGuardianArticlesLoader.parseNewArticles( xml , articles );

            assertTrue( newArticles.size() == 50 );



        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }






}
