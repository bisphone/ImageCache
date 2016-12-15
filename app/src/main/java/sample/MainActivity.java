package sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import imageCache.ImageCacheManager;
import pourvatan.shayan.imagecache.R;

public class MainActivity extends AppCompatActivity {


    public static final String CACHE_MAIN = "mainCache";
    public static final String CACHE_DETAIL = "detailCache";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {

            // based on documentation
            long maxCacheSize = Runtime.getRuntime().maxMemory() / 1024 / 8;

            /**
             * addCustomCache is better to put in application class.
             * otherwise you MUST put it in onCreate of first activity
             * you need put in 'if (savedInstanceState == null)' to prevent add cache in orientation changed.
             */
            /*
             *
             * you must first add your cache box with your size desire
             * if you have multiple size for one image you must pass last argument as TRUE.
             * consider you want to show one image in multiple imageView with multiple size,
             * like : 64dp and match_parent and ...
             *
             */
            ImageCacheManager.getInstance().addCustomCache(CACHE_MAIN, maxCacheSize / 3, false);
            ImageCacheManager.getInstance().addCustomCache(CACHE_DETAIL, maxCacheSize / 4, true);

        }


        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                android.R.drawable.ic_delete);

        Bitmap alert = BitmapFactory.decodeResource(getResources(),
                android.R.drawable.ic_dialog_alert);


        // store bitmap in mainCache
        ImageCacheManager.getInstance().storeBitmapImage(CACHE_MAIN,
                String.valueOf(android.R.drawable.ic_delete),
                icon);

        // store bitmap with 64x64 dimension in detailCache
        ImageCacheManager.getInstance().storeBitmapImage(CACHE_DETAIL,
                String.valueOf(android.R.drawable.ic_dialog_alert),
                alert,
                64,
                64);


        /*
         * sometimes you need to change key of persisted image in cache,
         * consider you need to upload image to the server,
         * you generate image with System.currentTime key, after image uploaded to the server,
         * server generate newKey for your image and you must work with the new key,
         * as you don't want to load your image one more time, you can use following method to change persisted bitmap key.
         */
        ImageCacheManager.getInstance().changeKeyOfBitmapImage(CACHE_DETAIL,
                String.valueOf(android.R.drawable.ic_dialog_alert),
                "newKey");


        // load images from cache
        Bitmap detailsBitmap = ImageCacheManager.getInstance().getBitmapImage(CACHE_DETAIL, "newKey", 64, 64);
        Bitmap mainBitmap = ImageCacheManager.getInstance().getBitmapImage(CACHE_MAIN, String.valueOf(android.R.drawable.ic_delete));

        ImageView ivMain = (ImageView) findViewById(R.id.iv_main);
        ImageView ivSub = (ImageView) findViewById(R.id.iv_sub);

        ivMain.setImageBitmap(mainBitmap);
        ivSub.setImageBitmap(detailsBitmap);


    }
}
