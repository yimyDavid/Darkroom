package com.applications.ctmy.darkroom;

import org.opencv.core.Mat;

import java.io.File;

/**
 * Created by yimy on 9/12/17.
 */

public class NoneFilter implements Filter {
    @Override
    public void apply(final Mat src, final Mat dst){
        // Do nothing.
    }
}
