### 准备

##### 设置卷位置

###### Mac

``` powershell
# docker卷目录
$ export DOCKER_HOME="$HOME/DockerData"
# mysql卷目录
$ export MYSQL_HOME="$DOCKER_HOME/mysql"
$ mkdir -p $MYSQL_HOME
```

######  Linux

``` powershell
$ export MYSQL_HOME="/srv/mysql"
```

<font color="#e83e8c">REDIS_HOME</font> 推荐附加设置在 shell 中

- bash	<font color="#e83e8c"> ~/.bash_profile</font>
- zsh       <font color="#e83e8c">~/.zshrc</font>

##### 容器使用主机装载的卷来存储持久数据

| 本地位置         | 容器位置          | 使用     |
| ---------------- | ----------------- | -------- |
| $MYSQL_HOME/conf | /etc/mysql/conf.d | 配置文件 |
| $MYSQL_HOME/data | /var/lib/mysql    | 数据     |
| $MYSQL_HOME/logs | /var/log/mysql    | 日志     |

说明：/etc/my.cnf 中有一条指令 `!includedir /etc/mysql/conf.d/ `

- `!includedir`: 这是 MySQL 特有的指令，表示要包含一个指定目录下的所有配置文件。

- `/etc/mysql/conf.d/`: 这是要包含的目录路径，通常用于存放额外的 MySQL 配置文件。在该目录下所有以 `.cnf` 结尾的文件都会被加载为附加的配置文件。

##### 目录结构

### 1、单机

###### 编写配置

``` powershell
[mysqld]
server-id = 1

# 启用二进制日志功能
log_bin = mysql-bin
# 指定错误日志文件的路径
log_error = "/var/log/mysql/error.log"
# 指定二进制日志中记录的内容限制为行级别的更改
binlog_format = ROW

# 指定将告警信息写入错误日志 0:否, 1:是, 默认:1
log_warnings = 1

# 指定数据文件的存储路径
datadir="/var/lib/mysql"

# 启用慢查询日志
slow_query_log = 1
# 指定查询执行时间超过多少秒才被记录到慢查询日志中（例如1表示超过1秒的查询）
long_query_time = 1
# 慢查询日志的输出方式为写入文件
log_output = FILE
# 指定慢查询日志文件的保存路径
slow_query_log_file = /var/log/mysql/slow-query.log

# 启用全局查询日志
general_log = 1
# 全局查询日志保存路径
general_log_file = /var/log/mysql/general-query.log
```

##### docker run

``` powershell
# 创建容器
$ docker run \
-itd \
--name mysql \
-p 3306:3306 \
-v /DockerData/mysql/conf:/etc/mysql/conf.d \
-v /DockerData/mysql/data:/var/lib/mysql \
-v /DockerData/mysql/log:/var/log/mysql \
-v /etc/localtime:/etc/localtime
-e MYSQL_ROOT_PASSWORD=123456	\
--restart always \
--privileged=true \
--network canary-net \
mysql8.0

# 如果不随系统一起启动，则设置 --restart no，也可以在创建完容器再修改
$ docker update --restart=no mysql
```

##### docker compose 

```powershell

```

##### 修改密码

``` sql
ALTER USER 'root'@'localhost' IDENTIFIED BY '123456';
ALTER USER 'root'@'%' IDENTIFIED BY '123456';
PRIVILEGES;
```

### 2、主从复制

| 名称    | IP         | 宿主端口 | 端口 | 备注   |
| ------- | ---------- | -------- | ---- | ------ |
| mysql-1 | 172.24.0.2 | 3306     | 3306 | master |
| mysql-2 | 172.24.0.3 | 3307     | 3306 | slave  |
| mysql-3 | 172.24.0.4 | 3308     | 3306 | slave  |

创建 mysql-1, mysql-2, mysql-3 的工作目录

``` powershell
$ mkdir -p $MYSQL_HOME/mysql{-1, -2, -3}/conf
```

