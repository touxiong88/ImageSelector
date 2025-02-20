package com.yuyh.library.imgsel.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yuyh.library.imgsel.ISNav;
import com.yuyh.library.imgsel.config.ISListConfig;
import com.yuyh.library.imgsel.R;
import com.yuyh.library.imgsel.bean.Image;
import com.yuyh.library.imgsel.common.Constant;
import com.yuyh.library.imgsel.common.OnItemClickListener;
import com.yuyh.library.imgsel.utils.LogUtils;

import java.util.List;

/**
 * @author yuyh.
 * @date 2016/9/28.
 */
public class PreviewAdapter extends PagerAdapter {

    private Activity activity;
    private List<Image> images;
    private ISListConfig config;
    private OnItemClickListener listener;

    public PreviewAdapter(Activity activity, List<Image> images, ISListConfig config) {
        this.activity = activity;
        this.images = images;
        this.config = config;
        LogUtils.d("yuyh", "PreviewAdapter: " + images.size());
    }

    @Override
    public int getCount() {
            return images.size();
    }

    @Override
    public View instantiateItem(ViewGroup container, final int position) {

        this.activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 设置全屏沉浸式模式
        hideSystemUI();
        View root = View.inflate(activity, R.layout.item_pager_img_sel, null);
        final ImageView photoView = (ImageView) root.findViewById(R.id.ivImage);


        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onImageClick(position, images.get(position));
                }
            }
        });


        container.addView(root, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        displayImage(photoView, images.get(position).path);

        return root;
    }

    private void displayImage(ImageView photoView, String path) {
        ISNav.getInstance().displayImage(activity, path, photoView);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private void hideSystemUI() {
        // Hide status bar and navigation bar
        this.activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View decorView = this.activity.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY; // 使用 IMMERSIVE_STICKY
        decorView.setSystemUiVisibility(uiOptions);
        this.activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    private void showSystemUI() {
        this.activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = this.activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }
}
