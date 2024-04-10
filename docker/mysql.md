## Mysql 主从复制

### 1、拉取 mysqll 8.0

``` powershell
$ docker pull mysql:8.0
```

### 2、创建容器

``` powershell
# 创建容器
$ docker run \
-itd \
--name mysql \
-p 3306:3306 \
-v /DockerData/mysql/conf:/etc/mysql/conf.d \
-v /DockerData/mysql/data:/var/lib/mysql \
-v /DockerData/mysql/log:/var/log/mysql \
-e MYSQL_ROOT_PASSWORD=123456	\
--restart always \
--privileged=true \
mysql8.0

# 设置 mysql 系统时间和物理机环境一致，需要设置 localtime
$ -v /etc/localtime:/etc/localtime

# 查看创建容器是否成功
$ docker ps -a

# 查看 mysql 日志
$ docker logs -f mysql

# 如果不随系统一起启动，则设置 --restart no，也可以在创建完容器再修改
$ docker update --restart=no mysql
```

### 3、登录

#### docker

``` powershell
# 登录 docker
$ docker exec -it mysql:/bin/bash
# 连接 mysql
$ mysql -u root -p
```

#### mycli

``` powershell
# 连接 mysql
$ mycli -u roo - p
```

### 4、修改密码

``` powershell
$ ALTER USER 'root'@'localhost' IDENTIFIED BY '123456';
$ ALTER USER 'root'@'%' IDENTIFIED BY '123456';
$ FLUSH PRIVILEGES;
```



## 主从同步

### 1、创建网络

``` powershell
# 创建一个主从mysql可以互相通信的网路
$ docker network create mysql-network
```

### 2、设置主机

``` powershell
$  docker run \                                                                                               
-itd \
--name mysql-master \
-p 3307:3306 \
-v /DockerData/mysql-master/conf:/etc/mysql/conf.d \
-v /DockerData/mysql-master/data:/var/lib/mysql \
-v /DockerData/mysql-master/log:/var/log/mysql \
-v /etc/localtime:/etc/localtime \
-e MYSQL_ROOT_PASSWORD=123456   \
--restart no \
--privileged=true \
--network mysql-network \
mysql
```

在 /DockerData/mysql-master/conf/my.cnf 中添加以下配置，并重启 mysql 服务使配置生效

``` tex
[mysqld]
server-id = 1
log_bin = mysql-bin
binlog_format = ROW

# 如果主服务器有多个网络接口，指定一个可访问的接口地址
# bind-address = 192.168.0.10
```

登录 mysql 控制台

``` powershell
# 先登录 mysql-master 容器
$ docker exec -it mysql-master /bin/bash 
# 登录 mysql 控制台
$ mysql -u root -p

# 还可以使用 IDE 、mycli、mysql-shell，此处演示一下 mycli
$ mycli -h 127.0.0.1 -u root -p -P 3307 
```

创建一个用于复制的专用用户，并赋予适当的权限。以下是示例命令

``` sql
CREATE USER 'replication.user'@'%' IDENTIFIED WITH mysql_native_password BY '123456';
GRANT REPLICATION SLAVE ON *.* TO 'replication.user'@'%';
FLUSH PRIVILEGES;
```

### 3、设置从机

``` powershell
$ docker run \                                                                                               
-itd \
--name mysql-slave-1 \
-p 3308:3306 \
-v /DockerData/mysql-master/conf:/etc/mysql/conf.d \
-v /DockerData/mysql-master/data:/var/lib/mysql \
-v /DockerData/mysql-master/log:/var/log/mysql \
-v /etc/localtime:/etc/localtime \
-e MYSQL_ROOT_PASSWORD=123456   \
--restart no \
--privileged=true \
--network mysql-network \
mysql
```

在 /DockerData/mysql-slave-1/conf/my.cnf 中添加以下配置，并重启 mysql 服务使配置生效

``` tex
[mysqld]
server-id = 2
log_bin = mysql-bin
binlog_format = ROW
relay_log = mysql-relay-bin
log_slave_updates = 1
read_only = 1

# 如果从服务器有多个网络接口，指定一个可访问的接口地址
# bind-address = 192.168.0.20
```

