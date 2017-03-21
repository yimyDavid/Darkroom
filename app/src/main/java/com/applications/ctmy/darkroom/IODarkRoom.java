package com.applications.ctmy.darkroom;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.icu.util.MeasureUnit;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.graphics.Point;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.IOException;

public class IODarkRoom extends AppCompatActivity {

    // A tag to filter the log messages
    private static final String TAG = "VisionWorld::Activity";

    // A class used to implement the iteraction between OpenCV and the
    // device camera
    private CameraBridgeViewBase mOpenCvCameraView;

    // Variables to handle user clicks on the menu
    private static final int SELECT_PICTURE = 1;
    private String selectedImagePath = "/storage/emulated/0/Pictures/Screenshots/Screenshot.png";
    Mat sampledImage;
    Mat originalImage;


    static{
        System.loadLibrary("opencv_java3");

        if(!OpenCVLoader.initDebug()){
            Log.d("ERROR", "Unable to load OpenCV");
        }else{
            Log.d("SUCCESS", "OpenCV loaded");
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iodark_room);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.iodark_room, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/up button, so long
        // as you specify a parent activity in  AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_openGallery){
            Intent intent = new Intent();
            // I HAD THIS WRONG "imag/*
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                    SELECT_PICTURE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this){
        @Override
        public void onManagerConnected(int status){
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                }break;
                default:{
                    super.onManagerConnected(status);
                }break;
            }
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            if(requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                Log.i(TAG, "selectedImagePath: " + selectedImagePath);
                loadImage(selectedImagePath);
                displayImage(sampledImage);
            }
        }

    }

    private String getPath(Uri uri){
        // just some safety built in
        if(uri == null){
            return null;
        }

        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if(cursor != null){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String s = cursor.getString(column_index);
            return s;
        }
        return uri.getPath();
    }

    private void loadImage(String path){
        originalImage = Imgcodecs.imread(path);
        Mat rgbImage = new Mat();

        Imgproc.cvtColor(originalImage, rgbImage, Imgproc.COLOR_BGR2RGB);

        Display display = getWindowManager().getDefaultDisplay();

        // This is "android graphics Point" class
        Point size = new Point();
        display.getSize(size);

        int width = (int) size.x;
        int height = (int) size.y;
        sampledImage = new Mat();

        double downSampleRatio = calculateSubSampleSize(rgbImage, width, height);

        Imgproc.resize(rgbImage, sampledImage, new Size(), downSampleRatio, downSampleRatio,
                Imgproc.INTER_AREA);

        try{
            ExifInterface exif = new ExifInterface(selectedImagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    //get the mirrored image
                    sampledImage = sampledImage.t();
                    //flip on the y-axis
                    Core.flip(sampledImage, sampledImage, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    //get up side down image
                    sampledImage=sampledImage.t();
                    //Flip on the x-axis
                    Core.flip(sampledImage, sampledImage, 0);
                    break;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void displayImage(Mat image){
        // create a bitMap
        Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.RGB_565);

        //Convert to bitmap
        Utils.matToBitmap(image, bitmap);

        // find the imageview and draw it!
        ImageView iv = (ImageView) findViewById(R.id.IODarkRoomImageView);
        iv.setImageBitmap(bitmap);
    }

    private static double calculateSubSampleSize(Mat srcImage, int reqWidth, int reqHeight){
        // Raw height and width of image
        final int height = srcImage.height();
        final int width = srcImage.width();
        double inSampleSize = 1;

        if(height > reqHeight || width > reqHeight){
            // Calculate ratios of requested height and width to the raw height and width
            final double heightRatio = (double) reqHeight / (double)height;
            final double widthRatio = (double) reqWidth / (double)width;

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }
}
