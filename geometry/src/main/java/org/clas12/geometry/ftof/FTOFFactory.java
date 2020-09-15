/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas12.geometry.ftof;

import java.util.Arrays;
import org.clas12.geometry.db.DatabaseConstantProvider;
//import org.clas12.geometry.units.SystemOfUnits.Length;
import org.jlab.jnp.detector.base.ConstantProvider;
import org.jlab.jnp.detector.base.Detector;
import org.jlab.jnp.detector.base.DetectorConstants;
import org.jlab.jnp.detector.base.Factory;
import org.jlab.jnp.geom.geant4.G4Detector;
import org.jlab.jnp.geom.geant4.G4LogVolume;
import org.jlab.jnp.geom.geant4.G4PhysVolume;
import org.jlab.jnp.geom.geant4.G4Unit;
import org.jlab.jnp.geom.geant4.G4Volume;
import org.jlab.jnp.geom.geant4.Geant4Shape;
import org.jlab.jnp.geom.geant4.Geant4Solid;
import org.jlab.jnp.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */
public class FTOFFactory implements Factory {

    private final double motherGap = 4.0;
    
    private final String[] stringLayers = new String[]{
        "/geometry/ftof/panel1a",
        "/geometry/ftof/panel1b",
        "/geometry/ftof/panel2"};

    private double[] sectorRotationX = new double[]{};
    private double[] sectorRotationY = new double[]{};
    private double[] sectorRotationZ = new double[]{};
    private Point3D[] sectorPosition = new Point3D[]{
        new Point3D(  227.952984,            0, 657.6473272),
        new Point3D(  113.976492,  197.4130751, 657.6473272),
        new Point3D( -113.976492,  197.4130751, 657.6473272),
        new Point3D( -227.952984,            0, 657.6473272),
        new Point3D( -113.976492, -197.4130751 ,657.6473272),
        new Point3D(  113.976492, -197.4130751, 657.6473272)
    };

private Point3D[] sectorRotation = new Point3D[]{
    new Point3D(-180, -65, 90),
    new Point3D(118.300052432748 , -26.9462152626277, 166.287829807799),
    new Point3D(118.300052432748,  26.9462152626277, -166.287829807799),
    new Point3D(180,65,-90),
    new Point3D(-118.300052432748 ,26.9462152626277 , -13.7121701922012),
    new Point3D(-118.300052432748, -26.9462152626277, 13.7121701922012)

};

    @Override
    public Detector createDetector(ConstantProvider cp) {
        FTOFDetector detector = new FTOFDetector();
        System.out.println(">>> creating layer FTOF.....");
        this.constrcutFTOF(detector, cp);
        return detector;
    }

