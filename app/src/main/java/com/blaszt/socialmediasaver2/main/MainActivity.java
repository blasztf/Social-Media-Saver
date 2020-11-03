package com.blaszt.socialmediasaver2.main;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.blaszt.socialmediasaver2.R;
import com.blaszt.socialmediasaver2.data.SMSContent;
import com.blaszt.socialmediasaver2.helper.ui.NotificationHelper;
import com.blaszt.socialmediasaver2.helper.ui.RecyclerViewCompat;
import com.blaszt.socialmediasaver2.logger.CrashCocoExceptionHandler;
import com.blaszt.socialmediasaver2.plugin.ModPlugin;
import com.blaszt.socialmediasaver2.plugin.ModPluginEngine;
import com.blaszt.socialmediasaver2.service.URLHandler;
import com.blaszt.socialmediasaver2.service.URLService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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

        // Url was shared from other app, handle the url.
        if (isSharedFromOtherApp()) return;

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

    private boolean isSharedFromOtherApp() {
        ModPlugin selectedPlugin = null;
        final int selectedPluginIndex;
        int index = -1;
        String url = null;
        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            url = intent.getStringExtra(Intent.EXTRA_TEXT);

            for (ModPlugin plugin : ModPluginEngine.getInstance(this).each()) {
                index++;
                if (plugin.isURLValid(url)) {
                    selectedPlugin = plugin;
                    break;
                }
            }

            if (selectedPlugin != null) {
                selectedPluginIndex = index;
                final AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Loading")
                        .setMessage("Fetching media urls...")
                        .setPositiveButton(null, null)
                        .setNegativeButton(null, null)
                        .setNeutralButton(null,  null)
                        .create();
                dialog.show();
                selectedPlugin.use(this).fetchMediaURLs(url, new ModPlugin.ModPluginListener() {
                    @Override
                    public void onPluginComplete(String[] mediaURLs) {
                        dialog.cancel();
                        if (mediaURLs.length != 0) {
                            if (mediaURLs.length == 1) {
                                downloadMedia(mediaURLs[0]);
                                finish();
                            }
                            else {
                                mediaChooser(mediaURLs);
                            }
                        }
                    }

                    private void downloadMedia(String url) {
                        Intent intent = new Intent(MainActivity.this, URLHandler.class);
                        intent.setAction(SMSContent.Intent.ACTION_DOWNLOAD_MEDIA);
                        intent.putExtra(SMSContent.Intent.EXTRA_MOD_PLUGIN_INDEX, selectedPluginIndex);
                        intent.putExtra(SMSContent.Intent.EXTRA_MEDIA_URL, new String[] { url });
                        MainActivity.this.startService(intent);
                    }

                    private void mediaChooser(final String[] urls) {
                        RecyclerView media;
                        LinearLayoutManager layoutManager;
                        Dialog dialog = new Dialog(MainActivity.this);
                        dialog.setContentView(R.layout.fragment_gallery);
                        media = dialog.findViewById(R.id.listImage);
                        layoutManager = new GridLayoutManager(MainActivity.this, 2, LinearLayoutManager.VERTICAL, false);
                        media.setLayoutManager(layoutManager);
                        media.addOnItemTouchListener(new RecyclerViewCompat.OnItemClickListener(media) {
                            @Override
                            public void onItemClick(View view, int position) {
                                downloadMedia(urls[position]);
                            }
                        });
                        media.setAdapter(new RecyclerView.Adapter() {
                            class Holder extends RecyclerView.ViewHolder {
                                private ImageView imView;
                                private RequestOptions opts;

                                public Holder(@NonNull View itemView) {
                                    super(itemView);
                                    double[] size = getDisplaySize();
                                    imView = (ImageView) itemView;
                                    opts = new RequestOptions()
                                            .override((int) size[0] / 2, (int) size[1] / 2)
                                            .centerCrop()
                                            .placeholder(android.R.drawable.ic_menu_gallery);
                                }

                                private double[] getDisplaySize() {
                                    DisplayMetrics metrics = getResources().getDisplayMetrics();
                                    double width = metrics.widthPixels / metrics.density;
                                    double height = metrics.heightPixels / metrics.density;
                                    return new double[] { width, height };
                                }
                            }

                            @NonNull
                            @Override
                            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                                return new Holder(new ImageView(viewGroup.getContext()));
                            }

                            @Override
                            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                                Holder holder = (Holder) viewHolder;
                                Glide.with(holder.itemView).asBitmap().apply(holder.opts).load(urls[i]).into(holder.imView);
                            }

                            @Override
                            public int getItemCount() {
                                return urls.length;
                            }
                        });
                        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        });
                        dialog.setCanceledOnTouchOutside(true);
                        dialog.setCancelable(true);
                        dialog.show();
                    }
                });
            }
            else {
                finish();
            }

            return true;
        }

        return false;
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
