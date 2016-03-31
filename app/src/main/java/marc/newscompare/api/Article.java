package marc.newscompare.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
    String imagesFileNameStr;
    String imagesUrlStr;
    List<String> keywords = new ArrayList<>();
    long date;
    NewsPaper newsPaper;
    String matchingArticlesIds = null;
    List<Article> matchingArticles = new ArrayList<>();


    public enum NewsPaper{

        THE_GUARDIAN( new TheGuardianArticlesLoader() ),
        THE_DAILY_MAIL( new TheDailyMailArticlesLoader() );

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

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
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

    public String getImagesFileNameStr() {
        return imagesFileNameStr;
    }

    public void setImagesFileNameStr(String imagesFileNameStr) {
        this.imagesFileNameStr = imagesFileNameStr;
    }

    public String[] getImagesFilesNames() {

        String[] imagesFileName = this.imagesFileNameStr.split(",");

        return imagesFileName;
    }

    public void setImagesFilesNames(String[] imagesFileName) {

        // PREPARE IMAGESNAMES
        StringBuilder imagesNamesStringBuilder = new StringBuilder();
        for(String imageName : imagesFileName ){
            imagesNamesStringBuilder.append( imageName+"," );
        }
        this.imagesFileNameStr = imagesNamesStringBuilder.toString();
    }


    public String getImagesUrlStr() {
        return imagesUrlStr;
    }

    public void setImagesUrlStr(String imagesUrlStr) {
        this.imagesUrlStr = imagesUrlStr;
    }

    public String[] getImagesUrls(){

        String[] imagesUrls = imagesUrlStr.split(",");
        return imagesUrls;
    }


    public void setImagesUrls( String[] imagesUrls ){

        StringBuilder stringBuilder = new StringBuilder();
        for(String imageUrl : imagesUrls ){
            stringBuilder.append( imageUrl + ",");
        }
        this.imagesUrlStr = stringBuilder.toString();
    }

    public List<Article> getMatchingArticles() {
        return matchingArticles;
    }

    public void setMatchingArticles(List<Article> matchingArticles) {
        this.matchingArticles = matchingArticles;
    }

    public String getMatchingArticlesIds() {
        return matchingArticlesIds;
    }

    public void setMatchingArticlesIds(String matchingArticlesIds) {
        this.matchingArticlesIds = matchingArticlesIds;
    }
}
