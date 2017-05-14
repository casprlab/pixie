package org.image.password.trinket.v1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class SharedMethods {
	public static Bitmap RotateBitmap(Bitmap source, float angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(),
				source.getHeight(), matrix, true);
	}
	
	public static boolean cropImage(int left, int top, int right, int botton,
			String filePath, String croppedFilePath) {

		boolean success = false;

		Mat image = Highgui.imread(filePath);
		int width = (int) image.size().width;// display.getWidth();

		int height = (int) image.size().height;// display.getHeight();
		Rect roi = new Rect(left, top, right, botton);
		Mat crop = new Mat(image, roi); // NOTE: this will only give you a
										// reference to the ROI of the
										// original data
		Mat output = crop.clone();

		Highgui.imwrite(croppedFilePath, output);

		return success;

	}
	
	// Checking if the original folder in the path exists
	public static boolean createDirIfNotExists(String folderName) {
		File folder = new File(Environment.getExternalStorageDirectory() + "/"
				+ folderName);
		boolean success = true;
		if (!folder.exists()) {
			success = folder.mkdir();
			return success;
		}
		return success;
	}

	// writing on the file
	public static void writeFileOnSDCard(String strWrite, String fileName) {

		try {

			String fullPath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			File myFile = new File(fullPath + "/Pixie/" + fileName);
			// File gpxfile = new File(root, "participants.txt");

			BufferedWriter bW;

			bW = new BufferedWriter(new FileWriter(myFile, true));
			bW.write(strWrite);
			bW.flush();
			bW.close();

		} catch (Exception e) {
			Log.e("Error", "Cannot access CrossSimilarity.txt file. \n");
		}
	}
	
	public double eucDist(Point p1, Point p2) {

		double dis = Math.sqrt(Math.pow(p1.x - p2.x, 2)
				+ Math.pow(p1.y - p2.y, 2));
		return dis;

	}
	

	public static void writeStringAsFile(final String fileContents, String fileName) {
		String fullPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
        try {
            FileWriter out = new FileWriter(new File(fullPath + "/Pixie/" + fileName));
            out.write(fileContents);
            out.close();
        } catch (IOException e) {
        }
    }

    public static String readFileAsString(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in = null;
        String fullPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
        try {
            in = new BufferedReader(new FileReader(new File(fullPath + "/Pixie/" + fileName)));
            if ((line = in.readLine()) != null) 
            	stringBuilder.append(line);
            in.close();

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } 

        return stringBuilder.toString();
    }
}
