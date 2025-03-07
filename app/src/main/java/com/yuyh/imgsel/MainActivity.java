package com.yuyh.imgsel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.yuyh.library.imgsel.ISNav;
import com.yuyh.library.imgsel.common.ImageLoader;
import com.yuyh.library.imgsel.config.ISListConfig;
import com.yuyh.library.imgsel.utils.LogUtils;
import com.yuyh.library.imgsel.utils.StatusBarCompat;

import java.util.List;

/**
 * https://github.com/smuyyh/ImageSelector
 *
 * @author yuyh.
 * @date 2016/8/5.
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LIST_CODE = 0;

    private TextView tvResult;
    private SimpleDraweeView draweeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);


        tvResult = (TextView) findViewById(R.id.tvResult);
        draweeView = (SimpleDraweeView) findViewById(R.id.my_image_view);

        ISNav.getInstance().init(new ImageLoader() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void displayImage(Context context, String path, ImageView imageView, boolean TouchIntercept) {
                Glide.with(context).load(path).into(imageView);
            }
        });
    }

    public void Multiselect(View view) {
        tvResult.setText("");
        ISListConfig config = new ISListConfig.Builder()
                .multiSelect(true)
                // 是否记住上次选中记录
                .rememberSelected(false)
                // 使用沉浸式状态栏
                .statusBarColor(Color.parseColor("#3F51B5"))
                // 设置状态栏字体风格浅色
                .isDarkStatusStyle(false)
                .build();

        ISNav.getInstance().toListActivity(this, config, REQUEST_LIST_CODE);
    }

    public void Single(View view) {
        tvResult.setText("");
        ISListConfig config = new ISListConfig.Builder()
                // 是否多选
                .multiSelect(false)
                .btnText("Confirm")
                // 确定按钮背景色
                //.btnBgColor(Color.parseColor(""))
                // 确定按钮文字颜色
                .btnTextColor(Color.WHITE)
                // 使用沉浸式状态栏
                .statusBarColor(Color.parseColor("#3F51B5"))
                // 设置状态栏字体风格黑色
                .isDarkStatusStyle(true)
                // 返回图标ResId
                .backResId(R.mipmap.back)
                .title("Images")
                .titleColor(Color.WHITE)
                .titleBgColor(Color.parseColor("#3F51B5"))
                .allImagesText("All Images")
                .needCrop(true)
                .cropSize(1, 1, 200, 200)
                // 最大选择图片数量
                .maxNum(9)
                .build();

        ISNav.getInstance().toListActivity(this, config, REQUEST_LIST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LIST_CODE && resultCode == RESULT_OK && data != null) {
            List<String> pathList = data.getStringArrayListExtra("result");

            // 测试Fresco
            // draweeView.setImageURI(Uri.parse("file://"+pathList.get(0)));
            for (String path : pathList) {
                tvResult.append(path + "\n");
            }
        } else if (resultCode == RESULT_OK && data != null) {
            String path = data.getStringExtra("result");
            tvResult.append(path + "\n");
        }
    }
}
