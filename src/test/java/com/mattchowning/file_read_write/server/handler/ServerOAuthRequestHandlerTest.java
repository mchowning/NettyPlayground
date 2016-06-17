package com.mattchowning.file_read_write.server.handler;

import com.mattchowning.file_read_write.server.ServerUtil;
import com.mattchowning.file_read_write.server.model.OAuthToken;
import com.mattchowning.file_read_write.server.model.OAuthTokenMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;

import static com.mattchowning.file_read_write.SharedConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServerOAuthRequestHandlerTest {

    private static final String VALID_USERNAME = "valid_username";
    private static final String VALID_PASSWORD = "valid_password";
    private static final String EXPECTED_REFRESH_TOKEN = "expected_refresh_token";
    private static final String EXPECTED_DATE = "expected_date";

    @Mock private OAuthTokenMap tokenMap;
    @Mock private ServerUtil serverUtil;
    @Mock private OAuthToken preexistingToken;
    @Mock private ChannelHandlerContext ctx;
    private FullHttpRequest request;
    @InjectMocks private ServerOAuthRequestHandler subject;

    @Before public void before() {
        when(serverUtil.getDate()).thenReturn(EXPECTED_DATE);
    }

    @Test public void error_if_not_oauth_path() throws Exception {
        setupPasswordRequest(VALID_USERNAME, VALID_PASSWORD);
        request.setUri("/notoauth");
        subject.channelRead0(ctx, request);
        verifyInvalidEndpointError();
    }

    @Test public void error_if_not_post() throws Exception {
        setupPasswordRequest(VALID_USERNAME, VALID_PASSWORD);
        request.setMethod(HttpMethod.GET);
        subject.channelRead0(ctx, request);
        verifyResponseError();
    }

    @Test public void error_if_invalid_grant_type() throws Exception {
        setupBasicRequestWithGrantType("unexpted_grant_type");
        subject.channelRead0(ctx, request);
        verifyResponseError();
    }

    /*
     * password request
     */

    @Test public void processes_valid_password_request() throws Exception {
        setupPasswordRequest(VALID_USERNAME, VALID_PASSWORD);
        subject.channelRead0(ctx, request);
        verifyNewTokenSentBackAsResponse();
    }

    @Test public void error_if_no_username() throws Exception {
        setupPasswordRequest("", VALID_PASSWORD);
        subject.channelRead0(ctx, request);
        verifyResponseError();
    }

    @Test public void error_if_no_password() throws Exception {
        setupPasswordRequest(VALID_USERNAME, "");
        subject.channelRead0(ctx, request);
        verifyResponseError();
    }

    @Test public void error_if_user_shady() throws Exception {
        setupPasswordRequest("sleepynate", VALID_PASSWORD);
        subject.channelRead0(ctx, request);
        verifyResponseError();
    }

    /*
     * refresh request
     */

    @Test public void processes_valid_refresh_request() throws Exception {
        setupRefreshRequest(EXPECTED_REFRESH_TOKEN);
        subject.channelRead0(ctx, request);
        verify(tokenMap).removeToken(preexistingToken);
        verifyNewTokenSentBackAsResponse();
    }

    @Test public void error_if_invalid_refresh_token() throws Exception {
        setupRefreshRequest("some_invalid_refresh_token");
        subject.channelRead0(ctx, request);
        verify(tokenMap, never()).removeToken(preexistingToken);
        verifyResponseError();
    }

    /*
     * helper methods
     */

    private void verifyNewTokenSentBackAsResponse() {
        verify(tokenMap, never()).add(preexistingToken);

        ArgumentCaptor<OAuthToken> tokenArgumentCaptor = ArgumentCaptor.forClass(OAuthToken.class);
        verify(tokenMap).add(tokenArgumentCaptor.capture());
        OAuthToken token = tokenArgumentCaptor.getValue();

        ArgumentCaptor<FullHttpResponse> responseArgumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(ctx).writeAndFlush(responseArgumentCaptor.capture());
        FullHttpResponse response = responseArgumentCaptor.getValue();

        String body = GSON.toJson(token);

        assertThat(response.protocolVersion())
                .isEqualTo(HttpVersion.HTTP_1_1);
        assertThat(response.status())
                .isEqualTo(HttpResponseStatus.OK);
        assertThat(response.content())
                .isEqualTo(Unpooled.copiedBuffer(body, RESPONSE_CHARSET));
        assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE))
                .isEqualTo("application/json; charset=UTF-8");
        assertThat(response.headers().get(HttpHeaderNames.CONTENT_LENGTH))
                .isEqualTo(String.valueOf(body.length()));
    }

    private void setupRefreshRequest(String refreshTokenInRequest) throws HttpPostRequestEncoder.ErrorDataEncoderException {

        when(preexistingToken.getRefreshToken()).thenReturn(EXPECTED_REFRESH_TOKEN);
        when(tokenMap.containsRefreshToken(EXPECTED_REFRESH_TOKEN)).thenReturn(true);
        when(tokenMap.getWithRefreshToken(EXPECTED_REFRESH_TOKEN)).thenReturn(preexistingToken);

        request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                             HttpMethod.POST,
                                             OAUTH_PATH);
        HttpPostRequestEncoder postRequestEncoder = new HttpPostRequestEncoder(request, false);
        postRequestEncoder.addBodyAttribute(GRANT_TYPE_KEY, GRANT_TYPE_REFRESH_TOKEN);
        postRequestEncoder.addBodyAttribute(REFRESH_TOKEN_KEY, refreshTokenInRequest);
        postRequestEncoder.finalizeRequest();
    }

    private void setupPasswordRequest(String username, String password) throws Exception {
        request =  new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                              HttpMethod.POST,
                                              OAUTH_PATH);
        HttpPostRequestEncoder postRequestEncoder = new HttpPostRequestEncoder(request, false);
        postRequestEncoder.addBodyAttribute(GRANT_TYPE_KEY, GRANT_TYPE_PASSWORD);
        postRequestEncoder.addBodyAttribute(USERNAME_KEY, username);
        postRequestEncoder.addBodyAttribute(PASSWORD_KEY, password);
        postRequestEncoder.finalizeRequest();
    }

    private void setupBasicRequestWithGrantType(String grantType) throws Exception {
        request =  new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                              HttpMethod.POST,
                                              OAUTH_PATH);
        HttpPostRequestEncoder postRequestEncoder = new HttpPostRequestEncoder(request, false);
        postRequestEncoder.addBodyAttribute(GRANT_TYPE_KEY, grantType);
        postRequestEncoder.finalizeRequest();
    }

    private void verifyInvalidEndpointError() {
        ArgumentCaptor<FullHttpResponse> argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(ctx).writeAndFlush(argumentCaptor.capture());
        FullHttpResponse response = argumentCaptor.getValue();
        assertThat(response.protocolVersion()).isEqualTo(HttpVersion.HTTP_1_1);
        assertThat(response.status()).isEqualTo(HttpResponseStatus.NOT_FOUND);
    }

    private void verifyResponseError() {
        verify(serverUtil).sendError(eq(ctx),
                                     eq(HttpResponseStatus.BAD_REQUEST),
                                     anyString(),
                                     anyString());
    }
}