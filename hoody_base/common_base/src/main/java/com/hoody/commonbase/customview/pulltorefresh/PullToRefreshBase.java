package com.hoody.commonbase.customview.pulltorefresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import com.hoody.commonbase.R;
import com.hoody.commonbase.customview.pulltorefresh.internal.AbstractLoadingLayout;
import com.hoody.commonbase.customview.pulltorefresh.internal.LoadingLayout;
import com.hoody.commonbase.customview.pulltorefresh.internal.RoundLoadingLayout;


/**
 * @see com.hoody.commonbase.customview.pulltorefresh.internal.AbstractLoadingLayout 的周期方法调用分析
 * @see #state   {@link #PULL_TO_REFRESH }  {@link #RELEASE_TO_REFRESH}    {@link #REFRESHING } {@link #MANUAL_REFRESHING }
 * ->startPull()               ->releaseToRefresh()              ->refreshing()
 *                                  pullToRefresh()<-
 * reset  <<<<<<----------------------------------------------------------------------
 */
public abstract class PullToRefreshBase<T extends View> extends RelativeLayout {

    private ValueAnimator resetAnimator;
    //单次拖动时有没有调用过startPull方法
    private boolean startPullCalled = false;
    //随下拉高度而改变高度的背景view
    private View mHeadBg;
    private boolean mHeadBgVisiable = true;

    static final int ANIMATION_DURATION_MS = 190;
    /**
     * 回弹时间
     */
    private int animationDurationMs = ANIMATION_DURATION_MS;
    /**
     * 动态设置回弹时间
     * **/
    public void setAnimationDurationMs(int animationDurationMs) {
        this.animationDurationMs = animationDurationMs > 0 ? animationDurationMs : ANIMATION_DURATION_MS;
    }

    final class SmoothScrollRunnable implements Runnable {

        static final int ANIMATION_FPS = 1000 / 200;

        private final Interpolator interpolator;
        private final int scrollToY;
        private final int scrollFromY;
        private final Handler handler;

        private boolean continueRunning = true;
        private long startTime = -1;
        private int currentY = -1;

        public SmoothScrollRunnable(Handler handler, int fromY, int toY) {
            this.handler = handler;
            this.scrollFromY = fromY;
            this.scrollToY = toY;
            this.interpolator = new AccelerateDecelerateInterpolator();
        }

        @Override
        public void run() {

            /**
             * Only set startTime if this is the first time we're starting, else
             * actually calculate the Y delta
             */
            if (startTime == -1) {
                startTime = System.currentTimeMillis();
            } else {

                /**
                 * We do do all calculations in long to reduce software float
                 * calculations. We use 1000 as it gives us good accuracy and
                 * small rounding errors
                 */
                long normalizedTime = (1000 * (System.currentTimeMillis() - startTime)) / animationDurationMs;
                normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

                final int deltaY = Math.round((scrollFromY - scrollToY)
                        * interpolator.getInterpolation(normalizedTime / 1000f));
                this.currentY = scrollFromY - deltaY;
                setHeaderScroll(currentY, false);
            }

            // If we're not at the target Y, keep going...
            if (continueRunning && scrollToY != currentY) {
                handler.postDelayed(this, ANIMATION_FPS);
            }
        }

        public void stop() {
            this.continueRunning = false;
            this.handler.removeCallbacks(this);
        }
    }

    ;

    // ===========================================================
    // Constants
    // ===========================================================

    static final float FRICTION = 2.0f;

    static final int PULL_TO_REFRESH = 0x0;
    static final int RELEASE_TO_REFRESH = 0x1;
    static final int REFRESHING = 0x2;
    static final int MANUAL_REFRESHING = 0x3;

