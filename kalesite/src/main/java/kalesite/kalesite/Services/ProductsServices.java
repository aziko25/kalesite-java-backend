package kalesite.kalesite.Services;

import kalesite.kalesite.Models.Product_Products;
import kalesite.kalesite.Models.Product_Subcategories;
import kalesite.kalesite.Repositories.Product_ProductsRepository;
import kalesite.kalesite.Repositories.Product_SubcategoriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductsServices {

    private final Product_ProductsRepository productProductsRepository;
    private final Product_SubcategoriesRepository product_subcategoriesRepository;

    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    public void insertFetchedProducts() {

        long startTime = System.currentTimeMillis();

        String url = "http://94.158.52.249/Base/hs/info/stocks/";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString(("kaleapi" + ":" + "kaleapi").getBytes()))
                .build();

        try {

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};

            HashMap<String, Object> jsonResponse = mapper.readValue(response.body(), typeRef);
            List<Map<String, Object>> products = (List<Map<String, Object>>) jsonResponse.get("Товары");

            int count = 0;

            Long firstInsertedProductID = 0L;
            Long firstInsertedProductSubCategoryID = 0L;

            product_subcategoriesRepository.deleteAll();
            productProductsRepository.deleteAll();

            for (Map<String, Object> productData : products) {

                System.out.println(count);

                Product_Products product = new Product_Products();
                product.setIsTop(false);
                product.setStatus(1);
                product.setCreatedAt(LocalDateTime.now());

                if (productData.get("Наименование") != null) product.setTitle((String) productData.get("Наименование"));
                if (productData.get("Код") != null) product.setCode((String) productData.get("Код"));
                if (productData.get("ЕдиницаИзмерения") != null) product.setUnit((String) productData.get("ЕдиницаИзмерения"));
                if (productData.get("Размеры") != null) product.setSize((String) productData.get("Размеры"));
                if (productData.get("Описание") != null) product.setDescription((String) productData.get("Описание"));
                if (productData.get("Производитель") != null) product.setManufacturer((String) productData.get("Производитель"));
                if (productData.get("Брэнд") != null) product.setBrand((String) productData.get("Брэнд"));

                if (productData.get("Остаток") != null) {
                    Number quantityNumber = (Number) productData.get("Остаток");
                    product.setQuantity(quantityNumber.intValue());
                }

                if (productData.get("Цена") != null) {
                    Number priceNumber = (Number) productData.get("Цена");
                    product.setPrice(priceNumber.doubleValue());
                }

                if (productData.get("ЦенаСоСкидкой") != null) {
                    Number priceNumber = (Number) productData.get("ЦенаСоСкидкой");
                    product.setDiscountPrice(priceNumber.doubleValue());
                }

                // Handling the subcategory (Категория)
                if (productData.get("Категория") != null) {
                    String categoryTitle = (String) productData.get("Категория");
                    Product_Subcategories product_subcategories = product_subcategoriesRepository.findByTitle(categoryTitle);
                    if (product_subcategories == null) {
                        product_subcategories = new Product_Subcategories();
                        product_subcategories.setCreatedAt(LocalDateTime.now());
                        product_subcategories.setTitle(categoryTitle);
                        product_subcategoriesRepository.save(product_subcategories);

                        if (firstInsertedProductSubCategoryID == 0L) {

                            firstInsertedProductSubCategoryID = product_subcategories.getId();
                        }
                    }
                    product.setSubcategoryId(product_subcategories);
                }

                productProductsRepository.save(product);

                count++;

                if (firstInsertedProductID == 0L) {

                    firstInsertedProductID = product.getId();
                }
            }

            long endTime = System.currentTimeMillis();

            long timeSpent = (endTime - startTime) / 1000;

            System.out.println("You Successfully Inserted " + count + " Items!\nTime Spent: " + timeSpent + " Seconds!");
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public void updateFetchedProducts() {

        long startTime = System.currentTimeMillis();

        String url = "http://94.158.52.249/Base/hs/info/stocks/";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString(("kaleapi" + ":" + "kaleapi").getBytes()))
                .build();

        try {

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};

            HashMap<String, Object> jsonResponse = mapper.readValue(response.body(), typeRef);
            List<Map<String, Object>> products = (List<Map<String, Object>>) jsonResponse.get("Товары");

            int count = 0;

            for (Map<String, Object> productData : products) {

                System.out.println(count);

                if (productData.get("Код") != null) {

                    String productCode = (String) productData.get("Код");

                    Product_Products product = productProductsRepository.findByCode(productCode);

                    if (product != null) {

                        if (productData.get("Остаток") != null) {
                            Number quantityNumber = (Number) productData.get("Остаток");
                            product.setQuantity(quantityNumber.intValue());
                        }

                        if (productData.get("Цена") != null) {
                            Number priceNumber = (Number) productData.get("Цена");
                            product.setPrice(priceNumber.doubleValue());
                        }

                        if (productData.get("ЦенаСоСкидкой") != null) {
                            Number discountPriceNumber = (Number) productData.get("ЦенаСоСкидкой");
                            product.setDiscountPrice(discountPriceNumber.doubleValue());
                        }

                        productProductsRepository.save(product);
                        count++;
                    }
                }
            }

            long endTime = System.currentTimeMillis();

            long timeSpent = (endTime - startTime) / 1000;

            System.out.println("Successfully Updated " + count + " Items!\nTime Spent: " + timeSpent + " Seconds!");
        }
        catch (Exception e) {

            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}