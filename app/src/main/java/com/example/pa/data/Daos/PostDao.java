package com.example.pa.data.Daos;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.pa.data.DatabaseHelper;
import com.example.pa.data.models.Post;

import java.util.ArrayList;
import java.util.List;

public class PostDao {
    /**
     * AI-generated-content
     * tool: Deepseek
     * version: latest
     * usage: I directly copy the code from its response and modify the logic of some method, add some
     * methods we need but it did not generate, and add some logs.
     */
    public static final String TABLE_NAME = "Posts";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_IMAGE_URI = "image_uri";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_AUTHOR_ID = "author_id";
    public static final String COLUMN_GROUP_ID = "group_id";
    public static final String COLUMN_CREATED_TIME = "created_time";
    public static final String COLUMN_LIKES = "likes";
    public static final String COLUMN_COMMENTS = "comments";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_IMAGE_URI + " TEXT NOT NULL, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_CONTENT + " TEXT NOT NULL, " +
                    COLUMN_AUTHOR_ID + " INTEGER NOT NULL, " +
                    COLUMN_GROUP_ID + " INTEGER NOT NULL, " +
                    COLUMN_CREATED_TIME + " TEXT DEFAULT (datetime('now')), " +
                    COLUMN_LIKES + " INTEGER DEFAULT 0, " +
                    COLUMN_COMMENTS + " INTEGER DEFAULT 0, " +
                    "FOREIGN KEY (" + COLUMN_AUTHOR_ID + ") REFERENCES " + UserDao.TABLE_NAME + "(" + UserDao.COLUMN_ID + "), " +
                    "FOREIGN KEY (" + COLUMN_GROUP_ID + ") REFERENCES " + GroupDao.TABLE_NAME + "(" + GroupDao.COLUMN_ID + "))";

    private final SQLiteDatabase db;

    public PostDao(Context context) {
        this.db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    /**
     * 创建新帖子
     */
    public long createPost(Post post) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_IMAGE_URI, post.getImageUri());
        values.put(COLUMN_TITLE, post.getTitle());
        values.put(COLUMN_CONTENT, post.getContent());
        values.put(COLUMN_AUTHOR_ID, post.getAuthorId());
        values.put(COLUMN_GROUP_ID, post.getGroupId());

        try {
            Log.d("PostDao", "创建新帖子: " + post.getTitle());
            return db.insert(TABLE_NAME, null, values);
        } catch (SQLException e) {
            Log.e("PostDao", "创建帖子失败: " + e.getMessage());
            return -1;
        }
    }

    /**
     * 获取帖子信息
     */
    public Cursor getPost(int postId) {
        return db.query(TABLE_NAME,
                null,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(postId)},
                null, null, null);
    }

    /**
     * 更新帖子信息
     */
    public boolean updatePost(Post post) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, post.getTitle());
        values.put(COLUMN_CONTENT, post.getContent());
        values.put(COLUMN_IMAGE_URI, post.getImageUri());
        values.put(COLUMN_GROUP_ID, post.getGroupId());

        try {
            int affected = db.update(TABLE_NAME, values,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(post.getId())});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("PostDao", "更新帖子失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 删除帖子
     */
    public boolean deletePost(int postId) {
        try {
            int affected = db.delete(TABLE_NAME,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(postId)});
            return affected > 0;
        } catch (SQLException e) {
            Log.e("PostDao", "删除帖子失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取群组的所有帖子
     */
    public Cursor getGroupPosts(int groupId) {
        return db.query(TABLE_NAME,
                null,
                COLUMN_GROUP_ID + " = ?",
                new String[]{String.valueOf(groupId)},
                null, null, COLUMN_CREATED_TIME + " DESC");
    }

    /**
     * 获取用户发布的所有帖子
     */
    public Cursor getUserPosts(int userId) {
        return db.query(TABLE_NAME,
                null,
                COLUMN_AUTHOR_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, COLUMN_CREATED_TIME + " DESC");
    }

    /**
     * 检查帖子是否存在
     */
    public boolean isPostExists(int postId) {
        try (Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_ID},
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(postId)},
                null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    /**
     * 增加帖子点赞数
     */
    public boolean incrementLikes(int postId) {
        try {
            db.execSQL("UPDATE " + TABLE_NAME +
                    " SET " + COLUMN_LIKES + " = " + COLUMN_LIKES + " + 1" +
                    " WHERE " + COLUMN_ID + " = ?",
                    new String[]{String.valueOf(postId)});
            return true;
        } catch (SQLException e) {
            Log.e("PostDao", "增加点赞数失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 增加帖子评论数
     */
    public boolean incrementComments(int postId) {
        try {
            db.execSQL("UPDATE " + TABLE_NAME +
                    " SET " + COLUMN_COMMENTS + " = " + COLUMN_COMMENTS + " + 1" +
                    " WHERE " + COLUMN_ID + " = ?",
                    new String[]{String.valueOf(postId)});
            return true;
        } catch (SQLException e) {
            Log.e("PostDao", "增加评论数失败: " + e.getMessage());
            return false;
        }
    }

    // 辅助方法：Cursor转Post对象
    @SuppressLint("Range")
    public Post cursorToPost(Cursor cursor) {
        Post post = new Post();
        post.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
        post.setAuthorId(cursor.getInt(cursor.getColumnIndex(COLUMN_AUTHOR_ID)));
        post.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
        post.setContent(cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)));
        post.setImageUri(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URI)));
        post.setCreatedTime(cursor.getString(cursor.getColumnIndex(COLUMN_CREATED_TIME)));
        post.setGroupId(cursor.getInt(cursor.getColumnIndex(COLUMN_GROUP_ID)));
        return post;
    }
} 