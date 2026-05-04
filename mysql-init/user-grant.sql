-- 创建应用程序使用的用户
CREATE USER IF NOT EXISTS 'yangzhijun'@'%' IDENTIFIED BY 'yangzhijun17771167448';

-- 授予所有非系统库权限
GRANT ALL PRIVILEGES ON *.* TO 'yangzhijun'@'%';

FLUSH PRIVILEGES;