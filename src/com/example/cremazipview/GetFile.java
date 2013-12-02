package com.example.cremazipview;

import android.os.Bundle;
import android.content.Context;
import android.app.Activity;
import android.graphics.Point;
import android.util.Log;
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

public class GetFile {

	private Context mContext;

	// private String filePath;
	// private String fileLocation;
	private String currentFile;

	private File file;
	private File destination;

	private ZipInputStream in;
	private ZipEntry entry;

	public GetFile() {
	}

	public void getFileInformation(String file, String location) {
		this.file = new File(file);
		this.destination = new File(location);
	}

	public String getFilename() {
		return currentFile;
	}

	public void openAndClose(int mode) {
		try {
			if (mode == 0) {
				in = new ZipInputStream(new FileInputStream(file));
				entry = null;
				System.out.println("new ZipInputStream");
			} else if (mode == 1) {
				in.close();
				System.out.println("close ZipInputStream");
			}
		} catch (IOException e) {
			Log.e("CremaZipViewer::FileUnzip", "file "
					+ (mode == 1 ? "open" : "close") + " error");
		}
	}

	public long getFileEntry(int index) throws IOException {
		in = new ZipInputStream(new FileInputStream(file));
		entry = null;
		FileOutputStream out = null;
		try {
			if (!destination.isDirectory())
				destination.mkdirs();
			
			for(int i=0; i<index-1; i++){
				entry= in.getNextEntry();
				if(entry.getName().contains("thum") || entry.getName().contains("mimetype") || entry.getName().contains("META-INF"))
					i--;
			}

			while (true) {
				entry = in.getNextEntry();
				if (entry.getName().contains("jpg")
						&& !entry.getName().contains("thum")
						&& !entry.getName().contains("mimetype")
						&& !entry.getName().contains("META-INF"))
					break;
				if (entry == null)
					break;
			}

			if (entry == null)
				return 2;
			if ((new File(entry.getName())).exists())
				return 3;

			if (entry.isDirectory()) {
				new File(destination, entry.getName()).mkdirs();
				System.out.println("new directory");
			} else {
				out = new FileOutputStream(new File(destination,
						entry.getName()));

				System.out.println("new fileoutputStream");

				// Transfer bytes from the ZIP file to the output file
				byte[] buf = new byte[4096];
				int len;

				System.out.println("file output");
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				// Close the stream
				out.close();
			}
		} catch (IOException e) {
			return -1;
		} finally {
			// Close the stream
			if (out != null)
				out.close();
			if (in != null)
				in.close();
			currentFile = entry.getName();
		}
		System.out.println("complete");

		return 1;
	}

	public static void decompress(File file, File destination)
			throws IOException {
		ZipInputStream in = null;
		FileOutputStream out = null;
		try {
			// Open the ZIP file
			in = new ZipInputStream(new FileInputStream(file));
			System.out.println("new ZipInputStream");

			// Get the first entry
			ZipEntry entry = null;

			if (!destination.isDirectory()) {
				destination.mkdirs();
			}

			while ((entry = in.getNextEntry()) != null) {
				String outFilename = entry.getName();

				while (true) {
					// if((!outFilename.contains(new String("META-INF")) &&
					// !outFilename.contains(new String("mimetype"))) &&
					// !outFilename.contains(new String("thum")))
					if (outFilename.contains(new String("jpg"))
							&& !outFilename.contains(new String("thum")))
						break;
					entry = in.getNextEntry();
					if (entry == null)
						break;
					outFilename = entry.getName();
				}
				if (entry == null)
					break;
				if ((new File(outFilename)).exists())
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

}
