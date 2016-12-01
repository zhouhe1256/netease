package netease.zh.com.neteasemaven.netease.view;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;

import java.util.ArrayList;

/**
 * see https://github.com/cyrilmottier/ListViewTipsAndTricks/blob/master/src/com/cyrilmottier/android/listviewtipsandtricks/view/TouchDelegateGroup.java.
 *
 * An implementation of a {@link TouchDelegate} container. This class can be
 * pretty useful when you want to set several {@link TouchDelegate} to a single
 * {@link View}. In case some areas overlap, the firstly added
 * {@link TouchDelegate} will be used.
 *
 * @author Cyril Mottier
 */
public class TouchDelegateGroup extends TouchDelegate {

    /**
     * A completely useless {@link Rect} that will only be used to create
     * instances of {@link TouchDelegateGroup}.
     */
    private static final Rect USELESS_HACKY_RECT = new Rect();

    private ArrayList<TouchDelegate> mTouchDelegates;
    private TouchDelegate mCurrentTouchDelegate;

    /**
     * Creates a {@link TouchDelegateGroup}.
     *
     * @param uselessHackyView A non-null {@link View} that will have no effect
     *            on the newly created {@link TouchDelegateGroup}. As a result
     *            you can pass any {@link View} instance you want as long as it
     *            is not null.
     */
    public TouchDelegateGroup(View uselessHackyView) {
        // I know this is pretty hacky. Unfortunately there is no other way to
        // create a TouchDelegate containing TouchDelegates since TouchDelegate
        // is not an interface, it doesn't support null arguments and has no
        // super() constructor
        super(USELESS_HACKY_RECT, uselessHackyView);
    }

    /**
     * Add a new {@link TouchDelegate}.
     *
     * @param bounds Bounds in local coordinates of the containing view that
     *            should be mapped to the delegate view
     * @param delegateView The view that should receive motion events
     */
    public void addTouchDelegate(TouchDelegate touchDelegate) {
        if (mTouchDelegates == null) {
            mTouchDelegates = new ArrayList<TouchDelegate>();
        }
        mTouchDelegates.add(touchDelegate);
    }

    /**
     * Remove a previously added {@link TouchDelegate}.
     *
     * @param touchDelegate The {@link TouchDelegate} to remove in this
     *            {@link TouchDelegateGroup}.
     */
    public void removeTouchDelegate(TouchDelegate touchDelegate) {
        if (mTouchDelegates != null) {
            mTouchDelegates.remove(touchDelegate);
        }
        if (mTouchDelegates.isEmpty()) {
            mTouchDelegates = null;
        }
    }

    /**
     * Remove all {@link TouchDelegate}
     */
    public void clearTouchDelegates() {
        if (mTouchDelegates != null) {
            mTouchDelegates.clear();
        }
        mCurrentTouchDelegate = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        TouchDelegate delegate = null;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mTouchDelegates != null) {
                    for (TouchDelegate touchDelegate : mTouchDelegates) {
                        if (touchDelegate != null) {
                            if (touchDelegate.onTouchEvent(event)) {
                                mCurrentTouchDelegate = touchDelegate;
                                return true;
                            }
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                delegate = mCurrentTouchDelegate;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                delegate = mCurrentTouchDelegate;
                mCurrentTouchDelegate = null;
                break;
        }

        return delegate == null ? false : delegate.onTouchEvent(event);
    }

}