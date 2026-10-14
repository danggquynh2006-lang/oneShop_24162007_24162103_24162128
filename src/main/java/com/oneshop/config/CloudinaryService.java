package com.oneshop.config;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /** Upload ảnh, trả về URL. */
    public UploadResult uploadImage(MultipartFile file, String folder) throws IOException {
        return upload(file, folder, "image");
    }

    /** Upload video, trả về URL. */
    public UploadResult uploadVideo(MultipartFile file, String folder) throws IOException {
        return upload(file, folder, "video");
    }

    /** Xóa file theo publicId. resourceType là "image" hoặc "video". */
    public void delete(String publicId, String resourceType) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
    }

    private UploadResult upload(MultipartFile file, String folder, String resourceType) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", "oneshop/" + folder, "resource_type", resourceType));
        return new UploadResult((String) result.get("secure_url"), (String) result.get("public_id"));
    }

    /** url để hiển thị, publicId để xóa sau này (lưu cả hai vào DB). */
    public record UploadResult(String url, String publicId) {}
}