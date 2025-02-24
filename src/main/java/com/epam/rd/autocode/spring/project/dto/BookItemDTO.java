package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.Book;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookItemDTO {
    private String bookName;
    private Integer quantity;
}
