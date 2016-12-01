package netease.zh.com.neteasemaven.netease.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import netease.zh.com.neteasemaven.netease.async.IPromise;


public class RemoteImageView extends ImageView {
    private String url;
    private IPromise promise;

    private int defaultImageId;
    private int errorImageId;

    public RemoteImageView(Context context) {
        super(context);
    }

    public RemoteImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RemoteImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public IPromise getPromise() {
        return promise;
    }

    public void setUrl(String url) {
        this.url = url;

        promise = ImageViewAdapter.adapt(this, this.url, defaultImageId, errorImageId,true);
    }

}
