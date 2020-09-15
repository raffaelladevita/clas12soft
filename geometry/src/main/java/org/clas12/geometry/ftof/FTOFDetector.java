/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas12.geometry.ftof;

import org.jlab.jnp.detector.abs.AbstractDetector;
import org.jlab.jnp.detector.base.Component;
import org.jlab.jnp.detector.base.DetectorFrame;
import org.jlab.jnp.detector.component.ScintilatorPaddle;
import org.jlab.jnp.geom.geant4.G4Detector.G4Position;
import org.jlab.jnp.geom.geant4.G4Detector.G4Rotation;
import org.jlab.jnp.geom.geant4.G4LogVolume;
import org.jlab.jnp.geom.geant4.G4PhysVolume;
import org.jlab.jnp.geom.geant4.G4Unit;
import org.jlab.jnp.geom.geant4.G4Volume;
import org.jlab.jnp.geom.geant4.Geant4Solid;
import org.jlab.jnp.geom.prim.Mesh3D;

/**
 *
 * @author gavalian
 */
public class FTOFDetector extends AbstractDetector<ScintilatorPaddle> {
    
    public int componentsInLayer(int layer){
       switch(layer){
           case 1: return 23;
           case 2: return 62;
           case 3: return 5;
           default: break;
       } 
       return 0;
    }
    
    @Override
    public int getCount(int... ids){
        int il = ids.length;
        switch(il){
            case 0: return 6;
            case 1: return 3;
            case 2: return componentsInLayer(ids[1]);
            default: break;
        }
        return 0;
    }
    
    @Override
    public Mesh3D getBoundary(Mesh3D mesh, DetectorFrame frame, int... ids) {
        long  id = G4Unit.construct(ids[0],ids[1],0);
        
        G4LogVolume lv = getGeantDetector().getLogicalVolumes().get(id);
        long ref = lv.getSolidRef();
        Geant4Solid solid = getGeantDetector().getSolids().get(ref);//.getMesh();
        
        //solid.getMesh().show();
        mesh.copyFrom(solid.getMesh());
         // This is because Geant  trapezoid parameters are half
        // lengths, but GDML requires full length         
        mesh.scale(0.5, 0.5, 0.5);

        lv.transform(mesh);
        //mesh.show();
        /*
        G4Rotation rot = lv.getRotation();
        G4Position pos = lv.getPosition();

        mesh.rotateZ(-Math.toRadians(rot.getZ()));
        mesh.rotateY(-Math.toRadians(rot.getY()));
        mesh.rotateX(-Math.toRadians(rot.getX()));

        mesh.translateXYZ(pos.getX(), pos.getY(), pos.getZ());*/
        //mesh.show();
        return mesh;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Component getComponent(Component comp, DetectorFrame frame, int... ids) {
        
        long  id = G4Unit.construct(ids[0],ids[1],0);        
        G4LogVolume lv = getGeantDetector().getLogicalVolumes().get(id);
        
        G4PhysVolume pv = lv.getVolume(ids[2]-1);
        long volRef     = pv.getVolumeRef();
        
        G4Volume     g4v = this.getGeantDetector().getVolumes().get(volRef);
        long   solidRef  = g4v.solid();
        Geant4Solid  solid = getGeantDetector().getSolids().get(solidRef);
        
        comp.getMesh().copyFrom(solid.getMesh());
        
        pv.transform(comp.getMesh());
        lv.transform(comp.getMesh());
        
        return comp;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
