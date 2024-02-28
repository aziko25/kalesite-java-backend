package kalesite.kalesite.Controllers;

import jakarta.persistence.EntityNotFoundException;
import kalesite.kalesite.Models.Orders.Order_OrderProducts;
import kalesite.kalesite.Models.Orders.Order_Order_Products;
import kalesite.kalesite.Models.Orders.Order_Orders;
import kalesite.kalesite.Models.Products.Product_Products;
import kalesite.kalesite.Models.User_Users;
import kalesite.kalesite.Repositories.Orders.Order_OrderProductRepository;
import kalesite.kalesite.Repositories.Orders.Order_Order_ProductsRepository;
import kalesite.kalesite.Repositories.Orders.Order_OrdersRepository;
import kalesite.kalesite.Repositories.Products.Product_ProductsRepository;
import kalesite.kalesite.Repositories.User_UsersRepository;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(maxAge = 3600)
public class OrdersController {

    private final JdbcTemplate jdbcTemplate;
    private final Product_ProductsRepository productProductsRepository;
    private final Order_Order_ProductsRepository order_order_productsRepository;
    private final Order_OrderProductRepository order_orderProductRepository;
    private final Order_OrdersRepository order_ordersRepository;
    private final User_UsersRepository user_usersRepository;

    private static final String SECRET_KEY = "s2FcNkUosXAZQh"; // Adapt based on your settings
    private static final String AUTHORIZATION_FAIL_CODE = "authorization_fail_code";
    private static final String ACTION_NOT_FOUND = "action_not_found";
    private static final String TRANSACTION_NOT_FOUND = "transaction_not_found";
    private static final String SUCCESS = "success";
    private static final String PREPARE = "prepare_action_constant";
    private static final String COMPLETE = "complete_action_constant";

    public static Map<String, String> clickWebhookErrors(String clickTransId, String serviceId, String merchantTransId,
                                                         String amount, String action, String signTime, String signString,
                                                         String merchantPrepareId) {
        Map<String, String> result = new HashMap<>();
        merchantPrepareId = action != null && action.equals("1") ? merchantPrepareId : "";

        String createdSignString = clickTransId + serviceId + SECRET_KEY + merchantTransId + merchantPrepareId + amount + action + signTime;
        String generatedSignString;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(createdSignString.getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            generatedSignString = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            result.put("error", "md5_error");
            result.put("error_note", "MD5 algorithm not found");
            return result;
        }

        if (!generatedSignString.equals(signString)) {
            result.put("error", AUTHORIZATION_FAIL_CODE);
            result.put("error_note", "SIGN CHECK FAILED!"); // Adapt localization
            return result;
        }

        if (!action.equals(PREPARE) && !action.equals(COMPLETE)) {
            result.put("error", ACTION_NOT_FOUND);
            result.put("error_note", "Action not found"); // Adapt localization
            return result;
        }

        if (action.equals(COMPLETE) && !merchantTransId.equals(merchantPrepareId)) {
            result.put("error", TRANSACTION_NOT_FOUND);
            result.put("error_note", "Transaction not found"); // Adapt localization
            return result;
        }

        result.put("error", SUCCESS);
        result.put("error_note", "Success"); // Adapt localization
        return result;
    }


