package com.example.foodie.identity.address.service;

import com.example.foodie.common.exception.ErrorCode;
import com.example.foodie.common.exception.business_exception.IdentityException;
import com.example.foodie.identity.address.dto.request.AddressDTO;
import com.example.foodie.identity.address.entity.Address;
import com.example.foodie.identity.address.helper.AddressHelper;
import com.example.foodie.identity.address.mapper.AddressMapper;
import com.example.foodie.identity.address.repository.AddressRepository;
import com.example.foodie.identity.user.entity.User;
import com.example.foodie.identity.user.helper.UserHelper;
import com.example.foodie.identity.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Bất biến duy nhất cần giữ: mỗi user có tối đa một địa chỉ mặc định. Helper/mapper
// dùng bản thật (Quy tắc 1) — chỉ AddressRepository và UserRepository (qua UserHelper
// thật) bị mock.
@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;

    private AddressServiceImpl addressService;
    private User caller;

    @BeforeEach
    void setUp() {
        caller = User.builder().id(1).email("caller@test.local").build();
        when(authentication.getName()).thenReturn("caller@test.local");
        when(userRepository.findByEmail("caller@test.local")).thenReturn(Optional.of(caller));

        UserHelper userHelper = new UserHelper(userRepository);
        addressService = new AddressServiceImpl(addressRepository, new AddressHelper(), new AddressMapper(), userHelper);
    }

    private static AddressDTO dto(String address, boolean isDefault) {
        return AddressDTO.builder().address(address).isDefault(isDefault).build();
    }

    @Nested
    class ThemDiaChi {

        @Test
        @DisplayName("Thêm địa chỉ trùng chuỗi địa chỉ đã có của user ném ADDRESS_ALREADY_EXISTS")
        void should_throwAddressAlreadyExists_when_addressLineDuplicatesExistingOne() {
            Address existing = Address.builder().id(10).user(caller).address("123 Le Loi").isDefault(false).build();
            when(addressRepository.findByUser_Id(1)).thenReturn(List.of(existing));

            assertThatThrownBy(() -> addressService.addAddressByUserId(authentication, dto("123 Le Loi", false)))
                    .isInstanceOf(IdentityException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.ADDRESS_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("Thêm địa chỉ mới isDefault=true khi đã có mặc định cũ -> mặc định cũ bị tắt")
        void should_unsetOldDefault_when_addingNewDefaultAddress() {
            Address oldDefault = Address.builder().id(10).user(caller).address("Old Address").isDefault(true).build();
            when(addressRepository.findByUser_Id(1)).thenReturn(List.of(oldDefault));
            when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

            addressService.addAddressByUserId(authentication, dto("New Address", true));

            // oldDefault là entity thật (không phải DTO) -> assert trực tiếp trạng thái sau lệnh,
            // không cần ArgumentCaptor vì service mutate ngay trên tham chiếu ta đang giữ.
            assertThat(oldDefault.getIsDefault()).isFalse();
        }

        @Test
        @DisplayName("Thêm địa chỉ mới isDefault=false thì mặc định cũ giữ nguyên")
        void should_keepOldDefault_when_addingNonDefaultAddress() {
            Address oldDefault = Address.builder().id(10).user(caller).address("Old Address").isDefault(true).build();
            when(addressRepository.findByUser_Id(1)).thenReturn(List.of(oldDefault));
            when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

            addressService.addAddressByUserId(authentication, dto("New Address", false));

            assertThat(oldDefault.getIsDefault()).isTrue();
        }
    }

    @Nested
    class CapNhatDiaChi {

        @Test
        @DisplayName("Cập nhật địa chỉ không thuộc người gọi ném ADDRESS_NOT_FOUND")
        void should_throwAddressNotFound_when_addressDoesNotBelongToCaller() {
            when(addressRepository.findByIdAndUser_Id(99, 1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.updateAddress(authentication, 99, dto("X", false)))
                    .isInstanceOf(IdentityException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.ADDRESS_NOT_FOUND);
        }

        // Biên dễ sót: nếu code "bỏ mặc định cũ" không loại trừ chính địa chỉ đang sửa,
        // user tự làm mất địa chỉ mặc định của chính mình khi chỉ re-save nó.
        @Test
        @DisplayName("Đặt isDefault=true cho chính địa chỉ đang là mặc định -> vẫn còn đúng một mặc định")
        void should_notUnsetItself_when_updatingAddressThatIsAlreadyDefault() {
            Address address = Address.builder().id(10).user(caller).address("Home").isDefault(true).build();
            when(addressRepository.findByIdAndUser_Id(10, 1)).thenReturn(Optional.of(address));
            when(addressRepository.findByUser_IdAndIsDefault(1, true)).thenReturn(Optional.of(address));
            when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

            addressService.updateAddress(authentication, 10, dto("Home", true));

            assertThat(address.getIsDefault()).isTrue();
        }
    }

    @Nested
    class XoaDiaChi {

        @Test
        @DisplayName("Xoá địa chỉ không thuộc người gọi ném ADDRESS_NOT_FOUND")
        void should_throwAddressNotFound_when_deletingAddressNotOwnedByCaller() {
            when(addressRepository.existsByIdAndUser_Id(99, 1)).thenReturn(false);

            assertThatThrownBy(() -> addressService.deleteAddressById(authentication, 99))
                    .isInstanceOf(IdentityException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.ADDRESS_NOT_FOUND);

            org.mockito.Mockito.verify(addressRepository, org.mockito.Mockito.never()).deleteByIdAndUser_Id(any(), any());
        }
    }
}
