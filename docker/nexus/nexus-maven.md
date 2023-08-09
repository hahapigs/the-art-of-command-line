### 1、拉取 nexus
```powershell
# search
$ docker search nexus

# pull
$ docker pull sonatype/nexus3

# 查看
$ docker iamges
```
### 2、创建容器
```powershell
# 创建容器
$ docker run -d --name nexus -p 8888:8081 -v /DockerData/nexus/nexus-data:/nexus-data  --restart=always --privileged=true sonatype/nexus3

# 查看创建容器是否成功
$ docker ps -a

# 查看 nexus 日志
$ docker logs -f nexus

# 如果日志创建容器完成，再去浏览器访问
```
### 3、访问 nexus
```powershell
# 打开浏览器访问
# 浏览器：http://localhost:8888
```
### 4、修改密码
```powershell
# 默认用户为admin

# 默认密码查看，因为 docker 创建容器的时候挂载了外部数据卷，目录为 ~/nexus/nexus-data/
$ cat /DockerData/nexus/nexus-data/admin.properties
```

**注意：如果要更改 nexus 容器的默认端口，可以执行如下操作：**<br />方法一：
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
### 5、创建 blob stores
```powershell
Blob Stores >> Create Blob Stores
```
### 6、创建 nexus 宿主仓库
```powershell
# snapshots
Repositories >> Create repository >> maven2 (hosted)
# releases
Repositories >> Create repository >> maven2 (hosted)
```
### 7、创建 nexus 代理仓库
```powershell
Repositories >> Create repository >> maven2 (proxy)
```
注意：设置 **Proxy >> Remote storage** 为 [http://maven.aliyun.com/nexus/content/groups/public/](http://maven.aliyun.com/nexus/content/groups/public/)
### 8、创建 nexus 仓库组
```powershell
Repositories >> Create repository >> maven2 (group)
```
注意：设置 **Group >> Member repositories** 要添加之前设置过的所有宿主仓库和代理仓库

![](https://cdn.nlark.com/yuque/0/2022/jpeg/725923/1661096980698-d46b0162-33e5-4e23-9b18-96f428356b47.jpeg)
### 9、创建角色
```powershell
Security >> Roles >> Create Role
```
注意：选择 **Privilieges** 要添加 nx-repository-view-maven2-*edit 权限，如果没有该权限会导致 maven 无法 deployee。关于 deployee 出现的异常如下：
```powershell
If an artifact fails to deploy from Maven (or other build tools) make note of the HTTP status code returned.  Then check below to see what the code means, and how to diagnose and fix the issue.

Code 400 - Method not Allowed
Nexus has received your deployment request but cannot process it because it is invalid.  There are two common causes for this.

The most common reason is that you are trying to re-deploy an artifact into a repository which does not allow redeployment.  Check the "deployment policy" in your hosted repository configuration.  If it is set to "disable redeploy" it means you cannot redeploy an artifact which is already in the repository.  Note that this is the default setting for Nexus release repositories, since redeploying release artifacts is a maven anti-pattern.

The second common reason for this code is that you are trying to deploy a release artifact into a snapshot repository, or vice versa.

Code 401 - Unauthorized
Either no login credentials were sent with the request, or login credentials which are invalid were sent.   Checking the "authorization and authentication" system feed in the Nexus UI can help narrow this down. If credentials were sent there will be an entry in the feed.

If no credentials were sent this is likely due to a mis-match between the id in your pom's distributionManagement section and your settings.xml's server section that holds the login credentials.

Code 402 - Payment Required
This error is returned if you are using Nexus Professional and your license has expired.

Code 403 - Forbidden
The login credentials sent were valid, but the user does not have permission to upload to the repository.  Go to "administration/security" in the Nexus UI, and bring up the user (or the user's role if they are mapped via an external role mapping) and examine the role tree to see what repository privileges they have been assigned.  A user will need create and update privileges for a repository to be able to deploy into it.

Code 404 - Not Found
The repository URL is invalid.  Note that this code is returned after the artifact upload has completed, so it can be a bit confusing.

Code 502 - Reverse Proxy Timeout
You have a reverse proxy in front of Nexus (such as Nginx or Apache+mod_proxy) and the pending deployment request had no activity for the period of time specified in the reverse proxy's timeout setting.   This could be due to the timeout being set to a very low value, the Nexus server being under very high load, or a bug in Nexus.  If you need help diagnosing this contact support.

Code 503 - Service unavailable
This is not thrown by Nexus but instead your reverse proxy.

Is Nexus running? Check that Nexus is running.
Nexus is not redirecting correctly due to its force base url or jetty.xml settings. Review what has changed to make this stop working.
another server has the same IP as the Nexus host and your reverse proxy is confused. This is a network issue that your IT staff may need to help you solve.
```
### 10、创建用户
```powershell
Security >> Users >> Create local user
```
注意：添加刚刚创建好的角色和 nx-anonymous 角色，不添加 nx-admin 的目的是为了 nexus  的安全
### 11、允许 anonymous 访问
```powershell
Security >> Anonymous Access
```
### 12、设置 settings.xml
```powershell
......

<servers> 
  <server>
    <id>nexus-releases</id>
    <username>alibabauser</username>
    <password>123456</password>
  </server>
  <server>
    <id>nexus-snapshots</id>
    <username>alibabauser</username>
    <password>123456</password>
    </server>
  </servers>
<servers>

......

<mirrors>
  <mirror>
    <id>nexus</id>
    <name>Nexus maven</name>
    <url>http://localhost:8888/repository/alibaba-public/</url>
    <mirrorOf> * </mirrorOf>       
  </mirror>
</mirros>

......
<profiles>
  <profile>
    <id>nexus</id> 
    <repositories>
      <repository>
        <id>nexus</id> 
        <name>Nexus</name>
        <url>http://localhost:8888/repository/alibaba-central/</url> 
        <releases>
           <enabled>true</enabled>
        </releases> 
        <snapshots>
           <enabled>true</enabled> 
           <updatePolicy>always</updatePolicy>
        </snapshots>
      </repository>  
    </repositories>    
    <pluginRepositories>
      <pluginRepository>
        <id>nexus</id>
        <name>Nexus</name>
        <url>http://localhost:8888/repository/alibaba-central/</url>
        <releases>
          <enabled>true</enabled>
        </releases>
        <snapshots>
          <enabled>true</enabled> 
          <updatePolicy>always</updatePolicy>
        </snapshots>
      </pluginRepository>
    </pluginRepositories>
  </profile>
</profiles>

......

<activeProfiles>
  <activeProfile>nexus</activeProfile>
</activeProfiles>
```
### 13、设置 pom.xml
```powershell
......

    <distributionManagement>
        <repository>
            <id>nexus-releases</id>
            <name>Nexus Releases Repository</name>
            <url>http://localhost:8888/repository/alibaba-releases/</url>
        </repository>
        <snapshotRepository>
            <id>nexus-snapshots</id>
            <name>Nexus Snapshots Repository</name>
            <url>http://localhost:8888/repository/alibaba-snapshots/</url>
        </snapshotRepository>
    </distributionManagement>
    
 ......
```
### 14、deployee
```powershell
$ mvn deployee -Dmaven.test.skip=true -s ~/.m2/settings.xml
```
