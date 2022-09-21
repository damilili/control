package com.hoody.commonbase.customview.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.hoody.commonbase.customview.pulltorefresh.internal.EmptyViewMethodAccessor;


public class PullToRefreshListView extends PullToRefreshAdapterViewBase<ListView> {

    private OnTouchListener onInterceptTouchListener;

	 class InternalListView extends ListView implements EmptyViewMethodAccessor {

		public InternalListView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void setEmptyView(View emptyView) {
			PullToRefreshListView.this.setEmptyView(emptyView);
		}
		
		@Override
		public void setEmptyViewInternal(View emptyView) {
			super.setEmptyView(emptyView);
		}

		public ContextMenuInfo getContextMenuInfo() {
			return super.getContextMenuInfo();
		}

         @Override
         public boolean onTouchEvent(MotionEvent ev) {
             if(onInterceptTouchListener!=null)
             {
                 if(onInterceptTouchListener.onTouch(this,ev))
                 {
                     return true;
                 }
             }
             return super.onTouchEvent(ev);
         }
     }

	public PullToRefreshListView(Context context) {
		super(context);
		this.setDisableScrollingWhileRefreshing(false);
	}
	
	public PullToRefreshListView(Context context, int mode) {
		super(context, mode);
		this.setDisableScrollingWhileRefreshing(false);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setDisableScrollingWhileRefreshing(false);
	}

	@Override
	public ContextMenuInfo getContextMenuInfo() {
		return ((InternalListView) getRefreshableView()).getContextMenuInfo();
	}

	@Override
	protected final ListView createRefreshableView(Context context, AttributeSet attrs) {
		ListView lv = new InternalListView(context, attrs);

		// Set it to this so it can be used in ListActivity/ListFragment
		lv.setId(android.R.id.list);
		return lv;
	}
	
	public void setAdapter(ListAdapter adapter){
		getRefreshableView().setAdapter(adapter);
	}

	public final void resetTopAutoRefreshing(){
		if(getRefreshableView()!=null){
			if (!getRefreshableView().isStackFromBottom()) {
				getRefreshableView().setStackFromBottom(true);
			}
			getRefreshableView().setStackFromBottom(false);
		}
		currentMode = MODE_PULL_DOWN_TO_REFRESH;
		setRefreshing(true);
	}

    public void setOnInterceptTouchListener(OnTouchListener onInterceptTouchListener) {
        this.onInterceptTouchListener = onInterceptTouchListener;
    }
}