    public static final int MODE_DISABLED = 0x0;
    public static final int MODE_PULL_DOWN_TO_REFRESH = 0x1;
    public static final int MODE_PULL_UP_TO_REFRESH = 0x2;
    public static final int MODE_BOTH = 0x3;
    /**
     * 上下移动的类型
     * 拖动时 headrview和footview 相对refreshableview不动的情况称之为拖动，而headrview和footview 相对refreshableview运动的情况称之为浮动
     *
     * @see #TYPE_HEADERDRAG_FOOTERDRAG 类型一：headerlayout拖动,footerLayout拖动
     * @see #TYPE_HEADERDRAG_FOOTERFLOAT 类型二：headerlayout拖动,footerLayout浮动
     * @see #TYPE_HEADERFLOAT_FOOTERDRAG 类型三：headerlayout,浮动，而footerLayout拖动
     * @see #TYPE_HEADERFLOAT_FOOTERFLOAT 类型四：headerlayout,浮动，而footerLayout浮动
     */
    public static final int TYPE_HEADERDRAG_FOOTERDRAG = 0x0;
    public static final int TYPE_HEADERDRAG_FOOTERFLOAT = 0x1;
    public static final int TYPE_HEADERFLOAT_FOOTERDRAG = 0x2;
    public static final int TYPE_HEADERFLOAT_FOOTERFLOAT = 0x3;
    // ===========================================================
    // Fields
    // ===========================================================


    private int overScrollType = TYPE_HEADERDRAG_FOOTERDRAG;
    private int touchSlop;

    private float initialMotionY;
    private float lastMotionX;
    private float lastMotionY;
    private boolean isBeingDragged = false;

    private int state = PULL_TO_REFRESH;
    private int mode = MODE_PULL_DOWN_TO_REFRESH;
    protected int currentMode;

    private boolean disableScrollingWhileRefreshing = true;

    T refreshableView;
    private boolean isPullToRefreshEnabled = true;

    private AbstractLoadingLayout headerLayout;
    private AbstractLoadingLayout footerLayout;
    private int headerHeight;
    private int footerHeight;
    /**
     * 下拉和上拉状态切换阈值，默认为对应header或footer的高度
     * 在{@link #PULL_TO_REFRESH}和{@link #RELEASE_TO_REFRESH}之间切换的临界值
     * 并且在松手之后，view将滑动到这个临界值所指定的位置
     * mode或type改变的时候这个值默认变为对应header或footer的高度，原先的设置失效
     */
    private int pullDownRefreshLimitHeight;
    private int pullUpRefreshLimitHeight;

    private final Handler handler = new Handler();

    private OnRefreshListener onRefreshListener;

    private SmoothScrollRunnable currentSmoothScrollRunnable;

    private String pullDownLabel = "下拉刷新";//context.getString(R.string.pull_to_refresh_pull_label);
    private String pullUpLabel = "上拉可以加载更多";
    private String loadingLabel = "正在加载更多的数据...";//context.getString(R.string.pull_to_refresh_refreshing_label);
    private String releaseLabel = "松开立即加载更多";//context.getString(R.string.pull_to_refresh_release_label);
    private OnTouchListener onTouchListener;
    // ===========================================================
    // Constructors
    // ===========================================================

    public PullToRefreshBase(Context context) {
        super(context);
        init(context, null);
    }

    public PullToRefreshBase(Context context, int mode) {
        super(context);
        this.mode = mode;
        init(context, null);
    }

    public PullToRefreshBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    /**
     * Deprecated. Use {@link #getRefreshableView()} from now on.
     *
     * @return The Refreshable View which is currently wrapped
     * @deprecated
     */
    public final T getAdapterView() {
        return refreshableView;
    }

    /**
     * Get the Wrapped Refreshable View. Anything returned here has already been
     * added to the content view.
     *
     * @return The View which is currently wrapped
     */
    public final T getRefreshableView() {
        return refreshableView;
    }

    /**
     * Whether Pull-to-Refresh is enabled
     *
     * @return enabled
     */
    public final boolean isPullToRefreshEnabled() {
        return isPullToRefreshEnabled;
    }

    /**
     * Returns whether the widget has disabled scrolling on the Refreshable View
     * while refreshing.
     * <p>
     * if the widget has disabled scrolling while refreshing
     */
    public final boolean isDisableScrollingWhileRefreshing() {
        return disableScrollingWhileRefreshing;
    }

    /**
     * Returns whether the Widget is currently in the Refreshing state
     *
     * @return true if the Widget is currently refreshing
     */
    public final boolean isRefreshing() {
        return state == REFRESHING || state == MANUAL_REFRESHING;
    }

