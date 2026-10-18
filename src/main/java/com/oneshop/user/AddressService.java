package com.oneshop.user;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.entity.Address;
import com.oneshop.entity.User;
import com.oneshop.repository.AddressRepository;
import com.oneshop.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    /** Danh sách địa chỉ của user, địa chỉ mặc định nằm đầu. (Tiến dùng ở checkout) */
    @Transactional(readOnly = true)
    public List<Address> list(Long userId) {
        return addressRepository.findByUserIdOrderByDefaultAddressDescIdDesc(userId);
    }

    /** Lấy địa chỉ theo id, chỉ khi thuộc về user này. (Tiến dùng ở checkout) */
    @Transactional(readOnly = true)
    public Address get(Long userId, Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ."));
    }

    /** Địa chỉ mặc định của user (nếu có). */
    @Transactional(readOnly = true)
    public Optional<Address> getDefault(Long userId) {
        return addressRepository.findByUserIdAndDefaultAddressTrue(userId).stream().findFirst();
    }

    @Transactional
    public Address create(Long userId, AddressForm form) {
        User user = userRepository.getReferenceById(userId);
        boolean isFirst = addressRepository.countByUserId(userId) == 0;

        Address address = Address.builder().user(user).build();
        apply(address, form);
        if (isFirst || form.isDefaultAddress()) {
            clearDefault(userId);
            address.setDefaultAddress(true);
        }
        return addressRepository.save(address);
    }

    @Transactional
    public Address update(Long userId, Long addressId, AddressForm form) {
        Address address = get(userId, addressId);
        apply(address, form);
        if (form.isDefaultAddress() && !address.isDefaultAddress()) {
            clearDefault(userId);
            address.setDefaultAddress(true);
        }
        return addressRepository.save(address);
    }

    @Transactional
    public void setDefault(Long userId, Long addressId) {
        Address address = get(userId, addressId);
        clearDefault(userId);
        address.setDefaultAddress(true);
    }

    @Transactional
    public void delete(Long userId, Long addressId) {
        Address address = get(userId, addressId);
        boolean wasDefault = address.isDefaultAddress();
        addressRepository.delete(address);
        // Xóa địa chỉ mặc định thì chuyển mặc định sang địa chỉ còn lại mới nhất
        if (wasDefault) {
            addressRepository.findByUserIdOrderByDefaultAddressDescIdDesc(userId)
                    .stream().findFirst().ifPresent(a -> a.setDefaultAddress(true));
        }
    }

    private void clearDefault(Long userId) {
        addressRepository.findByUserIdAndDefaultAddressTrue(userId)
                .forEach(a -> a.setDefaultAddress(false));
    }

    private void apply(Address address, AddressForm form) {
        address.setReceiverName(form.getReceiverName().trim());
        address.setPhone(form.getPhone().trim());
        address.setProvince(form.getProvince().trim());
        address.setWard(form.getWard().trim());
        address.setDetail(form.getDetail().trim());
    }
}