- mysql-1

  编写配置

  ``` powershell
  [mysqld]
  server-id = 1
  
  # 启用二进制日志功能
  log_bin = mysql-bin
  # 指定错误日志文件的路径
  log_error = "/var/log/mysql/error.log"
  # 指定二进制日志中记录的内容限制为行级别的更改
  binlog_format = ROW
  
  # 指定将告警信息写入错误日志 0:否, 1:是, 默认:1
  log_warnings = 1
  
  # 指定数据文件的存储路径
  datadir="/var/lib/mysql"
  
  # 启用慢查询日志
  slow_query_log = 1
  # 指定查询执行时间超过多少秒才被记录到慢查询日志中（例如1表示超过1秒的查询）
  long_query_time = 1
  # 慢查询日志的输出方式为写入文件
  log_output = FILE
  # 指定慢查询日志文件的保存路径
  slow_query_log_file = /var/log/mysql/slow-query.log
  
  # 启用全局查询日志
  general_log = 1
  # 全局查询日志保存路径
  general_log_file = /var/log/mysql/general-query.log
  ```

  启动

  ```powershell
  $ docker run \                                                                                             
  -itd \
  --name mysql-1 \
  -p 3307:3306 \
  -v $MYSQL_HOME/mysql-1/conf:/etc/mysql/conf.d \
  -v $MYSQL_HOME/mysql-1/data:/var/lib/mysql \
  -v $MYSQL_HOME/mysql-1/log:/var/log/mysql \
  -v /etc/localtime:/etc/localtime \
  -e MYSQL_ROOT_PASSWORD=123456   \
  --restart no \
  --privileged=true \
  --network canary-net \
  mysql
  ```

  创建一个用于复制的专用用户，并赋予适当的权限

  ``` sql
  CREATE USER 'replication.user'@'%' IDENTIFIED WITH mysql_native_password BY '123456';
  GRANT REPLICATION SLAVE ON *.* TO 'replication.user'@'%';
  FLUSH PRIVILEGES;
  ```

- mysql-2

  编写配置

  ``` powershell
  [mysqld]
  server-id = 2
  
  # 启用二进制日志功能
  log_bin = mysql-bin
  # 指定错误日志文件的路径
  log_error = "/var/log/mysql/error.log"
  # 指定二进制日志中记录的内容限制为行级别的更改
  binlog_format = ROW
  
  # 指定将告警信息写入错误日志 0:否, 1:是, 默认:1
  log_warnings = 1
  
  # 指定数据文件的存储路径
  datadir="/var/lib/mysql"
  
  # 启用慢查询日志
  slow_query_log = 1
  # 指定查询执行时间超过多少秒才被记录到慢查询日志中（例如1表示超过1秒的查询）
  long_query_time = 1
  # 慢查询日志的输出方式为写入文件
  log_output = FILE
  # 指定慢查询日志文件的保存路径
  slow_query_log_file = /var/log/mysql/slow-query.log
  
  # 启用全局查询日志
  general_log = 1
  # 全局查询日志保存路径
  general_log_file = /var/log/mysql/general-query.log
  
  relay_log = mysql-relay-bin
  log_slave_updates = 1
  read_only = 1
  ```

  启动

  ```powershell
  $ docker run \                                                                                             
  -itd \
  --name mysql-2 \
  -p 3307:3306 \
  -v $MYSQL_HOME/mysql-2/conf:/etc/mysql/conf.d \
  -v $MYSQL_HOME/mysql-2/data:/var/lib/mysql \
  -v $MYSQL_HOME/mysql-2/log:/var/log/mysql \
  -v /etc/localtime:/etc/localtime \
  -e MYSQL_ROOT_PASSWORD=123456   \
  --restart no \
  --privileged=true \
  --network canary-net \
  mysql
  ```

  查看 `master` 的 `IP` 地址，

  ```powershell
  $ docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' mysql-1
  172.24.0.2
  ```

  查看 `master` 的 `File` 和 `Position`

  ``` powershell
  $ mycli -h 127.0.0.1 -P 3307 -u root -e "SHOW MASTER STATUS;"
  Password:
  File    Position        Binlog_Do_DB    Binlog_Ignore_DB        Executed_Gtid_Set
  mysql-bin.000001        108                    558edd3c-02ec-11ea-9bb3-080027e39bd2:1-7
  ```

  登录 `slave` ，启动复制，并查看状态

  ``` sql
  CHANGE MASTER TO MASTER_HOST='172.19.0.2', MASTER_USER='replication.user', MASTER_PASSWORD='123456', MASTER_LOG_FILE='mysql-bin.000001', MASTER_LOG_POS=108;
  
  -- 启动
  start slave;
  
  -- 验证服务器状态
  show slave status;
  
  -- 停止
  stop slave;
  ```

  如果  `Slave_IO_Running` 和 `Slave_SQL_Running` 有一方显示 no， 请检查：

  1、主从服务，更改配置后，是否重启服务，文件是否生效

  2、主从数据库，表结构是否一致。一定要保证数据库一致后，再开启主从复制

  3、主从复制，不可本末倒置，用从机进行写入操作，可引起主从复制问题

  4、从机执行连接命令，发生异常，例如 IP、用户名、密码错误导致，可执行 `STOP REPLICA IO_THREAD FOR CHANNEL '';`

  ``` powershell
  $ mycli -h 127.0.0.1 -P 3307 -u root -e "STOP REPLICA IO_THREAD FOR CHANNEL '';"
  ```

