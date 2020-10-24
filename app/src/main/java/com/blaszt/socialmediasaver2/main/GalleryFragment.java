package com.blaszt.socialmediasaver2.main;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.blaszt.socialmediasaver2.AppSettings;
import com.blaszt.socialmediasaver2.R;
import com.blaszt.socialmediasaver2.SettingsCompatActivity;
import com.blaszt.socialmediasaver2.data.MediaData;
import com.blaszt.socialmediasaver2.helper.ui.LayoutManagerUtils;
import com.blaszt.socialmediasaver2.helper.ui.RecyclerViewCompat;
import com.blaszt.socialmediasaver2.helper.ui.RecyclerViewUtils;
import com.blaszt.socialmediasaver2.helper.ui.ZoomUtils;
import com.blaszt.socialmediasaver2.view.GIFVideoView;
import com.blaszt.socialmediasaver2.view.TouchImageView;
import com.blaszt.toolkit.util.TStrings;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class GalleryFragment extends BaseFragment {

    private RecyclerView gallery, galleryPager;

    private boolean canGalleryPagerScroll = true;
    private Configuration oldConfig;
    private StaggeredGridLayoutManager galleryGridLayoutManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery_fu, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        oldConfig = new Configuration(getResources().getConfiguration());
        setupGalleryPager();
        setupGallery();

        setupSettingsBtn();
    }

    @Override
    public void onResume() {
        super.onResume();
        String path = AppSettings.getInstance(getActivity()).storageMedia();
        String viewMedia = AppSettings.getInstance(getActivity()).viewMedia();
        if (!"all".equals(viewMedia)) path += File.separator + viewMedia;
        recalculateGalleryGridColumn();
        showImages(
                path,
                AppSettings.getInstance(getActivity()).sortMedia(),
                AppSettings.getInstance(getActivity()).findRecursively()
        );
    }

    @Override
    public boolean onBackPressed() {
        if (galleryPager != null) {
            GalleryAdapter adapter = RecyclerViewUtils.getAdapter(gallery);
            if (adapter != null && ZoomUtils.getInstance(getSupportActivity()).isZoomed(adapter.expandedImage)) {
                galleryPager.performClick();
                return true;
            }
        }
        return super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Orientation changed, recalculate gallery layout manager.
        if (oldConfig.orientation != newConfig.orientation) {
            recalculateGalleryGridColumn();
            oldConfig = new Configuration(newConfig);
        }
    }

    private void setupSettingsBtn() {
        View view = getView();
        if (view != null) {
            FloatingActionButton settingsBtn = view.findViewById(R.id.settingsBtn);
            settingsBtn.setOnClickListener(new View.OnClickListener() {
                /**
                 * Called when a view has been clicked.
                 *
                 * @param v The view that was clicked.
                 */
                @Override
                public void onClick(View v) {
                    Activity activity;
                    if ((activity = getActivity()) != null) {
                        Intent intent = new Intent(getContext(), SettingsCompatActivity.class);
                        intent.putExtra(SettingsCompatActivity.EXTRA_SHOW_INIT_FRAGMENT, true);
                        activity.startActivity(intent);
                    }
                }
            });
        }
    }

    private void setupGalleryPager() {
        final PagerSnapHelper pagerSnapHelper;
        View view = getView();
        if (view != null) {
            pagerSnapHelper = new PagerSnapHelper() {
                int targetSnapPosition;

                @Override
                public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
                    targetSnapPosition = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
                    return targetSnapPosition;
                }

                @Nullable
                @Override
                public View findSnapView(RecyclerView.LayoutManager layoutManager) {
                    View snapView = super.findSnapView(layoutManager);
                    if (snapView != null) {
                        onPageChanged(targetSnapPosition);
                    }
                    return snapView;
                }

                private void onPageChanged(int position) {
                    GalleryPagerAdapter adapter = RecyclerViewUtils.getAdapter(galleryPager);
                    if (adapter != null) {
                        if (position > 0) {
                            // Previous item view.
                            stopGifVideo(adapter.getItemViewHolder(position - 1));
                            resetZoomTouchImage(adapter.getItemViewHolder(position - 1));
                        }

                        if (position < adapter.getItemCount() - 1) {
                            // Next item view.
                            stopGifVideo(adapter.getItemViewHolder(position + 1));
                            resetZoomTouchImage(adapter.getItemViewHolder(position + 1));
                        }

                        // Current item view.
                        startGifVideo(adapter.getItemViewHolder(position));

                    }
                }

                private void startGifVideo(GalleryPagerAdapter.MediaHolder mediaHolder) {
                    if (mediaHolder != null) startGifVideo(mediaHolder.mView);
                }

                private void stopGifVideo(GalleryPagerAdapter.MediaHolder mediaHolder) {
                    if (mediaHolder != null) stopGifVideo(mediaHolder.mView);
                }

                private void resetZoomTouchImage(GalleryPagerAdapter.MediaHolder mediaHolder) {
                    if (mediaHolder != null) resetZoomTouchImage(mediaHolder.mView);
                }

                private void startGifVideo(View itemView) {
                    GIFVideoView gifVideoView;
                    if (itemView instanceof GIFVideoView) {
                        gifVideoView = (GIFVideoView) itemView;
                        if (!gifVideoView.isPlaying()) {
                            gifVideoView.start();
                        }
                    }
                }

                private void stopGifVideo(View itemView) {
                    GIFVideoView gifVideoView;
                    if (itemView instanceof GIFVideoView) {
                        gifVideoView = (GIFVideoView) itemView;
                        if (gifVideoView.isPlaying()) {
                            gifVideoView.stopPlayback();
                        }
                    }
                }

                private void resetZoomTouchImage(View itemView) {
                    TouchImageView touchImageView;
                    if (itemView instanceof TouchImageView) {
                        touchImageView = (TouchImageView) itemView;
                        if (touchImageView.isZoomed()) {
                            touchImageView.resetZoom();
                        }
                    }
                }
            };

            galleryPager = view.findViewById(R.id.zoomPager);
            galleryPager.setLayoutManager(new LinearLayoutManager(getSupportActivity(), LinearLayoutManager.HORIZONTAL, false) {
                @Override
                public boolean canScrollHorizontally() {
                    return canGalleryPagerScroll;
                }
            });
//            galleryPager.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
//                float touchX;
//                int gridX;
//
//                @Override
//                public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
//                    switch (e.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//                            touchX = rv.getX() + e.getRawX();
//                            gridX = rv.getWidth() / 5;
//                            canGalleryPagerScroll = !((touchX > gridX) && (touchX < (rv.getWidth() - gridX)));
//                            break;
//                    }
//                    return super.onInterceptTouchEvent(rv, e);
//                }
//            });

            pagerSnapHelper.attachToRecyclerView(galleryPager);
        }
    }

    private void setupGallery() {
        View view = getView();
        if (view != null) {
            gallery = view.findViewById(R.id.listImage);

            galleryGridLayoutManager = new StaggeredGridLayoutManager(0, StaggeredGridLayoutManager.VERTICAL);
            galleryGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
            calculateGridColumn(galleryGridLayoutManager);

            gallery.setLayoutManager(galleryGridLayoutManager);
            gallery.setItemAnimator(null);
            gallery.addOnItemTouchListener(new RecyclerViewCompat.OnItemClickListener(gallery) {
                GalleryAdapter adapter;

                @Override
                public void onItemLongClick(View view, int position) {
                    lazyLoadAdapter();
                    final MediaData media = adapter.getItem(position);
                    final int finalPosition = position;

                    new AlertDialog.Builder(view.getContext())
                            .setItems(new String[]{
                                "Open file manager",
                                    "Delete " + media.toTypeString(),
                                    "Cancel"
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    switch (i) {
                                        case 0:
                                            askOpenDir(media, finalPosition);
                                            break;
                                        case 1:
                                            askDelete(media, finalPosition);
                                            break;
                                    }
                                }
                            })
                            .show();
                }

                private void askOpenDir(final MediaData media, final int position) {
                    if (getContext() != null) {
                        new AlertDialog.Builder(getContext())
                                .setMessage("Open this " + media.toTypeString() + " in file manager?")
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        askDelete(media, position);
                                    }
                                })
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        Uri uriPath = Uri.parse(new File(media.getPath()).getParent());
                                        intent.setDataAndType(uriPath, "resource/folder");
                                        startActivity(intent);
                                    }
                                })
                                .show();
                    }
                }

                private void askDelete(final MediaData media, final int position) {
                    if (getContext() != null) {
                        new AlertDialog.Builder(getContext())
                                .setMessage("Delete this " + media.toTypeString() + "?")
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String msg;
                                        File file = new File(media.getPath());
                                        if (file.delete()) {
                                            adapter.removeItem(position);
                                            msg = TStrings.capitalize(media.toTypeString()) + " has been deleted";
                                        } else {
                                            msg = "Failed to delete file!";
                                        }
                                        Snackbar.make(Objects.requireNonNull(getView()).getRootView(), msg, Snackbar.LENGTH_LONG).show();
                                    }
                                })
                                .show();
                    }
                }

                private void lazyLoadAdapter() {
                    if (adapter == null) {
                        adapter = RecyclerViewUtils.getAdapter(gallery);
                    }
                }

            });
        }
    }

    private void setFullscreen(boolean fullscreen) {
        WindowManager.LayoutParams attributes = getSupportActivity().getWindow().getAttributes();

        if (fullscreen) {
            attributes.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attributes.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }

        getSupportActivity().getWindow().setAttributes(attributes);

        setNavigationVisibility(!fullscreen);
    }

    private void calculateGridColumn(StaggeredGridLayoutManager gridLayoutManager) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        double width = metrics.widthPixels / metrics.density;
        int column = (int) Math.round(width / 192);
