package marc.newscompare.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.os.Environment;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

import marc.newscompare.api.Article;

/**
 * Created by gilbertm on 10/03/2016.
 */
public class NewsDb extends SQLiteOpenHelper {

    static public final String DATA_DIRECTORY = Environment.getExternalStorageDirectory()+"/NewsCompare/";
    static private final String NEWS_DB_NAME = DATA_DIRECTORY+"news.db";
    static private final String ARTICLES_TABLE_NAME = "articles";
    static private final String KEYWORDS_TABLE_NAME = "keywords";

    public NewsDb(Context context) {
        super(context, NEWS_DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // CREATE TABLE ARTICLES
        db.execSQL(" CREATE TABLE IF NOT EXISTS " + ARTICLES_TABLE_NAME + " ( " +
                "id INTEGER PRIMARY KEY," +
                "title          TEXT," +
                "description    TEXT," +
                "text           TEXT," +
                "author         TEXT," +
                "imagesFileName TEXT," +
                "newsPaper    INTEGER," +
                "date         INTEGER," +
                "createdAt    INTEGER" +
                ")");
        // CREATE KEYWORDS DB
        db.execSQL(" CREATE TABLE IF NOT EXISTS " + KEYWORDS_TABLE_NAME + " ( " +
                "id INTEGER PRIMARY KEY," +
                "keyword      TEXT," +
                "articleId    INTEGER" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void saveArticles(List<Article> articles) {

        if (articles != null && articles.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("INSERT INTO '" + ARTICLES_TABLE_NAME + "'");

            for (int a = 0; a < articles.size() ; a++) {
                Article article = articles.get(a);
                // SAVE TO DB
                if (a == 0) {
                    stringBuilder.append("SELECT " +
                            "       NULL                      AS id," +
                            "       '" + StringEscapeUtils.escapeSql(article.getTitle()) + "'       AS title," +
                            "       '" + StringEscapeUtils.escapeSql(article.getDescription()) + "'  AS description," +
                            "       '" + StringEscapeUtils.escapeSql(article.getText()) + "'         AS text," +
                            "       '" + StringEscapeUtils.escapeSql(article.getAuthor()) + "'       AS author," +
                            "       '" + article.getImagesFileNameStr() + "'     AS imagesFileName," +
                            "       '" + article.getNewsPaper().ordinal() + "' AS newsPaper," +
                            "        " + article.getDate() + "           AS date," +
                            "        " + System.currentTimeMillis() + "  AS createdAt ");
                } else {
                    stringBuilder.append("UNION ALL SELECT " +
                            "       NULL ," +
                            "       '" + StringEscapeUtils.escapeSql(article.getTitle()) + "'," +
                            "       '" + StringEscapeUtils.escapeSql(article.getDescription()) + "'," +
                            "       '" + StringEscapeUtils.escapeSql(article.getText()) + "'," +
                            "       '" + StringEscapeUtils.escapeSql(article.getAuthor()) + "'," +
                            "       '" + article.getImagesFileNameStr() + "'," +
                            "       '" + article.getNewsPaper().ordinal() + "'," +
                            "        " + article.getDate() + "," +
                            "        " + System.currentTimeMillis() + " ");
                }
            }

            String sql = stringBuilder.toString();
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            sqLiteDatabase.execSQL(sql);
            sqLiteDatabase.close();
        }

    }
        /*
          "id INTEGER PRIMARY KEY," +
                "title        TEXT," +
                "description  TEXT," +
                "text         TEXT," +
                "author       TEXT," +
                "imagesFileName   TEXT," +
                "newsPaper    INTEGER," +
                "date         INTEGER," +
                "createdAt    INTEGER" +
        INSERT INTO 'tablename'
          SELECT 'data1' AS 'column1', 'data2' AS 'column2'
UNION ALL SELECT 'data1', 'data2'
UNION ALL SELECT 'data1', 'data2'
UNION ALL SELECT 'data1', 'data2'
        */

    public List<Article> getArticles(Long dateFrom, Article.NewsPaper newsPaper) {

        List<Article> articles = null;

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = null;
        if (newsPaper == null) {
            cursor = sqLiteDatabase.query(ARTICLES_TABLE_NAME, null, "date>" + dateFrom, null, null, null, null);
        }
        else {
            cursor = sqLiteDatabase.query(ARTICLES_TABLE_NAME, null, "date>"+dateFrom+" AND newsPaper="+newsPaper.ordinal(), null, null, null, null);
        }
        articles = new ArrayList<>();
        while (cursor.moveToNext()) {
                Article article = new Article();
                article.setId(cursor.getInt(0));
                article.setTitle(cursor.getString(1));
                article.setDescription(cursor.getString(2));
                article.setText(cursor.getString(3));
                article.setAuthor(cursor.getString(4));
                article.setImagesFileNameStr(cursor.getString(5));
                article.setNewsPaper(Article.NewsPaper.values()[cursor.getInt(6)]);
                article.setDate(cursor.getLong(7));
                articles.add(article);
        }




        return articles;
    }


    public void clearArticles( Long beforeDate ){

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL( "DELETE * FROM "+ARTICLES_TABLE_NAME+" WHERE date<"+beforeDate );
        sqLiteDatabase.close();

    }



}
