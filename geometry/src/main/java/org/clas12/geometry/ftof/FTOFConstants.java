/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas12.geometry.ftof;

import org.jlab.jnp.detector.base.DetectorConstants;

/**
 *
 * @author gavalian
 */
public class FTOFConstants {
    
    public static DetectorConstants getDetectorConstants(){
        double[] paddles = new double[]{
             32.3,48.1,64.0, 79.8, 95.7, 106.6, 122.4, 
            138.3, 154.1,170.0,185.8 ,201.7,217.6 ,233.4 , 
            249.3, 265.1,281.0,296.8,312.7,328.5,344.4,360.2,376.1
        };
        
        DetectorConstants constants = new DetectorConstants();
        constants.addDouble("/geometry/ftof/panel1a/Length", paddles);
        constants.addDouble("/geometry/ftof/panel1a/panel/paddlewidth",new double[]{ 15.01 });        
        constants.addDouble("/geometry/ftof/panel1a/panel/paddlethickness", new double[] {5.08});
        constants.addDouble("/geometry/ftof/panel1a/panel/thtilt",          new double[] {25});
        constants.addDouble("/geometry/ftof/panel1a/panel/thmin", new double[] { 5.453});
        constants.addDouble("/geometry/ftof/panel1a/panel/dist2edge", new double[] {726.689});
        constants.addDouble("/geometry/ftof/panel1a/panel/gap", new double[]{ 0.1384}); 
        constants.addDouble("/geometry/ftof/panel1a/panel/wrapperthickness", new double[]{0.02896});
        FTOFConstants.addAlignment(constants);
        return constants;
    }
    
    public static void addAlignment(DetectorConstants dc){
        dc.addInteger("/geometry/ftof/alignment/sector", new int[]{1,2,3,4,5,6});
        dc.addInteger("/geometry/ftof/alignment/layer", new int[]{1,1,1,1,1,1});
        dc.addInteger("/geometry/ftof/alignment/component", new int[]{0,0,0,0,0,0});
        dc.addDouble("/geometry/ftof/alignment/deltaX", new double[]{0,0,0,0,0,0});
        dc.addDouble("/geometry/ftof/alignment/deltaY", new double[]{0,0,0,0,0,0});
        dc.addDouble("/geometry/ftof/alignment/deltaZ", new double[]{0,50.0,0,0,0,0});
        dc.addDouble("/geometry/ftof/alignment/rotX", new double[]{0,0.0,0,0,0,0});
        dc.addDouble("/geometry/ftof/alignment/rotY", new double[]{0,0.0,0,0,0,0});
        dc.addDouble("/geometry/ftof/alignment/rotZ", new double[]{0,8.0,0,30.0,0,0});
    }
}