//        int column = (int) Math.round(width / 120);
        gridLayoutManager.setSpanCount(column);
    }

    private void recalculateGalleryGridColumn() {
        calculateGridColumn(galleryGridLayoutManager);
    }

    private void showImages(String path, int sortFlags, boolean recursive) {
        List<MediaData> mediaFiles = getMediaFiles(path, sortFlags, recursive);

        if (!mediaFiles.isEmpty()) {
            GalleryAdapter galleryAdapter = RecyclerViewUtils.getAdapter(gallery);
            if (galleryAdapter == null) {
                galleryAdapter = new GalleryAdapter(mediaFiles);
                gallery.setAdapter(galleryAdapter);
            } else {
                galleryAdapter.dataList.clear();
                galleryAdapter.dataList.addAll(mediaFiles);
                galleryAdapter.notifyDataSetChanged();
            }

            GalleryPagerAdapter galleryPagerAdapter = RecyclerViewUtils.getAdapter(galleryPager);
            if (galleryPagerAdapter == null) {
                galleryPagerAdapter = new GalleryPagerAdapter(mediaFiles);
                galleryPager.setAdapter(galleryPagerAdapter);
            } else {
                galleryPagerAdapter.dataList.clear();
                galleryPagerAdapter.dataList.addAll(mediaFiles);
                galleryPagerAdapter.notifyDataSetChanged();
            }

            RecyclerView.LayoutManager layoutManager = galleryPager.getLayoutManager();
            if (layoutManager != null) {
                int firstVisible = LayoutManagerUtils.getFirstVisiblePosition(layoutManager);
                layoutManager.scrollToPosition(firstVisible <= -1 ? 0 : firstVisible);
            }
        } else {
            GalleryAdapter galleryAdapter = RecyclerViewUtils.getAdapter(gallery);
            GalleryPagerAdapter galleryPagerAdapter = RecyclerViewUtils.getAdapter(galleryPager);
            if (galleryAdapter != null) {
                galleryAdapter.dataList.clear();
                galleryAdapter.notifyDataSetChanged();
            }
            if (galleryPagerAdapter != null) {
                galleryPagerAdapter.dataList.clear();
                galleryPagerAdapter.notifyDataSetChanged();
            }
        }
    }

