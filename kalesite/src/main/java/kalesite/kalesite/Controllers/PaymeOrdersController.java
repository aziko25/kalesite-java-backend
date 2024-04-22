package kalesite.kalesite.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import kalesite.kalesite.Exceptions.*;
import kalesite.kalesite.Models.Payme.Entities.Account;
import kalesite.kalesite.Models.Payme.Entities.OrderCancelReason;
import kalesite.kalesite.Services.Payme.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
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
    public ResponseEntity<?> handleTransaction(@RequestHeader(required = false) HttpHeaders headers,
                                               @RequestBody JsonNode jsonRequest) throws OrderNotExistsException, WrongAmountException, UnableCompleteException, TransactionNotFoundException, UnableCancelTransactionException {

        String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Basic ")) {

            String base64Credentials = authorization.substring("Basic ".length()).trim();
            String credentials;

            try {

                credentials = new String(Base64.getDecoder().decode(base64Credentials));
            }
            catch (IllegalArgumentException e) {

                throw new UnableCompleteException("Corrupted headers", -32504, "authorization");
            }

            String[] values = credentials.split(":");

            if (values.length == 2) {

                String username = values[0];
                String password = values[1];
            }
            else {

                throw new UnableCompleteException("Corrupted headers", -32504, "authorization");
            }
        }


        String method = jsonRequest.get("method").asText();
        JsonNode params = jsonRequest.get("params");
        JsonNode accountJson = params.get("account");
        Account account;

        String id;

        switch (method) {

            case "CheckPerformTransaction":

                int amount = params.get("amount").intValue();

                account = new Account(Long.parseLong(accountJson.get("KaleUz").asText()));

                return ResponseEntity.ok(merchantService.checkPerformTransaction(amount, account));

            case "CreateTransaction":

                id = params.get("id").asText();
                long time = params.get("time").longValue();
                amount = params.get("amount").intValue();
                Date transactionDate = new Date(time);

                account = new Account(Long.parseLong(accountJson.get("KaleUz").asText()));

                return ResponseEntity.ok(merchantService.createTransaction(id, transactionDate, amount, account));

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
}