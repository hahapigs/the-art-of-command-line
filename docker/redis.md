### 1、拉取 redis

``` powershell
 $ docker pull redis
```

### 2、创建容器

``` powershell
# 创建容器
$ docker run \
-itd \
--name redis \
-p 6379:6379 \
-v /DockerData/redis/conf/redis.conf:/etc/redis/redis.conf \
-v /DockerData/redis/data:/data \
--restart always \
--privileged=true \
redis redis-server /etc/redis/redis.conf

# 查看创建容器是否成功
$ docker ps -a

# 查看 gitlab 日志
$ docker logs -f redis
```

