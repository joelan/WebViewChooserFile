package com.example.administrator.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    ProgressDialog dialog;
    public ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> mUploadMessageForAndroid5;

    public final static int FILECHOOSER_RESULTCODE = 1;
    public final static int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 2;
    private String mCameraFilePath;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView mywebview= (WebView) findViewById(R.id.mywebview);

        dialog=new ProgressDialog(this);

        dialog.setMessage("正在加载。。。");
        WebSettings webSettings = mywebview.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mywebview.loadUrl("http://www.helloweba.com/demo/2016/webuploader/");
        mywebview.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                dialog.show();
                Toast.makeText(MainActivity.this,"加载开始",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                dialog.dismiss();
                Toast.makeText(MainActivity.this,"加载完成",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                dialog.dismiss();
                Toast.makeText(MainActivity.this,"加载失败",Toast.LENGTH_LONG).show();
            }
        });

        mywebview.setWebChromeClient(
                new WebChromeClient() {
                    public void onProgressChanged(WebView view, int progress) {// 载入进度改变而触发
                        if (progress == 100) {
                            //handler.sendEmptyMessage(1);// 如果全部载入,隐藏进度对话框
                        }

                        super.onProgressChanged(view, progress);
                    }
                    //扩展支持alert事件
                    @Override
                    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setTitle("xxx提示").setMessage(message).setPositiveButton("确定", null);
                        builder.setCancelable(false);
                      //  builder.setIcon(R.drawable.ic_launcher);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        result.confirm();
                        return true;
                    }

                    //扩展浏览器上传文件
                    //3.0++版本
                    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                        openFileChooserImpl(uploadMsg);
                    }

                    //3.0--版本
                    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                        openFileChooserImpl(uploadMsg);
                    }

                    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                        openFileChooserImpl(uploadMsg);
                    }

                    // For Android > 5.0

                    public boolean onShowFileChooser (WebView webView, ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {
                        openFileChooserImplForAndroid5(uploadMsg);
                        return true;
                    }


                }
        );


    }

    /**
     * 5.0以上的
     * @param uploadMsg
     */
    private void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg) {

        mUploadMessageForAndroid5 = uploadMsg;
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");

       // Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);

        Intent xxx= createChooserIntent(createCameraIntent());
        xxx.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);

        startActivityForResult(xxx, FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
    }

    /**
     * 5.0以下
     * @param uploadMsg
     */
    private void openFileChooserImpl(ValueCallback<Uri> uploadMsg) {

        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        Intent xxx= createChooserIntent(createCameraIntent());
        xxx.putExtra(Intent.EXTRA_INTENT, i);
        startActivityForResult(xxx, FILECHOOSER_RESULTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {


            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != RESULT_OK ? null: intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;

            //5.0以下拍照是不是也要像5.0以上那样处理拍照的事件呢？自己测试

        } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5){
            //针对5.0的
            Toast.makeText(MainActivity.this,"11",Toast.LENGTH_LONG).show();
            if (null == mUploadMessageForAndroid5)
                return;
            Toast.makeText(MainActivity.this,"22",Toast.LENGTH_LONG).show();
            Uri result = (intent == null || resultCode != RESULT_OK) ? null: intent.getData();
            if (result != null) {
                //相册或者文件
                Toast.makeText(MainActivity.this,"33",Toast.LENGTH_LONG).show();
                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{result});
            } else {
                //拍照
                Toast.makeText(MainActivity.this,"44",Toast.LENGTH_LONG).show();
                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{uri});
            }
            mUploadMessageForAndroid5 = null;
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {// 定义一个Handler，用于处理下载线程与UI间通讯
            if (!Thread.currentThread().isInterrupted()){
                switch (msg.what) {
                    case 0:
                        dialog.show();// 显示进度对话框
                        break;
                    case 1:
                        dialog.dismiss();// 隐藏进度对话框，不可使用dismiss()、cancel(),否则再次调用show()时，显示的对话框小圆圈不会动。
                        break;
                }
            }

            super.handleMessage(msg);
        }
    };

    private Intent createCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File externalDataDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);
        File cameraDataDir = new File(externalDataDir.getAbsolutePath() +
                File.separator + "browser-photos");
        cameraDataDir.mkdirs();
        mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator +
                System.currentTimeMillis() + ".jpg";
        uri=  Uri.fromFile(new File(mCameraFilePath));
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        return cameraIntent;
    }
    private Intent createChooserIntent(Intent ... intents) {
        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
        chooser.putExtra(Intent.EXTRA_TITLE, "选择图片来源");
        return chooser;
    }

}
