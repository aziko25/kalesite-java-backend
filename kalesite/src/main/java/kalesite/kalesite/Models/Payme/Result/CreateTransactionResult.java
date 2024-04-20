package kalesite.kalesite.Models.Payme.Result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionResult {

    private long create_time;
    private String transaction;
    private Integer state;
}