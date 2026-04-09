package ru.lombard.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.lombard.entity.Product;
import ru.lombard.entity.User;
import ru.lombard.entity.Store;
import ru.lombard.repository.StoreRepository;
import ru.lombard.service.CategoryService;
import ru.lombard.service.CurrentUserService;
import ru.lombard.service.ProductService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final StoreRepository storeRepository;
    private final CurrentUserService currentUserService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Product> products = productService.findAllForAdmin(page, 20);
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findAllRoot());
        return "admin/products";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAllRootEntities());
        model.addAttribute("stores", storeRepository.findByActiveTrueOrderBySortOrderAscIdAsc());
        return "admin/product-form";
    }

    @PostMapping("/new")
    public String create(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Long categoryId,
            @RequestParam Long storeId,
            @RequestParam String condition,
            @RequestParam java.math.BigDecimal price,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) MultipartFile[] images,
            RedirectAttributes ra
    ) {
        User user = currentUserService.getCurrentUser().orElseThrow();
        var category = categoryService.findById(categoryId).orElseThrow();
        Store store = storeRepository.findById(storeId).filter(Store::isActive).orElseThrow();
        Product product = Product.builder()
                .name(name)
                .description(description)
                .category(category)
                .store(store)
                .condition(Product.Condition.valueOf(condition.toUpperCase()))
                .price(price)
                .quantity(quantity)
                .year(year)
                .status(Product.ProductStatus.DRAFT)
                .build();
        try {
            productService.create(product, user, images != null ? List.of(images) : new ArrayList<>());
            ra.addFlashAttribute("message", "Товар создан.");
            return "redirect:/admin/products";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/products/new";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id).orElseThrow();
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAllRootEntities());
        model.addAttribute("stores", storeRepository.findByActiveTrueOrderBySortOrderAscIdAsc());
        return "admin/product-form";
    }

    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Long categoryId,
            @RequestParam Long storeId,
            @RequestParam String condition,
            @RequestParam java.math.BigDecimal price,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) Integer year,
            @RequestParam String status,
            @RequestParam(required = false) MultipartFile[] images,
            RedirectAttributes ra
    ) {
        Product product = productService.findById(id).orElseThrow();
        var category = categoryService.findById(categoryId).orElseThrow();
        Store store = storeRepository.findById(storeId).filter(Store::isActive).orElseThrow();
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setStore(store);
        product.setCondition(Product.Condition.valueOf(condition.toUpperCase()));
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setYear(year);
        product.setStatus(Product.ProductStatus.valueOf(status.toUpperCase()));
        try {
            productService.update(product, images != null ? List.of(images) : new ArrayList<>());
            ra.addFlashAttribute("message", "Товар обновлён.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products/edit/" + id;
    }

    @PostMapping("/publish/{id}")
    public String publish(@PathVariable Long id, RedirectAttributes ra) {
        productService.publish(id);
        ra.addFlashAttribute("message", "Товар опубликован.");
        return "redirect:/admin/products";
    }

    @PostMapping("/unpublish/{id}")
    public String unpublish(@PathVariable Long id, RedirectAttributes ra) {
        productService.unpublish(id);
        ra.addFlashAttribute("message", "Товар снят с публикации.");
        return "redirect:/admin/products";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.delete(id);
            ra.addFlashAttribute("message", "Товар удален.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
