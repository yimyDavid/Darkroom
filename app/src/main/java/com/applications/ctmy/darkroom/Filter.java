package com.applications.ctmy.darkroom;

import org.opencv.core.Mat;

/**
 * Created by yimy on 9/12/17.
 */

public interface Filter {
    public abstract void apply(final Mat src, final Mat dst);

}
