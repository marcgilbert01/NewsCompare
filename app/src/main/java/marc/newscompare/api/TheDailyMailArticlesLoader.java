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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by gilbertm on 16/03/2016.
 */
public class TheDailyMailArticlesLoader extends ArticlesLoader{

    static Map<Article.Category,String> categoriesMap = null;
    static{
       categoriesMap = new HashMap<>();
       categoriesMap.put(Article.Category.HOME, "http://www.dailymail.co.uk/home/index.rss" );
       // categoriesMap.put(Article.Category.POLITICS , "" );
       categoriesMap.put(Article.Category.BUSINESS , "http://www.dailymail.co.uk/money/index.rss" );
       // categoriesMap.put(Article.Category.CULTURE  , "" );
       categoriesMap.put(Article.Category.SCIENCE  , "http://www.dailymail.co.uk/sciencetech/index.rss" );
       categoriesMap.put(Article.Category.SPORT    , "http://www.dailymail.co.uk/sport/index.rss" );
    }


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
                    // SET CATEGORY
                    for( Article article : articlesFromRss ){
                       article.setCategory(entry.getKey());
                    }
                    // ADD TO LIST
                    existingArticles.addAll(articlesFromRss);
                    newArticles.addAll(articlesFromRss);
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

        // PARSE XML
        DocumentBuilder documentBuilder = null;
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(new InputSource(new StringReader(xml)));

        // GO THROUGH ELEMENTS
        NodeList nodeListItems = document.getElementsByTagName("item");
        SimpleDateFormat simpleDateFormat   = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
//                                                                 "Wed, 16 Mar 2016 13:19:34 +0000"


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

                try {
                    Article article = new Article();
                    article.newsPaper = Article.NewsPaper.THE_DAILY_MAIL;
                    // TITLE, DESCRIPTION, AUTHOR
                    article.title = elementItem.getElementsByTagName("title").item(0).getTextContent();
                    article.description = elementItem.getElementsByTagName("description").item(0).getTextContent();
                    article.author = elementItem.getElementsByTagName("media:credit").item(0).getTextContent();
                    // DATE
                    String dateStr = elementItem.getElementsByTagName("pubDate").item(0).getTextContent();
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
                    String[] imagesUrls = new String[nodeListMediaContent.getLength()];
                    String[] imagesFileNames = new String[nodeListMediaContent.getLength()];
                    for (int b = 0; b < imagesUrls.length; b++) {

                        Element elementMediaContent = (Element) nodeListMediaContent.item(b);
                        imagesUrls[b] = elementMediaContent.getAttribute("url");
                        URL url = new URL(imagesUrls[b]);

                        Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        int destWidth  = 200;
                        double ratio = ((double)destWidth/(double)bitmap.getWidth());
                        int destHeight = (int) ((double)bitmap.getHeight() * ratio);
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,destWidth,destHeight, false);

                        imagesFileNames[b] = saveImage(scaledBitmap);
                        bitmap = null;
                        scaledBitmap = null;
                    }
                    article.setImagesFilesNames(imagesFileNames);
                    // ADD TO LIST
                    articles.add(article);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        return articles;
    }






}
