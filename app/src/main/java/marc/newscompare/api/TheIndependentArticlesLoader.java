package marc.newscompare.api;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by gilbertm on 13/04/2016.
 *
 <item>
     <title>Film Reviews - Criminal, Despite The Falling Snow, Eisenstein in Guanajuato</title>
     <link>http://www.independent.co.uk/arts-entertainment/films/reviews/film-reviews-criminal-despite-the-falling-snow-eisenstein-in-guanajuato-a6982311.html</link>
     <description><![CDATA[HM Treasury and Chancellor George Osborne must take some of the responsibility for this misfiring and very jarring thriller. The original intention was to make it in an American city but the producers relocated the action to London to take advantage of the generous tax breaks available in the UK. The result is an American movie dressed up in British clothes that simply don&apos;t fit.]]></description>
     <pubDate>Wed, 13 Apr 2016 12:53:19 +0000</pubDate>
     <guid>a6982311</guid>
     <author>Geoffrey Macnab</author>
     <dc:creator>Geoffrey Macnab</dc:creator>
     <dc:date>2016-04-13T12:53:19+00:00</dc:date>
     <media:thumbnail url="http://static.independent.co.uk/s3fs-public/styles/feed/public/thumbnails/image/2016/04/13/13/criminal.jpg" type="image/jpeg"/>
     <media:content url="http://static.independent.co.uk/s3fs-public/thumbnails/image/2016/04/13/13/criminal.jpg" type="image/jpeg"/>
 </item>
 *
 */

public class TheIndependentArticlesLoader extends ArticlesLoader{



    //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat simpleDateFormat   = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ");
    //                                                          Thu, 28 Apr 2016 12:23:00 +0000

    public TheIndependentArticlesLoader() {

        categoriesMap = new HashMap<>();
        categoriesMap.put(Article.Category.HOME     , "http://www.independent.co.uk/rss" );
        categoriesMap.put(Article.Category.POLITICS , "http://www.independent.co.uk/news/uk/politics/rss" );
        categoriesMap.put(Article.Category.BUSINESS , "http://www.independent.co.uk/news/business/rss" );
        //categoriesMap.put(Article.Category.MONEY    , "" );
        categoriesMap.put(Article.Category.CULTURE  , "http://www.independent.co.uk/arts-entertainment/rss" );
        categoriesMap.put(Article.Category.SCIENCE  , "http://www.independent.co.uk/news/science/rss" );
        categoriesMap.put(Article.Category.SPORT    , "http://www.independent.co.uk/sport/rss" );

    }

    @Override
    String getElementItemTagName() {
        return "item";
    }

    @Override
    Article buildArticle(Element elementItem) {

        Article article = new Article();
        article.newsPaper = Article.NewsPaper.THE_INDEPENDENT;
        // TITLE
        Element elementTitle = (Element) elementItem.getElementsByTagName("title").item(0);
        if( elementTitle!=null ) {
            article.title = elementTitle.getTextContent();
        }
        // DESCRIPTION
        Element elementDescription = (Element) elementItem.getElementsByTagName("description").item(0);
        if( elementDescription!=null ){
            article.description = elementDescription.getTextContent();
        }
        // AUTHOR
        Element elementAuthor = (Element) elementItem.getElementsByTagName("dc:creator").item(0);
        if( elementAuthor!=null ) {
            article.author = elementAuthor.getTextContent();
        }
        // DATE
        Element elementDate = (Element) elementItem.getElementsByTagName("pubDate").item(0);
        if( elementDate!=null ) {

            String dateStr = elementDate.getTextContent();
            Date date = null;
            try {
                date = simpleDateFormat.parse(dateStr);
                article.date = date.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        // IMAGES
        NodeList nodeListMediaContent = elementItem.getElementsByTagName("media:content");
        if( nodeListMediaContent!=null && nodeListMediaContent.getLength()>0 ) {
            String[] imagesUrls = new String[nodeListMediaContent.getLength()];
            for (int b = 0; b < imagesUrls.length; b++) {
                Element elementMediaContent = (Element) nodeListMediaContent.item(b);
                imagesUrls[b] = elementMediaContent.getAttribute("url");
                if (b == 0) {
                    article.setThumbnailUrlStr(imagesUrls[b]);
                }
            }
            article.setImagesUrls(imagesUrls);
        }

        return article;

    }



/*
    @Override
    public List<Article> getNewArticles(List<Article> existingArticles) {

        List<Article> newArticles = new ArrayList<>();

        for(Map.Entry<Article.Category,String> entry : categoriesMap.entrySet() ){

            // READ DATA FROM RSS FEED
            String rssData = null;
            try {
                rssData = getData( entry.getValue() );
            } catch (IOException e) {
                e.printStackTrace();
            }
            // PARSE DATA
            if (rssData != null) {
                try {
                    List<Article> articlesFromRss = parseNewArticles(rssData, existingArticles);
                    existingArticles.addAll(articlesFromRss);
                    newArticles.addAll(articlesFromRss);
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                }
            }

        }

        return newArticles;

    }




    public List<Article> parseNewArticles(String xml , List<Article> existingArticles ) throws ParserConfigurationException, SAXException, ParseException {

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
        Document document = null;
        try {
           document = documentBuilder.parse(new InputSource(new StringReader(xml)));
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        if( document!=null ) {

            NodeList nodeListItems = document.getElementsByTagName("item");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < nodeListItems.getLength(); i++) {

                Element elementItem = (Element) nodeListItems.item(i);
                // CHECK IF ARTICLE IS ALREADY LOADED
                Boolean alreadyLoaded = false;
                if (existingArticles != null) {
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
                if (alreadyLoaded == false) {

                    try{

                            Article article = new Article();
                            article.newsPaper = Article.NewsPaper.THE_INDEPENDENT;
                            // TITLE, DESCRIPTION, AUTHOR
                            article.title = elementItem.getElementsByTagName("title").item(0).getTextContent();
                            article.description = elementItem.getElementsByTagName("description").item(0).getTextContent();
                            article.author = elementItem.getElementsByTagName("dc:creator").item(0).getTextContent();
                            // DATE
                            String dateStr = elementItem.getElementsByTagName("dc:date").item(0).getTextContent();
                            dateStr = dateStr.replaceAll("T", " ").replaceAll("Z", "");
                            Date date = simpleDateFormat.parse(dateStr);
                            article.date = date.getTime();
                            // IMAGES
                            NodeList nodeListMediaContent = elementItem.getElementsByTagName("media:content");
                            String[] imagesUrls = new String[nodeListMediaContent.getLength()];
                            String[] imagesFileNames = new String[nodeListMediaContent.getLength()];
                            for (int b = 0; b < imagesUrls.length; b++) {
                                Element elementMediaContent = (Element) nodeListMediaContent.item(b);
                                imagesUrls[b] = elementMediaContent.getAttribute("url");
                                URL url = new URL(imagesUrls[b]);
                                Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                imagesFileNames[b] = saveImage(bitmap, false);
                                if (b == 0) {
                                    article.setThumbnailFileName(saveImage(bitmap, true));
                                }
                                bitmap = null;
                            }
                            article.setImagesFilesNames(imagesFileNames);
                            // ADD TO LIST
                            articles.add(article);


                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }

                }

            }
        }

        return articles;
    }
*/

}
