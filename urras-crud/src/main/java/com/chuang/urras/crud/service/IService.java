package com.chuang.urras.crud.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.chuang.urras.crud.filters.RowQuery;
import com.chuang.urras.support.Result;
import com.chuang.urras.support.exception.SystemErrorException;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface IService<T> {
    /**
     * 插入一条记录（选择字段，策略插入）
     *
     * @param entity 实体对象
     */
    boolean save(T entity);

    default void saveEx(T entity) {
        if(!save(entity)) {
            throw new SystemErrorException(Result.FAIL_CODE, "数据保存失败");
        }
    }

    @SuppressWarnings("unchecked")
    default Class<T> currentModelClass() {
        return (Class<T>)ReflectionKit.getSuperClassGenericType(getClass(), 1);
    }

    default <C> List<C> contains(SFunction<T, C> map, List<C> inValue) {
        return lambdaQuery().select(map).in(map, inValue)
                .list()
                .stream()
                .map(map)
                .collect(Collectors.toList());
    }

    default <C> List<C> saveNotContains(SFunction<T, C> map, List<T> list) {
        List<C> contains = contains(map, list.stream().map(map).collect(Collectors.toList()));
        List<T> noContains = list.stream().filter(t -> !contains.contains(map.apply(t))).collect(Collectors.toList());
        this.saveBatch(noContains);
        return contains;
    }

    /**
     * 插入（批量）
     *
     * @param entityList 实体对象集合
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean saveBatch(Collection<T> entityList) {
        return saveBatch(entityList, 1000);
    }

    /**
     * 插入（批量）
     *
     * @param entityList 实体对象集合
     * @param batchSize  插入批次数量
     */
    boolean saveBatch(Collection<T> entityList, int batchSize);

    /**
     * 批量修改插入
     *
     * @param entityList 实体对象集合
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean saveOrUpdateBatch(Collection<T> entityList) {
        return saveOrUpdateBatch(entityList, 1000);
    }

    /**
     * 批量修改插入
     *
     * @param entityList 实体对象集合
     * @param batchSize  每次的数量
     */
    boolean saveOrUpdateBatch(Collection<T> entityList, int batchSize);

    /**
     * 根据 ID 删除
     *
     * @param id 主键ID
     */
    boolean removeById(Serializable id);

    /**
     * 根据 entity 条件，删除记录
     *
     * @param queryWrapper 实体包装类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    boolean remove(Wrapper<T> queryWrapper);

    default boolean remove(T condition) {
        return remove(new QueryWrapper<>(condition));
    }

    /**
     * 删除（根据ID 批量删除）
     *
     * @param idList 主键ID列表
     */
    boolean removeByIds(Collection<? extends Serializable> idList);

    /**
     * 根据 ID 选择修改
     *
     * @param entity 实体对象
     */
    boolean updateById(T entity);

    default void updateByIdEx(T entity) {
        if(!updateById(entity)) {
            throw new SystemErrorException(Result.FAIL_CODE, "数据更新失败");
        }
    }

    /**
     * 根据 whereEntity 条件，更新记录
     *
     * @param entity        实体对象
     * @param updateWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper}
     */
    boolean update(T entity, Wrapper<T> updateWrapper);

    /**
     * 根据 UpdateWrapper 条件，更新记录 需要设置sqlset
     *
     * @param updateWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper}
     */
    default boolean update(Wrapper<T> updateWrapper) {
        return update(null, updateWrapper);
    }

    /**
     * 根据ID 批量更新
     *
     * @param entityList 实体对象集合
     */
    @Transactional(rollbackFor = Exception.class)
    default boolean updateBatchById(Collection<T> entityList) {
        return updateBatchById(entityList, 1000);
    }

    /**
     * 根据ID 批量更新
     *
     * @param entityList 实体对象集合
     * @param batchSize  更新批次数量
     */
    boolean updateBatchById(Collection<T> entityList, int batchSize);

    /**
     * TableId 注解存在更新记录，否插入一条记录
     *
     * @param entity 实体对象
     */
    boolean saveOrUpdate(T entity);

    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     */
    Optional<T> getById(Serializable id);

    /**
     * 查询（根据ID 批量查询）
     *
     * @param idList 主键ID列表
     */
    Collection<T> listByIds(Collection<? extends Serializable> idList);


    /**
     * 根据 Wrapper，查询一条记录 <br/>
     * <p>结果集，如果是多个会抛出异常，随机取一条加上限制条件 wrapper.last("LIMIT 1")</p>
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    default Optional<T> getOne(Wrapper<T> queryWrapper) {
        return getOne(queryWrapper, true);
    }

    default Optional<T> getOne(T condition) {
        return getOne(new QueryWrapper<>(condition));
    }

    /**
     * 根据 Wrapper，查询一条记录
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     * @param throwEx      有多个 result 是否抛出异常
     */
    Optional<T> getOne(Wrapper<T> queryWrapper, boolean throwEx);


    /**
     * 根据 Wrapper，查询一条记录
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     * @param mapper       转换函数
     */
    default <V> V getObj(Wrapper<T> queryWrapper, Function<? super Object, V> mapper) {
        return SqlHelper.getObject(listObjs(queryWrapper, mapper));
    }

    /**
     * 根据 Wrapper 条件，查询总记录数
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    int count(Wrapper<T> queryWrapper);

    /**
     * 查询总记录数
     *
     * @see Wrappers#emptyWrapper()
     */
    default int count() {
        return count(Wrappers.emptyWrapper());
    }

    /**
     * 查询列表
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    List<T> list(Wrapper<T> queryWrapper);

    default List<T> list(T condition) {
        return list(new QueryWrapper<>(condition));
    }

    /**
     * 查询所有
     *
     * @see Wrappers#emptyWrapper()
     */
    default List<T> list() {
        return list(Wrappers.emptyWrapper());
    }

    /**
     * 翻页查询
     *
     * @param page         翻页对象
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    IPage<T> page(IPage<T> page, Wrapper<T> queryWrapper);

    /**
     * 无条件翻页查询
     *
     * @param page 翻页对象
     * @see Wrappers#emptyWrapper()
     */
    default IPage<T> page(IPage<T> page) {
        return page(page, Wrappers.emptyWrapper());
    }

    /**
     * 查询全部记录
     */
    default List<Object> listObjs() {
        return listObjs(Function.identity());
    }

    /**
     * 查询全部记录
     *
     * @param mapper 转换函数
     */
    default <V> List<V> listObjs(Function<? super Object, V> mapper) {
        return listObjs(Wrappers.emptyWrapper(), mapper);
    }

    /**
     * 根据 Wrapper 条件，查询全部记录
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    default List<Object> listObjs(Wrapper<T> queryWrapper) {
        return listObjs(queryWrapper, Function.identity());
    }

    /**
     * 根据 Wrapper 条件，查询全部记录
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     * @param mapper       转换函数
     */
    <V> List<V> listObjs(Wrapper<T> queryWrapper, Function<? super Object, V> mapper);


    /**
     * 获取对应 entity 的 BaseMapper
     *
     * @return BaseMapper
     */
    BaseMapper<T> getBaseMapper();

    /**
     * 链式查询 普通
     *
     * @return QueryWrapper 的包装类
     */
    default QueryChainWrapper<T> query() {
        return new QueryChainWrapper<>(getBaseMapper());
    }

    /**
     * 链式查询 lambda 式
     *
     * @return LambdaQueryWrapper 的包装类
     */
    default LambdaQueryChainWrapper<T> lambdaQuery() {
        return new LambdaQueryChainWrapper<>(getBaseMapper());
    }

    /**
     * 链式更改 普通
     *
     * @return UpdateWrapper 的包装类
     */
    default UpdateChainWrapper<T> update() {
        return new UpdateChainWrapper<>(getBaseMapper());
    }

    /**
     * 链式更改 lambda 式
     *
     * @return LambdaUpdateWrapper 的包装类
     */
    default LambdaUpdateChainWrapper<T> lambdaUpdate() {
        return new LambdaUpdateChainWrapper<>(getBaseMapper());
    }

    IPage<T> pageByRowQuery(RowQuery rowQuery);
}
