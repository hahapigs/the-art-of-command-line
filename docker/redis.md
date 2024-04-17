

### 1、单机（standalone）

##### 编写配置

``` powershell
$ mkdir -p $REDIS_HOME
```

``` powershell
# redis
bind 0.0.0.0
port 6379
protected-mode no
slave-read-only no
```

###### docker run

``` powershell
# 拉取镜像
$ docker pull redis

##### 设置环境变量
$ export DOCKER_HOME="/Users/zhaohongliang/DockerData"
$ export REDIS_HOME="$DOCKER_HOME/redis"

# 创建容器
$ docker run \
-itd \
--name redis \
-p 6379:6379 \
-v $REDIS_HOME/conf:/etc/redis \
-v $REDIS_HOME/data:/data \
--restart no \
--privileged=true \
redis redis-server /etc/redis/redis.conf

# 查看创建容器是否成功
$ docker ps -a

# 查看 redis 日志
$ docker logs -f redis
```

##### docker compose

``` yaml

```

##### DockerFile

``` powershell

```



### 2、主从复制

##### redis

``` powershell
# 创建目录
$ mkdir -p $REDIS_HOME/redis{-1, -2, -3}/conf
```

- redis-1

  ``` powershell
  # 配置
  $ vim $REDIS_HOME/redis-1/conf/redis.conf
  ```
  
  ``` powershell
  # redis-1
  bind 0.0.0.0
  port 6379
  protected-mode no
  slave-read-only no
  ```

  ``` powershell
  # 启动
  $ docker run \
  -itd \
  --name redis-1 \
  -p 6379:6379 \
  -v $REDIS_HOME/redis-1/conf/redis.conf:/etc/redis/redis.conf \
  -v $REDIS_HOME/redis-1/data:/data \
  --restart no \
  --privileged=true \
  redis redis-server /etc/redis/redis.conf
  ```

- redis-2
  ``` powershell
  # 查看 redis-master 主机ip, 假设IP为 172.17.0.2
  $ docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' redis-1
	# 配置
  $ vim $REDIS_HOME/redis-2/conf/redis.conf
  ```
  
  ``` powershell
  # redis-2
  bind 0.0.0.0
  port 6379
protected-mode no
  slave-read-only no
  slaveof 172.17.0.2 6379
  ```

  ``` powershell
  $ docker run \
  -itd \
  --name redis-2 \
  -p 6380:6379 \
  -v $REDIS_HOME/redis-2/conf/redis.conf:/etc/redis/redis.conf \
  -v $REDIS_HOME/redis-2/data:/data \
--restart no \
  --privileged=true \
  redis redis-server /etc/redis/redis.conf
  ```

- redis-3
  ``` powershell
  # 查看 redis-master 主机ip, 假设IP为 172.17.0.2
  $ docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' redis-1
  # 配置
  $ vim $REDIS_HOME/redis-2/conf/redis.conf
  ```

  ``` powershell
  # redis-3
  bind 0.0.0.0
  port 6379
  protected-mode no
  slave-read-only no
  slaveof 172.17.0.2 6379
  ```

  ``` powershell
  $ docker run \
  -itd \
  --name redis-3 \
  -p 6381:6379 \
  -v $REDIS_HOME/redis-3/conf/redis.conf:/etc/redis/redis.conf \
  -v $REDIS_HOME/redis-3/data:/data \
  --restart no \
  --privileged=true \
  redis redis-server /etc/redis/redis.conf
  ```

- 验证

  ``` powershell
# 分别验证 redis-1，redis-2，redis-3，返回 PONG 说明启动正常
  $ redis-cli 127.0.0.1 -p 6379 ping
  PONG
  ```
  
  ``` powershell
  # 查看 master 节点返回的信息，其他 slave 节点返回信息与 master 会不同
  $ redis-cli -h 127.0.0.1 -p 6379 info replication
  # Replication
  role:master
  connected_slaves:2
  slave0:ip=172.17.0.4,port=6379,state=online,offset=781363,lag=1
  slave1:ip=172.17.0.5,port=6379,state=online,offset=781363,lag=1
  master_failover_state:no-failover
  master_replid:9025ed1427964c8cc6bc5c32f5d473680e303f63
  master_replid2:5fa4bbdd4ed13ff18a8b5bf2e61754561278addf
  master_repl_offset:781498
  second_repl_offset:438077
  repl_backlog_active:1
  repl_backlog_size:1048576
  repl_backlog_first_byte_offset:438077
  repl_backlog_histlen:343422
  ```

### 3、哨兵模式

##### sentinel

- sentinel-1

  ``` powershell
  # 下载 sentinel.conf
  $ wget http://download.redis.io/redis-stable/sentinel.conf
  # 查看 redis-master 主机ip, 假设IP为 172.17.0.2
  $ docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' redis-1
  # 修改配置
  $ vim sentinel.conf
  ```

  ``` powershell
  # 替换ip
  $ sentinel monitor mymaster 127.0.0.1 6379 2
  ```

  ``` powershell
  ##### 利用 MacOS pbcopy 和 pbpaste 处理，其他系统请逐一拷贝
  # 拷贝进剪切板
  $ pbcopy < sentinel.conf
  # 粘贴文件到 redis-1/conf/sentinel.conf，redis-2/conf/sentinel.conf，redis3/conf/sentinel.conf
  $ pbpaste > $REDIS_HOME/redis{-1, -2, -3}/conf/sentinel.conf
  ```

  ``` powershell
  # 启动
  $ docker run \
  -itd \
  --name sentinel-1 \
  -p 26379:26379 \
  -v $REDIS_HOME/redis-1/conf/sentinel.conf:/etc/redis/sentinel.conf \
  -v $REDIS_HOME/redis-1/data:/data \
  --restart no \
  --privileged=true \
  redis redis-sentinel /etc/redis/sentinel.conf
  ```

- sentinel-2
	 ``` powershell
  $ docker run \
  -itd \
  --name sentinel-2 \
  -p 26380:26379 \
  -v $REDIS_HOME/redis-2/conf/sentinel.conf:/etc/redis/sentinel.conf \
-v $REDIS_HOME/redis-2/data:/data \
  --restart no \
  --privileged=true \
  redis redis-sentinel /etc/redis/sentinel.conf
  ```

- sentinel-3
	
  ``` powershell
  $ docker run \
  -itd \
  --name sentinel-3 \
  -p 26381:26379 \
-v $REDIS_HOME/redis-3/conf/sentinel.conf:/etc/redis/sentinel.conf \
  -v $REDIS_HOME/redis-3/data:/data \
  --restart no \
  --privileged=true \
  redis redis-sentinel /etc/redis/sentinel.conf
  ```
  
- 验证
	
  ``` powershell
  # 分别验证 sentinel-1，sentinel-2, sentinel-3，返回 PONG 说明启动正常
  $ redis-cli 127.0.0.1 -p 6379 ping
  ```
  
``` powershell
  $ redis-cli -h 127.0.0.1 -p 26379 info sentinel
  # Sentinel
  sentinel_masters:1
  sentinel_tilt:0
  sentinel_tilt_since_seconds:-1
  sentinel_running_scripts:0
  sentinel_scripts_queue_length:0
  sentinel_simulate_failure_flags:0
  master0:name=mymaster,status=ok,address=172.17.0.2:6379,slaves=2,sentinels=3
  ```
  
  
  
  