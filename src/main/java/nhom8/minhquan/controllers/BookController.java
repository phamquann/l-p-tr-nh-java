package nhom8.minhquan.controllers;

import nhom8.minhquan.entities.Book;
import nhom8.minhquan.services.BookService;
import nhom8.minhquan.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    
    private final BookService bookService;
    private final CategoryService categoryService;

    @GetMapping
    @Transactional(readOnly = true)
    public String showAllBooks(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        model.addAttribute("totalBooks", bookService.countBooks());
        return "list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEdit", false);
        return "form";
    }

    @PostMapping("/add")
    public String addBook(@Valid @ModelAttribute("book") Book book, 
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "form";
        }
        
        bookService.saveBook(book);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm sách mới thành công!");
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Book book = bookService.getBookById(id)
                .orElse(null);
        
        if (book == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sách!");
            return "redirect:/admin/products";
        }
        
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEdit", true);
        return "form";
    }

    @PostMapping("/edit/{id}")
    public String updateBook(@PathVariable("id") Long id, 
                           @Valid @ModelAttribute("book") Book book,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isEdit", true);
            return "form";
        }
        
        try {
            bookService.updateBook(id, book);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sách thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/admin/products";
    }

    @GetMapping("/detail/{id}")
    @Transactional(readOnly = true)
    public String showBookDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Book book = bookService.getBookById(id).orElse(null);
        
        if (book == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sách!");
            return "redirect:/books";
        }
        
        model.addAttribute("book", book);
        return "detail";
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sách thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
}