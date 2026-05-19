package com.example.foodie.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dbtuxgfq0",
                "api_key", "314332851847962",
                "api_secret", "wOQQaVan0J-I1H9xiEJdU2jCA6w"));
    }
}
