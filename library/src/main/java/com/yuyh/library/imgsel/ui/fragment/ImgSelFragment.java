package com.yuyh.library.imgsel.ui.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.yuyh.library.imgsel.R;
import com.yuyh.library.imgsel.adapter.FolderListAdapter;
import com.yuyh.library.imgsel.adapter.ImageListAdapter;
import com.yuyh.library.imgsel.adapter.PreviewAdapter;
import com.yuyh.library.imgsel.bean.Folder;
import com.yuyh.library.imgsel.bean.Image;
import com.yuyh.library.imgsel.common.Callback;
import com.yuyh.library.imgsel.common.Constant;
import com.yuyh.library.imgsel.common.OnFolderChangeListener;
import com.yuyh.library.imgsel.common.OnItemClickListener;
import com.yuyh.library.imgsel.config.ISListConfig;
import com.yuyh.library.imgsel.ui.ISListActivity;
import com.yuyh.library.imgsel.utils.DisplayUtils;
import com.yuyh.library.imgsel.utils.FileUtils;
import com.yuyh.library.imgsel.utils.LogUtils;
import com.yuyh.library.imgsel.widget.CustomViewPager;
import com.yuyh.library.imgsel.widget.DividerGridItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ImgSelFragment extends Fragment implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private RecyclerView rvImageList;
    private Button btnAlbumSelected;
    private View rlBottom;
    private CustomViewPager viewPager;

    private ISListConfig config;
    private Callback callback;
    private List<Folder> folderList = new ArrayList<>();
    private List<Image> imageList = new ArrayList<>();

    private ListPopupWindow folderPopupWindow;
    private ImageListAdapter imageListAdapter;
    private FolderListAdapter folderListAdapter;
    private PreviewAdapter previewAdapter;

    private boolean hasFolderGened = false;

    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;
    private static Handler mHandler;
    private static final int MSG_TOUCH_TIMEOUT = 1;
    private static final long DELAY_TIME_RECEIVE = 60 * 1000L;//5 second countdown
    private File tempFile;

    public static ImgSelFragment instance() {
        ImgSelFragment fragment = new ImgSelFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        LogUtils.d("yuyh", "ImgSelFragment: ");
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_img_sel, container, false);
        rvImageList = view.findViewById(R.id.rvImageList);
        btnAlbumSelected = view.findViewById(R.id.btnAlbumSelected);
        btnAlbumSelected.setOnClickListener(this);
        rlBottom = view.findViewById(R.id.rlBottom);
        viewPager = view.findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(this);

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MSG_TOUCH_TIMEOUT) {//touch timeout, executing setLocked
                    viewPager.setLocked(true);
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        config = ((ISListActivity) getActivity()).getConfig();
        callback = ((ISListActivity) getActivity());

        if (config == null) {
            Log.e("ImgSelFragment", "config 参数不能为空");
            return;
        }

        btnAlbumSelected.setText(config.allImagesText);

        rvImageList.setLayoutManager(new GridLayoutManager(rvImageList.getContext(), 3));
        rvImageList.addItemDecoration(new RecyclerView.ItemDecoration() {
            int spacing = DisplayUtils.dip2px(rvImageList.getContext(), 6);
            int halfSpacing = spacing >> 1;

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.left = halfSpacing;
                outRect.right = halfSpacing;
                outRect.top = halfSpacing;
                outRect.bottom = halfSpacing;
            }
        });


        imageListAdapter = new ImageListAdapter(getActivity(), imageList, config);
        imageListAdapter.setMutiSelect(config.multiSelect);
        rvImageList.setAdapter(imageListAdapter);
        imageListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public int onCheckedClick(int position, Image image) {
                return checkedImage(position, image);
            }

            @Override
            public void onImageClick(int position, Image image) {
                    if (config.multiSelect) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            TransitionManager.go(new Scene(viewPager), new Fade().setDuration(200));
                        }
                        viewPager.setAdapter((previewAdapter = new PreviewAdapter(getActivity(), imageList, config)));


                        mHandler.removeMessages(MSG_TOUCH_TIMEOUT);
                        mHandler.sendEmptyMessageDelayed(MSG_TOUCH_TIMEOUT, DELAY_TIME_RECEIVE);

                        previewAdapter.setListener(new OnItemClickListener() {
                            @Override
                            public int onCheckedClick(int position, Image image) {
                                return checkedImage(position, image);
                            }

                            @Override
                            public void onImageClick(int position, Image image) {
                                hidePreview();
                            }
                        });
                        callback.onPreviewChanged(position + 1, imageList.size(), true);
                        viewPager.setCurrentItem(position);
                        viewPager.setVisibility(View.VISIBLE);
                    } else {
                        if (callback != null) {
                            callback.onSingleImageSelected(image.path);
                        }
                    }

            }
        });

        folderListAdapter = new FolderListAdapter(getActivity(), folderList, config);

        getActivity().getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);
    }

    private int checkedImage(int position, Image image) {
        if (image != null) {
            if (Constant.imageList.contains(image.path)) {
                Constant.imageList.remove(image.path);
                if (callback != null) {
                    callback.onImageUnselected(image.path);
                }
            } else {
                if (config.maxNum <= Constant.imageList.size()) {
                    Toast.makeText(getActivity(), String.format(getString(R.string.maxnum), config.maxNum), Toast.LENGTH_SHORT).show();
                    return 0;
                }

                Constant.imageList.add(image.path);
                if (callback != null) {
                    callback.onImageSelected(image.path);
                }
            }
            return 1;
        }
        return 0;
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media._ID};

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_ALL) {
                return new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
            } else if (id == LOADER_CATEGORY) {
                return new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[0] + " not like '%.gif%'", null, MediaStore.Images.Media.DATE_ADDED + " DESC");
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                int count = data.getCount();
                if (count > 0) {
                    List<Image> tempImageList = new ArrayList<>();
                    data.moveToFirst();
                    do {
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        Image image = new Image(path, name);
                        tempImageList.add(image);
                        if (!hasFolderGened) {
                            File imageFile = new File(path);
                            File folderFile = imageFile.getParentFile();
                            if (folderFile == null || !imageFile.exists() || imageFile.length() < 10) {
                                continue;
                            }

                            Folder parent = null;
                            for (Folder folder : folderList) {
                                if (TextUtils.equals(folder.path, folderFile.getAbsolutePath())) {
                                    parent = folder;
                                }
                            }
                            if (parent != null) {
                                parent.images.add(image);
                            } else {
                                parent = new Folder();
                                parent.name = folderFile.getName();
                                parent.path = folderFile.getAbsolutePath();
                                parent.cover = image;

                                List<Image> imageList = new ArrayList<>();
                                imageList.add(image);

                                parent.images = imageList;
                                folderList.add(parent);
                            }
                        }
                    } while (data.moveToNext());

                    imageList.clear();
                    imageList.addAll(tempImageList);

                    imageListAdapter.notifyDataSetChanged();
                    folderListAdapter.notifyDataSetChanged();

                    hasFolderGened = true;
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private void createPopupFolderList(int width, int height) {
        folderPopupWindow = new ListPopupWindow(getActivity());
        folderPopupWindow.setAnimationStyle(R.style.PopupAnimBottom);
        folderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        folderPopupWindow.setAdapter(folderListAdapter);
        folderPopupWindow.setContentWidth(width);
        folderPopupWindow.setWidth(width);
        folderPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        folderPopupWindow.setAnchorView(rlBottom);
        folderPopupWindow.setModal(true);
        folderListAdapter.setOnFloderChangeListener(new OnFolderChangeListener() {
            @Override
            public void onChange(int position, Folder folder) {
                folderPopupWindow.dismiss();
                if (position == 0) {
                    getActivity().getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
                    btnAlbumSelected.setText(config.allImagesText);
                } else {
                    imageList.clear();
                    imageList.addAll(folder.images);
                    imageListAdapter.notifyDataSetChanged();

                    btnAlbumSelected.setText(folder.name);
                }
            }
        });
        folderPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(1.0f);
            }
        });
    }

    public void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = bgAlpha;
        getActivity().getWindow().setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        WindowManager wm = getActivity().getWindowManager();
        final int size = wm.getDefaultDisplay().getWidth() / 3 * 2;
        if (v.getId() == btnAlbumSelected.getId()) {
            if (folderPopupWindow == null) {
                createPopupFolderList(size, size);
            }

            if (folderPopupWindow.isShowing()) {
                folderPopupWindow.dismiss();
            } else {
                folderPopupWindow.show();
                if (folderPopupWindow.getListView() != null) {
                    folderPopupWindow.getListView().setDivider(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.mbottom_bg)));
                }
                int index = folderListAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                folderPopupWindow.getListView().setSelection(index);

                folderPopupWindow.getListView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            folderPopupWindow.getListView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            folderPopupWindow.getListView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        int h = folderPopupWindow.getListView().getMeasuredHeight();
                        if (h > size) {
                            folderPopupWindow.setHeight(size);
                            folderPopupWindow.show();
                        }
                    }
                });
                setBackgroundAlpha(0.6f);
            }
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        callback.onPreviewChanged(position + 1, imageList.size(), true);

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public boolean hidePreview() {
        if (viewPager.getVisibility() == View.VISIBLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TransitionManager.go(new Scene(viewPager), new Fade().setDuration(200));
            }
            viewPager.setVisibility(View.GONE);
            callback.onPreviewChanged(0, 0, false);
            imageListAdapter.notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }
}
