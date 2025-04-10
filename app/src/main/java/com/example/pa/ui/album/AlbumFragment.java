package com.example.pa.ui.album;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;
import com.example.pa.data.Daos.AlbumDao.Album;

import java.util.ArrayList;

public class AlbumFragment extends Fragment implements AlbumAdapter.OnAlbumClickListener {

    private RecyclerView recyclerView;
    private AlbumAdapter albumAdapter;
    private AlbumViewModel albumViewModel;

    private ImageView addIcon;
    private ImageView manageIcon;
    private ImageView setIcon;
    private FrameLayout maskLayer;
    private LinearLayout inputContainer;
    private Button confirm;
    private Button cancel;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 使用 inflater 加载 fragment_album 布局文件
        View rootView = inflater.inflate(R.layout.fragment_album, container, false);

        // 初始化 RecyclerView
        recyclerView = rootView.findViewById(R.id.grid_recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        // 设置适配器
        albumAdapter = new AlbumAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(albumAdapter);

        // 初始化右上角的图标
        addIcon = rootView.findViewById(R.id.add_icon);
        manageIcon = rootView.findViewById(R.id.order_icon);
        setIcon = rootView.findViewById(R.id.set_icon);

        // 遮罩层和输入栏
        maskLayer = rootView.findViewById(R.id.mask_layer);
        inputContainer = rootView.findViewById(R.id.input_container);
        confirm = rootView.findViewById(R.id.btnConfirm);
        cancel = rootView.findViewById(R.id.btnCancel);

        // 获取 ViewModel
        albumViewModel = new ViewModelProvider(this).get(AlbumViewModel.class);

        // 观察图片列表变化
        albumViewModel.getAlbumList().observe(getViewLifecycleOwner(), albums -> {
            albumAdapter = new AlbumAdapter(albums, this);
            recyclerView.setAdapter(albumAdapter);
        });

        // 设置点击事件
        addIcon.setOnClickListener(v -> showInputLayer());
        manageIcon.setOnClickListener(v -> albumAdapter.setManageMode(!albumAdapter.getManageMode()));
        setIcon.setOnClickListener(v -> onSetClicked());
        cancel.setOnClickListener(v -> onCancelClicked(requireView()));
        confirm.setOnClickListener(v -> onConfirmClicked(requireView()));


        return rootView;
    }

    private void onSetClicked() {
    }

    private void showInputLayer() {
        // 显示遮罩层和输入框
        maskLayer.setVisibility(View.VISIBLE);
        inputContainer.setVisibility(View.VISIBLE);
    }

    // 提交输入的内容
    public void onConfirmClicked(View view) {
        String inputText = ((EditText) view.findViewById(R.id.editText)).getText().toString();
        // 处理输入内容，做相应的操作
//        Toast.makeText(getContext(), "提交内容: " + inputText, Toast.LENGTH_SHORT).show();
        albumViewModel.addAlbum(inputText,1,false,false,"private");
        hideKeyboard();

        // 隐藏遮罩层和输入框
        hideInputLayer();
    }

    // 取消按钮的点击事件
    public void onCancelClicked(View view) {
        hideKeyboard();
        hideInputLayer();  // 关闭输入框和遮罩层
    }

    private void hideInputLayer() {
        // 隐藏遮罩层和输入框
        maskLayer.setVisibility(View.GONE);
        inputContainer.setVisibility(View.GONE);
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getActivity().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }



    @Override
    public void onAlbumClick(String albumName) {

        Intent intent = new Intent(getActivity(), PhotoinAlbumActivity.class);
        intent.putExtra("album_name", albumName);
        startActivity(intent);

        // 添加Activity过渡动画（可选）
        if (getActivity() != null) {
            getActivity().overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
        }

    }

    @Override
    public void onDeleteAlbum(Album album) {
        albumViewModel.deleteAlbum(album.id);  // 调用 ViewModel 删除相册
    }
}

