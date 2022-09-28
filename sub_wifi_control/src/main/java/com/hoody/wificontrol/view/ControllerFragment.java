package com.hoody.wificontrol.view;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hoody.wificontrol.model.KeyboardItem;
import com.hoody.wificontrol.model.SingleKey;
import com.hoody.wificontrol.model.DirKeyGroup;
import com.hoody.wificontrol.model.KeyNumGroup;
import com.hoody.wificontrol.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ControllerFragment extends Fragment {
    private KeyboardItem[] mItems = new KeyboardItem[]{
            new DirKeyGroup(),
            new KeyNumGroup(),
    };

    public ControllerFragment() {

        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_controller, container, false);
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
                if (mItems[position] instanceof DirKeyGroup) {
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
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new MyTouchEvent());
        itemTouchHelper.attachToRecyclerView(ls_keyboard);
    }


    public class MyTouchEvent extends ItemTouchHelper.Callback {

        public MyTouchEvent() {
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int i = ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT | ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            return makeMovementFlags(i, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int adapterPosition = viewHolder.getAdapterPosition();
            int adapterPosition1 = target.getAdapterPosition();
            Collections.swap( ((MainAdapter) recyclerView.getAdapter()).mKeyboardItems,adapterPosition,adapterPosition1);
            recyclerView.getAdapter().notifyItemMoved(adapterPosition, adapterPosition1);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            Log.d("TAGTAG", "onSwiped() called with: recyclerView =");
        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                viewHolder.itemView.setBackgroundColor(Color.RED);
            }
        }

        //手松开的时候还原
        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            Log.d("TAGTAG", "clearView() called with: recyclerView =");
            viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    static class MainAdapter extends RecyclerView.Adapter {
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

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof KeyHolder) {
                ((KeyHolder) holder).key.setText(((SingleKey) mKeyboardItems.get(position)).getName());
            } else {
                ((KeyGroupHolder) holder).keyGroup.setAdapter(((DirKeyGroup) mKeyboardItems.get(position)).getGroupAdapter());
                ((KeyGroupHolder) holder).keyGroup.setLayoutManager(((DirKeyGroup) mKeyboardItems.get(position)).getLayoutManager(holder.itemView.getContext()));
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

        static class KeyHolder extends RecyclerView.ViewHolder {

            private final TextView key;

            public KeyHolder(@NonNull View itemView) {
                super(itemView);
                key = ((TextView) itemView.findViewById(R.id.item_key));
            }
        }

        static class KeyGroupHolder extends RecyclerView.ViewHolder {
            private final RecyclerView keyGroup;

            public KeyGroupHolder(@NonNull View itemView) {
                super(itemView);
                keyGroup = itemView.findViewById(R.id.ls_key_group);
            }
        }
    }
}