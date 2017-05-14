package org.image.password.trinket.v1;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.StringTokenizer;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.KeyPoint;

import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class OpenCVObjectsToJSON {

	public static String matToJson(Mat mat){        
	    JsonObject obj = new JsonObject();

	    if(mat.isContinuous()){
	        int cols = mat.cols();
	        int rows = mat.rows();
	        int elemSize = (int) mat.elemSize();    

	        byte[] data = new byte[cols * rows * elemSize];

	        mat.get(0, 0, data);

	        obj.addProperty("rows", mat.rows()); 
	        obj.addProperty("cols", mat.cols()); 
	        obj.addProperty("type", mat.type());

	        // We cannot set binary data to a json object, so:
	        // Encoding data byte array to Base64.
	        String dataString = new String(Base64.encode(data, Base64.DEFAULT));

	        obj.addProperty("data", dataString);            

	        Gson gson = new Gson();
	        String json = gson.toJson(obj);

	        return json;
	    } else {
	        Log.e("JSONReader", "Mat not continuous.");
	    }
	    return "{}";
	}
	
	public static Mat matFromJson(String json){
	    JsonParser parser = new JsonParser();
	    JsonObject JsonObject = parser.parse(json).getAsJsonObject();

	    int rows = JsonObject.get("rows").getAsInt();
	    int cols = JsonObject.get("cols").getAsInt();
	    int type = JsonObject.get("type").getAsInt();

	    String dataString = JsonObject.get("data").getAsString();       
	    byte[] data = Base64.decode(dataString.getBytes(), Base64.DEFAULT); 

	    Mat mat = new Mat(rows, cols, type);
	    mat.put(0, 0, data);

	    return mat;
	}
	
	/*
	public static String matOfKeyPointToJson(MatOfKeyPoint mat){        
	    JsonObject obj = new JsonObject();

	    if(mat.isContinuous()){
	        int cols = mat.cols();
	        int rows = mat.rows();
	        int elemSize = (int) mat.elemSize();    

	        byte[] data = new byte[cols * rows * elemSize];

	        mat.get(0, 0, data);

	        obj.addProperty("rows", mat.rows()); 
	        obj.addProperty("cols", mat.cols()); 
	        obj.addProperty("type", mat.type());

	        // We cannot set binary data to a json object, so:
	        // Encoding data byte array to Base64.
	        String dataString = new String(Base64.encode(data, Base64.DEFAULT));

	        obj.addProperty("data", dataString);            

	        Gson gson = new Gson();
	        String json = gson.toJson(obj);

	        return json;
	    } else {
	        Log.e("JSONReader", "Mat not continuous.");
	    }
	    return "{}";
	}
	
	public static MatOfKeyPoint matOfKeyPointFromJson(String json){
	    JsonParser parser = new JsonParser();
	    JsonObject JsonObject = parser.parse(json).getAsJsonObject();

	    int rows = JsonObject.get("rows").getAsInt();
	    int cols = JsonObject.get("cols").getAsInt();
	    int type = JsonObject.get("type").getAsInt();

	    String dataString = JsonObject.get("data").getAsString();       
	    byte[] data = Base64.decode(dataString.getBytes(), Base64.DEFAULT); 

	    Mat mat = new Mat(rows, cols, type);
	    mat.put(0, 0, data);

	    MatOfKeyPoint matKP = new MatOfKeyPoint(mat);
	    return matKP;
	}
*/

//----------------------------------------------- from write kp to file
	public static String dMatchToJson(List<DMatch>  mat){
	    if(mat!=null){          
	        Gson gson = new Gson();

	        JsonArray jsonArr = new JsonArray();            
	        for (int counter = 0 ; counter < mat.size(); counter ++){
	            JsonObject obj = new JsonObject();
	            obj.addProperty("imgIdx", mat.get(counter).imgIdx); 
	            obj.addProperty("queryIdx", mat.get(counter).queryIdx);
	            obj.addProperty("trainIdx", mat.get(counter).trainIdx);
	            obj.addProperty("distance", mat.get(counter).distance);

	            jsonArr.add(obj); 
	            
	    }
	        
			String json = gson.toJson(jsonArr);   
			
			//write to file
	        /*FileWriter file;
			try {
				file = new FileWriter("/Users/mbani002/Desktop/" + fileName + ".txt");
				file.write(json);
	            System.out.println("Successfully Copied JSON Object to File...");
	            System.out.println("\nJSON Object: " + json);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
	        
	        return json;
	    }
	    return "{}";
	}
	
	public static String keypointsToJson(MatOfKeyPoint mat){
	    if(mat!=null && !mat.empty()){          
	        Gson gson = new Gson();

	        JsonArray jsonArr = new JsonArray();            

	        KeyPoint[] array = mat.toArray();
	        for(int i=0; i<array.length; i++){
	            KeyPoint kp = array[i];

	            JsonObject obj = new JsonObject();

	            obj.addProperty("class_id", kp.class_id); 
	            obj.addProperty("x",        kp.pt.x);
	            obj.addProperty("y",        kp.pt.y);
	            obj.addProperty("size",     kp.size);
	            obj.addProperty("angle",    kp.angle);                          
	            obj.addProperty("octave",   kp.octave);
	            obj.addProperty("response", kp.response);

	            jsonArr.add(obj);               
	        }

	        String json = gson.toJson(jsonArr);         

	        /*FileWriter file;
			try {
				file = new FileWriter("/Users/mbani002/Desktop/" + fileName + ".txt");
				file.write(json);
	            System.out.println("Successfully Copied JSON Object to File...");
	            System.out.println("\nJSON Object: " + json);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
	        
	        return json;
	    }
	    return "{}";
	}

	public static MatOfKeyPoint keypointsFromJson(String json){
	    MatOfKeyPoint result = new MatOfKeyPoint();

	    JsonParser parser = new JsonParser();
	    JsonArray jsonArr = parser.parse(json).getAsJsonArray();        

	    int size = jsonArr.size();

	    KeyPoint[] kpArray = new KeyPoint[size];

	    for(int i=0; i<size; i++){
	        KeyPoint kp = new KeyPoint(); 

	        JsonObject obj = (JsonObject) jsonArr.get(i);

	        Point point = new Point( 
	                obj.get("x").getAsDouble(), 
	                obj.get("y").getAsDouble() 
	        );          

	        kp.pt       = point;
	        kp.class_id = obj.get("class_id").getAsInt();
	        kp.size     =     obj.get("size").getAsFloat();
	        kp.angle    =    obj.get("angle").getAsFloat();
	        kp.octave   =   obj.get("octave").getAsInt();
	        kp.response = obj.get("response").getAsFloat();

	        kpArray[i] = kp;
	    }

	    result.fromArray(kpArray);

	    return result;
	}

	public static String intArrayToString(int[] data){
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
		    str.append(data[i]).append(",");
		}
		return str.toString();
	}

	public static int[] stringToIntArray(String string, int size){
		StringTokenizer st = new StringTokenizer(string, ",");
		int[] savedList = new int[size];
		for (int i = 0; i < size; i++) {
		    savedList[i] = Integer.parseInt(st.nextToken());
		}
		return savedList;
	}

	public static String doubleArrayToString(double[] data){
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
		    str.append(data[i]).append(",");
		}
		return str.toString();
	}

	public static double[] stringToDoubleArray(String string, int size){
		StringTokenizer st = new StringTokenizer(string, ",");
		double[] savedList = new double[size];
		for (int i = 0; i < size; i++) {
		    savedList[i] = Double.parseDouble(st.nextToken());
		}
		return savedList;
	}

	public static String double2DArrayToString(double[][] data){
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < data[0].length; i++) {
			for(int j = 0 ; j < data[0].length; j++)
				str.append(data[i][j]).append(",");
		}
		return str.toString();
	}

	public static double[][] stringToDouble2DArray(String string, int size){
		StringTokenizer st = new StringTokenizer(string, ",");
		double[][] savedList = new double[size][size];
		for (int i = 0; i < size; i++) {
			for(int j = 0 ; j < size; j++)
				savedList[i][j] = Double.parseDouble(st.nextToken());
		}
		return savedList;
	}

}
