package marc.newscompare.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import marc.newscompare.dao.NewsDb;

/**
 * Created by gilbertm on 10/03/2016.
 */
public abstract class ArticlesLoader {


    List<Article> articles = new ArrayList<>();


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



    abstract public List<Article> getNewArticles(List<Article> existingArticles);



}
