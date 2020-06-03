/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas12.geometry.ftof;

import java.util.Arrays;
import org.clas12.geometry.db.DatabaseConstantProvider;
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
        System.out.println(">>> creating layer 1a.....");
        this.constrcutFTOF1A(detector, cp);
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
    
    @Override
    public Detector createDetector(int run, String variation) {
        //ConstantProvider cp = FTOFConstants.getDetectorConstants();
        DatabaseConstantProvider provider = new DatabaseConstantProvider(10,"default");
        DetectorConstants dc = provider.read(
                Arrays.asList(new String[]{
                    "/geometry/ftof/panel1a/panel",
                    "/geometry/ftof/panel1a/paddles"
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
