package kalesite.kalesite.Services;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kalesite.kalesite.Models.Products.Product_Categories;
import kalesite.kalesite.Repositories.Products.Product_CategoriesRepository;
import kalesite.kalesite.Telegram.MainTelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductsServices {

    private final MainTelegramBot mainTelegramBot;
    private final JdbcTemplate jdbcTemplate;
    private final Product_CategoriesRepository product_categoriesRepository;

    @Transactional
    @SuppressWarnings("unchecked")
    @Scheduled(fixedDelay = 180000, initialDelay = 1000)
    public void insertFetchedProductsJdbcTemplateOptimized() {

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

            Map<String, Integer> codeCounts = jdbcTemplate.query(
                    "SELECT code, COUNT(*) as count FROM Product_Product GROUP BY code",
                    (ResultSetExtractor<Map<String, Integer>>) rs -> {
                        HashMap<String, Integer> mapRet = new HashMap<>();
                        while (rs.next()) {
                            mapRet.put(rs.getString("code"), rs.getInt("count"));
                        }
                        return mapRet;
                    });

            List<Map<String, Object>> newProducts = new ArrayList<>();
            List<Map<String, Object>> updateProducts = new ArrayList<>();
            Set<String> duplicateCodes = new HashSet<>(); // Track duplicates found during processing

            Integer count = 0;
            for (Map<String, Object> product : products) {
                System.out.println(count);
                count++;
                String code = (String) product.get("Код");

                // Check against the database counts to find pre-existing duplicates
                assert codeCounts != null;
                if (codeCounts.containsKey(code) && codeCounts.get(code) > 1) {
                    duplicateCodes.add(code); // This code has duplicates in the database
                } else {
                    // No duplicates in the database, proceed as before
                    if (codeCounts.containsKey(code)) {
                        updateProducts.add(product);
                    } else {
                        newProducts.add(product);
                    }
                }
            }

            if (!duplicateCodes.isEmpty()) {

                SendMessage message = new SendMessage();

                message.setChatId(chatId);
                message.setText("Duplicate product codes found in the database: " + duplicateCodes);

                mainTelegramBot.sendMessage(message);
            }

            batchInsertNewProducts(newProducts);
            batchUpdateExistingProducts(updateProducts);

            long endTime = System.currentTimeMillis();
            long timeSpent = (endTime - startTime) / 1000;

            if (lastMessageIdMap.containsKey(chatId)) {

                Integer lastMessageId = lastMessageIdMap.get(chatId);

                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(chatId);
                deleteMessage.setMessageId(lastMessageId);

                try {
                    mainTelegramBot.execute(deleteMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Operation Products Update completed in " + timeSpent + " seconds!");

            try {
                Integer messageId = mainTelegramBot.execute(message).getMessageId();
                lastMessageIdMap.put(chatId, messageId);
            }
            catch (TelegramApiException e) {
                e.printStackTrace();
            }

            System.out.println("Operation completed in " + timeSpent + " seconds!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    Map<String, Integer> lastMessageIdMap = new HashMap<>();

    private final String chatId = "-1002048013161";

    private void batchInsertNewProducts(List<Map<String, Object>> newProducts) {

        String sql = """
            INSERT INTO Product_Product ("isTop", status, created_at, title, title_ru, code, 
                                        unit, size, description, description_ru, manufacturer, manufacturer_ru, brand, 
                                        brand_ru, quantity, price, "discountPrice", subcategory_id, guid, discount) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";

        this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, Object> product = newProducts.get(i);
                ps.setBoolean(1, false); // is_top
                ps.setInt(2, 1); // status
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now())); // created_at
                ps.setString(4, (String) product.get("Наименование")); // title
                ps.setString(5, (String) product.get("Наименование")); // title_ru
                ps.setString(6, (String) product.get("Код")); // code
                ps.setString(7, (String) product.get("ЕдиницаИзмерения")); // unit
                ps.setString(8, (String) product.get("Размеры")); // size
                ps.setString(9, (String) product.get("Описание")); // description
                ps.setString(10, (String) product.get("Описание")); // description_ru
                ps.setString(11, (String) product.get("Производитель")); // manufacturer
                ps.setString(12, (String) product.get("Производитель")); // manufacturer_ru
                ps.setString(13, (String) product.get("Брэнд")); // brand
                ps.setString(14, (String) product.get("Брэнд")); // brand_ru
                ps.setInt(15, ((Number) product.get("Остаток")).intValue()); // quantity
                ps.setDouble(16, ((Number) product.get("Цена")).doubleValue()); // price
                ps.setDouble(17, ((Number) product.get("ЦенаСоСкидкой")).doubleValue()); // discount_price
                ps.setLong(18, getOrInsertSubcategoryId((String) product.get("Категория"))); // subcategory_id
                ps.setObject(19, UUID.randomUUID());

                double price = ((Number) product.get("Цена")).doubleValue(); // price
                double discountPrice = ((Number) product.get("ЦенаСоСкидкой")).doubleValue(); // discount_price

                double salePercent = ((price - discountPrice) / price) * 100;

                ps.setDouble(20, salePercent);
            }

            @Override
            public int getBatchSize() {
                return newProducts.size();
            }
        });
    }


    private void batchUpdateExistingProducts(List<Map<String, Object>> updateProducts) {

        String sql = "UPDATE Product_Product SET quantity = ?, price = ?, \"discountPrice\" = ? WHERE code = ?";

        this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {

                Map<String, Object> product = updateProducts.get(i);
                // Assume "Остаток" and "Цена" fields exist
                ps.setInt(1, ((Number) product.get("Остаток")).intValue());
                ps.setDouble(2, ((Number) product.get("Цена")).doubleValue());
                ps.setDouble(3, ((Number) product.get("ЦенаСоСкидкой")).doubleValue());
                ps.setString(4, (String) product.get("Код"));
            }

            @Override
            public int getBatchSize() {
                return updateProducts.size();
            }
        });
    }


    private Long getOrInsertSubcategoryId(String categoryTitle) {

        if (categoryTitle == null || categoryTitle.trim().isEmpty()) {
            categoryTitle = "Без Имени"; // Default category name if none provided
        }

        try {

            return jdbcTemplate.queryForObject(
                    "SELECT id FROM Product_Subcategory WHERE title = ? OR title_ru = ?",
                    Long.class, categoryTitle, categoryTitle);
        }
        catch (EmptyResultDataAccessException e) {

            Product_Categories product_categories = product_categoriesRepository.findTopByOrderByIdAsc();

            KeyHolder keyHolder = new GeneratedKeyHolder();
            String finalCategoryTitle = categoryTitle;
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO Product_Subcategory (title, title_ru, created_at, guid, category_id) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, finalCategoryTitle);
                ps.setString(2, finalCategoryTitle);
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.setObject(4, UUID.randomUUID());
                ps.setLong(5, product_categories.getId());
                return ps;
            }, keyHolder);

            if (keyHolder.getKeys() != null && keyHolder.getKeys().size() > 0) {
                return (Long) keyHolder.getKeys().get("id");
            } else {
                throw new IllegalStateException("Failed to insert subcategory, no ID returned.");
            }
        } catch (DataAccessException dae) {
            throw new RuntimeException("Error accessing data for category: " + categoryTitle, dae);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void updateAllPhotos() {

        long startTime = System.currentTimeMillis();

        List<String> productCodes = fetchProductCodes();
        System.out.println(productCodes.size());
        HttpClient client = HttpClient.newHttpClient();
        String urlTemplate = "http://94.158.52.249/Base/hs/info/foto?code=%s";
        String authorizationHeader = Base64.getEncoder().encodeToString("kaleapi:kaleapi".getBytes());

        int count = 0;
        for (String code : productCodes) {
            try {
                String encodedCode = URLEncoder.encode(code, StandardCharsets.UTF_8.toString());

                // Then, replace each + with %20
                encodedCode = encodedCode.replaceAll("\\+", "%20");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(String.format(urlTemplate, encodedCode)))
                        .header("Authorization", "Basic " + authorizationHeader)
                        .build();

                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                System.out.println(count + " " + code);
                count++;

                // Assuming response body is JSON with a "Фото" field for Base64 image.

                String base64Image = extractBase64ImageFromResponse(response.body());

                if (base64Image != null && !base64Image.isEmpty()) {
                    String sanitizedBase64Image = base64Image.replaceAll("\\s", "");
                    byte[] imageBytes = Base64.getDecoder().decode(sanitizedBase64Image);
                    String sanitizedCode = code.replaceAll("[/\\\\]", "_");
                    String filePath = "/var/www/kale.abba.uz/media/productImage/" + sanitizedCode + "_photo.png";
                    saveImage(imageBytes, filePath);
                    updatePhotoColumnInDatabase(code, "productImage/" + sanitizedCode + "_photo.png");
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                // Handle exceptions appropriately.
            }
        }

        long endTime = System.currentTimeMillis();
        long timeSpent = (endTime - startTime) / 1000;

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Operation Photo Update completed in " + timeSpent + " seconds!");
        mainTelegramBot.sendMessage(message);

        System.out.println("Operation completed in " + timeSpent + " seconds!");
    }

    private List<String> fetchProductCodes() {
        String sql = "SELECT code FROM Product_Product where photo IS NULL;";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("code"));
    }

    private void updatePhotoColumnInDatabase(String code, String photoPath) {
        String sql = "UPDATE Product_Product SET photo = ? WHERE code = ?";
        jdbcTemplate.update(sql, photoPath, code);
    }

    private void saveImage(byte[] imageBytes, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
            fos.write(imageBytes);
        }
    }

    private String extractBase64ImageFromResponse(InputStream responseBodyStream) {
        JsonFactory factory = new JsonFactory();
        JsonParser parser = null;
        String base64Image = "";

        try {
            parser = factory.createParser(responseBodyStream);
            // Iterate over the JSON tokens
            while (!parser.isClosed()) {
                JsonToken jsonToken = parser.nextToken();

                // If it's the end of the file, break
                if (jsonToken == null) {
                    break;
                }

                // Check if the current token is a field name
                if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                    String fieldName = parser.getCurrentName();
                    // Move to the next token which is the value of the field name
                    jsonToken = parser.nextToken();

                    // Check if the field name matches "Фото"
                    if ("Фото".equals(fieldName)) {
                        base64Image = parser.getValueAsString();
                        break; // Break the loop once we find the image
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Consider appropriate error handling here
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // Consider appropriate error handling here
                }
            }
        }

        return base64Image;
    }
}