package com.hoody.wificontrol.view;

import android.Manifest;
import android.app.Service;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hoody.annotation.model.ModelManager;
import com.hoody.annotation.permission.Permissions;
import com.hoody.annotation.router.Router;
import com.hoody.annotation.router.RouterUtil;
import com.hoody.commonbase.customview.ChildMoveLayout;
import com.hoody.commonbase.customview.slidedecidable.SlideDecidableLayout;
import com.hoody.commonbase.customview.slidedecidable.SwipeBackLayout;
import com.hoody.commonbase.util.DeviceInfo;
import com.hoody.commonbase.util.SharedPreferenceUtil;
import com.hoody.commonbase.util.ToastUtil;
import com.hoody.commonbase.view.fragment.SwipeBackFragment;
import com.hoody.model.wificontrol.IWifiDeviceModel;
import com.hoody.model.wificontrol.IWifiObserver;
import com.hoody.wificontrol.MainActivity;
import com.hoody.wificontrol.R;
import com.hoody.model.wificontrol.SingleKey;
import com.hoody.wificontrol.model.WifiDeviceModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@Router("wificontrol/wifi")
@Permissions({Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.CHANGE_NETWORK_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION})
public class WifiControlFragment extends SwipeBackFragment implements IWifiObserver {
    private static final String TAG = "MainFragment";
    public static final String Data_TAG = "MainFragment";

