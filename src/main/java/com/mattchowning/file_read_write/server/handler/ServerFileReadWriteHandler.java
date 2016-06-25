package com.mattchowning.file_read_write.server.handler;

import com.mattchowning.file_read_write.server.ServerUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import javax.activation.MimetypesFileTypeMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

public class ServerFileReadWriteHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final FileManager fileManager;
    private final ServerUtil serverUtil;

    public ServerFileReadWriteHandler(String fullFilePath) {
        this(new FileManager(fullFilePath), new ServerUtil());
    }

    protected ServerFileReadWriteHandler(FileManager fileManager, ServerUtil serverUtil) {
        super();
        this.fileManager = fileManager;
        this.serverUtil = serverUtil;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        switch (request.method().toString()) {
            case "GET":
                if (isClientAuthorized(ctx)) {
                    returnFileContent(ctx);
                } else {
                    returnEncodedFileContent(ctx);
                }
                break;
            case "POST":
                if (isClientAuthorized(ctx)) {
                    writeFileContent(ctx, request.content());
                } else {
                    serverUtil.sendError(ctx,
                                         HttpResponseStatus.UNAUTHORIZED,
                                         "invalid_request",
                                         "Authentication required to update file.");
                }
                break;
            default:
                serverUtil.sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
    }

    private boolean isClientAuthorized(ChannelHandlerContext ctx) {
        return ctx.channel().hasAttr(ServerOAuthVerificationHandler.AUTHORIZED) &&
               ctx.channel().attr(ServerOAuthVerificationHandler.AUTHORIZED).get();
    }

    private void returnEncodedFileContent(ChannelHandlerContext ctx) {
        try {
            File file = fileManager.getFile();
            byte[] bytes = getBase64EncodedFileBytes(file);
            HttpResponse response = getHttpResponse(bytes);
            ctx.writeAndFlush(response);
        } catch (IOException e) {
            serverUtil.sendError(ctx, HttpResponseStatus.CONFLICT);
            e.printStackTrace();
        }
    }

    private byte[] getBase64EncodedFileBytes(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encode(bytes);
    }

    private void returnFileContent(ChannelHandlerContext ctx) {
        File file = fileManager.getFile();
        HttpResponse response = getHttpResponse(file);
        DefaultFileRegion fileContent = new DefaultFileRegion(file, 0, file.length());
        ctx.write(response);
        ctx.writeAndFlush(fileContent);
    }

    @NotNull
    private HttpResponse getHttpResponse(File file) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, getContentType(file));
        response.headers().set(HttpHeaderNames.DATE, serverUtil.getDate());
        return response;
    }

    @NotNull
    private HttpResponse getHttpResponse(byte[] bytes) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                HttpResponseStatus.OK,
                                                                Unpooled.copiedBuffer(bytes));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.DATE, serverUtil.getDate());
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        return response;
    }

    private void writeFileContent(ChannelHandlerContext ctx, ByteBuf content) {
        boolean succeeded = fileManager.writeFileContent(content);
        if (succeeded) {
            returnFileContent(ctx);
        } else {
            serverUtil.sendError(ctx, HttpResponseStatus.CONFLICT);
        }
    }

    private String getContentType(File file) {
        return new MimetypesFileTypeMap().getContentType(file.getPath());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
