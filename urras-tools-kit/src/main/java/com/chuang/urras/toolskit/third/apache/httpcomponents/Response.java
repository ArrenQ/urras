package com.chuang.urras.toolskit.third.apache.httpcomponents;

import com.chuang.urras.toolskit.basic.RegexKit;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

//import org.apache.http.annotation.ThreadSafe;

/**
 * Created by ath on 2017/1/7.
 */
//@ThreadSafe
public class Response {
    private final Logger logger = LoggerFactory.getLogger(Response.class);
    private final StatusLine statusLine;
    private final HttpResponse response;
    private final String charset;
    private final HttpRequestBase request;

    private static final int READ_LEN = 1024;

    public Response(HttpRequestBase request, HttpResponse response, String charset) {
        statusLine = response.getStatusLine();
        this.response = response;
        this.charset = charset;
        this.request = request;

    }

    public int getStatusCode() {
        return statusLine.getStatusCode();
    }

    public StatusLine getStatusLine() {
        return statusLine;
    }

    public InputStream asStream() throws IOException {
        return response.getEntity().getContent();
    }

    public HttpRequestBase getRequest() {
        return request;
    }

    public HttpEntity asEntity() {
        return response.getEntity();
    }

    public HeaderIterator getHeaderIterator() {
        return response.headerIterator();
    }

    public String getHeader(String key) {
        return response.getFirstHeader(key).getValue();
    }

    public Header[] getHeaders(String key) {
        return response.getHeaders(key);
    }

    public String asString(@Nullable String charset) {

        if(null == charset) {
            charset = getResponseHeaderCharset().orElse("utf-8");
        }

        String result = null;
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // gzip 处理
                Header encode = response.getFirstHeader(HttpHeaders.CONTENT_ENCODING);
                if(null != encode && StringUtils.isNotBlank(encode.getValue()) && encode.getValue().toLowerCase().contains("gzip")) {
                    entity = new GzipDecompressingEntity(entity);
                }

                //result = EntityUtils.toString(entity, charset);
                // Apache 自带的EntityUtils 读取entity实体时默认会加上转换成String的处理流，当服务端发送过来的字符没有默认的字符串结束标记，就会出现EOF异常。
                // 这里借鉴netty的处理方式。直接读取节点流，然后再转换成字符串。但这种处理方式，会占用大量的缓冲区。
                result = HttpTools.entity2strAndClose(entity, READ_LEN, charset);
            }
            EntityUtils.consume(entity);
            // 如果状态不为 200 <= status < 300
            if (getStatusCode() < HttpStatus.SC_OK || getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
                if(null != request) {
                    request.abort();
//                    throw new CallHttpException("HttpClient,error status colNameCode :" + getStatusCode() + ", response:->" + result, getStatusCode(), request.getMethod());
                }
//                else {
//                    throw new CallHttpException("HttpClient,error status colNameCode :" + getStatusCode() + ", response:->" + result, getStatusCode(), "未知");
//                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            this.close();
        }
        logger.debug("response -> " + result);
        return result;
    }

    public Optional<String> getResponseHeaderCharset() {
        String contentType = response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
        if(null == contentType) {
            return Optional.empty();
        }
        String code = RegexKit.get("charset=(\\S+);?", contentType, 1);
        if(null == code) {
            return Optional.empty();
        }
        code = code.replaceAll(";", "").trim();
        return Optional.of(code);

    }

    public void close() {
        if(response instanceof CloseableHttpResponse) {
            HttpTools.closeQuietly((CloseableHttpResponse)response);
        }
    }

    public String asString() {
        return asString(this.charset);
    }

    public String asStringByReponseCharset() {
        return asString(null);
    }
}
