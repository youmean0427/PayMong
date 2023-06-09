package com.paymong.management.mong.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvolutionMongResDto {

    private Integer weight;

    private String mongCode;
    private String stateCode;
}
