/**
 * Copyright (c) 2005-2012 https://github.com/zhangkaitao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.sishuok.es.sys.auth.web.controller;

import com.sishuok.es.common.Constants;
import com.sishuok.es.common.entity.search.Searchable;
import com.sishuok.es.common.plugin.web.controller.BaseTreeableController;
import com.sishuok.es.common.web.bind.annotation.PageableDefaults;
import com.sishuok.es.common.web.bind.annotation.SearchableDefaults;
import com.sishuok.es.common.web.controller.BaseCRUDController;
import com.sishuok.es.sys.auth.entity.Auth;
import com.sishuok.es.sys.auth.entity.AuthType;
import com.sishuok.es.sys.auth.service.AuthService;
import com.sishuok.es.sys.permission.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

/**
 * <p>User: Zhang Kaitao
 * <p>Date: 13-1-28 下午4:29
 * <p>Version: 1.0
 */
@Controller
@RequestMapping(value = "/admin/sys/auth")
public class AuthController extends BaseCRUDController<Auth, Long> {

    private AuthService authService;

    @Autowired
    private RoleService roleService;

    public AuthController() {
        setListAlsoSetCommonData(true);
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        setBaseService(authService);
        this.authService = authService;
    }

    @Override
    protected void setCommonData(Model model) {
        super.setCommonData(model);
        model.addAttribute("types", AuthType.values());
        model.addAttribute("roles", roleService.findAll());
    }


    @SearchableDefaults(value = "type_eq=user")
    @Override
    public String list(Searchable searchable, Model model) {

        String typeName = String.valueOf(searchable.getValue("type_eq"));
        model.addAttribute("type", AuthType.valueOf(typeName));

        return super.list(searchable, model);
    }

    @RequestMapping(value = "create/discarded", method = RequestMethod.GET)
    public String showCreateForm(Model model) {
        throw new RuntimeException("discard method");
    }


    @RequestMapping(value = "create/discarded", method = RequestMethod.POST)
    public String create(
            Model model, @Valid @ModelAttribute("m") Auth m, BindingResult result,
            RedirectAttributes redirectAttributes) {
        throw new RuntimeException("discard method");
    }

    @RequestMapping(value = "{type}/create", method = RequestMethod.GET)
    public String showCreateFormWithType(@PathVariable("type") AuthType type, Model model) {
        return super.showCreateForm(model);
    }


    @RequestMapping(value = "{type}/create", method = RequestMethod.POST)
    public String createWithType(
            Model model,
            @RequestParam(value = "userIds", required = false) Long[] userIds,
            @RequestParam(value = "groupIds", required = false) Long[] groupIds,
            @RequestParam(value = "organizationIds", required = false) Long[] organizationIds,
            @RequestParam(value = "jobIds", required = false) Long[][] jobIds,
            @Valid @ModelAttribute("m") Auth m, BindingResult result,
            RedirectAttributes redirectAttributes) {


        if (hasError(m, result)) {
            return showCreateForm(model);
        }

        if(m.getType() == AuthType.user) {
            authService.addUserAuth(userIds, m);
        } else if(m.getType() == AuthType.user_group || m.getType() == AuthType.organization_group) {
            authService.addGroupAuth(groupIds, m);
        } else if(m.getType() == AuthType.organization_job) {
            authService.addOrganizationJobAuth(organizationIds, jobIds, m);
        }

        redirectAttributes.addFlashAttribute(Constants.MESSAGE, "新增成功");
        return redirectToUrl("/admin/sys/auth?search.type_eq=" + m.getType());
    }



}