    /**
     * By default the Widget disabled scrolling on the Refreshable View while
     * refreshing. This method can change this behaviour.
     *
     * @param disableScrollingWhileRefreshing - true if you want to disable scrolling while refreshing
     */
    public final void setDisableScrollingWhileRefreshing(boolean disableScrollingWhileRefreshing) {
        this.disableScrollingWhileRefreshing = disableScrollingWhileRefreshing;
    }

    /**
     * Mark the current Refresh as complete. Will Reset the UI and hide the
     * Refreshing View
     */
    public final void onRefreshComplete() {
        if (state != PULL_TO_REFRESH) {
            resetHeader();
        }
    }

    /**
     * Set OnRefreshListener for the Widget
     *
     * @param listener - Listener to be used when the Widget is set to Refresh
     */
    public final void setOnRefreshListener(OnRefreshListener listener) {
        onRefreshListener = listener;
    }

    /**
     * A mutator to enable/disable Pull-to-Refresh for the current View
     *
     * @param enable Whether Pull-To-Refresh should be used
     */
    public final void setPullToRefreshEnabled(boolean enable) {
        this.isPullToRefreshEnabled = enable;
    }

    /**
     * Set Text to show when the Widget is being pulled, and will refresh when
     * released
     *
     * @param releaseLabel - String to display
     */
    public void setReleaseLabel(String releaseLabel) {
        if (null != headerLayout) {
            headerLayout.setReleaseLabel(releaseLabel);
        }
        if (null != footerLayout) {
            footerLayout.setReleaseLabel(releaseLabel);
        }
    }

    //设置下拉刷新文本是否显示
    public void setLabelTextVisibility(int visibility) {
        if (null != headerLayout) {
            headerLayout.setTextVisibility(visibility);
        }
        if (null != footerLayout) {
            footerLayout.setTextVisibility(visibility);
        }
    }

    /**
     * Set Text to show when the Widget is being Pulled
     *
     * @param pullLabel - String to display
     */
    public void setPullLabel(String pullLabel) {
        if (null != headerLayout) {
            headerLayout.setPullLabel(pullLabel);
        }
        if (null != footerLayout) {
            footerLayout.setPullLabel(pullLabel);
        }
    }

    /**
     * Set Text to show when the Widget is refreshing
     *
     * @param refreshingLabel - String to display
     */
    public void setRefreshingLabel(String refreshingLabel) {
        if (null != headerLayout) {
            headerLayout.setRefreshingLabel(refreshingLabel);
        }
        if (null != footerLayout) {
            footerLayout.setRefreshingLabel(refreshingLabel);
        }
    }

    public final void setRefreshing() {
        this.setRefreshing(true);
    }

    /**
     * Sets the Widget to be in the refresh state. The UI will be updated to
     * show the 'Refreshing' view.
     *
     * @param doScroll - true if you want to force a scroll to the Refreshing view.
     */
    public final void setRefreshing(boolean doScroll) {
        if (!isRefreshing()) {
            setRefreshingInternal(doScroll);
            state = MANUAL_REFRESHING;

            //send onRefresh listener
            if (onRefreshListener != null) {
                onRefreshListener.onRefresh(currentMode);
            }
        }
    }

