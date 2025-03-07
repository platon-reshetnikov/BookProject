package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.model.BookItem;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.BookItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface BookItemMapper {

    @Mapping(target = "book", source = "bookName", qualifiedByName = "mapBookNameToBook")
    BookItem toEntity(BookItemDTO bookItemDTO);

    @Mapping(target = "bookName", expression = "java(bookItem.getBook() != null ? bookItem.getBook().getName() : null)")
    BookItemDTO toDTO(BookItem bookItem);

    @Named("mapBookNameToBook")
    default Book mapBookNameToBook(String bookName) {
        if (bookName == null || bookName.trim().isEmpty()) {
            return null;
        }
        Book book = new Book();
        book.setName(bookName.trim());
        return book;
    }
}