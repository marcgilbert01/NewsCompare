package marc.newscompare.UI;

import android.app.Fragment;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import marc.marcviews.OpenRightView;
import marc.marcviews.OpenRightViewHolder;
import marc.newscompare.R;
import marc.newscompare.api.Article;

/**
 * Created by gilbertm on 01/04/2016.
 */
public class ArticlesOpenRightViewAdapter extends OpenRightView.OpenRightAdapter<ArticlesOpenRightViewAdapter.ArticleViewHolder>{


    List<Article> articles;


    public ArticlesOpenRightViewAdapter(List<Article> articles) {
        this.articles = articles;
    }


    @Override
    public Fragment getFragment(int i) {
        return null;
    }

    @Override
    public ArticleViewHolder onCreateOpenRightViewHolder(ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.from( viewGroup.getContext() ).inflate(R.layout.article_small ,viewGroup);
        ArticleViewHolder articleViewHolder = new ArticleViewHolder(itemView);

        return articleViewHolder;
    }

    @Override
    public void onBindOpenRightViewHolder(ArticleViewHolder articleViewHolder, int i) {

        Article article = articles.get(i);
        // DESCRIPTION
        articleViewHolder.textViewPlusNumber.setText(article.getDescription());
        // PHOTO 1
        String fileName =null;
        if( (fileName = article.getImagesFilesNames()[0]) != null ){
            articleViewHolder.imageViewArticlePhoto.setImageURI( Uri.fromFile(new File(fileName)) );
        }

    }


    @Override
    public int getItemCount() {
        return articles.size();
    }


    class ArticleViewHolder extends OpenRightViewHolder{

        TextView  textViewArticleDescription;
        ImageView imageViewArticlePhoto;
        ImageView imageViewArticleLogo1;
        ImageView imageViewArticleLogo2;
        TextView  textViewPlusNumber;

        public ArticleViewHolder(View itemView) {
            super(itemView);

            textViewArticleDescription = (TextView) itemView.findViewById(R.id.textViewArticleDescription);
            imageViewArticlePhoto = (ImageView) itemView.findViewById(R.id.imageViewArticlePhoto);
            imageViewArticleLogo1 = (ImageView) itemView.findViewById(R.id.imageViewLogo1);
            imageViewArticleLogo2 = (ImageView) itemView.findViewById(R.id.imageViewLogo2);
            textViewPlusNumber    = (TextView)  itemView.findViewById(R.id.textViewPlusNumber);
        }

    }



}
