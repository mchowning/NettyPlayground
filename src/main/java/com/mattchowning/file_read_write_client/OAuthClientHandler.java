package com.mattchowning.file_read_write_client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mattchowning.file_read_write_server.model.OAuthModel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import static com.mattchowning.file_read_write_server.FileReadWriteServerHandler.GRANT_TYPE_KEY;
import static com.mattchowning.file_read_write_server.FileReadWriteServerHandler.GRANT_TYPE_PASSWORD;
import static com.mattchowning.file_read_write_server.FileReadWriteServerHandler.OAUTH_PATH;
import static com.mattchowning.file_read_write_server.FileReadWriteServerHandler.PASSWORD_KEY;
import static com.mattchowning.file_read_write_server.FileReadWriteServerHandler.USERNAME_KEY;

public class OAuthClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    public static final AttributeKey<OAuthModel> OAUTH_STATE = AttributeKey.valueOf("OAuth.state");

    private final String username;
    private final String password;

    public OAuthClientHandler(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
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
        String rawContent = response.content().toString(CharsetUtil.UTF_8);
        JsonElement element = new Gson().fromJson(rawContent, JsonElement.class);
        if (element instanceof JsonObject) {
            JsonObject object = (JsonObject) element;
            if (HttpResponseStatus.OK.equals(response.status())) {
                System.out.println("oauth token received");
                OAuthModel tokenResponse = new Gson().fromJson(rawContent, OAuthModel.class);
                ctx.channel().attr(OAUTH_STATE).set(tokenResponse);
                ctx.pipeline().remove(this);
                ctx.fireChannelActive();
            } else {
                System.out.println("oauth request did not succeed: \n" + object.toString());
            }
        }
        //ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
