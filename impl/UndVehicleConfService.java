package com.wxcp.server.price.impl;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UndVehicleConfService {

    void upload(List<MultipartFile> multipartFiles);
}
