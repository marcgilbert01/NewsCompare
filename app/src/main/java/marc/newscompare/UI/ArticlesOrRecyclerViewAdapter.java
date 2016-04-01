package marc.newscompare.UI;

import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import marc.marcviews.OpenRightView;
import marc.marcviews.OpenRightViewHolder;
import marc.newscompare.api.Article;

/**
 * Created by gilbertm on 01/04/2016.
 */
public class ArticlesOrRecyclerViewAdapter extends OpenRightView.OpenRightAdapter<ArticlesOrRecyclerViewAdapter.ArticleViewHolder>{


    List<Article> articles;


    public ArticlesOrRecyclerViewAdapter(List<Article> articles) {
        this.articles = articles;
    }


    @Override
    public Fragment getFragment(int i) {
        return null;
    }

    @Override
    public ArticleViewHolder onCreateOpenRightViewHolder(ViewGroup viewGroup, int i) {


        return null;

    }

    @Override
    public void onBindOpenRightViewHolder(ArticleViewHolder articleViewHolder, int i) {
    }


    @Override
    public int getItemCount() {
        return 0;
    }


    class ArticleViewHolder extends OpenRightViewHolder{


        public ArticleViewHolder(View itemView) {
            super(itemView);
        }

    }



}
