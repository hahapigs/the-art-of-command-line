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
$ docker edit --restart=no mysql
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
$ ALTER USER 'root'@'localhost' IDENTIFIED BY 'your_new_password';
$ ALTER USER 'root'@'%' IDENTIFIED BY 'your_new_password';
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
CREATE USER 'replication_user'@'%' IDENTIFIED WITH mysql_native_password BY 'password';
GRANT REPLICATION SLAVE ON *.* TO 'replication_user'@'%';
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
# 先登录 mysql-master 容器
$ docker exec -it mysql-slave-1 /bin/bash 
# 登录 mysql 控制台
$ mysql -u root -p

# 还可以使用 IDE 、mycli、mysql-shell，此处演示一下 mycli
$ mycli -h 127.0.0.1 -u root -p -P 3307
```

使用以下命令连接到主服务器

``` sql
CHANGE MASTER TO MASTER_HOST='master_host', MASTER_USER='replication_user', MASTER_PASSWORD='123456', MASTER_LOG_FILE='filename', MASTER_LOG_POS=log_position;
```

``` powershell
# MASTER_HOST 通过如下命令查看主机 IP
$ docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' mysql-master

# MASTER_USER 主机创建的用于复制的专用用户

# MASTER_PASSWORD 专用用户的密码

# MASTER_LOG_FILE 主服务器上二进制日志文件的名称，从 "SHOW MASTER STATUS;" 获取 
# MASTER_LOG_FILE = File + Position = mysql-bin.000001
$ mycli -h 127.0.0.1 -u root -p -P 3307 -e "SHOW MASTER STATUS;"

# MASTER_LOG_POS 需要从中开始复制的位置，从 "SHOW MASTER STATUS;" 获取 Binlog_Do_DB 值
$ mycli -h 127.0.0.1 -u root -p -P 3307 -e "SHOW MASTER STATUS;"

```

启动同步复制l，并查看状态，在 `Slave_IO_Running` 和 `Slave_SQL_Running` 字段中，确保状态显示为 `Yes`。如果有任何错误，可以在该状态中获取相关信息。

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

### 4、测试

在 master 上执行 `insert`、`update`、`delete` 操作，在 slave 上执行 `select` 操作。