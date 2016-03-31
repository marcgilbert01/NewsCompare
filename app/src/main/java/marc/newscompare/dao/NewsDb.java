package marc.newscompare.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import marc.newscompare.api.Article;

/**
 * Created by gilbertm on 10/03/2016.
 */
public class NewsDb extends SQLiteOpenHelper {

    static private final String NEWS_DB_NAME = "news.db";
    static private final String ARTICLES_TABLE_NAME = "articles";
    static private final String KEYWORDS_TABLE_NAME = "keywords";

    public NewsDb(Context context , File dbDirectory) {
        super(context, dbDirectory+"/"+NEWS_DB_NAME, null, 1);
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
                "matchingArticlesIds INTEGER," +
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
                            "       '" + article.getMatchingArticlesIds() + "' AS matchingArticlesIds," +
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
                            "       '" + article.getMatchingArticlesIds() + "'," +
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

            // IS A KEYWORD
            if( cursor.getInt(0)==previousArticleId ){
                Article article = articles.get(articles.size()-1);
                article.getKeywords().add( cursor.getString( 11 ) );
            }
            // IS AN ARTICLE
            else {

                Article article = readArticle(cursor);
                if (includeKeywords){
                    article.getKeywords().add(cursor.getString(11));
                }
                articles.add(article);
                previousArticleId = article.getId();
            }

        }

