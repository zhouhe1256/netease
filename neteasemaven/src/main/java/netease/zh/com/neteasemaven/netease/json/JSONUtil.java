package netease.zh.com.neteasemaven.netease.json;




import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import netease.zh.com.neteasemaven.netease.json.annotation.Ignore;
import netease.zh.com.neteasemaven.netease.json.annotation.JSON;
import netease.zh.com.neteasemaven.netease.json.annotation.JSONCollection;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class JSONUtil {
    private JSONUtil() {
    }

    static class JSONField {
        public final Field field;
        public final Class<?> type;
        public final Class<?> elementType;
        private final String name;

        public JSONField(Field field) {
            this.field = field;
            field.setAccessible(true);
            type = field.getType();

            JSON json = field.getAnnotation(JSON.class);
            if (json != null) {
                name = json.name();
            } else {
                name = field.getName();
            }

            JSONCollection jsonCollection = field
                    .getAnnotation(JSONCollection.class);
            if (jsonCollection != null) {
                elementType = jsonCollection.type();
            } else {
                elementType = null;
            }
        }

        public String getName() {
            return name;
        }

        public Object get(Object context) {
            try {
                return field.get(context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void set(Object context, Object value) {
            try {
                if (type.isPrimitive() && value == null) {
                    return;
                }

                if (value != null) {
                    if (type == Long.class && value instanceof Integer) {
                        value = ((Integer) value).longValue();
                    }
                    if (type == String.class && !(value instanceof String)) {
                        value = value.toString();
                    }
                }

                field.set(context, value);
            } catch (Exception e) {
                throw new RuntimeException("set " + context.getClass() + "#" + field.getName() + " to " + value + " failed", e);
            }
        }
    }

    static class ClassWrapper {
        private final Class<?> klass;
        private final List<JSONField> fields = new ArrayList<JSONField>();

        public ClassWrapper(Class<?> klass) {
            this.klass = klass;

            scan(klass, new HashSet<String>());
        }

        private void scan(Class<?> klass, Set<String> names) {
            for (Field field : klass.getDeclaredFields()) {
                int m = field.getModifiers();
                if (Modifier.isStatic(m) || Modifier.isFinal(m)) {
                    continue;
                }
                Ignore ignore = field.getAnnotation(Ignore.class);
                if (ignore != null) {
                    continue;
                }

                if (names.contains(field.getName())) {
                    continue;
                }

                JSONField f = new JSONField(field);
                fields.add(f);
            }

            Class<?> superClass = klass.getSuperclass();
            if (superClass == null || superClass == Object.class) {
                return;
            }

            scan(superClass, names);
        }

        public Class<?> getKlass() {
            return klass;
        }

        public List<JSONField> getFields() {
            return fields;
        }
    }

    private static ConcurrentHashMap<Class<?>, ClassWrapper> wrappers = new ConcurrentHashMap<Class<?>, ClassWrapper>();

    private static ClassWrapper getClassWrapper(Object obj) {
        Class<?> klass = obj.getClass();
        ClassWrapper wrapper = wrappers.get(klass);
        if (wrapper == null) {
            wrapper = new ClassWrapper(klass);
            wrappers.putIfAbsent(klass, wrapper);
        }

        return wrapper;
    }

    public static String dump(Object obj) {
        try {
            Object value = wrap(obj);
            return value.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object wrap(Object obj) throws JSONException {
        if (obj == null) {
            return JSONObject.NULL;
        } else if (isBasicValue(obj)) {
            return obj;
        } else if (obj.getClass().isArray()) {
            ArrayList<Object> values = new ArrayList<Object>();
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i += 1) {
                values.add(wrap(Array.get(obj, i)));
            }
            return new JSONArray(values);
        } else if (obj instanceof Collection) {
            ArrayList<Object> values = new ArrayList<Object>();
            for (Object item : (List<?>) obj) {
                values.add(wrap(item));
            }
            return new JSONArray(values);
        } else if (obj instanceof Map) {
            return wrap((Map<?, ?>) obj);
        } else {
            Class<?> klass = obj.getClass();
            ClassWrapper wrapper = wrappers.get(klass);
            if (wrapper == null) {
                wrapper = new ClassWrapper(klass);
                wrappers.putIfAbsent(klass, wrapper);
            }

            JSONObject json = new JSONObject();
            for (JSONField f : wrapper.getFields()) {
                Object value = f.get(obj);
                if (value == null) {
                    continue;
                }
                value = wrap(value);
                if (value == null || value == JSONObject.NULL) {
                    continue;
                }

                json.put(f.getName(), value);
            }
            return json;
        }
    }

    private static JSONObject wrap(Map<?, ?> map) throws JSONException {
        JSONObject object = new JSONObject();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            object.put(key, wrap(value));
        }
        return object;
    }

    private static boolean isBasicValue(Object o) {
        if (o.getClass().isPrimitive()) {
            return true;
        }

        if (o instanceof Boolean || o instanceof Byte || o instanceof Character
                || o instanceof Double || o instanceof Float
                || o instanceof Integer || o instanceof Long
                || o instanceof Short || o instanceof String) {
            return true;
        }

        return false;
    }

    private static boolean isBasicType(Class<?> klass) {
        if (klass.isPrimitive()) {
            return true;
        }

        if (klass == Boolean.class || klass == Byte.class
                || klass == Character.class || klass == Double.class
                || klass == Float.class || klass == Integer.class
                || klass == Long.class || klass == Short.class
                || klass == String.class) {
            return true;
        }
        return false;
    }

    public static <T> T load(Class<T> klass, String json) {
        JSONTokener tokener = new JSONTokener(json);
        try {
            return load(klass, tokener.nextValue());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T load(Class<T> klass, Object json) {
        if (json == null || json == JSONObject.NULL) {
            return null;
        } else if (isBasicType(klass)) {
            return (T) json;
        } else if (json instanceof JSONArray) {
            return (T) load(klass, (JSONArray) json);
        } else if (json instanceof JSONObject) {
            return load(klass, (JSONObject) json);
        } else {
            throw new IllegalArgumentException("Class=" + klass + ", json="
                    + json);
        }
    }

    public static <T> List<T> load(Class<T> klass, JSONArray array) {
        if (array == null) {
            return null;
        }

        ArrayList values = new ArrayList();
        int length = array.length();
        for (int i = 0; i < length; i++) {
            try {
                Object jsonValue = array.get(i);
                values.add(load(klass, jsonValue));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return values;
    }

    public static <T> T load(Class<T> klass, JSONObject json) {
        Object obj = newInstance(klass);
        ClassWrapper wrapper = getClassWrapper(obj);
        for (JSONField f : wrapper.getFields()) {
            if (isBasicType(f.type)) {
                f.set(obj, json.opt(f.getName()));
            } else if (f.type.isArray()) {
                JSONArray array = json.optJSONArray(f.getName());
                if (array == null) {
                    f.set(obj, null);
                    continue;
                }

                int length = array.length();
                Object values = Array.newInstance(f.type, length);
                for (int i = 0; i < length; i++) {
                    try {
                        Object jsonValue = array.get(i);
                        Array.set(values, i, load(f.elementType, jsonValue));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                f.set(obj, values);
            } else if (isCollection(f.type)) {
                JSONArray array = json.optJSONArray(f.getName());
                if (array == null) {
                    f.set(obj, null);
                    continue;
                }

                List values = load(f.elementType, array);

                Collection collection = null;
                if (f.type.isInterface()) {
                    if (f.type == List.class) {
                        collection = new ArrayList();
                    } else if (f.type == Set.class) {
                        collection = new HashSet();
                    } else {
                        throw new IllegalArgumentException();
                    }
                } else {
                    collection = (Collection) newInstance(f.type);
                }

                collection.addAll(values);
                f.set(obj, collection);
            } else if (isMap(f.type)) {
                Object value = loadMap(f.elementType,
                        (JSONObject) json.opt(f.getName()));
                f.set(obj, value);
            } else {
                Object value = load(f.type, json.opt(f.getName()));
                f.set(obj, value);
            }
        }
        return (T) obj;
    }

    public static Map<String, Object> loadMap(Class<?> valueType,
                                              JSONObject json) {
        if (json == null) {
            return null;
        }

        Map map = new HashMap();
        Iterator iterator = json.keys();
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            Object value = json.opt(name);
            map.put(name, load(valueType, value));
        }

        return map;
    }

    private static boolean isMap(Class<?> klass) {
        if (klass == null) {
            return false;
        }
        if (klass == Map.class) {
            return true;
        }

        for (Class<?> c : klass.getInterfaces()) {
            if (isMap(c)) {
                return true;
            }
        }

        return isMap(klass.getSuperclass());
    }

    private static boolean isCollection(Class<?> klass) {
        if (klass == null) {
            return false;
        }
        if (klass == Collection.class) {
            return true;
        }

        for (Class<?> c : klass.getInterfaces()) {
            if (isCollection(c)) {
                return true;
            }
        }

        return isCollection(klass.getSuperclass());
    }

    private static Object newInstance(Class<?> klass) {
        try {
            return klass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Class=" + klass, e);
        }
    }

    public static <T> List<T> loadList(Class<T> klass, JSONArray json) {
        try {
            ArrayList<T> result = new ArrayList<T>();
            boolean isBasicType = isBasicType(klass);
            if (json != null) {
                int size = json.length();
                for (int i = 0; i < size; i++) {
                    if (isBasicType) {
                        if (klass == Integer.TYPE || klass == Integer.class) {
                            Integer value = json.getInt(i);
                            result.add((T) value);
                        } else if (klass == Long.TYPE || klass == Long.class) {
                            Long value = json.getLong(i);
                            result.add((T) value);
                        } else if (klass == Boolean.TYPE || klass == Boolean.class) {
                            Boolean value = json.getBoolean(i);
                            result.add((T) value);
                        } else if (klass == Double.TYPE || klass == Double.class) {
                            Double value = json.getDouble(i);
                            result.add((T) value);
                        } else if (klass == String.class) {
                            result.add((T) json.getString(i));
                        }
                    } else {
                        T t = load(klass, json.getJSONObject(i));
                        result.add(t);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
