package kalesite.kalesite.Models.Payme.Result;

import kalesite.kalesite.Models.Payme.Entities.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetStatementResult {

    private String id;
    private Date time;
    private Integer amount;
    private Account account;
    private Long create_time;
    private Long perform_time;
    private Long cancel_time;
    private String transaction;
    private Integer state;
    private Integer reason;
}