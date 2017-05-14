package com.service.dropbox;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v1.DbxClientV1;
import com.dropbox.core.v1.DbxEntry;
import com.dropbox.core.v1.DbxWriteMode;

/**
 * 
 * @author shai
 *
 */
@Service
public class DropBoxServiceImpl implements DropBoxService {
	private final Logger logger = LoggerFactory.getLogger(DropBoxServiceImpl.class);

	@Value("${service.dropbox.dropboxConfig}")
	private String dropboxConfig;

	@Value("${service.dropbox.oldFilePreFix}")
	private String oldFilePreFix;

	@Value("${service.dropbox.accessToken}")
	private String accessToken;

	@Value("${service.dropbox.annexesFilePreFix}")
	private String annexesFilePreFix;

	@Value("${service.dropbox.annexesFolder}")
	private String annexesFolder;

	private DbxClientV1 client;

	private List<String> annexesFileList;

	private List<String> allFileList;

	public DropBoxServiceImpl() {
		annexesFileList = new ArrayList<String>();
		allFileList = new ArrayList<String>();

	}

	@PostConstruct
	private void init() {

		// DbxAppInfo appInfo = new DbxAppInfo(appkey, appsecret);
		//
		DbxRequestConfig config = new DbxRequestConfig(dropboxConfig);
		// DbxWebAuth webAuth = new DbxWebAuth(config, appInfo);
		try {
			client = new DbxClientV1(config, accessToken);
			logger.info("Linked account: " + client.getAccountInfo().displayName);
		} catch (Exception e) {
			logger.error("error while init to dropbox", e);
		}

	}

	@Override
	public List<String> fileChanges() throws DbxException {
		List<DbxEntry> newFileList = client.getMetadataWithChildren("/", true).children;
		ArrayList<String> tempList = newFileList.stream().map(DbxEntry::toString)
				.collect(Collectors.toCollection(ArrayList::new));

		if (!CollectionUtils.containsAll(tempList, newFileList)) {
			List<String> intersection = (List<String>) CollectionUtils.intersection(allFileList, tempList);
			annexesFileList = tempList;
			return intersection;
		}
		return annexesFileList;
	}

	@Override
	public List<String> folderChange() throws DbxException {

		DbxEntry.WithChildren listing = client.getMetadataWithChildren("/");
		List<String> tempList = new ArrayList<>();
		logger.debug("Files in the root path:");
		if (listing != null) {
			for (DbxEntry child : listing.children) {
				logger.debug("    " + child.name + ": " + child.toString());
				tempList.add(child.path);
			}
			if (!CollectionUtils.containsAll(tempList, annexesFileList)) {
				List<String> intersection = (List<String>) CollectionUtils.intersection(annexesFileList, tempList);
				annexesFileList = tempList;
				return intersection;
			}
		} else {
			logger.info("folder annexes is empty");
		}
		return tempList;
	}

	@Override
	public byte[] download(String filePath) throws DbxException, IOException {
		byte[] out = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			DbxEntry.File downloadedFile = client.getFile(filePath, null, outputStream);
			logger.info("Metadata: " + downloadedFile.toString());
			out = outputStream.toByteArray();

		} finally {
			outputStream.close();
		}

		return out;
	}

	@Override
	public void upload(File inputFile) throws DbxException, IOException {
		FileInputStream inputStream = new FileInputStream(inputFile);
		try {
			String inputName = inputFile.getName();
			String outputName = annexesFilePreFix + "-" + inputName;
			String path = annexesFolder + "/" + outputName;
			if (client.getFile(path, null, new ByteArrayOutputStream()) == null) {
				DbxEntry.File uploadedFile = client.uploadFile(path, DbxWriteMode.add(), inputFile.length(),
						inputStream);
				logger.info("Uploaded: " + uploadedFile.toString());
			} else {
				logger.error("file:" + outputName + " is in repository");
			}
		} finally {
			inputStream.close();
		}
	}

	@Override
	public void rename(String fromPath) throws DbxException {
		String toPath = fromPath.replace(".", "-" + oldFilePreFix + ".");
		client.move(fromPath, toPath);
	}
}
