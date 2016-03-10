package marc.newscompare.NewsApi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by gilbertm on 10/03/2016.
 */

public class TheGuardianArticlesLoader extends ArticlesLoader{

    static final String RSS_URL = "http://www.theguardian.com/uk/rss";

    List<Article> articles = new ArrayList<Article>();

    public List<Article> getNewArticlesFromRss(){

        List<Article> newArticles = new ArrayList<Article>();

        try {

            // READ DATA FROM RSS FEED
            String rssData = getData( RSS_URL );

            //
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource( new StringReader(rssData)));
            NodeList nodeListItems = document.getElementsByTagName("item");
            SimpleDateFormat simpleDateFormat   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for( int i=0 ; i<nodeListItems.getLength() ; i++ ){

                Element elementItem = (Element) nodeListItems.item(i);

                // CHECK IF ARTICLE IS ALREADY LOADED
                String articleTitle = elementItem.getElementsByTagName("title").item(0).getTextContent();
                Boolean alreadyLoaded = false;
                int a = 0;
                while( alreadyLoaded==false && a<articles.size() ){
                    if( articles.get(a).title.equals(articleTitle) ){
                        alreadyLoaded = true;
                    }
                    a++;
                }

                // LOAD INFO
                if( alreadyLoaded == false ) {
                    Article article = new Article();
                    article.newsPaper = Article.NewsPaper.THE_GUARDIAN;
                    // TITLE, DESCRIPTION, AUTHOR
                    article.title = elementItem.getElementsByTagName("title").item(0).getTextContent();
                    article.description = elementItem.getElementsByTagName("description").item(0).getTextContent();
                    article.author = elementItem.getElementsByTagName("dc:creator").item(0).getTextContent();
                    // DATE
                    String dateStr = elementItem.getElementsByTagName("dc:date").item(0).getTextContent();
                    dateStr = dateStr.replaceAll("T", " ").replaceAll("Z", "");
                    Date date = simpleDateFormat.parse(dateStr);
                    article.date = date.getTime();
                    // KEYWORDS
                    NodeList nodeListCategories = elementItem.getElementsByTagName("category");
                    article.categories = new String[nodeListCategories.getLength()];
                    for (int c = 0; c < article.categories.length; c++) {
                        article.categories[c] = nodeListCategories.item(c).getTextContent();
                    }
                    // IMAGES
                    NodeList nodeListMediaContent = elementItem.getElementsByTagName("media:content");
                    article.bitmaps = new Bitmap[nodeListMediaContent.getLength()];
                    for (int b = 0; b < article.bitmaps.length; b++) {
                        Element elementMediaContent = (Element) nodeListMediaContent.item(b);
                        URL url = new URL(elementMediaContent.getAttribute("url"));
                        article.bitmaps[b] = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    }
                    newArticles.add(article);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // ADD NEW ARTICLES TO ALL ARTICLES
        articles.addAll(newArticles);

        return newArticles;

    }





    @Override
    public void loadArticles(long maxAge, ArticleLoaderCallBack articleLoaderCallBack) {
    }



}
