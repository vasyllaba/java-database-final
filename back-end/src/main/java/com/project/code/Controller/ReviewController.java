package com.project.code.Controller;

import com.project.code.Model.Customer;
import com.project.code.Model.Review;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public ReviewController(ReviewRepository reviewRepository, CustomerRepository customerRepository) {
        this.reviewRepository = reviewRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/{storeId}/{productId}")
    public Map<String, Object> getReviews(@PathVariable("storeId") Long storeId,
                                          @PathVariable("productId") Long productId) {
        List<Map<String, Object>> reviews = new ArrayList<>();
        for (Review review : reviewRepository.findByStoreIdAndProductId(storeId, productId)) {
            String customerName = review.getCustomerId() == null
                    ? "Unknown"
                    : customerRepository.findById(review.getCustomerId())
                            .map(Customer::getName)
                            .orElse("Unknown");

            Map<String, Object> filteredReview = new HashMap<>();
            filteredReview.put("comment", review.getComment());
            filteredReview.put("rating", review.getRating());
            filteredReview.put("customerName", customerName);
            reviews.add(filteredReview);
        }
        return Map.of("reviews", reviews);
    }
}
