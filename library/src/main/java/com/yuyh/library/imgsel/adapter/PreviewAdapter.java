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
        if (config.needCamera)
            return images.size() - 1;
        else
            return images.size();
    }

    @Override
    public View instantiateItem(ViewGroup container, final int position) {

        View root = View.inflate(activity, R.layout.item_pager_img_sel, null);
        final ImageView photoView = (ImageView) root.findViewById(R.id.ivImage);

        if (config.multiSelect) {


            final Image image = images.get(config.needCamera ? position + 1 : position);


            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onImageClick(position, images.get(position));
                    }
                }
            });
        }

        container.addView(root, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        passwdInput = root.findViewById(R.id.EtInput);
        btnEnter = root.findViewById(R.id.BtnEnter);
        displayImage(photoView, images.get(position).path);
        passwdInput.setVisibility(View.VISIBLE);
        btnEnter.setVisibility(View.VISIBLE);
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
                if (inputText.equals("20250226")) {
                    // 隐藏软键盘
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(passwdInput.getWindowToken(), 0);

                    // 清除焦点和输入内容（可选）
                    passwdInput.clearFocus();
                    passwdInput.setText("");

                    TouchIntercept = true;
                    cHandler.sendEmptyMessage(MSG_TOUCH_ENABLE);
        			displayImage(photoView, images.get(config.needCamera ? position + 1 : position).path);
                    Toast.makeText(activity, "密码正确" +inputText, Toast.LENGTH_SHORT).show();
                    passwdInput.setText("");
                } else {
                    Toast.makeText(activity, "密码错误" +inputText, Toast.LENGTH_SHORT).show();
                    // 清除焦点和输入内容（可选）
                    passwdInput.clearFocus();
                    passwdInput.setText("");
                }

            }
        });

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
}
