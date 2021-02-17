package me.laijingzhi.short_video_app.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class FileUriPathUtils {

    public final String getUriToPath(Context context, Uri uri) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        if (cursor.moveToFirst()) {
            try {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return path;
    }

    /**
     * uri转file:
     *
     * @param uri
     */
    public final File getUriToFile(Uri uri) {
        File file = null;
        try {
            file = new File(new URI(uri.toString()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return file;

    }

    /**
     * file转uri:
     *
     * @param file
     */
    public final URI getFileToUri(File file) {
        URI uri = file.toURI();
        return uri;
    }

    /**
     * file转path:
     *
     * @param file
     */
    public final String getFileToPath(File file) {
        String path = file.getPath();
        return path;
    }


    /**
     * path转uri:
     *
     * @param path
     */
    public final Uri getPathToUri(String path) {
        Uri uri = Uri.parse(path);
        return uri;
    }

    /**
     * path转file:
     *
     * @param path
     */
    public final File getPathToFile(String path) {
        File file = new File(path);
        return file;
    }

    /**
     * Try to return the absolute file path from the given Uri
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePath( Context context,  Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null ) {
            data = uri.getPath();
        }
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
                data = uri.getPath();
            }
        else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
                Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
                if ( null != cursor ) {
                    if ( cursor.moveToFirst() ) {
                        int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                        if ( index > -1 ) {
                            data = cursor.getString( index );
                        }
                    }
                    cursor.close();
                }
            }
            return data;
        }
}
