package imageCache;

import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by shayan on 11/16/16.
 */

final class ImageCacheModel {


    //current size of cache
    private long currentSize;

    //maximum size of cache
    private long maxSize;

    //use for better performance for applications that don't have image with multiple size,
    // because we iterate through imageCache when you pass null for width and height.
    private boolean sizeAware;

    //main dataSource
    private LinkedHashMap<String, Bitmap> imageCacheMap;

    // separator of image key with width and height of image
    private final String imageKeySeparators = "_/_/_";

    //region init model

    ImageCacheModel(long sizeInK, boolean sizeAware) {
        this.maxSize = sizeInK * 1024;
        this.sizeAware = sizeAware;
        imageCacheMap = new LinkedHashMap<>(0, 0.75f, true);
    }

    //endregion

    //region private method

    /**
     * set width and height of image to -1 if user not set that.
     *
     * @return add separator between main parts and return the value.
     */
    private String getKeyOfImage(String imageKey, Float width, Float height) {
        if ((width != null && width < 0) || (height != null && height < 0)) {
            throw new IllegalArgumentException("width or height is lower than 0 width=" + width + " , height=" + height);
        }

        if (!sizeAware) {
            return imageKey;
        }

        if (width == null) {
            width = -1f;
        }

        if (height == null) {
            height = -1f;
        }

        return imageKey + imageKeySeparators + width + imageKeySeparators + height;


    }

    /**
     * remove images to make dataSource size lower than maxSize.
     */
    private void trimToSize() {

        if (currentSize <= maxSize) {
            return;
        }

        while (true) {

            String key;
            Bitmap value;
            synchronized (this) {
                if (currentSize < 0 || (imageCacheMap.isEmpty() && currentSize != 0)) {
                    throw new IllegalStateException(getClass().getName()
                            + ".sizeOf() is reporting inconsistent results!");
                }

                if (currentSize <= maxSize) {
                    break;
                }

                Map.Entry<String, Bitmap> toEvict = imageCacheMap.entrySet().iterator().next();
                if (toEvict == null) {
                    break;
                }
                key = toEvict.getKey();
                value = toEvict.getValue();
                imageCacheMap.remove(key);
                currentSize -= sizeOf(value);
            }
        }

    }

    /**
     * @param bitmap : bitmap that you want to insert into cache.
     * @return size of bitmap.
     */
    private long sizeOf(Bitmap bitmap) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount() / 1024;
        }

        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
    }

    //endregion

    //region store

    /**
     * throw IllegalArgumentException if you pass width and height with not null value and set sizeAware to false
     *
     * @return false if imageKey is exists,
     * true if bitmap stored in dataSource
     */
    boolean storeBitmapImage(String imageKey, Bitmap bitmap, Float width, Float height) {

        if (!sizeAware && (width != null || height != null)) {
            throw new IllegalArgumentException("you define sizeAware to false so you must pass width and height with Null value for better performance\n" +
                    "if you have multiple size of same key you need set sizeAware to true.");
        }

        String key = getKeyOfImage(imageKey, width, height);

        synchronized (this) {
            long bitmapSize = 0;
            if (!imageCacheMap.containsKey(key)) {
                bitmapSize = sizeOf(bitmap);
            }
            imageCacheMap.put(key, bitmap);
            currentSize += bitmapSize;
            trimToSize();
            return true;
        }
    }

    //endregion

    //region retrieve

    /**
     * @param imageKey is key of image that we persist.
     * @param width    is width of image or can be null if you don't care of image size or you don't have multiple size of same image.
     * @param height   same as width
     * @return if you pass null for width and height we iterate through imageCache and return first candidate
     */
    Bitmap getBitmapImage(String imageKey, @Nullable Float width, @Nullable Float height) {
        synchronized (this) {
            if (width == null && height == null) {

                if (!sizeAware) {
                    return getBitmapImage(imageKey);
                }

                for (String cacheKey : imageCacheMap.keySet()) {
                    if (cacheKey.matches(imageKey + imageKeySeparators + "\\d+" + imageKeySeparators + "\\d+")) {
                        return getBitmapImage(cacheKey);
                    }
                }
                return null;
            }


            String key = getKeyOfImage(imageKey, width, height);
            return getBitmapImage(key);
        }
    }

    private Bitmap getBitmapImage(String imageKey) {
        return imageCacheMap.get(imageKey);
    }

    //endregion

    //region changeKey

    /**
     * change all key that stored with oldKey to newKey.
     */
    boolean changeKeyOfBitmapImage(String oldKey, String newKey) {

        synchronized (this) {

            // don't need to iterate, key exists with oldKey.
            if (!sizeAware) {
                if (imageCacheMap.containsKey(oldKey)) {
                    imageCacheMap.put(newKey, imageCacheMap.get(oldKey));
                    imageCacheMap.remove(oldKey);
                }
                return true;
            }

            HashMap<String, Bitmap> copyIncomingMap = new HashMap<>(imageCacheMap);
            for (String cacheKey : copyIncomingMap.keySet()) {

                if (cacheKey.matches(oldKey + imageKeySeparators + "-?\\d+\\.\\d+" + imageKeySeparators + "-?\\d+\\.\\d+")) {
                    imageCacheMap.put(cacheKey.replace(oldKey, newKey), imageCacheMap.get(cacheKey));
                    imageCacheMap.remove(cacheKey);
                }
            }
            return true;
        }

    }

    //endregion

}
