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
public class PerformTransactionResult {

    private Long transaction;
    private Long performTime;
    private Integer state;
}