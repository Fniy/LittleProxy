package com.netty.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class HttpClient {

    boolean needSleep;

    private static final HttpClient instance = new HttpClient();

    public static HttpClient getInstance() {
        return instance;
    }

    public void send(String uri, FullHttpRequest request) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                // 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
                ch.pipeline().addLast(new HttpResponseDecoder());
                // 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
                ch.pipeline().addLast(new HttpRequestEncoder());

                // Deals with fragmentation in http traffic:
                ch.pipeline().addLast("inflater", new HttpContentDecompressor());
                ch.pipeline().addLast("aggregator", new HttpObjectAggregator(
                        Integer.MAX_VALUE));

                ch.pipeline().addLast(new com.netty.test.HttpClientInboundHandler());
            }
        });

        // Start the client.
        ChannelFuture f = b.connect("47.106.44.119", 80).await();
        System.out.println("连接成功.");

        // 发送http请求
        f.channel().write(request);
        f.channel().flush();
        f.channel().closeFuture().sync();
    }
    boolean bool;
    public synchronized void newThread() throws Exception {
        if(bool) return;

        bool = true;
        new Thread(){
            @Override
            public void run() {
                while (true) {

                    needSleep = true;

                    try {

                        HashMap<String, FullHttpRequest> requestMap = Cao.getInstance().copy();
                        Iterator<String> iterator = requestMap.keySet().iterator();
                        while (iterator.hasNext()) {
                            String uri = iterator.next();
                            System.out.println("repeat: " + uri);

                            try {

                                send(uri, Cao.getInstance().get(uri).copy());

                                needSleep = false;

                                Thread.sleep(5000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {

                        if (needSleep) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                }
            }
        }.start();
    }
}
