## docker 的基本操作

### 1、镜像

```powershell
# 查询镜像
$ docker search mysql
# 拉取镜像
$ docker pull mysql

# 查看所有镜像
$ docker images
# 查看所有none镜像
$ docker images | grep none
# 查看所有none镜像，并打印id
$ docker images | grep none | awk '{print $3}'
# 查看所有none镜像，并删除
$ docker images | grep none | awk '{print $3}' | xargs docker rmi
$ docker rmi `docker images | grep none | awk '{print $3}'`
```

### 容器

``` powershell
# 创建容器

# 启动容器
$ docker start mysql-1
# 停止容器
$ docker stop mysql-1
# 删除容器
$ docker rm mysql-1
# 查看容器
$ docker ps 
# 查看所有容器
$ docker ps -a
# 查看所有mysql容器
$ docker ps -a --filter 'name=mysql'
# 查看所有mysql容器，并打印id或名称
$ docker ps -a --filter 'name=mysql' | awk '{print $1}' 
$ docker ps -a --filter 'name=mysql' | awk '{print $12}' 
# 查看所有mysql容器，并格式化id或名称，然后启动/停止
$ docker ps -a --filter 'name=mysql' | awk -F " " '{print $1}' | xargs docker start
$ docker start `docker ps -a --filter 'name=mysql' | awk -F " " '{print $13}'`
# 查看所有运行的mysql容器，格式化名称，替换掉 /，排序后，停止运行
$ docker ps -q --filter "name=mysql" | xargs docker inspect --format '{{.Name}}' | sed 's|^/||' | sort | xargs docker stop

# 进入容器
$ docker exec -it mysql-1 /bin/bash
# 从容器拷贝文件到本地
$ docker cp mysql-1:/opt/readme.md .
# 从本地拷贝文件到容器
$ docker cp readme.md mysql-1:/opt

# 修改容器设置或参数
$ docker update --restart no mysql-1
```

### 网络

``` powershell
# 查看网络
$ docker network ls
# 创建网络
$ docker network create my-network
# 删除网络
$ docker network rm my-network
# 查看网关
$ docker network inspect my-network
# 查看 mysql-1 容器的ip
$ docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' mysql-1
# 查看正在运行所有名称包含mysql容器的ip
$  docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $(docker ps -q --filter "name=mysql")
$ docker ps -q --filter "name=mysql" | xargs docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'
$ docker ps -q --filter "name=mysql" | awk -F " " '{print $1}' | xargs docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'
```