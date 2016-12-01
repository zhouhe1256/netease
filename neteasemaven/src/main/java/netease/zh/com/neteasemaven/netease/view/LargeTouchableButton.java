package netease.zh.com.neteasemaven.netease.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class LargeTouchableButton extends Button {
    public LargeTouchableButton(Context context) {
        super(context);
    }

    public LargeTouchableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LargeTouchableButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        LargeTouchableAdapter.adapt(this, getPaddingTop(), getPaddingRight(), getPaddingBottom(), getPaddingLeft());
    }

}
