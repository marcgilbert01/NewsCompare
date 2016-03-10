package marc.newscompare.NewsApi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by gilbertm on 10/03/2016.
 */
public abstract class ArticlesLoader {


    abstract public void loadArticles( long maxAge , ArticleLoaderCallBack articleLoaderCallBack);

    interface ArticleLoaderCallBack{

        public void onArticlesLoaded(Article[] articles);

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




}
