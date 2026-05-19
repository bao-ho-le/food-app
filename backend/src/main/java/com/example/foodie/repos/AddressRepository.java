package com.example.foodie.repos;

import com.example.foodie.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface AddressRepository extends JpaRepository<Address, Integer> {
    List<Address> findByUser_Id(Integer userId);
    List<Address> findAllByUser_Id(Integer userId);
    Optional<Address> findByUser_IdAndIsDefault(Integer userId, Boolean isDefault);


}
