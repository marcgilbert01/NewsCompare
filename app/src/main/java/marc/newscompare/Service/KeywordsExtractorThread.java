package marc.newscompare.Service;

/**
 * Created by gilbertm on 16/03/2016.
 */
public class KeywordsExtractorThread extends Thread{

    Boolean exit = false;


    @Override
    public void run() {
        super.run();


        // GET ARTICLES FROM ARTICLES DB WHICH DON'T HAVE KEYWORDS


        // GET KEYWORDS FROM YAHOO API


        // SAVE KEYWORDS TO KEYWORDS DB




    }



    public void stopThread(){

        exit = true;
        interrupt();
    }

}
