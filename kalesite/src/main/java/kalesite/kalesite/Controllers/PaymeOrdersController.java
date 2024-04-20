package kalesite.kalesite.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import kalesite.kalesite.Models.Payme.Entities.OrderCancelReason;
import kalesite.kalesite.Services.Payme.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payme")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class PaymeOrdersController {

    private final MerchantService merchantService;

    @PostMapping("/transactions")
    public ResponseEntity<?> handleTransaction(@RequestBody JsonNode jsonRequest) {

        try {

            String method = jsonRequest.get("method").asText();
            JsonNode params = jsonRequest.get("params");
            JsonNode account = params.get("account");

            if (account == null || account.isEmpty()) {

                Map<String, String> map = new HashMap<>();
                map.put("code", "-32504");
                map.put("message", "HE-HE-HE-HA");

                Map<String, Object> result = new HashMap<>();

                result.put("error", map);

                return ResponseEntity.ok(result);
            }

            String id = null;

            switch (method) {

                case "CheckPerformTransaction":

                    int amount = params.get("amount").intValue();
                    JsonNode idNode = params.get("id");

                    if (idNode != null) {
                        id = idNode.asText();
                    }

                    return ResponseEntity.ok(merchantService.checkPerformTransaction(amount, id));

                case "CreateTransaction":

                    id = params.get("id").asText();
                    long time = params.get("time").longValue();
                    amount = params.get("amount").intValue();
                    Date transactionDate = new Date(time);

                    return ResponseEntity.ok(merchantService.createTransaction(id, transactionDate, amount));

                case "CheckTransaction":

                    id = params.get("id").asText();

                    return ResponseEntity.ok(merchantService.checkTransaction(id));

                case "PerformTransaction":

                    id = params.get("id").asText();

                    return ResponseEntity.ok(merchantService.performTransaction(id));

                case "CancelTransaction":

                    id = params.get("id").asText();
                    int reasonCode = params.get("reason").intValue();

                    OrderCancelReason reason = OrderCancelReason.fromCode(reasonCode);

                    return ResponseEntity.ok(merchantService.cancelTransaction(id, reason));

                case "GetStatement":

                    long from = params.get("from").longValue();
                    long to = params.get("to").longValue();

                    Date fromDate = new Date(from);
                    Date toDate = new Date(to);

                    return ResponseEntity.ok(merchantService.getStatement(fromDate, toDate));

                default:
                    return ResponseEntity.badRequest().body("Unsupported method");
            }
        }
        catch (Exception e) {

            return ResponseEntity.internalServerError().body("Error processing request: " + e.getMessage());
        }
    }
}