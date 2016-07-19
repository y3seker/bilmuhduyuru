package com.y3seker.bilmuhduyuru.activities;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.y3seker.bilmuhduyuru.PreferencesHelper;
import com.y3seker.bilmuhduyuru.R;
import com.y3seker.bilmuhduyuru.Utils;
import com.y3seker.bilmuhduyuru.connection.RestClient;
import com.y3seker.bilmuhduyuru.database.DatabaseHelper;
import com.y3seker.bilmuhduyuru.gcm.RegistrationIntentService;
import com.y3seker.bilmuhduyuru.models.Annc;
import com.y3seker.bilmuhduyuru.models.AnncProxy;
import com.y3seker.bilmuhduyuru.ui.NewRVAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "BilmuhDuyuru";
    private static final int PER_ANNC_COUNT = 15;
    private static final int SETTINGS_REQUEST_CODE = 22121;

    private SearchView searchView = null;

    @Bind(R.id.my_recycler_view)
    RecyclerView mRecyclerView;
    @Bind(R.id.swipeView)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.main_toolbar)
    Toolbar toolbar;
    @Bind(R.id.main_progressbar)
    ProgressBar progressBar;

    private NewRVAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<Annc> itemList = new ArrayList<>();
    private List<Annc> allItemsList = new ArrayList<>();

    private DatabaseHelper db;
    private Cursor dbCursor;
    private SharedPreferences sharedPreferences;

    private int lastIndex = -1;
    private int lastCursorPosition = 0;
    private View sneakbarView;
    private boolean isFirstTime = true;
    public static boolean isLightTheme = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferencesHelper.get(this);
        isLightTheme = sharedPreferences.getBoolean(PreferencesHelper.THEME, true);
        setTheme(isLightTheme ?
                R.style.MainLight : R.style.MainDark);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        db = new DatabaseHelper(this);

        isFirstTime = sharedPreferences.getBoolean(PreferencesHelper.FIRST_RUN, true);
        sneakbarView = findViewById(android.R.id.content);

        setupRefreshLayout();
        setupToolbar();
        setupRecyclerView();

        if (isFirstTime) {
            getFeedForFirstTime();
        } else {
            setupFeed();
        }

        if (!PreferencesHelper.isGcmRegistered(this) && Utils.checkPlayServices(this)) {
            Intent registerService = new Intent(this, RegistrationIntentService.class);
            startService(registerService);
        }

    }

    private void setupFeed() {
        progressBar.setVisibility(View.GONE);
        dbCursor = db.getAllRows();
        loadFeed();
    }

    private void getFeedForFirstTime() {
        RestClient.get().getAll(new Callback<ArrayList<Annc>>() {
            @Override
            public void success(final ArrayList<Annc> anncs, Response response) {
                itemList = anncs.subList(0, 20);
                progressBar.setVisibility(View.GONE);
                mAdapter.changeList(itemList);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        db.write(anncs);
                        dbCursor = db.getAllRows();
                        dbCursor.moveToPosition(PER_ANNC_COUNT);
                    }
                }).start();
                sharedPreferences.edit().putBoolean(PreferencesHelper.FIRST_RUN, false).apply();
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setEnabled(false);
                makeThemedSnackBar(R.string.db_con_err, Snackbar.LENGTH_LONG, R.string.try_again, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getFeedForFirstTime();
                    }
                });
                sharedPreferences.edit().putBoolean(PreferencesHelper.FIRST_RUN, true).apply();
            }
        });
    }

    private void setupToolbar() {
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
    }

    private void setupRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(new RecyclerViewToolbarScroller(toolbar) {
            @Override
            public void onLoadMore() {
                loadFeed();
            }
        });
        mAdapter = new NewRVAdapter(this, R.layout.main_listrow);
        mAdapter.setOnItemClickListener(new NewRVAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final View v, final Annc annc, int pos) {
                final Intent intent = new Intent(getBaseContext(), ContentActivity.class);
                intent.putExtra("annc", annc);
                intent.putExtra("isLightTheme", sharedPreferences.getBoolean(PreferencesHelper.THEME, true));
                startActivity(intent);
                overridePendingTransition(0, 0);
            }

            @Override
            public void onItemLongClick(View v, Annc annc, int pos) {
                showLongPressDialog(annc);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    private void loadFeed() {
        if (dbCursor == null || dbCursor.isClosed())
            return;

        for (int i = 0; i < PER_ANNC_COUNT; i++) {
            if (!dbCursor.isLast()) {
                Annc annc = new AnncProxy(dbCursor).getAnnc();
                if (annc != null) {
                    itemList.add(annc);
                    mAdapter.addItem(annc);
                    mAdapter.notifyItemInserted(itemList.size() - 1);
                }
            } else {
                return;
            }
            dbCursor.moveToNext();
        }
    }

    private void setupRefreshLayout() {

        swipeRefreshLayout.setColorSchemeResources(R.color.ateal400, R.color.red500);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white);
        swipeRefreshLayout.setProgressViewOffset(false, 112, 200);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                lastIndex = db.getLastIndex();
                RestClient.get().getNewer(lastIndex,
                        new Callback<ArrayList<Annc>>() {
                            @Override
                            public void success(ArrayList<Annc> anncs, Response response) {
                                int message = R.string.yeni_duyuru_yok;
                                if (anncs.size() > 0) {
                                    lastIndex = anncs.get(0).getIndex();
                                    if (itemList.size() != 0)
                                        itemList.addAll(0, anncs);
                                    if (allItemsList.size() != 0)
                                        allItemsList.addAll(0, anncs);
                                    mAdapter.addList(anncs);
                                    mLayoutManager.scrollToPosition(0);
                                    db.write(anncs);
                                    message = R.string.duyuru_guncel;
                                }
                                makeThemedSnackBar(message, Snackbar.LENGTH_SHORT, 0, null);
                                swipeRefreshLayout.setRefreshing(false);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                makeThemedSnackBar(R.string.con_error, Snackbar.LENGTH_LONG, 0, null);
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
            }
        });
    }

    private void makeThemedSnackBar(int message, int length, int actionMessage, @Nullable View.OnClickListener actionListener) {
        Snackbar snackbar = Snackbar.make(sneakbarView, message, length);
        if (actionMessage != 0)
            snackbar.setAction(actionMessage, actionListener);
        View sv = snackbar.getView();
        sv.setBackgroundColor(getResources().getColor(isLightTheme ? R.color.grey900 : R.color.grey100));
        TextView tv = (TextView) sv.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(isLightTheme ? Color.WHITE : Color.BLACK);
        snackbar.show();
    }

    private void showLongPressDialog(final Annc annc) {

        new AlertDialog.Builder(this)
                .setItems(R.array.long_press_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(annc.getUrl()));
                                startActivity(i);
                                break;
                            case 1:
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT, annc.getTitle() + "\n" + annc.getShortUrl());
                                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share)));
                                break;
                        }
                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                toolbar.setNavigationIcon(null);
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.onActionViewCollapsed();
                toolbar.setNavigationIcon(null);
                lastCursorPosition = dbCursor.getPosition();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivityForResult(new Intent(this, Settings.class), SETTINGS_REQUEST_CODE);
                return true;
            case android.R.id.home:
                toolbar.setNavigationIcon(null);
                searchView.onActionViewCollapsed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLightTheme != sharedPreferences.getBoolean(PreferencesHelper.THEME, true)) {
            Log.d(TAG, "Theme Changed");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    recreate();
                    overridePendingTransition(0, 0);
                }
            }, 1);
        }
        if (dbCursor != null && dbCursor.isClosed()) {
            Log.d(TAG, "Cursor is closed, restore");
            dbCursor = db.getAllRows();
            dbCursor.moveToPosition(lastCursorPosition);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    recreate();
                    overridePendingTransition(0, 0);
                }
            }, 1);

        if (requestCode == Utils.PLAY_SERVICES_RESOLUTION_REQUEST && resultCode == RESULT_OK
                && !PreferencesHelper.isGcmRegistered(this))
            if (PreferencesHelper.isGcmRegistered(this) && Utils.checkPlayServices(this)) {
                Log.i(TAG, "onActivityResult, register");
                Intent registerService = new Intent(this, RegistrationIntentService.class);
                startService(registerService);
            }
    }

    @Override
    protected void onStop() {
        if (dbCursor != null)
            dbCursor.close();
        db.close();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public abstract class RecyclerViewToolbarScroller extends RecyclerView.OnScrollListener {
        private Toolbar toolbar;
        private int scrollOffset = 0;
        int pastVisiblesItems, visibleItemCount, totalItemCount;

        public RecyclerViewToolbarScroller(Toolbar toolbar1) {
            toolbar = toolbar1;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            scroll(dy);
            LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();

            visibleItemCount = llm.getChildCount();
            totalItemCount = llm.getItemCount();
            pastVisiblesItems = llm.findFirstVisibleItemPosition();
            if (llm.findLastVisibleItemPosition() > mAdapter.getItemCount() - 3) {
                onLoadMore();
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                //Log.i(TAG,"t " + scrollOffset + " H " + toolbar.getHeight());
                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (llm.findFirstCompletelyVisibleItemPosition() != 1 && scrollOffset > (toolbar.getHeight() / 2)) {
                    collapseToolbar();
                } else
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

        public abstract void onLoadMore();
    }


}
