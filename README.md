# GifView
a gifView extends View

#使用方法
##1.在Layout中引用一个布局
<>com.example.view.GifView<br>
        android:id="@+id/gv_load"<br>
        android:layout_width="wrap_content"<br>
        android:layout_height="wrap_content" />
        
##2.在代码中设置gif源<br>
GifView gvLoad = (GifView) findViewById(R.id.gv_load);<br>
gvLoad.setMovieResource(R.raw.app_loading);
