-- CREATE TABLE `t_login_log` (
--   `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
--   `username` varchar(32) NOT NULL COMMENT '用户名',
--   `real_name` varchar(32) NOT NULL COMMENT '真实名',
--   `client_host` varchar(32) NOT NULL COMMENT '客户端host',
--   `login_time` datetime NOT NULL COMMENT '登陆时间',
--   `success` bit(1) NOT NULL COMMENT '是否登陆成功',
--   `client_mac` varchar(32) NOT NULL DEFAULT '' COMMENT '客户端mac地址',
--   `device` varchar(50) NOT NULL COMMENT '设备',
--   `referer` varchar(50) NOT NULL COMMENT '来源',
--   `user_agent` varchar(255) NOT NULL COMMENT '用户代理',
--   `creator` varchar(32) NOT NULL COMMENT '创建人',
--   `created_time` datetime NOT NULL COMMENT '创建时间',
--   `updater` varchar(32) NOT NULL COMMENT '更新人',
--   `updated_time` datetime NOT NULL COMMENT '更新时间',
--   PRIMARY KEY (`id`),
--   KEY `i_username` (`username`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='管理员登陆日子 ';

CREATE TABLE `t_operation_log` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `operator` varchar(32) NOT NULL COMMENT '操作者',
  `crud_type` tinyint(4) NOT NULL COMMENT '操作类型',
  `client_ip` varchar(32) NOT NULL COMMENT '客户端ip',
  `success` bit(1) NOT NULL COMMENT '是否成功',
  `data_class` varchar(32) NOT NULL COMMENT '数据类',
  `difference` varchar(512) NOT NULL COMMENT '差异',
  `creator` varchar(32) NOT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `updater` varchar(32) NOT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `i_operator` (`operator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作时间 ';

CREATE TABLE `t_resource` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `type` varchar(10) NOT NULL COMMENT '类型',
  `url` varchar(255) NOT NULL COMMENT '访问地址',
  `parent_id` int(11) NOT NULL COMMENT '父资源',
  `path` varchar(255) NOT NULL COMMENT '路径',
  `permission` varchar(50) NOT NULL COMMENT '权限字符',
  `available` bit(1) NOT NULL COMMENT '是否有效',
  `i18n` varchar(50) NOT NULL COMMENT '国际化',
  `icon` varchar(50) NOT NULL COMMENT '图标',
  `description` varchar(255) NOT NULL COMMENT '描述',
  `creator` varchar(32) NOT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `updater` varchar(32) NOT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='资源表 ';

CREATE TABLE `t_role` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role` varchar(32) NOT NULL COMMENT '角色',
  `description` varchar(32) NOT NULL COMMENT '说明',
  `available` bit(1) NOT NULL COMMENT '是否有效',
  `creator` varchar(32) NOT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `updater` varchar(32) NOT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='角色 ';

CREATE TABLE `t_role_resource` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_id` int(11) NOT NULL,
  `resource_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_role_id_resource_id` (`role_id`,`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `t_run_as` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `to_username` varchar(32) NOT NULL COMMENT '被授权用户',
  `principal_id` int(11) NOT NULL COMMENT '令牌',
  `creator` varchar(32) NOT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `updater` varchar(32) NOT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_to_username_principal_id` (`to_username`,`principal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='runAs ';

CREATE TABLE `t_user` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(32) NOT NULL COMMENT '用户名',
  `password` varchar(32) NOT NULL COMMENT '密码',
  `nick_name` varchar(32) NOT NULL COMMENT '昵称',
  `salt` varchar(32) NOT NULL COMMENT '盐',
  `real_name` varchar(32) NOT NULL COMMENT '真实名称',
  `gender` varchar(32) NOT NULL COMMENT '性别',
  `email` varchar(32) NOT NULL COMMENT '邮箱',
  `office_phone` varchar(32) NOT NULL COMMENT '办公手机',
  `phone` varchar(32) NOT NULL COMMENT '私人手机',
  `state` tinyint(4) NOT NULL COMMENT '状态',
  `picture` varchar(32) NOT NULL COMMENT '图片',
  `mac_address` varchar(32) DEFAULT NULL COMMENT 'mac地址',
  `ip_address` varchar(32) NOT NULL COMMENT 'ip地址',
  `role_id` int(11) NOT NULL COMMENT '角色id',
  `bound` bit(1) NOT NULL COMMENT '是否绑定',
  `last_login_time` datetime NOT NULL COMMENT '最后登陆时间',
  `use_language` tinyint(4) NOT NULL COMMENT '使用的语言',
  `last_login_ip` varchar(32) NOT NULL COMMENT '最后登陆时IP',
  `creator` varchar(32) NOT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `updater` varchar(32) NOT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='管理员 ';

CREATE TABLE `t_user_principal` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `principal_name` varchar(32) NOT NULL COMMENT '令牌名',
  `main` bit(1) NOT NULL COMMENT '是否为主令牌',
  `owner` varchar(32) NOT NULL COMMENT '令牌所有者',
  `creator` varchar(32) NOT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `updater` varchar(32) NOT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `i_owner` (`owner`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户令牌 ';

CREATE TABLE `t_user_principal_resource` (
  `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_principal_id` int(11) NOT NULL COMMENT '用户令牌id',
  `resource_id` int(11) NOT NULL COMMENT '资源id',
  `creator` varchar(32) NOT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `updater` varchar(32) NOT NULL COMMENT '更新人',
  `updated_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `i_user_principal_id_resource_id` (`user_principal_id`,`resource_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户令牌资源 ';



-- ----------------------------
-- Records of t_resource
-- ----------------------------
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('1', 'Main Navigation', 'menu', '#', 'Main Navigation', '0', '0/', ' ', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('2', '功能区', 'menu', '#', 'menu.function_area', '0', '0/', ' ', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('11', '首页', 'menu', '/home', 'menu.index_page', '1', '0/1/', 'home:*', 1, 'icon-home', '首页', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('21', '权限管理', 'menu', '#', 'menu.auth_opt', '2', '0/2/', '#', 1, 'icon-key', 'fasfd', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('22', '系统基础配置', 'menu', '#', 'menu.basic_sys_setting', '2', '0/2/', '#', 1, 'icon-settings', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('24', '日志管理', 'menu', '#', 'menu.log_opt', '2', '0/2/', '#', 1, 'icon-note', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('2101', '资源管理', 'menu', '/resource', 'menu.resource_opt', '21', '0/2/21', 'resource:*', 1, 'icon-social-dropbox', '资源管理，权限菜单配置', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('2102', '角色管理', 'menu', '/role', 'menu.role_opt', '21', '0/2/21', 'role:*', 1, 'icon-graduation', '角色管理', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('2104', '管理员账号', 'menu', '/user', 'menu.user_account_opt', '21', '0/2/21', 'user:*', 1, 'icon-people', '管理员账号管理', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('2402', '管理员登录日志', 'menu', '/log/login', 'menu.user_login_log', '24', '0/2/24/', 'user-login-log:*', 1, ' icon-flag', '管理员登录日志', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210101', '资源新增', 'button', '#', 'page.resource.add_resource', '2101', '0/2/21/2101/', 'resource:create', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210102', '资源修改', 'button', '#', 'page.resource.change_resource', '2101', '0/2/21/2101/', 'resource:update', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210103', '资源删除', 'button', '#', 'page.resource.del_resource', '2101', '0/2/21/2101/', 'resource:delete', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210104', '资源查看', 'button', '#', 'page.resource.view_resource', '2101', '0/2/21/2101/', 'resource:view', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210201', '角色新增', 'button', '#', 'page.role.create_role', '2102', '0/2/21/2102/', 'role:create', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210202', '角色修改', 'button', '#', 'page.role.update_role', '2102', '0/2/21/2102/', 'role:update', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210203', '角色删除', 'button', '#', 'page.role.del_role', '2102', '0/2/21/2102/', 'role:delete', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210204', '角色查看', 'button', '#', 'page.role.view_role', '2102', '0/2/21/2102/', 'role:view', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210401', '管理员新增', 'button', '#', 'page.user.create_user', '2104', '0/2/21/2104/', 'user:create', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210402', '管理员修改', 'button', '#', 'page.user.update_user', '2104', '0/2/21/2104/', 'user:update', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210403', '管理员删除', 'button', '#', 'page.user.del_user', '2104', '0/2/21/2104/', 'user:delete', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210404', '管理员查看', 'button', '#', 'page.user.user_view', '2104', '0/2/21/2104/', 'user:view', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210421', '管理员权限修改', 'button', '#', 'page.user.user_change_role', '2104', '0/2/21/2104/', 'user:change-role', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210427', '用户登录日志', 'menu', '/log/login/customer', 'menu.customer_login_log', '24', '0/2/24/', 'customer-login-log:*', 1, ' icon-flag', '用户登录日志', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210428', '用户管理', 'menu', '#', 'menu.user_opt', '2', '0/2/', '#', 1, 'icon-list', '用户管理', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210434', '操作记录', 'menu', '/log/operation', 'menu.operation_log', '24', '0/2/24/', 'operation-log:*', 1, ' icon-flag', ' ', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210446', '令牌管理', 'menu', '/principal', 'menu.principal_opt', '21', '0/2/21', 'user-principal:*', 1, 'icon-key', '令牌管理', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210447', '个人令牌铸造', 'button', '#', 'menu.principal_make', '210446', '0/2/21/210446', 'user-principal-personal:make', 1, 'icon-key', '个人令牌铸造', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210448', '个人令牌赋权', 'button', '#', 'menu.principal_reauth', '210446', '0/2/21/210446', 'user-principal-resource-personal:re-auth', 1, 'icon-key', '个人令牌赋权', 'sys', NOW(), 'sys', NOW());
INSERT INTO `t_resource` (id, `name`, type, url, i18n, parent_id, path, permission, available, icon, description, creator, created_time, updater, updated_time) VALUES ('210449', '管理员新增', 'button', '#', 'page.user.force_change_user_pwd', '2104', '0/2/21/2104/', 'auth:force-change-pwd', 1, 'icon-window', ' ', 'sys', NOW(), 'sys', NOW());

INSERT INTO `t_role` VALUES ('1', 'admin', '超级管理员', 1, 'sys', NOW(), 'sys', NOW());

INSERT INTO `t_user` VALUES ('1', 'admin', '', '超级管理员', 'de59aef03ebe', '1', '0', '管理员', '1', 'ath@gmail.com', '123123123', '313213123', '0', 'fasfasfasfsa', ' ', '0', '2019-03-19 14:18:54', '超级管理员', '127.0.0.1', 'https://cdn.pixabay.com/photo/2018/07/12/22/25/fantasy-3534494__340.jpg', '1', '1');

INSERT INTO `t_user_principal` VALUES ('1', 'Main Principal', 1, 'admin', 'sys', NOW(), 'sys', NOW());

INSERT INTO t_role_resource (role_id, resource_id)
SELECT 1, id FROM t_resource  WHERE available=1;

INSERT INTO t_user_principal_resource (user_principal_id, resource_id, creator, created_time, updater, updated_time)
SELECT 1, id, 'sys', NOW(), 'sys', NOW() FROM t_resource  WHERE available=1;
