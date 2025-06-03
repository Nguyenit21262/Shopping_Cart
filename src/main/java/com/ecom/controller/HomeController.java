package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}
		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

	@GetMapping("/")
	public String index(Model m) {
		
		List<Category> allActiveCategory = categoryService.getAllActiveCategory().stream()
				.sorted((c1, c2) -> Integer.compare(c2.getId(), c1.getId()))
				.limit(6).toList();
		List<Product> allActiveProducts = productService.getAllActiveProducts("").stream()
				.sorted(Comparator.comparing(Product::getId).reversed())
				.limit(8).toList();
		m.addAttribute("category", allActiveCategory);
		m.addAttribute("products", allActiveProducts);
		return "index";
	}

	@GetMapping("/signin")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@GetMapping("/products")
	public String products(Model m,@RequestParam(value = "category", defaultValue = "") String category,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "8") Integer pageSize) {
		//System.out.println("category =" + category);
		List<Category> categories = categoryService.getAllActiveCategory();
		m.addAttribute("paramValue", category);
		m.addAttribute("categories", categories);
		
		// List<Product> products = productService.getAllActiveProducts(category); 		
		// m.addAttribute("products", products);
		
		Page<Product> page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
		List<Product> products = page.getContent();
		m.addAttribute("products", products);
		m.addAttribute("productsSize", products.size());
		
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		
		return "product";
	}

	@GetMapping("/product/{id}")
	public String viewProduct(@PathVariable int id, Model m) {
		Product productById = productService.getProductById(id);
		m.addAttribute("product", productById);

		return "view_product";
	}

	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
			throws IOException {
		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		user.setProfileImage(imageName);
		UserDtls saveUser = userService.saveUser(user);

		if (!ObjectUtils.isEmpty(saveUser)) {
			if (!file.isEmpty()) {

				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Path.of(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());
				// System.out.print(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			}
			session.setAttribute("succMsg", "Register successfully");

		} else {
			session.setAttribute("errorMsg", "Something wrong on server ");
		}

		return "redirect:/register";
	}

	@GetMapping("/search")
	public String searchProduct(
	        @RequestParam String ch,
	        @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
	        @RequestParam(name = "pageSize", defaultValue = "8") Integer pageSize,
	        Model m) {

	    Page<Product> page = productService.searchProductPagination(pageNo, pageSize, ch);
	    List<Product> searchProducts = page.getContent();

	    m.addAttribute("products", searchProducts);
	    m.addAttribute("productsSize", searchProducts.size());

	    m.addAttribute("pageNo", page.getNumber());
	    m.addAttribute("pageSize", page.getSize());
	    m.addAttribute("totalElements", page.getTotalElements());
	    m.addAttribute("totalPages", page.getTotalPages());
	    m.addAttribute("isFirst", page.isFirst());
	    m.addAttribute("isLast", page.isLast());

	    List<Category> categories = categoryService.getAllActiveCategory();
	    m.addAttribute("categories", categories);

	    m.addAttribute("paramValue", ch); 

	    return "product";
	}


}
