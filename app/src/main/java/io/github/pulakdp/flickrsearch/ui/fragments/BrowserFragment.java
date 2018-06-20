package io.github.pulakdp.flickrsearch.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.pulakdp.flickrsearch.BuildConfig;
import io.github.pulakdp.flickrsearch.MyIdlingResource;
import io.github.pulakdp.flickrsearch.R;
import io.github.pulakdp.flickrsearch.adapter.PhotoAdapter;
import io.github.pulakdp.flickrsearch.adapter.SearchHistoryAdapter;
import io.github.pulakdp.flickrsearch.data.FlickrApiClient;
import io.github.pulakdp.flickrsearch.data.FlickrApiInterface;
import io.github.pulakdp.flickrsearch.data.RecentSearch;
import io.github.pulakdp.flickrsearch.data.SearchHistory;
import io.github.pulakdp.flickrsearch.model.Photo;
import io.github.pulakdp.flickrsearch.model.PhotoResponse;
import io.github.pulakdp.flickrsearch.ui.activities.BrowserActivity;
import io.github.pulakdp.flickrsearch.utils.FlickrSearchUtil;
import io.github.pulakdp.flickrsearch.utils.PreferenceUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author: PulakDebasish
 */

public class BrowserFragment extends android.support.v4.app.Fragment
        implements SearchView.OnQueryTextListener,
        PhotoAdapter.OnReloadClickListener,
        SearchHistoryAdapter.OnHistoryItemClickListener {

    public static final int PAGE_SIZE = 20;

    @BindView(R.id.photo_list)
    RecyclerView recyclerView;

    @BindView(R.id.loading_progress)
    ProgressBar progressBar;

    @BindView(R.id.empty_layout)
    LinearLayout emptyLayout;

    @BindView(R.id.error_text)
    TextView errorText;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.history_view)
    RecyclerView historyView;

    private MyIdlingResource idlingResource;

    private SearchView searchView;
    private boolean isLastPage = false;
    private boolean isLoading = false;
    private int currentPage = 1;
    private StaggeredGridLayoutManager layoutManager;
    private PhotoAdapter adapter;
    private FlickrApiInterface apiService;
    private String query;
    public List<Photo> list;
    private BrowserActivity activity;

    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource()
    {
        if (idlingResource == null)
        {
            idlingResource = new MyIdlingResource();
        }
        return idlingResource;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (BrowserActivity) activity;
    }

    public void saveSearchAndAddToDatabase(String query) {
        if (!doesQueryExistInDatabase(query))
            SearchHistory.getInstance(activity).addSearchString(query);
        RecentSearch.getInstance(activity).addRecentSearch(query, list);
    }

    public boolean doesQueryExistInDatabase(String query) {
        return SearchHistory.getInstance(activity).getRecentSearches().contains(query);
    }

    public List<Photo> getPhotosForQuery(String query) {
        return RecentSearch.getInstance(activity).getPhotosForQuery(query);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_browser, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((BrowserActivity) activity).setSupportActionBar(toolbar);

        setUpRecyclerView();

        apiService = FlickrApiClient.getClient().create(FlickrApiInterface.class);

        emptyLayout.setVisibility(View.VISIBLE);
    }

    private void setUpRecyclerView() {
        adapter = new PhotoAdapter((BrowserActivity) activity);
        if (list != null && list.size() > 0) {
            adapter.addAll(list);
            Log.d("list", "init");
        } else {
            list = new ArrayList<>();
        }
        adapter.setOnReloadClickListener(this);

        layoutManager = new StaggeredGridLayoutManager(
                PreferenceUtil.getInstance(getContext()).getPhotoGridSize(getContext()),
                StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(onPhotoScrollListener);
    }

    private RecyclerView.OnScrollListener onPhotoScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int[] firstVisibleItemPosition = layoutManager.findFirstVisibleItemPositions(null);

            if (!isLoading && !isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition[0]) >= totalItemCount
                        && firstVisibleItemPosition[0] >= 0
                        && totalItemCount >= PAGE_SIZE) {
                    loadMoreItems();
                }
            }
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);

        MenuItem gridSizeItem = menu.findItem(R.id.action_grid_size);
        setUpGridSizeMenu(gridSizeItem.getSubMenu());
        //noinspection deprecation
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                showAndUpdateHistory();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                historyView.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.VISIBLE);
                if (adapter.getItemCount() > 0) {
                    adapter.clear();
                    recyclerView.setVisibility(View.GONE);
                }
                return true;
            }
        });
        searchView.setMaxWidth(Integer.MAX_VALUE);
        if (query != null && !query.isEmpty()) {
            MenuItemCompat.expandActionView(searchItem);
            searchView.setQuery(query, false);
        }
        searchView.setOnQueryTextListener(this);
    }

    private void showAndUpdateHistory() {
        ArrayList<String> history = SearchHistory.getInstance(getContext()).getRecentSearches();
        if (history.size() > 0) {
            emptyLayout.setVisibility(View.GONE);
            historyView.setAdapter(new SearchHistoryAdapter(history, BrowserFragment.this));
            historyView.setLayoutManager(new LinearLayoutManager(getContext()));
            historyView.setVisibility(View.VISIBLE);
        }
    }

    private void setUpGridSizeMenu(@NonNull SubMenu gridSizeMenu) {
        switch (getGridSize()) {
            case 2:
                gridSizeMenu.findItem(R.id.action_grid_size_2).setChecked(true);
                break;
            case 3:
                gridSizeMenu.findItem(R.id.action_grid_size_3).setChecked(true);
                break;
            case 4:
                gridSizeMenu.findItem(R.id.action_grid_size_4).setChecked(true);
                break;
        }
    }

    protected int getGridSize() {
        return PreferenceUtil.getInstance(activity).getPhotoGridSize(activity);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int gridSize = 0;
        switch (item.getItemId()) {
            case R.id.action_grid_size_2:
                gridSize = 2;
                break;
            case R.id.action_grid_size_3:
                gridSize = 3;
                break;
            case R.id.action_grid_size_4:
                gridSize = 4;
                break;
        }
        if (gridSize > 0) {
            item.setChecked(true);
            setAndSaveGridSize(gridSize);
        }
        return super.onOptionsItemSelected(item);

    }

    private void setAndSaveGridSize(int gridSize) {
        saveGridSize(gridSize);
        setUpRecyclerView();
    }

    protected void saveGridSize(int gridSize) {
        PreferenceUtil.getInstance(activity).setPhotoGridSize(gridSize);
    }

    private Callback<PhotoResponse> firstFetchCallback = new Callback<PhotoResponse>() {
        @Override
        public void onResponse(@NonNull Call<PhotoResponse> call, @NonNull Response<PhotoResponse> response) {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            isLoading = false;

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                if (responseCode == 504) { // 504 Unsatisfiable Request (only-if-cached)
                    errorText.setText("Can't load data.\nCheck your network connection.");
                    errorText.setVisibility(View.VISIBLE);
                }
                return;
            }
            //noinspection ConstantConditions
            PhotoResponse photoResponse = response.body();
            if (photoResponse != null) {
                List<Photo> photos = photoResponse.getPhotos().getPhotoList();
                if (photos != null) {
                    if (photos.size() > 0) {
                        adapter.clear();
                        list.clear();
                        adapter.addAll(photos);
                        list.addAll(photos);
                        saveSearchAndAddToDatabase(query);
                    }

                    if (photos.size() >= PAGE_SIZE) {
                        adapter.addFooter();
                    } else {
                        isLastPage = true;
                    }
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (idlingResource != null) {
                            idlingResource.setIdleState(true);
                        }
                    }
                }, 5000);
            }
        }

        @Override
        public void onFailure(@NonNull Call<PhotoResponse> call, @NonNull Throwable t) {
            errorText.setVisibility(View.VISIBLE);
            if (!call.isCanceled()) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);

                if (t instanceof ConnectException
                        || t instanceof UnknownHostException
                        || t instanceof SocketTimeoutException
                        || t instanceof IOException) {
                    errorText.setText("Can't load data.\nCheck your network connection.");
                    errorText.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    private Callback<PhotoResponse> nextFetchCallback = new Callback<PhotoResponse>() {
        @Override
        public void onResponse(@NonNull Call<PhotoResponse> call, @NonNull Response<PhotoResponse> response) {
            adapter.removeFooter();
            isLoading = false;

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                switch (responseCode) {
                    case 504: // 504 Unsatisfiable Request (only-if-cached)
                        break;
                    case 400:
                        isLastPage = true;
                        break;
                }
                return;
            }
            PhotoResponse photoResponse = response.body();
            if (photoResponse != null) {
                List<Photo> photos = photoResponse.getPhotos().getPhotoList();
                if (photos != null) {
                    if (photos.size() > 0) {
                        adapter.addAll(photos);
                        list.addAll(photos);
                    }

                    if (photos.size() >= PAGE_SIZE) {
                        adapter.addFooter();
                    } else {
                        isLastPage = true;
                    }
                }
            }
        }

        @Override
        public void onFailure(@NonNull Call<PhotoResponse> call, @NonNull Throwable t) {
            if (!call.isCanceled()) {
                if (t instanceof ConnectException
                        || t instanceof UnknownHostException
                        || t instanceof SocketTimeoutException
                        || t instanceof IOException) {
                    adapter.updateFooter(PhotoAdapter.FooterType.ERROR);
                }
            }
        }
    };

    private void loadMoreItems() {
        isLoading = true;

        currentPage++;

        if (FlickrSearchUtil.hasInternetConnection(getContext()))
            makeNextFetchofPhotos();
        else {
            Toast.makeText(getContext(), R.string.need_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private void makeFirstFetchofPhotos() {

        Call<PhotoResponse> call = apiService.searchPhotos(BuildConfig.FLICKR_API_KEY,
                query,
                currentPage,
                PAGE_SIZE);
        call.enqueue(firstFetchCallback);
    }

    private void makeNextFetchofPhotos() {
        Call<PhotoResponse> call = apiService.searchPhotos("d7851ed778d72d3fa38ea9af777bb5a9",
                query,
                currentPage,
                PAGE_SIZE);
        call.enqueue(nextFetchCallback);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        historyView.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if (idlingResource  != null)
            idlingResource.setIdleState(false);

        if (query != null && !query.isEmpty()) {
            this.query = query.trim();
        }
        hideSoftKeyboard(activity);

        if (FlickrSearchUtil.hasInternetConnection(getContext()))
            makeFirstFetchofPhotos();
        else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            isLoading = false;
            List<Photo> photos = getPhotosForQuery(this.query);
            if (photos != null && photos.size() > 0) {
                adapter.clear();
                adapter.addAll(photos);
                list.clear();
                list.addAll(photos);
            }
            Toast.makeText(getContext(), R.string.loading_from_cache, Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (query.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            if (adapter.getItemCount() != 0)
                showAndUpdateHistory();
        }
        this.query = query;
        return false;
    }

    public void hideSoftKeyboard(@Nullable Activity activity) {
        if (activity != null) {
            View currentFocus = activity.getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                }
            }
        }
        if (searchView != null)
            searchView.clearFocus();
    }

    @Override
    public void onReloadClick() {
        adapter.updateFooter(PhotoAdapter.FooterType.LOAD_MORE);
        makeNextFetchofPhotos();
    }

    @Override
    public void onHistoryItemClick(String query) {
        this.query = query;
        searchView.setQuery(this.query, true);
    }
}
