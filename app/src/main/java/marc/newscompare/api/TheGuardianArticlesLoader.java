package marc.newscompare.api;

import android.content.Context;
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

    @Override
    public List<Article> getNewArticles(List<Article> existingArticles ) {


        List<Article> newArticles = new ArrayList<Article>();

        // READ DATA FROM RSS FEED
        String rssData = null;
        try {
            rssData = getData( RSS_URL );
        } catch (IOException e) {
            e.printStackTrace();
        }
        // PARSE DATA
        if( rssData!=null ){
            try {
                newArticles = parseNewArticles( rssData , existingArticles);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }

        return newArticles;

    }



    public List<Article> parseNewArticles(String xml , List<Article> existingArticles ) throws ParserConfigurationException, IOException, SAXException, ParseException {

        if( existingArticles!=null ){
            articles = existingArticles;
        }
        else{
            articles = new ArrayList<>();
        }


        List<Article> articles = new ArrayList<>();

        //
        DocumentBuilder documentBuilder = null;
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(new InputSource( new StringReader(xml)));
        NodeList nodeListItems = document.getElementsByTagName("item");
        SimpleDateFormat simpleDateFormat   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for( int i=0 ; i<nodeListItems.getLength() ; i++ ){

            Element elementItem = (Element) nodeListItems.item(i);

            // CHECK IF ARTICLE IS ALREADY LOADED
            Boolean alreadyLoaded = false;
            if( existingArticles!=null ) {
                String articleTitle = elementItem.getElementsByTagName("title").item(0).getTextContent();
                int a = 0;
                while (alreadyLoaded == false && a < existingArticles.size()) {
                    if (existingArticles.get(a).title.equals(articleTitle)) {
                        alreadyLoaded = true;
                    }
                    a++;
                }
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
                // CATEGORIES
                //NodeList nodeListCategories = elementItem.getElementsByTagName("category");
                //article.categories = new String[nodeListCategories.getLength()];
                //for (int c = 0; c < article.categories.length; c++) {
                //    article.categories[c] = nodeListCategories.item(c).getTextContent();
                //}
                // IMAGES
                NodeList nodeListMediaContent = elementItem.getElementsByTagName("media:content");
                String[] imagesUrls      = new String[nodeListMediaContent.getLength()];
                String[] imagesFileNames = new String[nodeListMediaContent.getLength()];
                for (int b = 0; b <imagesUrls.length ; b++) {

                    Element elementMediaContent = (Element) nodeListMediaContent.item(b);
                    imagesUrls[b] = elementMediaContent.getAttribute("url");
                    URL url = new URL( imagesUrls[b] );
                    Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    imagesFileNames[b] = saveImage(bitmap);
                    bitmap = null;
                }
                article.setImagesFilesNames(imagesFileNames);
                // ADD TO LIST
                articles.add(article);
            }
        }

        return articles;
    }

/*
    <item>
        <title>
        BBC presenter Jeremy Vine pictured riding a bike without a helmet in London
        </title>

        <link>
        http://www.dailymail.co.uk/news/article-3495075/Tut-tut-Jeremy-BBC-presenter-Jeremy-Vine-comes-fire-pictured-riding-bike-without-helmet-London.html?ITO=1490&amp;ns_mchannel=rss&amp;ns_campaign=1490
        </link>

        <description>
        The Radio 2 host was captured on camera as he waited at traffic lights on The Strand on his 'Boris Bike', but he was quickly criticised when people spotted that he was not wearing a helmet.
        </description>

        <enclosure url="http://i.dailymail.co.uk/i/pix/2016/03/16/13/323FD41000000578-0-image-m-157_1458133256539.jpg" type="image/jpeg" length="7506"/>

        <pubDate>Wed, 16 Mar 2016 13:19:34 +0000</pubDate>

        <guid>
        http://www.dailymail.co.uk/news/article-3495075/Tut-tut-Jeremy-BBC-presenter-Jeremy-Vine-comes-fire-pictured-riding-bike-without-helmet-London.html?ITO=1490&amp;ns_mchannel=rss&amp;ns_campaign=1490
        </guid>

        <media:description></media:description>

        <media:thumbnail url="http://i.dailymail.co.uk/i/pix/2016/03/16/13/323FD41000000578-0-image-m-157_1458133256539.jpg" width="154" height="115"/>

        <media:credit scheme="urn:ebu">None</media:credit>

        <media:content type="image/jpeg" url="http://i.dailymail.co.uk/i/pix/2016/03/16/13/323FD41000000578-0-image-m-157_1458133256539.jpg"/>

    </item>
*/



}
