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

import com.entity.ManagedFileFormats;
import com.entity.WDCInformation;

@Component
public class PoiUtils {

	private final Logger logger = LoggerFactory.getLogger(PoiUtils.class);

	/**
	 * 
	 * @param filePath
	 * @param info
	 * @throws IOException
	 */
	protected void updateSIForDocXFile(String filePath, List<WDCInformation> info) throws IOException {
		FileInputStream stream;
		File file = new File(filePath);
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
			logger.error("Error while trying to read file:" + filePath + " since it does not exsists please check for thread error");
		}

	}

	/**
	 * 
	 * @param filename
	 * @param info
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected void updateSIForDoc(String filename, List<WDCInformation> info) throws IOException, FileNotFoundException {
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

	/**
	 * 
	 * @param folderPath
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public List<WDCInformation> processFolder(String folderPath) throws IOException, FileNotFoundException {
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();

		List<WDCInformation> info = new ArrayList<>();
		collectFilesAndProcess(listOfFiles, info);

		return info;
	}

	/**
	 * 
	 * @param listOfFiles
	 * @param info
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	protected void collectFilesAndProcess(File[] listOfFiles, List<WDCInformation> info) throws IOException, FileNotFoundException {
		for (int i = 0; i < listOfFiles.length; i++) {
			String filename = listOfFiles[i].getPath();
			logger.info("process filePath:"+filename);
			if (listOfFiles[i].isFile() && filename.contains(ManagedFileFormats.DOCX.toString())) {
				updateSIForDocXFile(filename, info);
			} else if (listOfFiles[i].isFile() && filename.contains(ManagedFileFormats.DOC.toString())) {
				updateSIForDoc(filename, info);
			} else if (listOfFiles[i].isDirectory()) {
				File folder = new File(listOfFiles[i].getPath());
				File[] innerListOfFiles = folder.listFiles();
				collectFilesAndProcess(innerListOfFiles, info);
			}
		}
	}

	public static void main(String[] args) {
		PoiUtils poiUtils = new PoiUtils();
		try {
			List<WDCInformation> processFolder = poiUtils.processFolder("/home/shai/Downloads");
			for (WDCInformation wdcInformation : processFolder) {
				System.out.println(wdcInformation);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
