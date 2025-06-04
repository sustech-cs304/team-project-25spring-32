package com.example.pa.util.testHelper;

import android.database.Cursor;
import android.database.MatrixCursor;

public class CursorTestUtil {

    public static Cursor createAlbumCursor() {
        MatrixCursor cursor = new MatrixCursor(new String[]{
                "id", "name", "user_id", "is_auto_generated", "is_collaborative", "visibility", "created_time", "cover"
        });

        cursor.addRow(new Object[]{
                1, "测试", 1, 0, 0, "private", "2024-01-01", null
        });

        return cursor;
    }

    public static Cursor createEmptyCursor() {
        MatrixCursor cursor = new MatrixCursor(new String[]{
                "id", "name", "user_id", "is_auto_generated", "is_collaborative", "visibility", "created_time", "cover"
        });

        return cursor;
    }

    public static Cursor createCursorWithAlbumName(String name) {
        MatrixCursor cursor = new MatrixCursor(new String[]{
                "id", "name", "user_id", "is_auto_generated", "is_collaborative", "visibility", "created_time", "cover"
        });

        cursor.addRow(new Object[]{
                1, name, 1, 0, 0, "private", "2024-01-01", null
        });

        return cursor;
    }
}