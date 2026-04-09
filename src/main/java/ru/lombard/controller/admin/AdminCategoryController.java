package ru.lombard.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.lombard.entity.Category;
import ru.lombard.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAllRoot());
        return "admin/categories";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("parents", categoryService.findAllRootEntities());
        return "admin/category-form";
    }

    @PostMapping("/new")
    public String create(@RequestParam String name, @RequestParam(required = false) Long parentId,
                        @RequestParam(defaultValue = "0") int sortOrder, RedirectAttributes ra) {
        Category category = new Category();
        category.setName(name);
        category.setSortOrder(sortOrder);
        if (parentId != null && parentId > 0) {
            categoryService.findById(parentId).ifPresent(category::setParent);
        }
        categoryService.save(category);
        ra.addFlashAttribute("message", "Категория создана.");
        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id).orElseThrow();
        model.addAttribute("category", category);
        model.addAttribute("parents", categoryService.findAllRootEntities());
        return "admin/category-form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, @RequestParam String name, @RequestParam(required = false) Long parentId,
                         @RequestParam(defaultValue = "0") int sortOrder, RedirectAttributes ra) {
        Category category = categoryService.findById(id).orElseThrow();
        category.setName(name);
        category.setSortOrder(sortOrder);
        if (parentId != null && parentId > 0) {
            categoryService.findById(parentId).ifPresent(category::setParent);
        } else {
            category.setParent(null);
        }
        categoryService.save(category);
        ra.addFlashAttribute("message", "Категория обновлена.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        categoryService.deleteById(id);
        ra.addFlashAttribute("message", "Категория удалена.");
        return "redirect:/admin/categories";
    }
}
