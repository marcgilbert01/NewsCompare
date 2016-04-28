package marc.newscompare.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import yahooapi.contentAnalysis.YahooContentAnalysisApi;

/**
 * Created by gilbertm on 28/04/2016.
 */
public class KeywordsExtractor {

    static YahooContentAnalysisApi yahooContentAnalysis = new YahooContentAnalysisApi();


    public List<Article> extractKeywords(List<Article> articles ){

        for(Article article  : articles ){
            List<String> keywords = extractKeywords( article.getDescription() );
            article.setKeywords( keywords );
        }
        return articles;
    }


    public List<String> extractKeywords(String text){

        List<String> keywordList = null;

        String[] keywords = yahooContentAnalysis.getKeywords( text );
        if( keywords!=null ) {
           keywordList = new ArrayList<>(Arrays.asList(keywords));
        }
        return keywordList;
    }




}
