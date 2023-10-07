package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/admin/common")
@Api("公共接口")
@Slf4j
public class CommonController {

    @PostMapping("/upload")
    @ApiOperation(value = "文件上传")
    public Result<Path> upload(MultipartFile file) {
        log.info("文件上传: {}", file);


        // 获取文件的原始名称
        String originalFileName = file.getOriginalFilename();

        // 获取文件的后缀
        String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));

        // 构建文件的新名称
        originalFileName = System.currentTimeMillis() + suffix;

        // 构建文件存储的本地路径
        String downLoad="D:\\项目资料\\downLoad";

        // 构建本地文件路径 文件请求路径
        Path filePath = Paths.get(downLoad, originalFileName);
        log.info("文件上传成功，文件路径: {}", filePath);
        // 将文件保存到本地文件系统
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // 处理文件复制异常
            e.printStackTrace();
            return Result.error("文件上传失败");
        }

        return Result.success(filePath); // 重定向到文件显示页面
    }
}