登录 mysql 控制台

``` powershell
# 先登录 mysql-slave-1 容器
$ docker exec -it mysql-slave-1 /bin/bash 
# 登录 mysql 控制台
$ mysql -u root -p

# 还可以使用 IDE 、mycli、mysql-shell，此处演示一下 mycli
$ mycli -h 127.0.0.1 -u root -p -P 3307
```

使用以下命令连接到主服务器

``` sql
CHANGE MASTER TO MASTER_HOST='172.19.0.2', MASTER_USER='replication.user', MASTER_PASSWORD='123456', MASTER_LOG_FILE='mysql-bin.000001', MASTER_LOG_POS=108;
```

``` powershell
# MASTER_HOST 通过如下命令查看主机 IP
$ docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' mysql-master

# MASTER_LOG_FILE 主服务器上二进制日志文件的名称，从 "SHOW MASTER STATUS;" 获取 
# MASTER_LOG_FILE = File + Position = mysql-bin.000001

$ mycli -h 127.0.0.1 -u root -p -P 3307 -e "SHOW MASTER STATUS;"

# MASTER_LOG_POS 需要从中开始复制的位置，从 "SHOW MASTER STATUS;" 获取 Binlog_Do_DB 值
$ mycli -h 127.0.0.1 -u root -p -P 3307 -e "SHOW MASTER STATUS;"

```

启动同步复制，并查看状态，在 `Slave_IO_Running` 和 `Slave_SQL_Running` 字段中，确保状态显示为 `Yes`。如果有任何错误，可以在该状态中获取相关信息。

``` sql
-- 启动
start slave;

-- 验证服务器状态
show slave status;

-- 需要停止同步命令
stop slave;
```

如果  `Slave_IO_Running` 和 `Slave_SQL_Running` 有一方显示 no， 请检查：

1、主从服务，更改配置后，是否重启服务，文件是否生效

2、主从数据库，表结构是否一致。一定要保证数据库一致后，再开启主从复制

3、主从复制，不可本末倒置，用从机进行写入操作，可引起主从复制问题

4、从机执行连接命令，发生异常，例如 IP、用户名、密码错误导致，可执行 `STOP REPLICA IO_THREAD FOR CHANNEL '';`

``` powershell
$ mycli -uroot -h 127.0.0.1 -P 3308 -e "STOP REPLICA IO_THREAD FOR CHANNEL '';"
```

说明：从机2或者更多操作相同

### 4、测试

在 master 上执行 `insert`、`update`、`delete` 操作，在 slave 上执行 `select` 查看结果是否同步完成。

## MGR

### 1、修改 my.cnf

``` tex
[mysqld]
server-id = 1
# 启用二进制日志
log_bin = mysql-bin
# 将执行的二进制日志事件也记录到自己的二进制日志中，会增加从库写负载和二进制文件大小
log_slave_updates = 1
# 设置binlog格式 STATEMENT(同步SQL脚本) / ROW(同步行数据) / MIXED(混合同步)
binlog_format = ROW
# 考虑到后期故障切换，增加slave的中继日志
relay-log = relay-log-bin

# 全局事务
gtid_mode = ON
# 强制GTID的一致性
enforce_gtid_consistency = ON
# 将master.info元数据保存在系统表中
master_info_repository = TABLE
# 将relay.info元数据保存在系统表中
relay_log_info_repository = TABLE
# 禁用二进制日志事件校验
binlog_checksum = NONE

# 记录事务的算法，官网建议使用 XXHASH64
transaction_write_set_extraction = XXHASH64
# 启动时加载group_replication插件
plugin_load_add='group_replication.so'
# GROUP的名字，是UUID值，可以使用select uuid()生成
loose-group_replication_group_name = '558edd3c-02ec-11ea-9bb3-080027e39bd2'
# 是否随服务器启动而自动启动组复制，不建议直接启动，怕故障恢复时有扰乱数据准确性的特殊情况
loose-group_replication_start_on_boot = OFF
# 本地MGR的IP地址和端口，host:port，是MGR的端口,不是数据库的端口
loose-group_replication_local_address = '172.24.0.2:33061'
# 需要接受本MGR实例控制的服务器IP地址和端口，是MGR的端口，不是数据库的端口
loose-group_replication_group_seeds = '172.24.0.2:33061,172.24.0.3:33061,172.24.0.4:33061'
# 开启引导模式，添加组成员，用于第一次搭建MGR或重建MGR的时候使用，只需要在集群内的其中一台开启
loose-group_replication_bootstrap_group = OFF
# 白名单
loose-group_replication_ip_whitelist = '172.24.0.0/16'
# 本机ip
report-host = '172.24.0.2'
# 本机port
report-port = 3306
```

