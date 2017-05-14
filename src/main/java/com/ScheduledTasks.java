package com;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.service.annexes.DropBoxEXService;
import com.service.dropbox.DropBoxService;

@Service
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Value("${service.dropbox.oldFilePreFix}")
    private String oldFilePreFix;
    
    @Value("${service.dropbox.annexesFilePreFix}")
    private String annexesFilePreFix;

    private DropBoxService dropBoxService;

    private DropBoxEXService annexesService;

    @Autowired
    public ScheduledTasks(DropBoxEXService annexesService,DropBoxService dropBoxService) {
        this.annexesService = annexesService;
        this.dropBoxService = dropBoxService;
        log.info("Scheduler constracted");
    }

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
       /* log.info("The time is now {}", dateFormat.format(new Date()));
        try {
            List<String> folderChange = dropBoxService.folderChange();
            if (folderChange.size() > 0) {
            	List<String> processedFiles = annexesService.getProcessedFiles();
                for (String fileName : folderChange) {
                    if (!fileName.contains(annexesFilePreFix) && !processedFiles.contains(fileName)) {
                    	byte[] fileData = dropBoxService.download(fileName);
                        File outputFile = new File(annexesFilePreFix + fileName);
                        annexesService.createAnnexes(fileData, outputFile);
                        dropBoxService.upload(outputFile);
                        //dropBoxService.rename(fileName);
                        annexesService.addProcessedFile(fileName);
                        
                    }
                }

            }
        } catch (Exception e) {
            log.error("while checking dropBox", e);
            System.exit(0);
            
        }*/
    }
}
