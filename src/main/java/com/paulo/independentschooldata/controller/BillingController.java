package com.paulo.independentschooldata.controller;

import com.paulo.independentschooldata.domain.Plan;
import com.paulo.independentschooldata.domain.SubscriptionRecord;
import com.paulo.independentschooldata.exceptions.BadRequestException;
import com.paulo.independentschooldata.exceptions.PaymentException;
import com.paulo.independentschooldata.exceptions.ResourceNotFoundException;
import com.paulo.independentschooldata.repos.PlanRepository;
import com.paulo.independentschooldata.repos.SchoolRepository;
import com.paulo.independentschooldata.repos.SubscriptionRepository;
import com.paulo.independentschooldata.service.MailService;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bill")
@RequiredArgsConstructor
@Slf4j
public class BillingController {

    private final PlanRepository planRepo;
    private final SubscriptionRepository subRepo;
    private final SchoolRepository schoolRepository;
    private final MailService mailService;

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @GetMapping("/plans")
    public List<Plan> listPlans() {
        return planRepo.findAll().stream().filter(Plan::isActive).collect(Collectors.toList());
    }

    @PostMapping("/create-checkout-session")
    public Map<String, String> createCheckoutSession(@RequestBody CreateCheckoutRequest req) {
        Plan plan = planRepo.findById(req.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", req.getPlanId()));

        try {
            Map<String, Object> sessionParams = new HashMap<>();
            sessionParams.put("mode", "subscription");
            sessionParams.put("payment_method_types", List.of("card"));
            sessionParams.put("line_items", List.of(Map.of("price", plan.getStripePriceId(), "quantity", 1)));
            sessionParams.put("success_url", "https://independent-schools.uk/school-dashboard?subscription=success?session_id={CHECKOUT_SESSION_ID}");
            sessionParams.put("cancel_url", "https://independent-schools.uk/school-dashboard");
            sessionParams.put("metadata", Map.of("schoolId", String.valueOf(req.getSchoolId()), "planId", String.valueOf(plan.getId())));

            Session session = Session.create(sessionParams);

            SubscriptionRecord sub = new SubscriptionRecord();
            sub.setPlanId(plan.getId());
            sub.setSchoolId(req.getSchoolId());
            sub.setStripeSessionId(session.getId());
            sub.setStatus("created");
            subRepo.save(sub);

            mailService.sendSimpleEmailToOurSelves("Checkout session created", sub.toString());

            return Map.of("url", session.getUrl());
        } catch (StripeException e) {
            log.error("Stripe error creating checkout session: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create checkout session", e);
        }
    }

    @PostMapping("/create-product")
    public Map<String, String> createProduct(@RequestBody CreateCheckoutRequest req) {
        Plan plan = planRepo.findById(req.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", req.getPlanId()));
        schoolRepository.findById(req.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School", "id", req.getSchoolId()));

        try {
            String priceId = plan.getStripePriceId();
            if (priceId == null) {
                Map<String, Object> productParams = new HashMap<>();
                productParams.put("name", plan.getName());
                productParams.put("description", plan.getDescription());
                Product product = Product.create(productParams);

                Map<String, Object> priceParams = new HashMap<>();
                priceParams.put("unit_amount", plan.getDefaultPricePence());
                priceParams.put("currency", "gbp");
                priceParams.put("product", product.getId());
                priceParams.put("recurring", Map.of("interval", plan.getBillingPeriod().equals("monthly") ? "month" : "year"));
                Price price = Price.create(priceParams);

                priceId = price.getId();
                plan.setStripePriceId(priceId);
                planRepo.save(plan);
            }

            return Map.of("priceId", priceId);
        } catch (StripeException e) {
            log.error("Stripe error creating product: {}", e.getMessage(), e);
            throw new PaymentException("Failed to create product", e);
        }
    }

    @PostMapping("/cancel-subscription/{recordId}")
    public Map<String, String> cancelSubscription(@PathVariable Long recordId) {
        SubscriptionRecord record = subRepo.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionRecord", "id", recordId));

        String stripeSubscriptionId = record.getStripeSubscriptionId();
        if (stripeSubscriptionId == null || stripeSubscriptionId.isEmpty()) {
            throw new BadRequestException("No Stripe Subscription ID found for this record");
        }

        try {
            Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
            Subscription cancelledSubscription = subscription.cancel();

            record.setStatus("cancelled_by_user");
            subRepo.save(record);

            mailService.sendSimpleEmailToOurSelves("Subscription cancelled", record.toString());

            log.info("Subscription {} cancelled successfully", cancelledSubscription.getId());

            return Map.of(
                    "status", "success",
                    "message", "Subscription cancelled immediately.",
                    "stripeSubscriptionId", cancelledSubscription.getId()
            );
        } catch (StripeException e) {
            log.error("Stripe error during subscription cancellation: {}", e.getMessage(), e);
            throw new PaymentException("Failed to cancel subscription: " + e.getMessage(), e);
        }
    }

    @PostMapping("/cancel-subscription-period-end/{recordId}")
    public Map<String, Object> cancelSubscriptionAtPeriodEnd(@PathVariable Long recordId) {
        SubscriptionRecord record = subRepo.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionRecord", "id", recordId));

        String stripeSubscriptionId = record.getStripeSubscriptionId();
        if (stripeSubscriptionId == null || stripeSubscriptionId.isEmpty()) {
            throw new BadRequestException("No Stripe Subscription ID found for this record");
        }

        try {
            Map<String, Object> params = Map.of("cancel_at_period_end", true);

            Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
            Subscription updatedSubscription = subscription.update(params);

            record.setStatus("cancelled_at_period_end");
            record.setCancelAtPeriodEnd(true);
            subRepo.save(record);

            mailService.sendSimpleEmailToOurSelves("Subscription cancelled at end of period", record.toString());

            log.info("Subscription {} set to cancel at period end", updatedSubscription.getId());

            return Map.of(
                    "status", "success",
                    "message", "Subscription is set to cancel at the end of the current billing period.",
                    "stripeSubscriptionId", updatedSubscription.getId(),
                    "subscription", record
            );
        } catch (StripeException e) {
            log.error("Stripe error during period-end cancellation: {}", e.getMessage(), e);
            throw new PaymentException("Failed to schedule subscription cancellation: " + e.getMessage(), e);
        }
    }

    @PostMapping("/reactivate-subscription/{recordId}")
    public Map<String, Object> reactivateSubscription(@PathVariable Long recordId) {
        SubscriptionRecord record = subRepo.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("SubscriptionRecord", "id", recordId));

        String stripeSubscriptionId = record.getStripeSubscriptionId();
        if (stripeSubscriptionId == null || stripeSubscriptionId.isEmpty()) {
            throw new BadRequestException("No Stripe Subscription ID found for this record");
        }

        if (!"cancelled_at_period_end".equals(record.getStatus())) {
            throw new BadRequestException("Subscription is not currently set to cancel at period end");
        }

        try {
            Map<String, Object> params = Map.of("cancel_at_period_end", false);

            Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
            Subscription updatedSubscription = subscription.update(params);

            record.setStatus("active");
            record.setCancelAtPeriodEnd(false);
            subRepo.save(record);

            mailService.sendSimpleEmailToOurSelves("Subscription reactivated", record.toString());

            log.info("Subscription {} reactivated successfully", updatedSubscription.getId());

            return Map.of(
                    "status", "success",
                    "message", "Subscription successfully reactivated and will renew at the end of the period.",
                    "stripeSubscriptionId", updatedSubscription.getId(),
                    "subscription", record
            );
        } catch (StripeException e) {
            log.error("Stripe error during reactivation: {}", e.getMessage(), e);
            throw new PaymentException("Failed to reactivate subscription: " + e.getMessage(), e);
        }
    }

    @GetMapping("/subscription-details/{schoolId}")
    public SubscriptionRecord getSubscriptionDetails(@PathVariable Long schoolId) {
        return subRepo.findBySchoolId(schoolId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "schoolId", schoolId));
    }

    @Getter
    @Setter
    public static class CreateCheckoutRequest {
        private Long planId;
        private Long schoolId;
    }
}