    @PostMapping("/prepare-order")
    public ResponseEntity<?> prepareOrder(@RequestBody Map<String, String> requestData) {
        // Extract parameters from request data
        String clickTransId = requestData.get("click_trans_id");
        String serviceId = requestData.get("service_id");
        String clickPaydocId = requestData.get("click_paydoc_id");
        String merchantTransId = requestData.get("merchant_trans_id");
        String amount = requestData.get("amount");
        String action = requestData.get("action");
        String signTime = requestData.get("sign_time");
        String signString = requestData.get("sign_string");
        String error = requestData.get("error");
        String errorNote = requestData.get("error_note");

        // Assuming clickWebhookErrors, orderLoad and other methods are implemented elsewhere
        Map<String, String> result = clickWebhookErrors(clickTransId, serviceId, merchantTransId, amount, action, signTime, signString, error);

        result.put("click_trans_id", clickTransId);
        result.put("merchant_trans_id", merchantTransId);
        result.put("merchant_prepare_id", merchantTransId);
        result.put("merchant_confirm_id", merchantTransId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/complete-order")
    public ResponseEntity<?> completeOrder(@RequestBody Map<String, String> requestData) {
        // Extract parameters
        String clickTransId = requestData.get("click_trans_id");
        String serviceId = requestData.get("service_id");
        String clickPaydocId = requestData.get("click_paydoc_id");
        String merchantTransId = requestData.get("merchant_trans_id");
        String amount = requestData.get("amount");
        String action = requestData.get("action");
        String signTime = requestData.get("sign_time");
        String signString = requestData.get("sign_string");
        String error = requestData.get("error");
        String errorNote = requestData.get("error_note");
        String merchantPrepareId = requestData.getOrDefault("merchant_prepare_id", null);

        // Similar logic for loading order and handling errors
        Map<String, String> result = clickWebhookErrors(clickTransId, serviceId, merchantTransId, amount, action, signTime, signString, merchantPrepareId);
        result.put("click_trans_id", clickTransId);
        result.put("merchant_trans_id", merchantTransId);
        result.put("merchant_prepare_id", merchantPrepareId);
        result.put("merchant_confirm_id", merchantPrepareId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/create-order")
    @Transactional
    public ResponseEntity<?> createOrder(@RequestBody OrderBody body) {

        System.out.println("create order");

        List<Order_OrderProducts> order_orderProductsList = new ArrayList<>();

        Double orderTotalSum = 0.0;

        for (OrderBody.Product product : body.getProducts()) {

            Order_OrderProducts orderProducts = new Order_OrderProducts();

            orderProducts.setGuid(UUID.randomUUID());
            orderProducts.setCreatedAt(LocalDateTime.now());
            orderProducts.setQuantity(product.getQuantity());
            orderProducts.setOrderPrice(product.getOrderPrice());

            Product_Products productEntity = productProductsRepository.findById(product.getProduct())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + product.getProduct()));

            orderProducts.setProductId(productEntity);
            orderProducts.setDiscount(productEntity.getDiscount());

            order_orderProductRepository.save(orderProducts);

            order_orderProductsList.add(orderProducts);

            orderTotalSum += orderProducts.getOrderPrice();
        }

        Order_Orders order = new Order_Orders();

        order.setGuid(UUID.randomUUID());
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(orderTotalSum);
        order.setInstallation(body.getInstallation());
        order.setOrderedTime(LocalDateTime.now());
        order.setStatus(0);
        order.setComment(body.getComment());
        order.setPaymentStatus("waiting");
        order.setPaymentType(body.getPaymentType());

        User_Users user = user_usersRepository.findById(body.getUser()).orElseThrow();
        order.setUserId(user);

        order_ordersRepository.save(order);

        long sum = 1000000 + order.getId();
        order.setCode("#" + sum);

        for (Order_OrderProducts orderProducts : order_orderProductsList) {

            Order_Order_Products order_order_products = new Order_Order_Products();

            order_order_products.setOrderId(order);
            order_order_products.setOrderProductId(orderProducts);

            order_order_productsRepository.save(order_order_products);
        }

        BillingUrl billingUrl = new BillingUrl();

        if (order.getPaymentType() == 2) {

            billingUrl.setBilling_url("https://my.click.uz/services/pay?service_id=28420&merchant_id=11369&return_url=https://kale.uz/profile/purchases-history&amount=" + orderTotalSum + "&transaction_param=" + order.getId());
        }

        return ResponseEntity.ok(billingUrl);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillingUrl {

        private String billing_url;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderBody {

        private Long user;
        private List<Product> products;
        private Integer address;
        private Boolean installation;
        private String comment;
        private Integer paymentType;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Product {

            private Long product;
            private Double quantity;
            private Double orderPrice;
        }
    }
}