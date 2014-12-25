package kiwi.smartwallet;

import java.io.File;

/**
 * Created by 奇異果Kiwi on 2014/12/25.
 */
abstract class AlbumStorageDirFactory {
    public abstract File getAlbumStorageDir(String albumName);
}
