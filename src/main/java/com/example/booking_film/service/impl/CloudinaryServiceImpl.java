package com.example.booking_film.service.impl;

import com.cloudinary.Cloudinary;
import com.example.booking_film.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    @Override
    public String uploadPoster(MultipartFile file) {
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), Map.of("folder", "movie-posters", "resource_type", "image"));
            return (String)uploadResult.get("secure_url");
        }
        catch (IOException e) {
            throw new RuntimeException("Upload lên Cloudinary thất bại: " + e.getMessage());
        }
    }
}
