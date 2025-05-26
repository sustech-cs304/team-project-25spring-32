package com.example.pa.data;

import android.content.Context;
import android.net.Uri;

import com.example.pa.R; // 确保你的 R 文件路径正确
import com.example.pa.data.model.MusicItem;

import java.util.ArrayList;
import java.util.List;

public class MusicRepository {

    // 这是一个获取音乐列表的示例方法。
    // 你需要将这里的 placeholder 替换为真实的音乐资源或 URL。
    public List<MusicItem> getMusicList(Context context) {
        List<MusicItem> musicList = new ArrayList<>();

        // 1. 添加 "无音乐" 选项
        musicList.add(new MusicItem(context.getString(R.string.music_none), null));

        // 2. 添加内嵌音乐 (示例)
        // 假设你在 res/raw 目录下放了音乐文件，例如 gentle_breeze.mp3
         Uri beyond = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.beyond);
         Uri fish = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.fish);
         Uri solstice = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.solstice);
         Uri star = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.star);
         Uri street = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.street);

         musicList.add(new MusicItem(context.getString(R.string.beyond), beyond));
         musicList.add(new MusicItem(context.getString(R.string.deep_sea_fish), fish));
         musicList.add(new MusicItem(context.getString(R.string.solstice), solstice));
         musicList.add(new MusicItem(context.getString(R.string.star_at_sea), star));
         musicList.add(new MusicItem(context.getString(R.string.yandai_xiejie), street));


        // 3. 添加 URL 音乐 (示例) - 注意：使用网络 URL 需要 FFmpeg 支持或先下载
        // Uri music2Uri = Uri.parse("http://your.music.server/happy_journey.mp3");
        // musicList.add(new MusicItem(context.getString(R.string.music_happy_journey), music2Uri));

        // **** 占位符 - 请替换或添加真实的音乐 ****
        // 为了演示，我们暂时只添加名称，Uri 为 null (除了“无音乐”)
//        musicList.add(new MusicItem(context.getString(R.string.music_gentle_breeze), Uri.parse("placeholder_uri_1")));
//        musicList.add(new MusicItem(context.getString(R.string.music_happy_journey), Uri.parse("placeholder_uri_2")));
//        musicList.add(new MusicItem(context.getString(R.string.music_peaceful_moments), Uri.parse("placeholder_uri_3")));
        // **** 占位符结束 ****

        return musicList;
    }
}
