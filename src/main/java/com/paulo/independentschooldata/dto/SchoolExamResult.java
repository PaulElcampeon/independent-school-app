package com.paulo.independentschooldata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SchoolExamResult {
    @JsonProperty("school")
    private String school;
    private String reason;
    private String gcseResults;
    private String aLevelResults;
    private String email;
    private String name;

}
