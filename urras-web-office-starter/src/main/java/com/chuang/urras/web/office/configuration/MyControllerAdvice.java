package com.chuang.urras.web.office.configuration;

import com.chuang.urras.support.MapResult;
import com.chuang.urras.support.Result;
import com.chuang.urras.support.exception.SystemWarnException;
import com.chuang.urras.toolskit.basic.StringKit;
import com.chuang.urras.web.office.PrincipalExpiredException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Locale;
import java.util.UUID;

/**
 * 控制器建言，由于内部有@Component注解，自动会被spring当做组件管理。
 * 该类用来控制器全局配置,
 * 该类要再WebMvcConfig Import进入才有效。
 * Created by ath on 2017/3/17.
 */
@ControllerAdvice
public class MyControllerAdvice {

    @Resource
    private MessageSource messageSource;

    private Logger logger = LoggerFactory.getLogger(MyControllerAdvice.class);

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Result exception(Exception exception) {
        logger.error(exception.getMessage(), exception);
        if(exception instanceof PrincipalExpiredException) {
            Subject subject = SecurityUtils.getSubject();
            while(subject.isRunAs()) {
                subject.releaseRunAs();
            }
            return MapResult.fail( "检查到您的令牌被更新或回收，系统现已自动更新，请您再次重试。")
                    .data("refreshInfo", true)
                    .toResult();
        } else if(exception instanceof ShiroException) {
            return MapResult.fail( "您的权限不够：" + exception.getMessage())
                    .data("refreshInfo", true)
                    .toResult();
        } if (exception instanceof ConstraintViolationException) {
            Locale local = (Locale) SecurityUtils.getSubject().getSession().getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
            ConstraintViolationException ex = (ConstraintViolationException) exception;
            StringBuilder msg = new StringBuilder();
            for (ConstraintViolation cv : ex.getConstraintViolations()) {
                String _msg = cv.getMessageTemplate().trim();
                msg.append(
                        messageSource.getMessage(_msg.substring(1, _msg.length() - 1), null, local)
                ).append(",");
            }
            return Result.fail(msg.toString());
        } else if (exception instanceof BindException) {
            BindingResult bindingResult = ((BindException) exception).getBindingResult();
            if (bindingResult.getFieldError() == null) {
                return Result.fail("参数校验失败");
            } else {
                return Result.fail(StringKit.nullToEmpty(bindingResult.getFieldError().getDefaultMessage()));
            }
        } else if(exception instanceof SystemWarnException |
                exception instanceof HttpRequestMethodNotSupportedException |
                exception instanceof MethodArgumentNotValidException) {
            return Result.fail(exception.getMessage());
        }

        String err = "Ex:" + UUID.randomUUID();
        logger.error("controller异常, 错误号:" + err, exception);
        return Result.fail("系统异常,请联系开发人员!异常号:" + err);
    }

//    @ModelAttribute
//    public void addAttributes(Model m) {
//        m.addAttribute("staticDomain", staticDomain);
//    }
//
//    @InitBinder
//    public void initBinder(WebDataBinder binder) {
//        binder.setDisallowedFields("id");
//    }
}
