package kiwi.smartwallet;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ViewPagerChargeActivity extends ActionBarActivity {


    private ViewPager viewPager;//頁卡內容
    private ImageView imageView;//動畫圖片
    private TextView textView1,textView2,textView3;
    private List<View> views;// Tab頁面列表
    private int offset = 0;// 動畫圖片偏移量
    private int currIndex = 0;// 當前頁卡編號
    private int bmpW;// 動畫圖片寬度
    private View view1,view2,view3;//各個頁卡


    private static final int ACTION_TAKE_PHOTO_B = 1;
    private static final int ACTION_TAKE_PHOTO_S = 2;

    private static final String BITMAP_STORAGE_KEY = "viewbitmap";
    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
    private ImageView mImageView;
    private CalendarView calenderView;
    public static String CALENDAR_DATE;
    private Bitmap mImageBitmap;

    private static final String VIDEO_STORAGE_KEY = "viewvideo";
    private static final String VIDEOVIEW_VISIBILITY_STORAGE_KEY = "videoviewvisibility";
    private Uri mVideoUri;

    private String mCurrentPhotoPath;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

//==============取得名字=============//
    private String getAlbumName() {
        return getString(R.string.album_name);
    }
//==============給予資料夾路徑及判斷是否已存在此資料夾，若不存在則建立=============//
    private File getAlbumDir() {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
            if (storageDir != null) {//如果storageDir有值
                if (! storageDir.mkdirs()) {//如果尚無storageDir目錄
                    if (! storageDir.exists()){

                        Log.d("CameraSample", "未能創建目錄");

                        return null;
                    }
                }
            }
        } else {
            Log.v(getString(R.string.app_name), "外部存儲設備未安裝讀/寫。");
        }
        return storageDir;//回傳儲存文件
    }
//============產生圖片的(日期)檔名，回傳檔名==================//
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        /*照片檔名會等於IMG_yyyyMMdd_HHmmss_*/
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);//createTempFile(創一個空文件(前綴，後綴，指定目錄路徑))
        return imageF;
    }

    private File setUpPhotoFile() throws IOException {
        File f = createImageFile();
        /*調用 createImageFile()方法，建檔*/
        mCurrentPhotoPath = f.getAbsolutePath();
        /*getPath()得到的是構造file的時候的路徑。
          getAbsolutePath()得到的是全路徑
          如果構造的時候就是全路徑那直接返回全路徑
          如果構造的時候試相對路徑，返回當前目錄的路徑+構造file時候的路徑
          http://www.blogjava.net/dreamstone/archive/2007/08/08/134968.html*/
        return f;
    }
