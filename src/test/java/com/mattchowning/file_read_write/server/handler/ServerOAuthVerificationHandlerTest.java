package com.mattchowning.file_read_write.server.handler;

import com.mattchowning.file_read_write.server.ServerUtil;
import com.mattchowning.file_read_write.server.model.OAuthToken;
import com.mattchowning.file_read_write.server.model.OAuthTokenMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServerOAuthVerificationHandlerTest {

    private static String ENCODED_AUTH_HEADER = "encoded_auth_header";
    private static String ACCESS_TOKEN = "access_token";

    @Mock private OAuthTokenMap oAuthTokenMap;
    @Mock private ServerUtil serverUtil;
    @Mock private ChannelHandlerContext ctx;
    @Mock private FullHttpRequest request;
    @Mock private HttpHeaders httpHeaders;
    @Mock private OAuthToken oAuthToken;

    @InjectMocks private ServerOAuthVerificationHandler subject;

    @Before public void before() {
        setupAuthorizedRequest();
    }

    private void setupAuthorizedRequest() {
        when(serverUtil.getOAuthToken(ENCODED_AUTH_HEADER)).thenReturn(oAuthToken);

        when(request.headers()).thenReturn(httpHeaders);
        when(httpHeaders.contains(HttpHeaderNames.AUTHORIZATION)).thenReturn(true);
        when(httpHeaders.get(HttpHeaderNames.AUTHORIZATION)).thenReturn(ENCODED_AUTH_HEADER);

        when(oAuthToken.hasValidTokenType()).thenReturn(true);
        when(oAuthToken.getAccessToken()).thenReturn(ACCESS_TOKEN);
        when(oAuthToken.isExpired()).thenReturn(false);

        when(oAuthTokenMap.containsAccessToken(oAuthToken)).thenReturn(true);
        when(oAuthTokenMap.getWithAccessToken(ACCESS_TOKEN)).thenReturn(oAuthToken);
    }

    @Test public void forwards_authorized_request() throws Exception {
        subject.channelRead0(ctx, request);

        InOrder inOrder = inOrder(ctx, request);
        inOrder.verify(request).retain();
        inOrder.verify(ctx).fireChannelRead(request);
    }

    @Test public void sends_error_if_no_auth_header() throws Exception {
        when(httpHeaders.contains(HttpHeaderNames.AUTHORIZATION)).thenReturn(false);
        when(httpHeaders.get(HttpHeaderNames.AUTHORIZATION)).thenReturn(null);

        subject.channelRead0(ctx, request);

        verifyBadRequestErrorSent();
    }

    @Test public void sends_error_if_invalid_token_type() throws Exception {
        when(oAuthToken.hasValidTokenType()).thenReturn(false);
        subject.channelRead0(ctx, request);
        verifyBadRequestErrorSent();
    }

    @Test public void sends_error_if_invalid_token() throws Exception {
        when(oAuthTokenMap.containsAccessToken(oAuthToken)).thenReturn(false);
        subject.channelRead0(ctx, request);
        verifyBadRequestErrorSent();
    }

    @Test public void sends_error_if_token_expired() throws Exception {
        when(oAuthToken.isExpired()).thenReturn(true);
        subject.channelRead0(ctx, request);
        verifyBadRequestErrorSent();
    }

    private void verifyBadRequestErrorSent() {
        verify(serverUtil).sendError(eq(ctx),
                                     eq(HttpResponseStatus.BAD_REQUEST),
                                     anyString(),
                                     anyString());
    }
}