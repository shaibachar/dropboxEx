package com.service.annexes;

import java.io.File;
import java.util.List;

public interface DropBoxEXService {

	void createAnnexes(byte[] inputFile, File outputFile);

	List<String> getProcessedFiles();

	void addProcessedFile(String fileName);

}
