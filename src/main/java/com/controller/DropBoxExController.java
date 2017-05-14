package com.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.service.dropbox.DropBoxService;

@RestController
@RequestMapping("/annexes")
public class DropBoxExController {

    private static final Logger logger = LoggerFactory.getLogger(DropBoxExController.class);
    

    @Autowired
    private ApplicationContext appContext;
    
    private DropBoxService dropBoxService;

    public DropBoxExController() {
        // TODO Auto-generated constructor stub
    }

    @Autowired
    public DropBoxExController(DropBoxService dropBoxService) {
        this.dropBoxService = dropBoxService;
    }

    
    @GetMapping("/hello")
    public String index() {
        return "hello";
    }

    @GetMapping("/byebye")
    public void close() {
    	logger.info("going to close application");
    	int returnCode=0;
		SpringApplication.exit(appContext, () -> returnCode);
    }

    
    @GetMapping("/info")
    public @ResponseBody String info() {
        
        return "DATA " ;
    }

}
