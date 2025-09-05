package com.example.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SortDto {
    private boolean unsorted;
    private boolean sorted;
    private boolean empty;

    public SortDto() {
    }

    @JsonCreator
    public SortDto(
            @JsonProperty("unsorted") boolean unsorted,
            @JsonProperty("sorted") boolean sorted,
            @JsonProperty("empty") boolean empty
    ) {
        this.unsorted = unsorted;
        this.sorted = sorted;
        this.empty = empty;
    }
}
