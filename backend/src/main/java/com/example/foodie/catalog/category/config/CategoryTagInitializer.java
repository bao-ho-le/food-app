package com.example.foodie.catalog.category.config;

import com.example.foodie.catalog.category.entity.Category;
import com.example.foodie.catalog.category.repository.CategoryRepository;
import com.example.foodie.catalog.tag.entity.Tag;
import com.example.foodie.catalog.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(3)
@RequiredArgsConstructor
public class CategoryTagInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    private static final Map<String, List<String>> CATEGORY_TAGS = new LinkedHashMap<>();
    static {
        CATEGORY_TAGS.put("Ẩm thực", List.of("Việt Nam", "Nhật Bản", "Hàn Quốc", "Trung Quốc", "Thái Lan", "Ý", "Mỹ"));
        CATEGORY_TAGS.put("Nguyên liệu chính", List.of("Thịt bò", "Thịt gà", "Thịt heo", "Hải sản", "Cá", "Trứng", "Rau củ", "Phô mai"));
        CATEGORY_TAGS.put("Phương pháp chế biến", List.of("Chiên", "Xào", "Nướng", "Luộc", "Hấp", "Kho", "Sống"));
        CATEGORY_TAGS.put("Hương vị", List.of("Cay", "Ngọt", "Mặn", "Chua", "Đắng", "Béo"));
    }

    @Override
    public void run(String... args) {
        CATEGORY_TAGS.forEach((categoryName, tagNames) -> {
            Category category = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> categoryRepository.save(Category.builder().name(categoryName).build()));

            for (String tagName : tagNames) {
                if (tagRepository.findByName(tagName).isEmpty()) {
                    tagRepository.save(Tag.builder().name(tagName).category(category).build());
                }
            }
        });
    }
}