- mysql-3

  **两台 `slave` 操作相同，此处省略 `mysql-2` 的操作**

- 测试

  在 `master` 上执行 `insert`、`update`、`delete` 操作，在 `slave` 上执行 `select` 查看结果是否同步完成。

### 3、MGR

查看 `mysql-1`，`mysql-2`，` mysql-3` 的 `IP`

``` powershell
$ docker ps -q --filter "name=mysql" | xargs docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'
172.24.0.2
172.24.0.3
172.24.0.4
```

```powershell
$ docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $(docker ps -q --filter "name=mysql")
172.24.0.2
172.24.0.3
172.24.0.4
```

查看 `canary-net` 网络的 `Gateway` 和 `Subnet`

```powershell
$ docker network inspect canary-net | jq -r '.[0].IPAM.Config.[0].Gateway'
172.24.0.1
$ docker network inspect canary-net | jq -r '.[0].IPAM.Config.[0].Subnet'
172.24.0.0/16
```

- mysql-1

  编辑配置

  ``` powershell
  [mysqld]
  ······ 
  # 此处省略了 mysql 的配置，把下面的配置 copy 到 my.cnf
  
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

  如果配置了 `plugin_load_add='group_replication.so'` ，可以省略此操作

  ``` sql
  -- 安装插件
  install plugin group_replication soname 'group_replication.so';
  show plugins;
  ```

  登录 `master` 开启 `group_replication`

  ``` sql
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

  注意：使用 `8.0.35` 版本，需要改用如下命令

  ``` sql
  change replication source to source_user='repl', source_password='123456' for channel 'group_replication_recovery';
  ```

