package com.oneshop.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.oneshop.common.BusinessException;
import com.oneshop.common.MediaType;
import com.oneshop.common.OrderStatus;
import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.config.CloudinaryService;
import com.oneshop.entity.OrderItem;
import com.oneshop.entity.Product;
import com.oneshop.entity.Review;
import com.oneshop.entity.ReviewMedia;
import com.oneshop.entity.User;
import com.oneshop.repository.OrderItemRepository;
import com.oneshop.repository.ProductRepository;
import com.oneshop.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int PAGE_SIZE = 5;
    private static final int MAX_FILES = 5;
    private static final int MIN_CONTENT = 50;
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final long MAX_VIDEO_BYTES = 10L * 1024 * 1024;
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> VIDEO_TYPES = Set.of("video/mp4", "video/webm", "video/quicktime");

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    // ===== Xem =====

    /** Dữ liệu phần đánh giá cho trang chi tiết. user = null nếu là khách chưa đăng nhập. */
    @Transactional(readOnly = true)
    public ReviewSection section(Long productId, User user, int page) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);
        Double average = reviewRepository.averageRating(productId);

        boolean canReview = false;
        String note = null;
        if (user == null) {
            note = "Đăng nhập và mua sản phẩm này để viết đánh giá.";
        } else if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            note = "Bạn đã đánh giá sản phẩm này rồi.";
        } else if (orderItemRepository.findPurchased(user.getId(), productId, OrderStatus.DELIVERED).isEmpty()) {
            note = "Chỉ khách đã mua và nhận hàng thành công mới được đánh giá.";
        } else {
            canReview = true;
        }
        return new ReviewSection(productId, reviews, average == null ? 0 : average,
                reviews.getTotalElements(), user != null, canReview, note);
    }

    /** Vendor xem đánh giá của tất cả sản phẩm trong shop mình. */
    @Transactional(readOnly = true)
    public Page<Review> forShop(Long shopId, int page) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by(Sort.Direction.DESC, "id"));
        return reviewRepository.findByProductShopId(shopId, pageable);
    }

    // ===== Viết đánh giá =====

    @Transactional
    public void create(User user, Long productId, ReviewForm form) {
        if (user == null) {
            throw new BusinessException("Vui lòng đăng nhập.");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm."));

        // Kiểm tra lại ở server (không tin form): trim rồi mới đếm ký tự
        String content = form.getContent() == null ? "" : form.getContent().trim();
        if (content.length() < MIN_CONTENT) {
            throw new BusinessException("Bình luận phải có ít nhất 50 ký tự (không tính khoảng trắng đầu/cuối).");
        }
        if (form.getRating() < 1 || form.getRating() > 5) {
            throw new BusinessException("Vui lòng chọn số sao từ 1 đến 5.");
        }
        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new BusinessException("Bạn đã đánh giá sản phẩm này rồi.");
        }
        List<OrderItem> purchased = orderItemRepository.findPurchased(user.getId(), productId, OrderStatus.DELIVERED);
        if (purchased.isEmpty()) {
            throw new BusinessException("Chỉ khách đã mua và nhận hàng thành công mới được đánh giá.");
        }

        List<MultipartFile> files = form.getMedia() == null ? List.of()
                : form.getMedia().stream().filter(f -> f != null && !f.isEmpty()).toList();
        validateFiles(files);

        Review review = Review.builder()
                .user(user)
                .product(product)
                .orderItem(purchased.get(0))
                .rating(form.getRating())
                .content(content)
                .build();
        review.getMedia().addAll(upload(files, review));
        reviewRepository.save(review);

        // Cập nhật điểm trung bình của sản phẩm (làm tròn 1 chữ số thập phân)
        Double average = reviewRepository.averageRating(productId);
        product.setAvgRating(average == null ? 0 : Math.round(average * 10) / 10.0);
    }

    // ===== Xử lý tệp đính kèm =====

    private void validateFiles(List<MultipartFile> files) {
        if (files.size() > MAX_FILES) {
            throw new BusinessException("Chỉ được đính kèm tối đa " + MAX_FILES + " tệp.");
        }
        for (MultipartFile file : files) {
            String type = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
            if (IMAGE_TYPES.contains(type)) {
                if (file.getSize() > MAX_IMAGE_BYTES) {
                    throw new BusinessException("Mỗi ảnh tối đa 5MB.");
                }
            } else if (VIDEO_TYPES.contains(type)) {
                if (file.getSize() > MAX_VIDEO_BYTES) {
                    throw new BusinessException("Mỗi video tối đa 10MB.");
                }
            } else {
                throw new BusinessException("Chỉ nhận ảnh (JPG, PNG, WEBP, GIF) hoặc video (MP4, WEBM, MOV).");
            }
        }
    }

    /** Upload lên Cloudinary; nếu một tệp lỗi thì xóa các tệp đã upload trước đó. */
    private List<ReviewMedia> upload(List<MultipartFile> files, Review review) {
        List<ReviewMedia> uploaded = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                boolean video = file.getContentType().toLowerCase().startsWith("video/");
                CloudinaryService.UploadResult result = video
                        ? cloudinaryService.uploadVideo(file, "reviews")
                        : cloudinaryService.uploadImage(file, "reviews");
                uploaded.add(ReviewMedia.builder()
                        .review(review)
                        .url(result.url())
                        .publicId(result.publicId())
                        .type(video ? MediaType.VIDEO : MediaType.IMAGE)
                        .build());
            }
            return uploaded;
        } catch (Exception e) {
            for (ReviewMedia media : uploaded) {
                try {
                    cloudinaryService.delete(media.getPublicId(), media.getType() == MediaType.VIDEO ? "video" : "image");
                } catch (Exception ignored) {
                    // không làm hỏng luồng báo lỗi chính
                }
            }
            throw new BusinessException("Không tải được tệp đính kèm lên máy chủ, vui lòng thử lại.");
        }
    }
}