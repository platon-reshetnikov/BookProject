package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookPriceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BookPriceServiceImpl implements BookPriceService {
    private static final Logger logger = LoggerFactory.getLogger(BookPriceServiceImpl.class);
    private final BookRepository bookRepository;

    @Override
    public BigDecimal getBookPrice(String bookName) {
        if (bookName == null || bookName.trim().isEmpty()) {
            logger.warn("Book name is null or empty");
            return BigDecimal.ZERO;
        }

        String normalizedBookName = bookName.trim();
        return bookRepository.findByName(normalizedBookName)
                .map(Book::getPrice)
                .orElseGet(() -> {
                    logger.error("Book not found with name: {}", normalizedBookName);
                    return BigDecimal.ZERO;
                });
    }
}
