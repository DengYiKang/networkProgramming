package server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChatServerHandler extends SimpleChannelInboundHandler<String> {

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 客户端链接时，handlerAdded会执行
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel inComing = ctx.channel();
        for (Channel channel : channels) {
            if (channel != inComing) {
                channel.writeAndFlush("[" + inComing.remoteAddress() + "] enter！\n");
            }
        }
        channels.add(inComing);
    }

    /**
     * 断开连接时会执行
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel outComing = ctx.channel();
        for (Channel channel : channels) {
            if (channel != outComing) {
                channel.writeAndFlush("[" + outComing.remoteAddress() + "] leave\n");
            }
        }
        channels.remove(outComing);
    }

    /**
     * 对于TCP来说，建立连接后为active，对于UDP，被打开后为active
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel inComing = ctx.channel();
        System.out.println("[" + inComing.remoteAddress() + "]: online");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel outComing = ctx.channel();
        System.out.println("[" + outComing.remoteAddress() + "]: offline");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel inComing = ctx.channel();
        System.out.println(inComing.remoteAddress() + "exception founded");
        ctx.close();
    }

    /**
     * 注意，writeAndFlush的内容需要换行符！
     *
     * @param channelHandlerContext
     * @param s
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        Channel inComing = channelHandlerContext.channel();
        for (Channel channel : channels) {
            if (channel != inComing) {
                channel.writeAndFlush("[user " + inComing.remoteAddress() + "]: " + s + "\n");
            } else {
                channel.writeAndFlush("[Me]: " + s + "\n");
            }
        }
    }
}
