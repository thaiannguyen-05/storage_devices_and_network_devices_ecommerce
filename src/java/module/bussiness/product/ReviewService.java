package module.bussiness.product;

import entity.ProductEntity;
import entity.ProductReviewEntity;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import module.bussiness.product.dto.CreateReviewDto;
import module.bussiness.product.repository.impl.ProductRepository;
import module.bussiness.product.repository.impl.ProductReviewRepository;
import module.bussiness.product.response_dto.CreateReviewResponseDto;
import module.bussiness.product.response_dto.GetReviewsResponseDto;
import module.core.common.BaseResponse;

public class ReviewService {
    private final ProductReviewRepository reviewRepository = new ProductReviewRepository();
    private final ProductRepository productRepository = new ProductRepository();

    public CreateReviewResponseDto createReview(CreateReviewDto dto) {
        CreateReviewResponseDto response = new CreateReviewResponseDto();
        if (dto == null) {
            fail(response, "Review data is required");
            return response;
        }
        if (isBlank(dto.getProductId())) {
            fail(response, "Product is required");
            return response;
        }
        if (isBlank(dto.getUserId())) {
            fail(response, "Please log in to review this product");
            return response;
        }
        ProductEntity product = productRepository.findById(dto.getProductId());
        if (product == null) {
            fail(response, "Product not found");
            return response;
        }
        if (reviewRepository.existsByUserIdAndProductId(dto.getUserId(), dto.getProductId())) {
            fail(response, "You have already reviewed this product");
            return response;
        }
        if (dto.getRating() < 1 || dto.getRating() > 5) {
            fail(response, "Rating must be between 1 and 5");
            return response;
        }
        String comment = dto.getComment() == null ? "" : dto.getComment().trim();
        if (comment.isEmpty()) {
            fail(response, "Comment is required");
            return response;
        }
        if (comment.length() > 1000) {
            fail(response, "Comment must be 1000 characters or fewer");
            return response;
        }

        LocalDateTime now = LocalDateTime.now();
        ProductReviewEntity review = new ProductReviewEntity(
                UUID.randomUUID().toString(),
                dto.getProductId(),
                dto.getUserId(),
                dto.getRating(),
                comment,
                "APPROVED",
                now,
                now);
        reviewRepository.insert(review);

        response.setSuccess(true);
        response.setSuccessMessage("Review submitted");
        return response;
    }

    public GetReviewsResponseDto getReviewsByProductId(String productId) {
        GetReviewsResponseDto response = new GetReviewsResponseDto();
        if (isBlank(productId)) {
            response.setReviews(Collections.emptyList());
            response.setAverageRating(0);
            response.setTotalReviews(0);
            fail(response, "Product is required");
            return response;
        }
        response.setReviews(reviewRepository.findByProductIdApproved(productId));
        response.setAverageRating(reviewRepository.calculateAverageRating(productId));
        response.setTotalReviews(reviewRepository.countByProductId(productId));
        response.setSuccess(true);
        return response;
    }

    public boolean hasReviewed(String userId, String productId) {
        return !isBlank(userId) && !isBlank(productId) && reviewRepository.existsByUserIdAndProductId(userId, productId);
    }

    private void fail(BaseResponse response, String message) {
        response.setSuccess(false);
        response.setErrorMessage(message);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
