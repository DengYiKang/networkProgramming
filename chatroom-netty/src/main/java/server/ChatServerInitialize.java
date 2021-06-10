package server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * ChannelInitializer继承于ChannelInboundHandler接口，
 * ChannelInitializer是一个抽象类，不能直接使用
 * 用于在某个Channel注册到EventLoop后，对这个Channel执行一些初始化操作。
 * ChannelInitializer虽然会在一开始会被注册到Channel相关的pipeline里，但是在初始化完成之后，ChannelInitializer会将自己从pipeline中移除，不会影响后续的操作
 */
public class ChatServerInitialize extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        System.out.println("客户端连接：" + socketChannel.remoteAddress());
        ChannelPipeline pipeline = socketChannel.pipeline();
        /**
         * 发送的数据在管道里是无缝流动的，在数据量很大时，为了分割数据，采用以下几种方法
         * 定长方法
         * 固定分隔符
         * 将消息分成消息体和消息头，在消息头中用一个数组说明消息体的长度
         * 注意，这里使用的是行分隔符
         * 因此writeAndFlush的内容必须带有换行符！！！！！！！！！！
         */
        pipeline.addLast("frame", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast("decode", new StringDecoder());//解码器
        pipeline.addLast("encode", new StringEncoder());
        pipeline.addLast("handler", new ChatServerHandler());
    }
}
