package MuYuan.RecyclerViewAdapter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Auther: cheny
 * CreateDate 6/25/2017.
 */

public class mItemDecoration extends RecyclerView.ItemDecoration {
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.top = 10;
        outRect.bottom = 10;
        outRect.left = 10;
        outRect.right = 10;
    }
}
