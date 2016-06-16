package com.mattchowning.file_read_write.server.handler;

import com.mattchowning.file_read_write.server.ServerUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import javax.activation.MimetypesFileTypeMap;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.SystemPropertyUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServerFileReadWriteHandlerTest {

    private static final String EXPECTED_DATE_STRING = "expected_date_string";

    private File file;
    @Mock private ByteBuf fileContent;
    @Mock private FileManager fileManager;
    @Mock private ServerUtils serverUtils;
    @Mock private ChannelHandlerContext ctx;
    @Mock private FullHttpRequest request;
    @InjectMocks private ServerFileReadWriteHandler subject;


    @Before
    public void before() {
        file = new File(SystemPropertyUtil.get("user.dir") + File.separator + "src/test/java/com/mattchowning/file_read_write/server/handler/testfile");
        when(fileManager.getFile()).thenReturn(file);
        when(serverUtils.getDate()).thenReturn(EXPECTED_DATE_STRING);
        when(request.content()).thenReturn(fileContent);
    }

    @Test
    public void handles_get_request() throws Exception {
        when(request.method()).thenReturn(HttpMethod.GET);
        subject.channelRead0(ctx, request);
        verifyReturnFileContent();
    }

    @Test
    public void handles_successful_post_request() throws Exception {
        when(request.method()).thenReturn(HttpMethod.POST);
        when(fileManager.writeFileContent(fileContent)).thenReturn(true);

        subject.channelRead0(ctx, request);

        verify(fileManager).writeFileContent(fileContent);
        verifyReturnFileContent();
    }

    @Test
    public void handles_failed_post_request() throws Exception {
        when(request.method()).thenReturn(HttpMethod.POST);
        when(fileManager.writeFileContent(fileContent)).thenReturn(false);

        subject.channelRead0(ctx, request);

        verify(fileManager).writeFileContent(fileContent);
        verify(serverUtils).sendError(ctx, HttpResponseStatus.NO_CONTENT);
        verifyZeroInteractions(ctx);
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