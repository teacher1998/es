/**
 * Copyright (c) 2005-2012 https://github.com/zhangkaitao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.sishuok.es.conf.icon.web.controller;

import com.google.common.collect.Lists;
import com.sishuok.es.common.Constants;
import com.sishuok.es.common.entity.search.Searchable;
import com.sishuok.es.common.web.controller.BaseCRUDController;
import com.sishuok.es.common.web.upload.FileUploadUtils;
import com.sishuok.es.common.web.validate.ValidateResponse;
import com.sishuok.es.conf.icon.entity.Icon;
import com.sishuok.es.conf.icon.entity.IconType;
import com.sishuok.es.conf.icon.service.IconService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.*;
import java.util.List;

/**
 * <p>User: Zhang Kaitao
 * <p>Date: 13-1-28 下午4:29
 * <p>Version: 1.0
 */
@Controller
@RequestMapping(value = "/admin/conf/icon")
public class IconController extends BaseCRUDController<Icon, Long> {

    private IconService iconService;

    @Value("${icon.css.file.src}")
    private String iconClassFile;

    public IconController() {
        setListAlsoSetCommonData(true);
    }

    @Autowired
    public void setIconService(IconService iconService) {
        setBaseService(iconService);
        this.iconService = iconService;
    }

    @Override
    protected void setCommonData(Model model) {
        super.setCommonData(model);
        model.addAttribute("types", IconType.values());
    }



    @RequestMapping(value = "create/discard", method = RequestMethod.GET)
    public String showCreateForm(Model model) {
        throw new RuntimeException("discard method");
    }
    //不再是默认的create，因为下边的create具有多个参数，因此无法覆盖默认的create，因此为了使用该url 我们把父类的url改掉
    @RequestMapping(value = "create/discard", method = RequestMethod.POST)
    @Override
    public String create(Model model, @Valid @ModelAttribute("m") Icon icon, BindingResult result, RedirectAttributes redirectAttributes) {
        throw new RuntimeException("discarded method");
    }

    @RequestMapping(value = "create/{type}", method = RequestMethod.GET)
    public String showCreateForm(@PathVariable(value = "type") IconType type, Model model) {
        Icon icon = new Icon();
        icon.setType(type);
        if(type == IconType.css_sprite || type == IconType.upload_file) {
            icon.setWidth(13);
            icon.setHeight(13);
        }
        model.addAttribute("m", icon);
        return super.showCreateForm(model);
    }


    @RequestMapping(value = "create/{type}", method = RequestMethod.POST)
    public String create(
            HttpServletRequest request,
            Model model,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @Valid @ModelAttribute("m") Icon icon, BindingResult result,
            RedirectAttributes redirectAttributes) {

        if(file != null && !file.isEmpty()) {
            icon.setImgSrc(FileUploadUtils.upload(request, file, result));
        }
        String view = super.create(model, icon, result, redirectAttributes);
        genIconCssFile(request);
        return view;
    }


    @RequestMapping(value = "update/discard/{id}", method = RequestMethod.POST)
    @Override
    public String update(Model model, @Valid @ModelAttribute("m") Icon icon, BindingResult result, @RequestParam(value = "BackURL", required = false) String backURL, RedirectAttributes redirectAttributes) {
        throw new RuntimeException("discarded method");
    }

    @RequestMapping(value = "update/{id}", method = RequestMethod.POST)
    public String update(
            HttpServletRequest request,
            Model model,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @Valid @ModelAttribute("m") Icon icon, BindingResult result,
            @RequestParam(value = "BackURL") String backURL,
            RedirectAttributes redirectAttributes) {
        if(file != null && !file.isEmpty()) {
            icon.setImgSrc(FileUploadUtils.upload(request, file, result));
        }
        String view = super.update(model, icon, result, backURL, redirectAttributes);
        genIconCssFile(request);
        return view;
    }


    @RequestMapping(value = "delete/discard", method = RequestMethod.POST)
    @Override
    public String delete(
            @PathVariable("id") Icon m,
            @RequestParam(value = Constants.BACK_URL, required = false) String backURL,
            RedirectAttributes redirectAttributes) {

        throw new RuntimeException("discarded method");
    }


    @RequestMapping(value = "delete/{id}", method = RequestMethod.POST)
    public String delete(
            HttpServletRequest request,
            @PathVariable("id") Icon m,
            @RequestParam(value = Constants.BACK_URL, required = false) String backURL,
            RedirectAttributes redirectAttributes) {

        String view = super.delete(m, backURL, redirectAttributes);

        genIconCssFile(request);

        return view;

    }



    @RequestMapping(value = "batch/delete/discard")
    public String deleteInBatch(
            @RequestParam(value = "ids", required = false) Long[] ids,
            @RequestParam(value = Constants.BACK_URL, required = false) String backURL,
            RedirectAttributes redirectAttributes) {

        throw new RuntimeException("discarded method");

    }


