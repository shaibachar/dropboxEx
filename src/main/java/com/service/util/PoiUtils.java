package com.service.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.UnexpectedPropertySetTypeException;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class PoiUtils {

	public DocumentSummaryInformation getInfo(String poiFilesystem) throws IOException, NoPropertySetStreamException, MarkUnsupportedException, UnexpectedPropertySetTypeException {
		InputStream is = new FileInputStream(poiFilesystem);
		POIFSFileSystem poifs = new POIFSFileSystem(is);
		is.close();

		DirectoryEntry dir = poifs.getRoot();
		DocumentSummaryInformation dsi;
		try {
			DocumentEntry dsiEntry = (DocumentEntry) dir.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
			DocumentInputStream dis = new DocumentInputStream(dsiEntry);
			PropertySet ps = new PropertySet(dis);
			dis.close();
			dsi = new DocumentSummaryInformation(ps);
			String shortDescription = poifs.getShortDescription();
			System.out.println("short:" + shortDescription);
		} catch (FileNotFoundException ex) {
			/*
			 * There is no document summary information. We have to create a new one.
			 */
			dsi = PropertySetFactory.newDocumentSummaryInformation();
		}

		return dsi;
	}

	public static void main(String[] args) throws IOException {
		final String filename = "c:\\temp\\shalom.doc";
		POIFSReader r = new POIFSReader();
		r.registerListener(new MyPOIFSReaderListener(), "\005SummaryInformation");
		r.read(new FileInputStream(filename));
	}

	private static class MyPOIFSReaderListener implements POIFSReaderListener
	{
	    public void processPOIFSReaderEvent(POIFSReaderEvent event)
	    {
	        SummaryInformation si = null;
	        try
	        {
	            si = (SummaryInformation)
	                 PropertySetFactory.create(event.getStream());
	        }
	        catch (Exception ex)
	        {
	            throw new RuntimeException
	                ("Property set stream \"" +
	                 event.getPath() + event.getName() + "\": " + ex);
	        }
	        final String title = si.getTitle();
	        final String author = si.getAuthor();
	        if (title != null){
	            System.out.println("Title: \"" + title + "\"");
	        }
	        else{
	            System.out.println("Document has no title.");
	        }
	        if (author !=null){
	        	System.out.println("author:"+ author);
	        }
	    }
	}
}
