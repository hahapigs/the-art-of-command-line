

#### Linux 二进制部署安装

```powershell
# download MySQL Community Server 或者在 linux server 上使用 wget 方式下载
$ wget https://dev.mysql.com/get/Downloads/MySQL-8.0/mysql-8.0.16-linux-glibc2.12-x86_64.tar.xz 

# 解压到指定目录
$ tar -xvf mysql-8.0.16-linux-glibc2.12-x86_64.tar.xz -C /usr/local

# 重命名
$ mv mysql-8.0.16-linux-glibc2.12-x86_64 mysql-8.0.16

# 在mysql创建初始化数据目录
$ cd /usr/local/mysql-8.0.16
$ mkdir data
$ mkdir logs
$ mkdir etc
# 创建 mysql.log 文件，以备 my.cnf 配置 log-error 目录
$ cd /usr/local/mysql-8.0.16/logs
$ touch mysql.log

# 创建mysql用户
$ goupadd mysql
$ useradd mysql -g mysql
$ chown -R mysql.mysql /usr/local/mysql-8.0.16

# 添加配置文件，也可以在 /etc/my.cnf 文件中配置
$ cd /usr/local/etc
$ touch my.cnf
# 复制 my.cnf 内容到此文件中
[mysqld]
datadir=/usr/local/mysql-8.0.16/data
socket=/tmp/mysql.sock
log-error=/usr/local/mysql-8.0.16/logs/mysql.log
pid-file=/usr/local/mysql-8.0.16/logs/mysql.pid


# 初始化数据, --initialize-insecure 使用无密码，无安全策略方式
$ /usr/local/mysql-8.0.16/bin/mysqld --initialize-insecure --user=mysql --basedir=/usr/local/mysql-8.0.16 --datadir=/usr/local/mysql-8.0.16/data

# 如果使用 --initialize 带密码安全策略方式，则在 logs/mysql.log 日志下查看临时密码

# 启动 mysql 服务
$ /usr/local/mysql-8.0.16/mysql.server start
# 或者
$ cp -a /usr/local/mysql-8.0.16/support-files/mysql.server /etc/init.d/mysqld
$ /etc/init.d/mysqld start

# 登录 mysql
$ ./usr/local/mysql-8.0.16/bin/mysql
# 如果mysql已经存在 /usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin 中，则直接可以使用 mysql 命令
# 如果没有，需要手动添加mysql环境变量到 etc/profile 或者 ~/.bash_profile
$  echo 'export PATH=/usr/local/mysql-8.0.16/bin:$PATH' >> /etc/profile

# 修改 root 随机密码
mysql > alter user root@localhost identified by '123456';

# 测试密码是否生效
$ mysql -uroot -p123456 -e "select @@version;" 2>/dev/null


```

#### 启动方式

```powershell
# 第一种
$ /usr/local/mysql-8.0.16/mysql.server start
$ /usr/local/mysql-8.0.16/mysql.server stop
$ /usr/local/mysql-8.0.16/mysql.server restart
# 第二种
$ /etc/init.d/mysqld start
$ /etc/init.d/mysqld stop
$ /etc/init.d/mysqld restart
# 第三种
$ service mysqld start
$ service mysqld stop
$ service mysqld restart
```

#### 开机启动

```powershell
$ vim /etc/systemd/system/mysqld.service
[Unit]
Description=MySQL Server
Documentation=man:mysqld(8)
Documentation=http://dev.mysql.com/doc/refman/en/using-systemd.html
After=network.target
After=syslog.target
[Install]
WantedBy=multi-user.target
[Service]
User=mysql
Group=mysql
ExecStart=/usr/local/mysql-8.0.16/bin/mysqld --defaults-file=/etc/my.cnf
LimitNOFILE = 5000

[root@mysql ~]# systemctl start mysqld.service
[root@mysql ~]# systemctl enable mysqld.service
Created symlink from /etc/systemd/system/multi-user.target.wants/mysqld.service to /etc/systemd/system/mysqld.service.

$ systemctl start mysqld.service
$ systemctl enable mysqld.service

# 监测端口
$ netstat -tunpl |grep 3306
```

