/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.hoody.commonbase.customview.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.hoody.commonbase.R;

import androidx.recyclerview.widget.RecyclerView;



public class PullToRefreshRecyclerView extends PullToRefreshBase<RecyclerView> {

    protected RecyclerView mRecyclerView;

    public PullToRefreshRecyclerView(Context context) {
        super(context);
    }

    public PullToRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshRecyclerView(Context context, int mode) {
        super(context, mode);
    }

    @Override
    protected RecyclerView createRefreshableView(Context context, AttributeSet attrs) {
        mRecyclerView = new RecyclerView(context, attrs);
        mRecyclerView.setId(R.id.recyclerview);
        return mRecyclerView;
    }

    @Override
    protected boolean isReadyForPullDown() {
        return isFirstItemVisible();
    }

    @Override
    protected boolean isReadyForPullUp() {
        return isLastItemVisible();
    }


    public boolean isFirstItemVisible() {
        final RecyclerView.Adapter<?> adapter = getRefreshableView().getAdapter();

        if (null == adapter || adapter.getItemCount() == 0) {
            return true;

        } else {
            /**
             * This check should really just be:
             * mRefreshableView.getFirstVisiblePosition() == 0, but PtRListView
             * internally use a HeaderView which messes the positions up. For
             * now we'll just add one to account for it and rely on the inner
             * condition which checks getTop().
             */
            if (getFirstVisiblePosition() == 0) {
                final View firstVisibleChild = refreshableView.getChildAt(0);
                if (firstVisibleChild != null) {
                    return firstVisibleChild.getTop() >= refreshableView.getTop();
                }
            }
        }

        return false;
    }

    public boolean isLastItemVisible() {
        final RecyclerView.Adapter<?> adapter = getRefreshableView().getAdapter();

        if (null == adapter || adapter.getItemCount() == 0) {
            return true;
        } else {
            int lastVisiblePosition = getLastVisiblePosition();
            if (lastVisiblePosition >= refreshableView.getAdapter().getItemCount() - 1) {
                return refreshableView.getChildAt(
                        refreshableView.getChildCount() - 1).getBottom() <= refreshableView
                        .getBottom();
            }
        }
        return false;
    }

    /**
     * @Description: 获取第一个可见子View的位置下标
     */
    private int getFirstVisiblePosition() {
        View firstVisibleChild = refreshableView.getChildAt(0);
        return firstVisibleChild != null ? refreshableView
                .getChildAdapterPosition(firstVisibleChild) : -1;
    }

    /**
     * @Description: 获取最后一个可见子View的位置下标
     */
    private int getLastVisiblePosition() {
        View lastVisibleChild = refreshableView.getChildAt(refreshableView
                .getChildCount() - 1);
        return lastVisibleChild != null ? refreshableView
                .getChildAdapterPosition(lastVisibleChild) : -1;
    }

}