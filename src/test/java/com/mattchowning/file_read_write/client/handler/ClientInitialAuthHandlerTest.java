package com.mattchowning.file_read_write.client.handler;

import com.mattchowning.file_read_write.SharedConstants;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClientInitialAuthHandlerTest {

    @Mock private ChannelHandlerContext ctx;
    @Mock private FullHttpResponse response;

    private ClientInitialAuthHandler subject;

    @Before
    public void before() {
        subject = new ClientInitialAuthHandler();
        when(response.status()).thenReturn(HttpResponseStatus.OK);
    }

    @Test
    public void parses_oAuth_token() throws Exception {
        OAuthToken expected = new OAuthToken();
        setResponseBody(expected);

        subject.channelRead0(ctx, response);

        assertThat(subject.getOAuthToken())
                .isEqualToComparingFieldByField(expected);
    }

    private void setResponseBody(OAuthToken token) {
        String tokenJson = SharedConstants.GSON.toJson(token);
        ByteBuf byteBuf = mock(ByteBuf.class);
        when(response.content()).thenReturn(byteBuf);
        when(byteBuf.toString(CharsetUtil.UTF_8)).thenReturn(tokenJson);
    }
}