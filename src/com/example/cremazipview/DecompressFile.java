package com.example.cremazipview;

import android.os.Bundle;
import android.content.Context;
import android.app.Activity;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.Display;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipInputStream;
import java.io.FileNotFoundException;

import javaZLibrary.*;

class ZipFileIO {

	/**
	 * No instances needed.
	 */
	public ZipFileIO() {
		// don't instantiate this class
	}

	/**
	 * Extract zip file to destination folder.
	 * 
	 * @param file
	 *            zip file to extract
	 * @param destination
	 *            destinatin folder
	 */
	public static void extract(File file, File destination) throws IOException {
		ZipInputStream in = null;
		FileOutputStream out = null;
		try {
			// Open the ZIP file
			in = new ZipInputStream(new FileInputStream(file));
			System.out.println("new ZipInputStream");

			// Get the first entry
			ZipEntry entry = null;
			
			if(!destination.isDirectory()){
				destination.mkdirs();
			}

			while ((entry = in.getNextEntry()) != null ) {
				String outFilename = entry.getName();
				
				while(true){
					//if((!outFilename.contains(new String("META-INF")) && !outFilename.contains(new String("mimetype"))) && !outFilename.contains(new String("thum")))
					if(outFilename.contains(new String("jpg")) && !outFilename.contains(new String("thum")))
						break;
					entry = in.getNextEntry();
					if(entry == null)
						break;
					outFilename= entry.getName();
				}
				if(entry == null)
					break;
				if((new File(outFilename)).exists())
					break;
				
				System.out.println("outFilename: " + outFilename);

				// Open the output file
				if (entry.isDirectory()) {
					new File(destination, outFilename).mkdirs();
					System.out.println("new directory");
					System.out.println("IT IS A DIRECTORY");
				} else {
					out = new FileOutputStream(new File(destination,
							outFilename));

					System.out.println("new fileoutputStream");

					// Transfer bytes from the ZIP file to the output file
					byte[] buf = new byte[4096];
					int len;

					System.out.println("file output");
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
						// System.out.println("file output");
					}

					// Close the stream
					out.close();
				}
			}
		} finally {
			// Close the stream
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
		System.out.println("complete");
	}

	/**
	 * Return the first directory of this archive. This is needed to determine
	 * the plugin directory.
	 * 
	 * @param zipFile
	 * @return <class>File</class> containing the first entry of this archive
	 */
	public static File getFirstFile(File zipFile) throws IOException {
		// System.out.println("getFirstFile enter");
		ZipInputStream in = null;
		try {
			// Open the ZIP file
			in = new ZipInputStream(new FileInputStream(zipFile));
			System.out.println("Atfirst new zipinputstream");

			// Get the first entry
			ZipEntry entry = null;

			while ((entry = in.getNextEntry()) != null) {
				String outFilename = entry.getName();
				System.out.println("Atfirst outFilename: " + outFilename);

				if (entry.isDirectory()) {
					return new File(outFilename);
				}
			}
		} finally {
			if (in != null) {
				// Close the stream
				in.close();
			}
		}
		return null;
	}
}

public class DecompressFile {
	
	private String filePath;
	private String fileLocation;
	
	public DecompressFile(){
	}
	
	public void getFile(String file){
		filePath= file;
	}
	
	public void getLocation(String location){
		fileLocation= location;
	}
	
	public void decompressInputFile(){
		ZipFileIO zipfile= new ZipFileIO();
		try{
		//zipfile.extract(new File(filePath), new File(filePath.substring(0, filePath.indexOf('.'))+'/'));
			zipfile.extract(new File(filePath), new File(fileLocation));
		}
		catch(IOException e){
			System.out.println("FILE IO EXCEPTION");
		}
	}

	/*@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button button = (Button) findViewById(R.id.decompress);

		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				ZipFileIO a = new ZipFileIO();
				try {
					long start, end;
					start = System.currentTimeMillis();
					a.extract(new File("/sdcard/Download/sample.zip"),
							new File("/sdcard/Download/sample/"));
					end = System.currentTimeMillis();
					System.out.println(end - start);
					Toast.makeText(MainActivity.this.getApplicationContext(),
							"extract complete", Toast.LENGTH_SHORT).show();
				} catch (IOException e) {

				}

			}
		});
	}

	/*
	 * private static boolean contentsDataUnZip(Context context) { boolean
	 * rtnValue = false; FileInputStream fis = null; FileOutputStream fos =
	 * null; ZipInputStream zis = null; ZipEntry zentry = null; File zipFile =
	 * null; File targetFile = null;
	 * 
	 * Resources objResources = context.getResources();
	 * 
	 * String[] arr_zipfile_name = null; arr_zipfile_name = objResources
	 * .getStringArray(smart.android.neighbor_001.R.array.arr_contents_file);
	 * 
	 * for (int arrCnt = 0; arr_zipfile_name.length > arrCnt; arrCnt++) {
	 * Log.i("contentsFilePath::::::::::::", "1111" + arr_zipfile_name[arrCnt]);
	 * 
	 * zipFile = new File(arr_zipfile_name[arrCnt]); try { fis = new
	 * FileInputStream(zipFile); zis = new ZipInputStream(fis); //
	 * ZipInputStream
	 * 
	 * try { while ((zentry = zis.getNextEntry()) != null) { String
	 * fileNameToUnzip = "/sdcard/Download/image/" + zentry.getName();
	 * targetFile = new File(fileNameToUnzip); new
	 * File(targetFile.getParent()).mkdir(); fos = new
	 * FileOutputStream(targetFile); byte[] buffer = new byte[1024]; int len =
	 * 0; while ((len = zis.read(buffer)) != -1) { fos.write(buffer, 0, len); }
	 * fos.close(); fileNameToUnzip = ""; targetFile = null; }// end while }
	 * catch (IOException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }// end while
	 * 
	 * } catch (FileNotFoundException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } // FileInputStream zipFile = null; }// end for
	 * 
	 * rtnValue = true; return rtnValue; }// end function
	 */

}
