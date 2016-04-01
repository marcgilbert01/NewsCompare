package marc.newscompare.api;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gilbertm on 10/03/2016.
 */
public abstract class ArticlesLoader {

    static List<Article> articles = new ArrayList<>();

    static File imageDirectory;

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



    static public String saveImage( Bitmap bitmap ) {

        String fileName = null;

        if( bitmap!=null  && imageDirectory!=null ){

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


    static public void deletesImages(Long olderThan){

        if( imageDirectory!=null ) {

            for (File file : imageDirectory.listFiles()) {
                if (file.lastModified() < olderThan) {
                    file.delete();
                }
            }

        }

    }

    public static File getImageDirectory() {
        return imageDirectory;
    }

    public static void setImageDirectory(File imageDirectory) {
        ArticlesLoader.imageDirectory = imageDirectory;
    }
}

