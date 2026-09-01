package com.paulo.independentschooldata.controller;

import com.paulo.independentschooldata.domain.SubscriptionRecord;
import com.paulo.independentschooldata.repos.SubscriptionRepository;
import com.paulo.independentschooldata.service.MailService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Slf4j
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final SubscriptionRepository subscriptionRepository;
    private final MailService mailService;

    public StripeWebhookController(SubscriptionRepository subscriptionRepository, MailService mailService) {
        this.subscriptionRepository = subscriptionRepository;
        this.mailService = mailService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("Stripe-Signature") String sigHeader,
            @RequestBody String payload
    ) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        String type = event.getType();
        log.info("Received event: {}", type);

        switch (type) {

            // ------------------------------
            // 1) Checkout completed
            // ------------------------------
            case "checkout.session.completed":
                handleCheckoutCompleted(event);
                break;

            // ------------------------------
            // 2) Subscription officially created
            // ------------------------------
            case "customer.subscription.created":
                handleSubscriptionCreated(event);
                break;

            // ------------------------------
            // 3) Recurring invoice success
            // ------------------------------
            case "invoice.payment_succeeded":
                handleInvoiceSucceeded(event);
                break;

            // ------------------------------
            // 4) Payment failed (card declined)
            // ------------------------------
            case "invoice.payment_failed":
                handleInvoiceFailed(event);
                break;

            // ------------------------------
            // 5) Subscription cancelled
            // ------------------------------
            case "customer.subscription.deleted":
                handleSubscriptionCancelled(event);
                break;

            default:
//                System.out.println("Ignoring event type: " + type);
                break;
        }

        return ResponseEntity.ok("");
    }

    // ---------------------------------------------------------------------------
    // HANDLERS
    // ---------------------------------------------------------------------------

    private void handleCheckoutCompleted(Event event) {
        var session = (com.stripe.model.checkout.Session)
                event.getDataObjectDeserializer().getObject().orElse(null);

        if (session == null) return;

        String sessionId = session.getId();
        String customerId = session.getCustomer();
        String subscriptionId = session.getSubscription();
        String paymentIntentId = session.getPaymentIntent();

        Optional<SubscriptionRecord> subscriptionRecord = subscriptionRepository.findByStripeSessionId(sessionId);

        if (subscriptionRecord.isPresent()) {
            SubscriptionRecord record = subscriptionRecord.get();
            record.setStatus("completed");
            record.setStripeCustomerId(customerId);
            record.setStripeSubscriptionId(subscriptionId);
            record.setStripePaymentIntentId(paymentIntentId);
            subscriptionRepository.save(record);
        }

        subscriptionRecord.ifPresent(record -> mailService.sendSimpleEmailToOurSelves("Checkout completed", record.toString()));

    }

    private void handleSubscriptionCreated(Event event) {
        Subscription sub = (com.stripe.model.Subscription)
                event.getDataObjectDeserializer().getObject().orElse(null);

        if (sub == null) return;

        String subscriptionId = sub.getId();
        String customerId = sub.getCustomer();

        // Find the primary subscription item to get the billing dates
        Optional<SubscriptionItem> primaryItem = getPrimarySubscriptionItem(sub);

        // 1. Timestamps & Period End Flags
        Instant periodStart;
        Instant periodEnd;
        if (primaryItem.isPresent()) {
            SubscriptionItem subscriptionItem = primaryItem.get();
            // Use standard getters on SubscriptionItem, which are available
            Long periodStartLong = subscriptionItem.getCurrentPeriodStart();
            Long periodEndLong = subscriptionItem.getCurrentPeriodEnd();

            periodStart = periodStartLong != null ? Instant.ofEpochSecond(periodStartLong) : null;
            periodEnd = periodEndLong != null ? Instant.ofEpochSecond(periodEndLong) : null;
        } else {
            periodEnd = null;
            periodStart = null;
        }


        // 2. Boolean flag (STAYS on Subscription object)
        Boolean cancelAtPeriodEnd = sub.getCancelAtPeriodEnd() != null
                ? sub.getCancelAtPeriodEnd()
                : false;

        // 3. Latest Invoice ID (STAYS on Subscription object)
        String latestInvoiceId;
        if (sub.getLatestInvoice() != null) {
            latestInvoiceId = sub.getLatestInvoice();
        } else {
            latestInvoiceId = null;
        }


        Optional<SubscriptionRecord> subscriptionRecord = subscriptionRepository.findByStripeSubscriptionId(subscriptionId);

        if (subscriptionRecord.isPresent()) {

            SubscriptionRecord record = subscriptionRecord.get();
            record.setStatus("active");
            record.setStripeCustomerId(customerId);
            record.setStripeSubscriptionId(subscriptionId);

            // Update the new fields
            record.setCurrentPeriodStart(periodStart);
            record.setCurrentPeriodEnd(periodEnd);
            record.setCancelAtPeriodEnd(cancelAtPeriodEnd);
            record.setLatestInvoiceId(latestInvoiceId);

            subscriptionRepository.save(record);
        }

        subscriptionRecord.ifPresent(record -> mailService.sendSimpleEmailToOurSelves("Subscription created event", record.toString()));

    }

    private Optional<SubscriptionItem> getPrimarySubscriptionItem(com.stripe.model.Subscription sub) {
        // A standard subscription will have one item in the collection
        if (sub.getItems() != null && sub.getItems().getData() != null) {
            List<SubscriptionItem> items = sub.getItems().getData();
            if (!items.isEmpty()) {
                return Optional.of(items.getFirst());
            }
        }
        return Optional.empty();
    }

    // Reuse the SubscriptionItem helper from the previous step (put it in StripeWebhookController)
