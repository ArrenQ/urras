package com.chuang.urras.web.office.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.chuang.urras.crud.filters.RowQuery;
import com.chuang.urras.crud.service.IService;
import com.chuang.urras.support.Result;
import com.chuang.urras.support.enums.CRUD;
import com.chuang.urras.support.exception.SystemWarnException;
import com.chuang.urras.toolskit.basic.BeanKit;
import com.chuang.urras.toolskit.basic.CollectionKit;
import com.chuang.urras.toolskit.basic.IOKit;
import com.chuang.urras.toolskit.third.javax.servlet.HttpKit;
import com.chuang.urras.web.office.model.OperationLog;
import com.chuang.urras.web.office.service.single.IOperationLogService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyDescriptor;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CrudController<T> extends BaseController {

    private final String permissionPrefix;

    @Autowired protected IService<T> service;

    @Resource protected IOperationLogService operationLogService;

//    @Resource private IProductCrudManager productCrudManager;

//    @Value("urras.product.root")
//    private String rootProductCode;

    public CrudController(String permissionPrefix) {
        this.permissionPrefix = permissionPrefix;

    }

    /**
     * 对rowQuery进行预处理
     */
//    protected void preHandRowQuery(RowQuery rowQuery) {
//        User user = getLoginUser();
////        if (ISite.class.isAssignableFrom(service.currentModelClass())) {
//        if (BeanKit.getPropertyDescriptor(service.currentModelClass(), "productCode").isPresent()) {
//            String[] list = productCrudManager.findChildren(user.getProductCode());
//            if(list.length == 0) {
//                throw new SystemWarnException(Result.FAIL_CODE, "该账号存在问题，无法找到所在siteId");
//            }
//
//
//            if(!Objects.equals(rootProductCode, user.getProductCode())) {
//                String[] ids_str = Arrays.stream(list).map(Object::toString).toArray(String[]::new);
//                SetFilter f = new SetFilter();
//                f.setField("productCode");
//                f.setOption("in");
//                f.setValues(ids_str);
//                RowQuery.Filter[] fs = CollectionKit.append(rowQuery.getFilters(), f);
//                rowQuery.setFilters(fs);
//            }
//
//        }
//    }

    @PostMapping("/query")
    @ResponseBody
    @ApiOperation("根据RowQuery对象进行查询")
    @ApiImplicitParam(name = "rowQuery", value = "查询记录", required = true, dataTypeClass = RowQuery.class)
    public IPage<T> query(@RequestBody RowQuery rowQuery) {
        this.checkPermission(":view");
//        preHandRowQuery(rowQuery);
        return service.pageByRowQuery(rowQuery);

    }

    @PostMapping("/export/xlsx")
    public void exportXLSX(@RequestBody RowQuery rowQuery, HttpServletResponse response) {
        this.checkPermission(":export");
//        preHandRowQuery(rowQuery);
        int pageSize = 5000;
        int windowSize = 1000;


        Class<T> beanClass = service.currentModelClass();
        PropertyDescriptor[] properties = BeanKit.getPropertyDescriptors(beanClass);

        OutputStream os = null;
        try {
            response.setContentType("application/force-download"); // 设置下载类型
            response.setHeader("Content-Disposition","attachment;filename=" + beanClass.getName() + ".xlsx"); // 设置文件的名称
            os = response.getOutputStream(); // 输出流
            SXSSFWorkbook wb = new SXSSFWorkbook(windowSize);//内存中保留 1000 条数据，以免内存溢出，其余写入 硬盘
            Sheet sheet1 = wb.createSheet("data"); //获得该工作区的第一个sheet
            int excelRow = 0;
            //标题行
            Row titleRow = sheet1.createRow(excelRow++);
            for (int i = 0; i < properties.length; i++) {
                Cell cell = titleRow.createCell(i);
                cell.setCellValue(properties[i].getName());
            }

            int currentPage = 1;
            IPage<T> page;
            do {
                rowQuery.setPageNum(currentPage);
                rowQuery.setPageSize(pageSize);
                page = service.pageByRowQuery(rowQuery);

                List<T> records = page.getRecords();
                if(CollectionKit.isEmpty(records)){
                    continue;
                }
                for(T t : records) {
                    Row contentRow = sheet1.createRow(excelRow++);
                    for(int j = 0; j < properties.length; j++) {
                        Cell cell = contentRow.createCell(j);
                        cell.setCellValue(Objects.toString(properties[j].getReadMethod().invoke(t), ""));
                    }
                }

                currentPage++;
            } while(currentPage <= page.getPages());

            wb.write(os);
        } catch (Exception e) {
            throw new SystemWarnException(Result.FAIL_CODE, "下载失败", e);
        } finally {
            IOKit.close(os);
        }
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("根据id删除一条记录")
    @ResponseBody
    public Result deleteByKey(@PathVariable("id") String id, HttpServletRequest request) {
        this.checkPermission(":delete");

        T before = service.getById(id)
                .orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, "记录本身就不存在"));

        boolean deleted = service.removeById(id);
        createOptLogs(deleted, HttpKit.getIpAddress(request), CRUD.DELETE, before, null);
        // 添加操作日志
        return Result.whether(deleted);
    }

    @PostMapping("/deleteByIds")
    @ApiOperation("根据id删除一条记录")
    @ResponseBody
    public Result deleteByKeys(String ids) {
        this.checkPermission(":delete");
//        logger.info("delete by ids:" + ids);
        boolean deleted = service.removeByIds(Arrays.asList(ids.split(",")));
        // 添加操作日志
        return Result.whether(deleted);
    }

    @PutMapping(value = "/create")
    @ApiOperation("创建一条数据")
    @ResponseBody
    public Result create(@RequestBody T entity, HttpServletRequest request) {
        this.checkPermission(":create");
        try {
            boolean success = service.save(entity);
            // 添加操作日志
            createOptLogs(success, HttpKit.getIpAddress(request), CRUD.CREATE, null, entity);
            return Result.whether(success);
        } catch (DuplicateKeyException ex) {
            return Result.fail("新增的信息已存在");
        }
    }

    @PostMapping("/update")
    @ApiOperation("更新一条记录")
    @ResponseBody
    public Result update(@RequestBody T vo, HttpServletRequest request) {
        this.checkPermission(":update");
        Optional<Serializable> key = getIdValue(vo);
        Optional<T> optional = key.flatMap(service::getById);

        T before = optional.orElseThrow(() -> new SystemWarnException(Result.FAIL_CODE, "无法找到这笔数据"));

        boolean success = service.updateById(vo);

        if(success) {
            Optional<T> after = key.flatMap(service::getById);
            createOptLogs(true, HttpKit.getIpAddress(request), CRUD.UPDATE, before, after.orElseGet(null));
        } else {
            createOptLogs(false, HttpKit.getIpAddress(request), CRUD.UPDATE, before, null);
        }

        return Result.whether(success);
    }

    protected boolean hasPermission(String permission) {
        return SecurityUtils.getSecurityManager().isPermitted(
                SecurityUtils.getSubject().getPrincipals(),
                permissionPrefix +  permission
        );
    }

    protected void checkPermission(String permission) {

        SecurityUtils.getSecurityManager().checkPermission(
                SecurityUtils.getSubject().getPrincipals(),
                permissionPrefix  + permission
        );

//        if(checkSite) {
//            UserEntity user = getCurrentPrincipal();
//            if(user.getSiteId().intValue() != 0) {
//                throw new AuthorizationException("没权限操作！");
//            }
//        }
    }

    protected <S extends IService<T>> S getService() {
        return (S) service;
    }

    protected void createOptLogs(boolean success,
                                 String clientIp,
                                 CRUD optType,
                                 @Nullable T before,
                                 @Nullable T after) {
        OperationLog entity = new OperationLog();
        entity.setDataClass(service.currentModelClass().getSimpleName());
        entity.setSuccess(success);
        entity.setCrudType(optType);
        entity.setClientIp(clientIp);
        entity.setOperator(getLoginUser().getUsername());
        if(success && optType == CRUD.UPDATE) {
            entity.setDifference(defTip(before, after));
        } else {
            entity.setDifference("");
        }
        operationLogService.save(entity);
    }

    private String defTip(T t1, T t2) {

        PropertyDescriptor[] pds = BeanKit.getPropertyDescriptors(service.currentModelClass());
        JSONObject j1 = new JSONObject();
        JSONObject j2 = new JSONObject();
        try {
            for(PropertyDescriptor pd: pds) {
                Method reader = pd.getReadMethod();
                Object v1 = reader.invoke(t1);
                Object v2 = reader.invoke(t2);

                if (!Objects.equals(v1, v2) || null != pd.getPropertyType().getAnnotation(TableId.class)) {
                    j1.put(pd.getName(), v1);
                    j2.put(pd.getName(), v2);
                }
            }
        } catch (Exception e) {
            logger.warn("比对差异出现异常", e);
            return "";
        }

        return j1.toJSONString() + " -> " + j2.toJSONString();
    }

    protected Optional<Serializable> getIdValue(T obj) {

        List<Field> fields = ReflectionKit.getFieldList(service.currentModelClass());

        for(Field fd: fields) {
            if(null != fd.getAnnotation(TableId.class)) {
                try {
                    return Optional.ofNullable((Serializable) ReflectionKit.getMethodValue(obj, fd.getName()));
                } catch (Exception ignore) { }
            }
        }

        return Optional.empty();
    }
    protected <T> Mono<T> toMono(CompletableFuture<T> future) {
        return Mono.create(sink -> future.thenAccept(sink::success)
                .exceptionally(throwable -> {               //如果异常，则返回异常结果
                    sink.error(throwable);
                    return null;
                })
        );

    }
}
