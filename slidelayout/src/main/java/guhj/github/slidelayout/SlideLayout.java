package guhj.github.slidelayout;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * 侧滑Layout
 * @attr ref guhj.github.slidelayout.R.styleable#SlideLayout_sl_leftLayout 左边的LayoutId
 * @attr ref guhj.github.slidelayout.R.styleable#SlideLayout_sl_rightLayout 右边的LayoutId
 * @attr ref guhj.github.slidelayout.R.styleable#SlideLayout_sl_contentLayout 中间的LayoutId
 */
public class SlideLayout extends FrameLayout {
    private final long DEFAULT_DURATION = 400l;

//    public static final int SCROLL_STATE_CLOSE = 0;//关闭
//    public static final int SCROLL_STATE_OPEN_LEFT = 1;//左边打开
//    public static final int SCROLL_STATE_OPEN_RIGHT = 2;//右边打开

    public SlideLayout(Context context) {
        this(context, null);
    }

    public SlideLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mInterpolator = new AccelerateDecelerateInterpolator(context, attrs);
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SlideLayout, 0, 0);
        try {
            LayoutInflater factory = LayoutInflater.from(context);
            int id = a.getResourceId(R.styleable.SlideLayout_sl_leftLayout, -1);
            if (id != -1) {
                leftLayout = factory.inflate(id, this, false);
                addView(leftLayout);
            }
            id = a.getResourceId(R.styleable.SlideLayout_sl_rightLayout, -1);
            if (id != -1) {
                rightLayout = factory.inflate(id, this, false);
                addView(rightLayout);
            }
            id = a.getResourceId(R.styleable.SlideLayout_sl_contentLayout, -1);
            if (id != -1) {
                contentLayout = factory.inflate(id, this, false);
                addView(contentLayout);
            }
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View view = findViewById(R.id.sl_leftLayout);
        if (view != null) {
            leftLayout = view;
        }
        view = findViewById(R.id.sl_rightLayout);
        if (view != null) {
            rightLayout = view;
        }
        view = findViewById(R.id.sl_contentLayout);
        if (view != null) {
            contentLayout = view;
            bringChildToFront(contentLayout);
        }
    }

    View leftLayout, rightLayout, contentLayout;

    /**
     * True if the user is currently dragging this ScrollView around. This is
     * not the same as 'is being flinged', which can be checked by
     * mScroller.isFinished() (flinging begins when the user lifts his finger).
     */
    private boolean mIsBeingDragged = false;
    private boolean mIsTouchEnable = false;//是否有效的touch
    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = -1;
    /**
     * Position of the last motion event.
     */
    private int mLastMotionX, mLastMotionY;
    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int contentScrollX;
    private Integer minScrollX, maxScrollX;
    //    private int mScrollState = SCROLL_STATE_CLOSE;
    private ValueAnimator mScrollXAnim;
    private TimeInterpolator mInterpolator;

    private SwipeRefreshLayout layout;
    private boolean check = false;

    private SlideLayoutManager manager;


    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        measureChildren(widthMeasureSpec, heightMeasureSpec);
//    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (!mIsBeingDragged && contentScrollX != 0) {
                    final int x1 = (int) ev.getX();
                    final int y1 = (int) ev.getY();
                    if (isTransformedTouchPointInView(x1, y1, contentLayout)) {
                        closeSlide(true);
                        break;
                    }
                }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if (leftLayout != null) {
            leftLayout.layout(0, 0, leftLayout.getMeasuredWidth(), leftLayout.getMeasuredHeight());
        }
        if (rightLayout != null) {
            final int cR = getMeasuredWidth();
            final int cL = cR - rightLayout.getMeasuredWidth();
            rightLayout.layout(cL, 0, cR, rightLayout.getMeasuredHeight());
        }
        if (contentLayout != null) {
            final int cR = contentScrollX + contentLayout.getMeasuredWidth();
            contentLayout.layout(contentScrollX, 0, cR, contentLayout.getMeasuredHeight());
        }
        resetMinMaxScrollX();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */
        /*
        * Shortcut the most recurring case: the user is in the dragging
        * state and he is moving his finger.  We want to intercept this
        * motion.
        */
        final int action = ev.getAction();
        if (!isEnabled()) {
            return false;
        }
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            return true;
        }
        if (!hasScroll()) {
            return false;
        }
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                * Locally do absolute value. mLastMotionX is set to the x value
                * of the down event.
                */
                final int activePointerId = mActivePointerId;
                if (activePointerId == -1) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
