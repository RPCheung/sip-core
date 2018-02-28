package com.rp.sip.route;

import com.alibaba.fastjson.JSON;
import com.rp.sip.component.HttpMethod;
import com.rp.sip.component.IMessageInterceptor;
import com.rp.sip.component.MessageObject;
import com.rp.sip.db.mapper.RoutePoolSettingDAO;
import com.rp.sip.message.DefaultMessageObject;
import com.rp.sip.route.handlers.HttpTrustedHandler;
import com.rp.sip.route.packer.PackMessage;
import com.rp.sip.utils.CommonUtils;
import com.rp.sip.utils.MsgUtils;
import com.rp.sip.utils.SpringBeanUtils;
import io.netty.buffer.ByteBuf;
import org.apache.commons.io.IOUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.rp.sip.component.HttpMethod.GET;
import static com.rp.sip.component.HttpMethod.POST;

/**
 * Created by cheungrp on 18/2/26.
 */
public class HttpRoute implements IRoute {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private CloseableHttpClient client;
    private volatile CloseableHttpResponse response;

    private Map<String, String> headers = new ConcurrentHashMap<>(16);

    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    private PackMessage packMessage;
    private IMessageInterceptor messageInterceptor;
    private RequestConfig requestConfig;

    private int timeout;
    private Charset charset;

    private HttpTrustedHandler trustedHandler;
    private HttpMethod method;
    private String uri;


    public HttpRoute(
            String uri,
            PackMessage packMessage,
            IMessageInterceptor messageInterceptor,
            HttpMethod method,
            HttpTrustedHandler trustedHandler,
            int timeout,
            Charset charset) {
        this.uri = uri;
        this.trustedHandler = trustedHandler;
        this.method = method;
        this.packMessage = packMessage;
        this.messageInterceptor = messageInterceptor;
        this.timeout = timeout;
        this.charset = charset;
        init();
    }

    private void init() {

        int timeout = new Long(TimeUnit.SECONDS.toMillis(this.timeout)).intValue();
        requestConfig = RequestConfig.custom()
                .setSocketTimeout(timeout)
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .build();

        SSLContextBuilder builder = new SSLContextBuilder();

        try {
            builder.loadTrustMaterial(null, (x509Certificates, authType) -> trustedHandler == null || trustedHandler.isTrusted(x509Certificates, authType));
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build(), new String[]{"SSLv3", "TLSv1", "TLSv1.2"}, null,
                    NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register(HTTP, new PlainConnectionSocketFactory()).register(HTTPS, sslConnectionSocketFactory).build();
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
            cm.setMaxTotal(Integer.parseInt((String) getRoutePoolSetting().get("maxConnections")));// max connection

            client = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).setConnectionManager(cm).build();
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
        }

    }

    @Override
    public MessageObject sendAndReceiveMsg4User(MessageObject messageObject) throws InterruptedException {
        try {
            MessageObject responseMessageObject;
            byte[] messageBytes = null;
            if (messageInterceptor != null) {

                // 有些GET请求 不需要请求报文
                if (messageObject != null) {
                    // 打包
                    messageObject = messageInterceptor.beforeMarshal(messageObject);
                    ByteBuf message = packMessage.packMessage(messageObject);
                    messageBytes = MsgUtils.UTILS.byteBuf2Bytes(message);
                    messageBytes = messageInterceptor.afterMarshal(messageBytes);
                }
                // 初始化请求
                makeExecute(messageBytes);
                // 获取响应
                byte[] responseBytes = getResponseBytes();
                logger.info("路由收到响应:" + IOUtils.toString(responseBytes, charset.toString()));
                loggerMsg.info("路由收到响应:" + IOUtils.toString(responseBytes, charset.toString()));
                responseBytes = messageInterceptor.beforeUnmarshal(responseBytes);
                // 解包
                responseMessageObject = packMessage.unpackMessage(MsgUtils.UTILS.bytes2ByteBuf(responseBytes));
                responseMessageObject = messageInterceptor.afterUnmarshal(responseMessageObject);
                // 释放 Entity
                EntityUtils.consume(this.response.getEntity());
            } else {
                // 有些GET请求 不需要请求报文
                if (messageObject != null) {
                    // 打包
                    ByteBuf message = packMessage.packMessage(messageObject);
                    messageBytes = MsgUtils.UTILS.byteBuf2Bytes(message);
                }
                // 初始化请求
                makeExecute(messageBytes);
                // 获取响应
                byte[] responseBytes = getResponseBytes();
                logger.info("路由收到响应:" + IOUtils.toString(responseBytes, charset.toString()));
                loggerMsg.info("路由收到响应:" + IOUtils.toString(responseBytes, charset.toString()));
                // 解包
                responseMessageObject = packMessage.unpackMessage(MsgUtils.UTILS.bytes2ByteBuf(responseBytes));
                // 释放 Entity
                EntityUtils.consume(this.response.getEntity());
            }

            return responseMessageObject;
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                logger.error("路由接收响应失败!!!");
                loggerMsg.error("路由接收响应失败!!!");
            }
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
            return null;
        }
    }

    @Override
    public void setAssociationId(String associationId) {

    }

    @Override
    public void addHttpRequestHeader(String key, String value) {
        headers.put(key, value);
    }

    @Override
    public Header[] getResponseAllHeaders() {
        return this.response.getAllHeaders();
    }


    private void makeExecute(byte[] message) throws IOException {

        switch (method) {
            case GET: {
                String getContent;
                if (message != null) {
                    ByteBuf messageBuf = MsgUtils.UTILS.bytes2ByteBuf(message);
                    getContent = uri + "?" + packMessage.unpackMessage(messageBuf).toString();
                } else {
                    getContent = uri;
                }
                HttpGet get = new HttpGet(getContent);
                get.setConfig(requestConfig);
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    get.addHeader(entry.getKey(), entry.getValue());
                }
                headers.clear();
                logger.info("路由请求路径: " + get.getURI().toASCIIString() + "  请求方式为: " + GET);
                loggerMsg.info("路由请求路径: " + get.getURI().toASCIIString() + "  请求方式为: " + GET);
                response = client.execute(get);
                return;
            }
            case POST: {
                HttpPost post = new HttpPost(uri);
                post.setConfig(requestConfig);
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    post.addHeader(entry.getKey(), entry.getValue());
                }
                headers.clear();
                post.setEntity(new ByteArrayEntity(message));
                logger.info("路由请求路径: " + post.getURI().toASCIIString() + "  请求方式为: " + POST);
                loggerMsg.info("路由请求路径: " + post.getURI().toASCIIString() + "  请求方式为: " + POST);
                logger.info("路由发送数据: " + IOUtils.toString(message, charset.toString()));
                loggerMsg.info("路由发送数据: " + IOUtils.toString(message, charset.toString()));
                response = client.execute(post);
                return;
            }
            default: {
            }
        }
    }

    private Map<String, Object> getRoutePoolSetting() {
        RoutePoolSettingDAO routePoolSetting = SpringBeanUtils.UTILS.getSpringBeanByType(RoutePoolSettingDAO.class);
        return routePoolSetting.queryRoutePoolSetting();
    }

    private byte[] getResponseBytes() throws IOException {
        return EntityUtils.toByteArray(this.response.getEntity());
    }


}
