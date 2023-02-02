### Mac OS X / Linux

#### No space left on device 

查看 docker 磁盘使用情况

``` powershell
$ docker system df
```

查看 docker 挂载目录

``` powershell
$ docker info | grep "Docker Root Dir"
```

查看 docker 挂载目录的使用情况

``` powershell
$ df -hl /var/lib/docker
```

清理无效的数据文件

``` powershell
$ cd /var/lib/docker
$ ll -h
```

清理 docker 环境已经停止的容器

``` powershell
# 查看所有已经停止的容器
$ docker ps -ef | grep Exited
# 方法一：删除所有未运行的容器（已经运行的删除不了，未运行的就一起被删除了，删除前要慎重）
$ docker rm $(docker ps -a -q)

# 方法二：根据容器的状态，删除 Exited 状态的容器
$ docker rm $(docker ps -ef status=exited)

# 方法三：docker1.13版本以后，可以使用 docker sytem 或者 docker container 命令清理容器
# 删除已经停用的容器
$ docker container prune
# 删除关闭的容器、无用的数据卷和网络，以及 dangling 镜像
$ docker system prune
# docker system prune -a 清理的更加彻底，可以将没有容器使用 Docker 的镜像都删掉
$ docker system prune -a 

# 方法四：根据容器的ID删除容器
# 显示所有状态为 Exited 容器，取出这些容器的ID
$ docker ps -a | grep Exited | awk '{print $1}'
# 根据ID删除容器
$ docker rmm `docker ps -a | grep Exited | awk '{print $1}'`

# 方法五：创建新的挂载目录
$ mkdir -p /app/dockerdata
# 复制数据
$ mv /var/lib/docker /app/dockerdata
# 修改 docker 配置文件, 修改ExecStart = /usr/bin/dockerd-current 下面追加
# --graph /app/dockerdata/docker
$ vim /lib/systemd/system/docker.service
```

重启 docker

``` powershell
$ systemctl disable docker
$ systemctl enable docker
$ systemctl deamon-reload
$ systemctl start docker
```



注意：在以上过程中使用 docker system 等命令会发生很慢的现象

