package marc.newscompare.NewsApi;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by gilbertm on 10/03/2016.
 */

public class TheGuardianArticlesLoader extends ArticlesLoader{

    static final String RSS_URL = "http://www.theguardian.com/uk/rss";


    private Article[] getArticlesFromRss(){

        Article[] articles = null;

        try {

            // READ DATA FROM RSS FEED
            String rssData = getData( RSS_URL );

            //
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(rssData));
            NodeList nodeListItems = document.getElementsByTagName("item");
            for( int i=0 ; i<nodeListItems.getLength() ; i++ ){



            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return articles;
    }





    @Override
    public void loadArticles(long maxAge, ArticleLoaderCallBack articleLoaderCallBack) {
    }



}
