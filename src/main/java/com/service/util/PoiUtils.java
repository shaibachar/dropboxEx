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
import org.apache.poi.hpsf.UnexpectedPropertySetTypeException;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class PoiUtils {

	
	public void getInfo(String poiFilesystem) throws IOException, NoPropertySetStreamException, MarkUnsupportedException, UnexpectedPropertySetTypeException{
		InputStream is = new FileInputStream(poiFilesystem);
		POIFSFileSystem poifs = new POIFSFileSystem(is);
		is.close();

		DirectoryEntry dir = poifs.getRoot();
		DocumentSummaryInformation dsi;
		try
		{
		    DocumentEntry dsiEntry = (DocumentEntry)
		        dir.getEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
		    DocumentInputStream dis = new DocumentInputStream(dsiEntry);
		    PropertySet ps = new PropertySet(dis);
		    dis.close();
		    dsi = new DocumentSummaryInformation(ps);
		}
		catch (FileNotFoundException ex)
		{
		    /* There is no document summary information. We have to create a
		     * new one. */
		    dsi = PropertySetFactory.newDocumentSummaryInformation();
		}
	}
	
}
