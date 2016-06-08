package com.mattchowning.file_read_write.client;

import com.google.gson.JsonSyntaxException;
import com.mattchowning.file_read_write.server.model.OAuthModel;

import java.util.Scanner;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringEncoder;

import static com.mattchowning.file_read_write.SharedConstants.GRANT_TYPE_KEY;
import static com.mattchowning.file_read_write.SharedConstants.GRANT_TYPE_PASSWORD;
import static com.mattchowning.file_read_write.SharedConstants.GSON;
import static com.mattchowning.file_read_write.SharedConstants.OAUTH_PATH;
import static com.mattchowning.file_read_write.SharedConstants.PASSWORD_KEY;
import static com.mattchowning.file_read_write.SharedConstants.RESPONSE_CHARSET;
import static com.mattchowning.file_read_write.SharedConstants.USERNAME_KEY;

public class InitialAuthHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    public interface InitialAuthListener {
        void onOAuthReceived(OAuthModel oAuthModel);
    }

    private final InitialAuthListener listener;

    public InitialAuthHandler(InitialAuthListener listener) {
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        requestOAuthToken(ctx);
    }

    private void requestOAuthToken(ChannelHandlerContext ctx) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        QueryStringEncoder queryStringEncoder = new QueryStringEncoder(OAUTH_PATH);
        queryStringEncoder.addParam(GRANT_TYPE_KEY, GRANT_TYPE_PASSWORD);
        queryStringEncoder.addParam(USERNAME_KEY, username);
        queryStringEncoder.addParam(PASSWORD_KEY, password);
        String uriString = queryStringEncoder.toString();
        FullHttpMessage message = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                                             HttpMethod.POST,
                                                             uriString);
        ctx.writeAndFlush(message);
        System.out.println("oauth token requested");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        String responseBody = response.content().toString(RESPONSE_CHARSET);
        switch (response.status().code()) {
            case 200:
                OAuthModel oAuthModel = parseOAuthResponse(responseBody);
                listener.onOAuthReceived(oAuthModel);
                return;
            default:
                System.out.println("OAuth ERROR: " + responseBody);
        }
    }

    private OAuthModel parseOAuthResponse(String responseBody) {
        try {
            return GSON.fromJson(responseBody, OAuthModel.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
