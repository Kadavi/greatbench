package com.great.bench;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Controller
@RequestMapping("/")
public class HelloController {

    @Autowired
    private UploadService us;

    @Autowired
    private MongoTemplate mango;

    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @RequestMapping(method = RequestMethod.GET)
    public String printWelcome(ModelMap model) throws ExecutionException, InterruptedException {

        //Future<String> result = us.processImage("C:/Users/Grant Dawson/IdeaProjects/greatbench/src/test/java/com/great/bench/psyduck.jpg");
        //model.addAttribute("message", result.get());

        return "hello";

    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody String printUpload(HttpServletRequest req, HttpServletResponse resp,
                                            @RequestParam(value = "email", required = false) String email,
                                            @RequestParam(value = "key", required = false) String key) throws Exception {

        Map<String, MultipartFile> files = ((MultipartHttpServletRequest) req).getFileMap();
        OutputStream output = new FileOutputStream("C:/Users/Grant Dawson/IdeaProjects/greatbench/src/test/java/com/great/bench/" + files.get("fileupload").getOriginalFilename());
        IOUtils.copy(files.get("fileupload").getInputStream(), output);

        Future<String> result = us.processImage("C:/Users/Grant Dawson/IdeaProjects/greatbench/src/test/java/com/great/bench/" + files.get("fileupload").getOriginalFilename());
        String response = result.get();

        System.out.println(dateFormat.format(new Date()));

        if (response == null) System.out.println("wiffy");
        else System.out.println("yay");

        return response;

    }
}