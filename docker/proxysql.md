### 准备

##### 设置卷位置

###### Mac

``` powershell
# docker卷目录
$ export DOCKER_HOME="$HOME/DockerData"
# proxysql卷目录
$ export PROXYSQL_HOME="$DOCKER_HOME/proxysql"
$ mkdir -p $PROXYSQL_HOME
```

######  Linux

``` powershell
$ export PROXYSQL_HOME="/srv/proxysql"
```

<font color="#e83e8c">REDIS_HOME</font> 推荐附加设置在 shell 中

- bash	<font color="#e83e8c"> ~/.bash_profile</font>
- zsh       <font color="#e83e8c">~/.zshrc</font>

##### 容器使用主机装载的卷来存储持久数据

| 本地位置                         | 容器位置          | 使用     |
| -------------------------------- | ----------------- | -------- |
| $PROXYSQL_HOME/conf/proxysql.cnf | /etc/proxysql.cnf | 配置文件 |
| $PROXYSQL_HOME/data              | /var/lib/proxysql | 数据     |
| $PROXYSQL_HOME/logs              | /var/log/proxysql | 日志     |

##### 目录结构

├── conf
│   └── proxysql.cnf
├── data
│   ├── proxysql-ca.pem
│   ├── proxysql-cert.pem
│   ├── proxysql-key.pem
│   ├── proxysql.db
│   └── proxysql_stats.db
└── logs
    └── proxysql.log

### 1、单机（standalone）

| 服务器   | IP         | 宿主端口           | 端口             | 备注     |
| -------- | ---------- | ------------------ | ---------------- | -------- |
| mysql-1  | 172.19.0.2 | 3306               | 3306             | master   |
| mysql-2  | 172.19.0.3 | 3307               | 3306             | slave    |
| mysql-3  | 172.19.0.4 | 3308               | 3306             | slave    |
| proxysql | 172.19.0.5 | 1632、16033、16070 | 6032、6033、6070 | proxysql |

###### 编写配置

``` powershell
datadir="/var/lib/proxysql"
errorlog="/var/log/proxysql/proxysql.log"

admin_variables=
{
    admin_credentials="admin:admin;radmin:radmin"   	# 管理端账号密码
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
    monitor_username="proxy.monitor" 						# 监控账号
    monitor_password="123456" 			 						# 监控密码
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

##### docker run

``` powershell
$ docker run \
-itd \
--name proxysql \
-p 16032:6032 \
-p 16033:6033 \
-p 16070:6070 \
-v $PROXYSQL_HOME/conf/proxysql.cnf:/etc/proxysql.cnf \
-v $PROXYSQL_HOME/data:/var/lib/proxysql \
-v $PROXYSQL_HOME/logs:/var/log/proxysql \
--restart no \
--privileged=true \
--network canary-net \
proxysql/proxysql
```

##### docker compose

```yaml

```

##### Step1：创建监测和管理用户

先在 `mysql-1`，`mysql-2`，`mysql-3` 上创建两个用户，如果已经开启同步复制（不限制数据库），只需要在主服务器上执行，就会自动同步从服务器

``` sql
create user 'proxy.monitor'@'172.19.0.%' identified by '123456';
grant replication client on *.* to 'proxy.monitor'@'172.19.0.%';
create user 'proxy.admin'@'%' identified by 'Pass!234';
grant all privileges on *.* to 'proxy.admin'@'%' with grant option;
```

##### Step2：主从分组信息

登录 proxysql

```powershell
# 宿主机登录
$ mysql -h127.0.0.1 -P16032 -uradmin -p --prompt "ProxySQL Admin>"                                                                                                                           
# 进入mysql服务后登录
$ docker exec -it proxysql /bin/bash
$ mysql -h172.19.0.5 -P6032 -uradmin -p --prompt "ProxySQL Admin>"   
```

查看 proxysql 主从分组信息表结构

```sql
ProxySQL Admin> show create table mysql_replication_hostgroups\G;
```

- 主从读写分离规则

``` sql
insert into mysql_replication_hostgroups(writer_hostgroup,reader_hostgroup,check_type, COMMENT) values(10, 30,'read_only', 'proxy');
load mysql servers to runtime;
save mysql servers to disk;
select * from mysql_replication_hostgroups;
use main;
select * from mysql_replication_hostgroups;
select * from runtime_mysql_replication_hostgroups;
use disk;
select * from mysql_replication_hostgroups;
```

说明：`writer_hostgroup` 和 ` reader_hostgroup` 写组和读组都要大于 0 且不能相同，我的环境下，写组定义为 10，读组定义为 30

- MGR 高可用读写分离规则

``` sql
INSERT INTO mysql_group_replication_hostgroups (writer_hostgroup, backup_writer_hostgroup, reader_hostgroup, offline_hostgroup, active, max_writers, writer_is_also_reader, max_transactions_behind, COMMENT)
values(10, 20, 30, 40, 1, 1, 1, 100, 'mgr_cluster_01');
load mysql servers to runtime;
save mysql servers to disk;

