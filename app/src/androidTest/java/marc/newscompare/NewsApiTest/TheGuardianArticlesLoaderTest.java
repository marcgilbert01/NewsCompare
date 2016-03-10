package marc.newscompare.NewsApiTest;

import android.test.AndroidTestCase;

import marc.newscompare.NewsApi.Article;
import marc.newscompare.NewsApi.TheGuardianArticlesLoader;

/**
 * Created by gilbertm on 10/03/2016.
 */
public class TheGuardianArticlesLoaderTest extends AndroidTestCase{


    public void testGetArticlesFromRss(){


        TheGuardianArticlesLoader theGuardianArticlesLoader = new TheGuardianArticlesLoader();
        theGuardianArticlesLoader.getNewArticlesFromRss();





    }




}
