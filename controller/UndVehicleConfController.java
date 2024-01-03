package com.wxcp.server.price.controller;

import com.wxcp.server.boot.BaseController;
import com.wxcp.server.car.CarEnum;
import com.wxcp.server.comb.enums.RedisBackendKeyEnum;
import com.wxcp.server.price.impl.UndVehicleConfService;
import com.wxcp.server.price.vo.CustomException;
import com.ymukj.common.base.GlobalResponse;
import com.ymukj.fastdfs.service.FastdfsHelper;
import com.ymukj.redis.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@RestController
@RequestMapping("/comb/vehicleConf")
public class UndVehicleConfController extends BaseController {

    @Autowired
    private UndVehicleConfService confService;
    @Autowired
    private FastdfsHelper fastdfsHelper;
    @Autowired
    protected RedisHelper redisHelper;

    @PostMapping("/upload")
    public GlobalResponse upload(String fileUrl) {
        String[] split = fileUrl.split("\\.");
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/")).replaceFirst("/", "");
        String fil = fileName.substring(0, fileName.lastIndexOf("."));
        File file = fastdfsHelper.download(fileUrl,  fil, split[split.length - 1]);

        try {
            // FileInputStream input = new FileInputStream(file);
            // MockMultipartFile multipartFile = new MockMultipartFile("file",
            // file.getName(), null
            // , input);

            // 使用转换后的MultipartFile对象进行文件上传操作
            List<MultipartFile> multipartFiles = unzip(file);
            confService.upload(multipartFiles);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            String key = RedisBackendKeyEnum.ASYNC_EXPORT_PRICE_IMPORT.name() + ":" + getCorpId();
            redisHelper.del(key);
            return GlobalResponse.fail("导入失败，请重新导入");
        }

        return GlobalResponse.success(null);
    }

    public List<MultipartFile> unzip(File file) {
        if (file == null || file.length() <= 0) {
            throw new CustomException("文件为空");
        }

        List<MultipartFile> multipartFileList = new ArrayList<>();
        ZipInputStream zipInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        String zipEntryFile;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(file));
            bufferedInputStream = new BufferedInputStream(zipInputStream);
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                zipEntryFile = zipEntry.getName();
                //文件名称为空
                Assert.notNull(zipEntryFile, "压缩文件中子文件的名字格式不正确");

                if (zipEntry.isDirectory()) {
                    continue;
                }

                //每个文件的流
                byte[] bytes = new byte[(int) zipEntry.getSize()];
                int read = bufferedInputStream.read(bytes, 0, (int) zipEntry.getSize());
                InputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                MultipartFile multipartFile = new MockMultipartFile(zipEntryFile, zipEntryFile, "xlsx", byteArrayInputStream);
                String filename = multipartFile.getOriginalFilename();
                if (!filename.contains(".")) {
                    continue;
                }
                String suffix = filename.substring(filename.lastIndexOf("."));
                if (suffix.equals(".xls") || suffix.equals(".xlsx")) {
                    multipartFileList.add(multipartFile);
                }
                byteArrayInputStream.close();
            }
        } catch (IOException e) {
            throw new CustomException("解压zip文件失败");
        } finally {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return multipartFileList;
    }
}
