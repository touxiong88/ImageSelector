package com.yuyh.library.imgsel.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yuyh.library.imgsel.ISNav;
import com.yuyh.library.imgsel.config.ISListConfig;
import com.yuyh.library.imgsel.R;
import com.yuyh.library.imgsel.bean.Image;
import com.yuyh.library.imgsel.common.Constant;
import com.yuyh.library.imgsel.common.OnItemClickListener;
import com.yuyh.library.imgsel.ui.ISListActivity;
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
    private Button btnEnter;
    private EditText passwdInput;
    private static final int MSG_TOUCH_TIMEOUT = 1;
    private static final int MSG_TOUCH_ENABLE = 2;
    private static final long DELAY_TIME_RECEIVE = 60 * 1000L;//5 second countdown
    private  boolean TouchIntercept = false;
    private Handler cHandler;
    private static String inputText;
    public PreviewAdapter(Activity activity, List<Image> images, ISListConfig config, Handler mHandler) {
        this.activity = activity;
        this.images = images;
        this.config = config;
        this.cHandler = mHandler;
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
        container.addView(root, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        final ImageView photoView = (ImageView) root.findViewById(R.id.ivImage);
        passwdInput = root.findViewById(R.id.EtInput);
        btnEnter = root.findViewById(R.id.BtnEnter);
        displayImage(photoView, images.get(position).path);
        passwdInput.setVisibility(View.VISIBLE);
        btnEnter.setVisibility(View.VISIBLE);
        passwdInput.requestFocus();

        passwdInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // 文本变化前的处理
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            // 文本变化时的处理
            }

            @Override
            public void afterTextChanged(Editable s) {
            // 文本变化后的处理
                inputText = s.toString();
            // 处理变化后的文本
            }
        });
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputText.equals("456789")) {
                    // 隐藏软键盘
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(passwdInput.getWindowToken(), 0);

                    // 清除焦点和输入内容（可选）
                    passwdInput.clearFocus();
                    passwdInput.setText("");

                    TouchIntercept = true;
                    cHandler.sendEmptyMessage(MSG_TOUCH_ENABLE);
                    displayImage(photoView, images.get(position).path);
                    Toast.makeText(activity, "密码正确" +inputText, Toast.LENGTH_SHORT).show();
                    passwdInput.setText("");
                } else {
                    Toast.makeText(activity, "密码错误" +inputText, Toast.LENGTH_SHORT).show();
                    passwdInput.setText("");
                }

            }
        });

        return root;
    }




    @SuppressLint("ClickableViewAccessibility")
    private void displayImage(ImageView photoView, String path) {
        ISNav.getInstance().displayImage(activity, path, photoView, TouchIntercept);

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
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY; // 使用 IMMERSIVE_STICKY
        decorView.setSystemUiVisibility(uiOptions);
        this.activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    private void showSystemUI() {
        this.activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = this.activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }
}
