package com.yuyh.library.imgsel.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yuyh.library.imgsel.R;
import com.yuyh.library.imgsel.common.Callback;
import com.yuyh.library.imgsel.common.Constant;
import com.yuyh.library.imgsel.config.ISListConfig;
import com.yuyh.library.imgsel.ui.fragment.ImgSelFragment;
import com.yuyh.library.imgsel.utils.FileUtils;
import com.yuyh.library.imgsel.utils.StatusBarCompat;

import java.io.File;
import java.util.ArrayList;

/**
 * https://github.com/smuyyh/ImageSelector
 *
 * @author yuyh.
 * @date 2016/8/5.
 */
public class ISListActivity extends AppCompatActivity implements View.OnClickListener, Callback {

    public static final String INTENT_RESULT = "result";
    private static final int IMAGE_CROP_CODE = 1;
    private static final int STORAGE_REQUEST_CODE = 1;

    private ISListConfig config;
    private String cropImagePath;

    private ImgSelFragment fragment;

    private ArrayList<String> result = new ArrayList<>();

    public static void startForResult(Activity activity, ISListConfig config, int RequestCode) {
        Intent intent = new Intent(activity, ISListActivity.class);
        intent.putExtra("config", config);
        activity.startActivityForResult(intent, RequestCode);
    }

    public static void startForResult(Fragment fragment, ISListConfig config, int RequestCode) {
        Intent intent = new Intent(fragment.getActivity(), ISListActivity.class);
        intent.putExtra("config", config);
        fragment.startActivityForResult(intent, RequestCode);
    }

    public static void startForResult(android.app.Fragment fragment, ISListConfig config, int RequestCode) {
        Intent intent = new Intent(fragment.getActivity(), ISListActivity.class);
        intent.putExtra("config", config);
        fragment.startActivityForResult(intent, RequestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
//        getWindow().addFlags(
//                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        // 设置全屏沉浸式模式
        hideSystemUI();
        setContentView(R.layout.activity_img_sel);

        config = (ISListConfig) getIntent().getSerializableExtra("config");

        // Android 6.0 checkSelfPermission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_REQUEST_CODE);
        } else {
            fragment = ImgSelFragment.instance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fmImageList, fragment, null)
                    .commit();
        }

        initView();
        if (!FileUtils.isSdCardAvailable()) {
            Toast.makeText(this, getString(R.string.sd_disable), Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {


        if (config != null) {

            if (config.statusBarColor != -1) {
                StatusBarCompat.compat(this, config.statusBarColor, config.isDark);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                        && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                }
            }


            if (config.multiSelect) {
                if (!config.rememberSelected) {
                    Constant.imageList.clear();
                }
            } else {
                Constant.imageList.clear();
            }
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onSingleImageSelected(String path) {
        if (config.needCrop) {
            crop(path);
        } else {
            Constant.imageList.add(path);
            exit();
        }
    }

    @Override
    public void onImageSelected(String path) {

    }

    @Override
    public void onImageUnselected(String path) {

    }
    @Override
    public void onPreviewChanged(int select, int sum, boolean visible) {
    }

    private void crop(String imagePath) {
        File file = new File(FileUtils.createRootPath(this) + "/" + System.currentTimeMillis() + ".jpg");

        cropImagePath = file.getAbsolutePath();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(getImageContentUri(new File(imagePath)), "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", config.aspectX);
        intent.putExtra("aspectY", config.aspectY);
        intent.putExtra("outputX", config.outputX);
        intent.putExtra("outputY", config.outputY);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, IMAGE_CROP_CODE);
    }

    public Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public ISListConfig getConfig() {
        return config;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_CROP_CODE && resultCode == RESULT_OK) {
            Constant.imageList.add(cropImagePath);
            config.multiSelect = false; // 多选点击拍照，强制更改为单选
            exit();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void exit() {
        Intent intent = new Intent();
        result.clear();
        result.addAll(Constant.imageList);
        intent.putStringArrayListExtra(INTENT_RESULT, result);
        setResult(RESULT_OK, intent);

        if (!config.multiSelect) {
            Constant.imageList.clear();
        }

        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_REQUEST_CODE:
                if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.fmImageList, ImgSelFragment.instance(), null)
                            .commitAllowingStateLoss();
                } else {
                    Toast.makeText(this, getString(R.string.permission_storage_denied), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("config", config);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        config = (ISListConfig) savedInstanceState.getSerializable("config");
    }

    @Override
    public void onBackPressed() {
        if (fragment == null || !fragment.hidePreview()) {
            Constant.imageList.clear();
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void hideSystemUI() {
        // Hide status bar and navigation bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View decorView = this.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY; // 使用 IMMERSIVE_STICKY
        decorView.setSystemUiVisibility(uiOptions);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }
}
