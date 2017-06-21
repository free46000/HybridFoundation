## 前言
这里我整理了`Android`端会用到代码，包含`JS`通信，文件处理工具类，闪屏辅助类和`WebView`的封装。由于源码并没有完善，所以暂时没有发布到`Maven`仓库

## 用法

### `JS`通信
- 需要把`library`中`assets`文件夹下的`DeviceBridge.js`放入并加载到你的前端项目中
- `Android`调用`JS`
首先在`JS`中注册对应`Handler`，以注册的字符串为`key`对应
``` javascript
window.DeviceJsBridge.registMessageHandler("testCallJs", function (message, responseCallback) {
        alert("android调用js方法，消息内容：" + JSON.stringify(message))
        message.data = '我是在js中赋值的数据'
        responseCallback(message);
    });
```
然后就可以在`Android`代码中调用了
``` java
Message message = new Message();
message.setHandlerName("testCallJs");
message.setData("数据内容（onPageFinished执行android调用js，js返回的数据会打印在logcat）");
webView.getJsBridge().sendMessage(message, responseMessage ->
        System.out.println("js异步response消息:" + responseMessage));
```

- `JS`调用`Android`
同样首先需要在`Android`中注册对应`Handler`，以注册的字符串为`key`对应
``` java
jsBridge.registMessageHandler("connectBluetooth", messageHandler);
```
然后就可以在`JS`中调用了
``` javascript
var message = {handlerName: 'testAndroid'}
var result = window.DeviceJsBridge.sendMessage(message, function (message) {
    alert("js调用android异步方法Response结果：" + JSON.stringify(message))
})
```

### 文件处理工具
相关用法详见代码注释，包含对压缩文件的处理

### 闪屏辅助类
下面列一下常用的方法
``` java
/**
 * 设置闪屏等待时间，最终等待时间和setDelayObservable共同决定
 */
public void setDelayTime(long delayTime)

/**
 * 设置闪屏等待Observable 最终等待时间和setDelayTime共同决定
 */
public void setDelayObservable(Observable<?> delayObservable)

/**
 * 设置闪屏隐藏监听
 */
public void setSplashHideListener(SplashHideListener splashHideListener)

/**
 * 展示闪屏
 *
 * @param viewId         包含Fragment的view id
 * @param splashFragment 闪屏Fragment
 */
public void showSplash(int viewId, Fragment splashFragment)
```

### `HybridWebView`的封装
主要封装了通用的方法，对外提供了必要的监听回调，还包括了`JSBridge`的使用，让你在使用`WebView`的时候更方便，具体使用详见`Demo`


## 概述
移动开发的跨平台与快速发布一直是开发者的追求，也是技术的一个发展趋势，现在各大厂开始有了自己的大前端团队，所以我们也开始了自己的探索，目前来说主要有两种思路：
- `Hybrid App` 代表:`Cordova`
    通过`Webview`加载`Web`页面，在`Native`和`Web`页面之间建立双向通信
- `H5`代码`Native`化 代表:`ReactNative`,`Weex`等
    使用各平台`Api`，把`H5`代码编译成二进制代码直接运行

其实关于这两种思路对比，网上有很多大牛分析的很全面了，总结来说各有利弊很难完美，本篇文章我们主要讲一下Hybrid App实践，采用前后端分离以及单页应用技术开发`Web`页面，使用`WebView`加载`Web`页面，并通过`JS`通信提供一些`Native`层的支持，通过接口获取更新后的差异化页面资源文件，在本地覆盖，就可以达到热更新的需求。在我看来此方案更适用于需要快速发布、多端兼容、对性能要求稍低的业务，正好符合我们的需求。

## 方案详解
既然确定了方向，那么就应该确定具体的方案了，通过自己的经验和网上资料整理，画了时序图：
![image](https://github.com/free46000/cloud/raw/master/blog/app_web.png)

按照图上的时序，接下来说一下每一步中的实践，以及碰到的坑。下面讲解

### 初次安装
- **打包**
在打包程序时这一步主要是把`Html`相关资源文件压缩后放在`assets`文件夹下即可
- **安装**
用户安装完应用程序打开后，检测是否为初次使用，如果是则通过程序直接解压包内资源到手机存储上即可，不局限于SD卡。

### 展示页面
- **闪屏页展示**
由于上面的解压资源，还有`Webview`初始化、`JS`的加载执行、`html`的渲染都是耗时操作，并且都是发生在`Html`展示之前，所以我们选择把闪屏页用`Android`原生代码来编写，采用覆盖`WebView`所在`Activity`的方案，这样在闪屏页隐藏的时候，用户就可以看到业务界面，可以提升用户体验。
注：另外提供两种闪屏优化的小技巧，使用透明主题或者设置主题背景图片

- **加载本地Html页面**
直接使用`WebView#loadUrl()`加载本地资源文件即可。由于`WebView`加载不同页面会出现闪屏的问题，所以我们采用`Vue + Vue Router`构建单页应用即可。
这里`Vue Router`会有一个小坑，提醒大家注意一下：`Vue Router`默认采用`hash`模式，会有一个丑陋的`#`符号，作为一个有追求的程序员怎么能允许这种很丑的`hash`，一种更优雅的方式使用`HTML5 History`模式，但是不幸的是，加载本地资源文件的方式并不能正常解析`HTML5 History`模式的`url`，所以只能采用`hash`模式。

- **数据请求**
为了节省用户的流量和时间，需要把`Html`资源文件存储在本地，这样数据的请求必须在客户端完成。有两种方案供选择：
一是`Native`层拦截并请求数据再返回给`Html`层去展示，有我们采用前后端分离直接通过`JS`请求接口获取数据即可，这样会增加工作量，也不利于职责的分离，所以放弃。
二是直接使用`JS`请求数据，这样会出现跨域访问的问题，相比较来说还是这个比较容易解决的，采用`CORS`即可

- **Native调用JS**
`Native`层调用`JS`比较简单，执行一段`JS`代码即可，如：`javascript:callJS()`

- **JS调用Native**
`JS`层调用`Native`主要分为三种：
一：通过`WebView#addJavascriptInterface()`进行映射，使用起来简单，但是有安全风险，弃用
二：自定义协议然后由`Native`层拦截并解析请求，使用起来复杂，容易和业务耦合，也不是最优选，弃用
三：拦截`JS#prompt()`方法并解析，使用起来复杂，但是比第一种更安全，比第二种灵活一些，所以使用此方案

### 资源文件获取
资源文件采取差异化更新方案，本地存储一个标识，可以为版本号或者更新时间，这个可以和后端同学一起商量确定，资源文件下载还有推送之类的由于`Html`的局限性，所以还是直接由`Native`层做比较合适，下面简单讲解下应用中的两种更新方式：
- **服务端推送下发**
可以通过集成第三方的推送服务，在客户端收到更新推送后主动去请求下载差异化文件
- **主动请求**
可以在选择合适的时机，如在应用启动时去请求差异化文件

### 资源文件更新
根据差异化清单对资源文件进行整合，存放在临时目录中，然后在第二次打开应用时更换，并展示更新后的界面，达到热更新的效果。

## 总结
只是概括的讲了结构的内容，可能会遗漏一些要点，如果大家有什么问题欢迎提交`issue`

