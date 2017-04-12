package com.applications.ctmy.darkroom;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.graphics.Point;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.IOException;
import java.util.Arrays;


public class IODarkRoom extends AppCompatActivity {

    // A tag to filter the log messages
    private static final String TAG = "VisionWorld::Activity";

    // A class used to implement the iteraction between OpenCV and the
    // device camera
    private CameraBridgeViewBase mOpenCvCameraView;



    private EffectType effects;

    // Variables to handle user clicks on the menu
    private static final int SELECT_PICTURE = 1;
    private static final int THUMBNAIL = 1;
    private static final int NORMAL_SIZE = 2;
    private String selectedImagePath;
    Mat sampledImage;
    Mat originalImage;
    Mat imageThumbnail;
    Mat greyImage;

    ImageView v;
    int idMainImageView;

    private ImageView thumbnailRed;
    private ImageView thumbnailGreen;
    private ImageView thumbnailBlue;
    private ImageView thumbnailRedGreen;
    private ImageView thumbnailGreenBlue;
    private ImageView thumbnailRedBlue;
    private ImageView thumbnailGrey;
    private ImageView thumbnailGreyEnhanced;


//    static{
//        System.loadLibrary("opencv_java3");
//
//        if(!OpenCVLoader.initDebug()){
//            Log.d("ERROR", "Unable to load OpenCV");
//        }else{
//            Log.d("SUCCESS", "OpenCV loaded");
//        }
//    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iodark_room);

        thumbnailRed = (ImageView) findViewById(R.id.red);
        thumbnailGreen = (ImageView) findViewById(R.id.green);
        thumbnailBlue = (ImageView) findViewById(R.id.blue);
        thumbnailRedGreen = (ImageView) findViewById(R.id.red_green);
        thumbnailGreenBlue = (ImageView) findViewById(R.id.green_blue);
        thumbnailRedBlue = (ImageView) findViewById(R.id.red_blue);
        thumbnailGrey = (ImageView) findViewById(R.id.grey);
        thumbnailGreyEnhanced = (ImageView) findViewById(R.id.grey_enhanced);



        thumbnailRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Mat red = sampledImage;
                red = addEffect(red, effects.E_RED);
                displayImage(red, idMainImageView);


            }
        });

        thumbnailGreen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Mat green = sampledImage;
                green = addEffect(green, effects.E_GREEN);
                displayImage(green, idMainImageView);

            }
        });

        thumbnailBlue.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Mat blue = sampledImage;
                blue = addEffect(blue, effects.E_BLUE);
                displayImage(blue, idMainImageView);
            }
        });

        thumbnailRedGreen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Mat redgreen = sampledImage;
                redgreen = addEffect(redgreen, effects.E_REDGREEN);
                displayImage(redgreen, idMainImageView);
            }
        });

        thumbnailGreenBlue.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Mat greenblue = sampledImage;
                greenblue = addEffect(greenblue, effects.E_GREENBLUE);
                displayImage(greenblue, idMainImageView);
            }
        });

        thumbnailRedBlue.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Mat redblue = sampledImage;
                redblue = addEffect(redblue, effects.E_REDBLUE);
                displayImage(redblue, idMainImageView);

                view.setBackgroundColor(Color.parseColor("#EE6352"));
            }
        });

        thumbnailGrey.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Mat grey = sampledImage;
                grey = addEffect(grey, effects.GRAY);
                displayImage(grey, idMainImageView);
            }
        });

        thumbnailGreyEnhanced.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Mat gray = sampledImage;
                gray = addEffect(gray, effects.GRAY);
                gray = addEffect(gray, effects.E_GRAY);
                displayImage(gray, idMainImageView);
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.iodark_room, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        v = (ImageView) findViewById(R.id.IODarkRoomImageView);
        idMainImageView = v.getId();
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
        }else if(id == R.id.action_Hist){
            if(sampledImage == null){

                noImageMessage(getApplicationContext());
                return true;
            }
            Mat histImage = new Mat();
            sampledImage.copyTo(histImage);
            calcHist(histImage);
            displayImage(histImage, idMainImageView);
            return true;
        }else if(id == R.id.action_togs){
            if(sampledImage == null){

                noImageMessage(getApplicationContext());
                return true;
            }
            greyImage = new Mat();
            Imgproc.cvtColor(sampledImage, greyImage, Imgproc.COLOR_RGB2GRAY);
            displayImage(greyImage, idMainImageView);
            return true;
        }else if(id == R.id.action_egs){
            if(greyImage == null){

                noImageMessage(getApplicationContext());
                return true;
            }

            Mat eqGS = new Mat();
            Imgproc.equalizeHist(greyImage, eqGS);
            displayImage(eqGS, idMainImageView);
            return true;
        }else if(id == R.id.action_HSV){
            if(sampledImage == null){

                noImageMessage(getApplicationContext());
                return true;
            }

            Mat V = new Mat(sampledImage.rows(), sampledImage.cols(), CvType.CV_8UC1);
            Mat S = new Mat(sampledImage.rows(), sampledImage.cols(), CvType.CV_8UC1);

            Mat HSV = new Mat();
            Imgproc.cvtColor(sampledImage, HSV, Imgproc.COLOR_RGB2HSV);

            byte [] Vs = new byte[3];
            byte [] vsout = new byte[1];
            byte [] ssout = new byte[1];

            for(int i = 0; i < HSV.rows(); i++){
                for(int j = 0; j < HSV.cols(); j++){
                    HSV.get(i, j, Vs);
                    V.put(i,j,new byte[]{Vs[2]});
                    S.put(i,j,new byte[]{Vs[1]});
                }
            }

            Imgproc.equalizeHist(V, V);
            Imgproc.equalizeHist(S, S);

            for(int i = 0; i < HSV.rows(); i++){
                for(int j = 0; j < HSV.cols(); j++){
                    V.get(i, j, vsout);
                    S.get(i, j, ssout);
                    HSV.get(i, j, Vs);
                    Vs[2] = vsout[0];
                    Vs[1] = ssout[0];
                    HSV.put(i, j, Vs);
                }
            }

            Mat enhancedImage = new Mat();
            Imgproc.cvtColor(HSV, enhancedImage, Imgproc.COLOR_HSV2RGB);
            displayImage(enhancedImage, idMainImageView);
            return true;
        }else if(id == R.id.action_ER){
            if(sampledImage == null){

                noImageMessage(getApplicationContext());
                return true;
            }

            Mat redEnhanced = new Mat();
            sampledImage.copyTo(redEnhanced);
            Mat redMask = new Mat(sampledImage.rows(), sampledImage.cols(), sampledImage.type(), new Scalar(1,0,0,0));

            enhanceChannel(redEnhanced, redMask, sampledImage);
            displayImage(redEnhanced, idMainImageView);
        }else if(id == R.id.action_EG){
            if(sampledImage == null){

                noImageMessage(getApplicationContext());
                return true;
            }

            Mat greenEnhanced = new Mat();
            sampledImage.copyTo(greenEnhanced);
            Mat greenMask = new Mat(sampledImage.rows(), sampledImage.cols(), sampledImage.type(), new Scalar(0,1,0,0));

            enhanceChannel(greenEnhanced, greenMask, sampledImage);
            displayImage(greenEnhanced, idMainImageView);
        }else if(id == R.id.action_EB){
            if(sampledImage == null){

                noImageMessage(getApplicationContext());
                return true;
            }

            Mat blueEnhanced = new Mat();
            sampledImage.copyTo(blueEnhanced);
            Mat blueMask = new Mat(sampledImage.rows(), sampledImage.cols(), sampledImage.type(), new Scalar(0,0,1,0));

            enhanceChannel(blueEnhanced, blueMask, sampledImage);
            displayImage(blueEnhanced, idMainImageView);
        }else if(id == R.id.action_ERG){

           if(sampledImage == null) {
               noImageMessage(getApplicationContext());
               return true;
           }

           Mat rgEnhanced = new Mat();
            sampledImage.copyTo(rgEnhanced);
            Mat rgMask = new Mat(sampledImage.rows(), sampledImage.cols(), sampledImage.type(), new Scalar(1,1,0,0));
            enhanceChannel(rgEnhanced, rgMask, sampledImage);

            displayImage(rgEnhanced, idMainImageView);
        }else if(id == R.id.action_EGB){
            if(sampledImage == null){
                noImageMessage(getApplicationContext());
                return true;
            }
            Mat gbEnhanced = new Mat();
            sampledImage.copyTo(gbEnhanced);
            Mat gbMask = new Mat(sampledImage.rows(), sampledImage.cols(), sampledImage.type(), new Scalar(0,1,1,0));
            enhanceChannel(gbEnhanced, gbMask, sampledImage);
            displayImage(gbEnhanced, idMainImageView);
        }else if(id == R.id.action_ERB){
            if(sampledImage == null){
                noImageMessage(getApplicationContext());
                return true;
            }

            Mat rbEnhanced = new Mat();
            sampledImage.copyTo(rbEnhanced);
            Mat rbMask = new Mat(sampledImage.rows(),sampledImage.cols(), sampledImage.type(), new Scalar(1,0,1,0));
            enhanceChannel(rbEnhanced, rbMask, sampledImage);
            displayImage(rbEnhanced, idMainImageView);
        }

        return super.onOptionsItemSelected(item);
    }

    public void noImageMessage(Context c){

            Toast toast = Toast.makeText(c,"You need to load an image first!", Toast.LENGTH_SHORT);
            toast.show();

    }

    private void enhanceChannel(Mat imageToEnhance, Mat mask, Mat originalImage){
        //Mat channel = new Mat(sampledImage.rows(), sampledImage.cols(), CvType.CV_8UC1);
        Mat channel = new Mat(originalImage.rows(), originalImage.cols(), CvType.CV_8UC1);
        originalImage.copyTo(channel, mask);

        Imgproc.cvtColor(channel, channel, Imgproc.COLOR_RGB2GRAY, 1);
        Imgproc.equalizeHist(channel, channel);
        Imgproc.cvtColor(channel, channel, Imgproc.COLOR_GRAY2RGB, 3);
        channel.copyTo(imageToEnhance, mask);
    }

    private void calcHist(Mat image){
        int mHistSizeNum = 25;
        MatOfInt mHistSize = new MatOfInt(mHistSizeNum);
        Mat hist = new Mat();
        float [] mBuff = new float[mHistSizeNum];
        MatOfFloat histogramRanges = new MatOfFloat(0f, 256f);
        Scalar mColorSRGB[] = new Scalar[]{new Scalar(200,0,0,255),
                                           new Scalar(0,200,0,255),
                                           new Scalar(0,0,200,255)};
        org.opencv.core.Point mP1 = new org.opencv.core.Point();
        org.opencv.core.Point mP2 = new org.opencv.core.Point();

        int thickness = (int)(image.width() / (mHistSizeNum+10)/3);
        if(thickness > 3) thickness = 3;
        MatOfInt mChannels[] = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};

        Size sizeRgba = image.size();
        int offset = (int)((sizeRgba.width - (3*mHistSizeNum+30) * thickness));
        // RGB
        for(int c = 0; c < 3; c++){
            Imgproc.calcHist(Arrays.asList(image), mChannels[c], new Mat(), hist, mHistSize, histogramRanges);
            Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
            hist.get(0,0,mBuff);
            for(int h = 0; h < mHistSizeNum; h++){
                mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thickness;
                mP1.y = sizeRgba.height-1;
                mP2.y = mP1.y - (int)mBuff[h];
                Imgproc.line(image, mP1, mP2, mColorSRGB[c], thickness);
            }
        }

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


                // Main ImageView for the main picture
                sampledImage = loadImage(selectedImagePath, sampledImage, NORMAL_SIZE);

                // Mat object to add effects and display them in the imageViews views
                imageThumbnail = loadImage(selectedImagePath, imageThumbnail, THUMBNAIL);

                sampledImage = transformImage(sampledImage);
                imageThumbnail = transformImage(imageThumbnail);

                // Display main picture/image
                displayImage(sampledImage, idMainImageView);


                // Getting id's of the ImageViews
                int IDRed = thumbnailRed.getId();
                int IDGreen = thumbnailGreen.getId();
                int IDBlue = thumbnailBlue.getId();
                int IDRedGreen = thumbnailRedGreen.getId();
                int IDGreenBlue = thumbnailGreenBlue.getId();
                int IDRedBlue = thumbnailRedBlue.getId();
                int IDGrey = thumbnailGrey.getId();
                int IDGreyEnhanced = thumbnailGreyEnhanced.getId();

                // Using the same Mat imageThumbnail to display all the effects
                // on the ImageView's
                Mat red = imageThumbnail;
                red = addEffect(red, effects.E_RED);
                displayImage(red, IDRed);

                Mat green = imageThumbnail;
                green = addEffect(green, effects.E_GREEN);
                displayImage(green, IDGreen);

                Mat blue = imageThumbnail;
                blue = addEffect(blue, effects.E_BLUE);
                displayImage(blue, IDBlue);



                Mat greenblue = imageThumbnail;
                greenblue = addEffect(greenblue, effects.E_GREENBLUE);
                displayImage(greenblue, IDGreenBlue);

                Mat redblue = imageThumbnail;
                redblue = addEffect(redblue, effects.E_REDBLUE);
                displayImage(redblue, IDRedBlue);

                Mat redgreen = imageThumbnail;
                redgreen = addEffect(redgreen, effects.E_REDGREEN);
                displayImage(redgreen, IDRedGreen);

                Mat grey = imageThumbnail;
                grey = addEffect(grey, effects.GRAY);
                displayImage(grey, IDGrey);

                //Mat grayEnhanced = imageThumbnail;
                grey = addEffect(grey, effects.E_GRAY);
                displayImage(grey, IDGreyEnhanced);
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


    private Mat loadImage(String path, Mat img, int type){

        originalImage = Imgcodecs.imread(path);
        Mat rgbImage = new Mat();

        Imgproc.cvtColor(originalImage, rgbImage, Imgproc.COLOR_BGR2RGB);

       Mat resizedTemp = resize(rgbImage, img, type);
        /*try{
            ExifInterface exif = new ExifInterface(selectedImagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    sampledImage = sampledImage.t();
                    Core.flip(sampledImage, sampledImage, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    sampledImage = sampledImage.t();
                    Core.flip(sampledImage, sampledImage, 0);
                    break;

            }
        }catch (IOException e){
            e.printStackTrace();
        }*/

        return resizedTemp;
    }

    private Mat resize(Mat original, Mat resized, int sizeTo){

        Display display = getWindowManager().getDefaultDisplay();

        // This is "android graphics Point" class
        Point size = new Point();
        display.getSize(size);

        int width = (int) size.x;
        int height = (int) size.y;

        double downSampleRatio = calculateSubSampleSize(original, width, height);

        if(sizeTo == THUMBNAIL)
            downSampleRatio /= 4;


        Mat temp = resized;
        temp = new Mat();
        Imgproc.resize(original, temp, new Size(), downSampleRatio, downSampleRatio,
                Imgproc.INTER_AREA);

        return temp;
    }

    private Mat transformImage(Mat img){
        Mat temp = img;
        try{
            ExifInterface exif = new ExifInterface(selectedImagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            switch (orientation)
            {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    //get the mirrored image
                    temp = temp.t();
                    //flip on the y-axis
                    Core.flip(temp, temp, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    //get up side down image
                    temp=temp.t();
                    //Flip on the x-axis
                    Core.flip(temp, temp, 0);
                    break;
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return temp;

    }

    private void displayImage(Mat image, int id){
        // create a bitMap
        Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.RGB_565);

        //Convert to bitmap
        Utils.matToBitmap(image, bitmap);

        // find the imageview and draw it!
        ImageView iv = (ImageView) findViewById(id);
        //v = (ImageView) findViewById(R.id.ef_one);
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


    private Mat addEffect(Mat img, EffectType effect){
        Mat resultEffect = new Mat();
        if(effect == effects.E_RED){
            img.copyTo(resultEffect);
            Mat mask = new Mat(img.rows(),img.cols(), img.type(), new Scalar(1,0,0,0));
            enhanceChannel(resultEffect, mask, img);
        }
        else if(effect == effects.E_GREEN){
            img.copyTo(resultEffect);
            Mat mask = new Mat(img.rows(),img.cols(), img.type(), new Scalar(0,1,0,0));
            enhanceChannel(resultEffect, mask, img);
        }
        else if(effect == effects.E_BLUE){
            img.copyTo(resultEffect);
            Mat mask = new Mat(img.rows(),img.cols(), img.type(), new Scalar(0,0,1,0));
            enhanceChannel(resultEffect, mask, img);
        }
        else if(effect == effects.E_REDGREEN){
            img.copyTo(resultEffect);
            Mat mask = new Mat(img.rows(),img.cols(), img.type(), new Scalar(1,1,0,0));
            enhanceChannel(resultEffect, mask, img);
        }
        else if(effect == effects.E_GREENBLUE){
            img.copyTo(resultEffect);
            Mat mask = new Mat(img.rows(),img.cols(), img.type(), new Scalar(0,1,1,0));
            enhanceChannel(resultEffect, mask, img);
        }
        else if(effect == effects.E_REDBLUE){
            img.copyTo(resultEffect);
            Mat mask = new Mat(img.rows(),img.cols(), img.type(), new Scalar(1,0,1,0));
            enhanceChannel(resultEffect, mask, img);
        }
        else if(effect == effects.GRAY){

           Imgproc.cvtColor(img, resultEffect, Imgproc.COLOR_RGB2GRAY);

        }
        else if(effect == effects.E_GRAY){
            Imgproc.equalizeHist(img, resultEffect);
        }

        return resultEffect;
    }

}

/**
 * enum to identify the different effects.
 */
enum EffectType{
    E_RED, E_GREEN, E_BLUE,
    E_REDGREEN, E_GREENBLUE, E_REDBLUE,
    GRAY, E_GRAY;

}