//    private List<MediaData> getMediaFiles(String path, int pair) {
//        return getMediaFiles(path, pair, false, true);
//    }

    private List<MediaData> getMediaFiles(String path, int pair, boolean recursive) {
        return getMediaFiles(path, pair, recursive, true);
    }

    private List<MediaData> getMediaFiles(String path, final int pair, final boolean recursive, boolean init) {
        File directory = new File(path);
        final List<MediaData> mediaDataList = new ArrayList<>();
        if (directory.exists()) {
            directory.listFiles(new FilenameFilter() {
                MediaData mediaData;
                File checkFile;

                @Override
                public boolean accept(File file, String name) {
                    checkFile = new File(file, name);
                    if (checkFile.isFile() && isMediaFile(name)) {
                        mediaData = new MediaData(name, checkFile.getAbsolutePath(), checkFile.length(), checkFile.lastModified(), pair);
                        mediaDataList.add(mediaData);
                    } else {
                        if (recursive) {
                            mediaDataList.addAll(getMediaFiles(checkFile.getAbsolutePath(), pair, true, false));
                        }
                    }
                    return false;
                }

                private boolean isMediaFile(String name) {
                    int lastIndex = name.lastIndexOf(".");
                    return lastIndex != -1 && name.substring(lastIndex).matches("\\.((jpe?|pn)g|gif|mp4|ts)");
                }
            });

            if (init && pair != 0) {
                Collections.sort(mediaDataList);
            }
        }

        return mediaDataList;
    }

    private class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MediaHolder> {
        private List<MediaData> dataList;
        private ImageView expandedImage = getView() != null ? (ImageView) getView().findViewById(R.id.expandedImage) : null;

        GalleryAdapter(List<MediaData> dataList) {
            this.dataList = dataList;
        }

        @NonNull
        @Override
        public MediaHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_gallery, viewGroup, false);
            return new MediaHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MediaHolder mediaHolder, int position) {
            MediaData polaroid = dataList.get(position);

            mediaHolder.bindPolaroid(polaroid);
            mediaHolder.bindDate(polaroid.getReadableDate());
            mediaHolder.bindType(TStrings.capitalize(polaroid.toTypeString()));
            mediaHolder.bindSize(polaroid.getReadableSize());
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        void removeItem(int position) {
            dataList.remove(position);
            notifyItemRemoved(position);
        }

        MediaData getItem(int position) {
            return dataList.get(position);
        }

        class MediaHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView imageDate, imageSize, imageType;

            MediaHolder(View rootView) {
                super(rootView);
                imageView = rootView.findViewById(R.id.thumbnail);
                imageDate = rootView.findViewById(R.id.date);
                imageSize = rootView.findViewById(R.id.size);
                imageType = rootView.findViewById(R.id.type);

                bindOnClickListener();
            }

            void bindDate(String date) {
                imageDate.setText(date);
            }

            void bindSize(String size) {
                imageSize.setText(size);
            }

            void bindType(String type) {
                imageType.setText(type);
            }

            void bindPolaroid(MediaData polaroid) {
                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .override(galleryPager.getWidth() / LayoutManagerUtils.getSpanCount(galleryPager.getLayoutManager()), galleryPager.getHeight() / LayoutManagerUtils.getSpanCount(galleryPager.getLayoutManager()));

                Glide.with(GalleryFragment.this)
                        .asBitmap()
                        .load(polaroid.getPath())
                        .apply(requestOptions)
                        .into(imageView);
            }

            private void bindOnClickListener() {
                itemView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        zoom(imageView);
                    }
                });
            }

            private void zoom(final View thumbView) {
                int position = getAdapterPosition();
                galleryPager.scrollToPosition(position);
                setFullscreen(true);

                int totalSpan = LayoutManagerUtils.getSpanCount(galleryPager.getLayoutManager());
                RequestOptions requestOptions = new RequestOptions()
                        .dontAnimate()
                        .override(galleryPager.getRootView().getWidth() * totalSpan, galleryPager.getRootView().getHeight() * totalSpan)
                        .placeholder(android.R.drawable.ic_menu_gallery);

                Glide.with(GalleryFragment.this)
                        .asBitmap()
                        .apply(requestOptions)
                        .load(getItem(position).getPath())
                        .into(expandedImage);

                ZoomUtils.getInstance(GalleryFragment.this.getSupportActivity()).zoomIn(thumbView, expandedImage, new ZoomUtils.OnZoomFinishedListener() {
                    @Override
                    public void onZoomFinished() {
                        galleryPager.setVisibility(View.VISIBLE);
                        expandedImage.setVisibility(View.GONE);

                        GalleryPagerAdapter adapter = RecyclerViewUtils.getAdapter(galleryPager);
                        GalleryPagerAdapter.MediaHolder holder;
                        View expandedView = (holder = adapter.getItemViewHolder(LayoutManagerUtils.getFirstVisiblePosition(galleryPager.getLayoutManager()))) != null ? holder.mView : null;
                        if (expandedView instanceof GIFVideoView) {
                            ((GIFVideoView) expandedView).start();
                        }
                    }
                });
                galleryPager.setOnClickListener(new View.OnClickListener() {
                    int position = LayoutManagerUtils.getFirstVisiblePosition(galleryPager.getLayoutManager());

                    @Override
                    public void onClick(final View v) {
                        final GalleryPagerAdapter adapter = RecyclerViewUtils.getAdapter(galleryPager);
                        final int newPosition = LayoutManagerUtils.getFirstVisiblePosition(galleryPager.getLayoutManager());

                        if (position != newPosition) {
                            thumbView.setAlpha(1f);
                            gallery.scrollToPosition(newPosition);

                            final int millis = 150;
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    MediaHolder thumbViewHolder = (MediaHolder) gallery.findViewHolderForLayoutPosition(newPosition);
                                    if (thumbViewHolder != null) {
                                        thumbViewHolder.imageView.setAlpha(0f);
                                        next(thumbViewHolder.imageView, v, adapter, newPosition);
                                    } else {
                                        handler.postDelayed(this, millis);
                                    }
                                }
                            }, millis);
                        } else {
                            next(thumbView, v, adapter, position);
                        }
                    }

                    private void next(View currentThumbView, View v, GalleryPagerAdapter adapter, int newPosition) {
                        View currentExpandedView;
                        currentExpandedView = adapter.getItemViewHolder(newPosition).mView;
                        if (currentExpandedView instanceof GIFVideoView) {
                            ((GIFVideoView) currentExpandedView).stopPlayback();
                        }
//                        currentExpandedView = v;

                        int totalSpan = LayoutManagerUtils.getSpanCount(galleryPager.getLayoutManager());
                        RequestOptions requestOptions = new RequestOptions()
                                .override(galleryPager.getRootView().getWidth() * totalSpan, galleryPager.getRootView().getHeight() * totalSpan)
                                .placeholder(android.R.drawable.ic_menu_gallery);

                        Glide.with(GalleryFragment.this)
                                .load(getItem(newPosition).getPath())
                                .apply(requestOptions)
                                .into(expandedImage);

                        expandedImage.setVisibility(View.VISIBLE);
                        v.setVisibility(View.GONE);
                        ZoomUtils.getInstance(GalleryFragment.this.getSupportActivity()).zoomOut(currentThumbView, expandedImage, new ZoomUtils.OnZoomFinishedListener() {
                            @Override
                            public void onZoomFinished() {
                                setFullscreen(false);
                            }
                        });
                    }
                });
            }

