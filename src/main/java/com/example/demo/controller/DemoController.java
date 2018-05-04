package com.example.demo.controller;

import com.example.demo.fastdfs.FastDFSClient;
import com.example.demo.fastdfs.FastDFSFile;
import com.example.demo.zip.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;

/**
 * Created by song on 2018/5/3.
 */
@Controller
public class DemoController {

    private static Logger logger = LoggerFactory.getLogger(DemoController.class);

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload") //new annotation since 4.3
    public String singleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:result";
        }

        String fileName=file.getOriginalFilename();
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
        if(!ext.equals("zip")){
            redirectAttributes.addFlashAttribute("message", "必须为zip文件");
            return "redirect:result";
        }

        try {
            InputStream in = file.getInputStream();
            File zipFile=new File("F:\\uploadtest\\"+fileName);
            FileOutputStream out = new FileOutputStream(zipFile);
            FileCopyUtils.copy(in,out);
            String zipFilePath="F:\\uploadtest\\"+fileName;
            String unzipFilePath="F:\\uploadtest\\unzip";
            ZipUtil.unzip(zipFilePath,unzipFilePath,false);
            File unzipFileDir=new File(unzipFilePath);
            File[] files = unzipFileDir.listFiles();
            for (File file1:files){
                String unzipfileName = file1.getName();//test001
                logger.info("unzipfileName"+unzipfileName);
                String path = saveFile(file1);
                logger.info("path"+path);
                //unzipfileName group_name，remoteFileName保存数据库
               // file1.delete();
            }
            //zipFile.delete();
            //删除本地文件



//            // Get the file and save it somewhere
//           // String path=saveFile(file);
//           // redirectAttributes.addFlashAttribute("message",
//                    "You successfully uploaded '" + file.getOriginalFilename() + "'");
//           // redirectAttributes.addFlashAttribute("path",
//           //         "file path url '" + path + "'");
        } catch (Exception e) {
            logger.error("upload file failed",e);
        }
        return "redirect:/result";
    }

    @GetMapping("/result")
    public String uploadStatus() {
        return "result";
    }

    public String saveFile(File file) throws IOException {
        String[] fileAbsolutePath={};
        String fileName=file.getName();
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
        byte[] file_buff = null;
        InputStream inputStream=new FileInputStream(file);
        if(inputStream!=null){
            int len1 = inputStream.available();
            file_buff = new byte[len1];
            inputStream.read(file_buff);
        }
        inputStream.close();
        FastDFSFile fastFile = new FastDFSFile(fileName, file_buff, ext);
        try {
            fileAbsolutePath = FastDFSClient.upload(fastFile);  //upload to fastdfs
        } catch (Exception e) {
            logger.error("upload file Exception!",e);
        }
        if (fileAbsolutePath==null) {
            logger.error("upload file failed,please upload again!");
        }
        String path=FastDFSClient.getTrackerUrl()+fileAbsolutePath[0]+ "/"+fileAbsolutePath[1];
        return path;
    }

    /**
     * @param multipartFile
     * @return
     * @throws IOException
     */
//    public String saveFile(MultipartFile multipartFile) throws IOException {
//        String[] fileAbsolutePath={};
//        String fileName=multipartFile.getOriginalFilename();
//        String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
//        byte[] file_buff = null;
//        InputStream inputStream=multipartFile.getInputStream();
//        if(inputStream!=null){
//            int len1 = inputStream.available();
//            file_buff = new byte[len1];
//            inputStream.read(file_buff);
//        }
//        inputStream.close();
//        FastDFSFile file = new FastDFSFile(fileName, file_buff, ext);
//        try {
//            fileAbsolutePath = FastDFSClient.upload(file);  //upload to fastdfs
//        } catch (Exception e) {
//            logger.error("upload file Exception!",e);
//        }
//        if (fileAbsolutePath==null) {
//            logger.error("upload file failed,please upload again!");
//        }
//        String path=FastDFSClient.getTrackerUrl()+fileAbsolutePath[0]+ "/"+fileAbsolutePath[1];
//        return path;
//    }

}
