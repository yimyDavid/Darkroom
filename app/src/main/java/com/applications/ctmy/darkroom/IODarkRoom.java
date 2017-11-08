package com.applications.ctmy.darkroom;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.graphics.Point;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


public class IODarkRoom extends AppCompatActivity {

    // A tag to filter the log messages
    private static final String TAG = "VisionWorld::Activity";

    // A class used to implement the iteraction between OpenCV and the
    // device camera
    private CameraBridgeViewBase mOpenCvCameraView;

    static final int REQUEST_IMAGE_CAPTURE = 2;


    private EffectType effects;
    private EffectType currentEffect;

    // Variables to handle user clicks on the menu
    private static final int SELECT_PICTURE = 1;
    private static final int THUMBNAIL = 1;
    private static final int NORMAL_SIZE = 2;
    private String selectedImagePath;
    private String imageFileName="";
    Mat sampledImage;
    Mat originalImage;
    Mat imageThumbnail;
    Mat greyImage;

    ImageView v;
    int idMainImageView;

    //Action provider to share content
    private ShareActionProvider mShareActionProvider;

    /* Variables to manage the filters. */
    // Keys for storing the indices of the active filters.
    private static final String STATE_CURVE_FILTER_INDEX = "curveFilterIndex";
    private static final String STATE_CONVOLUTION_FILTER_INDEX = "convolutionFilterIndex";

    // The filters.
    private Filter[] mCurveFilters;
    private Filter[] mConvolutionFilters;

    // The indices of the active filters.
    private int mCurveFilterIndex;
    private int mConvolutionFilterIndex;




