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
import java.net.MalformedURLException;
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
 * Created by gilbertm on 10/03/2016.
 */

public class TheGuardianArticlesLoader extends ArticlesLoader{


    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
    //                                                       "Thu, 28 Apr 2016 10:17:39 GMT"
    //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public TheGuardianArticlesLoader() {

        categoriesMap = new HashMap<>();
        categoriesMap.put(Article.Category.HOME     , "http://www.theguardian.com/uk/rss" );
        categoriesMap.put(Article.Category.POLITICS , "http://www.theguardian.com/politics/rss" );
        categoriesMap.put(Article.Category.BUSINESS , "http://www.theguardian.com/uk/business/rss" );
        //categoriesMap.put(Article.Category.MONEY    , "" );
        categoriesMap.put(Article.Category.CULTURE  , "http://www.theguardian.com/uk/culture/rss" );
        categoriesMap.put(Article.Category.SCIENCE  , "https://www.theguardian.com/uk/technology/rss" );
        categoriesMap.put(Article.Category.SPORT    , "http://www.theguardian.com/uk/sport/rss" );

    }



    @Override
    String getElementItemTagName() {
        return "item";
    }

    @Override
    Article buildArticle(Element elementItem) {

        Article article = new Article();
        article.newsPaper = Article.NewsPaper.THE_GUARDIAN;
        // TITLE, DESCRIPTION, AUTHOR
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
        if( elementAuthor!=null ){
            article.author = elementAuthor.getTextContent();
        }
        // DATE
        Element elementPubDate = (Element) elementItem.getElementsByTagName("pubDate").item(0);
        if( elementPubDate!=null ) {
            String dateStr = elementPubDate.getTextContent();
            dateStr = dateStr.replace(" GMT" , "");
            Date date = null;
            try {
                date = simpleDateFormat.parse(dateStr);
                article.date = date.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        // IMAGES URLS
        NodeList nodeListMediaContent = elementItem.getElementsByTagName("media:content");
        if( nodeListMediaContent!=null && nodeListMediaContent.getLength()>0 ){

            String[] imagesUrls = new String[nodeListMediaContent.getLength()];
            for (int b = 0; b < imagesUrls.length; b++) {
                Element elementMediaContent = (Element) nodeListMediaContent.item(b);
                imagesUrls[b] = elementMediaContent.getAttribute("url");
                if (b == 0) {
                    article.setThumbnailUrl( imagesUrls[b] );
                }
            }
            article.setImagesUrls(imagesUrls);
        }


        return article;


    }











/*
    @Override
    public List<Article> getNewArticles(List<Article> existingArticles ) {

        List<Article> newArticles = new ArrayList<Article>();

        for(Map.Entry<Article.Category,String> entry : categoriesMap.entrySet() ) {

            // READ DATA FROM RSS FEED
            String rssData = null;
            try {
                rssData = getData( entry.getValue() );
            } catch (IOException e) {
                e.printStackTrace();
            }
            // PARSE DATA
            if( rssData!=null ){
                try {
                    List<Article> articlesFromRss = parseNewArticles( rssData , existingArticles);
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
                // CREATE ARTICLE
                if (alreadyLoaded == false) {
                    try {
                        Article article = createArticle(elementItem);
                        articles.add(article);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return articles;
    }




    private Article createArticle(Element elementItem) throws IOException, ParseException {

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

        return article;
    }
*/


}