// private Optional<SubscriptionItem> getPrimarySubscriptionItem(com.stripe.model.Subscription sub) { ... }
// private String extractSubscriptionId(Invoice invoice) { ... } // Use the corrected parent logic

    private void handleInvoiceSucceeded(Event event) {
        Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (invoice == null) return;

        String subscriptionId = extractSubscriptionId(invoice);
        if (subscriptionId == null) {
            System.out.println("Invoice has no subscription associated.");
            return;
        }

        try {
            // 1. Retrieve the renewed Subscription object (expanded to get dates)
            Subscription renewedSub = retrieveExpandedSubscription(subscriptionId);

            // 2. Extract new dates and invoice ID
            Optional<SubscriptionItem> primaryItem = getPrimarySubscriptionItem(renewedSub);

            Instant periodEnd;
            Instant periodStart;
            if (primaryItem.isPresent()) {
                // Get the new period end date from the SubscriptionItem
                Long periodEndLong = primaryItem.get().getCurrentPeriodEnd();
                Long periodStartLong = primaryItem.get().getCurrentPeriodStart();

                periodEnd = periodEndLong != null ? Instant.ofEpochSecond(periodEndLong) : null;
                periodStart = periodStartLong != null ? Instant.ofEpochSecond(periodStartLong) : null;
            } else {
                periodEnd = null;
                periodStart = null;
            }

            // Get the ID of the invoice that just succeeded
            String latestInvoiceId = invoice.getId();

            // 3. Update the local record
            Optional<SubscriptionRecord> subscriptionRecord = subscriptionRepository.findByStripeSubscriptionId(subscriptionId);

            if (subscriptionRecord.isPresent()) {
                SubscriptionRecord record = subscriptionRecord.get();

                record.setStatus("active");
                record.setCurrentPeriodEnd(periodEnd); // Set the new renewal date
                record.setLatestInvoiceId(latestInvoiceId); // Set the new receipt ID
                record.setCurrentPeriodStart(periodStart);
                // Note: cancelAtPeriodEnd status may have been updated
                // by a subscription.updated webhook if it was set to true

                subscriptionRepository.save(record);
            }

            subscriptionRecord.ifPresent(record -> mailService.sendSimpleEmailToOurSelves("Invoice succeeded event", record.toString()));


        } catch (StripeException e) {
            log.error("Stripe API Error fetching subscription for renewal: {}", e.getMessage(), e);
        }
    }


    /**
     * Retrieves the full Stripe Subscription object, including expanded items.
     *
     * @param subscriptionId The ID of the Stripe Subscription.
     * @return The expanded Subscription object.
     */
    private Subscription retrieveExpandedSubscription(String subscriptionId) throws StripeException {
        // We must expand the 'items' field to access the SubscriptionItem details
        // that hold the current_period_end date.
        Map<String, Object> params = new HashMap<>();
        params.put("expand", List.of("items"));

        return Subscription.retrieve(subscriptionId, params, null);
    }


    private void handleInvoiceFailed(Event event) {
        Invoice invoice = (com.stripe.model.Invoice)
                event.getDataObjectDeserializer().getObject().orElse(null);

        if (invoice == null) return;

        String subscriptionId = extractSubscriptionId(invoice);
        if (subscriptionId == null) {
            System.out.println("Invoice has no subscription associated.");
            return;
        }
        Optional<SubscriptionRecord> subscriptionRecord = subscriptionRepository.findByStripeSubscriptionId(subscriptionId);

        if (subscriptionRecord.isPresent()) {
            SubscriptionRecord record = subscriptionRecord.get();

            record.setStatus("payment_failed");
            subscriptionRepository.save(record);
        }

        subscriptionRecord.ifPresent(record -> mailService.sendSimpleEmailToOurSelves("Invoice failed event", record.toString()));

    }

    private void handleSubscriptionCancelled(Event event) {
        var sub = (com.stripe.model.Subscription)
                event.getDataObjectDeserializer().getObject().orElse(null);

        if (sub == null) return;

        String subscriptionId = sub.getId();

        Optional<SubscriptionRecord> subscriptionRecord = subscriptionRepository.findByStripeSubscriptionId(subscriptionId);

        if (subscriptionRecord.isPresent()) {
            SubscriptionRecord record = subscriptionRecord.get();

            record.setStatus("cancelled");
            subscriptionRepository.save(record);
        }

        subscriptionRecord.ifPresent(record -> mailService.sendSimpleEmailToOurSelves("Subscription canceled event", record.toString()));

    }

    private String extractSubscriptionId(Invoice invoice) {
        // 1. Check the deprecated, but still common, direct field (might work
        //    if your API version is older or the SDK hasn't fully removed the getter)
        // You should use reflection here if you can't see the getter, but let's
        // assume you need the new path.

        // 2. MODERN / CORRECT PATH (for newer Stripe API versions like 'Basil' and later)
        Invoice.Parent parent = invoice.getParent();

        if (parent != null && "subscription_details".equals(parent.getType())) {
            Invoice.Parent.SubscriptionDetails subscriptionDetails = parent.getSubscriptionDetails();

            if (subscriptionDetails != null && subscriptionDetails.getSubscription() != null) {
                // This is the correct Subscription ID string from the parent object
                return subscriptionDetails.getSubscription();
            }
        }

        // Fallback: If your Stripe API version is older, the subscription ID might still
        // be available via the old direct accessor. Since it throws a compile error,
        // you likely need to update the way you access it.

        // If you were using an older SDK/API (pre-Basil):
        // if (invoice.getSubscription() != null) {
        //     return invoice.getSubscription();
        // }

        // If you need to access the direct field on older API versions but your
        // current SDK hides the method, you can sometimes use the 'get' method
        // on the Stripe object's underlying map, but this is a last resort.

        // Sticking to the Parent object is the future-proof solution.
        return null; // No subscription ID found
    }
}