//                    Log.e(TAG, "Invalid pointerId=" + activePointerId
//                            + " in onInterceptTouchEvent");
                    break;
                }

                final int x = (int) ev.getX(pointerIndex);
                final int y = (int) ev.getY(pointerIndex);
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);


                if (xDiff > mTouchSlop || yDiff > mTouchSlop) {
                    if (xDiff > yDiff) {
                        mIsTouchEnable = true;
                        mIsBeingDragged = true;
                        mLastMotionX = x;
                        initVelocityTrackerIfNotExists();
                        mVelocityTracker.addMovement(ev);
                        if (getParent() != null)
                            getParent().requestDisallowInterceptTouchEvent(true);
                        manager = findSlideLayoutManager();
                        if (manager != null)
                            manager.onStart(this);

                        if (check) {
                            check = false;
                        }
                    } else {
                        if (getParent() != null)
                            getParent().requestDisallowInterceptTouchEvent(false);
                        if (check && layout != null) {
                            check = false;
                            layout.setEnabled(true);
                            layout = null;
                        }
                    }
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mLastMotionX = x;
                mLastMotionY = y;
                mActivePointerId = ev.getPointerId(0);
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                /*
                * If being flinged and user touches the screen, initiate drag;
                * otherwise don't.  mScroller.isFinished should be false when
                * being flinged.
                */
                mIsBeingDragged = animIsRunning();

                layout = findSwipe();
                if (layout != null) {
                    layout.setEnabled(false);
                    check = true;
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                /* Release the drag */
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    mActivePointerId = -1;
                    toNearbyView();
                }
                if (layout != null) {
                    layout.setEnabled(true);
                    layout = null;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                mLastMotionX = (int) ev.getX(index);
                mLastMotionY = (int) ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                mLastMotionX = (int) ev.getX(ev.findPointerIndex(mActivePointerId));
                break;
        }

        /*
        * The only time we want to intercept motion events is if we are in the
        * drag mode.
        */
        return mIsBeingDragged;
    }

    protected boolean isTransformedTouchPointInView(float x, float y, View child) {
        int cx = contentScrollX;
        int cy = 0;
        if (x < cx || x > (cx + child.getWidth()) || y < cy || y > (cy + child.getHeight())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }

        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if (!hasScroll()) {
                    return false;
                }
                mIsTouchEnable = true;

                if (layout == null)
                    layout = findSwipe();
                if (layout != null) {
                    layout.setEnabled(false);
                    check = true;
                }

                if ((mIsBeingDragged = animIsRunning())) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                /*
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */
                if (animIsRunning()) {
                    mScrollXAnim.removeAllUpdateListeners();
                    mScrollXAnim.cancel();
                    mScrollXAnim = null;
                }
                // Remember where the motion event started
                mLastMotionX = (int) ev.getX();
                mLastMotionY = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN:
                mIsTouchEnable = true;

                final int index = ev.getActionIndex();
                mLastMotionX = (int) ev.getX(index);
                mLastMotionY = (int) ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsTouchEnable) {
                    return false;
                }
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    break;
                }
                final int x = (int) ev.getX(activePointerIndex);
                final int y = (int) ev.getY(activePointerIndex);
                int deltaX = x - mLastMotionX;
                int deltaY = y - mLastMotionY;
                if (!mIsBeingDragged && (Math.abs(deltaX) > mTouchSlop || Math.abs(deltaY) > mTouchSlop)) {
                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        final ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                        mIsBeingDragged = true;
                        if (deltaX > 0) {
                            deltaX -= mTouchSlop;
                        } else {
                            deltaX += mTouchSlop;
                        }
                        if (check)
                            check = false;


                        manager = findSlideLayoutManager();
                        if (manager != null)
                            manager.onStart(this);
                    } else {
                        if (getParent() != null)
                            getParent().requestDisallowInterceptTouchEvent(false);
                        if (check && layout != null) {
                            check = false;
                            layout.setEnabled(true);
                            layout = null;
                        }
                    }
                }
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    mLastMotionX = x;
                    contentScrollX += deltaX;
                    valiMinMaxScrollX();
                    if (contentScrollX < minScrollX) {
                        if (minScrollX == 0)
                            contentScrollX = 0;
                        else
                            contentScrollX -= deltaX * (minScrollX - contentScrollX) / getMeasuredWidth() * 2;
                    } else if (contentScrollX > maxScrollX) {
                        if (maxScrollX == 0)
                            contentScrollX = 0;
                        else
                            contentScrollX -= deltaX * (contentScrollX - maxScrollX) / getMeasuredWidth() * 2;
                    }
                    changeChildViewScrollX();
                    if (manager != null)
                        manager.onMove(this);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getXVelocity(mActivePointerId);

                    if (getChildCount() > 0) {
                        if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                            fling(initialVelocity);
                        } else {
                            toNearbyView();
                        }
                    }
                    mActivePointerId = -1;
                    mIsBeingDragged = false;
                    recycleVelocityTracker();
                }
                if (layout != null) {
                    layout.setEnabled(true);
                    layout = null;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged && getChildCount() > 0) {
                    if (mIsBeingDragged) {
                        toNearbyView();
                    }
                    mActivePointerId = -1;
                    mIsBeingDragged = false;
                    recycleVelocityTracker();
                }
                if (layout != null) {
                    layout.setEnabled(true);
                    layout = null;
                }
                break;
        }
        return true;
    }

    private boolean isChangeChildView;

    private void onChangeChildViewScrollX() {
        if (getMeasuredWidth() != 0) {
            changeChildViewScrollX();
        }
        if (isChangeChildView) return;
        isChangeChildView = true;
        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                changeChildViewScrollX();
                isChangeChildView = false;
            }
        });
    }

    private void changeChildViewScrollX() {
        if (contentScrollX == 0) {
            if (leftLayout != null)
                leftLayout.setVisibility(INVISIBLE);
            if (rightLayout != null)
                rightLayout.setVisibility(INVISIBLE);
            if (contentLayout != null) {
                contentLayout.setEnabled(true);
            }
        } else if (contentScrollX > 0) {
            if (leftLayout != null)
                leftLayout.setVisibility(VISIBLE);
            if (rightLayout != null)
                rightLayout.setVisibility(INVISIBLE);
            if (contentLayout != null) {
                contentLayout.setEnabled(false);
            }
        } else {
            if (leftLayout != null)
                leftLayout.setVisibility(INVISIBLE);
            if (rightLayout != null)
                rightLayout.setVisibility(VISIBLE);
            if (contentLayout != null) {
                contentLayout.setEnabled(false);
            }
        }
        requestLayout();
    }


    private SwipeRefreshLayout findSwipe() {
        ViewParent v = getParent();
        while (v != null && v != getRootView() && (!(v instanceof SwipeRefreshLayout) || !((View) v).isEnabled())) {
            v = v.getParent();
        }
        if (v instanceof SwipeRefreshLayout)
            return (SwipeRefreshLayout) v;
        return null;
    }

    private boolean hasScroll() {
        return (leftLayout != null && leftLayout.getMeasuredWidth() > 0)
                || (rightLayout != null && rightLayout.getMeasuredWidth() > 0);
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = (int) ev.getX(newPointerIndex);
            mLastMotionY = (int) ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private void valiMinMaxScrollX() {
        if (minScrollX != null && maxScrollX != null)
            return;
        int w = getMeasuredWidth();
        if (w != 0) {
            if (rightLayout != null) {
                minScrollX = -rightLayout.getMeasuredWidth();
            } else {
                minScrollX = 0;
            }
            if (leftLayout != null) {
                maxScrollX = leftLayout.getMeasuredWidth();
            } else {
                maxScrollX = 0;
            }
        }
    }

    private void resetMinMaxScrollX() {
        minScrollX = null;
        maxScrollX = null;
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private boolean animIsRunning() {
        if (mScrollXAnim == null) return false;
        return mScrollXAnim.isRunning();
    }

    private void animScrollTo(int scrollX) {
        if (mScrollXAnim != null && mScrollXAnim.isRunning()) {
            mScrollXAnim.cancel();
        }
        valiMinMaxScrollX();
        if (scrollX < minScrollX) {
            scrollX = minScrollX;
        } else if (scrollX > maxScrollX) {
            scrollX = maxScrollX;
        }

        mScrollXAnim = ValueAnimator.ofInt(contentScrollX, scrollX);
        mScrollXAnim.setInterpolator(mInterpolator);
        long duration = (long) (DEFAULT_DURATION * Math.abs(scrollX - contentScrollX) / (float) getMeasuredWidth());
        mScrollXAnim.setDuration(duration);
        mScrollXAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                contentScrollX = (int) animation.getAnimatedValue();
                changeChildViewScrollX();
                if (manager != null)
                    manager.onMove(SlideLayout.this);
            }
        });
        mScrollXAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (manager != null)
                    manager.onEnd(SlideLayout.this);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        mScrollXAnim.start();
    }

    private void toNearbyView() {
        valiMinMaxScrollX();
        if (contentScrollX < minScrollX / 2) {
            animScrollTo(minScrollX);
        } else if (contentScrollX > maxScrollX / 2) {
            animScrollTo(maxScrollX);
        } else {
            animScrollTo(0);
        }
    }

    private void fling(int initialVelocity) {
        valiMinMaxScrollX();
        if (contentScrollX < minScrollX) {
            animScrollTo(minScrollX);
        } else if (contentScrollX < 0) {
            if (initialVelocity < 0)
                animScrollTo(minScrollX);
            else
                animScrollTo(0);
        } else if (contentScrollX < maxScrollX) {
            if (initialVelocity < 0)
                animScrollTo(0);
            else
                animScrollTo(maxScrollX);
        } else {
            animScrollTo(maxScrollX);
        }
    }

    private SlideLayoutManager findSlideLayoutManager() {
        ViewParent v = getParent();
        while (v != null && v != getRootView() && (!(v instanceof SlideLayoutManager) || !((View) v).isEnabled())) {
            v = v.getParent();
        }
        if (v instanceof SlideLayoutManager)
            return (SlideLayoutManager) v;
        return null;
    }

    public void closeSlide(boolean anim) {
        if (manager == null)
            manager = findSlideLayoutManager();
        if (manager != null)
            manager.onStart(this);
        _closeSlide(anim);
    }

    private void _closeSlide(boolean anim) {
        if (mScrollXAnim != null && mScrollXAnim.isRunning()) {
            mScrollXAnim.cancel();
        }
        mIsTouchEnable = false;
        mIsBeingDragged = false;
        if (contentScrollX == 0) return;
        if (getMeasuredWidth() == 0 || !anim) {
            contentScrollX = 0;
            onChangeChildViewScrollX();
        } else {
            animScrollTo(0);
        }
    }

    public int getContentScrollX() {
        return contentScrollX;
    }

    public void setContentScrollX(int contentScrollX) {
        if (contentScrollX != this.contentScrollX) {
            this.contentScrollX = contentScrollX;
            onChangeChildViewScrollX();
        }
    }

    public int getLeftLayoutWidth(){
        return leftLayout.getWidth();
    }

    public interface SlideLayoutManager {
        void onStart(SlideLayout slideLayout);

        void onMove(SlideLayout slideLayout);

        void onEnd(SlideLayout slideLayout);
    }
}
