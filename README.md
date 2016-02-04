# slidelayout
安卓侧滑布局

效果图：![slidelayout](https://raw.githubusercontent.com/guhongjie/slidelayout/master/img/image.jpg)

使用方法：

直接在xml中使用：
`
            <guhj.github.slidelayout.SlideLayout 
                android:id="@id/sl_slideBindingId"
                android:layout_width="match_parent"
                android:layout_height="match_parent">
                <RelativeLayout
                    android:id="@id/sl_contentLayout"
                    android:layout_width="match_parent"
                    android:layout_height="90dp"
                    android:background="@android:color/white">
                    <TextView
                        android:id="@+id/tv_title"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:textSize="18sp" />
                </RelativeLayout>
                <View
                    android:id="@id/sl_leftLayout"
                    android:layout_width="40dp"
                    android:layout_height="match_parent"/>
                <View
                    android:id="@id/sl_rightLayout"
                    android:layout_width="80dp"
                    android:layout_height="match_parent"/>
            </guhj.github.slidelayout.SlideLayout>
`