    private ImageView thumbnailRed;
    private ImageView thumbnailGreen;
    private ImageView thumbnailBlue;
    private ImageView thumbnailRedGreen;
    private ImageView thumbnailGreenBlue;
    private ImageView thumbnailRedBlue;
    private ImageView thumbnailGrey;
    private ImageView thumbnailGreyEnhanced;
    private ImageView thumbnailPortra;
    private ImageView thumbnailProvia;
    private ImageView thumbnailVelvia;
    private ImageView thumbnailStoke;
    private ImageView thumbnailNone;
    private ImageView thumbnailCross;


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
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        // Hide Notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_iodark_room);
        // Currently it seems to have no effect.
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.mipmap.ic_color_lens_white_48dp);

        v = (ImageView)findViewById(R.id.IODarkRoomImageView);

        thumbnailRed = (ImageView) findViewById(R.id.red);
        thumbnailGreen = (ImageView) findViewById(R.id.green);
        thumbnailBlue = (ImageView) findViewById(R.id.blue);
        thumbnailRedGreen = (ImageView) findViewById(R.id.red_green);
        thumbnailGreenBlue = (ImageView) findViewById(R.id.green_blue);
        thumbnailRedBlue = (ImageView) findViewById(R.id.red_blue);
        thumbnailGrey = (ImageView) findViewById(R.id.grey);
        thumbnailGreyEnhanced = (ImageView) findViewById(R.id.grey_enhanced);
        thumbnailPortra = (ImageView) findViewById(R.id.portra_curve);
        thumbnailProvia = (ImageView) findViewById(R.id.provia_curve);
        thumbnailCross = (ImageView) findViewById(R.id.cross_proc);
        thumbnailVelvia = (ImageView) findViewById(R.id.velvia_curve);
        thumbnailNone   = (ImageView) findViewById(R.id.original);
        thumbnailStoke = (ImageView) findViewById(R.id.stroke_edge);
        thumbnailVelvia = (ImageView) findViewById(R.id.velvia_curve);


        if(savedInstanceState != null){
            mCurveFilterIndex = savedInstanceState.getInt(STATE_CURVE_FILTER_INDEX, 0);
            mConvolutionFilterIndex = savedInstanceState.getInt(STATE_CONVOLUTION_FILTER_INDEX, 0);
        }else{
            mCurveFilterIndex = 0;
            mConvolutionFilterIndex = 0;
        }

        /* THUMBNAIL WITH EFFECTS
         * OnClick events for the thumbnails with the effects applied.
         * Once the user click on of them, the effect is applied to the main view/image
         */

        thumbnailRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Mat red = sampledImage;
                red = addEffect(red, effects.E_RED);

                currentEffect = effects.E_RED;
                displayImage(red, idMainImageView);


            }
        });

        thumbnailGreen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Mat green = sampledImage;
                green = addEffect(green, effects.E_GREEN);

                currentEffect = effects.E_GREEN;
                displayImage(green, idMainImageView);

            }
        });

        thumbnailBlue.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Mat blue = sampledImage;
                blue = addEffect(blue, effects.E_BLUE);

                currentEffect = effects.E_BLUE;
                displayImage(blue, idMainImageView);
            }
        });

        thumbnailRedGreen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Mat redgreen = sampledImage;
                redgreen = addEffect(redgreen, effects.E_REDGREEN);

                currentEffect = effects.E_REDGREEN;
                displayImage(redgreen, idMainImageView);
            }
        });

        thumbnailGreenBlue.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Mat greenblue = sampledImage;
                greenblue = addEffect(greenblue, effects.E_GREENBLUE);

                currentEffect = effects.E_GREENBLUE;
                displayImage(greenblue, idMainImageView);
            }
        });

        thumbnailRedBlue.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Mat redblue = sampledImage;
                redblue = addEffect(redblue, effects.E_REDBLUE);

                currentEffect = effects.E_REDBLUE;
                displayImage(redblue, idMainImageView);

                //view.setBackgroundColor(Color.parseColor("#EE6352"));
            }
        });

        thumbnailGrey.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Mat grey = sampledImage;
                grey = addEffect(grey, effects.GRAY);

                currentEffect = effects.GRAY;
                displayImage(grey, idMainImageView);
            }
        });

        thumbnailGreyEnhanced.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Mat gray = sampledImage;
                gray = addEffect(gray, effects.GRAY);
                gray = addEffect(gray, effects.E_GRAY);

                currentEffect = effects.E_GRAY;
                displayImage(gray, idMainImageView);
            }
        });

        thumbnailPortra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Mat portra = new Mat();
                sampledImage.copyTo(portra);
                portra = addEffect(portra, effects.PORTRA);

                currentEffect = effects.PORTRA;
                displayImage(portra, idMainImageView);
            }
        });

        thumbnailProvia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Mat provia = new Mat();
                sampledImage.copyTo(provia);
                provia = addEffect(provia, effects.PROVIA);

                currentEffect = effects.PROVIA;
                displayImage(provia, idMainImageView);
            }
        });

        thumbnailCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Mat cross = new Mat();
                sampledImage.copyTo(cross);
                cross = addEffect(cross, effects.CROSS);

                currentEffect = effects.CROSS;
                displayImage(cross, idMainImageView);
            }
        });

        thumbnailStoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Mat stroke = new Mat();
                sampledImage.copyTo(stroke);
                stroke = addEffect(stroke, effects.STROKE);

                currentEffect = effects.STROKE;
                displayImage(stroke, idMainImageView);
            }
        });

        thumbnailVelvia.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Mat velvia = new Mat();
                sampledImage.copyTo(velvia);
                velvia = addEffect(velvia, effects.VELVIA);

                currentEffect = effects.VELVIA;
                displayImage(velvia, idMainImageView);
            }
        });

        thumbnailNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Mat none = new Mat();
                sampledImage.copyTo(none);
                none = addEffect(none, effects.NONE);

                currentEffect = effects.NONE;
                displayImage(none, idMainImageView);
            }
        });
        /*END OF THUMBNAIL WITH EFFECTS*/


    }

    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putInt(STATE_CURVE_FILTER_INDEX, mCurveFilterIndex);
        savedInstanceState.putInt(STATE_CONVOLUTION_FILTER_INDEX, mConvolutionFilterIndex);
    }
    /**
     * Sets the intent to share an image with other apps, such as whatsapp, viber, etc.
     * @return the intent loaded with the image to share.
     */

    public Intent prepareShareIntent(){

        File image = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/" + imageFileName + ".jpg");
       //  File image = new File(path);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");


        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(image.getPath()));
        // I think the createChooser should be used when not using the ShareActionProvider method.
        //startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
        mShareActionProvider.setShareIntent(shareIntent);
        // DEBUG LINE
        // System.out.println("prepare " + image.getAbsolutePath());
        return shareIntent;

    }

    private void setShareIntent(Intent shareIntent){
        if(mShareActionProvider != null){
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    /**
     * Used to load the menu from the xml file and run the prepareShareIntent method above.
     * This methods is only setup once and that's why (after so much trying) I am using
     * this method invalidateOptionsMenu(); to refresh it.
     * @param menu Parameter used to inflate the menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getSupportActionBar().setIcon(R.mipmap.ic_color_lens_white_48dp);


        getMenuInflater().inflate(R.menu.iodark_room, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.share_picture);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        //mShareActionProvider = new ShareActionProvider(this);
        MenuItemCompat.setActionProvider(item, mShareActionProvider);


        File root = Environment.getExternalStorageDirectory();
        String path = root.getAbsolutePath() + "/Pictures/" + imageFileName + ".jpg";
        //Uri uriPath = Uri.parse(selectedImagePath);
        selectedImagePath = path;
        System.out.println("onCreateOptions " + selectedImagePath);
        // Set it for the first time
        setShareIntent(prepareShareIntent());

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        //v = (ImageView) findViewById(R.id.IODarkRoomImageView);
        idMainImageView = v.getId();
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/up button, so long
        // as you specify a parent activity in  AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_openGallery){
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, SELECT_PICTURE);

            return true;

        }else if(id == R.id.take_picture){
            dispatchTakePictureIntent();
            return true;

        }else if(id == R.id.save_picture){
            invalidateOptionsMenu();
            if(sampledImage == null){

                noImageMessage(getApplicationContext());
                // TODO: use a string resource to translate to spanish
                System.out.println(R.string.no_image);
                return true;
            }

            Mat rgbImage = new Mat();
            Imgproc.cvtColor(originalImage, rgbImage, Imgproc.COLOR_BGR2RGB);

            // Convert to gray before gray enhancing.
            if(currentEffect == effects.E_GRAY){
                rgbImage = addEffect(rgbImage, effects.GRAY);
            }
            rgbImage = addEffect(rgbImage, currentEffect);
            Bitmap bitmap = Bitmap.createBitmap(rgbImage.cols(), rgbImage.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(rgbImage, bitmap);
            saveImageViewImage(bitmap);

        }else if(id == R.id.share_picture){
            /* This method does not do or trigger anything because of the action provider*/
            /*if(sampledImage == null){

                noImageMessage(getApplicationContext());
                System.out.println("the sample image is NULL");
                return true;
            }

            ///saveImageViewImage();
            Mat rgbImage = new Mat();
            Imgproc.cvtColor(originalImage, rgbImage, Imgproc.COLOR_BGR2RGB);

            // Convert to gray before gray enhancing.
            if(currentEffect == effects.E_GRAY){
                rgbImage = addEffect(rgbImage, effects.GRAY);
            }
            rgbImage = addEffect(rgbImage, currentEffect);
            Bitmap bitmap = Bitmap.createBitmap(rgbImage.cols(), rgbImage.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(rgbImage, bitmap);

            saveImageViewImage(bitmap);

            File root = Environment.getExternalStorageDirectory();
            String path = root.getAbsolutePath()  + "/Pictures/" + imageFileName + ".jpg";
            Uri uriPath = Uri.parse(selectedImagePath);
            //prepareShareIntent();
            System.out.println(path + "  tina");

            if(mShareActionProvider != null)
                mShareActionProvider.setShareIntent(prepareShareIntent());
                return true;
*/


        }/*else if(id == R.id.action_Hist){
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
        }else if(id == R.id.portra_curve){
            if(sampledImage == null){
                noImageMessage(getApplicationContext());
                return true;
            }

            Mat portra = new Mat();
            sampledImage.copyTo(portra);
            mCurveFilters[0].apply(portra, portra);
            displayImage(portra, idMainImageView);
            return true;

        }

        else if(id == R.id.menu_context_curve_filter){
            mCurveFilterIndex++;
            if(mCurveFilterIndex == mCurveFilters.length){
                mCurveFilterIndex = 0;
                System.out.println("yimy index " + mCurveFilterIndex);
            }
            System.out.println("curve " + mCurveFilterIndex);

            Mat curveSampleImage = new Mat();
            sampledImage.copyTo(curveSampleImage);
            mCurveFilters[mCurveFilterIndex].apply(curveSampleImage, curveSampleImage);
            displayImage(curveSampleImage, idMainImageView);
            return true;
        }else if(id == R.id.menu_next_convolution_filter){
            if(sampledImage == null){
                noImageMessage(getApplicationContext());
                return true;
            }

            mConvolutionFilterIndex++;
            if(mConvolutionFilterIndex == mConvolutionFilters.length){
                mConvolutionFilterIndex = 0;
            }

            Mat convolutionSampleImage = new Mat();
            sampledImage.copyTo(convolutionSampleImage);
            mConvolutionFilters[mConvolutionFilterIndex].apply(convolutionSampleImage, convolutionSampleImage);
            displayImage(convolutionSampleImage, idMainImageView);
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    public void noImageMessage(Context c){

            Toast toast = Toast.makeText(c,R.string.load_image, Toast.LENGTH_SHORT);
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
                    mCurveFilters = new Filter[]{
                        new NoneFilter(),
                        new PortraCurveFilter(),
                        new CrossProcessCurveFilter(),
                        new ProviaCurveFilter(),
                        new VelviaCurveFilter(),
                        //new VintageCurveFilter()
                    };
                    mConvolutionFilters = new Filter[]{
                            new NoneFilter(),
                            new StrokeEdgeFilter()
                    };
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
        // supuestamente invalida el menu y puede refrescarlo
        // It works! It refreshes the action provider to share the images
        invalidateOptionsMenu();
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_IMAGE_CAPTURE){
                galleryAddPic();
                sampledImage = loadImage(selectedImagePath, sampledImage, NORMAL_SIZE);
                displayImage(sampledImage, idMainImageView);
            }

            else if(requestCode == SELECT_PICTURE){
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                // Log.i(TAG, "selectedImagePath: " + selectedImagePath);
                // System.out.println(selectedImagePath + " YIMY");
                sampledImage = loadImage(selectedImagePath, sampledImage, NORMAL_SIZE);
                displayImage(sampledImage, idMainImageView);
                //TODO Create name of the file selected. Not too happy
                imageFileName = selectedImagePath.substring(selectedImagePath.lastIndexOf('/'), selectedImagePath.length()-4);
                // System.out.println("SELECT_PICTURE " + imageFileName);

            }
                // Mat object to add effects and display them in the imageViews views
                imageThumbnail = loadImage(selectedImagePath, imageThumbnail, THUMBNAIL);

                sampledImage = transformImage(sampledImage);
                imageThumbnail = transformImage(imageThumbnail);

                // Getting id's of the ImageViews
                int IDRed = thumbnailRed.getId();
                int IDGreen = thumbnailGreen.getId();
                int IDBlue = thumbnailBlue.getId();
                int IDRedGreen = thumbnailRedGreen.getId();
                int IDGreenBlue = thumbnailGreenBlue.getId();
                int IDRedBlue = thumbnailRedBlue.getId();
                int IDGrey = thumbnailGrey.getId();
                int IDGreyEnhanced = thumbnailGreyEnhanced.getId();
                int IDPortra = thumbnailPortra.getId();
                int IDProvia = thumbnailProvia.getId();
                int IDCross = thumbnailCross.getId();
                int IDStroke = thumbnailStoke.getId();
                int IDVelvia = thumbnailVelvia.getId();
                int IDNone = thumbnailNone.getId();

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

                Mat portra = imageThumbnail;
                portra = addEffect(portra, effects.PORTRA);
                displayImage(portra, IDPortra);

                Mat provia = imageThumbnail;
                provia = addEffect(provia, effects.PROVIA);
                displayImage(provia, IDProvia);

                Mat cross = imageThumbnail;
                cross = addEffect(cross, effects.CROSS);
                displayImage(cross, IDCross);

                Mat stroke = imageThumbnail;
                stroke = addEffect(stroke, effects.STROKE);
                displayImage(stroke, IDStroke);

                Mat velvia = imageThumbnail;
                velvia = addEffect(velvia, effects.VELVIA);
                displayImage(velvia, IDVelvia);

                Mat none = imageThumbnail;
                none = addEffect(none, effects.NONE);
                displayImage(none, IDNone);
            ///}
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
            cursor.close();
            return s;
        }
        cursor.close();
        return uri.getPath();
    }


    private Mat loadImage(String path, Mat img, int type){

        originalImage = Imgcodecs.imread(path);
        Mat rgbImage = new Mat();

        Imgproc.cvtColor(originalImage, rgbImage, Imgproc.COLOR_BGR2RGB);

       Mat resizedTemp = resize(rgbImage, img, type);

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


        //Mat temp = resized;
        resized = new Mat();
        Imgproc.resize(original, resized, new Size(), downSampleRatio, downSampleRatio,
                Imgproc.INTER_AREA);

        return resized;
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
        else if(effect == effects.PORTRA){

            img.copyTo(resultEffect);
            mCurveFilters[0].apply(resultEffect, resultEffect);
        }
        else if(effect == effects.PROVIA){
            img.copyTo(resultEffect);
            mCurveFilters[1].apply(resultEffect, resultEffect);
        }
        else if(effect == effects.CROSS){
            img.copyTo(resultEffect);
            mCurveFilters[2].apply(resultEffect, resultEffect);
        }
        else if(effect == effects.STROKE){
            img.copyTo(resultEffect);
            mConvolutionFilters[1].apply(resultEffect, resultEffect);
        }
        else if(effect == effects.VELVIA){
            img.copyTo(resultEffect);
            mCurveFilters[3].apply(resultEffect, resultEffect);
        }
        else if(effect == effects.NONE){
            img.copyTo(resultEffect);
            mConvolutionFilters[0].apply(resultEffect, resultEffect);
        }

        return resultEffect;
    }


    /**
     * Camera functionalities
     */

    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            //Create the file where the photo should go
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch(IOException ex){
                Context c = getApplicationContext();
                Toast toast = Toast.makeText(c,"Could not load image", Toast.LENGTH_SHORT);
                toast.show();
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this, "com.applications.ctmy.darkroom.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException{
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg",       /* suffix */
                storageDir    /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        selectedImagePath = image.getAbsolutePath();
        return image;
    }

    /**
     *  Adds the picture taken with the camera to the gallery and accessible to
     *  other apps.
     */
    private void galleryAddPic(){
        Intent mediaScanerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(selectedImagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanerIntent.setData(contentUri);
        this.sendBroadcast(mediaScanerIntent);
    }

    private void saveImageViewImage(Bitmap bmp){
        ///v.buildDrawingCache();
        ///Bitmap bmWithEffect = v.getDrawingCache();
        File root = Environment.getExternalStorageDirectory();
        File cachePath = new File(root.getAbsolutePath()  + "/Pictures/" + imageFileName + ".jpg");
        try{
            cachePath.createNewFile();
            FileOutputStream ostream = new FileOutputStream(cachePath);
            ///bmWithEffect.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
            ostream.close();
            ///v.destroyDrawingCache();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

/**
 * enum to identify the different effects.
 */
enum EffectType{
    E_RED, E_GREEN, E_BLUE,
    E_REDGREEN, E_GREENBLUE, E_REDBLUE,
    GRAY, E_GRAY, PORTRA, CROSS,
    PROVIA, STROKE, VELVIA, NONE;

}
