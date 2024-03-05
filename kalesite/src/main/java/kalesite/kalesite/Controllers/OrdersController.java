package kalesite.kalesite.Controllers;

import jakarta.persistence.EntityNotFoundException;
import kalesite.kalesite.Models.Address_Addresses;
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
import kalesite.kalesite.Telegram.MainTelegramBot;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;
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

    @Value("${chat_id}")
    private String chatId;

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
            order.setPaymentStatus("Оплачено");
            order_ordersRepository.save(order);

            String comment = null;
            if (order.getComment() != null) {
                comment = "\n" + order.getComment();
            }

            StringBuilder orderMessage = new StringBuilder("Новый Заказ:\n\n" + order.getCode() + " " +
                    order_order_productsList.get(0).getOrderId().getUserId().getPhone() + " "
                    + order_order_productsList.get(0).getOrderId().getUserId().getName()
                    + "\nАдрес: " + order_order_productsList.get(0).getOrderId().getAddressId().getRegion() + " " + order_order_productsList.get(0).getOrderId().getAddressId().getDistrict() + " " + order_order_productsList.get(0).getOrderId().getAddressId().getStreet()
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

        Address_Addresses address = new Address_Addresses();
        address.setId(body.getAddress());
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

        if (order.getPaymentType() == 2) {

            billingUrl.setBilling_url("https://my.click.uz/services/pay?service_id=28420&merchant_id=11369&return_url=https://kale.mdholding.uz/profile/purchases-history&amount=" + orderTotalSum + "&transaction_param=" + order.getId());
        }
        else if (order.getPaymentType() == 1) {

            String paymeUrl = "https://checkout.paycom.uz";
            String merchantId = "65e2f91cf4193eeca0afd4b0";
            long amount = (long) (orderTotalSum * 100); // тиины
            String orderId = order.getId().toString();
            String returnUrl = "https://kale.mdholding.uz/profile/purchases-history";

            String data = "m=" + merchantId + ";ac.order_id=" + orderId + ";a=" + amount + ";c=" + returnUrl;
            String encodedData = Base64.getEncoder().encodeToString(data.getBytes());

            String url = paymeUrl + "/" + encodedData;

            billingUrl.setBilling_url(url);
        }

        return ResponseEntity.ok(billingUrl);
    }

    @PostMapping("/payme/prepare")
    public ResponseEntity<?> paymePrepare(Map<String, String> map) {

        System.out.println("Incoming Payme Request");

        return ResponseEntity.ok("");
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