-- 查看复制方案请执行如下sql
show tables like 'mysql%hostgroups';
```

##### Step3：添加节点服务器

``` sql
insert into mysql_servers(hostgroup_id,hostname,port)  values(10,'172.19.0.2',3306);
insert into mysql_servers(hostgroup_id,hostname,port)  values(20,'172.19.0.3',3306);
insert into mysql_servers(hostgroup_id,hostname,port)  values(20,'172.19.0.4',3306);
load mysql servers to runtime;
save mysql servers to disk;
select * from mysql_servers;
```

##### Step4：配置监控账号

```sql
UPDATE global_variables SET variable_value='proxy.monitor' WHERE variable_name='mysql-monitor_username';
UPDATE global_variables SET variable_value='123456' WHERE variable_name='mysql-monitor_password';
load mysql variables to runtime;
save mysql variables to disk;
select * from monitor.mysql_server_connect_log;
select * from mysql_server_ping_log limit 10;
select * from mysql_server_read_only_log limit 10;
```

##### Step5：配置对外访问账号

``` sql
show create table mysql_users\G;
insert into mysql_users(username,password,default_hostgroup) values('proxy.admin','123456',10);
load mysql users to runtime;
save mysql users to disk;
select * from mysql_users\G;
```

##### Step6：读写分离策略规则

``` sql
INSERT INTO mysql_query_rules (rule_id, active, match_pattern, destination_hostgroup, apply)
VALUES (1, 1, '^select', 20, 1),
       (2, 1, '^select.*for update$', 10, 1);
load mysql query rules to runtime;
save mysql query rules to disk;
```

如果想在 `ProxySQL` 中查看 SQL 请求路由信息 `stats_mysql_query_digest`

```sql
select hostgroup,schemaname,username,digest_text,count_star from  stats_mysql_query_digest;
```

###### 调配权重

```sql
update mysql_servers set weight=10 hostname='172.19.0.4';
load mysql servers to runtime;
save mysql servers to disk;
```

##### Step7：测试

``` powershell
$ mysql -h 127.0.0.1 -P 16033 -uproxy.admin -p -e "select @@server_id";
$ mysql -h 127.0.0.1 -P 16033 -uproxy.admin -p -e "select @@hostname;"
```

##### SpringBoot

``` yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:16033/canary?characterEncoding=utf8&useLegacyDatetimeCode=false&serverTimezone=GMT%2b8
    username: proxy.admin
    password: 123456