//=======
    private void setPic() {
        /* 得到的ImageView的大小 */
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

		/* 獲得的Image的大小 */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        /*在通過BitmapFactory.decodeFile(String path)方法將圖片轉成Bitmap時，遇
        到大一些的圖片，我們經常會遇到OOM(Out Of Memory)的問題。如果我們把它設為true
        ，那麼BitmapFactory.decodeFile(String path, Options opt)並不會真的返
        回一個Bitmap給你，它僅僅會把它的寬，高取回來給你，這樣就不會佔用太多的記憶體，也
        就不會那麼頻繁的發生OOM了。*/
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        /*轉成轉成Bitmap(點陣圖)時，的長寬*/

		/* 找出哪種方式需要降低更少 */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
            /*Math.min(X,Y)兩者取最小*/
        }
        /*更多略縮圖資料 : http://www.tuicool.com/articles/FNvmumJ*/
		/* 設置選項位圖縮放圖像解碼目標 */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        /*inSampleSize，假設原圖是1500x700的，我們給縮略圖留出的空間是100x100的。
        那麼inSampleSize=min(1500/100, 700/100)=7。我們可以得到的縮略圖是原圖的1/7。
        這裡如果你要問15:7的圖片怎麼顯示到1:1的區域內，請去看ImageView的scaleType屬性。
        但是事實沒有那麼完美，雖然設置了inSampleSize=7，但是得到的縮略圖卻是原圖的1/4，
        原因是inSampleSize只能是2的整數次冪，如果不是的話，向下取得最大的2的整數次冪，
        7向下尋找2的整數次冪，就是4。*/
        bmOptions.inPurgeable = true;

		/* 解碼JPEG文件轉換成bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);//decodeFile(文件路徑, bmOptions)
        /*解碼文件路徑成點陣圖，假如路徑為空值或者該物件無法被轉為點陣圖，則return null*/

        mImageView.setImageBitmap(bitmap);
        /*將點陣圖資料留傳給mImageView*/

        mImageView.setVisibility(View.VISIBLE);
        /*顯示mImageView*/

    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        switch(actionCode) {
            case ACTION_TAKE_PHOTO_B:
                File f = null;

                try {
                    f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                }
                break;

            default:
                break;
        } // switch

        startActivityForResult(takePictureIntent, actionCode);
    }



    private void handleSmallCameraPhoto(Intent intent) {
        Bundle extras = intent.getExtras();
        mImageBitmap = (Bitmap) extras.get("data");
        mImageView.setImageBitmap(mImageBitmap);

        mImageView.setVisibility(View.VISIBLE);

    }
    private void handleBigCameraPhoto() {
        if (mCurrentPhotoPath != null) {
            setPic();
            galleryAddPic();
            mCurrentPhotoPath = null;
        }
    }
    Button.OnClickListener mTakePicOnClickListener = new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
                }
            };
    Button.OnClickListener mTakePicSOnClickListener = new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_S);
                }
            };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager_charge);


        mImageBitmap = null;

        setTitle("記帳功能");
        InitImageView();
        InitTextView();
        InitViewPager();

//======================拍照部門=========================//
        Button cameraBtn = (Button)this.view2.findViewById(R.id.button3);
        setBtnListenerOrDisable( cameraBtn, mTakePicSOnClickListener, MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
//======================月曆部門========================//

        calenderView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                Toast.makeText(getApplicationContext(), ""+dayOfMonth, Toast.LENGTH_LONG).show();
                CALENDAR_DATE = Integer.toString(year) + Integer.toString(month + 1) + Integer.toString(dayOfMonth);
                Intent intent = new Intent(ViewPagerChargeActivity.this,The_Date_Expenses.class);
                startActivity(intent);
            }
        });

