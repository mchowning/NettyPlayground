package com.mattchowning.file_read_write.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.internal.SystemPropertyUtil;

import static com.mattchowning.file_read_write.server.ServerUtils.getDate;
import static com.mattchowning.file_read_write.server.ServerUtils.sendError;

public class FileReadWriteServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String RELATIVE_FILE_PATH = "src/main/java/com/mattchowning/file_read_write/server/SecretServerFile.txt";
    private static final String FULL_FILE_PATH = SystemPropertyUtil.get("user.dir") + File.separator + RELATIVE_FILE_PATH;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        processFileRequest(ctx, request);
    }

    private void processFileRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
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
    }

    private static void returnFileContent(ChannelHandlerContext ctx) {
        try {
            File file = new File(FULL_FILE_PATH);
            HttpResponse response = getHttpResponse(file);
            DefaultFileRegion fileContent = new DefaultFileRegion(file, 0, file.length());
            ctx.write(response);
            ctx.writeAndFlush(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
            sendError(ctx, HttpResponseStatus.NO_CONTENT);
        }
    }

    private static HttpResponse getHttpResponse(File file) throws IOException {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, getContentType(file));
        response.headers().set(HttpHeaderNames.DATE, getDate());
        return response;
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

    private static String getContentType(File file) {
        return new MimetypesFileTypeMap().getContentType(file.getPath());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