说明：其他两台 mysql 复制此配置，只需要更改 server-id、loose-group_replication_local_address、report-host 这几个参数。

如果在配置了 plugin_load_add='group_replication.so' ，可以不用在 mysql 执行安装插件的sql

``` sql
#加载GR插件
install plugin group_replication soname 'group_replication.so';
show plugins;
```

### 2、启动主机

```sql
SET session sql_log_bin = 0;
CREATE USER 'replication.user'@'172.24.0.%' IDENTIFIED WITH mysql_native_password BY '123456';
GRANT REPLICATION SLAVE ON *.* TO 'replication.user'@'172.24.0.%';
FLUSH PRIVILEGES;
SET session sql_log_bin = 1;
CHANGE MASTER TO MASTER_USER='replication.user', MASTER_PASSWORD='123456' FOR CHANNEL 'group_replication_recovery';
SET global group_replication_bootstrap_group = ON;
START group_replication;
SET global group_replication_bootstrap_group = OFF;
```

注意：使用 8.0.35 版本，需要改用如下命令

``` sql
change replication source to source_user='repl', source_password='123456' for channel 'group_replication_recovery';
```

### 3、启动从机

``` sql
SET session sql_log_bin = 0;
CREATE USER 'replication.user'@'172.24.0.%' IDENTIFIED WITH mysql_native_password BY '123456';
GRANT REPLICATION SLAVE ON *.* TO 'replication.user'@'172.24.0.%';
FLUSH PRIVILEGES;
SET session sql_log_bin = 1;
CHANGE MASTER TO MASTER_USER='replication.user', MASTER_PASSWORD='123456' FOR CHANNEL 'group_replication_recovery';
START group_replication;
```

注意：如果之前已经设置了主从复制或者其他复制方式，在执行 START group_replication 遇到一场，需要执行如下命令，主要是解决RECOVERING 状态

``` sql
stop slave;
reset master;
reset slave all;
```

### 4、查看状态

``` sql
select * from performance_schema.replication_group_members;

+---------------------------+--------------------------------------+-------------+-------------+--------------+-------------+----------------+----------------------------+
| CHANNEL_NAME              | MEMBER_ID                            | MEMBER_HOST | MEMBER_PORT | MEMBER_STATE | MEMBER_ROLE | MEMBER_VERSION | MEMBER_COMMUNICATION_STACK |
+---------------------------+--------------------------------------+-------------+-------------+--------------+-------------+----------------+----------------------------+
| group_replication_applier | 78a416ba-f67d-11ee-bf4f-0242ac180002 | 172.24.0.2  | 3306        | ONLINE       | PRIMARY     | 8.1.0          | XCom                       |
| group_replication_applier | 815538df-f67d-11ee-bf53-0242ac180003 | 172.24.0.3  | 3306        | ONLINE       | SECONDARY   | 8.1.0          | XCom                       |
| group_replication_applier | 89f87e8d-f67d-11ee-beb4-0242ac180004 | 172.24.0.4  | 3306        | ONLINE       | SECONDARY   | 8.1.0          | XCom                       |
+---------------------------+--------------------------------------+-------------+-------------+--------------+-------------+----------------+----------------------------+
```

MEMBER_STATE 是 ONLINE 说明正常，如果出现 RECOVERING 参看 3

### 5、结合 ProxySql 使用

参看 [proxysql](proxysql.md)

