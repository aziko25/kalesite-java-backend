package kalesite.kalesite.Controllers;

import jakarta.persistence.EntityNotFoundException;
import kalesite.kalesite.Models.Address_Addresses;
import kalesite.kalesite.Models.Orders.Order_OrderProducts;
import kalesite.kalesite.Models.Orders.Order_Order_Products;
import kalesite.kalesite.Models.Orders.Order_Orders;
import kalesite.kalesite.Models.Products.Product_Products;
import kalesite.kalesite.Models.User_Users;
import kalesite.kalesite.Repositories.Address_AddressesRepository;
import kalesite.kalesite.Repositories.Orders.Order_OrderProductRepository;
import kalesite.kalesite.Repositories.Orders.Order_Order_ProductsRepository;
import kalesite.kalesite.Repositories.Orders.Order_OrdersRepository;
import kalesite.kalesite.Repositories.Products.Product_ProductsRepository;
import kalesite.kalesite.Repositories.User_UsersRepository;
import kalesite.kalesite.Telegram.MainTelegramBot;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(maxAge = 3600)
public class OrdersController {

    private final Product_ProductsRepository productProductsRepository;
    private final Order_Order_ProductsRepository order_order_productsRepository;
    private final Order_OrderProductRepository order_orderProductRepository;
    private final Order_OrdersRepository order_ordersRepository;
    private final User_UsersRepository user_usersRepository;
    private final MainTelegramBot telegramBot;
    private final Address_AddressesRepository address_addressesRepository;

    @Value("${orders_chat_id}")
    private String chatId;

