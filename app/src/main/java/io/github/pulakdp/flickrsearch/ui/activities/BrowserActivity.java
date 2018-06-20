package io.github.pulakdp.flickrsearch.ui.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import io.github.pulakdp.flickrsearch.R;
import io.github.pulakdp.flickrsearch.ui.fragments.BrowserFragment;

public class BrowserActivity extends AppCompatActivity {

    public static final String BROWSER_FRAGMENT = "browser_fragment";

    private BrowserFragment browserFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        FragmentManager fm = getSupportFragmentManager();

        browserFragment = (BrowserFragment) fm.findFragmentByTag(BROWSER_FRAGMENT);

        if (browserFragment == null) {
            browserFragment = new BrowserFragment();
            fm.beginTransaction().add(R.id.fragment_container, browserFragment, BROWSER_FRAGMENT).commit();
        }
    }

    public BrowserFragment getFragment() {
        return browserFragment;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
