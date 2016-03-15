package marc.newscompare.api;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import marc.newscompare.dao.NewsDb;

/**
 * Created by gilbertm on 10/03/2016.
 */
public class Article {

    int id;
    String title;
    String description;
    String text;
    String author;
    String imagesFileName;
    Bitmap[] bitmaps;
    String[] keywords;
    long date;

    NewsPaper newsPaper;

    public enum NewsPaper{

        THE_GUARDIAN( new TheGuardianArticlesLoader() );
        //THE_TELEGRAPHE,

        ArticlesLoader articlesLoader;

        NewsPaper( ArticlesLoader articlesLoader ) {
            this.articlesLoader = articlesLoader;
        }

        public ArticlesLoader getArticlesLoader() {
            return articlesLoader;
        }

        //public getNewsPaperFrom

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public Bitmap[] getBitmaps() {
        return bitmaps;
    }

    public void setBitmaps(Bitmap[] bitmaps) {
        this.bitmaps = bitmaps;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public NewsPaper getNewsPaper() {
        return newsPaper;
    }

    public void setNewsPaper(NewsPaper newsPaper) {
        this.newsPaper = newsPaper;
    }

    public String getImagesFileName() {
        return imagesFileName;
    }

    public void setImagesFileName(String imagesFileName) {
        this.imagesFileName = imagesFileName;
    }

    public static String[] saveImages(Article article) {

        String[] imagesNames = null;

        Bitmap[] bitmaps = article.getBitmaps();
        if( bitmaps!=null ){
            imagesNames = new String[bitmaps.length];
        }

        for(int b=0 ; b< bitmaps.length ; b++ ){

            try {

                // CREATE FILE NAME
                File imageFile = null;
                while (imageFile == null || imageFile.exists()) {

                    imagesNames[b] = NewsDb.DATA_DIRECTORY + System.currentTimeMillis() + "-" + b + ".jpg";
                    imageFile = new File(imagesNames[b]);
                }

                // CREATE FILE
                if( !imageFile.getParentFile().exists()  ){
                    imageFile.getParentFile().mkdirs();
                }
                Boolean created = imageFile.createNewFile();


                if (created == true) {
                    if (bitmaps[b] != null) {

                        FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                        bitmaps[b].compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
                        fileOutputStream.close();
                    }
                }

            }catch (IOException e){
                e.printStackTrace();
            }
        }

        /*
        // PREPARE IMAGESNAMES
        String[] imagesNames = Article.saveImages(article);
        StringBuilder imagesNamesStringBuilder = new StringBuilder();
        for(String imageName : imagesNames ){
            imagesNamesStringBuilder.append( imageName+"," );
        }
        article.setImagesFileName(imagesNamesStringBuilder.toString());
        */


        return  imagesNames;
    }


}
