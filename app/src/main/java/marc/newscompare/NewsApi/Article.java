package marc.newscompare.NewsApi;

import android.graphics.Bitmap;
import android.media.Image;

/**
 * Created by gilbertm on 10/03/2016.
 */
public class Article {

    int id;
    String title;
    String description;
    String text;
    long date;
    String[] categories;
    String[] keywords;
    Bitmap[] bitmaps;
    String author;
    NewsPaper newsPaper;

    enum NewsPaper{

        THE_GUARDIAN,
        THE_TELEGRAPHE,
        THE_DAILY_MAIL

    }


}
