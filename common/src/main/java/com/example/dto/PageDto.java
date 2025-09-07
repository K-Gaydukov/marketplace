package com.example.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageDto<T> {
    private List<T> content;
    private PageableDto pageable;
    private boolean last;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number;
    private boolean first;
    private int numberOfElements;
    private boolean empty;

    public PageDto() {
    }

    public PageDto(Page<T> page) {
        this.content = page.getContent();
        this.pageable = new PageableDto(
                page.getPageable().getPageNumber(),
                page.getPageable().getPageSize(),
                new SortDto(
                        page.getPageable().getSort().isUnsorted(),
                        page.getPageable().getSort().isSorted(),
                        page.getPageable().getSort().isEmpty()
                ),
                page.getPageable().getOffset(),
                page.getPageable().isPaged(),
                page.getPageable().isUnpaged()
        );
        this.last = page.isLast();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.size = page.getSize();
        this.number = page.getNumber();
        this.first = page.isFirst();
        this.numberOfElements = page.getNumberOfElements();
        this.empty = page.isEmpty();
    }

    @JsonCreator
    public PageDto(
            @JsonProperty("content") List<T> content,
            @JsonProperty("pageable") PageableDto pageable,
            @JsonProperty("last") boolean last,
            @JsonProperty("totalPages") int totalPages,
            @JsonProperty("totalElements") long totalElements,
            @JsonProperty("size") int size,
            @JsonProperty("number") int number,
            @JsonProperty("first") boolean first,
            @JsonProperty("numberOfElements") int numberOfElements,
            @JsonProperty("empty") boolean empty
    ) {
        this.content = content;
        this.pageable = pageable;
        this.last = last;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.size = size;
        this.number = number;
        this.first = first;
        this.numberOfElements = numberOfElements;
        this.empty = empty;
    }
}