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
    public ResponseEntity<?> prepareOrder(@RequestBody Map<String, String> body) {

        System.out.println("Prepare Order API Request Received");

        return ResponseEntity.ok("ok");
    }

    @PostMapping("/complete-order")
    public ResponseEntity<?> completeOrder(@RequestBody Map<String, String> body) {

        System.out.println("Complete Order API Request Received");

        return ResponseEntity.ok("ok");
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