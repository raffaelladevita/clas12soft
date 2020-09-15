/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas12.geometry.ftof;

import java.util.ArrayList;
import java.util.List;
import org.jlab.jnp.detector.base.Detector;
import org.jlab.jnp.detector.base.DetectorFrame;
import org.jlab.jnp.detector.base.DetectorHit;
import org.jlab.jnp.detector.base.DetectorManager;
import org.jlab.jnp.detector.base.DetectorType;
import org.jlab.jnp.detector.component.ScintilatorPaddle;
import org.jlab.jnp.geom.geant4.GDMLExporter;
import org.jlab.jnp.geom.prim.Mesh3D;
import org.jlab.jnp.geom.prim.Path3D;
import org.jlab.jnp.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */
public class FTOFDebug {
    
    
    public static void intersect(Detector det, int iter){
        
        Mesh3D boundary = new Mesh3D(8,12);
        Path3D path     = new Path3D();
        ScintilatorPaddle comp = new ScintilatorPaddle();
        
        List<Point3D> intersections = new ArrayList<>();
        List<DetectorHit> hits      = new ArrayList<>();
                path.generateRandom(0.0, 0.0, 0.0, 
                        Math.toRadians(20.0), Math.toRadians(20.0), 
                        Math.toRadians(0.0), 
                        Math.toRadians(0.0), 
                        800.0, 2);
        for(int loop = 1; loop < 62; loop++){

            for(int isector = 0; isector < 1; isector++){
                
                intersections.clear();
//                det.getBoundary(boundary, DetectorFrame.CLAS, isector+1,2,0);
                det.getComponent(comp, DetectorFrame.CLAS, isector+1,2,loop);
                
                
//                boundary.intersectionPath(path, intersections);
                comp.getMesh().intersectionPath(path, intersections);
                /*System.err.println(String.format(">>>> sector %4d : intersections = %3d\n",
                        i+1, intersections.size()));*/
                if(intersections.size()>0){
                    //if(i==0||i==3)
                    System.out.println((isector+1) + " " + " " + loop);
                    System.out.println(intersections.size());
                    if(intersections.size()<2) continue;
                    System.out.printf("%8.5f %8.5f %8.5f\n",
                            intersections.get(0).x(),
                            intersections.get(0).y(),
                            intersections.get(0).z()                            
                            );
                    System.out.printf("%8.5f %8.5f %8.5f\n",
                            intersections.get(1).x(),
                            intersections.get(1).y(),
                            intersections.get(1).z()                            
                            );
                    System.out.println(intersections.get(1).distance(intersections.get(0)));
                }
                
                
                hits.clear();
                hits=det.getLayerHits(path);
                if(hits.size()>0){
                    //if(i==0||i==3)
                    System.out.println(hits.size());
                    System.out.printf("%8.5f %8.5f %8.5f\n",
                            hits.get(0).position.x(),
                            hits.get(0).position.y(),
                            hits.get(0).position.z()                            
                            );
                }
                
            }
        }
    }
    public static void main(String[] args){
        
        DetectorManager.getInstance().addFactory(DetectorType.FTOF, new FTOFFactory());
        
        Detector ftof = DetectorManager.getInstance().getDetector(DetectorType.FTOF, 10, "default");
        
        /*System.out.println(ftof.getGeantDetector().getGDML());
        System.out.println(ftof.getGeantDetector().getGDMLSolids());
        System.out.println(ftof.getGeantDetector().getGDMLStructure());        
        */
        GDMLExporter output = new GDMLExporter();
        output.export(ftof.getGeantDetector(),"ftof_components.gdml");
        System.out.println("GDML geometry created");
        FTOFDebug.intersect(ftof,50);
       /* String gdmlDefine = ftof.getGDML();
        String gdmlSolids = ftof.getGDMLSolids();
        String gdmlStructure = ftof.getGDMLStructure();
        
        System.out.println(gdmlDefine);
        System.out.println(gdmlSolids);
        
        System.out.println(gdmlStructure);*/
    }
}
