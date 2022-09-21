package com.hoody.commonbase.view.fragment;

import com.hoody.commonbase.view.activity.IFragmentControl;

import androidx.fragment.app.FragmentActivity;

public abstract class CloseableFragment extends BaseFragment {

    final public void close() {
        FragmentActivity activity = getActivity();
        if (activity instanceof IFragmentControl) {
            ((IFragmentControl) activity).closeFragment(this.getTag());
        }
    }

    protected abstract void onClose();

}