    private ChildMoveLayout mFreeMoveBase;
    private View.OnClickListener mOnKeyClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mKeyPopupWindow != null) {
                if (mKeyPopupWindow.isShowing()) {
                    mKeyPopupWindow.dismiss();
                }
            }
            if (mEditMode) {
                Object tag = v.getTag();
                mKeyPopupWindow = new PopupWindow(getContext());
                mKeyPopupWindow.setOutsideTouchable(true);
                mKeyPopupWindow.setContentView(View.inflate(getContext(), R.layout.pop_key_setting, null));
                int windowWidth = (int) DeviceInfo.dp2px(getContext(), 100);
                mKeyPopupWindow.setWidth(windowWidth);
                mKeyPopupWindow.setHeight(windowWidth);
                mKeyPopupWindow.getContentView().findViewById(R.id.bt_reset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mKeyPopupWindow.dismiss();
                        if (tag instanceof SingleKey) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("keyBean", (SingleKey) tag);
                            RouterUtil.getInstance().navigateTo(getContext(), "wifi/keyset", bundle);
                        }
                    }
                });
                mKeyPopupWindow.getContentView().findViewById(R.id.bt_del_key).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mKeyPopupWindow.dismiss();
                        if (tag instanceof SingleKey) {
                            for (int i = 0; i < mFreeMoveBase.getChildCount(); i++) {
                                View child = mFreeMoveBase.getChildAt(i);
                                Object childTag = child.getTag();
                                if (childTag instanceof SingleKey) {
                                    if (((SingleKey) tag).getId().equals(((SingleKey) childTag).getId())) {
                                        mKeys.remove(tag);
                                        mFreeMoveBase.removeView(child);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                });
                int yoff = 0;
                if (v.getBottom() + windowWidth > mFreeMoveBase.getHeight()) {
                    yoff = -v.getHeight() - windowWidth;
                }
                mKeyPopupWindow.showAsDropDown(v, 0, yoff, Gravity.CENTER);
            } else {
                Vibrator vib = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
                vib.vibrate(100);
            }
        }
    };
    private boolean mEditMode = false;
    private PopupWindow mKeyPopupWindow;

    @Override
    protected void onClose() {

    }

    private void saveKeyInfo() {
        JSONObject jsonObject = new JSONObject();
        JSONArray datas = new JSONArray();
        try {
            for (SingleKey key : mKeys) {
                JSONObject jsonKey = new JSONObject();
                jsonKey.putOpt("id", key.getId());
                jsonKey.putOpt("name", key.getName());
                jsonKey.putOpt("pos_x", key.getPosX());
                jsonKey.putOpt("pos_y", key.getPosY());
                jsonKey.putOpt("width", key.getWidth());
                jsonKey.putOpt("height", key.getHeight());
                jsonKey.putOpt("backgroundColor", key.getBackgroundColor());
                jsonKey.putOpt("textColor", key.getTextColor());
                jsonKey.putOpt("textSize", key.getTextSize());
                jsonKey.putOpt("data", key.getDataCode());
                datas.put(jsonKey);
            }
            jsonObject.put("keys", datas);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferenceUtil.getInstance().saveSharedPreferences(Data_TAG, jsonObject.toString());
    }

    @Override
    protected View createContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return View.inflate(getContext(), R.layout.fragment_wifi_control, null);
    }

    private ArrayList<SingleKey> mKeys = new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            String s = SharedPreferenceUtil.getInstance().readSharedPreferences(Data_TAG, "");
            if (TextUtils.isEmpty(s)) {
                s = "{\n" +
                        "    \"keys\": [\n" +
                        "        {\n" +
                        "            \"id\": \"1\",\n" +
                        "            \"name\": \"上\",\n" +
                        "            \"pos_x\": 1,\n" +
                        "            \"pos_y\": 3,\n" +
                        "            \"width\": 4,\n" +
                        "            \"height\": 3,\n" +
                        "            \"data\": \"110111011100011110111\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"id\": \"2\",\n" +
                        "            \"name\": \"下\",\n" +
                        "            \"pos_x\": 1,\n" +
                        "            \"pos_y\": 3,\n" +
                        "            \"width\": 4,\n" +
                        "            \"height\": 3,\n" +
                        "            \"data\": \"110111011100011110111\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"id\": \"3\",\n" +
                        "            \"name\": \"左\",\n" +
                        "            \"pos_x\": 1,\n" +
                        "            \"pos_y\": 3,\n" +
                        "            \"width\": 4,\n" +
                        "            \"height\": 3,\n" +
                        "            \"data\": \"110111011100011110111\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"id\": \"4\",\n" +
                        "            \"name\": \"右\",\n" +
                        "            \"pos_x\": 1,\n" +
                        "            \"pos_y\": 3,\n" +
                        "            \"width\": 4,\n" +
                        "            \"height\": 3,\n" +
                        "            \"data\": \"110111011100011110111\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}";
            }
            JSONObject jsonObject = new JSONObject(s);
            JSONArray keys = jsonObject.optJSONArray("keys");
            for (int i = 0; i < keys.length(); i++) {
                JSONObject jsonKey = keys.optJSONObject(i);
                String id = jsonKey.optString("id");
                String name = jsonKey.optString("name");
                int pos_x = jsonKey.optInt("pos_x");
                int pos_y = jsonKey.optInt("pos_y");
                int width = jsonKey.optInt("width");
                int height = jsonKey.optInt("height");
                int backgroundColor = jsonKey.optInt("backgroundColor");
                int textColor = jsonKey.optInt("textColor");
                int textSize = jsonKey.optInt("textSize");
                String data = jsonKey.optString("data");
                SingleKey singleKey = new SingleKey(id, name, data);
                singleKey.setPosX(pos_x);
                singleKey.setPosY(pos_y);
                singleKey.setWidth(width);
                singleKey.setHeight(height);
                singleKey.setTextColor(textColor);
                singleKey.setTextSize(textSize);
                singleKey.setBackgroundColor(backgroundColor);
                mKeys.add(singleKey);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        initView();
        //填充数据
        for (SingleKey key : mKeys) {
            Button button = new Button(getContext());
            button.setSingleLine();
            bindKeyInfo(key, button);
            mFreeMoveBase.addView(button);
        }
        mObserverRegister.regist(this);
        //检查设备状态
        ModelManager.getModel(IWifiDeviceModel.class).checkDeviceStatus();
    }

    private void bindKeyInfo(SingleKey key, Button button) {
        button.setTag(key);
        button.setOnClickListener(mOnKeyClickListener);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mKeyPopupWindow != null) {
                    if (mKeyPopupWindow.isShowing()) {
                        mKeyPopupWindow.dismiss();
                    }
                }
                return false;
            }
        });
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) button.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new FrameLayout.LayoutParams(ChildMoveLayout.SPLIT_LENGTH * key.getWidth(), ChildMoveLayout.SPLIT_LENGTH * key.getHeight());

        }
        layoutParams.width = ChildMoveLayout.SPLIT_LENGTH * key.getWidth();
        layoutParams.height = ChildMoveLayout.SPLIT_LENGTH * key.getHeight();
        button.setLayoutParams(layoutParams);

        button.setText(key.getName());
        button.setY(key.getPosY() * ChildMoveLayout.SPLIT_LENGTH);
        button.setX(key.getPosX() * ChildMoveLayout.SPLIT_LENGTH);
        button.setTextColor(key.getTextColor());
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, key.getTextSize());
        button.setBackgroundColor(key.getBackgroundColor());
        button.setPadding(0, 0, 0, 0);

    }

    private void initView() {
        SwipeBackLayout swipeBackLayout = getSwipeBackLayout();
        swipeBackLayout.addDecider(new SlideDecidableLayout.SlidableDecider(getView()) {
            @Override
            public boolean slidable(PointF curEvPos, SlideDecidableLayout.SlideOrientation slideOrientation) {
                for (int i = 0; i < mFreeMoveBase.getChildCount(); i++) {
                    View childAt = mFreeMoveBase.getChildAt(i);
                    if (isTouchPointInView(childAt, curEvPos.x, curEvPos.y)) {
                        return false;
                    }
                }
                return true;
            }
        });
        mFreeMoveBase = ((ChildMoveLayout) findViewById(R.id.free_move_base));
        mFreeMoveBase.setOnViewPositionChangeedListener(new ChildMoveLayout.OnViewPositionChangeedListener() {
            @Override
            public void onViewPositionChangeed(View positionChangedView) {
                float x = positionChangedView.getX();
                float y = positionChangedView.getY();
                Object tag = positionChangedView.getTag();
                if (tag instanceof SingleKey) {
                    ((SingleKey) tag).setPosX((int) x / ChildMoveLayout.SPLIT_LENGTH);
                    ((SingleKey) tag).setPosY((int) y / ChildMoveLayout.SPLIT_LENGTH);
                }
            }
        });
        Bundle arguments = getArguments();
        if (arguments.getInt("forset") == 1) {
            mEditMode = true;
            mFreeMoveBase.startAnim();
            mFreeMoveBase.setChildDragEnable(true);
        }
        findViewById(R.id.ll_bottom).setVisibility(mEditMode ? View.VISIBLE : View.GONE);
        findViewById(R.id.bt_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mKeyPopupWindow != null) {
                    if (mKeyPopupWindow.isShowing()) {
                        mKeyPopupWindow.dismiss();
                    }
                }
                mEditMode = false;
                mFreeMoveBase.stopAnim();
                mFreeMoveBase.setChildDragEnable(false);
                findViewById(R.id.ll_bottom).setVisibility(View.GONE);
                saveKeyInfo();
            }
        });
        findViewById(R.id.bt_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterUtil.getInstance().navigateTo(getContext(), "wifi/keyset", null);
            }
        });
    }


    @Override
    public void onNoFoundDevices() {
        ToastUtil.showToast(getContext(), "没有发现设备");
    }

    @Override
    public void onStudySuccess(String keyId, String data) {
        ToastUtil.showToast(getContext(), "学习成功 :" + data);
    }

    @Override
    public void onStudyFail(int code, String keyId) {
        if (code == WifiDeviceModel.Code.Code_study_key_err) {
            //按键输入错误
            ToastUtil.showToast(getContext(), "学习失败 :按键输入错误");
        } else if (code == WifiDeviceModel.Code.Code_being_study_other) {
            //正在学习其他按键
            ToastUtil.showToast(getContext(), "学习失败 :正在学习其他按键");
        } else if (code == WifiDeviceModel.Code.Code_study_outtime) {
            //按键学习超时
            ToastUtil.showToast(getContext(), "学习失败 :按键学习超时");
        }
    }

    @Override
    public void onManagerPassSetFail(int code) {
        ToastUtil.showToast(getContext(), "设置失败，密码长度8-20位");
    }

    @Override
    public void onModifyWifiPassSuccess() {
        ToastUtil.showToast(getContext(), "修改成功，等待设备重启");
    }

    @Override
    public void onModifyWifiPassFail() {
        ToastUtil.showToast(getContext(), "修改失败，密码长度8-20位");
    }

    @Override
    public void onKeyInfoChanged(SingleKey key) {
        for (int i = 0; i < mFreeMoveBase.getChildCount(); i++) {
            View child = mFreeMoveBase.getChildAt(i);
            Object tag = child.getTag();
            if (tag instanceof SingleKey) {
                if (((SingleKey) tag).getId().equals(key.getId())) {
                    ((SingleKey) tag).copy(key);
                    if (child instanceof Button) {
                        bindKeyInfo(key, (Button) child);
                    }
                    return;
                }
            }
        }

        mKeys.add(key);
        Button button = new Button(getContext());
        button.setSingleLine();
        bindKeyInfo(key, button);
        mFreeMoveBase.addView(button);
    }
}
