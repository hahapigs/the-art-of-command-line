### 1 、拉取 gitlab
```powershell
$ docker pull gitlab/gitlae-ce:latest
```
说明：也可以不拉取，直接创建容器，docker 会自动拉取

### 2、创建容器

```powershell
# 先设置环境变量
export DOCKER_HOME="/Users/hahapig/DockerData"
export GITLAB_HOME="$DOCKER_HOME/gitlab"

# 创建容器
$ docker run \
-itd \
--name gitlab \
-p 8880:80 \
-p 8822:22 \
-v $GITLAB_HOME/config:/etc/gitlab \
-v $GITLAB_HOME/logs:/var/log/gitlab \
-v $GITLAB_HOME/data:/var/opt/gitlab \
-v /etc/localtime:/etc/localtime
--restart always \
--privileged=true \
--shm-size 256m \
gitlab/gitlab-ce:latest

# 查看创建容器是否成功
$ docker ps -a

# 查看 gitlab 日志
$ docker logs -f gitlab

# 设置不开机启动
$ docker update --restart no gitlab
```
注意：如果容器的 status 是 health 状态，说明 gitlab 启动完成

### 3、访问
```powershell
http://localhost:8889

# 默认账户为 root
# 查看 root 密码
$ cat /DockerData/gitlab/etc/initial_root_password
```
