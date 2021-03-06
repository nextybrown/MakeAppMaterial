package com.example.xyzreader.ui.articledetail;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.Article;
import com.example.xyzreader.data.source.ArticlesRepository;
import com.example.xyzreader.data.source.local.ArticleLocalDataSoure;
import com.example.xyzreader.data.source.remote.ArticleRemoteDataSource;
import com.example.xyzreader.ui.article.ArticleContract;
import com.example.xyzreader.ui.article.ArticleListActivity;
import com.example.xyzreader.ui.customviews.ImageLoaderHelper;
import com.example.xyzreader.utils.AppExecutors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements ArticleDetailContract.View {
    private static final String TAG = "ArticleDetailFragment";
    private static final String ARG_ARTICLE = "article";
    private static final float PARALLAX_FACTOR = 1.25f;
    @BindView(R.id.scrollview)
    NestedScrollView mScrollView;
    //    @BindView(R.id.draw_insets_frame_layout)
//    DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    @BindView(R.id.photo_container)
    View mPhotoContainerView;
    @BindView(R.id.photo)
    ImageView mPhotoView;
    @BindView(R.id.share_fab)
    FloatingActionButton actionButton;
    @BindView(R.id.article_title)
    TextView titleView;
    @BindView(R.id.article_byline)
    TextView bylineView;
    @BindView(R.id.article_body)
    TextView bodyView;
    @BindView(R.id.toolbar_detail)
    Toolbar toolbar;
    private Cursor mCursor;
    private long mItemId;
    private Article article;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private DetailFragmentPresenter mFragmentPresenter;
    private ColorDrawable mStatusBarColorDrawable;
    private int mTopInset;
    private int mScrollY;
    private int mStatusBarFullOpacityBottom;
    private boolean mIsCard = false;


    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {


    }


    public static ArticleDetailFragment newInstance(Article article) {
        Bundle arguments = new Bundle();
        // arguments.putLong(ARG_ITEM_ID, itemId);
        arguments.putParcelable(ARG_ARTICLE, article);


        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ARTICLE)) {
            article = (Article) getArguments().getParcelable(ARG_ARTICLE);
        }
        mFragmentPresenter = new DetailFragmentPresenter(ArticlesRepository.getInstance(
                ArticleRemoteDataSource.getNewInstance(new AppExecutors()),
                ArticleLocalDataSoure.getNewInstance(getActivity().getContentResolver(), new AppExecutors())),
                this);

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, mRootView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPhotoView.setTransitionName(article.get_ID());
        }
        getActivityCast().setSupportActionBar(toolbar);

//        mDrawInsetsFrameLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
//            @Override
//            public void onInsetsChanged(Rect insets) {
//                mTopInset = insets.top;
//            }
//        });

        mScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollX) {
//                    mScrollY = mScrollView.getScrollY();
//                    getActivityCast().onUpButtonFloorChanged(mItemId, ArticleDetailFragment.this);
//                    mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
//                    updateStatusBar();

                }
            }
        });

        mStatusBarColorDrawable = new ColorDrawable(0);

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });
        bindViews();
        updateStatusBar();
        mFragmentPresenter.getArticleContent(article.get_ID());
        return mRootView;
    }

    private void updateStatusBar() {
        int color = 0;
        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
            float f = progress(mScrollY,
                    mStatusBarFullOpacityBottom - mTopInset * 3,
                    mStatusBarFullOpacityBottom - mTopInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mMutedColor) * 0.9),
                    (int) (Color.green(mMutedColor) * 0.9),
                    (int) (Color.blue(mMutedColor) * 0.9));
        }
        mStatusBarColorDrawable.setColor(color);
        // mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
    }

    private Date parsePublishedDate() {
        try {
            String date = article.getPUBLISHED_DATE();
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }
        bylineView.setMovementMethod(new LinkMovementMethod());
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));
        mRootView.setAlpha(0);
        mRootView.setVisibility(View.VISIBLE);
        mRootView.animate().alpha(1);
        titleView.setText(article.getTITLE());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPhotoView.setTransitionName(article.get_ID());
        }
        bodyView.setText(Html.fromHtml(article.getBODY().toLowerCase()));
        Date publishedDate = parsePublishedDate();
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + article.getAUTHOR()
                            + "</font>"));

        } else {
            // If date is before 1902, just show the string
            bylineView.setText(Html.fromHtml(
                    outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                            + article.getAUTHOR()
                            + "</font>"));

        }
        //bodyView.setText(Html.fromHtml(article.getBODY().replaceAll("(\r\n|\n)", "<br />")));
        ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                .get(article.getPHOTO_URL(), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        Bitmap bitmap = imageContainer.getBitmap();
                        if (bitmap != null) {
                            Palette p = Palette.generate(bitmap, 12);
                            mMutedColor = p.getDarkMutedColor(0xFF333333);
                            mPhotoView.setImageBitmap(imageContainer.getBitmap());
                            updateStatusBar();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });

    }


    public int getUpButtonFloor() {
        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return mIsCard
                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
                : mPhotoView.getHeight() - mScrollY;

    }

    @Override
    public void setPresenter(ArticleContract.Presenter presenter) {

    }

    @Override
    public void setLoadingIndicator(boolean active) {

    }

    @Override
    public void showArticleContent(String Content) {
        Log.d("ShowArticleContent...", Content);
        bodyView.setText(Html.fromHtml(Content));

    }
}
