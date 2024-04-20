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

    private long createTime;
    private Long transaction;
    private Integer state;

    public void setCreateTime(Date createTime) {
        this.createTime = createTime.getTime();
    }
}