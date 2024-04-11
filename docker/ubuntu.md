## ubuntu

### 1、拉取镜像

``` powershell
$ docker pull ubuntu
$ docker images 
```

### 2、启动

``` powershell
docker run \
-itd \
--name ubuntu-1 \
-p 16032:6032 \
-p 16033:6033 \
-p 16070:6070 \
-p 180:80 \
--restart no \
--privileged=true \
--network my-network \
ubuntu bash
```

说明：16032、16033、16070、180等端口是为了演示 keepalived + proxysql + nginx 的高可用

### 3、安装软件

``` powershell
# 进入容器
$ docker exec -it ubuntu-1 /bin/bash
# 先更新下包，不然有些东西下载不下来（apt和apt-get这里用到的都是一样的作用）
$ apt update
# ping命令工具包
$ apt -y install iputils-ping
# ip 命令工具包
$ apt -y install iproute2
# ifconfig命令工具包（可选，iproute2就够用，看习惯用ip addr还是ifconfig命令）
$ apt -y install net-tools
# vim编辑器 (可选)
$ apt -y install vim 
# ssh服务器（可选）
$ apt -y install openssh-server
# 网络测试工具，例如： nc -z -v -w 3 proxysql-1 6033
apt -y install netcat

# 安装 keepalived
apt -y install keepalived
# 查看安装位置
$ whereis keepalived
```

安装 keepalived

``` powershell
$ apt -y install keepalived
```

手动安装 keepalived

```powershell
#下载安装包
$ wget http://www.keepalived.org/software/keepalived-2.2.7.tar.gz
# 解压缩
$ tar xvf keepalived-2.2.7.tar.gz
 
$ cd keepalived-2.2.7
 
# 设置编译位置
$ ./configure --prefix=/usr/local/keepalived  --sysconf=/etc
 
# 下载make
$ apt install make
 
# 编译安装
$ make && make install
```

安装 proxysql

``` powershell
# 安装 proxysql，先添加源，再安装
$ apt-get install -y --no-install-recommends lsb-release wget apt-transport-https ca-certificates gnupg
$ wget -O - 'https://repo.proxysql.com/ProxySQL/proxysql-2.5.x/repo_pub_key' | apt-key add - 
echo deb https://repo.proxysql.com/ProxySQL/proxysql-2.5.x/$(lsb_release -sc)/ ./ | tee /etc/apt/sources.list.d/proxysql.list

$ wget -nv -O /etc/apt/trusted.gpg.d/proxysql-2.5.x-keyring.gpg 'https://repo.proxysql.com/ProxySQL/proxysql-2.5.x/repo_pub_key.gpg'

$ apt-get update
$ apt-get install proxysql 或者 apt-get install proxysql=version

# 查看安装位置
$ whereis keepalived
```

手动安装 proxysql

``` powershell
$ wget https://github.com/sysown/proxysql/releases/download/v2.6.2/proxysql_2.6.2-ubuntu20_amd64.deb
$ dpkg -i proxysql_2.6.2-ubuntu20_amd64.deb
```

安装 nginx

``` powershell
$ apt -y install nginx
```

安装 mysql

``` powershell
# 如果是简单的 keepalived + msql 高可用解决方案
$ apt -y install mysql-server
```

### 4、配置 keepalived

查看网卡

``` powershell
# ip a 或者 ip addr
$ ip a
# 如果安装了 net-tools，还可以使用 ifconfig
$ ifconfig

# 网卡是 eth0，不同的环境网卡可能不同
root@a541e7a3e33e:/etc/keepalived# ip a
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
2: tunl0@NONE: <NOARP> mtu 1480 qdisc noop state DOWN group default qlen 1000
    link/ipip 0.0.0.0 brd 0.0.0.0
3: ip6tnl0@NONE: <NOARP> mtu 1452 qdisc noop state DOWN group default qlen 1000
    link/tunnel6 :: brd :: permaddr de9c:e837:4b65::
94: eth0@if95: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue state UP group default
    link/ether 02:42:ac:18:00:07 brd ff:ff:ff:ff:ff:ff link-netnsid 0
    inet 172.24.0.7/16 brd 172.24.255.255 scope global eth0
       valid_lft forever preferred_lft forever
```

配置 keepalived.conf （主）

