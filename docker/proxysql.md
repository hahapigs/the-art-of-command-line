

## proxysql 读写分离

首先准备好三台 mysql 集群和一台 porxysql 服务

| 服务器     | 端口                                        | 备注    |
| ---------- | ------------------------------------------- | ------- |
| 172.19.0.2 | 3306（3306）                                | 主      |
| 172.19.0.3 | 3306（3307）                                | 从      |
| 172.19.0.4 | 3306（3308）                                | 从      |
| 172.19.0.5 | 6032（16032）、6033（16033）、6070（16070） | proxsql |

#### 准备

``` sql
-- 先在 172.19.0.2/3/4 上创建两个用户，如果已经开启同步复制（不限制数据库），只需要在主服务器上执行，就会自动同步从服务器
create user 'proxy.monitor'@'172.19.0.%' identified by '123456';
grant replication client on *.* to 'proxy.monitor'@'172.19.0.%';
create user 'proxy.admin'@'%' identified by 'Pass!234';
grant all privileges on *.* to 'proxy.admin'@'%' with grant option;

```



#### Step1：启动服务

```powershell
$ docker search proxysql
$ docker pull proxysql/proxysql
# 如果使用自定义配置，则配置 proxysql.cnf
$ docker run -p 16032:6032 -p 16033:6033 -p 16070:6070 --name=proxysql --network canary-network -d -v /DockerData/proxysql/proxysql.cnf:/etc/proxysql.cnf proxysql/proxysql
```

proxysql.cnf

``` tex
datadir="/var/lib/proxysql"
admin_variables=
{
    admin_credentials="admin:admin;radmin:radmin"   ##管理端账号密码
    mysql_ifaces="0.0.0.0:6032"
}
mysql_variables=
{
    threads=4
    max_connections=2048
    default_query_delay=0
    default_query_timeout=36000000
    have_compress=true
    poll_timeout=2000
    interfaces="0.0.0.0:6033"
    default_schema="information_schema"
    stacksize=1048576
    server_version="8.0.4"
    connect_timeout_server=3000
    monitor_username="proxy.monitor" ##监控账号
    monitor_password="123456" ##监控密码
    monitor_history=600000
    monitor_connect_interval=60000
    monitor_ping_interval=10000
    monitor_read_only_interval=1500
    monitor_read_only_timeout=500
    ping_interval_server_msec=120000
    ping_timeout_server=500
    commands_stats=true
    sessions_sort=true
    connect_retries_on_failure=10
}
```

#### Step2：主从分组信息

```powershell
# 物理机登录
$ mysql -h127.0.0.1 -P16032 -uradmin -p --prompt "ProxySQL Admin>"                                                                                                                           
# 进入mysql服务后登录
$ docker exec -it proxysql /bin/bash
$ mysql -h172.19.0.5 -P6032 -uradmin -p --prompt "ProxySQL Admin>"   
```

```sql
# 查看 proxysql 主从分组信息表结构
show create table mysql_replication_hostgroups\G;
```

writer_hostgroup` 和`reader_hostgroup` 写组和读组都要大于0且不能相同，我的环境下，`写组定义与10，读组定义为20

``` sql
insert into mysql_replication_hostgroups(writer_hostgroup,reader_hostgroup,check_type, COMMENT) values(10, 20,'read_only', 'proxy');
load mysql servers to runtime;
save mysql servers to disk;
select * from mysql_replication_hostgroups;
use main;
select * from mysql_replication_hostgroups;
select * from runtime_mysql_replication_hostgroups;
use disk;
select * from mysql_replication_hostgroups;
```

#### Step3：添加节点服务器

``` sql
insert into mysql_servers(hostgroup_id,hostname,port)  values(10,'172.19.0.2',3306);
insert into mysql_servers(hostgroup_id,hostname,port)  values(20,'172.19.0.3',3306);
insert into mysql_servers(hostgroup_id,hostname,port)  values(20,'172.19.0.4',3306);
load mysql servers to runtime;
save mysql servers to disk;
select * from mysql_servers;
```

#### Step4：配置监控账号

```sql
UPDATE global_variables SET variable_value='proxy.monitor' WHERE variable_name='mysql-monitor_username';
UPDATE global_variables SET variable_value='123456' WHERE variable_name='mysql-monitor_password';
load mysql variables to runtime;
save mysql variables to disk;
select * from monitor.mysql_server_connect_log;
select * from mysql_server_ping_log limit 10;
select * from mysql_server_read_only_log limit 10;
```

#### Step5：配置对外访问账号

``` sql
show create table mysql_users\G;
insert into mysql_users(username,password,default_hostgroup) values('proxy.admin','123456',10);
load mysql users to runtime;
save mysql users to disk;
select * from mysql_users\G;
```

#### Step6：读写分离策略规则

``` sql
INSERT INTO mysql_query_rules (rule_id, active, match_pattern, destination_hostgroup, apply)
VALUES (1, 1, '^select', 20, 1),
       (2, 1, '^select.*for update$', 10, 1);
load mysql query rules to runtime;
save mysql query rules to disk;
```

如果想在 ProxySQL 中查看SQL请求路由信息stats_mysql_query_digest

```sql
select hostgroup,schemaname,username,digest_text,count_star from  stats_mysql_query_digest;
```

#### 调配权重

```sql
update mysql_servers set weight=10 hostname='172.19.0.4';
load mysql servers to runtime;
save mysql servers to disk;
```

#### Step7：测试

``` powershell
$ mysql -h127.0.0.1 -P16033 -uproxy.admin -p -e "select @@server_id";
```

#### SpringBoot

``` yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:16033/canary?characterEncoding=utf8&useLegacyDatetimeCode=false&serverTimezone=GMT%2b8
    username: proxy.admin
    password: 123456
```

问题：如果 springboot 启动发现 SQLException: Unknown system variable query_cache_size 异常，则修改 proxysql.cnf 中的 server_version 配置。因为 mysql 8.0 以后取消了 query_cache_size 参数

``` tex
# 修改完重启 proxysql 服务
server_version="8.0.4"
```

也可以在 proxysql 中使用 sql 语句修改

``` sql
update global_variables set variable_value="8.0.4 (ProxySQL)" where variable_name='mysql-server_version';
load mysql variables to run;
save mysql variables to disk;
```