//============================================================//

    }

    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void setBtnListenerOrDisable( Button btn, Button.OnClickListener onClickListener, String intentName) {
        if (isIntentAvailable(this, intentName)) {
            btn.setOnClickListener(onClickListener);
        } else {
            btn.setText( getText(R.string.cannot).toString() + " " + btn.getText());
            btn.setClickable(false);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_TAKE_PHOTO_B: {
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                }
                break;
            } // ACTION_TAKE_PHOTO_B

            case ACTION_TAKE_PHOTO_S: {
                if (resultCode == RESULT_OK) {
                    handleSmallCameraPhoto(data);
                }
                break;
            } // ACTION_TAKE_PHOTO_S
        } // switch
    }


    private void InitViewPager() {
        viewPager=(ViewPager) findViewById(R.id.vPager);
        views=new ArrayList<View>();
        LayoutInflater inflater=getLayoutInflater();
        view1=inflater.inflate(R.layout.activity_charge, null);
        calenderView = (CalendarView)view1.findViewById(R.id.calendarView);
        /*inflate有填充的意思*/
        /*把activity_charge的layout布局給view1*/
        view2=inflater.inflate(R.layout.activity_in_camera, null);
        /*同view1，同理*/
        mImageView = (ImageView)view2.findViewById(R.id.imageView3);
        /*把view2的imageView3給mImageView。
        activity_in_camera的布局要先給view2，
        view2才有imageView3*/
        view3=inflater.inflate(R.layout.activity_test, null);
        /*同view1，同理*/
        views.add(view1);
        views.add(view2);
        views.add(view3);
        /*把view1、2、3都放進views這個大家庭裡*/
        viewPager.setAdapter(new MyViewPagerAdapter(views));
        /*再把views這大家庭交給viewPager*/
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }
    /**
     *  初始化頭標
     **/

    private void InitTextView() {
        textView1 = (TextView) findViewById(R.id.text1);
        textView2 = (TextView) findViewById(R.id.text2);
        textView3 = (TextView) findViewById(R.id.text3);

        textView1.setOnClickListener(new MyOnClickListener(0));
        textView2.setOnClickListener(new MyOnClickListener(1));
        textView3.setOnClickListener(new MyOnClickListener(2));
    }

    /**
     * 初始化動畫，就是頁卡滑動時，下面的橫線也滑動的效果，在這裡需要計算一些數據
     **/

    private void InitImageView() {
        imageView= (ImageView) findViewById(R.id.cursor);
        bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.a).getWidth();// 獲取圖片寬度
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 獲取分辨率寬度
        offset = (screenW / 3 - bmpW) / 2;// 計算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        imageView.setImageMatrix(matrix);// 設置動畫初始位置
    }
    //<img src="http://img.my.csdn.net/uploads/201211/10/1352554452_1685.jpg" alt="">
    /**
     *
     * 頭標點擊監聽
     **/
    private class MyOnClickListener implements View.OnClickListener {
        private int index=0;
        public MyOnClickListener(int i){
            index=i;
        }
        public void onClick(View v) {
            viewPager.setCurrentItem(index);
        }

    }

    public class MyViewPagerAdapter extends PagerAdapter{
        private List<View> mListViews;

        public MyViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)   {
            Log.d("ViewPagerChargeActivity", "Remove view "+ position);
            container.removeView(mListViews.get(position));
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.d("ViewPagerChargeActivity", "Init view "+ position);
            container.addView(mListViews.get(position), 0);
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return  mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0==arg1;
        }
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        int one = offset * 2 + bmpW;// 頁卡1 -> 頁卡2 偏移量
        int two = one * 2;// 頁卡1 -> 頁卡3 偏移量
        public void onPageScrollStateChanged(int arg0) {


        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {


        }

        public void onPageSelected(int arg0) {
            /*兩種方法這是第一種，比較麻煩
            Animation animation = null;
            switch (arg0) {
            case 0:
                if (currIndex == 1) {
                    animation = new TranslateAnimation(one, 0, 0, 0);
                } else if (currIndex == 2) {
                    animation = new TranslateAnimation(two, 0, 0, 0);
                }
                break;
            case 1:
                if (currIndex == 0) {
                    animation = new TranslateAnimation(offset, one, 0, 0);
                } else if (currIndex == 2) {
                    animation = new TranslateAnimation(two, one, 0, 0);
                }
                break;
            case 2:
                if (currIndex == 0) {
                    animation = new TranslateAnimation(offset, two, 0, 0);
                } else if (currIndex == 1) {
                    animation = new TranslateAnimation(one, two, 0, 0);
                }
                break;

            }
            */
            Animation animation = new TranslateAnimation(one*currIndex, one*arg0, 0, 0);//第二種比較簡單，只有一行
            currIndex = arg0;
            animation.setFillAfter(true);// True:圖片停在動畫結束位置
            animation.setDuration(300);
            imageView.startAnimation(animation);
            //Toast.makeText(ViewPagerChargeActivity.this, "您選擇了"+ viewPager.getCurrentItem()+"頁卡", Toast.LENGTH_SHORT).show();
        }

    }

    // Some lifecycle callbacks so that the image can survive orientation change
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
        outState.putParcelable(VIDEO_STORAGE_KEY, mVideoUri);
        outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
        outState.putBoolean(VIDEOVIEW_VISIBILITY_STORAGE_KEY, (mVideoUri != null) );
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
        mVideoUri = savedInstanceState.getParcelable(VIDEO_STORAGE_KEY);
        mImageView.setImageBitmap(mImageBitmap);
        mImageView.setVisibility(
                savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ?
                        ImageView.VISIBLE : ImageView.INVISIBLE
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_pager_charge, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
