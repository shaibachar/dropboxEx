package com.service.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.entity.WDCInformation;

@Component
public class PoiUtils {

	private final Logger logger = LoggerFactory.getLogger(PoiUtils.class);

	public void updateSIForDocXFile(String filepath, List<WDCInformation> info) throws IOException {
		FileInputStream stream;
		File file = new File(filepath);
		if (file.exists()) {
			WDCInformation entry = new WDCInformation();
			stream = new FileInputStream(file);
			XWPFDocument docx = new XWPFDocument(stream);
			CoreProperties props = docx.getProperties().getCoreProperties();
			entry.setFilePath(file.getPath());
			entry.setCreator(props.getCreator());
			entry.setLastModifiedByUser(props.getLastModifiedByUser());
			entry.setModified(props.getModified());
			entry.setCreated(props.getCreated());
			info.add(entry);
			stream.close();
			docx.close();

		} else {

		}

	}

	public void updateSIForDoc(String filename, List<WDCInformation> info) throws IOException, FileNotFoundException {
		POIFSReader r = new POIFSReader();
		r.registerListener(new POIFSReaderListener() {

			@Override
			public void processPOIFSReaderEvent(POIFSReaderEvent event) {
				WDCInformation entry = new WDCInformation(); 
				SummaryInformation si = null;
				try {
					si = (SummaryInformation) PropertySetFactory.create(event.getStream());
				} catch (Exception ex) {
					throw new RuntimeException("Property set stream \"" + event.getPath() + event.getName() + "\": " + ex);
				}
				entry.setFilePath(filename);
				entry.setCreator(si.getAuthor());
				entry.setLastModifiedByUser(si.getLastAuthor());
				entry.setModified(si.getLastSaveDateTime());
				entry.setCreated(si.getCreateDateTime());
				info.add(entry);

			}
		}, "\005SummaryInformation");
		r.read(new FileInputStream(filename));
	}

	public void processFolder(String folderPath) throws IOException, FileNotFoundException {
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();

		List<WDCInformation> info = new ArrayList<>();
		for (int i = 0; i < listOfFiles.length; i++) {
			String filename = listOfFiles[i].getPath();
			if (listOfFiles[i].isFile() && filename.contains(".docx")) {
				updateSIForDocXFile(filename, info);
			} else if (listOfFiles[i].isFile() && filename.contains(".doc")) {
				updateSIForDoc(filename, info);

			} else if (listOfFiles[i].isDirectory()) {
				// System.out.println("Directory " + filename);
			}
		}
		
		for (WDCInformation wdcInformation : info) {
			System.out.println(wdcInformation);
		}
	}


}
