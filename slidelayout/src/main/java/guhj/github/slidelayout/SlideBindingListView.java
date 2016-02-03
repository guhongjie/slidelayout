package guhj.github.slidelayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * 实现了ListView内部SlideLayout同时滑动
 */
public class SlideBindingListView extends ListView implements SlideLayout.SlideLayoutManager {
    public SlideBindingListView(Context context) {
        this(context, null);
    }

    public SlideBindingListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideBindingListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /***
     * 关闭所有SlideLayout
     * @return
     */
    public boolean closeAll(){
        for (int i = 0, len = getChildCount(); i < len; i++) {
            View v = getChildAt(i);
            SlideLayout slide2 = (SlideLayout) v.findViewById(R.id.sl_slideBindingId);
            if (slide2 != null) {
                slide2.closeSlide(true);
                return true;
            }
        }
        return false;
    }

    private SlideLayout slide;
    private ArrayList<SlideLayout> binderSlide = new ArrayList<SlideLayout>();

    @Override
    public void onStart(SlideLayout slideLayout) {
        if (slide == null && slideLayout.getId() == R.id.sl_slideBindingId) {
            slide = slideLayout;
            for (int i = 0, len = getChildCount(); i < len; i++) {
                View v = getChildAt(i);
                SlideLayout slide2 = (SlideLayout) v.findViewById(R.id.sl_slideBindingId);
                if (slide2 != null && slide2 != slide) {
                    binderSlide.add(slide2);
                    slide2.setEnabled(false);
                }
            }
        }
    }

    @Override
    public void onMove(SlideLayout slideLayout) {
        if (slide == slideLayout) {
            for (int i = 0, len = binderSlide.size(); i < len; i++) {
                binderSlide.get(i).setContentScrollX(slideLayout.getContentScrollX());
            }
            if (onSlideChangedListener != null) {
                onSlideChangedListener.onSlideMove(slide);
            }
        }
    }

    @Override
    public void onEnd(SlideLayout slideLayout) {
        if (slideLayout == slide) {
            for (int i = 0, len = binderSlide.size(); i < len; i++) {
                binderSlide.get(i).setContentScrollX(slideLayout.getContentScrollX());
                binderSlide.get(i).setEnabled(true);
            }
            if (onSlideChangedListener != null) {
                onSlideChangedListener.onSlideMove(slide);
                onSlideChangedListener.onSlideEnd(slide);
            }

            slide = null;
            binderSlide.clear();

        }
    }

    OnSlideChangedListener onSlideChangedListener;

    public void setOnSlideChangedListener(OnSlideChangedListener listener) {
        onSlideChangedListener = listener;
    }

    public OnSlideChangedListener getOnSlideChangedListener() {
        return onSlideChangedListener;
    }


    public interface OnSlideChangedListener {
        void onSlideMove(SlideLayout slide);

        void onSlideEnd(SlideLayout slide);
    }
}
