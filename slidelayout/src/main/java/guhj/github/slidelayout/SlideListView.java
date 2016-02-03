package guhj.github.slidelayout;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * 只允许一个SlideLayout划出的ListView
 */
public class SlideListView extends ListView implements SlideLayout.SlideLayoutManager {
    public SlideListView(Context context) {
        this(context, null);
    }

    public SlideListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        ListAdapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        super.setAdapter(adapter);
        if (slide != null) {
            slide.closeSlide(false);
            slide = null;
        }
        adapter.registerDataSetObserver(mDataSetObserver);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (slide != null) {
                    final int y = (int) ev.getY();
                    int deltaY = y - mLastMotionY;
                    if (Math.abs(deltaY) > mTouchSlop) {
                        slide.closeSlide(true);
                        slide = null;
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private SlideLayout slide;
    private int mLastMotionY;
    private int mTouchSlop;
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            if (slide != null) {
                slide.closeSlide(false);
                slide = null;
            }
        }

        @Override
        public void onInvalidated() {
        }
    };


    @Override
    public void onStart(SlideLayout slideLayout) {
        if (slide != null && slide != slideLayout) {
            slide.closeSlide(true);
        }
        slide = slideLayout;
    }

    @Override
    public void onMove(SlideLayout slideLayout) {
    }

    @Override
    public void onEnd(SlideLayout slideLayout) {
        if (slideLayout.getContentScrollX() == 0) {
            if (slideLayout == slide)
                slide = null;
        }
    }
}
