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

    private Date createTime;
    private Date performTime;
    private Date cancelTime;
    private Long transaction;
    private Integer state;
    private Integer reason;
}