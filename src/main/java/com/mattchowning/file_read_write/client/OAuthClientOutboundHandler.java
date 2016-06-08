package com.mattchowning.file_read_write.client;

import com.mattchowning.file_read_write.server.model.OAuthModel;

import java.util.Scanner;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringEncoder;

import static com.mattchowning.file_read_write.SharedConstants.GRANT_TYPE_KEY;
import static com.mattchowning.file_read_write.SharedConstants.GRANT_TYPE_PASSWORD;
import static com.mattchowning.file_read_write.SharedConstants.OAUTH_PATH;
import static com.mattchowning.file_read_write.SharedConstants.PASSWORD_KEY;
import static com.mattchowning.file_read_write.SharedConstants.USERNAME_KEY;

public class OAuthClientOutboundHandler extends ChannelOutboundHandlerAdapter implements InitialAuthHandler.Listener {

    private OAuthModel oAuthModel;
    private FullHttpRequest pendingRequest;

    @Override
    public void onOAuthReceived(ChannelHandlerContext ctx, OAuthModel oAuthModel) {
        this.oAuthModel = oAuthModel;
        if (pendingRequest != null) {
            makeAuthorizedRequest(ctx, pendingRequest);
            pendingRequest = null;
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            if (oAuthModel == null) {
                pendingRequest = request;
                requestOAuthToken(ctx);
            } else {
                makeAuthorizedRequest(ctx, request);
            }
        }
    }

    private void makeAuthorizedRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        addAuthorizationHeader(request);
        System.out.println("Issuing previous request...");
        ctx.writeAndFlush(request);
    }

    private void addAuthorizationHeader(FullHttpRequest request) {
        if (oAuthModel == null) {
            throw new RuntimeException("Cannot set authorization header with null OAuthModel");
        } else {
            System.out.println("Adding authorization header to request...");
            String encodedAuthorizationHeader = oAuthModel.getEncodedAuthorizationHeader();
            request.headers().add(HttpHeaderNames.AUTHORIZATION, encodedAuthorizationHeader);
        }

    }

    private void requestOAuthToken(ChannelHandlerContext ctx) {
        System.out.println("Client lacks authorization. Obtaining authorization...");
        String username = getUserInput("username");
        String password = getUserInput("password");
        FullHttpMessage message = getOAuthTokenRequest(username, password);
        System.out.println("Requesting OAuth token...");
        ctx.writeAndFlush(message);

        //ChannelFuture f = ctx.writeAndFlush(message);
        //f.addListener(new ChannelFutureListener() {
        //    @Override
        //    public void operationComplete(ChannelFuture future) throws Exception {
        //        // do something once operation completed
        //    }
        //});
    }

    private FullHttpMessage getOAuthTokenRequest(String username, String password) {
        QueryStringEncoder queryStringEncoder = new QueryStringEncoder(OAUTH_PATH);
        queryStringEncoder.addParam(GRANT_TYPE_KEY, GRANT_TYPE_PASSWORD);
        queryStringEncoder.addParam(USERNAME_KEY, username);
        queryStringEncoder.addParam(PASSWORD_KEY, password);
        String uriString = queryStringEncoder.toString();
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                          HttpMethod.POST,
                                          uriString);
    }

    private String getUserInput(String inputDescription) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(String.format("Enter your %s: ", inputDescription));
        return scanner.nextLine();
    }
}
