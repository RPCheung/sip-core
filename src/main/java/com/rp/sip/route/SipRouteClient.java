package com.rp.sip.route;

import com.rp.sip.codec.SipReplayingDecoder;
import com.rp.sip.component.IMessageInterceptor;
import com.rp.sip.component.MessageObject;
import com.rp.sip.db.mapper.*;
import com.rp.sip.route.codec.LengthFieldByteToMessageDecoder;
import com.rp.sip.route.codec.LengthFieldPrepender;
import com.rp.sip.route.handlers.ReceiveMsgHandler;
import com.rp.sip.route.packer.PackMessage;
import com.rp.sip.utils.CommonUtils;
import com.rp.sip.utils.MsgUtils;
import com.rp.sip.utils.SpringBeanUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ExecutionException;


/**
 * Created by RP on 2017/4/26.
 */
public class SipRouteClient {

    private static EventLoopGroup group = null;
    private static Bootstrap bootstrap = new Bootstrap();
    private static ChannelPoolMap<String, FixedChannelPool> poolMap;

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public void init() {

        if (!(Boolean.valueOf((String) getSettings().get("isUserRoute")))) {
            return;
        }
        group = new NioEventLoopGroup(Integer.parseInt((String) getRouteSetting().get("IONum")));
        bootstrap.group(group)
                .remoteAddress((String) getRouteSetting().get("route_host"), Integer.parseInt((String) getRouteSetting().get("route_port")))
                .channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000);
        SipRouteClient.poolMap = new AbstractChannelPoolMap<String, FixedChannelPool>() {
            @Override
            protected FixedChannelPool newPool(String key) {
                ChannelPoolHandler handler = new ChannelPoolHandler() {
                    @Override
                    public void channelReleased(Channel ch) throws Exception {
                        logger.info("与 [" + ch.remoteAddress() + "] 关闭了连接.");
                        loggerMsg.info("与 [" + ch.remoteAddress() + "] 关闭了连接.");
                    }

                    @Override
                    public void channelCreated(Channel ch) throws Exception {
                        ch.pipeline().addLast(new LengthFieldPrepender(Integer.parseInt((String) getRouteSetting().get("lengthFieldLength")),
                                Boolean.valueOf((String) getRouteSetting().get("lengthIncludesLengthFieldLength")),
                                (String) getRouteSetting().get("charset")));
                        ch.pipeline().addLast(new SipReplayingDecoder(Integer.parseInt((String) getRouteSetting().get("lengthFieldLength")),
                                Boolean.valueOf((String) getRouteSetting().get("lengthIncludesLengthFieldLength"))));
                        ch.pipeline().addLast(new LengthFieldByteToMessageDecoder(Integer.parseInt((String) getRouteSetting().get("lengthFieldLength")),
                                Integer.parseInt((String) getRouteSetting().get("lengthFieldOffset")),
                                (String) getRouteSetting().get("charset")));
                        ch.pipeline().addLast(new ReceiveMsgHandler());
                    }

                    @Override
                    public void channelAcquired(Channel ch) throws Exception {

                    }
                };
                return new FixedChannelPool(bootstrap, handler, 2);
            }
        };
        logger.info("路由始化完成.");
        loggerMsg.info("路由始化完成.");
    }


    private void sendMsg(ByteBuf msg) {
        Channel future = getChannelFromPool();
        future.write(msg);
        future.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    public MessageObject sendAndReceiveMsg4User(MessageObject messageObject) throws InterruptedException {

        ByteBuf msg;

        PackMessage packMessage;
        IMessageInterceptor iMessageInterceptor;

        if (SpringBeanUtils.UTILS.isContainsBean4Id("customRoutePackMessage")) {
            packMessage = (PackMessage) SpringBeanUtils.UTILS.getSpringBeanById("customRoutePackMessage");

        } else {
            packMessage = (PackMessage) SpringBeanUtils.UTILS.getSpringBeanById("routePackMessage");
        }

        iMessageInterceptor = (IMessageInterceptor) SpringBeanUtils.UTILS.getSpringBeanById("routeMessageInterceptor");

        if (iMessageInterceptor != null) {
            msg = packMessage.packMessage(iMessageInterceptor.beforeMarshal(messageObject));
            msg = MsgUtils.UTILS.bytes2ByteBuf(iMessageInterceptor.afterMarshal(MsgUtils.UTILS.byteBuf2Bytes(msg)));
            msg = sendAndReceiveMsg4Route(msg);
            msg = MsgUtils.UTILS.bytes2ByteBuf(iMessageInterceptor.afterMarshal(MsgUtils.UTILS.byteBuf2Bytes(msg)));
            return packMessage.unpackMessage(msg);

        } else {
            msg = packMessage.packMessage(messageObject);

            msg = sendAndReceiveMsg4Route(msg);
            return packMessage.unpackMessage(msg);
        }
    }

    private ByteBuf sendAndReceiveMsg4Route(ByteBuf msg) throws InterruptedException {
        Channel future = getChannelFromPool();
        logger.info("路由发送请求: " + msg.toString(Charset.forName((String) getRouteSetting().get("charset"))));
        loggerMsg.info("路由发送请求: " + msg.toString(Charset.forName((String) getRouteSetting().get("charset"))));
        future.write(msg);
        future.writeAndFlush(Unpooled.EMPTY_BUFFER);
        HostCallBack callBack = new HostCallBack(future, 30);
        ByteBuf respMsg = callBack.getResponseByteBuf();
        SipRouteClient.poolMap.get((String) getRouteSetting().get("route_host")).release(future);

        if (respMsg == null) {
            logger.error("路由接收响应失败!!!");
            loggerMsg.error("路由接收响应失败!!!");
            return null;
        }
        logger.info("路由收到响应:" + respMsg.toString(Charset.forName((String) getRouteSetting().get("charset"))));
        loggerMsg.info("路由收到响应:" + respMsg.toString(Charset.forName((String) getRouteSetting().get("charset"))));
        return respMsg;
    }

    private ByteBuf receiveMsg() throws InterruptedException {
        Channel future = getChannelFromPool();
        HostCallBack callBack = new HostCallBack(future, 30);
        SipRouteClient.poolMap.get((String) getRouteSetting().get("route_host")).release(future);
        ByteBuf respMsg = callBack.getResponseByteBuf();
        if (respMsg == null) {
            logger.error("路由接收响应失败!!!");
            loggerMsg.error("路由接收响应失败!!!");
            return null;
        }
        logger.info("路由收到响应:" + respMsg.toString(Charset.forName((String) getRouteSetting().get("charset"))));
        loggerMsg.info("路由收到响应:" + respMsg.toString(Charset.forName((String) getRouteSetting().get("charset"))));
        return respMsg;

    }

    public Channel getChannelFromPool() {
        Future<Channel> future;
        Channel channel = null;
        try {
            future = SipRouteClient.poolMap.get((String) getRouteSetting().get("route_host")).acquire();
            channel = future.get();
            logger.info("与 [" + channel.remoteAddress() + "] 创建了连接.");
            loggerMsg.info("与 [" + channel.remoteAddress() + "] 创建了连接.");
        } catch (InterruptedException | ExecutionException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
        }
        return channel;
    }


    public void shutdownGracefully() {
        // 退出，释放资源
        group.shutdownGracefully();
    }

    private Map<String, Object> getRouteSetting() {
        RouteSettingDAO routeSetting = SpringBeanUtils.UTILS.getSpringBeanByType(RouteSettingDAO.class);
        return routeSetting.querySetting((String) getSettings().get("route_id"));
    }

    private Map<String, Object> getSettings() {
        SipSettingDAO settingDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipSettingDAO.class);
        return settingDAO.querySetting();
    }

    private Map<String, Object> getTran(String host, String txCode) {
        SipTranDAO sipTranDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipTranDAO.class);
        return sipTranDAO.queryTranByTxCode(host, txCode);
    }

    private Map<String, Object> getRouteTran(String txCode) {
        String host = (String) getSettings().get("host");
        String routeTranId = (String) getTran(host, txCode).get("route_tran_id");
        RouteTranDAO routeTranDAO = SpringBeanUtils.UTILS.getSpringBeanByType(RouteTranDAO.class);
        return routeTranDAO.queryTran(routeTranId);
    }

}
