package guhj.github.simple;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import guhj.github.slidelayout.SlideBindingListView;
import guhj.github.slidelayout.SlideLayout;

public class SlideBindingListViewAdapter extends SlideListViewAdapter implements SlideBindingListView.OnSlideChangedListener {

    private int scrollX;

    public SlideBindingListViewAdapter(Context context, SlideBindingListView listView) {
        super(context);
        listView.setOnSlideChangedListener(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.slidelayout.setContentScrollX(scrollX);
        return convertView;
    }

    @Override
    protected String getSimpleText(int position) {
        return "SlideBindingListViewAdapter " + position;
    }

    @Override
    public void onSlideMove(SlideLayout slide) {
        scrollX = slide.getContentScrollX();
    }

    @Override
    public void onSlideEnd(SlideLayout slide) {
        scrollX = slide.getContentScrollX();
    }
}
