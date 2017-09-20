package com.applications.ctmy.darkroom;

/**
 * Created by yimy on 9/16/17.
 */

public class VintageCurveFilter extends CurveFilter{

    public VintageCurveFilter(){
        super(
                new double[] {0, 49, 175, 255}, // vValIn
                new double[] {0, 58, 98, 255}, // vValOut
                new double[] {0, 21, 128, 255}, // rValIn
                new double[] {0, 35, 195, 255}, // rValOut
                new double[] {0, 12, 198, 255}, // gValIn
                new double[] {0, 47, 199, 255}, // gValOut
                new double[] {0, 17, 179, 255}, // bValIn
                new double[] {0, 33, 147, 255} // bValOut
        );
    }
}
