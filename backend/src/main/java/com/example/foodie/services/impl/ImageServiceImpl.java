package com.example.foodie.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.foodie.dtos.ImageDTO;
import com.example.foodie.models.Dish;
import com.example.foodie.models.Image;
import com.example.foodie.repos.DishRepository;
import com.example.foodie.repos.ImageRepository;
import com.example.foodie.services.interfaces.ImageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Service
@AllArgsConstructor
public class ImageServiceImpl implements ImageService {

    private Cloudinary cloudinary;
    private ImageRepository imageRepository;
    private DishRepository dishRepository;

    @Override
    public Map<String, Object> uploadImage(MultipartFile file, ImageDTO imageDTO){
        File uploadedImage = null;

        try {
            Dish existingDish = dishRepository.findById(imageDTO.getDishId())
                    .orElseThrow(() -> new RuntimeException("Không có món này"));

            if (file.isEmpty() || file == null)
                throw new RuntimeException("Gửi file ảnh rỗng");

            uploadedImage = convertMultiPartToFile(file);
            Map uploadResult = cloudinary.uploader().upload(uploadedImage, ObjectUtils.emptyMap());

            Image newImage = Image.builder()
                    .url((String) uploadResult.get("secure_url"))
                    .publicId((String) uploadResult.get("public_id"))
                    .format((String) uploadResult.get("format"))
                    .width((Integer) uploadResult.get("width"))
                    .height((Integer) uploadResult.get("height"))
                    .imageName(imageDTO.getImageName())
                    .altText(imageDTO.getAltText())
                    .isThumbnail(imageDTO.getIsThumbnail())
                    .dish(existingDish)
                    .build();

            imageRepository.save(newImage);

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
        Dish existingDish = dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("Không có món này"));

        return imageRepository.findByDish_Id(dishId);
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

}
