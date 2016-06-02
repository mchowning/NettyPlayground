package com.mattchowning.file_read_write_server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.activation.MimetypesFileTypeMap;

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

    private static final String RELATIVE_FILE_PATH = "src/main/java/com/mattchowning/file_read_write_server/SecretServerFile.txt";
    private static final String FULL_FILE_PATH = SystemPropertyUtil.get("user.dir") + File.separator + RELATIVE_FILE_PATH;
    private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "secret";
    private static final String AUTH_STRING = String.format("%s:%s", USERNAME, PASSWORD);


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (isAuthorized(request)) {
            processRequest(ctx, request);
        } else {
            sendError(ctx, HttpResponseStatus.UNAUTHORIZED);
        }
        ctx.writeAndFlush('\n');
    }

    private void processRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
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

    private boolean isAuthorized(FullHttpRequest request) {
        String encodedAuthHeader = request.headers().get(HttpHeaderNames.AUTHORIZATION);
        String userPassPortionOfAuthHeader = encodedAuthHeader.split("\\s")[1];
        byte[] decodedAuthHeader = Base64.getDecoder().decode(userPassPortionOfAuthHeader);
        return Arrays.equals(AUTH_STRING.getBytes(), (decodedAuthHeader));
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
        HttpUtil.setContentLength(response, file.length());
        setContentTypeHeader(response, file);
        setDateHeader(response);
        return response;
    }

    private static void setDateHeader(HttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));
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

    private static void setContentTypeHeader(HttpResponse response, File file) {
        String contentType = new MimetypesFileTypeMap().getContentType(file.getPath());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        String contentText = "Failure: " + status + "\r\n";
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                status,
                                                                Unpooled.copiedBuffer(contentText, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpUtil.setContentLength(response, contentText.length());
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
