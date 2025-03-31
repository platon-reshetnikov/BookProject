package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private MessageSource messageSource;

    private BookDTO bookDTO;

    @BeforeEach
    void setUp() {
        bookDTO = new BookDTO();
        bookDTO.setName("Test Book");
        bookDTO.setGenre("Fiction");
        bookDTO.setAgeGroup(AgeGroup.ADULT);
        bookDTO.setPrice(BigDecimal.valueOf(19.99));
        bookDTO.setPublicationDate(LocalDate.now());
        bookDTO.setAuthor("Test Author");
        bookDTO.setPages(300);
        bookDTO.setCharacteristics("Hardcover");
        bookDTO.setDescription("A test book description");
        bookDTO.setLanguage(Language.ENGLISH);

        when(messageSource.getMessage(eq("book.added"), any(), any(Locale.class)))
                .thenReturn("Book Test Book has been added successfully");
        when(messageSource.getMessage(eq("book.updated"), any(), any(Locale.class)))
                .thenReturn("Book Test Book has been updated successfully");
        when(messageSource.getMessage(eq("book.deleted"), any(), any(Locale.class)))
                .thenReturn("Book Test Book has been deleted successfully");
        when(messageSource.getMessage(eq("book.not.found"), any(), any(Locale.class)))
                .thenReturn("Book not found with name: Test Book");
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void getAllBooks_ReturnsBooksViewWithBookList() throws Exception {
        // Подготовка данных
        List<BookDTO> books = Collections.singletonList(bookDTO);
        Page<BookDTO> bookPage = new PageImpl<>(books, PageRequest.of(0, 10), books.size());

        // Настройка поведения сервиса
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        when(bookService.getAllBooks(pageable, null)).thenReturn(bookPage);

        // Выполнение запроса и проверка
        mockMvc.perform(MockMvcRequestBuilders.get("/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("books"))
                .andExpect(MockMvcResultMatchers.model().attribute("books", books))
                .andExpect(MockMvcResultMatchers.model().attribute("currentPage", 0))
                .andExpect(MockMvcResultMatchers.model().attribute("totalPages", 1))
                .andExpect(MockMvcResultMatchers.model().attribute("totalItems", 1L))
                .andExpect(MockMvcResultMatchers.model().attribute("pageSize", 10))
                .andExpect(MockMvcResultMatchers.model().attribute("sortField", "name"))
                .andExpect(MockMvcResultMatchers.model().attribute("sortDirection", "asc"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void getBookByName_ReturnsBookDetailsView() throws Exception {
        when(bookService.getBookByName("Test Book")).thenReturn(bookDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/books/Test Book"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("book-details"))
                .andExpect(MockMvcResultMatchers.model().attribute("book", bookDTO));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void getBookByName_ThrowsNotFoundException_Returns404() throws Exception {
        when(bookService.getBookByName("Unknown Book")).thenThrow(new NotFoundException("Book not found with name: Unknown Book"));

        mockMvc.perform(MockMvcRequestBuilders.get("/books/Unknown Book"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"EMPLOYEE"})
    void showAddBookForm_ReturnsBookFormView() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/books/add"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("book-form"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("book"))
                .andExpect(MockMvcResultMatchers.model().attribute("ageGroups", AgeGroup.values()))
                .andExpect(MockMvcResultMatchers.model().attribute("languages", Language.values()));
    }

    @Test
    @WithMockUser(authorities = {"EMPLOYEE"})
    void addBook_Success_RedirectsToBooks() throws Exception {
        when(bookService.addBook(bookDTO)).thenReturn(bookDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/books/add")
                        .flashAttr("book", bookDTO)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/books"))
                .andExpect(MockMvcResultMatchers.flash().attribute("successMessage", "Book Test Book has been added successfully"));

        verify(bookService, times(1)).addBook(bookDTO);
    }

    @Test
    @WithMockUser(authorities = {"EMPLOYEE"})
    void addBook_ValidationErrors_ReturnsBookForm() throws Exception {
        BookDTO invalidBook = new BookDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(invalidBook, "book");
        bindingResult.rejectValue("name", "NotNull", "Name is required");

        mockMvc.perform(MockMvcRequestBuilders.post("/books/add")
                        .flashAttr("book", invalidBook)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("book-form"));

        verify(bookService, never()).addBook(any());
    }

    @Test
    @WithMockUser(authorities = {"EMPLOYEE"})
    void showEditBookForm_ReturnsBookFormView() throws Exception {
        when(bookService.getBookByName("Test Book")).thenReturn(bookDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/books/edit/Test Book"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("book-form"))
                .andExpect(MockMvcResultMatchers.model().attribute("book", bookDTO))
                .andExpect(MockMvcResultMatchers.model().attribute("ageGroups", AgeGroup.values()))
                .andExpect(MockMvcResultMatchers.model().attribute("languages", Language.values()));
    }

    @Test
    @WithMockUser(authorities = {"EMPLOYEE"})
    void showEditBookForm_ThrowsNotFoundException_Returns404() throws Exception {
        when(bookService.getBookByName("Unknown Book")).thenThrow(new NotFoundException("Book not found with name: Unknown Book"));

        mockMvc.perform(MockMvcRequestBuilders.get("/books/edit/Unknown Book"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"EMPLOYEE"})
    void updateBook_Success_RedirectsToBooks() throws Exception {
        when(bookService.updateBookByName(eq("Test Book"), eq(bookDTO))).thenReturn(bookDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/books/update")
                        .flashAttr("book", bookDTO)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/books"))
                .andExpect(MockMvcResultMatchers.flash().attribute("successMessage", "Book Test Book has been updated successfully"));

        verify(bookService, times(1)).updateBookByName(eq("Test Book"), eq(bookDTO));
    }

    @Test
    @WithMockUser(authorities = {"EMPLOYEE"})
    void updateBook_ValidationErrors_ReturnsBookForm() throws Exception {
        BookDTO invalidBook = new BookDTO();
        BindingResult bindingResult = new BeanPropertyBindingResult(invalidBook, "book");
        bindingResult.rejectValue("name", "NotNull", "Name is required");

        mockMvc.perform(MockMvcRequestBuilders.post("/books/update")
                        .flashAttr("book", invalidBook)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("book-form"));

        verify(bookService, never()).updateBookByName(any(), any());
    }

    @Test
    @WithMockUser(authorities = {"EMPLOYEE"})
    void deleteBook_Success_RedirectsToBooks() throws Exception {
        doNothing().when(bookService).deleteBookByName("Test Book");

        mockMvc.perform(MockMvcRequestBuilders.post("/books/delete/Test Book")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/books"))
                .andExpect(MockMvcResultMatchers.flash().attribute("successMessage", "Book Test Book has been deleted successfully"));

        verify(bookService, times(1)).deleteBookByName("Test Book");
    }

    @Test
    @WithMockUser(authorities = {"EMPLOYEE"})
    void deleteBook_NotFound_RedirectsToBooksWithError() throws Exception {
        doThrow(new NotFoundException("Book not found with name: Test Book")).when(bookService).deleteBookByName("Test Book");

        mockMvc.perform(MockMvcRequestBuilders.post("/books/delete/Test Book")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/books"))
                .andExpect(MockMvcResultMatchers.flash().attribute("errorMessage", "Book not found with name: Test Book"));

        verify(bookService, times(1)).deleteBookByName("Test Book");
    }
}
