package com.blaszt.socialmediasaver2.main;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.blaszt.socialmediasaver2.R;
import com.blaszt.socialmediasaver2.helper.ui.NotificationHelper;
import com.blaszt.socialmediasaver2.logger.CrashCocoExceptionHandler;
import com.blaszt.socialmediasaver2.services.URLHandler;
import com.blaszt.socialmediasaver2.services.URLService;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

import java.util.List;

public final class MainActivity extends AppCompatActivity {
    private interface OnNavigationVisibilityChangeListener {
        void onNavigationVisibilityChange(boolean visible);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    changePage(new HomeFragment());
                    return true;
                case R.id.navigation_gallery:
                    changePage(new GalleryFragment());
                    return true;
                case R.id.navigation_modules:
                    changePage(new ModulesFragment());
                    return true;
            }
            return false;
        }
    };

    private FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener;

    private OnNavigationVisibilityChangeListener mOnNavigationVisibilityChangeLister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new CrashCocoExceptionHandler("sms"));

        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            String url = intent.getStringExtra(Intent.EXTRA_TEXT);
            intent = new Intent(MainActivity.this, URLHandler.class);
            intent.setAction(URLHandler.ACTION_HANDLE_URL);
            intent.putExtra(URLHandler.EXTRA_URL, url);
            startService(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        stopURLService();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        setNavigationOnBackStackChanged(navigation);
        setNavigationVisibilityListener(navigation);

        initFragment(navigation);

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new BaseMultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        super.onPermissionsChecked(report);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        super.onPermissionRationaleShouldBeShown(permissions, token);
                        Toast.makeText(MainActivity.this, "We need these permissions", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .check();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationHelper.cancelAll(this);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (!isFragmentValid(fragment) || !((BaseFragment) fragment).onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsetNavigationOnBackStackChanged();
    }

    private void stopURLService() {
        Intent intent = new Intent(this, URLService.class);
        stopService(intent);
    }

    public void setNavigationVisibility(boolean visible) {
        if (mOnNavigationVisibilityChangeLister != null) {
            mOnNavigationVisibilityChangeLister.onNavigationVisibilityChange(visible);
        }
    }

    private void setNavigationVisibilityListener(final BottomNavigationView navigation) {
        mOnNavigationVisibilityChangeLister = new OnNavigationVisibilityChangeListener() {
            @Override
            public void onNavigationVisibilityChange(boolean visible) {
                navigation.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        };
    }

    private void setNavigationOnBackStackChanged(final BottomNavigationView navigation) {
        mOnBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
            int tempBackStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();

            @Override
            public void onBackStackChanged() {
                int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();

                // There is a change in the back stack.
                if (backStackEntryCount != tempBackStackEntryCount) {
                    // The type of change is "POP"
                    if (backStackEntryCount < tempBackStackEntryCount ) {
                        String fragmentName = backStackEntryCount > 0 ? getSupportFragmentManager().getBackStackEntryAt(backStackEntryCount - 1).getName() : HomeFragment.class.getSimpleName();
                        int itemId = 0;
                        if (GalleryFragment.class.getSimpleName().equals(fragmentName)) {
                            itemId = R.id.navigation_gallery;
                        } else if (HomeFragment.class.getSimpleName().equals(fragmentName)) {
                            itemId = R.id.navigation_home;
                        } else if (ModulesFragment.class.getSimpleName().equals(fragmentName)) {
                            itemId = R.id.navigation_modules;
                        }

                        if (itemId != 0) {
                            navigation.getMenu().findItem(itemId).setChecked(true);
                        }
                    } else {
                        // The type of change is "PUSH"
                    }
                    tempBackStackEntryCount = backStackEntryCount;
                }
            }
        };
        getSupportFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);
    }

    private void unsetNavigationOnBackStackChanged() {
        if (mOnBackStackChangedListener != null) {
            getSupportFragmentManager().removeOnBackStackChangedListener(mOnBackStackChangedListener);
            mOnBackStackChangedListener = null;
        }
    }

    private void initFragment(BottomNavigationView navigation) {
        Fragment fragment = new HomeFragment();
        changePage(fragment, false);
        navigation.getMenu().findItem(R.id.navigation_home).setChecked(true);
    }

    private void changePage(Fragment fragment) {
        changePage(fragment, true);
    }

    private void changePage(Fragment fragment, boolean addToBackStack) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (isFragmentValid(fragment) && !fragment.getClass().isInstance(currentFragment)) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment);
            if (addToBackStack) {
                transaction.addToBackStack(fragment.getClass().getSimpleName());
            }
            transaction.commit();
        }
    }

    private boolean isFragmentValid(Fragment fragment) {
//        String fragmentName = fragment.getClass().getSimpleName();
        return fragment != null &&
                (fragment instanceof GalleryFragment ||
                 fragment instanceof HomeFragment ||
                 fragment instanceof ModulesFragment);
    }

}
