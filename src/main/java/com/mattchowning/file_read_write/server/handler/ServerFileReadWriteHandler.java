package com.mattchowning.file_read_write.server.handler;

import com.mattchowning.file_read_write.server.ServerUtil;

import java.io.File;

import javax.activation.MimetypesFileTypeMap;

import io.netty.buffer.ByteBuf;
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
                returnFileContent(ctx);
                break;
            case "POST":
                writeFileContent(ctx, request.content());
                break;
            default:
                serverUtil.sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
    }

    private void returnFileContent(ChannelHandlerContext ctx) {
        File file = fileManager.getFile();
        HttpResponse response = getHttpResponse(file);
        DefaultFileRegion fileContent = new DefaultFileRegion(file, 0, file.length());
        ctx.write(response);
        ctx.writeAndFlush(fileContent);
    }

    private HttpResponse getHttpResponse(File file) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, getContentType(file));
        response.headers().set(HttpHeaderNames.DATE, serverUtil.getDate());
        return response;
    }

    private void writeFileContent(ChannelHandlerContext ctx, ByteBuf content) {
        boolean succeeded = fileManager.writeFileContent(content);
        if (succeeded) {
            returnFileContent(ctx);
        } else {
            serverUtil.sendError(ctx, HttpResponseStatus.NO_CONTENT);
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