    @RequestMapping(value = "batch/delete")
    public String deleteInBatch(
            HttpServletRequest request,
            @RequestParam(value = "ids", required = false) Long[] ids,
            @RequestParam(value = Constants.BACK_URL, required = false) String backURL,
            RedirectAttributes redirectAttributes) {

        String view = super.deleteInBatch(ids, backURL, redirectAttributes);

        genIconCssFile(request);

        return view;
    }




    @RequestMapping(value = "/select")
    public String select(Model model) {
        setCommonData(model);
        model.addAttribute("icons", iconService.findAll());
        return getViewPrefix() + "/select";
    }



    /**
     * 如果量大 建议 在页面设置按钮 然后点击生成
     * @param request
     * @return
     */
    @RequestMapping(value = "/genCssFile")
    @ResponseBody
    public String genIconCssFile(HttpServletRequest request) {
        String uploadFileTemplate = ".%1$s{background:url(%2$s/%3$s);width:%4$spx;height:%5$spx;display:inline-block;vertical-align: middle;%6$s}";
        String cssSpriteTemplate = ".%1$s{background:url(%2$s/%3$s) no-repeat -%4$spx -%5$spx;width:%6$spx;height:%7$spx;display:inline-block;vertical-align: middle;%8$s}";

        ServletContext sc = request.getSession().getServletContext();
        String ctx = sc.getContextPath();

        List<String> cssList = Lists.newArrayList();

        Searchable searchable = Searchable.newSearchable()
                .addSearchFilter("type_in", new IconType[]{IconType.upload_file, IconType.css_sprite});

        List<Icon> iconList = iconService.findAllWithNoPageNoSort(searchable);

        for(Icon icon : iconList) {

            if(icon.getType() == IconType.upload_file) {
                cssList.add(String.format(
                        uploadFileTemplate,
                        icon.getIdentity(),
                        ctx, icon.getImgSrc(),
                        icon.getWidth(), icon.getHeight(),
                        icon.getStyle()));
                continue;
            }

            if(icon.getType() == IconType.css_sprite) {
                cssList.add(String.format(
                        cssSpriteTemplate,
                        icon.getIdentity(),
                        ctx, icon.getSpriteSrc(),
                        icon.getLeft(), icon.getTop(),
                        icon.getWidth(), icon.getHeight(),
                        icon.getStyle()));
                continue;
            }

        }

        try {
            ServletContextResource resource = new ServletContextResource(sc, iconClassFile);
            FileUtils.writeLines(resource.getFile(), cssList);
        } catch (Exception e) {
            //TODO 记日志
            e.printStackTrace();
            return "生成失败：" + e.getMessage();
        }

        return "生成成功";
    }




    @RequestMapping(value = "validate", method = RequestMethod.GET)
    @ResponseBody
    public Object validate(
            @RequestParam("fieldId") String fieldId, @RequestParam("fieldValue") String fieldValue,
            @RequestParam(value = "id", required = false) Long id) {
        ValidateResponse response = ValidateResponse.newInstance();

        if ("identity".equals(fieldId)) {
            Icon icon = iconService.findByIdentity(fieldValue);
            if (icon == null || (icon.getId().equals(id) && icon.getIdentity().equals(fieldValue))) {
                //如果msg 不为空 将弹出提示框
                response.validateSuccess(fieldId, "");
            } else {
                response.validateFail(fieldId, "该标识符已被其他人使用");
            }
        }
        return response.result();
    }


    /**
     * 转换图标为sql的
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
//        readClass();
        readUploadFile();
    }

    private static void readUploadFile() throws IOException {
         String fromFile = "E:\\workspace\\git\\es\\web\\src\\main\\webapp\\WEB-INF\\static\\comp\\zTree\\css\\zTreeStyle\\img\\diy\\icon.txt";
        String toFile = "C:\\Documents and Settings\\Administrator\\桌面\\b.sql";
        String template = "insert into `conf_icon` (`id`, `identity`, `img_src`, `type`, `width`, `height`) values(%1$d, '%2$s', '%3$s', 'upload_file', %4$d, %5$d);";

        List<String> list = FileUtils.readLines(new File(fromFile));
        FileWriter writer = new FileWriter(toFile);

        int count = 300;
        for(int i = 0, l = list.size(); i < l; i+=2) {
            writer.write(String.format(template, count++, list.get(i), list.get(i+1), 16, 16));
            writer.write("\r\n");
        }

        writer.close();
    }

//    private static void readClass() {
//        String fromFile = "C:\\Documents and Settings\\Administrator\\桌面\\a.sql";
//        String toFile = "C:\\Documents and Settings\\Administrator\\桌面\\b.sql";
//        String template = "insert into `conf_icon` (`id`, `identity`, `css_class`, `type`) values(%1$d, '%2$s', '%2$s', 'css_class');";
//
//        List<String> cssClassList = FileUtils.readLines(new File(fromFile));
//        FileWriter writer = new FileWriter(toFile);
//
//        for(int i = 1, l = cssClassList.size(); i < l; i++) {
//            writer.write(String.format(template, i, cssClassList.get(i)));
//            writer.write("\r\n");
//        }
//
//        writer.close();
//    }

}
