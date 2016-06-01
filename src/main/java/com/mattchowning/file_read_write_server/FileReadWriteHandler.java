package com.mattchowning.file_read_write_server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SystemPropertyUtil;

public class FileReadWriteHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String RELATIVE_FILE_PATH = "src/main/java/com/mattchowning/file_read_write_server/ServerFile.txt";
    private static final String FULL_FILE_PATH = SystemPropertyUtil.get("user.dir") + File.separator + RELATIVE_FILE_PATH;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        switch (request.method().toString()) {
            case "GET":
                returnFileContent(ctx);
                break;
            case "POST":
                writeFileContent(ctx, request.content());
                returnFileContent(ctx);
                break;
            default:
                sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        ctx.writeAndFlush('\n');
    }

    private static void returnFileContent(ChannelHandlerContext ctx) throws Exception {
        RandomAccessFile raf = getRandomAccessFile(ctx);
        if (raf != null) {
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpUtil.setContentLength(response, raf.length());
            ctx.write(response);
            DefaultFileRegion defaultFileRegion = new DefaultFileRegion(raf.getChannel(), 0, raf.length());
            ctx.writeAndFlush(defaultFileRegion);
        } else {
            sendError(ctx, HttpResponseStatus.NO_CONTENT);
        }
    }

    private static void writeFileContent(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        byte[] bytes = getBytes(byteBuf);
        File file = new File(FULL_FILE_PATH);
        try {
            FileOutputStream fileStream = new FileOutputStream(file);
            fileStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            sendError(ctx, HttpResponseStatus.NO_CONTENT);
        }
    }

    private static byte[] getBytes(ByteBuf buf) {
        byte[] result = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), result);
        return result;
    }

    private static RandomAccessFile getRandomAccessFile(ChannelHandlerContext ctx) throws Exception {
        //RandomAccessFile raf = null;
        //long length = -1;
        try {
            return new RandomAccessFile(FULL_FILE_PATH, "r");
        } catch (Exception e) {
            ctx.writeAndFlush("ERR: " + e.getClass().getSimpleName() + ": " + e.getMessage() + '\n');
            return null;
        } finally {
            //if (length < 0 && raf != null) {
            //    raf.close();
            //}
        }
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                status,
                                                                Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
