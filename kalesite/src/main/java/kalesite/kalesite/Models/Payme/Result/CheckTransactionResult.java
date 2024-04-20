package kalesite.kalesite.Models.Payme.Result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckTransactionResult {

    private Long create_time;
    private Date perform_time;
    private Date cancel_time;
    private String transaction;
    private Integer state;
    private Integer reason;
}