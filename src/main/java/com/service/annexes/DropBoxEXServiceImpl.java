package com.service.annexes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.service.util.GeneralUtils;

/**
 * 
 * @author shai
 *
 */
@Service
public class DropBoxEXServiceImpl implements DropBoxEXService {

	private final Logger log = LoggerFactory.getLogger(DropBoxEXServiceImpl.class);

	@Value("${service.annexes.regex}")
	private String regExpList;

	@Value(value = "classpath:templates/empty.docx")
	private Resource emptyTemplate;

	private List<String> processedFiles;

	public DropBoxEXServiceImpl() {
		processedFiles = new ArrayList<String>();

	}

	@Override
	public void addProcessedFile(String fileName) {
		processedFiles.add(fileName);
	}

	@Override
	public List<String> getProcessedFiles() {
		return processedFiles;
	}

	public void setProcessedFiles(List<String> processedFiles) {
		this.processedFiles = processedFiles;
	}

	public String getRegExpList() {
		return regExpList;
	}

	public void setRegExpList(String regExpList) {
		this.regExpList = regExpList;
	}

	/**
	 * The method will 1) load the current file 2) parse all the info according
	 * to regular expressions 3) save the results to PaperDTO
	 * 
	 * @param inputStream
	 *            - the file we want to have annexes for
	 * @param annexesRegularExpression
	 *            - the annexes regular expression
	 */
	@Override
	public void createAnnexes(byte[] inputFile, File outputFile) {
		List<String> regExpListValues = Arrays.asList(regExpList.split(","));

		try {
			// FileInputStream fileInputStream = new FileInputStream(inputFile);
			String loadFileToString = loadFileToString(new ByteArrayInputStream(inputFile));
			List<String> extractedData = extractedData(loadFileToString, regExpListValues);
			byte[] generateResult = generateResult(extractedData);
			FileUtils.writeByteArrayToFile(outputFile, generateResult);

		} catch (Exception e) {
			String message = "Error while open original file ";
			log.error(message, e);
			throw new RuntimeException(message);
		}

	}

	private void setTableAlignment(XWPFTable table, STJc.Enum justification) {
		CTTblPr tblPr = table.getCTTbl().getTblPr();
		CTJc jc = (tblPr.isSetJc() ? tblPr.getJc() : tblPr.addNewJc());
		jc.setVal(justification);
		tblPr.setJc(jc);
	}

	private byte[] generateResult(List<String> extracted) throws IOException {
		byte[] result;
		// Workaround to create right aligned document - i am using empty right
		// aligned document
		XWPFDocument doc = new XWPFDocument(emptyTemplate.getInputStream());
		try {
			XWPFParagraph allP = doc.createParagraph();
			allP.setAlignment(ParagraphAlignment.RIGHT);

			XWPFTable table = doc.createTable(extracted.size() + 1, 2);
			setTableAlignment(table, STJc.RIGHT);

			setTableHeader(table);

			setTableContent(extracted, table, doc);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				doc.write(out);
			} finally {
				out.close();
			}
			result = out.toByteArray();
		} finally {
			doc.close();
		}
		return result;

	}

	private void setTableContent(List<String> extracted, XWPFTable table, XWPFDocument doc) {
		for (int i = 1; i <= extracted.size() - 1; i++) {
			System.out.println(i);
			String string = extracted.get(i);
			XWPFParagraph p1 = (XWPFParagraph) table.getRow(i).getCell(0).getParagraphs().get(0);
			p1.setAlignment(ParagraphAlignment.RIGHT);
			XWPFRun r1 = p1.createRun();
			r1.setText(string);
			r1.setFontFamily("David");

			XWPFParagraph p2 = (XWPFParagraph) table.getRow(i).getCell(1).getParagraphs().get(0);
			p2.setAlignment(ParagraphAlignment.RIGHT);
			XWPFRun r2 = p2.createRun();
			r2.setText("נספח " + GeneralUtils.formatHebrew(i));
			r2.setFontFamily("David");

		}

		for (int i = 1; i <= extracted.size() - 1; i++) {
			XWPFParagraph addPageBreak = addPageBreak(doc);
			String string = extracted.get(i);
			addPageBreak.setAlignment(ParagraphAlignment.RIGHT);
			XWPFRun r1 = addPageBreak.createRun();
			r1.setText(string);
			r1.setFontFamily("David");
		}

	}

	private XWPFParagraph addPageBreak(XWPFDocument doc) {
		XWPFParagraph pageBreak = doc.createParagraph();
		pageBreak.setPageBreak(true);
		return pageBreak;

	}

	private void setTableHeader(XWPFTable table) {

		XWPFParagraph h1 = (XWPFParagraph) table.getRow(0).getCell(1).getParagraphs().get(0);
		h1.setAlignment(ParagraphAlignment.RIGHT);
		XWPFRun rh1 = h1.createRun();
		rh1.setBold(true);
		rh1.setText("נספח");
		rh1.setFontFamily("David");
		XWPFParagraph h2 = (XWPFParagraph) table.getRow(0).getCell(0).getParagraphs().get(0);
		h2.setAlignment(ParagraphAlignment.RIGHT);
		XWPFRun rh2 = h2.createRun();
		rh2.setBold(true);
		rh2.setText("נושא");
		rh2.setFontFamily("David");
	}

	private List<String> extractedData(String data, List<String> regularExpression)
			throws UnsupportedEncodingException {
		List<String> res = new ArrayList<String>();
		for (String regExPattern : regularExpression) {

			// Create a Pattern object
			Pattern r = Pattern.compile(regExPattern, Pattern.UNICODE_CASE);
			String[] lines = data.split("\n");
			for (String line : lines) {

				// String value = new String(line.getBytes("UTF-8"));
				// Now create matcher object.
				Matcher m = r.matcher(line);
				if (m.find()) {
					String toAdd1 = line.replace("*","");
					int untilIndex = toAdd1.length(); 
					if (toAdd1.contains("מצורף")){
						untilIndex=toAdd1.indexOf("מצורף");
					}else if (toAdd1.contains("מצורפים")){
						untilIndex=toAdd1.indexOf("מצורפים");
					}else if (toAdd1.contains("מצורפת")){
						untilIndex=toAdd1.indexOf("מצורפת");
					}
					String toAdd2 = toAdd1.substring(0, untilIndex);
					res.add(toAdd2 + "\n");
				}
			}

		}
		return res;
	}

	public String loadFileToString(InputStream inputStream) throws FileNotFoundException, IOException {
		String res = new String();
		try {
			XWPFDocument docx = new XWPFDocument(inputStream);
			// using XWPFWordExtractor Class

			XWPFWordExtractor we = new XWPFWordExtractor(docx);
			res = we.getText();
			we.close();

		} catch (Exception e) {

			log.error("error while load file", e);
			try {
				HWPFDocument doc = new HWPFDocument(inputStream);
				WordExtractor extractor = new WordExtractor(doc);
				res = extractor.getText();
			} catch (Exception ex) {
				log.error("error while load file", ex);
			}
		}
		return res;
	}

}