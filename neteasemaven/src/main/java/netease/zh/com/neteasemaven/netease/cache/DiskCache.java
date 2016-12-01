package netease.zh.com.neteasemaven.netease.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;



import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import netease.zh.com.neteasemaven.netease.async.WorkQueue;
import netease.zh.com.neteasemaven.netease.util.Logger;
import netease.zh.com.neteasemaven.netease.util.MD5Util;

public class DiskCache<K, V> implements ICache<K, V> {
    private static volatile File baseDir;

    private final String type;
    private final ICacheKeyGenerator cacheKeyGenerator;
    private final ISerialization<K, V> serialization;

    public DiskCache(String type, ISerialization<K, V> serialization) {
        this(type, new MD5CacheKeyGenerator(), serialization);
    }

    public DiskCache(String type, ICacheKeyGenerator cacheKeyGenerator, ISerialization<K, V> serialization) {
        this.type = type;
        this.cacheKeyGenerator = cacheKeyGenerator;
        this.serialization = serialization;
    }

    public void clear() {
        final File cacheDir = getCacheDir();

        WorkQueue.run(new Runnable() {
            @Override
            public void run() {
                rm(cacheDir);
            }

            private void rm(File file) {
                if (file.isFile()) {
                    file.delete();
                } else if (file.isDirectory()) {
                    for (File child : file.listFiles()) {
                        rm(file);
                    }
                    file.delete();
                }
            }
        });
    }

    @Override
    public void set(K k, V v) {
        if (k == null) {
            return;
        }

        File file = fileForKey(k);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                Logger.w("DiskCache", "create directory(" + parent + ") failed");
                return;
            }
        }

        serialization.write(file, k, v);
    }

    private File getCacheDir() {
        if (baseDir == null) {
            throw new IllegalStateException();
        }
        File dir = new File(baseDir, type);
        return dir;
    }

    public File fileForKey(K k) {
        File dir = getCacheDir();

        String key = cacheKeyGenerator.createKey(k);
        File subDir = new File(dir, key.substring(0, 2));
        return new File(subDir, key);
    }

    @Override
    public V get(K k) {
        if (k == null) {
            return null;
        }

        File file = fileForKey(k);
        if (!file.exists()) {
            return null;
        }

        return serialization.read(file, k);
    }

    public static void setBaseDir(File dir) {
        baseDir = dir;
    }

    public static interface ICacheKeyGenerator<K> {
        String createKey(K k);
    }

    public static class MD5CacheKeyGenerator<K> implements ICacheKeyGenerator<K> {

        @Override
        public String createKey(K k) {
            String md5 = MD5Util.md5ToString(k.toString());
            return md5;
        }

    }

    public static interface ISerialization<K, V> {
        void write(File file, K k, V v);

        V read(File file, K k);
    }

    public static abstract class BaseSerialization<K, V> implements ISerialization<K, V> {
        public static final String CHARSET = "UTF-8";

        @Override
        public void write(final File file, final K k, final V v) {
            WorkQueue.run(new Runnable() {
                @Override
                public void run() {
                    FileOutputStream out = null;
                    try {
                        out = new FileOutputStream(file);
                        BaseSerialization.this.write(out, k, v);
                        out.flush();
                    } catch (IOException e) {
                        Logger.w("DiskCache", "save failed: " + e.getMessage());
                    } finally {
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            });
        }

        protected abstract void write(OutputStream out, K k, V v) throws IOException;

        @Override
        public V read(File file, K k) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(file);
                return read(file, in, k);
            } catch (Exception e) {
                Logger.w("DiskCache", "read " + file + "failed: " + e.getMessage());
                return null;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        protected abstract V read(File file, InputStream in, K k) throws Exception;

        protected byte[] readFully(File file, InputStream in) throws IOException {
            int length = (int) file.length();
            byte[] data = new byte[length];
            in.read(data);
            return data;
        }

        protected String readString(File file, InputStream in) throws IOException {
            byte[] data = readFully(file, in);
            return new String(data, CHARSET);
        }
    }

    public static class BitmapSerialization<K> extends BaseSerialization<K, Bitmap> {
        protected void write(OutputStream out, K k, Bitmap bitmap) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        }

        @Override
        protected Bitmap read(File file, InputStream in, K o) {
            return BitmapFactory.decodeStream(in);
        }
    }

    public static class StringSerialization<K> extends BaseSerialization<K, String> {

        @Override
        protected void write(OutputStream out, K o, String s) throws IOException {
            out.write(s.getBytes(CHARSET));
        }

        @Override
        protected String read(File file, InputStream in, K o) throws Exception {
            return readString(file, in);
        }
    }

    public static class ByteArraySerialization<K> extends BaseSerialization<K, byte[]> {

        @Override
        protected void write(OutputStream out, K o, byte[] bytes) throws IOException {
            out.write(bytes);
        }

        @Override
        protected byte[] read(File file, InputStream in, K o) throws IOException {
            return readFully(file, in);
        }
    }

    public static class JSONSerialization<K> extends BaseSerialization<K, Object> {
        @Override
        protected void write(OutputStream out, Object K, Object json) throws IOException {
            if (json instanceof JSONObject || json instanceof JSONArray
                    || json == JSONObject.NULL) {
                out.write(json.toString().getBytes(CHARSET));
            }
        }

        @Override
        protected Object read(File file, InputStream in, K o) throws Exception {
            String json = readString(file, in);
            JSONTokener tokener = new JSONTokener(json);
            return tokener.nextValue();
        }
    }
}