    public final boolean hasPullFromTop() {
        return currentMode != MODE_PULL_UP_TO_REFRESH;
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    /**
     * @param pullDownRefreshLimitHeight 下拉触发临界值
     */
    public void setPullDownRefreshLimitHeight(int pullDownRefreshLimitHeight) {
        this.pullDownRefreshLimitHeight = pullDownRefreshLimitHeight;
    }

    /**
     * @param pullUpRefreshLimitHeight 上拉拉触发临界值
     */
    public void setPullUpRefreshLimitHeight(int pullUpRefreshLimitHeight) {
        this.pullUpRefreshLimitHeight = pullUpRefreshLimitHeight;
    }
    @Override
    public final boolean onTouchEvent(MotionEvent event) {
        if (!isPullToRefreshEnabled) {
            return false;
        }

        if (isRefreshing() && disableScrollingWhileRefreshing) {
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
            return false;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_MOVE: {
                if (isBeingDragged) {
                    lastMotionY = event.getY();
                    this.pullEvent();
                    return true;
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                if (isReadyForPull()) {
                    lastMotionY = initialMotionY = event.getY();
                    return true;
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                startPullCalled = false;
                if (isBeingDragged) {
                    isBeingDragged = false;

                    if (state == RELEASE_TO_REFRESH && null != onRefreshListener) {
                        setRefreshingInternal(true);
                        onRefreshListener.onRefresh(currentMode);
                    } else {
                        smoothScrollTo(0);
                    }
                    return true;
                }
                break;
            }
        }

        return false;
    }

    @Override
    public final boolean onInterceptTouchEvent(MotionEvent event) {
        if (onTouchListener != null) {
            onTouchListener.onTouch(this, event);
        }
        if (!isPullToRefreshEnabled) {
            return false;
        }

        if (isRefreshing() && disableScrollingWhileRefreshing) {
            return true;
        }

        final int action = event.getAction();

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            isBeingDragged = false;
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN && isBeingDragged) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (isReadyForPull()) {

                    final float y = event.getY();
                    final float dy = y - lastMotionY;
                    final float yDiff = Math.abs(dy);
                    final float xDiff = Math.abs(event.getX() - lastMotionX);

                    if (yDiff > touchSlop && yDiff > xDiff) {
                        if ((mode == MODE_PULL_DOWN_TO_REFRESH || mode == MODE_BOTH) && dy >= 0.0001f
                                && isReadyForPullDown()) {
                            lastMotionY = y;
                            isBeingDragged = true;
                            if (mode == MODE_BOTH) {
                                currentMode = MODE_PULL_DOWN_TO_REFRESH;
                            }
                        } else if ((mode == MODE_PULL_UP_TO_REFRESH || mode == MODE_BOTH) && dy <= 0.0001f
                                && isReadyForPullUp()) {
                            lastMotionY = y;
                            isBeingDragged = true;
                            if (mode == MODE_BOTH) {
                                currentMode = MODE_PULL_UP_TO_REFRESH;
                            }
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_DOWN: {
                if (mHeadBg != null) {
//                    mHeadBg.setBackgroundColor(ThemeManager.getInstance().getColor(R.color.kwjx_theme_color_MOD1));
                }
                if (isReadyForPull()) {
                    lastMotionY = initialMotionY = event.getY();
                    lastMotionX = event.getX();
                    isBeingDragged = false;
                }
                break;
            }
        }

        return isBeingDragged;
    }

    protected void addRefreshableView(Context context, T refreshableView) {
        addView(refreshableView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    /**
     * This is implemented by derived classes to return the created View. If you
     * need to use a custom View (such as a custom ListView), override this
     * method and return an instance of your custom class.
     * <p>
     * Be sure to set the ID of the view in this method, especially if you're
     * using a ListActivity or ListFragment.
     *
     * @param context
     * @param attrs   AttributeSet from wrapped class. Means that anything you
     *                include in the XML layout declaration will be routed to the
     *                created View
     * @return New instance of the Refreshable View
     */
    protected abstract T createRefreshableView(Context context, AttributeSet attrs);

    protected final int getCurrentMode() {
        return currentMode;
    }

    public final AbstractLoadingLayout getFooterLayout() {
        return footerLayout;
    }

    public final AbstractLoadingLayout getHeaderLayout() {
        return headerLayout;
    }

    protected final int getHeaderHeight() {
        return headerHeight;
    }

    protected final int getFooterHeight() {
        return footerHeight;
    }

    public final int getMode() {
        return mode;
    }

    /**
     * Implemented by derived class to return whether the View is in a state
     * where the user can Pull to Refresh by scrolling down.
     *
     * @return true if the View is currently the correct state (for example, top
     * of a ListView)
     */
    protected abstract boolean isReadyForPullDown();

    /**
     * Implemented by derived class to return whether the View is in a state
     * where the user can Pull to Refresh by scrolling up.
     *
     * @return true if the View is currently in the correct state (for example,
     * bottom of a ListView)
     */
    protected abstract boolean isReadyForPullUp();

    // ===========================================================
    // Methods
    // ===========================================================

    protected void resetHeader() {
        state = PULL_TO_REFRESH;
        isBeingDragged = false;

        smoothScrollTo(0);

        if (null != headerLayout) {
            headerLayout.reset();
        }
        if (null != footerLayout) {
            footerLayout.reset();
        }
    }

    protected void setRefreshingInternal(boolean doScroll) {
        state = REFRESHING;

        if (doScroll) {
            smoothScrollTo(currentMode == MODE_PULL_UP_TO_REFRESH ? pullUpRefreshLimitHeight : -pullDownRefreshLimitHeight);
        }

        if (null != headerLayout && currentMode == MODE_PULL_DOWN_TO_REFRESH) {
            headerLayout.refreshing();
        }
        if (null != footerLayout && currentMode == MODE_PULL_UP_TO_REFRESH) {
            footerLayout.refreshing();
        }
    }

    /**
     * @param y
     * @param byUser 是不是用户拖动
     */
    protected final void setHeaderScroll(int y, boolean byUser) {
        switch (overScrollType) {
            case TYPE_HEADERDRAG_FOOTERDRAG:
                scrollTo(0, y);
                if (currentMode == MODE_PULL_UP_TO_REFRESH) {
                    footerLayout.onMove(y, byUser);
                } else {
                    headerLayout.onMove(y, byUser);
                    resizeHeaderBg(y);
                }
                break;
            case TYPE_HEADERDRAG_FOOTERFLOAT:
                if (currentMode == MODE_PULL_UP_TO_REFRESH) {
                    bringChildToFront(footerLayout);
                    if (!footerLayout.onMove(y, byUser)) {
                        footerLayout.setTranslationY(-y);
                    }
                    fastAnimResetDragHeadview();
                } else {
                    if (footerLayout != null) {
                        fastAnimResetFloatHeadFootview(footerLayout);
                    }
                    headerLayout.onMove(y, byUser);
                    resizeHeaderBg(y);
                    scrollTo(0, y);
                }
                break;
            case TYPE_HEADERFLOAT_FOOTERDRAG:
                if (currentMode == MODE_PULL_DOWN_TO_REFRESH) {
                    bringChildToFront(headerLayout);
                    if (!headerLayout.onMove(y, byUser)) {
                        headerLayout.setTranslationY(-y);
                    }
                    fastAnimResetDragHeadview();
                } else {
                    if (headerLayout != null) {
                        fastAnimResetFloatHeadFootview(headerLayout);
                    }
                    footerLayout.onMove(y, byUser);
                    scrollTo(0, y);
                }
                break;
            case TYPE_HEADERFLOAT_FOOTERFLOAT:
                if (currentMode == MODE_PULL_DOWN_TO_REFRESH) {
                    bringChildToFront(headerLayout);
                    if (!headerLayout.onMove(y, byUser)) {
                        headerLayout.setTranslationY(-y);
                    }
                    if (footerLayout != null) {
                        fastAnimResetFloatHeadFootview(footerLayout);
                    }
                } else {
                    bringChildToFront(footerLayout);
                    if (!footerLayout.onMove(y, byUser)) {
                        footerLayout.setTranslationY(-y);
                    }
                    if (headerLayout != null) {
                        fastAnimResetFloatHeadFootview(headerLayout);
                    }
                }
                break;
        }
    }

    /**
     * 短时内用动画的方式恢复混合类型下的headerview
     */
    private void fastAnimResetDragHeadview() {
        if (getScrollY() != 0 && resetAnimator == null) {
            resetAnimator = ValueAnimator.ofInt(getScrollY(), 0);
            resetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int animatedValue = (int) animation.getAnimatedValue();
                    scrollTo(0, animatedValue);
                }
            });
            resetAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    resetAnimator.removeAllUpdateListeners();
                    resetAnimator.removeAllListeners();
                    resetAnimator = null;
                }
            });
            resetAnimator.setDuration(100);
            if (!resetAnimator.isRunning()) {
                resetAnimator.start();
            }
        }
    }