``` tex
global_defs {
  default_interface eth0
}

vrrp_script check_proxysql {
  script "/opt/check_proxysql.sh"
  interval 5
}

vrrp_instance VI_1 {
  interface eth0

  state MASTER
  virtual_router_id 51
  priority 50
  nopreempt

  unicast_peer {
    172.24.0.7
    172.24.0.8
  }

  virtual_ipaddress {
    172.24.0.101
  }

  authentication {
    auth_type PASS
    auth_pass d0cker
  }

  track_script {
    check_proxysql
  }

  # notify "/container/service/keepalived/assets/notify.sh"
}

```

配置 keepalived.conf （从）

``` tex
global_defs {
  default_interface eth0
}

vrrp_script check_proxysql {
  script "/opt/check_proxysql.sh"
  interval 5
}

vrrp_instance VI_1 {
  interface eth0

  state BACKUP
  virtual_router_id 51
  priority 50
  nopreempt

  unicast_peer {
    172.24.0.7
    172.24.0.8
  }

  virtual_ipaddress {
    172.24.0.101
  }

  authentication {
    auth_type PASS
    auth_pass d0cker
  }

  track_script {
    check_proxysql
  }

  # notify "/container/service/keepalived/assets/notify.sh"
}

```

启动 keepalived

``` powershell
# 启动
$ service keepalived start
```

``` powershell
# 推荐使用该命令启动
$ keepalived -l -f /etc/keepalived/keepalived.conf
```

``` powershell
# 一般容器中的系统没有权限使用 systemctl（不推荐或不能用）
$ systemctl start keepalived.service
```

拓展： 如果想使用 systemctl，参考 [systemctl](https://zhuanlan.zhihu.com/p/648578218)

查看进程是否启动

``` powershell
# 查看进程
$ ps -ef|grep keepalived
# 查看进程
$ pgrep keepalived
# 或者使用 systemctl 查看
$ systemctl status keepalived.service
```

再次查看ip挂载情况

``` powershell
$ ip a

# 网卡是 eth0 多了  inet 172.24.0.101/32 
root@a541e7a3e33e:/etc/keepalived# ip a
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
2: tunl0@NONE: <NOARP> mtu 1480 qdisc noop state DOWN group default qlen 1000
    link/ipip 0.0.0.0 brd 0.0.0.0
3: ip6tnl0@NONE: <NOARP> mtu 1452 qdisc noop state DOWN group default qlen 1000
    link/tunnel6 :: brd :: permaddr de9c:e837:4b65::
94: eth0@if95: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue state UP group default
    link/ether 02:42:ac:18:00:07 brd ff:ff:ff:ff:ff:ff link-netnsid 0
    inet 172.24.0.7/16 brd 172.24.255.255 scope global eth0
       valid_lft forever preferred_lft forever
    inet 172.24.0.101/32 scope global eth0
       valid_lft forever preferred_lft forever
```

关闭 keepalived

``` powershell
$ service keepalived stop
# 或杀死进程
$ pkill keepalived
```

如果 keepalived 启动出现问题，大概率是权限的问题

``` powershell
$ chmod 644 /etc/keepalived/keepalived.conf
```

### 5、配置 proxysql

启动方式

``` powershell
# 以下两种启动方式都可以
$ /usr/bin/proxysql start
$ proxysql -l -f /etc/proxysql.cnf
# 还可以直接使用命令方式启动
$ proxysql

# 查看进程
$ ps -ef|grep proxysql
# 查看进程
$ pgrep proxysql
```

问题：使用 service proxysql start 会出现 proxysql: unrecognized service 问题，尝试一段时间未能解决，暂时搁置

关于 proxysql.cnf 、proxysql-cluster 和 读写分离的配置，请参看 [proxysql.md](.proxysql.md)

### 6、测试

``` powershell
# 停掉 ubuntu-1，在第三台服务器上访问 proxysql, 测试后发现可以正常访问，然后查看 ubuntu-2 的网卡，发现 vip 漂移到 ubuntu-2 上了
$ nc -z -v -w 3 172.24.0.101 6033

# 同时停掉 ubuntu-2, 访问 proxysql 失败

# 启动 ubuntu-1，访问 proxysql 成功
```

问题：关于 keepalive 的 vrrpscript 脚本无法执行的问题，尝试一段时间未能解决，暂时搁置。（keepalived 脑裂问题）

参考文章：[在docker的ubuntu中安装keepalived](https://blog.csdn.net/lxcw_sir/article/details/134688834)

