package com.hoody.commonbase.customview.slidedecidable;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.ScrollView;

import com.hoody.commonbase.R;
import com.hoody.commonbase.customview.pulltorefresh.PullToRefreshBase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;


/**
 * Created by cdm on 2020/6/1.
 * 滑动可被控制的控件基类
 */
public abstract class SlideDecidableLayout extends FrameLayout {
    private static final String TAG = SlideDecidableLayout.class.getSimpleName();
    protected SlidableDecider mSlidableDecider;

    public SlideDecidableLayout(@NonNull Context context) {
        super(context);
        initBaseDecider();
    }

    public SlideDecidableLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initBaseDecider();
    }

    public SlideDecidableLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBaseDecider();
    }

    private void initBaseDecider() {
        //配置滑动决策器
        mSlidableDecider = new SlidableDecider(this) {
            @Override
            public boolean slidable(PointF curEvPos, SlideOrientation slideOrientation) {
                boolean slideable = true;
                Set<WeakReference<SlidableDecider>> needRemoves = new HashSet<>();
                Log.d(TAG, "mSlidableDeciders.size()= " + mSlidableDeciders.size());
                for (WeakReference<SlidableDecider> weakReference : mSlidableDeciders) {
                    SlidableDecider slidableDecider = weakReference.get();
                    if (slidableDecider != null) {
                        if (!slidableDecider.slidable(curEvPos, slideOrientation)) {
                            return false;
                        }
                    } else {
                        needRemoves.add(weakReference);
                    }
                }
                Log.d(TAG, " needRemoves.size() = " + needRemoves.size());
                mSlidableDeciders.removeAll(needRemoves);
                Log.d(TAG, "slideable = " + slideable);
                return slideable;
            }
        };
    }

    private LinkedList<WeakReference<SlidableDecider>> mSlidableDeciders = new LinkedList<WeakReference<SlidableDecider>>();

    /**
     * @param slidableDecider 添加滑动决策器，一个SlidableDecider对象只能被添加进去一次，多次添加无效，
     *                        SlidableDecider 和其对应的view具有一对一的关系，当一个view对应多个SlidableDecider实例时，
     *                        这个view对应的其他SlidableDecider实例会被后添加的SlidableDecider实例代替
     */
    public void addDecider(SlidableDecider slidableDecider) {
        Log.d(getClass().getSimpleName(), "addDecider() called with: slidableDecider = [" + slidableDecider + "]" + mSlidableDeciders.size());
        if (slidableDecider != null && slidableDecider.mTargetView != null) {
            Iterator<WeakReference<SlidableDecider>> iterator = mSlidableDeciders.iterator();
            while (iterator.hasNext()) {
                WeakReference<SlidableDecider> next = iterator.next();
                if (next == null
                        || next.get() == null
                        || next.get() == slidableDecider
                        || (slidableDecider.mTargetView != null && slidableDecider.mTargetView.equals(next.get().mTargetView))) {
                    iterator.remove();
                }
            }
            Log.d(getClass().getSimpleName(), "addDecider() called with: mSlidableDeciders.size() =" + mSlidableDeciders.size());
            WeakReference<SlidableDecider> weakReference = new WeakReference<SlidableDecider>(slidableDecider);
            mSlidableDeciders.add(weakReference);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mSlidableDeciders.clear();
    }

    /**
     * 滑动方向枚举
     */
    public enum SlideOrientation {
        SLIDE_DOWN,
        SLIDE_UP,
        SLIDE_LEFT,
        SLIDE_RIGHT
    }

    /**
     * 决策器——用于决定此控件是否可以滑动
     */
    public abstract static class SlidableDecider {
        public boolean mDecided = false;
        protected View mTargetView;

        public SlidableDecider(View targetView) {
            //！！！！！强调 ！！！！！！！！
            // 这里要求targetView不能是null,否则SlidableDecider会被回收，从而起不到作用!!
            // 只有当targetView不被除SlidableDecider对象之外的其他对象引用的时候，SlidableDecider对象才应该被回收
            // 这里让targetView和SlidableDecider对象之间互相建立强引用关系，以保证SlidableDecider对象不会被意外回收，
            if (targetView != null) {
                mTargetView = targetView;
                mTargetView.setTag(R.id.kwqt_switchlayout_decider, this);
            } else {
                throw new NullPointerException("targetView不能是null,否则SlidableDecider会被回收，从而起不到作用!!");
            }
        }

        /**
         * 决定此控件是否可以滑动
         *
         * @param curEvPos         触摸事件位置
         * @param slideOrientation 滑动方向
         * @return true:此控件可以滑动 false:此控件不可以滑动 !!!!默认一定返回true
         */
        public abstract boolean slidable(PointF curEvPos, SlideOrientation slideOrientation);

        protected boolean isTouchPointInView(View view, float x, float y) {
            if (view == null || !view.isShown()) {
                return false;
            }
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int left = location[0];
            int top = location[1];
            int right = left + view.getMeasuredWidth();
            int bottom = top + view.getMeasuredHeight();
            if (y >= top && y <= bottom && x >= left
                    && x <= right) {
                return true;
            }
            return false;
        }
    }

    public static class DeciderProduceUtil {

        public static SlidableDecider getViewSlidableDecider(final WebView webView) {
            if (webView == null) {
                return null;
            }
            return new SlidableDecider(webView) {
                @Override
                public boolean slidable(PointF curEvPos, SlideOrientation slideOrientation) {
                    boolean isHorizentalSlide = slideOrientation == SlideOrientation.SLIDE_LEFT || slideOrientation == SlideOrientation.SLIDE_RIGHT;
                    boolean slideDown = slideOrientation == SlideOrientation.SLIDE_DOWN;
                    boolean touchPointInView = isTouchPointInView(webView, curEvPos.x, curEvPos.y);
                    Log.d(TAG, "webView.getScrollY()=" + webView.getScrollY());
                    Log.d(TAG, "webView.getContentHeight()=" + webView.getContentHeight());
                    if (touchPointInView) {
                        if (!isHorizentalSlide) {
                            if (slideDown) {
                                if (webView.canScrollVertically(-1)) {
                                    return false;
                                }
                            } else {
                                if (webView.canScrollVertically(1)) {
                                    return false;
                                }
                            }
                        }
                    }
                    return true;
                }
            };
        }

        public static SlidableDecider getViewSlidableDecider(final ListView listView) {
            if (listView == null) {
                return null;
            }
            return new SlidableDecider(listView) {
                @Override
                public boolean slidable(PointF curEvPos, SlideOrientation slideOrientation) {
                    boolean isHorizentalSlide = slideOrientation == SlideOrientation.SLIDE_LEFT || slideOrientation == SlideOrientation.SLIDE_RIGHT;
                    boolean slideDown = slideOrientation == SlideOrientation.SLIDE_DOWN;
                    if (listView != null && !isHorizentalSlide) {
                        boolean touchPointInView = isTouchPointInView(listView, curEvPos.x, curEvPos.y);
                        Log.d(TAG, "slidable() called with:touchPointInView touchPointInView = " + touchPointInView);
                        if (touchPointInView) {
                            if (!slideDown) {
                                return isLastItemVisible();
                            } else {
                                boolean firstItemVisible = isFirstItemVisible();
                                Log.d(TAG, "slidable() called with: curEvPos = " + firstItemVisible);
                                return firstItemVisible;
                            }
                        }
                    }
                    return true;
                }

                private boolean isFirstItemVisible() {
                    if (listView.getCount() == 0) {
                        Log.d(TAG, "isFirstItemVisible() called3ddd");
                        return true;
                    } else if (listView.getFirstVisiblePosition() == 0) {
                        View firstVisibleChild = listView.getChildAt(0);
                        Log.d(TAG, "isFirstItemVisible() called" + firstVisibleChild);
                        if (firstVisibleChild != null) {
                            Log.d(TAG, "isFirstItemVisible() firstVisibleChild.getTop()=" + firstVisibleChild.getTop() + " listView.getTop()" + listView.getTop());
                            return firstVisibleChild.getTop() >= 0;
                        }
                    }
                    return false;
                }

                private boolean isLastItemVisible() {
                    int count = listView.getCount();
                    int lastVisiblePosition = listView.getLastVisiblePosition();
                    if (count == 0) {
                        return true;
                    } else if (lastVisiblePosition == count - 1) {
                        int childIndex = lastVisiblePosition - listView.getFirstVisiblePosition();
                        View lastVisibleChild = listView.getChildAt(childIndex);
                        if (lastVisibleChild != null) {
                            Log.d(TAG, "isFirstItemVisible() lastVisibleChild.getBottom() =" + lastVisibleChild.getBottom() + " listView.getBottom()" + listView.getHeight());
                            return lastVisibleChild.getBottom() <= listView.getHeight();
                        }
                    }
                    return false;
                }
            };
        }

        public static SlidableDecider getViewSlidableDecider(final PullToRefreshBase refreshView) {
            if (refreshView == null) {
                return null;
            }
            return new SlidableDecider(refreshView) {
                @Override
                public boolean slidable(PointF curEvPos, SlideOrientation slideOrientation) {
                    boolean isHorizentalSlide = slideOrientation == SlideOrientation.SLIDE_LEFT || slideOrientation == SlideOrientation.SLIDE_RIGHT;
                    if (refreshView != null && !isHorizentalSlide) {
                        boolean touchPointInView = isTouchPointInView(refreshView, curEvPos.x, curEvPos.y);
                        Log.d(TAG, "slidable() called with:touchPointInView touchPointInView = " + touchPointInView);
                        if (touchPointInView) {
                            View refreshableView = refreshView.getRefreshableView();
                            SlidableDecider listviewSlidableDecider = null;
                            if (refreshableView instanceof RecyclerView) {
                                listviewSlidableDecider = getViewSlidableDecider((RecyclerView) refreshableView);
                            } else if (refreshableView instanceof ListView) {
                                listviewSlidableDecider = getViewSlidableDecider((ListView) refreshableView);
                            }
                            if (listviewSlidableDecider == null) {
                                return true;
                            }
                            if (!listviewSlidableDecider.slidable(curEvPos, slideOrientation)) {
                                return false;
                            } else {
                                int mode = refreshView.getMode();
                                switch (mode) {
                                    case PullToRefreshBase.MODE_BOTH:
                                        return false;
                                    case PullToRefreshBase.MODE_DISABLED:
                                        return true;
                                    case PullToRefreshBase.MODE_PULL_DOWN_TO_REFRESH:
                                        return slideOrientation != SlideOrientation.SLIDE_DOWN;
                                    case PullToRefreshBase.MODE_PULL_UP_TO_REFRESH:
                                        return slideOrientation != SlideOrientation.SLIDE_UP;
                                }
                            }
                        }
                    }
                    return true;
                }
            };
        }

        public static SlidableDecider getViewSlidableDecider(final ScrollView scrollView) {
            if (scrollView == null) {
                return null;
            }
            return new SlidableDecider(scrollView) {
                @Override
                public boolean slidable(PointF curEvPos, SlideOrientation slideOrientation) {
                    boolean isHorizentalSlide = slideOrientation == SlideOrientation.SLIDE_LEFT || slideOrientation == SlideOrientation.SLIDE_RIGHT;
                    boolean touchPointInViewPager = isTouchPointInView(scrollView, curEvPos.x, curEvPos.y);
                    Log.d(TAG, "viewPager slidable() called with: touchPointInViewPager = [" + touchPointInViewPager + "], slideOrientation = [" + slideOrientation + "]");
                    if (touchPointInViewPager && !isHorizentalSlide) {
                        if (scrollView.canScrollVertically(1) && slideOrientation == SlideOrientation.SLIDE_UP) {
                            return false;
                        }
                        if (scrollView.canScrollVertically(-1) && slideOrientation == SlideOrientation.SLIDE_DOWN) {
                            return false;
                        }
                    }
                    return true;
                }
            };
        }

        public static SlidableDecider getViewSlidableDecider(final HorizontalScrollView scrollView) {
            if (scrollView == null) {
                return null;
            }
            return new SlidableDecider(scrollView) {
                @Override
                public boolean slidable(PointF curEvPos, SlideOrientation slideOrientation) {
                    boolean isHorizentalSlide = slideOrientation == SlideOrientation.SLIDE_LEFT || slideOrientation == SlideOrientation.SLIDE_RIGHT;
                    boolean touchPointInViewPager = isTouchPointInView(scrollView, curEvPos.x, curEvPos.y);
                    Log.d(TAG, "viewPager slidable() called with: touchPointInViewPager = [" + touchPointInViewPager + "], slideOrientation = [" + slideOrientation + "]");
                    if (touchPointInViewPager && isHorizentalSlide) {
                        if (scrollView.canScrollHorizontally(1) && slideOrientation == SlideOrientation.SLIDE_LEFT) {
                            return false;
                        }
                        if (scrollView.canScrollHorizontally(-1) && slideOrientation == SlideOrientation.SLIDE_RIGHT) {
                            return false;
                        }
                    }
                    return true;
                }
            };
        }

        public static SlidableDecider getViewSlidableDecider(final RecyclerView recyclerView) {
            if (recyclerView == null) {
                return null;
            }
            return new SlidableDecider(recyclerView) {
                @Override
                public boolean slidable(PointF curEvPos, SlideOrientation slideOrientation) {
                    boolean isHorizentalSlide = slideOrientation == SlideOrientation.SLIDE_LEFT || slideOrientation == SlideOrientation.SLIDE_RIGHT;
                    boolean slideDown = slideOrientation == SlideOrientation.SLIDE_DOWN;
                    boolean slideLeft = slideOrientation == SlideOrientation.SLIDE_LEFT;
                    if (mTargetView != null) {
                        boolean touchPointInView = isTouchPointInView(mTargetView, curEvPos.x, curEvPos.y);
                        Log.d(TAG, "slidable() called with:touchPointInView touchPointInView = " + touchPointInView);
                        if (touchPointInView) {
                            RecyclerView.LayoutManager layoutManager = ((RecyclerView) mTargetView).getLayoutManager();
                            if (layoutManager != null && layoutManager instanceof LinearLayoutManager) {
                                switch (((LinearLayoutManager) layoutManager).getOrientation()) {
                                    case LinearLayoutManager.HORIZONTAL:
                                        if (isHorizentalSlide) {
                                            if (slideLeft) {
                                                return isLastItemVisible();
                                            } else {
                                                return isFirstItemVisible();
                                            }
                                        }
                                        break;
                                    case LinearLayoutManager.VERTICAL:
                                        if (!isHorizentalSlide) {
                                            if (slideDown) {
                                                return isFirstItemVisible();
                                            } else {
                                                return isLastItemVisible();
                                            }
                                        }
                                        break;
                                }
                            }
                            if (!slideDown) {
                                return isLastItemVisible();
                            } else {
                                boolean firstItemVisible = isFirstItemVisible();
                                Log.d(TAG, "slidable() called with: curEvPos = " + firstItemVisible);
                                return firstItemVisible;
                            }
                        }
                    }
                    return true;
                }

                public boolean isFirstItemVisible() {
                    final RecyclerView.Adapter<?> adapter = ((RecyclerView) mTargetView).getAdapter();
                    if (null == adapter || adapter.getItemCount() == 0) {
                        return true;
                    } else {
                        if (getFirstVisiblePosition() == 0) {
                            final View firstVisibleChild = ((RecyclerView) mTargetView).getChildAt(0);
                            if (firstVisibleChild != null) {
                                RecyclerView.LayoutManager layoutManager = ((RecyclerView) mTargetView).getLayoutManager();
                                if (layoutManager != null && layoutManager instanceof LinearLayoutManager) {
                                    switch (((LinearLayoutManager) layoutManager).getOrientation()) {
                                        case LinearLayoutManager.HORIZONTAL:
                                            return firstVisibleChild.getLeft() >= 0;
                                        case LinearLayoutManager.VERTICAL:
                                            return firstVisibleChild.getTop() >= 0;
                                    }

                                }
                                return firstVisibleChild.getTop() >= ((RecyclerView) mTargetView).getTop();
                            }
                        }
                    }
                    return false;
                }

                public boolean isLastItemVisible() {
                    final RecyclerView.Adapter<?> adapter = ((RecyclerView) mTargetView).getAdapter();
                    if (null == adapter || adapter.getItemCount() == 0) {
                        return true;
                    } else {
                        int lastVisiblePosition = getLastVisiblePosition();
                        if (lastVisiblePosition >= ((RecyclerView) mTargetView).getAdapter().getItemCount() - 1) {
                            View lastChild = ((RecyclerView) mTargetView).getChildAt(((RecyclerView) mTargetView).getChildCount() - 1);
                            RecyclerView.LayoutManager layoutManager = ((RecyclerView) mTargetView).getLayoutManager();
                            if (layoutManager != null && layoutManager instanceof LinearLayoutManager) {
                                switch (((LinearLayoutManager) layoutManager).getOrientation()) {
                                    case LinearLayoutManager.HORIZONTAL:
                                        return lastChild.getRight() <= ((RecyclerView) mTargetView).getWidth();
                                    case LinearLayoutManager.VERTICAL:
                                        return lastChild.getBottom() <= ((RecyclerView) mTargetView).getHeight();
                                }

                            }

                            return lastChild.getBottom() <= ((RecyclerView) mTargetView)
                                    .getBottom();
                        }
                    }
                    return false;
                }

                /**
                 * @Description: 获取第一个可见子View的位置下标
                 */
                private int getFirstVisiblePosition() {
                    View firstVisibleChild = ((RecyclerView) mTargetView).getChildAt(0);
                    return firstVisibleChild != null ? ((RecyclerView) mTargetView).getChildAdapterPosition(firstVisibleChild) : -1;
                }

                /**
                 * @Description: 获取最后一个可见子View的位置下标
                 */
                private int getLastVisiblePosition() {
                    View lastVisibleChild = ((RecyclerView) mTargetView).getChildAt(((RecyclerView) mTargetView).getChildCount() - 1);
                    return lastVisibleChild != null ? ((RecyclerView) mTargetView).getChildAdapterPosition(lastVisibleChild) : -1;
                }
            };
        }

        public static SlidableDecider getViewSlidableDecider(final ViewPager viewPager) {
            if (viewPager == null) {
                return null;
            }
            return new SlidableDecider(viewPager) {
                @Override
                public boolean slidable(PointF curEvPos, SlideOrientation slideOrientation) {
                    boolean isHorizentalSlide = slideOrientation == SlideOrientation.SLIDE_LEFT || slideOrientation == SlideOrientation.SLIDE_RIGHT;
                    boolean slideDown = slideOrientation == SlideOrientation.SLIDE_DOWN;
                    boolean slideLeft = slideOrientation == SlideOrientation.SLIDE_LEFT;
                    boolean touchPointInViewPager = isTouchPointInView(viewPager, curEvPos.x, curEvPos.y);
                    Log.d(TAG, "viewPager slidable() called with: touchPointInViewPager = [" + touchPointInViewPager + "], isHorizentalSlide = [" + isHorizentalSlide + "], slideDown = [" + slideDown + "], slideLeft = [" + slideLeft + "]");
                    if (touchPointInViewPager && isHorizentalSlide) {
                        PagerAdapter adapter = viewPager.getAdapter();
                        if (adapter == null) {
                            return true;
                        }
                        if (viewPager.getCurrentItem() == adapter.getCount() - 1 && slideLeft) {
                            return true;
                        }
                        if (viewPager.getCurrentItem() == 0 && !slideLeft) {
                            return true;
                        }
                        return false;
                    }
                    return true;
                }
            };
        }

    }
}
