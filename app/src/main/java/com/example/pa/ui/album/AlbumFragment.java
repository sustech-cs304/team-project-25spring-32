package com.example.pa.ui.album;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import com.example.pa.R;
import com.example.pa.data.Daos.AlbumDao.Album;

import java.util.ArrayList;
import java.util.List;

/**
 * AI-generated-content
 * tool: ChatGPT
 * version: 4o
 * usage: I described my UI design to it, and asked how to program.
 * I use the generated code as template.
 */
public class AlbumFragment extends Fragment implements AlbumAdapter.OnAlbumClickListener {

    private RecyclerView recyclerView;
    private AlbumAdapter albumAdapter;
    private AlbumViewModel albumViewModel;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // 分组视图
    private LinearLayout customSection;
    private LinearLayout timeSection;
    private LinearLayout locationSection;

    // RecyclerViews
    private RecyclerView customRecyclerView;
    private RecyclerView timeRecyclerView;
    private RecyclerView locationRecyclerView;

    // 适配器
    private AlbumAdapter customAdapter;
    private AlbumAdapter timeAdapter;
    private AlbumAdapter locationAdapter;

    // 分组箭头
    private ImageView customArrow;
    private ImageView timeArrow;
    private ImageView locationArrow;

    // 其他视图
    private ImageView addIcon;
    private ImageView manageIcon;
    private ImageView setIcon;
    private FrameLayout maskLayer;
    private LinearLayout inputContainer;
    private Button confirm;
    private Button cancel;


    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            albumViewModel.loadAlbums();
        } else {
            Toast.makeText(getContext(), "需要权限才能创建相册", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 使用 inflater 加载 fragment_album 布局文件
        View rootView = inflater.inflate(R.layout.fragment_album, container, false);

        // 初始化分组视图
        customSection = rootView.findViewById(R.id.custom_section);
        timeSection = rootView.findViewById(R.id.time_section);
        locationSection = rootView.findViewById(R.id.location_section);

        customArrow = rootView.findViewById(R.id.custom_arrow);
        timeArrow = rootView.findViewById(R.id.time_arrow);
        locationArrow = rootView.findViewById(R.id.location_arrow);

        View customHeader = rootView.findViewById(R.id.custom_header);
        View timeHeader = rootView.findViewById(R.id.time_header);
        View locationHeader = rootView.findViewById(R.id.location_header);

        // 初始化 RecyclerViews
        customRecyclerView = rootView.findViewById(R.id.custom_recycler_view);
        timeRecyclerView = rootView.findViewById(R.id.time_recycler_view);
        locationRecyclerView = rootView.findViewById(R.id.location_recycler_view);

        // 设置布局管理器
        customRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        timeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        locationRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // 初始化适配器
        customAdapter = new AlbumAdapter(new ArrayList<>(), this, AlbumType.CUSTOM);
        timeAdapter = new AlbumAdapter(new ArrayList<>(), this, AlbumType.TIME);
        locationAdapter = new AlbumAdapter(new ArrayList<>(), this, AlbumType.LOCATION);

        customRecyclerView.setAdapter(customAdapter);
        timeRecyclerView.setAdapter(timeAdapter);
        locationRecyclerView.setAdapter(locationAdapter);

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
        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);

        // 观察相册列表变化
        albumViewModel.getAlbumList().observe(getViewLifecycleOwner(), albums -> {
            // 按类型分类相册
            List<Album> customAlbums = new ArrayList<>();
            List<Album> timeAlbums = new ArrayList<>();
            List<Album> locationAlbums = new ArrayList<>();

            for (Album album : albums) {
                if (album.isAutoGenerated) {
                    if (album.name.contains("年") || album.name.contains("月")) {
                        timeAlbums.add(album);
                    } else {
                        locationAlbums.add(album);
                    }
                } else {
                    customAlbums.add(album);
                }
            }
            Log.d("Album", "customAlbums: "+customAlbums.size()+"\n"+"timeAlbums: "+timeAlbums.size()+"\n"+"locationAlbums: "+locationAlbums.size()+"\n");

            // 更新适配器
            customAdapter.updateAlbums(customAlbums);
            timeAdapter.updateAlbums(timeAlbums);
            locationAdapter.updateAlbums(locationAlbums);

            // 更新分组标题计数
            ((TextView) rootView.findViewById(R.id.custom_count)).setText(customAlbums.size() + "个相册");
            ((TextView) rootView.findViewById(R.id.time_count)).setText(timeAlbums.size() + "个相册");
            ((TextView) rootView.findViewById(R.id.location_count)).setText(locationAlbums.size() + "个相册");

            // 如果没有自动生成的相册，隐藏对应分组
            if (timeAlbums.isEmpty()) {
                timeSection.setVisibility(View.GONE);
            } else {
                timeSection.setVisibility(View.VISIBLE);
            }

            if (locationAlbums.isEmpty()) {
                locationSection.setVisibility(View.GONE);
            } else {
                locationSection.setVisibility(View.VISIBLE);
            }
        });

        // 设置点击事件
        addIcon.setOnClickListener(v -> showInputLayer());
        manageIcon.setOnClickListener(v -> toggleManageMode());
        setIcon.setOnClickListener(v -> onSetClicked());
        cancel.setOnClickListener(v -> onCancelClicked(requireView()));
        confirm.setOnClickListener(v -> onConfirmClicked(requireView()));

        // 分组折叠/展开事件
        customHeader.setOnClickListener(v -> toggleSection(customRecyclerView, customArrow));
        timeHeader.setOnClickListener(v -> toggleSection(timeRecyclerView, timeArrow));
        locationHeader.setOnClickListener(v -> toggleSection(locationRecyclerView, locationArrow));

        return rootView;
    }

    // 切换管理模式（所有分组同时进入/退出管理模式）
    private void toggleManageMode() {
        boolean newMode = !customAdapter.getManageMode();
        customAdapter.setManageMode(newMode);
        timeAdapter.setManageMode(newMode);
        locationAdapter.setManageMode(newMode);
    }

    // 分组折叠/展开
    private void toggleSection(RecyclerView recyclerView, ImageView arrow) {
        Log.d("Album", "Toggling section...");
        if (recyclerView.getVisibility() == View.VISIBLE) {
            recyclerView.setVisibility(View.GONE);
            arrow.setRotation(180); // 箭头向上
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            arrow.setRotation(0); // 箭头向下
        }
    }

    /**
     * AI-generated-content
     * tool: DeepSeek
     * version: R1
     * usage: I asked how to create a local folder, and
     * directly copy the code from its response.
     */
    // 直接检查权限是否已授予
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Android 5.1 及以下默认授予权限
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
        if (checkPermissions()) {
            albumViewModel.addAlbum(inputText,1,false,false,"private");
        }
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


    /**
     * AI-generated-content
     * tool: ChatGPT
     * version: 4o
     * usage: I asked it how to implement interface jumps.
     * Directly copy the code from its response.
     */
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
        albumViewModel.deleteAlbum(album.id, album.name);  // 调用 ViewModel 删除相册
    }

    // 相册类型枚举
    public enum AlbumType {
        CUSTOM, TIME, LOCATION
    }
}

