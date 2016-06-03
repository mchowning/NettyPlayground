package com.mattchowning.file_read_write_server;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
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

    private List<String> issuedTokens = new ArrayList();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (isOAuthRequest(request)) {
            processOAuthRequest(ctx, request);
        } else if (isAuthorized(request)) {
            processFileRequest(ctx, request);
        } else {
            sendError(ctx, HttpResponseStatus.UNAUTHORIZED, "invalid_client", "client not authorized");
        }
        ctx.writeAndFlush('\n');
    }

    private void processOAuthRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (checkOAuthRequest(ctx, request)) {
            String token = getNewToken();
            // FIXME make successful response consistent with 5.1
            respondWithPlainTextBody(ctx, token);
        }
    }

    private boolean checkOAuthRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        boolean isApproved = false;
        Map<String, List<String>> params = new QueryStringDecoder(request.uri()).parameters();
        if (request.method() != HttpMethod.POST) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "oauth request must be POST");
        } else if (!hasParam(params, "grant_type", "password")) {
            String errorDescription = "oauth request must specify grant_type of password";
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
        } else if (!hasParamKey(params, "username")) {
            String errorDescription = "oauth request must specify username";
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
        } else if (!hasParamKey(params, "password")) {
            String errorDescription = "oauth request must specify password";
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
        } else if (isShadyUser(params)) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_client", "user is shady");
        } else {
            isApproved = true;
        }
        return isApproved;
    }

    private String getNewToken() {
        String token = TokenGenerator.generateNew();
        issuedTokens.add(token);
        return token;
    }

    private void respondWithPlainTextBody(ChannelHandlerContext ctx, String token) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                HttpResponseStatus.OK,
                                                                Unpooled.copiedBuffer(token, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        setDateHeader(response);
        HttpUtil.setContentLength(response, token.length());
        ctx.write(response);
        ctx.write(token);
    }

    private boolean hasParamKey(Map<String, List<String>> params, String key) {
        return params != null
               && params.get(key) != null;
    }

    private boolean hasParam(Map<String, List<String>> params, String key, String value) {
        return params != null
                && params.get(key) != null &&
               params.get(key).contains(value);
    }

    private boolean isShadyUser(Map<String, List<String>> params) {
        return hasParam(params, "username", "sleepynate");
    }

    private boolean isOAuthRequest(FullHttpRequest request) {
        if (request != null) {
            String path = new QueryStringDecoder(request.uri()).path();
            return "/oauth".equals(path);
        } else {
            return false;
        }
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

    private boolean isAuthorized(FullHttpRequest request) {
        String encodedAuthHeader = request.headers().get(HttpHeaderNames.AUTHORIZATION);
        if (encodedAuthHeader == null) {
            return false;
        } else {
            String userPassPortionOfAuthHeader = encodedAuthHeader.split("\\s")[1];
            byte[] decodedAuthHeader = Base64.getDecoder().decode(userPassPortionOfAuthHeader);
            return Arrays.equals(AUTH_STRING.getBytes(), (decodedAuthHeader));
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
        sendError(ctx, status, status.toString());
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String errorMsg) {
        sendError(ctx, status, errorMsg, null);
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String errorMsg, String errorDescription) {
        Error error = new Error(errorMsg, errorDescription);
        String json = new Gson().toJson(error);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                status,
                                                                Unpooled.copiedBuffer(json, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpUtil.setContentLength(response, json.length());
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