    private void constrcutFTOF1A(Detector detector, ConstantProvider cp){
        Point3D edge = new Point3D();
        double rotationY      = Math.toRadians(25.0);
        
        double width          = 15.01;
        double thikness       =  5.08;
        
        double dist2edge      = cp.getDouble("/geometry/ftof/panel1a/panel/dist2edge", 0);
        double theta_min      = cp.getDouble("/geometry/ftof/panel1a/panel/thmin", 0);
        double tiltedOffset   = dist2edge*Math.cos(rotationY - Math.toRadians(theta_min));
        System.out.println("DIST 2 EDGE =  " +  dist2edge + " TILTED OFFSET = " + tiltedOffset );
        int    nPaddles       = cp.length("/geometry/ftof/panel1a/paddles/Length");
        edge.set(dist2edge*Math.sin(Math.toRadians(theta_min)), 0, dist2edge*Math.cos(Math.toRadians(theta_min)));
        edge.show();
        
        edge.rotateY(-rotationY);
        edge.show();
        double xPositionStart = edge.x() + cp.getDouble("/geometry/ftof/panel1a/panel/paddlewidth", 0)*0.5;
        double xPositionStep  = 
                cp.getDouble("/geometry/ftof/panel1a/panel/paddlewidth", 0) +
                cp.getDouble("/geometry/ftof/panel1a/panel/gap", 0) +
                cp.getDouble("/geometry/ftof/panel1a/panel/wrapperthickness", 0); 
        
        G4Detector g4d = detector.getGeantDetector();
        // Create volumes SOLIDS (box) for paddles
        // and define volumes to be used in each sector.
        for(int i = 0; i < nPaddles; i++){
            double length = cp.getDouble("/geometry/ftof/panel1a/paddles/Length",i);
            Geant4Solid paddle = new Geant4Solid(Geant4Shape.BOX, new double[]{length,thikness,width});
            paddle.setId(0,1,i+1); // 0 means used in all sectors, 1 - layer one (1A), i+1 paddle number
            g4d.addSolid(paddle);
            G4Volume volume = new G4Volume(0,1,i+1);
            volume.solid(G4Unit.construct(0,1,i+1));
            g4d.addVolume(G4Unit.construct(0,1,i+1), volume);
        }
        
        double  p1a_L = cp.getDouble("/geometry/ftof/panel1a/paddles/Length",0);
        double p23a_L = cp.getDouble("/geometry/ftof/panel1a/paddles/Length",nPaddles-1);
        double height = (nPaddles+1)*width;
        
        for(int sector = 0; sector < 6; sector++){
            
            Geant4Solid secMother = new Geant4Solid(Geant4Shape.TRD, new double[]{p1a_L,p23a_L,thikness*1.5, thikness*1.5,height});
            secMother.setId(sector+1,1,0);
            g4d.addSolid(secMother);
            
            G4LogVolume secVolume = new G4LogVolume();
            secVolume.setId(sector+1,1,0);
            secVolume.setSolidRef(G4Unit.construct(sector+1,1,0));
            
            double moveX = sectorPosition[sector].x();
            double moveY = sectorPosition[sector].y();
            double moveZ = sectorPosition[sector].z();
            
            if(cp.hasConstant("/geometry/ftof/alignment/deltaX")==true){
                moveX += cp.getDouble("/geometry/ftof/alignment/deltaX", sector);
            }
            
            if(cp.hasConstant("/geometry/ftof/alignment/deltaY")==true){
                moveY += cp.getDouble("/geometry/ftof/alignment/deltaY", sector);
            }
            
            if(cp.hasConstant("/geometry/ftof/alignment/deltaZ")==true){
                moveZ += cp.getDouble("/geometry/ftof/alignment/deltaZ", sector);
            }
            
            secVolume.setPosition(
                    moveX,moveY,moveZ
                    );
            
            double rotX = sectorRotation[sector].x();
            double rotY = sectorRotation[sector].y();
            double rotZ = sectorRotation[sector].z();
            
            if(cp.hasConstant("/geometry/ftof/alignment/rotX")==true){
                rotX += cp.getDouble("/geometry/ftof/alignment/rotX", sector);
            }
            
            if(cp.hasConstant("/geometry/ftof/alignment/rotY")==true){
                rotY += cp.getDouble("/geometry/ftof/alignment/rotY", sector);
            }
            
            if(cp.hasConstant("/geometry/ftof/alignment/rotZ")==true){
                rotZ += cp.getDouble("/geometry/ftof/alignment/rotZ", sector);
            }
            
            secVolume.setRotation(rotX,rotY,rotZ);
                        
            double offset = -nPaddles*width/2.0;
            for(int paddle = 0 ; paddle < nPaddles; paddle++){
                G4PhysVolume paddlePhysVol = new G4PhysVolume(0,1,paddle+1);
                paddlePhysVol.setVolumeRef(G4Unit.construct(0,1,paddle+1));
                paddlePhysVol.setPosition(0.0, 0.0, offset);
                offset += width;
                secVolume.addVolume(paddlePhysVol);
            }
            g4d.addLogVolume(G4Unit.construct(sector+1,1,0), secVolume);
        }
        
    }
    
