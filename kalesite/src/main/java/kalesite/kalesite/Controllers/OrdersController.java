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

    private static final String SECRET_KEY = "s2FcNkUosXAZQh";
    private static final String MERCHANT_USER_ID = "33370";


    @PostMapping("/prepare-order")
    public ResponseEntity<?> prepareOrder(@RequestParam Map<String, String> body) {

        System.out.println("Prepare Order API Request Received");

        String clickTransId = body.get("click_trans_id");
        String merchantTransId = body.get("merchant_trans_id");

        String error = "0";
        String errorNote = "Success";

        Map<String, String> response = new HashMap<>();

        response.put("click_trans_id", clickTransId);
        response.put("merchant_trans_id", merchantTransId);
        response.put("merchant_prepare_id", merchantTransId);
        response.put("error", error);
        response.put("error_note", errorNote);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/complete-order")
    public ResponseEntity<?> completeOrder(@RequestParam Map<String, String> body) {
        System.out.println("Complete Order API Request Received");

        // Получение параметров из запроса
        String clickTransId = body.get("click_trans_id");
        String serviceId = body.get("service_id");
        String clickPaydocId = body.get("click_paydoc_id");
        String merchantTransId = body.get("merchant_trans_id");
        String merchantPrepareId = body.get("merchant_prepare_id");
        String amount = body.get("amount");
        String action = body.get("action"); // Должно быть 1 для Complete
        String error = body.get("error"); // Статус завершения платежа
        String signTime = body.get("sign_time");
        // Проверка подписи, как описано в документации, не показана здесь

        // Логика проверки статуса заказа и обработки завершения платежа
        // Это может включать в себя проверку, был ли платеж уже обработан, корректность суммы и т.д.

        // Пример ответа
        Map<String, Object> response = new HashMap<>();
        response.put("click_trans_id", clickTransId);
        response.put("merchant_trans_id", merchantTransId);
        // Этот ID должен быть реальным ID транзакции завершения платежа в вашей системе, может быть null, если ошибка
        int merchantConfirmId = error.equals("0") ? /* Получить ID транзакции завершения из вашей системы */ 1 : null;
        response.put("merchant_confirm_id", merchantConfirmId);
        response.put("error", error); // "0" для успешного завершения, другое значение для ошибки
        response.put("error_note", "Success"); // Или описание ошибки, если таковая имеется

        // В зависимости от результата обработки, измените error и error_note соответственно
        // Например, если платеж не может быть завершен по какой-либо причине, укажите соответствующий код ошибки и описание

        return ResponseEntity.ok(response);
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

            billingUrl.setBilling_url("https://my.click.uz/services/pay?service_id=28420&merchant_id=11369&return_url=https://kale.mdholding.uz/profile/purchases-history&amount=" + orderTotalSum + "&transaction_param=" + order.getId());
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