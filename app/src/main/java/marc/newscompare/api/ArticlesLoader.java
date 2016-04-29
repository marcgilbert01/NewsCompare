package marc.newscompare.api;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Size;

import org.apache.commons.lang.BooleanUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by gilbertm on 10/03/2016.
 */
public abstract class ArticlesLoader {

    static public final int THUMBNAIL_WIDTH = 150;
    static public final int IMAGE_WIDTH     = 600;

    static List<Article> articles = new ArrayList<>();
    Map<Article.Category,String> categoriesMap = null;

    File imageDirectory;

    static public ArticlesLoader newInstance(Article.NewsPaper newsPaper , File imageDirectory){

        ArticlesLoader articlesLoader = newsPaper.getArticlesLoader();
        articlesLoader.imageDirectory = imageDirectory;

        return articlesLoader;
    }

    public Article.NewsPaper getNewsPaperType(){

        Article.NewsPaper newsPaper= null;
        for(Article.NewsPaper newsPaper1 : Article.NewsPaper.values()){
            if( newsPaper.articlesLoader==this ){
                newsPaper = newsPaper1;
            }
        }
        return newsPaper;
    }



    protected String getData(String urlStr) throws IOException {

        String data = null;

        URL url = new URL(urlStr);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0" );
        int responseCode = httpURLConnection.getResponseCode();

        if( responseCode==200 ){

            BufferedReader in = new BufferedReader(  new InputStreamReader(httpURLConnection.getInputStream()));
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            data = stringBuilder.toString();
            in.close();
        }

        return data;
    }


    abstract String getElementItemTagName();
    abstract Article buildArticle(Element elementItem);


    public List<Article> getNewArticles(List<Article> existingArticles){

        List<Article> newArticles = new ArrayList<>();
        // FOR EACH CATEGORY
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

        // PARSE XML
        DocumentBuilder documentBuilder = null;
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = null;
        try {
            document = documentBuilder.parse(new InputSource(new StringReader(xml)));
        }catch (IOException ioe){
            ioe.printStackTrace();
        }

        if( document!=null ){

            // GO THROUGH ELEMENTS
            NodeList nodeListItems = document.getElementsByTagName(getElementItemTagName());
            if( nodeListItems!=null ) {

                for (int i = 0; i < nodeListItems.getLength(); i++) {

                    Element elementItem = (Element) nodeListItems.item(i);
                    Article article = buildArticle( elementItem );
                    if( article!=null && article.getTitle()!=null && article.getTitle().length()>0 ) {

                        // CHECK IF ARTICLE IS ALREADY LOADED
                        int position = findArticleByTitle(existingArticles, article.getTitle());
                        // CREATE ARTICLE
                        if ( position < 0 ) {
                            articles.add(article);
                        }
                    }
                }
            }
        }
        return articles;
    }



    public Article saveArticleImages(Article article){

        if(  article!=null ) {

            // SAVE THUMBNAIL
            if (article.getThumbnailUrlStr() != null) {
                try {
                    URL url = new URL(article.getThumbnailUrlStr());
                    Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    article.setThumbnailFileName(saveImage(bitmap, true));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // SAVE IMAGES
            if ( article.getImagesUrls() != null) {
                String[] imagesFilesName = new String[article.getImagesUrls().length];
                for (int i = 0; i < article.getImagesUrls().length; i++) {
                    try {
                        URL url = new URL(article.getImagesUrls()[i]);
                        Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        imagesFilesName[i] = saveImage(bitmap, false);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                article.setImagesFilesNames(imagesFilesName);
            }
        }

        return article;
    }





    public String saveImage( Bitmap bitmap , Boolean asThumbnail) {

        String fileName = null;

        if( bitmap!=null  && imageDirectory!=null ){

            // SCALE BITMAP
            bitmap = scaleBitmap( bitmap , asThumbnail );

            try {
                // CREATE FILE NAME
                File imageFile = null;
                while (imageFile == null || imageFile.exists()) {
                    fileName = imageDirectory.toString() + "/" + System.currentTimeMillis() + ".jpg";
                    imageFile = new File(fileName);
                    Thread.sleep(50);
                }
                // CREATE FILE
                if (  imageFile.getParentFile()!=null && !imageFile.getParentFile().exists() ) {
                    imageFile.getParentFile().mkdirs();
                }
                Boolean created = imageFile.createNewFile();
                if ( created == true ) {
                    FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                    bitmap.compress( Bitmap.CompressFormat.JPEG, 90, fileOutputStream );
                    fileOutputStream.close();
                }

            }catch (IOException e){
                e.printStackTrace();
                fileName = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return fileName;
    }


    public void deletesImages(Long olderThan){

        if( imageDirectory!=null ) {

            File[] imageFiles = imageDirectory.listFiles();
            if( imageFiles!=null ) {
                for (File file : imageDirectory.listFiles()) {
                    if (file.lastModified() < olderThan) {
                        file.delete();
                    }
                }
            }
        }
    }




    public File getImageDirectory() {
        return imageDirectory;
    }





    private static Bitmap scaleBitmap(Bitmap bitmap , Boolean asThumbnail){

        Bitmap scaledBitmap = null;

        int scaledWidth  = 0;
        int scaledHeight = 0;

        if( asThumbnail ){
            scaledWidth  = THUMBNAIL_WIDTH;
        }
        else{
            scaledWidth  = IMAGE_WIDTH;
        }
        scaledHeight = (int) ( (double)bitmap.getHeight() * ( (double)scaledWidth/(double)bitmap.getWidth() ) );

        scaledBitmap = Bitmap.createScaledBitmap( bitmap , scaledWidth , scaledHeight , false);

        return scaledBitmap;

    }


    static public int findArticleByTitle( List<Article> articles , String articleTitle){

        int position = -1;

        if ( articles != null && articleTitle!=null && articleTitle.length()>0 ) {

            int a = 0;
            while ( position<0 && a < articles.size()) {
                if ( articles.get(a).title.equals(articleTitle)) {
                    position = a;
                }
                a++;
            }
        }

        return position;
    }


}

