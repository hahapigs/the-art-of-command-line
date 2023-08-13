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
jenkins/jenkins:lts

# 设置 mysql 系统时间和物理机环境一致，需要设置 localtime
$ -v /etc/localtime:/etc/localtime

# 查看创建容器是否成功
$ docker ps -a

# 查看 jenkins 日志
$ docker logs -f jenkins
```

浏览器访问 http://localhost:8887，出现问题：Please wait while Jenkins is getting ready to work 的解决方法

``` powershell
# 修改 hudson.model.UpdateCenter.xml
$ vim /DockerData/jenkins/home/hudson.model.UpdateCenter.xml
```

替换 url 为 http://mirror.xmission.com/jenkins/updates/update-center.json

``` xml
<?xml version='1.1' encoding='UTF-8'?>
<sites>
  <site>
    <id>default</id>
    <url>http://mirror.xmission.com/jenkins/updates/update-center.json</url>
  </site>
</sites>
```

### 3、查看登陆密码

``` powershell
# 方式一：在日志中查看
$ docker log -f jenkins

# 方式二：在 initialAdminPassword 文件中查看
$ cat /DockerData/jenkins/home/secrets/initialAdminPassword
```



