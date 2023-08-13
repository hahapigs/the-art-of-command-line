### 1、拉取 jenkins-lts

``` powershell
$ docker pull jenkins/jenkins:lts
```

### 2、创建容器

``` powershell
# 创建容器
$ docker run \
-itd \
--name jenkins	 \
-p 8887:8080 \
-p 50000:50000 \
-v /DockerData/jenkins/home:/var/jenkins_home \
--restart always \
--privileged=true \
jenkins/jenkins-lts

# 设置 mysql 系统时间和物理机环境一致，需要设置 localtime
$ -v /etc/localtime:/etc/localtime

# 查看创建容器是否成功
$ docker ps -a

# 查看 jenkins 日志
$ docker logs -f jenkins
```



