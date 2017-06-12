package com.freelib.hybrid;

import android.graphics.Bitmap;
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
    @ViewById(R.id.webView)
    protected HybridWebView webView;

    private SplashHelper splashHelper;


    @AfterViews
    public void initView() {
        splashHelper = new SplashHelper(this);
        splashHelper.setDelayObservable(createH5ResourceObservable());
        splashHelper.setDelayTime(5000);
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
                Message message = new Message();
                message.setHandlerName("testCallJs");
                message.setData("android调用js数据");
                webView.getJsBridge().sendMessage(message, responseMessage -> System.out.println("js异步response消息:" + responseMessage));
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
        Observable<String> observable = Observable.just("").map(s -> {
            unZip();
            return "";
        }).observeOn(AndroidSchedulers.mainThread())
                .doOnNext(s -> webView.loadUrl("file:///android_asset/test.html"));

        return observable;
    }

    private void unZip() {
        boolean isSuccess = ZipUtils.unZipFile(this, getH5SavePath() + "/disk_temp/", "dist.zip");
        Log.e("MainActivity", "temp unZipFile:" + isSuccess);
        isSuccess = FileUtils.copyFile(getH5SavePath() + "/disk_temp/index.html", getH5SavePath() + "/disk/index.html");
        Log.e("MainActivity", "copyFile:" + isSuccess);
        isSuccess = FileUtils.deleteDir(getH5SavePath() + "/disk/");
        Log.e("MainActivity", "deleteDir:" + isSuccess);
        isSuccess = FileUtils.copyDir(getH5SavePath() + "/disk_temp/", getH5SavePath() + "/disk/");
        Log.e("MainActivity", "copyDir:" + isSuccess);
        isSuccess = FileUtils.deleteDir(getH5SavePath() + "/disk_temp/");
        Log.e("MainActivity", "temp deleteDir:" + isSuccess);


    }

    /**
     * 获取内置SD卡路径
     *
     * @return
     */
    public String getH5SavePath() {
        return getDir("h5", MODE_PRIVATE).getPath();
    }

}
