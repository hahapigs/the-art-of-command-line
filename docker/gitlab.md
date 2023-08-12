### 1 、拉取 gitlab
```powershell
$ docker pull gitlab/gitlae-ce:latest
```
### 2、创建容器
```powershell
# 创建容器
$ docker run \
-itd \
--name gitlab \
-p 8889:80 \
-p 8822:22 \
-v /DockerData/gitlab/etc:/etc/gitlab \
-v /DockerData/gitlab/log:/var/log/gitlab \
-v /DockerData/gitlab/opt:/var/opt/gitlab \
--restart always \
--privileged=true \
gitlab/gitlab-ce

# 设置 gitlab 系统时间和物理机环境一致，需要设置 localdate
$ -v /etc/localtime:/etc/localtime

# 查看创建容器是否成功
$ docker ps -a

# 查看 gitlab 日志
$ docker logs -f gitlab
```
注意：如果容器的 status 是 health 状态，说明 gitlab 启动完成
### 3、访问
```powershell
http://localhost:8889

# 默认账户为 root
# 查看 root 密码
$ cat /DockerData/gitlab/etc/initial_root_password
```
