package kiwi.smartwallet;

import android.os.Environment;

import java.io.File;

/**
 * Created by 奇異果Kiwi on 2014/12/25.
 */
public final class BaseAlbumDirFactory extends AlbumStorageDirFactory {

    // Standard storage location for digital camera files
    private static final String CAMERA_DIR = "/dcim/";

    @Override
    public File getAlbumStorageDir(String albumName) {
        return new File (
                Environment.getExternalStorageDirectory()//取得外部儲存體的根目錄，預設位置為 /mnt/sdcard
                        + CAMERA_DIR//"/dcim/"
                        + albumName//傳入值"CameraSample"
        );
    }
}