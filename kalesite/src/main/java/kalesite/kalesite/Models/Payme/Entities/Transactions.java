package kalesite.kalesite.Models.Payme.Entities;

import kalesite.kalesite.Models.Payme.Result.GetStatementResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transactions {

    private List<GetStatementResult> transactions;
}