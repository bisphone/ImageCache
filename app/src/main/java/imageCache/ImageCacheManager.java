package imageCache;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by shayan on 11/16/16.
 */

public class ImageCacheManager {
    HashMap<String, ImageCacheModel> imageCacheHashMap;
    private ImageCacheLog imageCacheLog;

    private static ImageCacheManager imageCacheManager;
    private long sumSize;
    private long maxCacheSize;

    //region init imageCacheManager.

    public static ImageCacheManager getInstance() {
        if (imageCacheManager == null) {
            synchronized (ImageCacheManager.class) {
                if (imageCacheManager == null) {
                    imageCacheManager = new ImageCacheManager();
                }

            }
        }
        return imageCacheManager;
    }

    private ImageCacheManager() {
        imageCacheHashMap = new HashMap<>();
        imageCacheLog = ImageCacheLog.getInstance();
        imageCacheLog.setLogEnable(true);

        /**
         *  based on {@see <a href="https://developer.android.com/training/displaying-bitmaps/cache-bitmap.html#memory-cache">Document</a>}
         */
        maxCacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024) / 8;
    }

    //endregion

    //region private method

    private ImageCacheModel getCacheIfExist(String cacheKey) {
        ImageCacheModel imageCacheModel = imageCacheHashMap.get(cacheKey);
        if (imageCacheModel == null) {
            throw new IllegalArgumentException("Cache with key = " + cacheKey + " is not exists." +
                    "\nyou can add your custom cache with ImageCacheManager#getInstance#addCustomCache");
        }
        return imageCacheModel;
    }

    //endregion

    //region public method

    public void setLogEnable(boolean logEnable) {
        imageCacheLog.setLogEnable(logEnable);
    }

    //region add to imageCacheManager by key

    /**
     * @param cacheKey  is name of cache dataSource.
     * @param sizeInK   is size of dataSource
     * @param sizeAware did you have multiple value with same key ? set it true, if you don't have it set it to false.
     */
    public void addCustomCache(String cacheKey, long sizeInK, boolean sizeAware) {

        synchronized (this) {
            if (imageCacheHashMap.get(cacheKey) != null) {
                throw new IllegalArgumentException("Key is already exists, you need to add another key if you want");
            }

            sumSize += sizeInK;
            if (sumSize > maxCacheSize) {
                imageCacheLog.w("You make cache size more than document tutorial");
            }

            ImageCacheModel imageCacheModel = new ImageCacheModel(sizeInK, sizeAware);
            imageCacheHashMap.put(cacheKey, imageCacheModel);
        }
    }

    //endregion

    //region store bitmap
    public boolean storeBitmapImage(String cacheKey, String imageKey, Bitmap bitmap) {
        return storeBitmapImage(cacheKey, imageKey, bitmap, null, null);
    }

    public boolean storeBitmapImage(String cacheKey, String imageKey, Bitmap bitmap, float width, float height) {
        return storeBitmapImage(cacheKey, imageKey, bitmap, Float.valueOf(width), Float.valueOf(height));
    }

    /*
     * if you care on size you must pass width or height value.
     * if those is null we don't care of size and we return first value that start with key.
     */
    private boolean storeBitmapImage(String cacheKey, String imageKey, Bitmap bitmap, Float width, Float height) {
        ImageCacheModel imageCacheModel = getCacheIfExist(cacheKey);
        return imageCacheModel.storeBitmapImage(imageKey, bitmap, width, height);
    }

    //endregion

    //region retrieve bitmap

    public Bitmap getBitmapImage(String cacheKey, String imageKey) {
        return getBitmapImage(cacheKey, imageKey, null, null);
    }

    public Bitmap getBitmapImage(String cacheKey, String imageKey, float width, float height) {
        return getBitmapImage(cacheKey, imageKey, Float.valueOf(width), Float.valueOf(height));
    }

    private Bitmap getBitmapImage(String cacheKey, String imageKey, Float width, Float height) {
        ImageCacheModel imageCacheModel = getCacheIfExist(cacheKey);
        return imageCacheModel.getBitmapImage(imageKey, width, height);
    }

    //endregion

    //region change key of value

    public boolean changeKeyOfBitmapImage(String cacheKey, String oldKey, String newKey) {
        ImageCacheModel imageCacheModel = getCacheIfExist(cacheKey);
        return imageCacheModel.changeKeyOfBitmapImage(oldKey, newKey);
    }

    //endregion

    //endregion


}
