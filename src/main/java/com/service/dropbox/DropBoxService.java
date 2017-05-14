package com.service.dropbox;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.dropbox.core.DbxException;

public interface DropBoxService {

	void upload(File inputFile) throws DbxException, IOException;

	byte[] download(String filePath) throws DbxException, IOException;

	List<String> folderChange() throws DbxException;

	List<String> fileChanges() throws DbxException;

	void rename(String fromPath) throws DbxException;

}
