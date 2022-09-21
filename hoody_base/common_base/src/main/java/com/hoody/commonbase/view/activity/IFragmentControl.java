package com.hoody.commonbase.view.activity;


import com.hoody.commonbase.view.fragment.CloseableFragment;

public interface IFragmentControl {

    void showFragment(CloseableFragment baseFragment);

    void closeFragment(String fragmentTag);

    CloseableFragment getTopFragment();

    void navigate2Fragment(String fragmentTag);

    void showPreFragmentView(String fragmentTag);

    void hidePreFragmentView(String fragmentTag);
}
