package netease.zh.com.neteasemaven.netease.remote;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

public class ContentType {
    private static final Pattern MIME_PATTERN = Pattern.compile("^([^,; \t]+)");
    private static final Pattern CHARSET_PATTERN = Pattern.compile("[,; \t]charset=([^; ]+)");

    private final String mime;
    private final Charset charset;

    public ContentType(String mime, Charset charset) {
        this.mime = mime;
        this.charset = charset;
    }

    public String getMime() {
        return mime;
    }

    public Charset getCharset() {
        return charset;
    }

    public boolean isJSON() {
        return "application/json".equals(mime) ||
                "text/json".equals(mime) ||
                "text/javascript".equals(mime);
    }

    public boolean isText() {
        return mime != null && mime.startsWith("text/");
    }

    public boolean isImage() {
        return mime != null && mime.startsWith("image/");
    }

}
