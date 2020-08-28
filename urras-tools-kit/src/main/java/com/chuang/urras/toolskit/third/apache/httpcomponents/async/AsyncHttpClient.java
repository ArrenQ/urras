package com.chuang.urras.toolskit.third.apache.httpcomponents.async;

import com.chuang.urras.toolskit.third.apache.httpcomponents.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;


/**
 * Created by ath on 2017/1/6.
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class AsyncHttpClient {
    private final String defaultCharset;
    private final CloseableHttpAsyncClient asyncHttpClient;
    private final RequestConfig defaultConfig;
    private static final Logger logger = LoggerFactory.getLogger(AsyncHttpClient.class);

    public AsyncHttpClient(final String defaultCharset, final CloseableHttpAsyncClient asyncHttpClient, final RequestConfig defaultConfig) {
        this.defaultCharset = defaultCharset;
        this.asyncHttpClient = asyncHttpClient;
        this.defaultConfig = defaultConfig;
    }

    public AsyncHttpClient init() {
        try {
            start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("开始关闭异步Http客户端");
                AsyncHttpClient.this.shutdown();
                logger.info("异步Http客户端 成功 关闭");
            }));
        } catch (Throwable e) {
            logger.error("无法开启异步http客户端", e);
        }
        return this;
    }

    public void start() {
        asyncHttpClient.start();
    }

    public void shutdown() {
        try {
            asyncHttpClient.close();
        } catch (Exception e) {
            logger.error("无法关闭异步http客户端，请检查相关问题，避免内存泄露", e);
        }
    }

    public CompletableFuture<String> doGet(String url) {
        return doGet(url, null);
    }

    public CompletableFuture<String> doPost(String url) {
        return doPost(url, null);
    }

    public CompletableFuture<String> doGet(String url, Map<String, String> params) {
        return doGet(url, params, defaultCharset);
    }

    public CompletableFuture<String> doPost(String url, Map<String, String> params) {
        return doPost(url, params, defaultCharset);
    }

    public CompletableFuture<String> doPost(String url, Map<String, String> params, String charset) {
        return doPost(url, params, null, null, charset, null).thenApply(Response::asString);
    }

    public CompletableFuture<String> doGet(String url, Map<String, String> params, String charset) {
        return doGet(url, params, null, null,charset, null, -1, -1).thenApply(Response::asString);
    }

    /**
     * 执行http请求
     * @param request httrequest对象
     * @param requestData request数据，例如xml，json等等
     * @param heads 头信息
     * @param charset 编码
     * @param proxy 代理
     * @return
     */
    public CompletableFuture<Response> exec(HttpRequestBase request, String requestData, HttpContext context, Map<String, String> heads, String charset, HttpHost proxy, int connTimeout, int readTimeout) {
        return exec(request, new StringEntity(requestData, charset), context, heads, charset, proxy, connTimeout, readTimeout);
    }

    /**
     * 执行请求
     * @param request 请求对象
     * @param params 参数键值
     * @param heads 头信息
     * @param charset 编码
     * @param proxy 代理
     * @return
     */
    public CompletableFuture<Response> exec(HttpRequestBase request, Map<String, String> params, HttpContext context, Map<String, String> heads, String charset, HttpHost proxy, int connTimeout, int readTimeout) {

        UrlEncodedFormEntity requestEntity = null;
        if (!(null == params  || params.isEmpty())) {
            ArrayList<NameValuePair> pairs = new ArrayList(params.size());
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String value = entry.getValue();

                if (value == null) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), ""));
                } else {
                    pairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }

            try {
                requestEntity = new UrlEncodedFormEntity(pairs, charset);
            } catch (UnsupportedEncodingException e) {
                CompletableFuture<Response> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }

        }
        return exec(request, requestEntity, context, heads, charset, proxy, connTimeout, readTimeout);

    }

    /**
     * HTTP Get 获取内容
     *
     * @param url     请求的url地址 ?之前的地址 不能为空
     * @param params  请求的参数 允许为空
     * @param charset 编码格式  允许为空，若为空取httpclient配置中的默认字符编码
     * @param proxy 代理地址，允许为空，若为空，则不适用代理
     * @return 页面内容
     */
    public CompletableFuture<Response> doGet(String url, Map<String, String> params, Map<String, String> heads, HttpContext context, String charset, HttpHost proxy, int connTimeout, int readTimeout) {

        if (StringUtils.isBlank(url)) {
            return CompletableFuture.completedFuture(null);
        }

        HttpGet httpGet = new HttpGet(url);


        return exec(httpGet, params, context, heads, charset, proxy, connTimeout, readTimeout);

    }


    /**
     * HTTP post 获取内容
     *
     * @param url     请求的url地址 ?之前的地址 不能为空
     * @param params  请求的参数 允许为空
     * @param charset 编码格式  允许为空，若为空取httpclient配置中的默认字符编码
     * @param proxy 代理地址，允许为空，若为空，则不适用代理
     * @return 页面内容
     */
    public CompletableFuture<Response> doPost(String url, Map<String, String> params, HttpContext context, Map<String, String> heads, String charset, HttpHost proxy) {

        if (StringUtils.isBlank(url)) {
            return CompletableFuture.completedFuture(null);
        }

        return exec(new HttpPost(url), params, context, heads, charset, proxy, -1, -1);
    }



    /**
     * HTTP Post 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param requestData  请求体字符串
     * @param charset 编码格式
     * @return 页面内容
     */
    public CompletableFuture<Response> doPost(String url, String requestData, HttpContext context, Map<String, String> heads, String charset, HttpHost proxy) {

        if (StringUtils.isBlank(url)) {
            return CompletableFuture.completedFuture(null);
        }
        return exec(new HttpPost(url) , requestData, context, heads, charset, proxy, -1, -1);
    }


    /**
     * HTTP put 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param requestData  请求体字符串
     * @param charset 编码格式
     * @return 页面内容
     */
    public CompletableFuture<Response> doPut(String url, String requestData, HttpContext context, Map<String,String> heads, String charset, HttpHost proxy) {

        if (StringUtils.isBlank(url)) {
            return CompletableFuture.completedFuture(null);
        }
        return exec(new HttpPut(url), requestData, context, heads, charset, proxy, -1, -1);
    }

    /**
     * HTTP put 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param parmas  请求体
     * @param charset 编码格式
     * @return 页面内容
     */
    public CompletableFuture<Response> doPut(String url, Map<String, String> parmas, HttpContext context, Map<String,String> heads, String charset, HttpHost proxy) {

        if (StringUtils.isBlank(url)) {
            return CompletableFuture.completedFuture(null);
        }
        return exec(new HttpPut(url), parmas, context, heads, charset, proxy, -1, -1);
    }

    /**
     * 执行请求
     * @param request 请求对象
     * @param requestEntity 请求数据体
     * @param heads 头信息
     * @param charset 编码
     * @param proxy 代理
     * @return
     */
    public CompletableFuture<Response> exec(HttpRequestBase request, HttpEntity requestEntity, HttpContext context, Map<String, String> heads, String charset, HttpHost proxy, int connTimeout, int readTimeout) {
        MyCompletableFuture<Response> future = new MyCompletableFuture<>();

        RequestConfig.Builder cfgBuilder = RequestConfig.copy(defaultConfig);
        if(null != proxy) {
            cfgBuilder.setProxy(proxy);
        }

        if(-1 != readTimeout){
            cfgBuilder.setSocketTimeout(readTimeout);
        }

        if(-1 != connTimeout){
            cfgBuilder.setConnectTimeout(connTimeout);
            cfgBuilder.setConnectionRequestTimeout(connTimeout);
        }

        request.setConfig(cfgBuilder.build());

        if(heads != null) {
            for(String key : heads.keySet()) {
                request.addHeader(key, heads.get(key));
            }
        }
        if(null == charset || charset.isEmpty()) {
            charset = this.defaultCharset;
        }



        if (null != requestEntity) {
            //如果是将参数写入entity的
            if(request instanceof HttpEntityEnclosingRequest) {
                ((HttpEntityEnclosingRequest)request).setEntity(requestEntity);
            } else {
                try {
                    request.setURI(URI.create(request.getURI().toString() + "?" + EntityUtils.toString(requestEntity)));
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }
        }

        final String finalCharset = charset;

        FutureCallback<HttpResponse> fc = new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                synchronized (future) {
                    Response response = new Response(request, httpResponse, finalCharset);
                    future.complete(response);

                }
            }
            @Override
            public void failed(Exception e) {
                logger.debug("urras request 失败", e);
                future.completeExceptionally(new IOException(request.toString() + "失败", e));
            }

            public void cancelled() {
                logger.debug("urras request 取消");
                future.cancel(true);
            }
        };

        if(null == context) {
            Future<HttpResponse> f = asyncHttpClient.execute(request, fc);
            future.setCancelHandler(f::cancel);
        } else {
            Future<HttpResponse> f = asyncHttpClient.execute(request, context, fc);
            future.setCancelHandler(f::cancel);
        }
        return future;
    }
}
