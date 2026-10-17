package com.oneshop.admin;

import java.math.RoundingMode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneshop.common.BusinessException;
import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.entity.Carrier;
import com.oneshop.repository.CarrierRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCarrierService {

    private static final int PAGE_SIZE = 10;

    private final CarrierRepository carrierRepository;

    @Transactional(readOnly = true)
    public Page<Carrier> search(String keyword, int page) {
        return carrierRepository.findByNameContainingIgnoreCase(keyword == null ? "" : keyword.trim(),
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));
    }

    @Transactional(readOnly = true)
    public Carrier get(Long id) {
        return carrierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà vận chuyển."));
    }

    @Transactional
    public Carrier create(CarrierForm form) {
        String name = form.getName().trim();
        checkDuplicateName(null, name);
        return carrierRepository.save(Carrier.builder()
                .name(name)
                .shippingFee(form.getShippingFee().setScale(0, RoundingMode.HALF_UP))
                .active(form.isActive())
                .build());
    }

    @Transactional
    public Carrier update(Long id, CarrierForm form) {
        Carrier carrier = get(id);
        String name = form.getName().trim();
        checkDuplicateName(id, name);
        if (carrier.isActive() && !form.isActive()) {
            ensureNotLastActive();
        }
        carrier.setName(name);
        carrier.setShippingFee(form.getShippingFee().setScale(0, RoundingMode.HALF_UP));
        carrier.setActive(form.isActive());
        return carrier;
    }

    @Transactional
    public void toggleActive(Long id) {
        Carrier carrier = get(id);
        if (carrier.isActive()) {
            ensureNotLastActive();
        }
        carrier.setActive(!carrier.isActive());
    }

    @Transactional
    public void delete(Long id) {
        Carrier carrier = get(id);
        if (carrier.isActive()) {
            ensureNotLastActive();
        }
        carrierRepository.delete(carrier);
        // flush để lỗi khóa ngoại (đã có đơn hàng dùng nhà vận chuyển này) xuất hiện ngay tại đây
        carrierRepository.flush();
    }

    private void ensureNotLastActive() {
        if (carrierRepository.countByActiveTrue() <= 1) {
            throw new BusinessException("Phải còn ít nhất một nhà vận chuyển đang hoạt động.");
        }
    }

    private void checkDuplicateName(Long selfId, String name) {
        carrierRepository.findFirstByNameIgnoreCase(name).ifPresent(existing -> {
            if (!existing.getId().equals(selfId)) {
                throw new BusinessException("Tên nhà vận chuyển đã tồn tại.");
            }
        });
    }
}