        return articles;
    }




    public List<Article> getArticles(Integer[] articlesIds ){

        List<Article> articles = new ArrayList<>();

        if( articlesIds!=null && articlesIds.length>0 ){

            StringBuilder stringBuilder = new StringBuilder();
            for(int i=0 ; i<articlesIds.length-1 ; i++){
                stringBuilder.append( articlesIds[i]+"," );
            }
            stringBuilder.append( articlesIds[ articlesIds.length-1 ] );

            String idListStr = stringBuilder.toString();

            SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + ARTICLES_TABLE_NAME + " " +
                    "WHERE id IN ("+idListStr+") " +
                    "ORDER BY " + ARTICLES_TABLE_NAME + ".id", null);
            //WHERE first_name IN ('Sarah', 'Jane', 'Heather');

            while( cursor.moveToNext() ){

                Article article = readArticle(cursor);
                articles.add(article);
            }
        }

        return articles;
    }





    public void clearArticles( Long beforeDate ){

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL( "DELETE * FROM "+ARTICLES_TABLE_NAME+" WHERE date<"+beforeDate );
        sqLiteDatabase.close();
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




/*
        SELECT t1.ID
        FROM Table1 t1
        LEFT JOIN Table2 t2 ON t1.ID = t2.ID
        WHERE t2.ID IS NULL
*/
//  GET ARTICLES WHICH HAVE NOT GOT ANY KEYWORDS
    public List<Article> getArticlesWithNoKeywords(){

        List<Article> articles = new ArrayList<>();

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery( "SELECT * FROM "+ARTICLES_TABLE_NAME+" " +
                "LEFT OUTER JOIN "+KEYWORDS_TABLE_NAME+" " +
                "ON "+ARTICLES_TABLE_NAME+".id = "+KEYWORDS_TABLE_NAME+".articleId " +
                "WHERE "+KEYWORDS_TABLE_NAME+".articleId IS NULL"
                ,null);

        if( cursor!=null && cursor.getCount()>0 ) {

            articles = new ArrayList<>();
            while (cursor.moveToNext()) {

               Article article = readArticle(cursor);
               articles.add(article);
            }
        }

        return articles;
    }


    //  GET ARTICLES WHICH HAVE KEYWORDS
    public List<Article> getArticlesWithKeywords(){

        List<Article> articles = null;

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery( "SELECT * FROM "+ARTICLES_TABLE_NAME+" " +
                "LEFT OUTER JOIN "+KEYWORDS_TABLE_NAME+" " +
                "ON "+ARTICLES_TABLE_NAME+".id = "+KEYWORDS_TABLE_NAME+".articleId " +
                "WHERE "+KEYWORDS_TABLE_NAME+".articleId IS NOT NULL"
                ,null);


        articles = new ArrayList<>();

        int previousArticleId = -1;
        while( cursor.moveToNext() ) {

            // IS A KEYWORD
            if( cursor.getInt(0)==previousArticleId ){
                Article article = articles.get(articles.size()-1);
                article.getKeywords().add( cursor.getString( 11 ) );
            }
            // IS AN ARTICLE
            else {

                Article article = readArticle(cursor);
                article.getKeywords().add(cursor.getString(11));
                articles.add(article);
                previousArticleId = article.getId();
            }

        }


        return articles;
    }



    public void deleteArticles(Long olderThan){

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        sqLiteDatabase.execSQL( "DELETE FROM "+KEYWORDS_TABLE_NAME+" WHERE articleId IN " +
                "( SELECT id FROM "+ARTICLES_TABLE_NAME+" WHERE createdAt<"+olderThan+" )" );

        sqLiteDatabase.execSQL( "DELETE FROM "+ARTICLES_TABLE_NAME+" WHERE createdAt<"+olderThan );

        sqLiteDatabase.close();

    }



    // METHOD WILL RETURNED ARTICLES WHO HAVE 2 OR MORE OF THE KEYWORDS PROVIDED
    //
    public List<Article> getMatchingArticles( List<String> keywords , int nbMatchingKeywords ){

        List<Article> matchingArticles = new ArrayList<>();

        StringBuilder stringBuilderKeywords = new StringBuilder();
        stringBuilderKeywords.append( " (" );
        for(int k=0 ; k<keywords.size()-1 ; k++ ){
            stringBuilderKeywords.append("'"+keywords.get(k)+"' ," );
        }
        stringBuilderKeywords.append("'"+keywords.get(keywords.size()-1)+"')" );
        //    ('Sarah', 'Jane', 'Heather');

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + ARTICLES_TABLE_NAME + " " +
                "LEFT OUTER JOIN " + KEYWORDS_TABLE_NAME + " " +
                "ON " + ARTICLES_TABLE_NAME + ".id = " + KEYWORDS_TABLE_NAME + ".articleId " +
                "WHERE keyword IN " + stringBuilderKeywords.toString() + " " +
                "ORDER BY " + ARTICLES_TABLE_NAME + ".id", null);


        List<Article> articles = new ArrayList<>();
        int previousArticleId = -1;
        while( cursor.moveToNext() ) {

            // IS A KEYWORD
            if( cursor.getInt(0)==previousArticleId ){

                Article article = articles.get(articles.size()-1);
                article.getKeywords().add( cursor.getString( 11 ) );
            }
            // IS AN ARTICLE
            else {

                Article article = readArticle(cursor);
                article.getKeywords().add(cursor.getString(11));
                articles.add(article);
                previousArticleId = article.getId();
            }

            // DETECT IF MATCHING (WE USE EQUAL SO IT IS NOT ADDED TWICE)
            Article article = articles.get(articles.size()-1);
            if( article.getKeywords().size()==nbMatchingKeywords ){
                matchingArticles.add(article);
            }

        }

        return matchingArticles;
    }



    public List<Article> getMatchingArticles(Article article , int nbMatchingKeywords){

        List<Article> matchingArticles = new ArrayList<>();

        if( article.getKeywords()!=null && article.getKeywords().size()>0 ){

            matchingArticles = getMatchingArticles(article.getKeywords() , nbMatchingKeywords );
            int a = 0;
            while( a<matchingArticles.size() ){
                if( matchingArticles.get(a).getId() == article.getId() ){
                    matchingArticles.remove(a);
                    a = matchingArticles.size();
                }
                a++;
            }
        }

        return matchingArticles;
    }








    private Article readArticle(Cursor cursor) {
        Article article = new Article();
        article.setId(cursor.getInt(0));
        article.setTitle(cursor.getString(1));
        article.setDescription(cursor.getString(2));
        article.setText(cursor.getString(3));
        article.setAuthor(cursor.getString(4));
        article.setImagesFileNameStr(cursor.getString(5));
        article.setNewsPaper(Article.NewsPaper.values()[cursor.getInt(6)]);
        article.setMatchingArticlesIds(cursor.getString(7));
        article.setDate(cursor.getLong(8));

        return article;
    }



    public void updateMatchingArticles(Article article) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("UPDATE " + ARTICLES_TABLE_NAME + " SET " +
                "matchingArticlesIds = '" + article.getMatchingArticlesIds() + "' " +
                "WHERE id = " + article.getId());
        // UPDATE COMPANY SET ADDRESS = 'Texas' WHERE ID = 6;
        sqLiteDatabase.close();

    }


    public List<Article> getArticlesWithMatchingArticles( Long dateFrom , Article.NewsPaper newsPaper ){

        List<Article> articles = new ArrayList<>();

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + ARTICLES_TABLE_NAME + " " +
                "WHERE matchingArticlesIds !='null' AND " +
                "date>"+dateFrom+" AND " +
                "newsPaper=" + newsPaper.ordinal() + " " +
                "ORDER BY id", null);

        while( cursor.moveToNext() ){
            articles.add( readArticle(cursor) );
        }

        sqLiteDatabase.close();

        return articles;
    }



}
