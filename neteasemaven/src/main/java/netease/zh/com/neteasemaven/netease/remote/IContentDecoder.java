package netease.zh.com.neteasemaven.netease.remote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import netease.zh.com.neteasemaven.netease.json.JSONUtil;

public interface IContentDecoder<R> {

    R decode(ContentType contentType, byte[] data) throws Exception;

    public static abstract class BaseDecoder<R> implements IContentDecoder<R> {
        // 兼容android level 8
        protected String toString(byte[] data, Charset charset) {
            try {
                return new String(data, charset.name());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        // 兼容android level 8
        protected byte[] toBytes(String s, Charset charset) {
            try {
                return s.getBytes(charset.name());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class JSONDecoder extends BaseDecoder<Object> {

        @Override
        public Object decode(ContentType contentType, byte[] data) throws JSONException {
            String json = toString(data, contentType.getCharset());
            JSONTokener tokenizer = new JSONTokener(json);
            Object result = tokenizer.nextValue();
            if (result instanceof JSONObject || result instanceof JSONArray) {
                return result;
            }
            throw new JSONException("数据错误");
        }
    }

    public static class BitmapDecoder extends BaseDecoder<Bitmap> {
        @Override
        public Bitmap decode(ContentType contentType, byte[] data) throws Exception {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
    }

    public static class TextDecoder extends BaseDecoder<String> {
        @Override
        public String decode(ContentType contentType, byte[] data) throws Exception {
            return toString(data, contentType.getCharset());
        }
    }

    public static class BinaryDecoder extends BaseDecoder<byte[]> {
        @Override
        public byte[] decode(ContentType contentType, byte[] data) throws Exception {
            return data;
        }
    }

    public static class BeanDecoder<T> extends BaseDecoder<T> {
        private final Class<T> klass;
        private final String key;

        public BeanDecoder(Class<T> klass, String key) {
            this.klass = klass;
            this.key = key;
        }

        public BeanDecoder(Class<T> klass) {
            this(klass, null);
        }

        public T decode(ContentType contentType, byte[] data) throws JSONException {
            String json = toString(data, contentType.getCharset());
            JSONObject jsonObject = new JSONObject(json);

            if (key != null) {
                jsonObject = jsonObject.getJSONObject(key);
            }

            return JSONUtil.load(klass, jsonObject);
        }
    }

}