- mysql-2

  编辑配置

  ```powershell
  [mysqld]
  ······ 
  # 此处省略了 mysql 的配置，把下面的配置 copy 到 my.cnf
  
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
  loose-group_replication_local_address = '172.24.0.3:33061'
  # 需要接受本MGR实例控制的服务器IP地址和端口，是MGR的端口，不是数据库的端口
  loose-group_replication_group_seeds = '172.24.0.2:33061,172.24.0.3:33061,172.24.0.4:33061'
  # 开启引导模式，添加组成员，用于第一次搭建MGR或重建MGR的时候使用，只需要在集群内的其中一台开启
  loose-group_replication_bootstrap_group = OFF
  # 白名单
  loose-group_replication_ip_whitelist = '172.24.0.0/16'
  # 本机ip
  report-host = '172.24.0.3'
  # 本机port
  report-port = 3306
  ```

  登录 `slave` 开启 `group_replication`

  ``` sql
  SET session sql_log_bin = 0;
  CREATE USER 'replication.user'@'172.24.0.%' IDENTIFIED WITH mysql_native_password BY '123456';
  GRANT REPLICATION SLAVE ON *.* TO 'replication.user'@'172.24.0.%';
  FLUSH PRIVILEGES;
  SET session sql_log_bin = 1;
  CHANGE MASTER TO MASTER_USER='replication.user', MASTER_PASSWORD='123456' FOR CHANNEL 'group_replication_recovery';
  START group_replication;
  ```

- mysql-3

  编辑配置

  ```powershell
  [mysqld]
  ······ 
  # 此处省略了 mysql 的配置，把下面的配置 copy 到 my.cnf
  
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
  loose-group_replication_local_address = '172.24.0.4:33061'
  # 需要接受本MGR实例控制的服务器IP地址和端口，是MGR的端口，不是数据库的端口
  loose-group_replication_group_seeds = '172.24.0.2:33061,172.24.0.3:33061,172.24.0.4:33061'
  # 开启引导模式，添加组成员，用于第一次搭建MGR或重建MGR的时候使用，只需要在集群内的其中一台开启
  loose-group_replication_bootstrap_group = OFF
  # 白名单
  loose-group_replication_ip_whitelist = '172.24.0.0/16'
  # 本机ip
  report-host = '172.24.0.4'
  # 本机port
  report-port = 3306
  ```

  登录 `slave` 开启 `group_replication`

  ``` sql
  SET session sql_log_bin = 0;
  CREATE USER 'replication.user'@'172.24.0.%' IDENTIFIED WITH mysql_native_password BY '123456';
  GRANT REPLICATION SLAVE ON *.* TO 'replication.user'@'172.24.0.%';
  FLUSH PRIVILEGES;
  SET session sql_log_bin = 1;
  CHANGE MASTER TO MASTER_USER='replication.user', MASTER_PASSWORD='123456' FOR CHANNEL 'group_replication_recovery';
  START group_replication;
  ```

- 查看状态

  ```sql
  select * from performance_schema.replication_group_members;
  
  +---------------------------+--------------------------------------+-------------+-------------+--------------+-------------+----------------+----------------------------+
  | CHANNEL_NAME              | MEMBER_ID                            | MEMBER_HOST | MEMBER_PORT | MEMBER_STATE | MEMBER_ROLE | MEMBER_VERSION | MEMBER_COMMUNICATION_STACK |
  +---------------------------+--------------------------------------+-------------+-------------+--------------+-------------+----------------+----------------------------+
  | group_replication_applier | 78a416ba-f67d-11ee-bf4f-0242ac180002 | 172.24.0.2  | 3306        | ONLINE       | PRIMARY     | 8.1.0          | XCom                       |
  | group_replication_applier | 815538df-f67d-11ee-bf53-0242ac180003 | 172.24.0.3  | 3306        | ONLINE       | SECONDARY   | 8.1.0          | XCom                       |
  | group_replication_applier | 89f87e8d-f67d-11ee-beb4-0242ac180004 | 172.24.0.4  | 3306        | ONLINE       | SECONDARY   | 8.1.0          | XCom                       |
  +---------------------------+--------------------------------------+-------------+-------------+--------------+-------------+----------------+----------------------------+
  ```

  - `ONLINE`：在线

  - `RECOVERING`：发生了 `启动从服务器` 、`复制中断或损坏` 、`故障恢复` 等情况，需要在 `master`、`slave` 执行如下指令再开启 

    ``` sql
    stop slave;
    reset master;
    reset slave all;
    ```


