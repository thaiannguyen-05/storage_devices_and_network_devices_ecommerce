package module.bussiness.product.response_dto;

import entity.ReviewView;
import java.util.List;
import module.core.common.BaseResponse;

public class GetReviewsResponseDto extends BaseResponse {
    private List<ReviewView> reviews;
    private double averageRating;
    private int totalReviews;

    public List<ReviewView> getReviews() { return reviews; }
    public void setReviews(List<ReviewView> reviews) { this.reviews = reviews; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public int getTotalReviews() { return totalReviews; }
    public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }
}
