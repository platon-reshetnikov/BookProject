package com.epam.rd.autocode.spring.project.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public interface BookPriceService {
    BigDecimal getBookPrice(String bookName);
}
