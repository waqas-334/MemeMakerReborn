
package com.androidbull.meme.maker.helper;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

import static com.androidbull.meme.maker.helper.ConstantsKt.APP_NAME;
import static com.androidbull.meme.maker.helper.ConstantsKt.All_Memes_STORAGE_FOLDER;
import static com.androidbull.meme.maker.helper.ConstantsKt.CUSTOM_FONT_STORAGE_FOLDER;
import static com.androidbull.meme.maker.helper.ConstantsKt.TEMPLATES_STORAGE_FOLDER;


public class StorageHelper {

    public static boolean isExternalStorageWriteable() {
        return Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED);
    }

    public static boolean isExternalStorageReadable() {
        return Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ||
                Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED_READ_ONLY);

    }

    public static File getExternalCacheDir() {
        File dir = AppContext.getInstance().getContext().getExternalCacheDir();
        if (!dir.exists())
            dir.mkdirs();
        return dir;
    }

    public static void clearCacheDirs() {
        File[] externalCacheDir = getExternalCacheDir().listFiles();
        if (externalCacheDir != null) {
            for (File file : externalCacheDir) {
                file.delete();
            }
        }
    }

    public static String getFontsPrivateDir() {
        return AppContext.getInstance().getContext().getExternalFilesDir(null)
                + File.separator
                + APP_NAME
                + File.separator
                + CUSTOM_FONT_STORAGE_FOLDER
                + File.separator;
    }

    public static String getTemplatesPrivateDir() {
        return AppContext.getInstance().getContext().getExternalFilesDir(null)
                + File.separator
                + APP_NAME
                + File.separator
                + TEMPLATES_STORAGE_FOLDER
                + File.separator;
    }

    public static String getMemesPrivateDir() {

        return AppContext.getInstance().getContext().getExternalFilesDir(null)
                + File.separator
                + APP_NAME
                + File.separator
                + All_Memes_STORAGE_FOLDER
                + File.separator;
    }


  /*  public static File getAttachmentDir(Context mContext) {
        return mContext.getExternalFilesDir(null);
    }


    *//**
     * Retrieves the folderwhere to store data to sync notes
     *
     * @param mContext
     * @return
     *//*
    public static File getDbSyncDir(Context mContext) {
        File extFilesDir = mContext.getExternalFilesDir(null);
        File dbSyncDir = new File(extFilesDir, Constants.APP_STORAGE_DIRECTORY_SB_SYNC);
        dbSyncDir.mkdirs();
        if (dbSyncDir.exists() && dbSyncDir.isDirectory()) {
            return dbSyncDir;
        } else {
            return null;
        }
    }


    *//**
     * Create a path where we will place our private file on external
     *
     * @param mContext
     * @param uri
     * @return
     *//*
    public static File createExternalStoragePrivateFile(Context mContext, Uri uri, String extension) {

        // Checks for external storage availability
        if (!checkStorage()) {
            Toast.makeText(mContext, mContext.getString(R.string.storage_not_available), Toast.LENGTH_SHORT).show();
            return null;
        }
        File file = createNewAttachmentFile(mContext, extension);

        InputStream is;
        OutputStream os;
        try {
            is = mContext.getContentResolver().openInputStream(uri);
            os = new FileOutputStream(file);
            copyFile(is, os);
        } catch (IOException e) {
            try {
                is = new FileInputStream(FileHelper.getPath(mContext, uri));
                os = new FileOutputStream(file);
                copyFile(is, os);
                // It's a path!!
            } catch (NullPointerException e1) {
                try {
                    is = new FileInputStream(uri.getPath());
                    os = new FileOutputStream(file);
                    copyFile(is, os);
                } catch (FileNotFoundException e2) {
                    Log.e(Constants.TAG, "Error writing " + file, e2);
                    file = null;
                }
            } catch (FileNotFoundException e2) {
                Log.e(Constants.TAG, "Error writing " + file, e2);
                file = null;
            }
        }
        return file;
    }


    public static boolean copyFile(File source, File destination) {
        try {
            return copyFile(new FileInputStream(source), new FileOutputStream(destination));
        } catch (FileNotFoundException e) {
            Log.e(Constants.TAG, "Error copying file", e);
            return false;
        }
    }


    *//**
     * Generic file copy method
     *
     * @param is Input
     * @param os Output
     * @return True if copy is done, false otherwise
     *//*
    public static boolean copyFile(InputStream is, OutputStream os) {
        boolean res = false;
        byte[] data = new byte[1024];
        int len;
        try {
            while ((len = is.read(data)) > 0) {
                os.write(data, 0, len);
            }
            is.close();
            os.close();
            res = true;
        } catch (IOException e) {
            Log.e(Constants.TAG, "Error copying file", e);
        }
        return res;
    }


    public static boolean deleteExternalStoragePrivateFile(Context mContext, String name) {
        boolean res = false;

        // Checks for external storage availability
        if (!checkStorage()) {
            Toast.makeText(mContext, mContext.getString(R.string.storage_not_available), Toast.LENGTH_SHORT).show();
            return false;
        }

        File file = new File(mContext.getExternalFilesDir(null), name);
        file.delete();

        return true;
    }


    public static boolean delete(Context mContext, String name) {
        boolean res = false;

        // Checks for external storage availability
        if (!checkStorage()) {
            Toast.makeText(mContext, mContext.getString(R.string.storage_not_available), Toast.LENGTH_SHORT).show();
            return false;
        }

        File file = new File(name);
        if (file.isFile()) {
            res = file.delete();
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file2 : files) {
                res = delete(mContext, file2.getAbsolutePath());
            }
            res = file.delete();
        }

        return res;
    }


    public static String getRealPathFromURI(Context mContext, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = mContext.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) {
            return null;
        }
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    public static File createNewAttachmentFile(Context mContext, String extension) {
        File f = null;
        if (checkStorage()) {
            f = new File(mContext.getExternalFilesDir(null), createNewAttachmentName(extension));
        }
        return f;
    }


    public static synchronized String createNewAttachmentName(String extension) {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_SORTABLE);
        String name = sdf.format(now.getTime());
        name += extension != null ? extension : "";
        return name;
    }


    public static File createNewAttachmentFile(Context mContext) {
        return createNewAttachmentFile(mContext, null);
    }


    *//**
     * Create a path where we will place our private file on external
     *
     * @param mContext
     * @param uri
     * @return
     *//*
    public static File copyToBackupDir(File backupDir, File file) {

        // Checks for external storage availability
        if (!checkStorage()) {
            return null;
        }

        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        File destination = new File(backupDir, file.getName());

        try {
            copyFile(new FileInputStream(file), new FileOutputStream(destination));
        } catch (FileNotFoundException e) {
            Log.e(Constants.TAG, "Error copying file to backup", e);
            destination = null;
        }

        return destination;
    }


    public static File getCacheDir(Context mContext) {
        File dir = mContext.getExternalCacheDir();
        if (!dir.exists())
            dir.mkdirs();
        return dir;
    }


    public static File getExternalStoragePublicDir() {
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.EXTERNAL_STORAGE_FOLDER + File
                .separator);
        if (!dir.exists())
            dir.mkdirs();
        return dir;
    }


    public static File getBackupDir(String backupName) {
        File backupDir = new File(getExternalStoragePublicDir(), backupName);
        if (!backupDir.exists())
            backupDir.mkdirs();
        return backupDir;
    }


    public static File getSharedPreferencesFile(Context mContext) {
        File appData = mContext.getFilesDir().getParentFile();
        String packageName = mContext.getApplicationContext().getPackageName();
        return new File(appData
                + System.getProperty("file.separator")
                + "shared_prefs"
                + System.getProperty("file.separator")
                + packageName
                + "_preferences.xml");
    }


    *//**
     * Returns a directory size in bytes
     *//*
    @SuppressWarnings("deprecation")
    public static long getSize(File directory) {
        StatFs statFs = new StatFs(directory.getAbsolutePath());
        long blockSize = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFs.getBlockSizeLong();
            } else {
                blockSize = statFs.getBlockSize();
            }
            // Can't understand why on some devices this fails
        } catch (NoSuchMethodError e) {
            Log.e(Constants.TAG, "Mysterious error", e);
        }
        return getSize(directory, blockSize);
    }


    private static long getSize(File directory, long blockSize) {
        File[] files = directory.listFiles();
        if (files != null) {

            // space used by directory itself 
            long size = directory.length();

            for (File file : files) {
                if (file.isDirectory()) {
                    // space used by subdirectory
                    size += getSize(file, blockSize);
                } else {
                    // file size need to rounded up to full block sizes
                    // (not a perfect function, it adds additional block to 0 sized files
                    // and file who perfectly fill their blocks) 
                    size += (file.length() / blockSize + 1) * blockSize;
                }
            }
            return size;
        } else {
            return 0;
        }
    }


    public static boolean copyDirectory(File sourceLocation, File targetLocation) {
        boolean res = true;

        // If target is a directory the method will be iterated
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {
                res = res && copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation,
                        children[i]));
            }

            // Otherwise a file copy will be performed
        } else {
            try {
                res = res && copyFile(new FileInputStream(sourceLocation), new FileOutputStream(targetLocation));
            } catch (FileNotFoundException e) {
                Log.e(Constants.TAG, "Error copying directory");
                res = false;
            }
        }
        return res;
    }


    *//**
     * Retrieves uri mime-type using ContentResolver
     *
     * @param mContext
     * @param uri
     * @return
     *//*
    public static String getMimeType(Context mContext, Uri uri) {
        ContentResolver cR = mContext.getContentResolver();
        String mimeType = cR.getType(uri);
        if (mimeType == null) {
            mimeType = getMimeType(uri.toString());
        }
        return mimeType;
    }


    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }


    *//**
     * Retrieves uri mime-type between the ones managed by application
     *
     * @param mContext
     * @param uri
     * @return
     *//*
    public static String getMimeTypeInternal(Context mContext, Uri uri) {
        String mimeType = getMimeType(mContext, uri);
        mimeType = getMimeTypeInternal(mContext, mimeType);
        return mimeType;
    }


    *//**
     * Retrieves mime-type between the ones managed by application from given string
     *
     * @param mContext
     * @param mimeType
     * @return
     *//*
    public static String getMimeTypeInternal(Context mContext, String mimeType) {
        if (mimeType != null) {
            if (mimeType.contains("image/")) {
                mimeType = Constants.MIME_TYPE_IMAGE;
            } else if (mimeType.contains("audio/")) {
                mimeType = Constants.MIME_TYPE_AUDIO;
            } else if (mimeType.contains("video/")) {
                mimeType = Constants.MIME_TYPE_VIDEO;
            } else {
                mimeType = Constants.MIME_TYPE_FILES;
            }
        }
        return mimeType;
    }


    *//**
     * Creates a new attachment file copying data from source file
     *
     * @param mContext
     * @param uri
     * @return
     *//*
    public static Attachment createAttachmentFromUri(Context mContext, Uri uri) {
        return createAttachmentFromUri(mContext, uri, false);
    }


    *//**
     * Creates a fiile to be used as attachment.
     *//*
    public static Attachment createAttachmentFromUri(Context mContext, Uri uri, boolean moveSource) {
        String name = FileHelper.getNameFromUri(mContext, uri);
        String extension = FileHelper.getFileExtension(FileHelper.getNameFromUri(mContext, uri)).toLowerCase(
                Locale.getDefault());
        File f;
        if (moveSource) {
            f = createNewAttachmentFile(mContext, extension);
            try {
                FileUtils.moveFile(new File(uri.getPath()), f);
            } catch (IOException e) {
                Log.e(Constants.TAG, "Can't move file " + uri.getPath());
            }
        } else {
            f = StorageHelper.createExternalStoragePrivateFile(mContext, uri, extension);
        }
        Attachment mAttachment = null;
        if (f != null) {
            mAttachment = new Attachment(Uri.fromFile(f), StorageHelper.getMimeTypeInternal(mContext, uri));
            mAttachment.setName(name);
            mAttachment.setSize(f.length());
        }
        return mAttachment;
    }


    *//**
     * Creates new attachment from web content
     *
     * @param mContext
     * @param url
     * @return
     * @throws IOException
     *//*
    public static File createNewAttachmentFileFromHttp(Context mContext, String url)
            throws IOException {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return getFromHttp(url, createNewAttachmentFile(mContext, FileHelper.getFileExtension(url)));
    }


    *//**
     * Retrieves a file from its web url
     *
     * @param url
     * @return
     * @throws IOException
     *//*
    public static File getFromHttp(String url, File file) throws IOException {
        URL imageUrl = new URL(url);
        // HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
        // conn.setConnectTimeout(30000);
        // conn.setReadTimeout(30000);
        // conn.setInstanceFollowRedirects(true);
        // InputStream is=conn.getInputStream();
        // OutputStream os = new FileOutputStream(f);
        // Utils.CopyStream(is, os);

        // File file = File.createTempFile("img", ".jpg");

        FileUtils.copyURLToFile(imageUrl, file);
        // os.close();
        return file;
    }*/


}