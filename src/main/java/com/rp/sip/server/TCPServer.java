package com.rp.sip.server;

import com.rp.sip.codec.*;
import com.rp.sip.component.MessageType;
import com.rp.sip.controller.BusinessController;
import com.rp.sip.db.mapper.SipSettingDAO;
import com.rp.sip.dispatcher.BusinessDispatcher;
import com.rp.sip.mapper.TransactionMapper;
import com.rp.sip.model.SIPInfo;
import com.rp.sip.utils.CommonUtils;
import com.rp.sip.utils.SpringBeanUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.io.IOUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by RP on 2017/5/3.
 */
@Component("nettyServer")
public class TCPServer {

    private Logger loggerMsg = LogManager.getLogger("com.rp.sip.SipMsg");
    private Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private EventExecutorGroup executorGroup;
    private EventLoopGroup primary;
    private EventLoopGroup secondary;

    private Map<String, Object> setting;

    public void startup() {
        initParam();
        this.executorGroup = new DefaultEventExecutorGroup((Integer) setting.get("connectNum"));
        ServerBootstrap bootstrap = new ServerBootstrap();

        // 设置 半双工 或 全双工
        if (Boolean.valueOf((String) setting.get("isFullDuplex"))) {
            primary = new NioEventLoopGroup((Integer) setting.get("connectNum"));// 通过nio方式来接收连接和处理连接
            secondary = new NioEventLoopGroup((Integer) setting.get("IONum"));
            bootstrap.group(primary, secondary);
            bootstrap.channel(NioServerSocketChannel.class);// 设置nio类型的channel
        } else {
            primary = new OioEventLoopGroup((Integer) setting.get("connectNum"));// 通过nio方式来接收连接和处理连接
            secondary = new OioEventLoopGroup((Integer) setting.get("IONum"));
            bootstrap.group(primary, secondary);
            bootstrap.channel(OioServerSocketChannel.class);// 设置nio类型的channel
        }

        bootstrap.localAddress(new InetSocketAddress((String) setting.get("host"), (Integer) setting.get("port")));// 设置监听端口
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.option(ChannelOption.SO_TIMEOUT, 30000);
        bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(64, 1024, 65536));
        bootstrap.childOption(ChannelOption.SO_RCVBUF, 2048);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {//有连接到达时会创建一个channel
            protected void initChannel(SocketChannel ch) throws Exception {

                //   pipeline管理channel中的Handler，在channel队列中添加一个handler来处理业务
                ch.pipeline().channel().config().setAutoRead(true);
                // decoder
                boolean lengthIncludesLengthFieldLength = Boolean.valueOf((String) setting.get("lengthIncludesLengthFieldLength"));
                ch.pipeline().addLast("replayingDecoder", new SipReplayingDecoder((Integer) setting.get("lengthFieldLength"), lengthIncludesLengthFieldLength));
                ch.pipeline().addLast("byteToMessageDecoder", new LengthFieldByteToMessageDecoder((Integer) setting.get("lengthFieldLength"), (Integer) setting.get("lengthFieldOffset"), (String) setting.get("charset")));
                ch.pipeline().addLast("txCodeDecoder", new TxCodeDecoder((String) setting.get("charset"), MessageType.valueOf((String) setting.get("msgType"))));
                ch.pipeline().addLast("messageDecoder", new MessageDecoder((String) setting.get("charset"), MessageType.valueOf((String) setting.get("msgType"))));
                // business operate
                ch.pipeline().addLast("transactionMapper", new TransactionMapper());
                ch.pipeline().addLast("businessDispatcher", new BusinessDispatcher((String) setting.get("charset")));
                ch.pipeline().addLast(executorGroup, "idleStateHandler", new IdleStateHandler(10, 10, 0));
                ch.pipeline().addLast(executorGroup, "businessController", new BusinessController());
                // encoder
                ch.pipeline().addLast("lengthFieldPrepender", new LengthFieldPrepender((Integer) setting.get("lengthFieldLength"), lengthIncludesLengthFieldLength, (String) setting.get("charset")));
                ch.pipeline().addLast("messageEncoder", new MessageEncoder((String) setting.get("charset"), MessageType.valueOf((String) setting.get("msgType"))));

            }
        });
        try {
            ChannelFuture f = bootstrap.bind().sync();// 配置完成，开始绑定server，通过调用sync同步方法阻塞直到绑定成功
            logger.info(MethodHandles.lookup().lookupClass() + " started and listen on " + f.channel().localAddress().toString());
            loggerMsg.info(MethodHandles.lookup().lookupClass() + " started and listen on " + f.channel().localAddress().toString());
            f.channel().closeFuture().sync();// 应用程序会一直等待，直到channel关闭
        } catch (InterruptedException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
        } finally {
            shutdownGracefully();//关闭EventLoopGroup，释放掉所有资源包括创建的线程
        }
    }

    public void shutdownGracefully() {
        try {
            primary.shutdownGracefully().sync();
            secondary.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            CommonUtils.getCommonUtils().printExceptionFormat(logger, e);
            CommonUtils.getCommonUtils().printExceptionFormat(loggerMsg, e);
        }
    }

    private void initParam() {
        SipSettingDAO settingDAO = SpringBeanUtils.UTILS.getSpringBeanByType(SipSettingDAO.class);
        SIPInfo info = (SIPInfo) SpringBeanUtils.UTILS.getSpringBeanById("sip-info");
        this.setting = settingDAO.querySetting(info.getServerId());
    }


}
