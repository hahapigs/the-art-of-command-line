### 拉取 nexus
```powershell
# search
$ docker search nexus

# pull
$ docker pull sonatype/nexus3

# 查看
$ docker iamges
```
### 创建容器
```powershell
# 创建容器
$ docker run -d --name nexus -p 8888:8081 -v /alibaba/nexus/nexus-data:/nexus-data  --restart=always --privileged=true sonatype/nexus3

# 查看创建容器是否成功
$ docker ps -a

# 查看 nexus 日志
$ docker logs -f nexus

# 如果日志创建容器完成，再去浏览器访问
```
### 访问
```powershell
# 打开浏览器访问
# 浏览器：http://localhost:8888
```
### 修改密码
```powershell
# 默认用户为admin

# 默认密码查看，因为 docker 创建容器的时候挂载了外部数据卷，目录为 ~/nexus/nexus-data/
$ cat ~/nexus/nexus-data/admin.properties
```

注意：如果要更改 nexus 容器的默认端口，可以执行如下操作：<br />方法一：
```powershell
# 进入容器
$ docker exec -it 容器ID /bin/bash

# 安装vim编辑器
$ apt-get update
$ apt-get install vim
```
方法二：
```powershell
# 如果在有宽带网络的情况下，可以将文件 copy 出来，编辑完成后重新放回到容器中
$ docker cp nexus:/opt/sonatype/nexus/etc/nexus-default.properties ~/Desktop

# 放回容器中
$ docker cp nexus-default.properties nexus:/opt/sonatype/nexus/etc
```
