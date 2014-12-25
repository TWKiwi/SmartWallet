package kiwi.smartwallet;

import android.os.Environment;

import java.io.File;

/**
 * Created by 奇異果Kiwi on 2014/12/25.
 */
public class FroyoAlbumDirFactory extends AlbumStorageDirFactory{
    @Override
    public File getAlbumStorageDir(String albumName) {
        // TODO Auto-generated method stub
        return new File(
                Environment.getExternalStoragePublicDirectory(//取得外部儲存體存放公開檔案的目錄
                        Environment.DIRECTORY_PICTURES
                ),
                albumName
        );
    }
}
