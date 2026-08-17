package com.example.foodie.catalog.image.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.foodie.catalog.dish.entity.Dish;
import com.example.foodie.catalog.dish.repository.DishRepository;
import com.example.foodie.catalog.image.dto.request.ImageDTO;
import com.example.foodie.catalog.image.entity.Image;
import com.example.foodie.catalog.image.helper.ImageHelper;
import com.example.foodie.catalog.image.mapper.ImageMapper;
import com.example.foodie.catalog.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final Cloudinary cloudinary;
    private final ImageRepository imageRepository;
    private final DishRepository dishRepository;
    private final ImageHelper imageHelper;
    private final ImageMapper imageMapper;

    @Override
    public Map<String, Object> uploadImage(MultipartFile file, ImageDTO imageDTO){
        imageHelper.validateUploadRequest(file, imageDTO);

        File uploadedImage = null;

        try {
            Dish existingDish = dishRepository.findById(imageDTO.getDishId())
                    .orElseThrow(() -> new RuntimeException("Không có món này"));

            uploadedImage = convertMultiPartToFile(file);
            Map<String, Object> uploadResult = cloudinary.uploader().upload(uploadedImage, ObjectUtils.emptyMap());

            imageRepository.save(imageMapper.toEntity(uploadResult, imageDTO, existingDish));

            return uploadResult;
        }catch (IOException e) {
            throw new RuntimeException("Không thể upload ảnh: " + e.getMessage());
        } finally {
            if (uploadedImage != null && uploadedImage.exists())
                uploadedImage.delete();
        }
    }

    @Override
    public List<Image> getImagesByDishId(Integer dishId){
        imageHelper.validateDishId(dishId);

        dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("Không có món này"));

        return imageRepository.findByDish_Id(dishId);
    }

    // Helper

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}
