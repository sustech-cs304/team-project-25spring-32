package com.example.pa.ui.memory;


public enum TransitionType {
    FADE("fade"), // 淡入淡出
    SLIDE_LEFT("slideleft"), // 向左滑动
    SLIDE_RIGHT("slideright"), // 向右滑动
    SLIDE_UP("slideup"), // 向上滑动
    SLIDE_DOWN("slidedown"), // 向下滑动
    WIPE_LEFT("wipeleft"), // 向左擦除
    WIPE_RIGHT("wiperight"), // 向右擦除
    ZOOM_IN_FADE("zoompan"), // 放大并淡入（简化为xfade的某种变体或特定FFmpeg滤镜）
    // 可以根据 FFmpeg xfade 支持添加更多：https://ffmpeg.org/ffmpeg-filters.html#xfade
    // 例如: distance, dissolve, pixelize, radial, rectcrop, slidedown, slideleft, slideright, slideup,
    // wipedown, wipeleft, wiperight, wipeup, circlecrop, circletop, circleclose, horzclose,
    // horzopen, vertclose, vertopen, diagbl, diagbr, diagtl, diagtr, hlslice, hrslice,vuslice, vdslice
    Distance("distance"),//渐渐远去
    DISSOLVE("dissolve"),
    PIXELIZE("pixelize"),
    RANDOM("random"); // TODO:希望能支持多种不同的切换模式


    private final String ffmpegFilterName;

    TransitionType(String ffmpegFilterName) {
        this.ffmpegFilterName = ffmpegFilterName;
    }

    public String getFfmpegFilterName() {
        return ffmpegFilterName;
    }
}