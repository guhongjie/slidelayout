package guhj.github.simple;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import guhj.github.slidelayout.SlideBindingListView;

public class MainActivity extends AppCompatActivity {
    ListView listView1, listView2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView1 = (ListView) findViewById(R.id.listView1);
        listView2 = (ListView) findViewById(R.id.listView2);
        listView1.setAdapter(new SlideListViewAdapter(this));
        listView2.setAdapter(new SlideBindingListViewAdapter(this, (SlideBindingListView) listView2));
    }
}
