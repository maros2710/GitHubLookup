package com.ciklum.ghl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Error {

    private int status;

    @JsonProperty("Message")
    private String message;

}
