package com.example.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PageableDto {
    private int pageNumber;
    private int pageSize;
    private SortDto sort;
    private long offset;
    private boolean paged;
    private boolean unpaged;

    public PageableDto() {
    }

    @JsonCreator
    public PageableDto(
            @JsonProperty("pageNumber") int pageNumber,
            @JsonProperty("pageSize") int pageSize,
            @JsonProperty("sort") SortDto sort,
            @JsonProperty("offset") long offset,
            @JsonProperty("paged") boolean paged,
            @JsonProperty("unpaged") boolean unpaged
    ) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sort = sort;
        this.offset = offset;
        this.paged = paged;
        this.unpaged = unpaged;
    }
}
