package com.y3seker.bilmuhduyuru.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.y3seker.bilmuhduyuru.R;
import com.y3seker.bilmuhduyuru.models.Annc;
import com.y3seker.bilmuhduyuru.ui.RevealEffect;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.codetail.widget.RevealFrameLayout;

/**
 * Created by Yunus Emre Şeker on 02.05.2015.
 * --
 */
public class ContentActivity extends AppCompatActivity implements ViewTreeObserver.OnGlobalLayoutListener {

    @Bind(R.id.ac_content_card)
    CardView contentCard;
    @Bind(R.id.ac_toolbar_title)
    TextView toolbarTitle;
    @Bind(R.id.ac_content)
    TextView content;
    @Bind(R.id.ac_toolbar_sub_title)
    TextView toolbarSubTitle;
    @Bind(R.id.ac_toolbar)
    Toolbar toolbar;
    @Bind(R.id.ac_reveal_layout)
    RevealFrameLayout revealFrameLayout;
    @Bind(R.id.ac_root_container)
    RelativeLayout root;
    @Bind(R.id.ac_toolbar_container)
    RelativeLayout toolbarContainer;

    Annc annc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(getIntent().getBooleanExtra("isLightTheme", true) ?
                R.style.ContentLight : R.style.ContentDark);
        setContentView(R.layout.activity_content);
        ButterKnife.bind(this);
        annc = getIntent().getParcelableExtra("annc");

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentActivity.this.onBackPressed();
            }
        });

        toolbarTitle.setText(annc.getTitle());
        toolbarSubTitle.setText(annc.getDateString());

        content.setMovementMethod(LinkMovementMethod.getInstance());
        content.setText(Html.fromHtml(annc.getContent()));

        getViewTreeObserver().addOnGlobalLayoutListener(this);

    }


    @Override
    public void onGlobalLayout() {
        if (Build.VERSION.SDK_INT >= 16) {
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        RevealEffect.revealFrom((toolbarContainer.getRight() + toolbarContainer.getLeft()) / 2,
                toolbarContainer.getHeight() / 2, toolbarContainer, 600, new RevealEffect.AnimationListener() {
                    @Override
                    public void onAnimEnd() {

                    }
                });
        animateContent();
    }


    protected ViewTreeObserver getViewTreeObserver() {
        return toolbarContainer.getViewTreeObserver();
    }

    private void animateContent() {
        contentCard.setAlpha(0);
        contentCard.setVisibility(View.VISIBLE);
        contentCard.setTranslationY(100);
        contentCard.animate()
                .translationY(0)
                .alpha(1)
                .setDuration(90)
                .start();
    }

    @Override
    public void onBackPressed() {
        root.animate()
                .translationY(root.getHeight())
                .setDuration(400)
                .alpha(0.7f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        root.setVisibility(View.GONE);
                        finish();
                        overridePendingTransition(0, 0);
                    }
                })
                .start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.content, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ac_share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String title = toolbarTitle.getText().toString();
                shareIntent.putExtra(Intent.EXTRA_TEXT, title + "\n" + annc.getShortUrl());
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
                return true;
            case R.id.ac_inbrowser:
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(annc.getShortUrl()));
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