```

问题：如果 `springboot` 启动发现` SQLException: Unknown system variable query_cache_size` 异常，则修改 `proxysql.cnf `中的 `server_version` 配置。因为` mysql 8.0` 以后取消了 `query_cache_size` 参数

``` tex
# 修改完重启 proxysql 服务
server_version="8.0.4"
```

也可以在 `proxysql` 中使用 `sql` 语句修改

``` sql
update global_variables set variable_value="8.0.4 (ProxySQL)" where variable_name='mysql-server_version';
load mysql variables to run;
save mysql variables to disk;
```

### 2、ProxySql Cluster

| 服务器     | IP         | 宿主端口           | 端口             | 备注       |
| ---------- | ---------- | ------------------ | ---------------- | ---------- |
| mysql-1    | 172.19.0.2 | 3306               | 3306             | master     |
| mysql-2    | 172.19.0.3 | 3307               | 3306             | slave      |
| mysql-3    | 172.19.0.4 | 3308               | 3306             | slave      |
| proxysql-1 | 172.19.0.5 | 1632、16033、16070 | 6032、6033、6070 | proxysql-1 |
| proxysql-2 | 172.19.0.6 | 2632、26033、26070 | 6032、6033、6070 | proxysql-2 |

创建 `proxysql-1`, `proxysql-2` 的工作目录

``` powershell
$ mkdir -p $PROXYSQL_HOME/proxysql{-1, -2}/conf
```

- proxysql-1

  先把单机 `proxysql.cnf` 配置拷贝用来启动 `proxysql-1`，启动成功后再修改为集群配置

  ``` powershell
  $ docker run \
  -itd \
  --name proxysql-1 \
  -p 16032:6032 \
  -p 16033:6033 \
  -p 16070:6070 \
  -v $PROXYSQL_HOME/proxysql-1/conf/proxysql.cnf:/etc/proxysql.cnf \
  -v $PROXYSQL_HOME/proxysql-1/data:/var/lib/proxysql \
  -v $PROXYSQL_HOME/proxysql-1/logs:/var/log/proxysql \
  --restart no \
  --privileged=true \
  --network canary-net \
  proxysql/proxysql
  ```

- proxysql-2

  先把单机 `proxysql.cnf` 配置拷贝用来启动 `proxysql-2`，启动成功后再修改为集群配置

  ``` powershell
  $ docker run \
  -itd \
  --name proxysql-2 \
  -p 26032:6032 \
  -p 26033:6033 \
  -p 26070:6070 \
  -v $PROXYSQL_HOME/proxysql-2/conf/proxysql.cnf:/etc/proxysql.cnf \
  -v $PROXYSQL_HOME/proxysql-2/data:/var/lib/proxysql \
  -v $PROXYSQL_HOME/proxysql-2/logs:/var/log/proxysql \
  --restart no \
  --privileged=true \
  --network canary-net \
  proxysql/proxysql
  ```

启动后，监测是否正常启动

``` powershell
$ docker ps -q --filter "name=proxysql"
```

说明：因为 `docker` 构建的容器无法预知 `IP`，所以先构建两个独立的 `proxysql` 容器，待启动成功后再修改为集群的配置。

查看 `proxysql-1`，`proxysql-2` 的 `IP`，下面两条指令效果相同

```powershell
$ docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $(docker ps -q --filter "name=proxysql")
172.24.0.5
172.24.0.6
```

```powershell
$ docker ps -q --filter "name=proxysql" | xargs docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'
172.24.0.5
172.24.0.6
```

###### 修改配置

``` powershell
datadir="/var/lib/proxysql"
errorlog="/var/log/proxysql/proxysql.log"

admin_variables=
{
	admin_credentials="admin:admin;radmin:radmin"   # 管理端账号密码
	mysql_ifaces="0.0.0.0:6032"
	cluster_username="radmin"					# 集群用户名称,与admin_credentials中配置的相同
	cluster_password="radmin"        	# 集群用户密码,与admin_credentials中配置的相同
	cluster_check_interval_ms=200
	cluster_check_status_frequency=100
	cluster_mysql_query_rules_save_to_disk=true
	cluster_mysql_servers_save_to_disk=true
	cluster_mysql_users_save_to_disk=true
	cluster_proxysql_servers_save_to_disk=true
	cluster_mysql_query_rules_diffs_before_sync=3
	cluster_mysql_servers_diffs_before_sync=3
	cluster_mysql_users_diffs_before_sync=3
	cluster_proxysql_servers_diffs_before_sync=3
}

mysql_variables=
{
	threads=4
	max_connections=2048
	default_query_delay=0
	default_query_timeout=36000000
	have_compress=true
	poll_timeout=2000
	interfaces="0.0.0.0:6033"                       # 代理请求端口
	default_schema="information_schema"
	stacksize=1048576
	server_version="8.0.4"                          # 指定数据库版本
	connect_timeout_server=3000
	monitor_username="proxy.monitor"                # 监控账号
	monitor_password="123456"            						# 监控密码
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

proxysql_servers =
(
	{
		hostname="172.24.0.5"
		port=6032
		weight=1
		comment="proxysql-1"
	},
	{
		hostname="172.24.0.6"
		port=6032
		weight=1
		comment="proxysql-2"
	}
)
```

将修改后的配置分别 `copy` 到 `proxysql-1`，`proxysql-2` 的 `conf` 文件夹

``` powershell
##### 利用 MacOS pbcopy 和 pbpaste 处理，其他系统请逐一拷贝
# 拷贝进剪切板
$ pbcopy < proxysql.cnf
# 粘贴文件到 proxysql-1/conf/proxysql.cnf，redis-2/conf/proxysql.cnf，redis3/conf/proxysql.cnf
$ pbpaste > $PROXYSQL_HOME/proxysql{-1, -2}/conf/sentinel.cnf
```

然后删除旧 `proxysql.db` 数据<font color="red">（非常重要）</font>，如果不删除配置不会生效

``` powershell
$ rmdir $PROXYSQL_HOME/proxysql{-1, -2}/data/
```

重新启动 `proxysql-1`， `proxysql-2`

``` powershell
$ docker restart `docker ps -a | grep proxysql | awk -F " " '{print $1}' `
```

**说明：在 `proxysql-1` 执行 `step1~7`，`proxysql-2` 会自动拉取配置**

