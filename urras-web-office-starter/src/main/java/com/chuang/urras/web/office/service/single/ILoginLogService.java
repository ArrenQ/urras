package com.chuang.urras.web.office.service.single;

import com.chuang.urras.crud.service.IService;
import com.chuang.urras.toolskit.basic.StringKit;
import com.chuang.urras.web.office.model.LoginLog;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

/**
 * <p>
 * 管理员登陆日子  服务类
 * </p>
 *
 * @author ath
 * @since 2020-02-25
 */
public interface ILoginLogService extends IService<LoginLog> {

    default boolean addLoginLog(String username,
                               String realName,
                               boolean success,
                               String host,
                               @Nullable String device,
                               @Nullable String clientMac,
                               @Nullable String userAgent,
                               @Nullable String referer) {
        return this.save(new LoginLog()
            .setUsername(username)
            .setClientHost(host)
            .setRealName(realName)
            .setLoginTime(LocalDateTime.now())
            .setSuccess(success)
            .setDevice(StringKit.nullToEmpty(device))
            .setClientMac(StringKit.nullToEmpty(clientMac))
            .setUserAgent(StringKit.nullToEmpty(userAgent))
            .setReferer(StringKit.nullToEmpty(referer)));
    }
}
