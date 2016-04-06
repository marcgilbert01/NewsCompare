package marc.newscompare.UI;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import marc.newscompare.R;
import marc.newscompare.api.Article;
import marc.newscompare.dao.NewsDb;

public class ArticleFragment extends Fragment {

    private static final String ARG_ARTICLE = "article";
    private Article mArticle;
    private OnFragmentInteractionListener mListener;

    ViewPager viewPagerArticle;


    public ArticleFragment() {
        // Required empty public constructor
    }


    public static ArticleFragment newInstance(Article article) {

        ArticleFragment fragment = new ArticleFragment();
        Bundle args = new Bundle();
        args.putSerializable( ARG_ARTICLE, article );
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mArticle = (Article) getArguments().getSerializable(ARG_ARTICLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate( R.layout.fragment_article, container, false);
        viewPagerArticle = (ViewPager) view.findViewById(R.id.viewPagerArticle);
        viewPagerArticle.setAdapter( new ArticlePagerAdapter(mArticle) );

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabLayoutViewPagerArticle);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPagerArticle);


        return view;
    }


    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    public interface OnFragmentInteractionListener {

        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);


    }



    class ArticlePagerAdapter extends PagerAdapter {

        Article article;

        public ArticlePagerAdapter(Article article) {
            this.article = article;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            Article articleToDisplay = article;
            if( position>0 ){
                articleToDisplay = article.getMatchingArticles().get(position-1);
            }

            ViewGroup layout = (ViewGroup) LayoutInflater.from( container.getContext() ).inflate(R.layout.article_large , container, false);
            // TITLE
            TextView textViewTitle = (TextView) layout.findViewById(R.id.textViewLargeArticleTitle);
            textViewTitle.setText( article.getTitle() );
            // PHOTO
            String fileName =null;
            if( (fileName = article.getImagesFilesNames()[0]) != null ){
                ImageView imageViewOne = (ImageView) layout.findViewById(R.id.imageViewOneLargeArticle);
                imageViewOne.setImageURI( Uri.fromFile(new File( fileName )) );
            }
            // DESCRIPTION
            TextView textViewDescription = (TextView) layout.findViewById(R.id.textViewLargeArticleDescription);
            textViewDescription.setText( Html.fromHtml(article.getDescription()) );
            // KEYWORDS
            if( article.getKeywords()!=null ) {
                StringBuilder stringBuilder = new StringBuilder();
                for (String string : article.getKeywords()) {
                   stringBuilder.append(string+",");
                }
                TextView textViewKeywords = (TextView) layout.findViewById(R.id.textViewLargeArticlesKeywords);
                textViewKeywords.setText( stringBuilder.toString() );
            }

            container.addView(layout);


            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {

            return 1 + article.getMatchingArticles().size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return   view ==object ;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            String pageTitle = "";

            if( position==0 ){
                pageTitle = String.valueOf(article.getId());
            }
            else{
                pageTitle = String.valueOf( article.getMatchingArticles().get(position-1).getId() );
            }

            return pageTitle;
        }
    }





}
