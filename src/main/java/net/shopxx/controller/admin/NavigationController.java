/*
 * Copyright 2005-2017 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 */
package net.shopxx.controller.admin;

import java.util.List;

import javax.inject.Inject;

import net.shopxx.Message;
import net.shopxx.Pageable;
import net.shopxx.entity.ArticleCategory;
import net.shopxx.entity.Country;
import net.shopxx.entity.Navigation;
import net.shopxx.entity.ProductCategory;
import net.shopxx.service.ArticleCategoryService;
import net.shopxx.service.CountryService;
import net.shopxx.service.NavigationService;
import net.shopxx.service.ProductCategoryService;
import net.shopxx.util.StringUtil;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Controller - 导航
 * 
 * @author SHOP++ Team
 * @version 5.0.3
 */
@Controller("adminNavigationController")
@RequestMapping("/admin/navigation")
public class NavigationController extends BaseController {

	@Inject
	private NavigationService navigationService;
	@Inject
	private ArticleCategoryService articleCategoryService;
	@Inject
	private ProductCategoryService productCategoryService;
	@Inject
	private CountryService countryService;
	
	/**
	 * 添加
	 */
	@GetMapping("/add")
	public String add(ModelMap model) {
		Country  country = countryService.getDefaultCountry();
		model.addAttribute("positions", Navigation.Position.values());
		model.addAttribute("articleCategoryTree", articleCategoryService.findTree(country));
		model.addAttribute("productCategoryTree", productCategoryService.findTree(country));
		return "admin/navigation/add";
	}

	/**
	 * 保存
	 */
	@PostMapping("/save")
	public String save(Navigation navigation, String countryName, RedirectAttributes redirectAttributes) {
		if (!isValid(navigation)) {
			return ERROR_VIEW;
		}
		navigation.setCountry(countryService.findByName(countryName));
		navigationService.save(navigation);
		addFlashMessage(redirectAttributes, Message.success(SUCCESS_MESSAGE));
		return "redirect:list";
	}

	/**
	 * 编辑
	 */
	@GetMapping("/edit")
	public String edit(Long id, ModelMap model) {
		Navigation navigation = navigationService.find(id);
		model.addAttribute("positions", Navigation.Position.values());
		model.addAttribute("articleCategoryTree", articleCategoryService.findTree(navigation.getCountry()));
		model.addAttribute("productCategoryTree", productCategoryService.findTree(navigation.getCountry()));
		model.addAttribute("navigation", navigation);
		return "admin/navigation/edit";
	}

	/**
	 * 更新
	 */
	@PostMapping("/update")
	public String update(Navigation navigation, String countryName, RedirectAttributes redirectAttributes) {
		if (!isValid(navigation)) {
			return ERROR_VIEW;
		}
		navigation.setCountry(countryService.findByName(countryName));
		navigationService.update(navigation);
		addFlashMessage(redirectAttributes, Message.success(SUCCESS_MESSAGE));
		return "redirect:list";
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	public String list(String countryName, Pageable pageable, ModelMap model) {
		Country country = null;
		if (StringUtil.isNotEmpty(countryName)) {
			country = countryService.findByName(countryName);
		}
		model.addAttribute("countryName", countryName);
		model.addAttribute("topNavigations", navigationService.findList(Navigation.Position.top, country));
		model.addAttribute("middleNavigations", navigationService.findList(Navigation.Position.middle, country));
		model.addAttribute("bottomNavigations", navigationService.findList(Navigation.Position.bottom, country));
		return "admin/navigation/list";
	}

	/**
	 * 删除
	 */
	@PostMapping("/delete")
	public @ResponseBody Message delete(Long[] ids) {
		navigationService.delete(ids);
		return Message.success(SUCCESS_MESSAGE);
	}
	/**
	 * 根据国家选择上级导航分类
	 */
	@GetMapping("/listByCountry")
	public @ResponseBody JSONArray listByCountry(String countryName) {
		Country country = null;
		if (StringUtil.isNotEmpty(countryName)) {
			country = countryService.findByName(countryName);
		}
		List<ArticleCategory> articleCategoryTree = articleCategoryService.findTree(country);
		List<ProductCategory> productCategoryTree = productCategoryService.findTree(country);
		
		JSONArray jsonArray = new JSONArray();
		if (null != articleCategoryTree  ) {
			for (ArticleCategory bean : articleCategoryTree) {
				JSONObject object = new JSONObject();
				object.put("path", bean.getPath());
				object.put("name", bean.getName());
				object.put("grade", bean.getGrade());
				jsonArray.add(object);
			}			
		}
		if(null != productCategoryTree){
			for (ProductCategory bean : productCategoryTree) {
				JSONObject object = new JSONObject();
				object.put("path", bean.getPath());
				object.put("name", bean.getName());
				object.put("grade", bean.getGrade());
				jsonArray.add(object);
			}
		}
		return jsonArray;
	}
}