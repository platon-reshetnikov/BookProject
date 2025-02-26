package com.epam.rd.autocode.spring.project.MapStruct;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookMapper {
    @Mapping(source = "language", target = "language")
    BookDTO toDTO(Book book);
    @Mapping(source = "language", target = "language")
    Book toEntity(BookDTO bookDTO);
    @Mapping(source = "language", target = "language")
    void updateEntityFromDTO(BookDTO bookDTO, @MappingTarget Book book);
}
