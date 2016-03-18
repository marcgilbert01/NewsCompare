package marc.newscompare.api;

import android.graphics.Bitmap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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



    public static String saveImage(Bitmap bitmap) {

        String fileName = null;

        if( bitmap!=null ){

            try {
                // CREATE FILE NAME
                File imageFile = null;
                while (imageFile == null || imageFile.exists()) {
                    fileName = NewsDb.DATA_DIRECTORY + System.currentTimeMillis() + ".jpg";
                    imageFile = new File(fileName);
                    Thread.sleep(50);
                }
                // CREATE FILE
                if (!imageFile.getParentFile().exists()) {
                    imageFile.getParentFile().mkdirs();
                }
                Boolean created = imageFile.createNewFile();
                if (created == true) {
                    FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
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


}