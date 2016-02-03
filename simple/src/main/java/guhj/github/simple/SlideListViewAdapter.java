package guhj.github.simple;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import guhj.github.slidelayout.SlideLayout;

public class SlideListViewAdapter extends BaseAdapter implements View.OnClickListener {

    LayoutInflater mInflater;

    public SlideListViewAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 100;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
            convertView = mInflater.inflate(R.layout.listview_item, parent, false);
            holder = new ViewHolder();
            holder.slidelayout = (SlideLayout) convertView.findViewById(R.id.sl_slideBindingId);
            holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            holder.iv_edit = (ImageView) convertView.findViewById(R.id.iv_edit);
            holder.iv_delete_1 = (ImageView) convertView.findViewById(R.id.iv_delete_1);
            holder.iv_delete_2 = (ImageView) convertView.findViewById(R.id.iv_delete_2);
            holder.iv_edit.setOnClickListener(this);
            holder.iv_delete_1.setOnClickListener(this);
            holder.iv_delete_2.setOnClickListener(this);
            holder.iv_edit.setTag(holder);
            holder.iv_delete_1.setTag(holder);
            holder.iv_delete_2.setTag(holder);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_title.setText(getSimpleText(position));
        return convertView;
    }

    protected String getSimpleText(int position) {
        return "SlideListView " + position;
    }

    @Override
    public void onClick(View v) {
        ViewHolder holder = (ViewHolder) v.getTag();
        holder.slidelayout.closeSlide(true);
    }
}
