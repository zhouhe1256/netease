package netease.zh.com.neteasemaven.netease.util;

public final class ValidationUtil {

    public static class ValidationException extends RuntimeException {
        private String value;
        private String message;

        public String getValue() {
            return value;
        }

        @Override
        public String getMessage() {
            return message;
        }

        public ValidationException(String value, String message) {
            super(message);
            this.value = value;
            this.message = message;
        }
    }

    public static void required(String value, String message) {
        if (StringUtil.isEmpty(value)) {
            throw new ValidationException(value, message);
        }
    }

    public static void matches(String value, String pattern, String message) {
        if (value == null) {
            return;
        }

        if (!value.matches(pattern)) {
            throw new ValidationException(value, message);
        }
    }

    public static void confirm(String value1, String value2, String message) {
        if (!value1.equals(value2)) {
            throw new ValidationException(value2, message);
        }
    }
}
