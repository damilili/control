package com.hoody.commonbase.customview.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;

import com.hoody.commonbase.R;


public class PullToRefreshScrollView extends PullToRefreshBase<ScrollView> {

    private final OnRefreshListener defaultOnRefreshListener = new OnRefreshListener() {

        @Override
        public void onRefresh(int curMode) {
            onRefreshComplete();
        }

    };


    public PullToRefreshScrollView(Context context) {
        super(context);

        /**
         * Added so that by default, Pull-to-Refresh refreshes the page
         */
        setOnRefreshListener(defaultOnRefreshListener);
    }

    public PullToRefreshScrollView(Context context, int mode) {
        super(context, mode);

        /**
         * Added so that by default, Pull-to-Refresh refreshes the page
         */
        setOnRefreshListener(defaultOnRefreshListener);
    }

    public PullToRefreshScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        /**
         * Added so that by default, Pull-to-Refresh refreshes the page
         */
        setOnRefreshListener(defaultOnRefreshListener);
    }

    @Override
    protected ScrollView createRefreshableView(Context context, AttributeSet attrs) {
        ScrollView scrollView = new ScrollView(context, attrs);
        scrollView.setId(R.id.scrollview);
        return scrollView;
    }

    @Override
    protected boolean isReadyForPullDown() {
        Log.d("text", "mScrollView.getHeight()=" + this.getHeight());
        return refreshableView.getScrollY() == 0;
    }

    @Override
    protected boolean isReadyForPullUp() {
        ScrollView view = getRefreshableView();
        int off = view.getScrollY() + view.getHeight() - view.getChildAt(0).getHeight();
        Log.d("text", "view.getScrollY()=" + view.getScrollY() + ",view.getChildAt(0).getHeight()" + view.getChildAt(0).getHeight());
        if (off == 0) {
            return true;
        } else {
            return false;
        }
        /* if (view.getScrollY() + view.getHeight() >=  view.getMeasuredHeight()) {  
             //?????????????????????????????  
        	 return true;
         } else{
	    	return false;
	    }  */
        //return (refreshableView.getScrollY() >= refreshableView.getHeight());
    }

}
