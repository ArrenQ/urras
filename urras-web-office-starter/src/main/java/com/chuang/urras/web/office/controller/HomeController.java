package com.chuang.urras.web.office.controller;

import com.chuang.urras.support.MapResult;
import com.chuang.urras.support.Result;
import com.chuang.urras.support.enums.Language;
import com.chuang.urras.toolskit.basic.Captcha;
import com.chuang.urras.web.office.SessionKeys;
import com.chuang.urras.web.office.model.User;
import com.chuang.urras.web.office.service.single.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.util.Date;

@Controller
@Api(tags = "基础模块")
public class HomeController extends BaseController {

    private static final Captcha tool = new Captcha();

    @Resource
    private IUserService userService;

    @GetMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        // 设置相应类型,告诉浏览器输出的内容为图片
        response.setContentType("image/jpeg");
        // 不缓存此内容
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "No-cache");
        response.setDateHeader("Expire", 0);
        try {

            //HttpSession session = req.getSession();


            StringBuffer code = new StringBuffer();
            BufferedImage image = tool.genRandomCodeImage(code);
            /*session.removeAttribute(KEY_CAPTCHA);
            session.removeAttribute(KEY_CAPTCHA_TIME);*/
            session.setAttribute(SessionKeys.KEY_CAPTCHA_TIME, new Date().getTime());
            session.setAttribute(SessionKeys.KEY_CAPTCHA, code.toString());
            // 将内存中的图片通过流动形式输出到客户端
            ImageIO.write(image, "JPEG", response.getOutputStream());

        } catch (Exception e) {
            logger.error("验证码生成失败",e);
        }

    }

    @PostMapping("/language/{lang}")
    @ResponseBody
    @ApiOperation("修改语言")
    @ApiImplicitParam(name = "lang", value = "语言", required = true, dataTypeClass = String.class, paramType = "path")
    public Result language(@PathVariable("lang") String lang) {
        User user = new User();
        User entity = getLoginUser();
        user.setUsername(entity.getUsername());

        Language language = Language.valueOf(lang);

        super.changeLanguage(language);

        user.setUseLanguage(language);
        userService.updateById(user);
        entity.setUseLanguage(language);
        getSession().setAttribute(SessionKeys.LOGIN_USER, entity);

        return Result.success();
    }

    @GetMapping("/api/auth/unauthorized")
    @ResponseBody
    public Result unauthorized4Api() {
        return MapResult.fail("认证失败").data("unauthorized", true).toResult();
    }
}