    @GetMapping("/order-list")
    public ResponseEntity<?> orderList(@RequestParam(defaultValue = "0") int offset,
                                       @RequestParam(defaultValue = "10") int limit) {

        int pageNumber = offset / limit;
        PageRequest pageRequest = PageRequest.of(pageNumber, limit, Sort.by("orderedTime").descending());
        Page<Order_Orders> orders = order_ordersRepository.findAll(pageRequest);

        List<Map<String, Object>> ordersResponseList = new ArrayList<>();

        for (Order_Orders order : orders) {

            Map<String, Object> orderMap = new LinkedHashMap<>();

            orderMap.put("id", order.getId());
            orderMap.put("guid", order.getGuid());
            orderMap.put("user", order.getUserId() != null ? order.getUserId().getId() : null);
            orderMap.put("code", order.getCode());

            if (order.getUserId() != null) {
                orderMap.put("orderer", order.getUserId().getPhone() + " " + order.getUserId().getName());
            }

            if (order.getAddressId() != null) {
                orderMap.put("address", order.getAddressId().getRegion() + " " + order.getAddressId().getDistrict() + " " + order.getAddressId().getStreet());
            }

            orderMap.put("comment", order.getComment());

            List<Map<String, Object>> productsList = new ArrayList<>();

            List<Order_Order_Products> orderProducts = order_order_productsRepository.findAllByOrderId(order);

            for (Order_Order_Products orderProduct : orderProducts) {

                Map<String, Object> productDetailMap = new LinkedHashMap<>();

                Order_OrderProducts orderProductDetails = orderProduct.getOrderProductId();
                Product_Products product = orderProductDetails.getProductId();

                if (product != null) {

                    Map<String, Object> productMap = new LinkedHashMap<>();

                    productMap.put("id", product.getId());
                    productMap.put("guid", product.getGuid());
                    productMap.put("code", product.getCode());
                    productMap.put("title", product.getTitle());
                    productMap.put("unit", product.getUnit());
                    productMap.put("brand", product.getBrand());
                    productMap.put("size", product.getSize());
                    productMap.put("photo_small", "https://api.kale.mdholding.uz/media/" + product.getPhoto());

                    productDetailMap.put("product", productMap);
                    productDetailMap.put("quantity", orderProductDetails.getQuantity());
                    productDetailMap.put("orderPrice", orderProductDetails.getOrderPrice());
                    productDetailMap.put("discount", orderProductDetails.getDiscount());
                }

                productsList.add(productDetailMap);
            }

            orderMap.put("products", productsList);
            orderMap.put("totalAmount", order.getTotalAmount());
            orderMap.put("orderedTime", order.getOrderedTime());
            orderMap.put("deliveredTime", order.getDeliveredTime());
            orderMap.put("paymentStatus", order.getPaymentStatus());
            orderMap.put("paymentType", order.getPaymentType());
            orderMap.put("status", order.getStatus());

            ordersResponseList.add(orderMap);
        }

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("results", ordersResponseList);
        response.put("count", orders.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/order-history")
    public ResponseEntity<?> orderHistory(@RequestParam String name) {

        User_Users user = user_usersRepository.findByName(name);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        List<Order_Orders> orders = order_ordersRepository.findAllByUserId(user, Sort.by("orderedTime").descending());

        List<Map<String, Object>> ordersResponseList = new ArrayList<>();

        for (Order_Orders order : orders) {

            Map<String, Object> orderMap = new HashMap<>();

            orderMap.put("code", order.getCode());
            orderMap.put("paymentStatus", order.getPaymentStatus());
            orderMap.put("orderedTime", order.getOrderedTime());
            orderMap.put("totalAmount", order.getTotalAmount());

            List<Order_Order_Products> orderProducts = order_order_productsRepository.findAllByOrderId(order);

            List<Map<String, Object>> productsList = new ArrayList<>();

            for (Order_Order_Products orderProduct : orderProducts) {

                Order_OrderProducts orderProductDetails = orderProduct.getOrderProductId();

                Product_Products product = orderProductDetails.getProductId();

                Map<String, Object> productMap = new HashMap<>();

                if (product != null) {

                    productMap.put("photo_small", "https://api.kale.mdholding.uz/media/" + product.getPhoto());
                    productMap.put("title", product.getTitle());
                    productMap.put("code", product.getCode());
                    productMap.put("quantity", orderProduct.getOrderProductId().getQuantity());
                    productMap.put("discount", orderProductDetails.getDiscount());
                }

                Map<String, Object> productDetailsMap = new HashMap<>();

                productDetailsMap.put("product", productMap);
                productDetailsMap.put("orderPrice", orderProductDetails.getOrderPrice());

                productsList.add(productDetailsMap);
            }

            orderMap.put("products", productsList);
            ordersResponseList.add(orderMap);
        }

        return ResponseEntity.ok(ordersResponseList);
    }

    @PostMapping("/prepare-order")
    public ResponseEntity<?> prepareOrder(@RequestParam Map<String, String> body) {

        String clickTransId = body.get("click_trans_id");
        String merchantTransId = body.get("merchant_trans_id");

        Map<String, String> response = new HashMap<>();

        response.put("click_trans_id", clickTransId);
        response.put("merchant_trans_id", merchantTransId);
        response.put("merchant_prepare_id", merchantTransId);
        response.put("error", "0");
        response.put("error_note", "Success");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete-order")
    public ResponseEntity<?> completeOrder(@RequestParam Map<String, String> body) {

        String clickTransId = body.get("click_trans_id");
        String merchantTransId = body.get("merchant_trans_id");
        String error = body.get("error");

        Map<String, Object> response = new HashMap<>();

        response.put("click_trans_id", clickTransId);
        response.put("merchant_trans_id", merchantTransId);
        Integer merchantConfirmId = error.equals("0") ? 1 : null;
        response.put("merchant_confirm_id", merchantConfirmId);

        Long orderId = Long.valueOf(body.get("merchant_trans_id"));

        Order_Orders order = order_ordersRepository.findById(orderId).orElseThrow();

        List<Order_Order_Products> order_order_productsList = order_order_productsRepository.findAllByOrderId(order);

        boolean itemEnded = false;

        for (Order_Order_Products order_order_products : order_order_productsList) {

            if (order_order_products.getOrderProductId().getProductId().getQuantity() == 0) {

                itemEnded = true;

                break;
            }
        }

        if (itemEnded) {

            response.put("error", "-1905");
            response.put("error_note", "Товар Закончился!");

            order.setStatus(4);
            order.setPaymentStatus("Отменен");
            order_ordersRepository.save(order);
        }
        else {

            response.put("error", "0");
            response.put("error_note", "Success");

            order.setStatus(0);
            order.setPaymentStatus("confirmed");
            order_ordersRepository.save(order);

            String comment = null;
            if (order.getComment() != null) {
                comment = "\nКомментарий: " + order.getComment();
            }

            StringBuilder orderMessage = new StringBuilder("Новый Заказ:\n\n" + order.getCode() + " " +
                    order_order_productsList.get(0).getOrderId().getUserId().getPhone() + " "
                    + order_order_productsList.get(0).getOrderId().getUserId().getName()
                    + "\nАдрес: " + order_order_productsList.get(0).getOrderId().getAddressId().getRegion() +
                    " " + order_order_productsList.get(0).getOrderId().getAddressId().getDistrict() +
                    " " + order_order_productsList.get(0).getOrderId().getAddressId().getStreet() +
                    "\nОплата Click."
                    + comment + "\n---------------------");

            for (Order_Order_Products order_order_products : order_order_productsList) {

                System.out.println(order_order_products.getId() + " " + order_order_products.getOrderProductId().getId());

                Order_OrderProducts order_orderProducts = order_orderProductRepository.findById(order_order_products.getOrderProductId().getId()).orElseThrow();

                System.out.println(order_orderProducts.getId() + ", Количество: " + order_orderProducts.getQuantity());

                orderMessage.append("\nИмя Товара: ").append(order_orderProducts.getProductId().getTitle())
                        .append("\nКод Товара: ").append(order_orderProducts.getProductId().getCode())
                        .append("\nКоличество: ").append(order_orderProducts.getQuantity())
                        .append("\nСумма: ").append(order_orderProducts.getOrderPrice())
                        .append("\n--------------");
            }

            SendMessage message = new SendMessage();

            message.setText(orderMessage.toString());
            message.setChatId(chatId);

            telegramBot.sendMessage(message);
        }

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
            orderProducts.setOrderPrice(product.getOrderPrice() * product.getQuantity());

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
        order.setStatus(1);
        order.setComment(body.getComment());
        order.setPaymentStatus("waiting");
        order.setPaymentType(body.getPaymentType());

        Address_Addresses address = address_addressesRepository.findById(body.getAddress()).orElseThrow();
        order.setAddressId(address);

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

        if (order.getPaymentType() == 2 || order.getPaymentType() == 1) {

            billingUrl.setBilling_url("https://my.click.uz/services/pay?service_id=28420&merchant_id=11369&return_url=https://kale.mdholding.uz/profile/purchases-history&amount=" + orderTotalSum + "&transaction_param=" + order.getId());
        }
        else if (order.getPaymentType() == 3) {

            StringBuilder orderMessage = new StringBuilder("Новый Заказ:\n\n" + order.getCode() + " " +
                    user.getPhone() + " " + user.getName() + "\nАдрес: " + order.getAddressId().getRegion() +
                    " " + order.getAddressId().getDistrict() +
                    " " + order.getAddressId().getStreet() + "\nОплата Наличными.");

            if (order.getComment() != null) {
                orderMessage.append("\nКомментарий: ").append(order.getComment());
            }

            orderMessage.append("\n---------------------");

            for (Order_OrderProducts orderProducts : order_orderProductsList) {

                orderMessage.append("\nИмя Товара: ").append(orderProducts.getProductId().getTitle())
                        .append("\nКод Товара: ").append(orderProducts.getProductId().getCode())
                        .append("\nКоличество: ").append(orderProducts.getQuantity())
                        .append("\nСумма: ").append(orderProducts.getOrderPrice())
                        .append("\n--------------");
            }

            SendMessage message = new SendMessage();

            message.setChatId(chatId);
            message.setText(orderMessage.toString());

            telegramBot.sendMessage(message);

            billingUrl.setBilling_url("https://kale.mdholding.uz/profile/purchases-history");
        }
        /*else if (order.getPaymentType() == 1) {

            String paymeUrl = "https://checkout.paycom.uz";
            String merchantId = "65e2f91cf4193eeca0afd4b0";
            long amount = (long) (orderTotalSum * 100); // тиины
            String orderId = order.getId().toString();
            String returnUrl = "https://kale.mdholding.uz/profile/purchases-history";

            String data = "m=" + merchantId + ";ac.order_id=" + orderId + ";a=" + amount + ";c=" + returnUrl;
            String encodedData = Base64.getEncoder().encodeToString(data.getBytes());

            String url = paymeUrl + "/" + encodedData;

            billingUrl.setBilling_url(url);
        }*/

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
        private Long address;
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