package com.mattchowning.file_read_write_server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;
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

public class FileReadWriteHandler extends ChannelInboundHandlerAdapter {

    private final String FILE_PATH = "/Users/matt/dev/NettySample/src/main/java/com/mattchowning/file_read_write_server/ServerFile.txt";

    // FIXME make it so a FullHttpMessage is coming in here (see HttpStaticFileServerHandler)
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            switch (fullHttpRequest.method().toString()) {
                case "GET":
                    returnFileContent(ctx);
                    break;
                case "POST":
                    writeFileContent(fullHttpRequest.content());
                    returnFileContent(ctx);
                    break;
                default:
                    sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            }
            ctx.writeAndFlush('\n');
        }
    }

    private void returnFileContent(ChannelHandlerContext ctx) throws Exception {
        RandomAccessFile raf = getRaf(ctx);
        if (raf != null) {
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpUtil.setContentLength(response, raf.length());
            ctx.write(response);
            DefaultFileRegion defaultFileRegion = new DefaultFileRegion(raf.getChannel(), 0, raf.length());
            ctx.writeAndFlush(defaultFileRegion);
        }
    }

    private void writeFileContent(ByteBuf byteBuf) {
        byte[] bytes = getBytes(byteBuf);
        File file = new File(FILE_PATH);
        try {
            FileOutputStream fileStream = new FileOutputStream(file);
            fileStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] getBytes(ByteBuf buf) {
        byte[] result = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), result);
        return result;
    }

    private RandomAccessFile getRaf(ChannelHandlerContext ctx) throws Exception {
        //RandomAccessFile raf = null;
        //long length = -1;
        try {
            return new RandomAccessFile(FILE_PATH, "r");
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