    /**
     * 短时内用动画的方式恢复浮动类型下的headerview
     *
     * @param view 要操作的headerview
     */
    private void fastAnimResetFloatHeadFootview(final View view) {
        if (view.getTranslationY() != 0 && resetAnimator == null) {
            resetAnimator = ValueAnimator.ofFloat(view.getTranslationY(), 0);
            resetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float animatedValue = (float) animation.getAnimatedValue();
                    view.setTranslationY(animatedValue);
                }
            });
            resetAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    resetAnimator.removeAllUpdateListeners();
                    resetAnimator.removeAllListeners();
                    resetAnimator = null;
                }
            });
            //这个时间不能太长
            resetAnimator.setDuration(100);
            if (!resetAnimator.isRunning()) {
                resetAnimator.start();
            }
        }
    }

    /***
     * 回弹时间 默认为190
     * **/
    protected final void smoothScrollTo(int y) {
        if (null != currentSmoothScrollRunnable) {
            currentSmoothScrollRunnable.stop();
        }

        if (this.getScrollY() != y || (headerLayout != null && headerLayout.getTranslationY() != -y) || (footerLayout != null && footerLayout.getTranslationY() != -y)) {
            int temY = 0;
            if (headerLayout != null && headerLayout.getTranslationY() != 0) {
                temY = (int) -headerLayout.getTranslationY();
            } else if (footerLayout != null && footerLayout.getTranslationY() != 0) {
                temY = (int) -footerLayout.getTranslationY();
            } else if (getScrollY() != 0) {
                temY = getScrollY();
            }
            this.currentSmoothScrollRunnable = new SmoothScrollRunnable(handler, temY, y);
            handler.post(currentSmoothScrollRunnable);
        }
    }

    private void init(Context context, AttributeSet attrs) {

        touchSlop = ViewConfiguration.getTouchSlop();

        // Styleables from XML
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KwjxPullToRefresh);
        if (a.hasValue(R.styleable.KwjxPullToRefresh_kwjxPullmode)) {
            mode = a.getInteger(R.styleable.KwjxPullToRefresh_kwjxPullmode, MODE_PULL_DOWN_TO_REFRESH);
        }

        // Refreshable View
        // By passing the attrs, we can add ListView/GridView params via XML
        refreshableView = this.createRefreshableView(context, attrs);
        this.addRefreshableView(context, refreshableView);

        // Loading View Strings


        // Add Loading Views
        if (mode == MODE_PULL_DOWN_TO_REFRESH || mode == MODE_BOTH) {
            headerLayout = initHeaderLayout();
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(ALIGN_PARENT_TOP);
            addView(headerLayout, 0, params);
            if (overScrollType == TYPE_HEADERDRAG_FOOTERDRAG ||
                    overScrollType == TYPE_HEADERDRAG_FOOTERFLOAT) {
                //头部拖动模式下添加头部背景
                addHeaderBg();
            }
            measureView(headerLayout);
            headerHeight = headerLayout.getMeasuredHeight();
            pullDownRefreshLimitHeight = headerHeight;
            params.topMargin = -headerHeight;
        }
        if (mode == MODE_PULL_UP_TO_REFRESH || mode == MODE_BOTH) {
            footerLayout = initFooterLayout();
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(ALIGN_PARENT_BOTTOM);
            addView(footerLayout, params);
            measureView(footerLayout);
            footerHeight = footerLayout.getMeasuredHeight();
            pullUpRefreshLimitHeight = footerHeight;
            params.bottomMargin = -footerHeight;
        }

        // Styleables from XML
        if (a.hasValue(R.styleable.KwjxPullToRefresh_kwjxHeaderTextColor)) {
            final int color = a.getColor(R.styleable.KwjxPullToRefresh_kwjxHeaderTextColor, Color.BLACK);
            if (null != headerLayout) {
                headerLayout.setTextColor(color);
            }
            if (null != footerLayout) {
                footerLayout.setTextColor(color);
            }
        }
        if (a.hasValue(R.styleable.KwjxPullToRefresh_kwjxHeaderBackground)) {
            this.setBackgroundResource(a.getResourceId(R.styleable.KwjxPullToRefresh_kwjxHeaderBackground, Color.WHITE));
        }
        if (a.hasValue(R.styleable.KwjxPullToRefresh_kwjxAdapterViewBackground)) {
            refreshableView.setBackgroundResource(a.getResourceId(R.styleable.KwjxPullToRefresh_kwjxAdapterViewBackground,
                    Color.WHITE));
        }
        a.recycle();

        // If we're not using MODE_BOTH, then just set currentMode to current
        // mode
        if (mode != MODE_BOTH) {
            currentMode = mode;
        }
    }

    /**
     * 为下拉添加随下拉高度而改变高度的背景view
     */
    private void addHeaderBg() {
        mHeadBg = new View(getContext());
        LayoutParams bgViewParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        mHeadBg.setVisibility(mHeadBgVisiable ? VISIBLE : GONE);
        addView(mHeadBg, 0, bgViewParams);
    }

    /**
     *随下拉高度而改变背景view的高度
     */
    private void resizeHeaderBg(int y) {
        if (mHeadBg != null) {
            LayoutParams layoutParams = (LayoutParams) mHeadBg.getLayoutParams();
            layoutParams.height = -y;
            mHeadBg.setY(y);
            mHeadBg.requestLayout();
        }
    }

    /**
     * 隐藏头部的背景
     */
    public void hideHeaderBg() {
        mHeadBgVisiable = false;
        if (mHeadBg != null) {
            mHeadBg.setVisibility(GONE);
        }
    }
    protected AbstractLoadingLayout initHeaderLayout() {
        if (overScrollType == TYPE_HEADERFLOAT_FOOTERFLOAT || overScrollType == TYPE_HEADERFLOAT_FOOTERDRAG) {
            return new RoundLoadingLayout(getContext());
        }
        return new LoadingLayout(getContext(), MODE_PULL_DOWN_TO_REFRESH, releaseLabel, pullDownLabel,
                loadingLabel);
    }

    protected AbstractLoadingLayout initFooterLayout() {
        if (overScrollType == TYPE_HEADERFLOAT_FOOTERFLOAT || overScrollType == TYPE_HEADERDRAG_FOOTERFLOAT) {
            return new RoundLoadingLayout(getContext());
        }
        return new LoadingLayout(getContext(), MODE_PULL_UP_TO_REFRESH, releaseLabel, pullUpLabel, loadingLabel);
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    /**
     * Actions a Pull Event
     *
     * @return true if the Event has been handled, false if there has been no
     * change
     */
    private boolean pullEvent() {

        final int newHeight;
        final int oldHeight = this.getScrollY();

        switch (currentMode) {
            case MODE_PULL_UP_TO_REFRESH:
                newHeight = Math.round(Math.max(initialMotionY - lastMotionY, 0) / FRICTION);
//				newHeight = Math.round((initialMotionY - lastMotionY) / FRICTION);
                break;
            case MODE_PULL_DOWN_TO_REFRESH:
                newHeight = Math.round(Math.min(initialMotionY - lastMotionY, 0) / FRICTION);
                break;
            default:
                newHeight = 0;
//				newHeight = Math.round((initialMotionY - lastMotionY) / FRICTION);
                break;
        }

        if (newHeight != 0) {
            switch (currentMode) {
                case MODE_PULL_UP_TO_REFRESH:
                    if (state == PULL_TO_REFRESH && pullUpRefreshLimitHeight < Math.abs(newHeight)) {
                        state = RELEASE_TO_REFRESH;
                        if (footerLayout != null) {
                            footerLayout.releaseToRefresh();
                        }
                        return true;
                    } else if (state == RELEASE_TO_REFRESH && pullUpRefreshLimitHeight >= Math.abs(newHeight)) {
                        state = PULL_TO_REFRESH;
                        if (footerLayout != null) {
                            footerLayout.pullToRefresh();
                        }
                        return true;
                    } else if ((state == PULL_TO_REFRESH || state == REFRESHING) && !startPullCalled) {
                        // 这个逻辑分支为了触发footer的startPull方法
                        if (footerLayout != null) {
                            footerLayout.startPull();
                            startPullCalled = true;
                        }
                    }
                    break;
                case MODE_PULL_DOWN_TO_REFRESH:
                    if (state == PULL_TO_REFRESH && pullDownRefreshLimitHeight < Math.abs(newHeight)) {
                        state = RELEASE_TO_REFRESH;
                        if (headerLayout != null) {
                            headerLayout.releaseToRefresh();
                        }
                        return true;
                    } else if (state == RELEASE_TO_REFRESH && pullDownRefreshLimitHeight >= Math.abs(newHeight)) {
                        state = PULL_TO_REFRESH;
                        if (headerLayout != null) {
                            headerLayout.pullToRefresh();
                        }
                        return true;
                    } else if ((state == PULL_TO_REFRESH || state == REFRESHING) && !startPullCalled) {
                        // 这个逻辑分支为了触发header的startPull方法
                        if (headerLayout != null) {
                            headerLayout.startPull();
                            startPullCalled = true;
                        }
                    }
                    break;
            }
        }
        setHeaderScroll(newHeight, true);
        return oldHeight != newHeight;
    }

    private boolean isReadyForPull() {
        switch (mode) {
            case MODE_PULL_DOWN_TO_REFRESH:
                return isReadyForPullDown();
            case MODE_PULL_UP_TO_REFRESH:
                return isReadyForPullUp();
            case MODE_BOTH:
                return isReadyForPullUp() || isReadyForPullDown();
        }
        return false;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    public static interface OnRefreshListener {

        public void onRefresh(int curMode);

    }

    public static interface OnLastItemVisibleListener {

        public void onLastItemVisible();

    }

    @Override
    public void setLongClickable(boolean longClickable) {
        getRefreshableView().setLongClickable(longClickable);
    }

    public void setOnTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    public final void setMode(int mode) {
        if (mode != this.mode) {
            this.mode = mode;
            updateUIForMode();
        }
    }

    public int getOverScrollType() {
        return overScrollType;
    }

    /**
     * 设置拖动类型
     *
     * @param overScrollType
     * @see {@link #TYPE_HEADERDRAG_FOOTERDRAG } {@link #TYPE_HEADERDRAG_FOOTERFLOAT}
     * {@link #TYPE_HEADERFLOAT_FOOTERDRAG}{@link #TYPE_HEADERFLOAT_FOOTERFLOAT}
     */
    public void setOverScrollType(int overScrollType) {
        if (this.overScrollType != overScrollType) {
            this.overScrollType = overScrollType;
            updateUIForType();
        }
    }

    protected void updateUIForType() {
        updateUIForMode();
    }

    protected void updateUIForMode() {
        // We need to use the correct LayoutParam values, based on scroll
        // direction

        // Remove Header, and then add Header Loading View again if needed
        if (headerLayout != null && this == headerLayout.getParent()) {
            removeView(headerLayout);
            headerLayout = null;
        }
        headerLayout = initHeaderLayout();
        if (headerLayout != null && (mode == MODE_BOTH || mode == MODE_PULL_DOWN_TO_REFRESH)) {
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            lp.addRule(ALIGN_PARENT_TOP);
            measureView(headerLayout);
            headerHeight = headerLayout.getMeasuredHeight();
            pullDownRefreshLimitHeight = headerHeight;
            lp.topMargin = -headerHeight;
            super.addView(headerLayout, 0, lp);
            if (overScrollType == TYPE_HEADERDRAG_FOOTERDRAG ||
                    overScrollType == TYPE_HEADERDRAG_FOOTERFLOAT) {
                //头部拖动模式下添加头部背景
                addHeaderBg();
            }
        }
        // Remove Footer, and then add Footer Loading View again if needed
        if (footerLayout != null && this == footerLayout.getParent()) {
            removeView(footerLayout);
            footerLayout = null;
        }
        footerLayout = initFooterLayout();
        if (footerLayout != null && (mode == MODE_BOTH || mode == MODE_PULL_UP_TO_REFRESH)) {
            LayoutParams lpf = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            lpf.addRule(ALIGN_PARENT_BOTTOM);
            measureView(footerLayout);
            footerHeight = footerLayout.getMeasuredHeight();
            pullUpRefreshLimitHeight = footerHeight;
            lpf.bottomMargin = -footerHeight;
            super.addView(footerLayout, lpf);
        }
        // Hide Loading Views
        refreshLoadingViewsSize();

        // If we're not using Mode.BOTH, set mCurrentMode to mMode, otherwise
        // set it to pull down
        currentMode = (mode != MODE_BOTH) ? mode : MODE_PULL_DOWN_TO_REFRESH;
    }

    private void refreshLoadingViewsSize() {

    }


}