//            private void zoom(final View thumbView) {
//                int position = getAdapterPosition();
//                galleryPager.scrollToPosition(position);
//                setFullscreen(true);
//
//                ZoomUtils.getInstance(GalleryFragment.this.getSupportActivity()).zoomIn(thumbView, galleryPager, new ZoomUtils.OnZoomFinishedListener() {
//                    @Override
//                    public void onZoomFinished() {
//                        GalleryPagerAdapter adapter = RecyclerViewUtils.getAdapter(galleryPager);
//                        View expandedView = adapter.getItemViewItem(LayoutManagerUtils.getFirstVisiblePosition(galleryPager.getLayoutManager()));
//                        if (expandedView instanceof GIFVideoView) {
//                            ((GIFVideoView) expandedView).start();
//                        }
//                    }
//                });
//                galleryPager.setOnClickListener(new View.OnClickListener() {
//                    int position = LayoutManagerUtils.getFirstVisiblePosition(galleryPager.getLayoutManager());
//
//                    @Override
//                    public void onClick(final View v) {
//                        final GalleryPagerAdapter adapter = RecyclerViewUtils.getAdapter(galleryPager);
//                        final int newPosition = LayoutManagerUtils.getFirstVisiblePosition(galleryPager.getLayoutManager());
//
//                        if (position != newPosition) {
//                            thumbView.setAlpha(1f);
//                            gallery.scrollToPosition(newPosition);
//
//                            final int millis = 150;
//                            final Handler handler = new Handler();
//                            handler.postDelayed(new Runnable() {
//
//                                @Override
//                                public void run() {
//                                    MediaHolder thumbViewHolder = (MediaHolder) gallery.findViewHolderForLayoutPosition(newPosition);
//                                    if (thumbViewHolder != null) {
//                                        thumbViewHolder.imageView.setAlpha(0f);
//                                        next(thumbViewHolder.imageView, v, adapter, newPosition);
//                                    } else {
//                                        handler.postDelayed(this, millis);
//                                    }
//                                }
//                            }, millis);
//                        } else {
//                            next(thumbView, v, adapter, position);
//                        }
//                    }
//
//                    private void next(View currentThumbView, View v, GalleryPagerAdapter adapter, int newPosition) {
//                        View currentExpandedView;
//                        currentExpandedView = adapter.getItemViewItem(newPosition);
//                        if (currentExpandedView instanceof GIFVideoView) {
//                            ((GIFVideoView) currentExpandedView).stopPlayback();
//                        }
//                        currentExpandedView = v;
//                        ZoomUtils.getInstance(GalleryFragment.this.getSupportActivity()).zoomOut(currentThumbView, currentExpandedView, new ZoomUtils.OnZoomFinishedListener() {
//                            @Override
//                            public void onZoomFinished() {
//                                setFullscreen(false);
//                            }
//                        });
//                    }
//                });
//            }
        }
    }

    private class GalleryPagerAdapter extends RecyclerView.Adapter<GalleryPagerAdapter.MediaHolder> {
        private static final int TYPE_PHOTO = 0, TYPE_VIDEO = 1;

        private List<MediaData> dataList;
        private SparseIntArray colorPalette = new SparseIntArray();

        GalleryPagerAdapter(List<MediaData> dataList) {
            this.dataList = dataList;
        }

        @NonNull
        @Override
        public MediaHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            ViewGroup layer = (ViewGroup) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_gallery_fs, viewGroup, false);
            setupLayerView(layer);

            return new MediaHolder(layer, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull MediaHolder mediaHolder, int position) {
            mediaHolder.lazyWriting(position);
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return dataList.get(position).isPhoto() ? TYPE_PHOTO : TYPE_VIDEO;
        }

        public MediaHolder getItemViewHolder(int position) {
            return (MediaHolder) galleryPager.findViewHolderForAdapterPosition(position);
        }

        private void setupLayerView(ViewGroup layer) {
            int width, height;
            width = FrameLayout.LayoutParams.MATCH_PARENT;
            height = FrameLayout.LayoutParams.MATCH_PARENT;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            params.gravity = Gravity.CENTER;
            layer.setLayoutParams(params);
            layer.setFitsSystemWindows(true);
            layer.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    galleryPager.performClick();
                }
            });
        }

        class MediaHolder extends RecyclerView.ViewHolder {
            private View mView;
            private ViewGroup mBackground;

            MediaHolder(@NonNull View itemView, int viewType) {
                super(itemView);
                mBackground = itemView.findViewById(R.id.background);
                mView = itemView.findViewById(viewType == TYPE_PHOTO ? R.id.fs_photo : R.id.fs_video);
                mView.setVisibility(View.VISIBLE);
                mView = mView.findViewById(R.id.fs_item);

                if (mView instanceof TouchImageView) {
                    mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            galleryPager.performClick();
                        }
                    });
                }
            }

            private int getColorFromPalette(int key) {
                return colorPalette.get(key, -1);
            }

            private void updateColorPalette(int key, Bitmap bitmap) {
                Palette palette = Palette.from(bitmap).generate();

                Palette.Swatch swatch = palette.getVibrantSwatch();
                if (
                        swatch != null ||
                                (swatch = palette.getDominantSwatch()) != null ||
                                (swatch = palette.getMutedSwatch()) != null
                        ) {
                    colorPalette.put(key, swatch.getRgb());
                }
            }

            private void updateBackgroundColor(int colorTo) {
                mBackground.setBackgroundColor(colorTo);
            }

            private void lazyWriting(int position) {
                MediaData polaroid = dataList.get(position);
                lazyWritingBackground(position, polaroid);
                if (getItemViewType() == TYPE_VIDEO) {
                    lazyWritingVideo(polaroid);
                } else if (getItemViewType() == TYPE_PHOTO) {
                    lazyWritingPhoto(polaroid);
                }
            }

            private void lazyWritingBackground(final int position, MediaData polaroid) {
                int color = getColorFromPalette(position);
                if (color != -1) {
                    if (((ColorDrawable) mBackground.getBackground()).getColor() != color) {
                        updateBackgroundColor(color);
                    }
                } else {
                    lazyWritingUpdateBackgroundColor(position, polaroid);
                }
            }

            private void lazyWritingUpdateBackgroundColor(final int position, final MediaData polaroid) {
                Context context;
                if ((context = getContext()) != null) {
                    Glide.with(context)
                            .asBitmap()
                            .load(polaroid.getPath())
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    updateColorPalette(position, resource);
                                    lazyWritingBackground(position, polaroid);
                                }
                            });
                }
            }

            private void lazyWritingPhoto(MediaData polaroid) {
                if (polaroid.isPhoto()) {
                    final TouchImageView imView = (TouchImageView) mView;

                    imView.resetZoom();

                    int totalSpan = LayoutManagerUtils.getSpanCount(galleryPager.getLayoutManager());
                    RequestOptions requestOptions = new RequestOptions()
                            .dontAnimate()
                            .override(galleryPager.getRootView().getWidth() * totalSpan * 2, galleryPager.getRootView().getHeight() * totalSpan * 2)
                            .placeholder(android.R.drawable.ic_menu_gallery);

                    Glide.with(GalleryFragment.this)
                            .load(polaroid.getPath())
                            .apply(requestOptions)
                            .into(imView);

                    imView.setOnTouchImageViewListener(new TouchImageView.OnTouchImageViewListener() {
                        @Override
                        public void onMove() {
                            float rectXL = imView.getZoomedRect().left,
                                    rectXR = imView.getZoomedRect().right;

                            canGalleryPagerScroll = (rectXL == 0.0f || rectXR == 1.0f);
                        }
                    });
                }
            }

            private void lazyWritingVideo(MediaData polaroid) {
                GIFVideoView gifVideoView = (GIFVideoView) mView;
                if (!polaroid.isPhoto()) {
                    if (gifVideoView.isPlaying()) {
                        gifVideoView.stopPlayback();
                    }
                    gifVideoView.setVideoPath(polaroid.getPath());
                }
            }
        }
    }
}
