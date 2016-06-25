package com.mattchowning.file_read_write.server.handler;

import com.mattchowning.file_read_write.server.ServerUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

import javax.activation.MimetypesFileTypeMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;
import io.netty.util.internal.SystemPropertyUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServerFileReadWriteHandlerTest {

    private static final String EXPECTED_DATE_STRING = "expected_date_string";

    private File file;
    @Mock private ByteBuf fileContent;
    @Mock private FileManager fileManager;
    @Mock private ServerUtil serverUtil;
    @Mock private ChannelHandlerContext ctx;
    @Mock private FullHttpRequest request;
    @Mock private Channel channel;
    @Mock private Attribute<Boolean> authorizationAttribute;
    @InjectMocks private ServerFileReadWriteHandler subject;


    @Before
    public void before() {
        file = new File(SystemPropertyUtil.get("user.dir") + File.separator + "src/test/java/com/mattchowning/file_read_write/server/handler/testfile");
        when(fileManager.getFile()).thenReturn(file);
        when(serverUtil.getDate()).thenReturn(EXPECTED_DATE_STRING);
        when(request.content()).thenReturn(fileContent);
        when(ctx.channel()).thenReturn(channel);
        when(channel.hasAttr(ServerOAuthVerificationHandler.AUTHORIZED)).thenReturn(true);
        when(channel.attr(ServerOAuthVerificationHandler.AUTHORIZED)).thenReturn(authorizationAttribute);
    }

    @Test
    public void handles_get_request_with_authorization() throws Exception {
        setAuthorizationState(true);
        when(request.method()).thenReturn(HttpMethod.GET);
        subject.channelRead0(ctx, request);
        verifyReturnFileContent();
    }

    @Test
    public void handles_get_request_without_authorization() throws Exception {
        setAuthorizationState(false);
        when(request.method()).thenReturn(HttpMethod.GET);
        subject.channelRead0(ctx, request);
        verifyReturnEncryptedFileContent();
    }

    @Test
    public void handles_post_request_with_authorization() throws Exception {
        setAuthorizationState(true);
        when(request.method()).thenReturn(HttpMethod.POST);
        when(fileManager.writeFileContent(fileContent)).thenReturn(true);

        subject.channelRead0(ctx, request);

        verify(fileManager).writeFileContent(fileContent);
        verifyReturnFileContent();
    }

    @Test
    public void handles_post_request_without_authorization() throws Exception {
        setAuthorizationState(false);
        when(request.method()).thenReturn(HttpMethod.POST);
        when(fileManager.writeFileContent(fileContent)).thenReturn(true);

        subject.channelRead0(ctx, request);

        verify(serverUtil).sendError(eq(ctx), eq(HttpResponseStatus.UNAUTHORIZED), anyString());
        verify(fileManager, never()).writeFileContent(any(ByteBuf.class));
    }

    @Test
    public void handles_failed_post_request() throws Exception {
        setAuthorizationState(true);
        when(request.method()).thenReturn(HttpMethod.POST);
        when(fileManager.writeFileContent(fileContent)).thenReturn(false);

        subject.channelRead0(ctx, request);

        verify(fileManager).writeFileContent(fileContent);
        verify(serverUtil).sendError(ctx, HttpResponseStatus.CONFLICT);
    }

    /*
     * helper methods
     */

    private void setAuthorizationState(boolean attrValue) {
        when(authorizationAttribute.get()).thenReturn(attrValue);
    }

    private void verifyReturnEncryptedFileContent() throws Exception {
        ArgumentCaptor<FullHttpResponse> responseCaptor = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(ctx).writeAndFlush(responseCaptor.capture());
        FullHttpResponse response = responseCaptor.getValue();

        assertThat(response.status()).isEqualTo(HttpResponseStatus.OK);
        assertThat(response.protocolVersion()).isEqualTo(HttpVersion.HTTP_1_1);

        byte[] encodedFileBody = Base64.getEncoder().encode(Files.readAllBytes(file.toPath()));
        assertThat(response.content()).isEqualTo(Unpooled.copiedBuffer(encodedFileBody));

        HttpHeaders headers = response.headers();
        assertThat(headers.get(HttpHeaderNames.CONTENT_LENGTH)).isEqualTo(Long.toString(encodedFileBody.length));
        assertThat(headers.get(HttpHeaderNames.CONTENT_TYPE)).isEqualTo("text/plain");
        assertThat(headers.get(HttpHeaderNames.DATE)).isEqualTo(EXPECTED_DATE_STRING);

    }

    private void verifyReturnFileContent() {
        ArgumentCaptor<HttpResponse> responseCaptor = ArgumentCaptor.forClass(HttpResponse.class);
        verify(ctx).write(responseCaptor.capture());
        HttpResponse response = responseCaptor.getValue();
        assertThat(response.status()).isEqualTo(HttpResponseStatus.OK);
        assertThat(response.protocolVersion()).isEqualTo(HttpVersion.HTTP_1_1);
        HttpHeaders headers = response.headers();
        assertThat(headers.get(HttpHeaderNames.CONTENT_LENGTH)).isEqualTo(Long.toString(file.length()));
        assertThat(headers.get(HttpHeaderNames.CONTENT_TYPE)).isEqualTo(new MimetypesFileTypeMap().getContentType(file));
        assertThat(headers.get(HttpHeaderNames.DATE)).isEqualTo(EXPECTED_DATE_STRING);

        ArgumentCaptor<DefaultFileRegion> fileContentCaptor = ArgumentCaptor.forClass(DefaultFileRegion.class);
        verify(ctx).writeAndFlush(fileContentCaptor.capture());
        DefaultFileRegion defaultFileRegion = fileContentCaptor.getValue();
        assertThat(defaultFileRegion.count()).isEqualTo(file.length());
        assertThat(defaultFileRegion.position()).isEqualTo(0L);
    }
}