    private void constrcutFTOF(FTOFDetector detector, ConstantProvider cp){
        
        int nSectors = detector.getCount();
        int nLayers  = detector.getCount(0);
        System.out.println("Building detector with " + nSectors + " sectors and " + nLayers + " layers");
        
        // for each layers
        for(int ilayer=0; ilayer<nLayers; ilayer++) {
            int layer = ilayer+1;
            double thtilt    = cp.getDouble(stringLayers[ilayer] + "/panel/thtilt", 0);
            double thmin     = cp.getDouble(stringLayers[ilayer] + "/panel/thmin", 0);
            double dist2edge = cp.getDouble(stringLayers[ilayer] + "/panel/dist2edge", 0);
            double gap       = cp.getDouble(stringLayers[ilayer] + "/panel/gap", 0);
            double pairgap   = 0;
            if(layer==2) pairgap = cp.getDouble(stringLayers[ilayer] + "/panel/pairgap", 0);
        
            // create paddles as G4BOX and add them to detector
            int nPaddles = detector.componentsInLayer(layer);
            double width      = cp.getDouble(stringLayers[layer - 1] + "/panel/paddlewidth", 0);
            double thickness  = cp.getDouble(stringLayers[layer - 1] + "/panel/paddlethickness", 0);
            G4Detector g4d = detector.getGeantDetector();
            for(int i = 0; i < nPaddles; i++){
                int component = i+1;
                double length = cp.getDouble(stringLayers[ilayer] + "/paddles/Length",i);
                Geant4Solid paddle = new Geant4Solid(Geant4Shape.BOX, new double[]{length/2,thickness/2,width/2});
                paddle.setId(0,layer,component); // 0 means used in all sectors, 1 - layer one (1A), i+1 paddle number
                g4d.addSolid(paddle);
                G4Volume volume = new G4Volume(0,layer,component);
                volume.solid(G4Unit.construct(0,layer,component));
                g4d.addVolume(G4Unit.construct(0,layer,component), volume);
            }

//            int ipair = (int) ipaddle/2;
//            double zoffset = (ipaddle - numPaddles / 2. + 0.5) * (paddlewidth + gap);
//            if(layer==2) zoffset = (ipair - numPaddles/4-0.5) * (2*paddlewidth + gap + pairgap) + ((ipaddle%2)+0.5) * (paddlewidth + gap);

            // for each sector
            for(int isector = 0; isector < nSectors;  isector++){
                int sector = isector+1;
                
                // create mother volumes as G4TRDs
                double shortedge =  cp.getDouble(stringLayers[ilayer] + "/paddles/Length",0);
                double longedge  = (cp.getDouble(stringLayers[ilayer] + "/paddles/Length",nPaddles-1) +
                                    cp.getDouble(stringLayers[ilayer] + "/paddles/Length",nPaddles-1) -
                                    cp.getDouble(stringLayers[ilayer] + "/paddles/Length",nPaddles-2));
                double height = (nPaddles * width + (nPaddles-1) * gap);
                if(layer==2) height = height + nPaddles/2 * pairgap; //FIXME PROBABLE WRONG
                Geant4Solid secMother = new Geant4Solid(Geant4Shape.TRD, new double[]{shortedge/2+motherGap,
                                                                                      longedge/2+motherGap,
                                                                                      thickness/2+motherGap, 
                                                                                      thickness/2+motherGap,
                                                                                      height/2+motherGap});
                secMother.setId(sector,layer,0);
                g4d.addSolid(secMother);
                
                G4LogVolume secVolume = new G4LogVolume();
                secVolume.setId(sector,layer,0);
                secVolume.setSolidRef(G4Unit.construct(sector,layer,0));
                
                // define rotations
                int index = isector*nLayers + ilayer;
                double rotX = cp.getDouble("/geometry/ftof/alignment/rotX", index);
                double rotY = cp.getDouble("/geometry/ftof/alignment/rotY", index);
                double rotZ = cp.getDouble("/geometry/ftof/alignment/rotZ", index);
//                secVolume.setRotation(rotX,rotY,rotZ);
//                secVolume.setRotation(180, thtilt-90, -90);
                Point3D myrot = new Point3D(Math.toRadians(-30.0 - sector * 60.0), Math.toRadians(0.0), Math.toRadians(-90-thtilt));
                myRotationMatrix r = new myRotationMatrix();
                r.setZYX(myrot.x(), myrot.y(), myrot.z());
                r.show();
                System.out.println(Math.toDegrees(myrot.x()) + " " + Math.toDegrees(myrot.y()) + " " + Math.toDegrees(myrot.z()));
                System.out.println(Math.toDegrees(r.getRotations()[0]) + " " + Math.toDegrees(r.getRotations()[1]) + " " + Math.toDegrees(r.getRotations()[2]));  
                System.out.println(sectorRotation[isector].x() + " " + sectorRotation[isector].y() + " " + sectorRotation[isector].z());
                
//                secVolume.setRotation(sectorRotation[isector].x(), sectorRotation[isector].y(), sectorRotation[isector].z());
                secVolume.setRotation(Math.toDegrees(r.getRotations()[0]), Math.toDegrees(r.getRotations()[1]), Math.toDegrees(r.getRotations()[2]));
                    
                // define shifts to positions mother volumes
                // get alignment shifts from CCDB (these are defined in the tilted sector coordinate frame)
                double deltaX = cp.getDouble("/geometry/ftof/alignment/deltaX", index);
                double deltaY = cp.getDouble("/geometry/ftof/alignment/deltaY", index);
                double deltaZ = cp.getDouble("/geometry/ftof/alignment/deltaZ", index);
                // define position for sector 1
                double moveX = dist2edge * Math.sin(Math.toRadians(thmin)) + 
                             (height/2 + deltaX) * Math.cos(Math.toRadians(thtilt)) + 
                             ((thickness+motherGap)/2 + deltaZ) * Math.sin(Math.toRadians(thtilt));
                double moveY = deltaY;
                double moveZ = dist2edge * Math.cos(Math.toRadians(thmin)) - 
                             (height/2 + deltaX) * Math.sin(Math.toRadians(thtilt)) + 
                             ((thickness+motherGap)/2 + deltaZ) * Math.cos(Math.toRadians(thtilt));
                Point3D panelCenter = new Point3D(moveX,moveY,moveZ);
                panelCenter.rotateZ(Math.toRadians(isector*60));
                secVolume.setPosition(panelCenter.x(),panelCenter.y(),panelCenter.z());
                
//                double offset = -nPaddles*width/2.0;
                for(int i = 0 ; i < nPaddles; i++){
                    int paddle = i+1;
                    int ipair = (int) i/2;
                    double offset = (i - nPaddles / 2. + 0.5) * (width + gap);
                    if(layer==2) offset = (ipair - nPaddles/4-0.5) * (2*width + gap + pairgap) + ((i%2)+0.5) * (width + gap);
                    G4PhysVolume paddlePhysVol = new G4PhysVolume(0,layer,paddle);
                    paddlePhysVol.setVolumeRef(G4Unit.construct(0,layer,paddle));
                    paddlePhysVol.setPosition(0.0, 0.0, offset);
//                    offset += width;
                    secVolume.addVolume(paddlePhysVol);
                }

                g4d.addLogVolume(G4Unit.construct(sector,layer,0), secVolume);
            }
                
        }
        
    }
    
    @Override
    public Detector createDetector(int run, String variation) {
        //ConstantProvider cp = FTOFConstants.getDetectorConstants();
        DatabaseConstantProvider provider = new DatabaseConstantProvider(10,"default");
        DetectorConstants dc = provider.read(
                Arrays.asList(new String[]{
                    "/geometry/ftof/panel1a/panel",
                    "/geometry/ftof/panel1a/paddles",
                    "/geometry/ftof/panel1b/panel",
                    "/geometry/ftof/panel1b/paddles",
                    "/geometry/ftof/panel2/panel",
                    "/geometry/ftof/panel2/paddles",
                    "/geometry/ftof/alignment"
                }));
        dc.show();
        provider.disconnect();
        return this.createDetector(dc);
    }

    @Override
    public String getType() {
        return "FTOF";
    }

    @Override
    public void show() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
