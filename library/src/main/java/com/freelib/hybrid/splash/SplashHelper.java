package com.freelib.hybrid.splash;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.view.View;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author free46000  2017/06/09
 * @version v1.0
 */
public class SplashHelper {
    private Activity activity;
    private Fragment splashFragment;
    private long delayTime = 1500; //延时时间，单位ms
    private Observable<?> delayObservable;
    private Subscription subscription;
    private SplashHideListener splashHideListener;
    private View container;

    public SplashHelper(Activity activity) {
        this.activity = activity;
    }

    /**
     * 设置闪屏等待时间，最终等待时间和setDelayObservable共同决定
     *
     * @param delayTime
     * @see #setDelayObservable(Observable)
     */
    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }

    /**
     * 设置闪屏等待Observable 最终等待时间和setDelayTime共同决定
     *
     * @param delayObservable
     * @see #setDelayTime(long)
     */
    public void setDelayObservable(Observable<?> delayObservable) {
        this.delayObservable = delayObservable;
    }

    /**
     * 设置闪屏隐藏监听
     *
     * @param splashHideListener SplashHideListener
     */
    public void setSplashHideListener(SplashHideListener splashHideListener) {
        this.splashHideListener = splashHideListener;
    }

    /**
     * 展示闪屏
     *
     * @param viewId         包含Fragment的view id
     * @param splashFragment 闪屏Fragment
     */
    public void showSplash(int viewId, Fragment splashFragment) {
        container = activity.findViewById(viewId);
        container.setVisibility(View.VISIBLE);
        this.splashFragment = splashFragment;

        FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
        transaction.replace(viewId, splashFragment);
        transaction.commit();

        delayHideSplash();
    }

    /**
     * 隐藏闪屏 一般不需要手动调用
     */
    public void hideSplash() {
        if (splashFragment == null) {
            return;
        }

        FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
        transaction.remove(splashFragment);
        transaction.commit();

        splashFragment = null;

        try {
            container.setVisibility(View.GONE);
            container = null;
            subscription.unsubscribe();
            subscription = null;
        } catch (Throwable ignore) {
        }
    }

    private void delayHideSplash() {
        Observable<Long> delayTimeObservable = Observable.timer(delayTime, TimeUnit.MILLISECONDS);
        if (delayObservable == null) {
            delayObservable = Observable.just("");
        }
        subscription = Observable.zip(delayObservable, delayTimeObservable, (o, aLong) -> aLong)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {
                        hideSplash();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideSplash();
                    }

                    @Override
                    public void onNext(Long aLong) {
                    }
                });

    }

    /**
     * 闪屏消失监听
     */
    public interface SplashHideListener {
        void onSplashHide();
    }
}
