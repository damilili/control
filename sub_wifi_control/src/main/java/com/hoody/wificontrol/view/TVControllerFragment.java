package com.hoody.wificontrol.view;

import android.app.Service;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hoody.annotation.model.ModelManager;
import com.hoody.annotation.router.RouterUtil;
import com.hoody.commonbase.util.ToastUtil;
import com.hoody.model.wificontrol.IWifiDeviceModel;
import com.hoody.wificontrol.R;
import com.hoody.wificontrol.model.KeyboardItem;
import com.hoody.wificontrol.model.SingleKey;
import com.hoody.wificontrol.model.DirKeyGroup;
import com.hoody.wificontrol.model.KeyGroup;
import com.hoody.wificontrol.model.KeyNumGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TVControllerFragment extends Fragment {
    private KeyboardItem[] mItems = new KeyboardItem[]{
            new DirKeyGroup(),
            new KeyNumGroup(),
    };

    public TVControllerFragment() {

        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_controller_tv, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        switch (getArguments().getInt("position")) {
            case 0:
                ((ImageView) view.findViewById(R.id.device_logo)).setImageResource(R.drawable.logo_tv);
                break;
            case 1:
                ((ImageView) view.findViewById(R.id.device_logo)).setImageResource(R.drawable.logo_air);
                break;
        }

        RecyclerView ls_keyboard = (RecyclerView) view.findViewById(R.id.ls_keyboard);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 12);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mItems[position] instanceof KeyGroup) {
                    return mItems[position].spanSize;
                }
                return 4;
            }
        });
        ls_keyboard.setLayoutManager(layoutManager);
        DividerItemDecoration decorV = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(2, 0, 2, 0);
            }
        };
        ColorDrawable drawable = new ColorDrawable(Color.TRANSPARENT);
        decorV.setDrawable(drawable);
        ls_keyboard.addItemDecoration(decorV);

        DividerItemDecoration decorH = new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL) {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 0, 0, 2);
            }
        };
        decorH.setDrawable(new ColorDrawable(Color.TRANSPARENT));

        ls_keyboard.addItemDecoration(decorH);
        RecyclerView.Adapter adapter = new MainAdapter(new ArrayList<KeyboardItem>(Arrays.asList(mItems)));
        ls_keyboard.setAdapter(adapter);
    }

    class MainAdapter extends RecyclerView.Adapter {
        private ArrayList<KeyboardItem> mKeyboardItems;

        public MainAdapter(ArrayList<KeyboardItem> keyboardItems) {
            mKeyboardItems = keyboardItems;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 0) {
                return new KeyHolder(View.inflate(parent.getContext(), R.layout.item_key, null));
            } else {
                return new KeyGroupHolder(View.inflate(parent.getContext(), R.layout.item_key_group, null));
            }
        }

        KeyboardItem.OnKeyClickListener onKeyClickListener = new KeyboardItem.OnKeyClickListener() {


            @Override
            public void OnStudy(View v, SingleKey singleKey) {
                ToastUtil.showToast(v.getContext(), singleKey.getName());
                Bundle bundle = new Bundle();
                bundle.putSerializable("keyBean",singleKey);
                RouterUtil.getInstance().navigateTo(getContext(), "wifi/keyset", bundle);
            }

            @Override
            public void OnClick(View v, SingleKey singleKey) {
                Vibrator vib = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
                vib.vibrate(100);
                ModelManager.getModel(IWifiDeviceModel.class).sendSignal(singleKey.getDataCode());
            }
        };

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof KeyHolder) {
                ((KeyHolder) holder).key.setText(((SingleKey) mKeyboardItems.get(position)).getName());
            } else {
                mKeyboardItems.get(position).setOnStudyListener(onKeyClickListener);
                ((KeyGroupHolder) holder).keyGroup.setAdapter(((KeyGroup) mKeyboardItems.get(position)).getGroupAdapter());
                ((KeyGroupHolder) holder).keyGroup.setLayoutManager(((KeyGroup) mKeyboardItems.get(position)).getLayoutManager(holder.itemView.getContext()));
                List<DividerItemDecoration> dividerItemDecorations = ((KeyGroup) mKeyboardItems.get(position)).getDividerItemDecorations(holder.itemView.getContext());
                for (DividerItemDecoration dividerItemDecoration : dividerItemDecorations) {
                    if (dividerItemDecoration != null) {
                        ((KeyGroupHolder) holder).keyGroup.addItemDecoration(dividerItemDecoration);
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return mKeyboardItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (mKeyboardItems.get(position) instanceof SingleKey) {
                return 0;
            } else {
                return 1;
            }
        }

        class KeyHolder extends RecyclerView.ViewHolder {

            private final TextView key;

            public KeyHolder(@NonNull View itemView) {
                super(itemView);
                key = ((TextView) itemView.findViewById(R.id.item_key));
            }
        }

        class KeyGroupHolder extends RecyclerView.ViewHolder {
            private final RecyclerView keyGroup;

            public KeyGroupHolder(@NonNull View itemView) {
                super(itemView);
                keyGroup = itemView.findViewById(R.id.ls_key_group);
            }
        }
    }
}