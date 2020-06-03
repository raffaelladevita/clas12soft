{
  //TGeoManager::Import("test.gdml");
  //TGeoManager::Import("ftof_boundary.gdml");
  //TGeoManager::Import("ftof_boundary_allign.gdml");
  //TGeoManager::Import("ftof_components.gdml");
  TGeoManager::Import("ftof_components_allign.gdml");
  gGeoManager->GetTopVolume()->Draw("ogl");
}
