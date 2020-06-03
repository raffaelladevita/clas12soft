/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas12.geometry.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import org.jlab.ccdb.Assignment;
import org.jlab.ccdb.CcdbPackage;
import org.jlab.ccdb.TypeTable;
import org.jlab.ccdb.TypeTableColumn;
import org.jlab.jnp.detector.base.DetectorConstants;

/**
 *
 * @author gavalian
 */
public class DatabaseConstantProvider {
    
    private boolean PRINT_ALL = true;
    private String variation  = "default";
    private Integer runNumber = 10;
    private Integer loadTimeErrors = 0;
    private Boolean PRINTOUT_FLAG  = false;
    private Integer dataYear       = 118;
    private Integer dataMonth      = 1;
    private Date    databaseDate   = new Date();
    
    private org.jlab.ccdb.JDBCProvider provider;
    
    //private List<String>  databaseTables = 
            
    private int          debugMode = 1;
    
    public DatabaseConstantProvider(int run, String var){
        
        this.loadTimeErrors = 0;
        this.runNumber = run;
        this.variation = var;
        
        String address = "mysql://clas12reader@clasdb.jlab.org/clas12";
        
        String envAddress = this.getEnvironment();        
        if(envAddress!=null) address = envAddress;
        this.connect(address);
    }
    
    public DatabaseConstantProvider(int run, String var, String timestamp){
        
        this.loadTimeErrors = 0;
        this.runNumber = run;
        this.variation = var;
        
        String address = "mysql://clas12reader@clasdb.jlab.org/clas12";
        
        String envAddress = this.getEnvironment();        
        if(envAddress!=null) address = envAddress;
        if(timestamp.length()>8){
            this.setTimeStamp(timestamp);
        }
        this.connect(address);
    }
    
        private String getEnvironment(){
        
        String envCCDB   = System.getenv("CCDB_DATABASE");
        String envCLAS12 = System.getenv("CLAS12DIR");
        String connection = System.getenv("CCDB_CONNECTION");
        
        if(connection!=null){
            return connection;
        }
        
        String propCLAS12 = System.getProperty("CLAS12DIR");
        String propCCDB   = System.getProperty("CCDB_DATABASE");
        
        //System.out.println("ENVIRONMENT : " + envCLAS12 + " " + envCCDB + " " + propCLAS12 + " " + propCCDB);
        
        if(envCCDB!=null&&envCLAS12!=null){
            StringBuilder str = new StringBuilder();
            str.append("sqlite:///");
            if(envCLAS12.charAt(0)!='/') str.append("/");
            str.append(envCLAS12);
            if(envCCDB.charAt(0)!='/' && envCLAS12.charAt(envCLAS12.length()-1)!='/'){
                str.append("/");
            }
            str.append(envCCDB);
            return str.toString();
        }
        
        if(propCCDB!=null&&propCLAS12!=null){
            StringBuilder str = new StringBuilder();
            str.append("sqlite:///");
            if(propCLAS12.charAt(0)!='/') str.append("/");
            str.append(propCLAS12);
            if(propCCDB.charAt(0)!='/' && propCLAS12.charAt(propCLAS12.length()-1)!='/'){
                str.append("/");
            }
            str.append(propCCDB);
            return str.toString();
        }
        
        return null;
    }
        
        private void connect(String address){
            provider = CcdbPackage.createProvider(address);
            if(debugMode>0){
                System.out.println("[DB] --->  open connection with : " + address);
                System.out.println("[DB] --->  database variation   : " + this.variation);
                System.out.println("[DB] --->  database run number  : " + this.runNumber);
                System.out.println("[DB] --->  database time stamp  : " + databaseDate);
            }
            
            provider.connect();
            
            if(provider.isConnected()==true){
                if(debugMode>0) System.out.println("[DB] --->  database connection  : success");
            } else {
                System.out.println("[DB] --->  database connection  : failed");
            }
            
        provider.setDefaultVariation(variation);
        provider.setDefaultDate(databaseDate);
        provider.setDefaultRun(this.runNumber);

        //Directory dir = provider.getDirectory("/calibration/ftof/");        
        //Assignment asgmt = provider.getData("/test/test_vars/test_table");
    }
    
    public final void setTimeStamp(String timestamp){
        String pattern = "MM/dd/yyyy-HH:mm:ss";
	if(timestamp.contains("-")==false){
	    pattern = "MM/dd/yyyy";
	}
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        
        try {
            databaseDate = format.parse(timestamp);
        } catch (ParseException ex) {
            System.out.println("\n\n ***** TIMESTAMP ERROR ***** error parsing timestamp : " + timestamp);
            databaseDate = new Date();
            System.out.println(" ***** TIMESTAMP WARNING ***** setting date to : " + databaseDate);

        }
        
    }
    
    public DetectorConstants read(List<String> tables){
        DetectorConstants dc = new DetectorConstants();
        for(String item : tables){
            readTable(dc,item);
        }
        return dc;
    }
    
    public void readTable(DetectorConstants cp, String table_name){
        try {
            Assignment asgmt = provider.getData(table_name);
            
            int ncolumns = asgmt.getColumnCount();
            TypeTable  table = asgmt.getTypeTable();
            Vector<TypeTableColumn> typecolumn = asgmt.getTypeTable().getColumns();
            System.out.println("[DB LOAD] ---> loading data table : " + table_name);
            System.out.println("[DB LOAD] ---> number of columns  : " + typecolumn.size());
            System.out.println();
            for(int loop = 0; loop < ncolumns; loop++){
                //System.out.println("Reading column number " + loop 
                //+ "  " + typecolumn.elementAt(loop).getCellType()
                //+ "  " + typecolumn.elementAt(loop).getName());
                String name = typecolumn.get(loop).getName();
                Vector<String> row = asgmt.getColumnValuesString(name);
                String[] values = new String[row.size()];
                for(int el = 0; el < row.size(); el++){
                    values[el] = row.elementAt(el);
                    //for(String cell: row){
                    //System.out.print(cell + " ");
                }
                
                String type = typecolumn.get(loop).getCellType().name();
                String cname = typecolumn.elementAt(loop).getName();
                
                System.out.printf("--> %s : %s\n",cname,type);
                StringBuilder str = new StringBuilder();
                str.append(table_name);
                str.append("/");
                str.append(typecolumn.elementAt(loop).getName());
                if(type.compareTo("DOUBLE")==0){
                    double[] data = new double[values.length];
                    for(int d = 0; d < values.length; d++){
                        data[d] = Double.parseDouble(values[d]);
                    }
                    cp.addDouble(str.toString(), data);
                }
                if(type.compareTo("INT")==0){
                    int[] data = new int[values.length];
                    for(int d = 0; d < values.length; d++){
                        data[d] = Integer.parseInt(values[d]);
                    }
                    cp.addInteger(str.toString(), data);
                }
                //constantContainer.put(str.toString(), values);
                //System.out.println(); //next line after a row
            }
            //provider.close();
        } catch (Exception e){
            System.out.println("[DB LOAD] --->  error loading table : " + table_name);
            this.loadTimeErrors++;
        }
    }
    public void disconnect(){
        System.out.println("[DB] --->  database disconnect  : success");
        this.provider.close();
    }
    
    public static void main(String[] args){
        DatabaseConstantProvider provider = new DatabaseConstantProvider(10,"default");
        DetectorConstants dc = provider.read(
                Arrays.asList(new String[]{
                    "/geometry/ftof/alignment"
                }));
        dc.show();
        
    }
}
