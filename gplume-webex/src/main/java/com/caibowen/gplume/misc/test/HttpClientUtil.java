/*
 * *****************************************************************************
 *  Copyright 2014 Bowen Cai
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * *****************************************************************************
 */

package com.caibowen.gplume.misc.test;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.Serializable;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * http 最新工具类
 */
public final class HttpClientUtil {
    private final static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    public static String get(String url) {
        return get(url, null, 3, Consts.UTF_8);
    }

    public static String get(String url, int connectTimeout) {
        return get(url, null, connectTimeout, Consts.UTF_8);
    }

    public static String get(String url, Charset charset) {
        return get(url, null, 3, charset);
    }

    public static String get(String url, Map<String, String> params) {
        return get(url, params, 3, Consts.UTF_8);
    }

    public static String get(String url, Map<String, String> params, int connectTimeout) {
        return get(url, params, connectTimeout, Consts.UTF_8);
    }

    public static String post(String url) {
        return post(url, null, 3, Consts.UTF_8);
    }

    public static String post(String url, int connectTimeout) {
        return post(url, null, connectTimeout, Consts.UTF_8);
    }

    public static String post(String url, Charset charset) {
        return post(url, null, 3, charset);
    }

    public static String post(String url, Map<String, String> params) {
        return post(url, params, 3, Consts.UTF_8);
    }


    public static String post(String url, Map<String, String> params, int connectTimeout) {
        return post(url, params, connectTimeout, Consts.UTF_8);
    }

    /**
     * http get
     *
     * @param url
     * @param params
     * @param connectTimeout
     * @return
     */
    public static String get(String url, Map<String, String> params, int connectTimeout, Charset charset) {
        final String uri = setParam(url, params, charset);
        final HttpGet get = new HttpGet(uri);
        get.setConfig(buildConfig(connectTimeout, connectTimeout));
        try {
            final CloseableHttpResponse response = httpClient.execute(get);
            try {
                final HttpEntity entity = response.getEntity();
                if (entity != null) return EntityUtils.toString(entity, charset);
            } catch (Exception e) {
                logger.error(String.format("[HttpUtils Get] get response error, url:%s", uri), e);
            } finally {
                if (response != null) response.close();
            }
        } catch (SocketTimeoutException e) {
            logger.error(String.format("[HttpUtils Get] invoke timeout error, url:%s", uri));
        } catch (SocketException e) {
            logger.error(String.format("[HttpUtils Get] invoke connection refused, url:%s", uri));
        } catch (Exception e) {
            logger.error(String.format("[HttpUtils Get] invoke error, url:%s", uri), e);
        } finally {
            get.releaseConnection();
        }
        return null;
    }

    /**
     * HTTPS请求
     *
     * @param url
     * @param params
     * @return
     */
    public static String post(String url, Map<String, String> params, int connectTimeout, Charset charset) {
        final HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(buildConfig(connectTimeout, connectTimeout));
        try {
            setParam(httpPost, params, charset);
            final CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                // 执行POST请求
                final HttpEntity entity = response.getEntity(); // 获取响应实体
                if (null != entity) return EntityUtils.toString(entity, charset);
            } catch (Exception e) {
                logger.error("[HttpUtils Post] get response error, url:" + url, e);
            } finally {
                if (response != null) response.close();
            }
        } catch (ClientProtocolException e) {
            logger.error("[HttpUtils Post] invoke timeout, url=" + url, e);
        } catch (Exception e) {
            logger.error("[HttpUtils Post] invoke error, url=" + url, e);
        } finally {
            httpPost.releaseConnection();
        }
        return null;
    }

    public static byte[] getBytes(String url, Map<String, String> params, int connectTimeout) {
        final String uri = setParam(url, params, Consts.UTF_8);
        final HttpGet get = new HttpGet(uri);
        get.setConfig(buildConfig(connectTimeout, connectTimeout));
        try {
            final CloseableHttpResponse response = httpClient.execute(get);
            try {
                final HttpEntity entity = response.getEntity();
                if (entity.getContentLength() > 0)
                    return EntityUtils.toByteArray(entity);
                logger.error("[HttpUtils Get]get content error,content=" + EntityUtils.toString(entity));
            } catch (Exception e) {
                logger.error(String.format("[HttpUtils Get]get response error, url:%s", uri), e);
            } finally {
                if (response != null) response.close();
            }
        } catch (SocketTimeoutException e) {
            logger.error(String.format("[HttpUtils Get]invoke get timeout error, url:%s", uri), e);
        } catch (Exception e) {
            logger.error(String.format("[HttpUtils Get]invoke get error, url:%s", uri), e);
        } finally {
            get.releaseConnection();
        }
        return null;
    }


    private static RequestConfig buildConfig(int connectTimeout, int soTimeout) {
        return RequestConfig.custom()
                .setSocketTimeout(soTimeout * 1000)
                .setConnectTimeout(connectTimeout * 1000)
                .setConnectionRequestTimeout(connectTimeout * 1000).build();
    }

    public static HttpEntityEnclosingRequestBase setParam(HttpEntityEnclosingRequestBase request,
                                                          Map<String, ? extends Serializable> params,
                                                          Charset charset) {
        if (charset == null) charset = Consts.UTF_8;
        if (params != null && params.size() > 0) {
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(getParamsList(params), charset);
            request.setEntity(formEntity);
        }
        return request;
    }

    /**
     * Get方式提交,URL中不包含查询参数, 格式：http://www.g.cn
     *
     * @param url    提交地址
     * @param params 查询参数集, 键/值对
     * @return 响应消息
     */
    public static String setParam(String url, Map<String, ? extends Serializable> params, Charset charset) {
        if (charset == null) charset = Consts.UTF_8;
        if (params != null && params.size() > 0) {
            List<NameValuePair> qparams = getParamsList(params);
            if (qparams != null && qparams.size() > 0) {
                String formatParams = URLEncodedUtils.format(qparams, charset);
                url = url + (url.indexOf("?") < 0 ? "?" : "&") + formatParams;
            }
        }

        return url;
    }

    /**
     * 将传入的键/值对参数转换为NameValuePair参数集
     *
     * @param paramsMap 参数集, 键/值对
     * @return NameValuePair参数集
     */
    private static List<NameValuePair> getParamsList(Map<String, ? extends Serializable> paramsMap) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (Map.Entry<String, ? extends Serializable> map : paramsMap.entrySet()) {
            params.add(new BasicNameValuePair(map.getKey(), map.getValue().toString()));
        }
        return params;
    }

    private static CloseableHttpClient httpClient = HttpClients.custom()
            .setDefaultSocketConfig(SocketConfig.custom()
                    .setTcpNoDelay(true)/*.setSoKeepAlive(false)*/.build())
            .setDefaultConnectionConfig(ConnectionConfig.custom()
                    .setMalformedInputAction(CodingErrorAction.IGNORE)
                    .setUnmappableInputAction(CodingErrorAction.IGNORE)
                    .setCharset(Consts.UTF_8)
                    .setMessageConstraints(MessageConstraints.custom()
                            .setMaxHeaderCount(200)
                            .setMaxLineLength(2000)
                            .build())
                    .build())
            .setMaxConnTotal(1000)
            .setMaxConnPerRoute(800)
            .disableAutomaticRetries()
            .disableCookieManagement()
            .disableContentCompression()
            .disableAuthCaching()
            .disableConnectionState()
            .build();


}