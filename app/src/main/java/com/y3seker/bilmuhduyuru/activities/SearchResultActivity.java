package com.y3seker.bilmuhduyuru.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.y3seker.bilmuhduyuru.PreferencesHelper;
import com.y3seker.bilmuhduyuru.R;
import com.y3seker.bilmuhduyuru.database.DatabaseHelper;
import com.y3seker.bilmuhduyuru.models.Annc;
import com.y3seker.bilmuhduyuru.models.AnncProxy;
import com.y3seker.bilmuhduyuru.ui.NewRVAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Yunus Emre Şeker on 14.11.2015.
 * -
 */
public class SearchResultActivity extends AppCompatActivity {

    private static final String TAG = SearchResultActivity.class.getSimpleName();

    List<Annc> list;
    DatabaseHelper databaseHelper;
    @Bind(R.id.search_rv)
    RecyclerView rv;
    @Bind(R.id.search_toolbar)
    Toolbar toolbar;
    private NewRVAdapter mAdapter;
    SearchView searchView;

    String _query = "";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferencesHelper.get(this);
        boolean isLightTheme = sharedPreferences.getBoolean(PreferencesHelper.THEME, true);
        setTheme(isLightTheme ?
                R.style.MainLight : R.style.MainDark);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        databaseHelper = new DatabaseHelper(this);
        list = new ArrayList<>();
        rv.setHasFixedSize(true);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setLayoutManager(new LinearLayoutManager(this));
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
            }
        });
        rv.setAdapter(mAdapter);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            _query = query;
            doSearch(query);
        }
    }

    private void doSearch(String query) {
        boolean searchInContent = sharedPreferences.getBoolean(PreferencesHelper.SEARCH_IN_CONTENT, false);
        Cursor searchCursor = databaseHelper.search(query, searchInContent);
        list.clear();
        if (searchCursor.getCount() == 0) {
            mAdapter.clearList();
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.search_no_result), Snackbar.LENGTH_LONG).show();
            searchCursor.close();
            return;
        }
        for (int i = 0; i < searchCursor.getCount(); i++) {
            Annc a = new AnncProxy(searchCursor).getAnnc();
            if (a != null) list.add(a);
            searchCursor.moveToNext();
        }
        searchCursor.close();
        mAdapter.changeList(list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQuery(_query, false);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }
}
