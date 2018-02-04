package com.rp.sip.route;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * Created by RP on 2017/4/26.
 */
public class TcpIpClient {

    private static EventLoopGroup group = new NioEventLoopGroup(64);
    private static Bootstrap bootstrap = new Bootstrap();
    private static FixedChannelPool pool = null;

    //private Logger loggerErr = LogManager.getLogger("heartbeatErr");

//    static {
//        bootstrap.group(group).channel(EpollSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000).handler(new ChannelInitializer<SocketChannel>() {
//            protected void initChannel(SocketChannel ch) throws Exception {
//                TcpIpClient.pool = new FixedChannelPool(bootstrap, new ChannelPoolHandler() {
//                    @Override
//                    public void channelReleased(Channel ch) throws Exception {
//
//                    }
//
//                    @Override
//                    public void channelAcquired(Channel ch) throws Exception {
//
//                    }
//
//                    @Override
//                    public void channelCreated(Channel ch) throws Exception {
//                        ch.pipeline().addLast(new SimpleLengthFieldPrepender(8, false, "GBK"));
//                        ch.pipeline().addLast(new SimpleLengthFieldDecoder(8, 0, "GBK"));
//                        ch.pipeline().addLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS));
//                        ch.pipeline().addLast(new ReceiveMsgHandler());
//                    }
//                }, 8);
//            }
//        });
//    }
//
// //   private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
//
//
//  //  private Logger loggerMsg = LogManager.getLogger("heartbeatMsg");
//
//
//
//    public void sendMsg(ByteBuf msg) {
//        Channel future = getChannelFromPool();
//        future.write(msg);
//        future.writeAndFlush(Unpooled.EMPTY_BUFFER);
//    }
//
//    public void sendAndReceiveMsg(ByteBuf msg) throws InterruptedException {
//        Channel future = getChannelFromPool();
//        future.write(msg);
//        future.writeAndFlush(Unpooled.EMPTY_BUFFER);
//        HostCallBack callBack = new HostCallBack(future, 30000);
//        ByteBuf respMsg = callBack.getResponseByteBuf();
//        pool.release(future);
//        if (respMsg == null) {
//            logger.error("接收业务平台响应失败!!!");
//            loggerMsg.error("接到业务平台响应失败!!!");
//            return;
//        }
//        logger.info("收到业务平台响应:" + respMsg.toString(Charset.forName(configManager.getCharset())));
//        loggerMsg.info("收到业务平台响应:" + respMsg.toString(Charset.forName(configManager.getCharset())));
//        respMsg.release();
//    }
//
//    public void receiveMsg() throws InterruptedException {
//        Channel future = getChannelFromPool();
//        HostCallBack callBack = new HostCallBack(future, 30000);
//        TcpIpClient.pool.release(future);
//        ByteBuf respMsg = callBack.getResponseByteBuf();
//        if (respMsg == null) {
//            logger.error("接收业务平台响应失败!!!");
//            loggerMsg.error("接到业务平台响应失败!!!");
//            return;
//        }
//        logger.info("收到业务平台响应:" + respMsg.toString(Charset.forName(configManager.getCharset())));
//        loggerMsg.info("收到业务平台响应:" + respMsg.toString(Charset.forName(configManager.getCharset())));
//        respMsg.release();
//
//    }
//
//    public Channel getChannelFromPool() {
//        Channel future = null;
//        try {
//            future = TcpIpClient.pool.acquire().get();
//        } catch (InterruptedException e) {
//            logger.error(CommonUtils.getCommonUtils().printExceptionFormat(e));
//            loggerErr.error(CommonUtils.getCommonUtils().printExceptionFormat(e));
//        } catch (ExecutionException e) {
//            logger.error(CommonUtils.getCommonUtils().printExceptionFormat(e));
//            loggerErr.error(CommonUtils.getCommonUtils().printExceptionFormat(e));
//        }
//        return future;
//    }
//
//
//    public void shutdownGracefully() {
//        // 退出，释放资源
//        group.shutdownGracefully();
//    }
//
//    public void startup() {
//        try {
//            sendAndReceiveMsg(Unpooled.copiedBuffer(configManager.getMsgHandler().writeXmlMsg(null)));
//        } catch (InterruptedException e) {
//            logger.error(CommonUtils.getCommonUtils().printExceptionFormat(e));
//            loggerErr.error(CommonUtils.getCommonUtils().printExceptionFormat(e));
//        }
//    }

}
