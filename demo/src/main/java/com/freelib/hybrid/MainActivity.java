package com.freelib.hybrid;

import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.freelib.hybrid.bridge.Message;
import com.freelib.hybrid.file.FileUtils;
import com.freelib.hybrid.file.ZipUtils;
import com.freelib.hybrid.splash.SplashHelper;
import com.freelib.hybrid.webview.HybridWebView;
import com.google.gson.Gson;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
    public static final long DELAY_TIME = 2000;

    @ViewById(R.id.webView)
    protected HybridWebView webView;

    private SplashHelper splashHelper;


    @AfterViews
    public void initView() {
        splashHelper = new SplashHelper(this);
        splashHelper.setDelayObservable(createH5ResourceObservable());
        splashHelper.setDelayTime(DELAY_TIME);
        splashHelper.showSplash(R.id.container, SplashFragment_.builder().build());

        JsApi jsApi = new JsApi(webView.getJsBridge());

        jsApi.registConnectBluetoothHandler((message, responseCallBack) -> {
            System.out.println("testAndroid:::" + new Gson().toJson(message));

            message.setData("android 异步 response 结果");
            responseCallBack.onMessageCallBack(message);
        });


        webView.getJsBridge().registMessageHandler("testAndroid", (message, responseCallBack) -> {
            System.out.println("testAndroid:::" + new Gson().toJson(message));

            message.setData("android 异步 response 结果");
            responseCallBack.onMessageCallBack(message);
        });
        webView.setListener(new HybridWebView.Listener() {
            @Override
            public void onPageStarted(String url, Bitmap favicon) {
                System.out.println("page started:" + url);
            }

            @Override
            public void onPageProgressed(int newProgress) {
                System.out.println("page progress:" + newProgress);
            }

            @Override
            public void onPageFinished(String url) {
                new Handler().postDelayed(() -> {
                    Message message = new Message();
                    message.setHandlerName("testCallJs");
                    message.setData("数据内容（onPageFinished执行android调用js，js返回的数据会打印在logcat）");
                    webView.getJsBridge().sendMessage(message, responseMessage ->
                            System.out.println("js异步response消息:" + responseMessage));
                }, DELAY_TIME);
                System.out.println("page finished:" + url);

            }

            @Override
            public void onPageError(int errorCode, String description, String failingUrl) {
                System.out.println("page error:" + errorCode + "=" + description + "=" + failingUrl);
            }

            @Override
            public void onReceiveTitle(String title) {
                System.out.println("title:" + title);
            }
        });

    }

    public void showWebViewUrl() {
        Log.e("MainActivity", "loadUrl:" + "file:///android_asset/test.html");
        webView.loadUrl("file:///android_asset/test.html");
    }

    private Observable createH5ResourceObservable() {
        return Observable.just(getH5SavePath() + "/disk/index.html").map(url -> {
//        return Observable.just("file:///android_asset/index.html").map(url -> {
            unZip();
            return url;
        }).observeOn(AndroidSchedulers.mainThread())
                .doOnNext(url -> webView.loadUrl("file://" + url));
    }

    private void unZip() {
        boolean isSuccess = ZipUtils.unZipFile(this, getH5SavePath() + "/disk_temp/", "disk.zip");
        Log.e("MainActivity", "temp unZipFile:" + isSuccess);
        isSuccess = FileUtils.deleteDir(getH5SavePath() + "/disk/");
        Log.e("MainActivity", "deleteDir:" + isSuccess);
        isSuccess = FileUtils.copyDir(getH5SavePath() + "/disk_temp/", getH5SavePath() + "/disk/");
        Log.e("MainActivity", "copyDir:" + isSuccess);
        isSuccess = FileUtils.deleteDir(getH5SavePath() + "/disk_temp/");
        Log.e("MainActivity", "temp deleteDir:" + isSuccess);


    }

    /**
     * 获取H5保存路径
     *
     * @return
     */
    public String getH5SavePath() {
        return getDir("h5", MODE_PRIVATE).getPath();
    }

}
