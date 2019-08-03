package com.aliyun.ossimgdemo.service;

import com.aliyun.oss.OSSClient;
import com.aliyun.ossimgdemo.config.AliyunConfig;
import com.aliyun.ossimgdemo.domain.PicUploadResult;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

/**
 * @Author dezhe
 * @Date 2019/8/2 11:34
 */
@Service
public class PicUploadService {

    private static final String[] IMAGE_TYPE = new String[]{".bmp",".jpg",".jpeg",".gif",".png"};

    @Autowired
    private OSSClient ossClient;

    @Autowired
    private AliyunConfig aliyunConfig;

    public PicUploadResult upload(MultipartFile multipartFile) {

        PicUploadResult picUploadResult = new PicUploadResult();

        //对图片做一个校验，对后缀名校验
        boolean isLegal = false;
        for (String type : IMAGE_TYPE) {
            if (StringUtils.endsWithIgnoreCase(multipartFile.getOriginalFilename(),type)){
                isLegal = true;
                break;
            }
        }
        if (!isLegal){
            picUploadResult.setStatus("error");
            return picUploadResult;
        }

        String filename = multipartFile.getOriginalFilename();
        String filepath = getFilePath(filename);

        //上传到阿里云    images/年/月/日/XXXX.jpg
        try {
            ossClient.putObject(aliyunConfig.getBucketName(),filepath,new ByteArrayInputStream(multipartFile.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            picUploadResult.setStatus("error");
            return picUploadResult;
        }

        picUploadResult.setStatus("done");
        picUploadResult.setName(this.aliyunConfig.getUrlPrefix()+filepath);
        picUploadResult.setUid(String.valueOf(System.currentTimeMillis()));
        return picUploadResult;
    }

    private String getFilePath(String filename) {
        DateTime dateTime = new DateTime();
        return "image/"+dateTime.toString("yyyy")+"/"+dateTime.toString("dd")+"/"+System.currentTimeMillis()+
                RandomUtils.nextInt(100, 9999)+"."+ StringUtils.substringAfterLast(filename,".");
    }
}
