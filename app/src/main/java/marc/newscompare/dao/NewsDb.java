package marc.newscompare.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;

import org.apache.commons.lang.StringEscapeUtils;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


    public List<Article> getArticles(Long dateFrom, Article.NewsPaper newsPaper , Boolean includeKeywords) {

        List<Article> articles = null;

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = null;
        if (newsPaper == null) {
            if( includeKeywords==false ) {
                cursor = sqLiteDatabase.query(ARTICLES_TABLE_NAME, null, "date>" + dateFrom, null, null, null, null);
            }
            else {
                cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + ARTICLES_TABLE_NAME + " " +
                        "LEFT OUTER JOIN " + KEYWORDS_TABLE_NAME + " " +
                        "ON " + ARTICLES_TABLE_NAME + ".id = " + KEYWORDS_TABLE_NAME + ".articleId " +
                        "WHERE date>" + dateFrom + " " +
                        "ORDER BY " + ARTICLES_TABLE_NAME + ".id", null);
            }
        }
        else {
            if( includeKeywords==false ) {
                cursor = sqLiteDatabase.query(ARTICLES_TABLE_NAME, null, "date>" + dateFrom + " AND newsPaper=" + newsPaper.ordinal(), null, null, null, null);
            }
            else {
                cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + ARTICLES_TABLE_NAME + " " +
                        "LEFT OUTER JOIN " + KEYWORDS_TABLE_NAME + " " +
                        "ON " + ARTICLES_TABLE_NAME + ".id = " + KEYWORDS_TABLE_NAME + ".articleId " +
                        "WHERE date>" + dateFrom + " AND newsPaper=" + newsPaper.ordinal() + " " +
                        "ORDER BY " + ARTICLES_TABLE_NAME + ".id", null);
            }
        }
        articles = new ArrayList<>();






        int previousArticleId = -1;
        while( cursor.moveToNext() ) {
/*
System.out.println( cursor.getInt(0) +"," +
                    cursor.getString(1) +", " +
                    cursor.getString(2) +"," +
                    cursor.getString(3) +", " +
                    cursor.getString(4) +", " +
                    cursor.getString(5) +", " +
                    cursor.getInt(6) +", " +
                    cursor.getInt(7) +", " +
                    cursor.getInt(8) +", " +

                    cursor.getInt(9) +", " +
                    cursor.getString(10) +", " +
                    cursor.getInt(11) +", "
);
*/
            // IS A KEYWORD
            if( cursor.getInt(0)==previousArticleId ){
                Article article = articles.get(articles.size()-1);
                article.getKeywords().add( cursor.getString( 10 ) );
            }
            // IS AN ARTICLE
            else {
                Article article = new Article();
                article.setId(cursor.getInt(0));
                article.setTitle(cursor.getString(1));
                article.setDescription(cursor.getString(2));
                article.setText(cursor.getString(3));
                article.setAuthor(cursor.getString(4));
                article.setImagesFileNameStr(cursor.getString(5));
                article.setNewsPaper(Article.NewsPaper.values()[cursor.getInt(6)]);
                article.setDate(cursor.getLong(7));
                if (includeKeywords){
                    article.getKeywords().add(cursor.getString(10));
                }
                articles.add(article);
                previousArticleId = article.getId();
            }

        }

        return articles;
    }









    public void clearArticles( Long beforeDate ){

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL( "DELETE * FROM "+ARTICLES_TABLE_NAME+" WHERE date<"+beforeDate );
        sqLiteDatabase.close();
    }



    public List<Article> getArticlesWithNoKeywords(){

        List<Article> articles = null;

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery( "SELECT * FROM "+ARTICLES_TABLE_NAME+" " +
                                "LEFT OUTER JOIN "+KEYWORDS_TABLE_NAME+" " +
                                "ON "+ARTICLES_TABLE_NAME+".id = "+KEYWORDS_TABLE_NAME+".articleId " +
                                "WHERE "+KEYWORDS_TABLE_NAME+".articleId = NULL"
                               ,null);

        if( cursor!=null && cursor.getCount()>0 ) {

            articles = new ArrayList<>();
            while (cursor.moveToNext()) {

                Article article = new Article();
                //article.setId( cursor. );

            }




        }






/*
        SELECT t1.ID
        FROM Table1 t1
        LEFT JOIN Table2 t2 ON t1.ID = t2.ID
        WHERE t2.ID IS NULL
*/


        return articles;
    }


    public void saveKeywords(List<Article> articles ) {

        if (articles != null && articles.size() > 0) {

            class Keyword{
                String keywordStr;
                int articleId;
            }
            List<Keyword> keywords = new ArrayList<>();
            for( Article article : articles ) {
                if( article.getKeywords()!=null && article.getKeywords().size()>0 ) {

                    for (String keywordStr : article.getKeywords() ) {
                        Keyword keyword = new Keyword();
                        keyword.keywordStr = keywordStr;
                        keyword.articleId  = article.getId();
                        keywords.add(keyword);
                    }
                }
            }

            // SAVE TO DB
            if( keywords!=null && keywords.size()>0 ) {

                Keyword keyword = keywords.get(0);

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("INSERT INTO '" + KEYWORDS_TABLE_NAME + "'");
                stringBuilder.append("SELECT " +
                        "NULL                      AS id, " +
                        "'" + keyword.keywordStr + "' AS keyword, " +
                        "'" + keyword.articleId  + "' AS articleId ");

                if( keywords.size()>1 ) {

                    for (int k=1 ; k<keywords.size() ; k++ ) {

                        keyword = keywords.get(k);
                        stringBuilder.append("UNION ALL SELECT " +
                                "       NULL ," +
                                "       '" + keyword.keywordStr + "'," +
                                "       '" + keyword.articleId + "' ");
                    }
                }
                String sql = stringBuilder.toString();
                SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
                sqLiteDatabase.execSQL(sql);
                sqLiteDatabase.close();
            }
        }

    }







}
