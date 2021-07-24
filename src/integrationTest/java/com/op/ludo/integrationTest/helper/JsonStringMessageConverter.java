package com.op.ludo.integrationTest.helper;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;

public class JsonStringMessageConverter extends AbstractMessageConverter {

    private final Charset defaultCharset;

    public JsonStringMessageConverter() {
        this(StandardCharsets.UTF_8);
    }

    public JsonStringMessageConverter(Charset defaultCharset) {
        super(new MimeType("text", "plain", defaultCharset), new MimeType("application", "json"));
        Assert.notNull(defaultCharset, "Default Charset must not be null");
        this.defaultCharset = defaultCharset;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return (String.class == clazz);
    }

    @Override
    protected Object convertFromInternal(
            Message<?> message, Class<?> targetClass, @Nullable Object conversionHint) {
        Charset charset = getContentTypeCharset(getMimeType(message.getHeaders()));
        Object payload = message.getPayload();
        return (payload instanceof String ? payload : new String((byte[]) payload, charset));
    }

    @Override
    @Nullable
    protected Object convertToInternal(
            Object payload, @Nullable MessageHeaders headers, @Nullable Object conversionHint) {

        if (byte[].class == getSerializedPayloadClass()) {
            Charset charset = getContentTypeCharset(getMimeType(headers));
            payload = ((String) payload).getBytes(charset);
        }
        return payload;
    }

    private Charset getContentTypeCharset(@Nullable MimeType mimeType) {
        if (mimeType != null && mimeType.getCharset() != null) {
            return mimeType.getCharset();
        } else {
            return this.defaultCharset;
        }
    }
}
