package com.example.pa.ui.album

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.pa.MyApplication
import com.example.pa.R
import com.example.pa.data.Daos.AlbumDao.Album
import com.example.pa.data.FileRepository
import com.example.pa.data.FileRepository.MediaScanCallback
import com.example.pa.ui.album.AlbumAdapter.AlbumViewHolder
import com.example.pa.ui.album.AlbumFragment.AlbumType

/**
 * AI-generated-content
 * tool: ChatGPT
 * version: 4o
 * usage: I described my UI design to it, and asked how to program.
 * I use the generated code as template.
 */
class AlbumAdapter(
    private var albumList: List<Album>,
    private val listener: OnAlbumClickListener?,
    private val albumType: AlbumType
) :
    RecyclerView.Adapter<AlbumViewHolder>() {
    private val fileRepository: FileRepository = MyApplication.getInstance().fileRepository
    private val positionTimestamps = SparseArray<Long>()
    private var isManageMode = false // 控制删除图标显示

    interface OnAlbumClickListener {
        fun onAlbumClick(album: Album?)
        fun onDeleteAlbum(album: Album?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        // 加载每个项的布局
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }

    /**
     * AI-generated-content
     * tool: DeepSeek
     * version: R1
     * usage: I asked how to solve the problem of asynchronous scan, and
     * modify the code according to my existed code.
     */
    override fun onBindViewHolder(
        holder: AlbumViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val timestamp = System.currentTimeMillis()
        positionTimestamps.put(position, timestamp)

        // 在这里加载图片
        val album = albumList[position]
        holder.textView.text = album.name
        Log.d("AlbumAdapter", "onBindViewHolder: " + album.name)

        // 根据相册类型设置样式
//        switch (albumType) {
//            case CUSTOM:
//                holder.typeBadge.setVisibility(View.VISIBLE);
//                holder.typeBadge.setText("自定义");
//                holder.typeBadge.setBackgroundResource(R.drawable.bg_custom_badge);
//                break;
//            case TIME:
//                holder.typeBadge.setVisibility(View.VISIBLE);
//                holder.typeBadge.setText("时间");
//                holder.typeBadge.setBackgroundResource(R.drawable.bg_time_badge);
//                break;
//            case LOCATION:
//                holder.typeBadge.setVisibility(View.VISIBLE);
//                holder.typeBadge.setText("地点");
//                holder.typeBadge.setBackgroundResource(R.drawable.bg_location_badge);
//                break;
//        }

        // 你可以使用 Glide 或 Picasso 来加载图片
        if (albumType == AlbumType.CUSTOM) {
            Glide.with(holder.itemView.context)
                .load(getAlbumCover(album))
                .into(holder.imageView)
        } else {
            Glide.with(holder.itemView.context)
                .load(getAutoAlbumCover(album))
                .into(holder.imageView)
        }


        // 带回调的扫描
        fileRepository.triggerMediaScanForAlbum(album.name, object : MediaScanCallback {
            override fun onScanCompleted(uri: Uri?) {
                // 验证视图是否仍然有效
                if (positionTimestamps[position, -1L] != timestamp) return

                val coverUri = getAlbumCover(album)
                if (coverUri != null) {
                    Glide.with(holder.itemView.context)
                        .load(coverUri)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(holder.imageView)
                }
            }

            override fun onScanFailed(error: String?) {
                Log.e("AlbumAdapter", error!!)
            }
        })

        holder.itemView.setOnClickListener { v: View? ->
            listener?.onAlbumClick(album)
        }

        // 绑定删除图标的显示与点击事件
        if (isManageMode) {
            holder.deleteIcon.visibility = View.VISIBLE
            holder.deleteIcon.setOnClickListener { v: View? ->
                listener?.onDeleteAlbum(album)
            }
        } else {
            holder.deleteIcon.visibility = View.GONE
        }
    }

    private fun getAlbumCover(album: Album): Uri? {
        return if (album.cover != null) Uri.parse(album.cover) else fileRepository.getAlbumCover(
            album.name
        )
    }

    private fun getAutoAlbumCover(album: Album): Uri {
        val mainRepository = MyApplication.getInstance().mainRepository
        return Uri.parse(mainRepository.getLatestPhotoPath(album.name))
    }

    override fun getItemCount(): Int {
        return albumList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setManageMode(isManageMode: Boolean) {
        this.isManageMode = isManageMode
        notifyDataSetChanged() // 更新图标显示
    }

    fun getManageMode(): Boolean {
        return isManageMode
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAlbums(newAlbums: List<Album>) {
        this.albumList = newAlbums
        notifyDataSetChanged()
    }

    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 引用布局中的 ImageView 和 TextView
        var imageView: ImageView =
            itemView.findViewById(R.id.imageView)
        var textView: TextView =
            itemView.findViewById(R.id.image_text)

        //        TextView typeBadge; // 新增类型标识
        //            typeBadge = itemView.findViewById(R.id.type_badge); // 新增视图
        var deleteIcon: ImageView =
            itemView.findViewById(R.id.delete_icon)
    }
}


