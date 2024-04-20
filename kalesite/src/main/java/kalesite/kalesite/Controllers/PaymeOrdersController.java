package kalesite.kalesite.Controllers;

import kalesite.kalesite.Exceptions.*;
import kalesite.kalesite.Models.Payme.Entities.Account;
import kalesite.kalesite.Models.Payme.Entities.OrderCancelReason;
import kalesite.kalesite.Services.Payme.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/payme")
@RequiredArgsConstructor
@CrossOrigin(maxAge = 3600)
public class PaymeOrdersController {

    private final MerchantService merchantService;

    @PostMapping("/checkPerformTransaction")
    public ResponseEntity<?> checkPerformTransaction(@RequestParam int amount, @RequestBody Account account) throws OrderNotExistsException, WrongAmountException {

        return ResponseEntity.ok(merchantService.checkPerformTransaction(amount, account));
    }

    @PostMapping("/createTransaction")
    public ResponseEntity<?> createTransaction(@RequestParam String id, @RequestParam Date time,
                                                     @RequestParam int amount, @RequestBody Account account) throws OrderNotExistsException, WrongAmountException, UnableCompleteException {

        return ResponseEntity.ok(merchantService.createTransaction(id, time, amount, account));
    }

    @PostMapping("/performTransaction")
    public ResponseEntity<?> performTransaction(@RequestParam String id) throws TransactionNotFoundException, UnableCompleteException {

        return ResponseEntity.ok(merchantService.performTransaction(id));
    }

    @PostMapping("/cancelTransaction")
    public ResponseEntity<?> cancelTransaction(@RequestParam String id, @RequestBody OrderCancelReason reason) throws UnableCancelTransactionException, TransactionNotFoundException {

        return ResponseEntity.ok(merchantService.cancelTransaction(id, reason));
    }

    @GetMapping("/checkTransaction")
    public ResponseEntity<?> checkTransaction(@RequestParam String id) throws TransactionNotFoundException {

        return ResponseEntity.ok(merchantService.checkTransaction(id));
    }

    @GetMapping("/getStatement")
    public ResponseEntity<?> getStatement(@RequestParam Date from, @RequestParam Date to) {

        return ResponseEntity.ok(merchantService.getStatement(from, to));
    }
}