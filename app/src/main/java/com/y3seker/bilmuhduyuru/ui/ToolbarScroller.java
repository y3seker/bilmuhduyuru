package com.y3seker.bilmuhduyuru.ui;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

/**
 * Created by Yunus Emre Şeker on 31.03.2015.
 * -
 */
public class ToolbarScroller extends RecyclerView.OnScrollListener {

    private Toolbar toolbar;
    private int scrollOffset = 0;

    public ToolbarScroller(Toolbar toolbar1) {
        toolbar = toolbar1;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        scroll(dy);
        int topRowVerticalPosition =
                (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
        LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
        //swipeRefreshLayout.setEnabled(llm.findFirstCompletelyVisibleItemPosition() == 0);
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            //Log.i(TAG,"t " + scrollOffset + " H " + toolbar.getHeight());
            if (scrollOffset > (toolbar.getHeight() / 2))
                collapseToolbar();
            else
                expandToolbar();

        }
    }

    private void scroll(int dy) {
        scrollOffset += dy;
        int toolbarHeight = toolbar.getHeight();

        if (scrollOffset > toolbarHeight)
            scrollOffset = toolbarHeight;

        if (scrollOffset < 0)
            scrollOffset = 0;

        toolbar.setTranslationY(-scrollOffset);
    }

    private void collapseToolbar() {
        toolbar.animate().translationY(-toolbar.getHeight());
        scrollOffset = toolbar.getHeight();
    }

    private void expandToolbar() {
        toolbar.animate().translationY(0);
        scrollOffset = 0;
    }


}
