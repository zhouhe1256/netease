package netease.zh.com.neteasemaven.netease.util;

import android.content.Context;
import android.widget.Toast;

public class DialogUtil {

    private static Toast toast;

    private DialogUtil() {

    }

    public static void hintMessage(Context context, String message) {
        if (toast == null) {
            toast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

}
