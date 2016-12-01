package netease.zh.com.neteasemaven.netease.view;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;

public class LargeTouchableAdapter {
    public static void adapt(final View view, int padding) {
        adapt(view, padding, padding, padding, padding);
    }

    public static void adapt(final View view, final int paddingTop, final int paddingRight,
                             final int paddingBottom, final int paddingLeft) {
        final View parent = (View) view.getParent();
        if (parent == null) {
            return;
        }

        parent.post(new Runnable() {
            @Override
            public void run() {
                final Rect r = new Rect();

                view.getHitRect(r);
                r.left -= paddingLeft;
                r.top -= paddingTop;
                r.right += paddingRight;
                r.bottom += paddingBottom;

                TouchDelegate touchDelegate = parent.getTouchDelegate();
                if (touchDelegate != null && touchDelegate instanceof TouchDelegateGroup) {
                    TouchDelegateGroup group = (TouchDelegateGroup) touchDelegate;
                    group.addTouchDelegate(new TouchDelegate(r, view));
                } else {
                    TouchDelegateGroup group = new TouchDelegateGroup(parent);
                    if (touchDelegate != null) {
                        group.addTouchDelegate(touchDelegate);
                    }
                    group.addTouchDelegate(new TouchDelegate(r, view));

                    parent.setTouchDelegate(group);
                }
            }
        });
    }
}
