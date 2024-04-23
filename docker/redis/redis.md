 ### 准备

##### 设置卷位置

###### Mac

``` powershell
# docker卷目录
$ export DOCKER_HOME="$HOME/DockerData"
# redis卷目录
$ export REDIS_HOME="$DOCKER_HOME/redis"
$ mkdir -p $REDIS_HOME
```

######  Linux

``` powershell
$ export REDIS_HOME="/srv/redis"
```

<font color="#e83e8c">REDIS_HOME</font> 推荐附加设置在 shell 中

- bash	<font color="#e83e8c"> ~/.bash_profile</font>

- zsh       <font color="#e83e8c">~/.zshrc</font>

  

##### 容器使用主机装载的卷来存储持久数据

| 本地位置         | 容器位置       | 使用     |
| ---------------- | -------------- | -------- |
| $REDIS_HOME/conf | /etc/redis     | 配置文件 |
| $REDIS_HOME/data | /data          | 数据     |
| $REDIS_HOME/logs | /var/log/redis | 日志     |

##### 目录结构

├── conf
│   ├── redis.conf
│   └── sentinel.conf
├── data
│   └── dump.rdb
└── logs
    ├── redis.log
    └── sentinel.log



### 1、单机（standalone）

###### 编写配置

``` powershell
# redis
bind 0.0.0.0
port 6379
daemonize no
protected-mode no
dbfilename "dump.rdb"
dir /data
logfile "/var/log/redis/redis.log"
```

也可以使用官方的 redis.conf

``` powershell
$ wget http://download.redis.io/redis-stable/redis.conf
```

注意：

1、`docker` 启动不要设置 `daemonize yes`，否则会自动退出，不配置则默认 `no`

2、如果 `protected-mode` 设置为 `yes`，需要额外设置 `bind`，建议生产开启，不配置默认为 `yes`

##### docker run

``` powershell
# 创建容器
$ docker run \
-itd \
--name redis \
-p 6379:6379 \
-v $REDIS_HOME/conf:/etc/redis \
-v $REDIS_HOME/data:/data \
-v $REDIS_HOME/logs:/var/log/redis \
-v /etc/localtime:/etc/localtime:ro \
--restart no \
--privileged=true \
--network canary-net \
redis redis-server /etc/redis/redis.conf

# 查看创建容器是否成功
$ docker ps -a

# 查看 redis 日志
$ docker logs -f redis
```

##### docker-compose

``` yaml
version: '3.8'

services:
  redis:
    image: redis:latest
    container_name: redis
    command: redis-server /etc/redis/redis.conf
    ports:
      - "6379:6379"
    volumes:
      - $REDIS_HOME/conf:/etc/redis
      - $REDIS_HOME/data:/data
      - $REDIS_HOME/logs:/var/log/redis 
      - /etc/localtime:/etc/localtime:ro
    privileged: true
```

启动

```powershell
$ docker-compose up -d
```

### 2、主从复制

| 名称    | IP         | 宿主端口 | 端口 | 备注   |
| ------- | ---------- | -------- | ---- | ------ |
| redis-1 | 172.17.0.2 | 6379     | 6379 | master |
| redis-2 | 172.17.0.3 | 6380     | 6379 | slave  |
| redis-3 | 172.17.0.4 | 6381     | 6379 | slave  |

##### redis

创建 `redis-1`， `redis-2`，`redis-3` 的工作目录

``` powershell
$ mkdir -p $REDIS_HOME/redis{-1, -2, -3}/conf
```

- redis-1

  编写配置

  ``` powershell
  # redis-1
  bind 0.0.0.0
  port 6379
  protected-mode no
  dbfilename "dump.rdb"
  dir /data
  logfile "/var/log/redis/redis.log"
  ```

  启动

  ``` powershell
  $ docker run \
  -itd \
  --name redis-1 \
  -p 6379:6379 \
  -v $REDIS_HOME/redis-1/conf/redis.conf:/etc/redis/redis.conf \
  -v $REDIS_HOME/redis-1/data:/data \
  -v $REDIS_HOME/redis-1/logs:/var/log/redis \
  -v /etc/localtime:/etc/localtime:ro \
  --restart no \
  --privileged=true \
  --network canary-net \
  redis redis-server /etc/redis/redis.conf
  ```

- redis-2
  
   查看主机IP
  
	``` powershell
  # 查看 redis-master 主机ip, 假设IP为 172.17.0.2
  $ docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' redis-1
  172.17.0.2
  ```
  
  编写配置
  
``` powershell
  # redis-2
  bind 0.0.0.0
  port 6379
protected-mode no
  dbfilename "dump.rdb"
  dir /data
  logfile "/var/log/redis/redis.log"
  
  # 开启复制
  # replicaof 172.17.0.2 6379
```

  启动

  ``` powershell
$ docker run \
  -itd \
  --name redis-2 \
  -p 6380:6379 \
  -v $REDIS_HOME/redis-2/conf/redis.conf:/etc/redis/redis.conf \
  -v $REDIS_HOME/redis-2/data:/data \
  -v $REDIS_HOME/redis-2/logs:/var/log/redis \
  -v /etc/localtime:/etc/localtime:ro \
  --restart no \
  --privileged=true \
  --network canary-net \
  redis redis-server /etc/redis/redis.conf --replicaof redis-1 6379
  ```

- redis-3
  
  编写配置
  
  ``` powershell
  # redis-3
  bind 0.0.0.0
port 6379
  protected-mode no
  dbfilename "dump.rdb"
  dir /data
  logfile "/var/log/redis/redis.log"
  
  # 开启复制
  # replicaof 172.17.0.2 6379
  ```

  启动
  
  ``` powershell
  $ docker run \
  -itd \
  --name redis-3 \
  -p 6381:6379 \
  -v $REDIS_HOME/redis-3/conf/redis.conf:/etc/redis/redis.conf \
  -v $REDIS_HOME/redis-3/data:/data \
  -v $REDIS_HOME/redis-3/logs:/var/log/redis \
  -v /etc/localtime:/etc/localtime:ro \
--restart no \
  --privileged=true \
  --network canary-net \
  redis redis-server /etc/redis/redis.conf --replicaof redis-1 6379
  ```

##### 测试

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

##### docker-compose

```yaml
version: '3.8'

services:
  redis-1:
    image: redis:latest
    container_name: redis-1
    command: redis-server /etc/redis/redis.conf
    ports:
      - "6379:6379"
    volumes:
      - $REDIS_HOME/redis-1/conf:/etc/redis
      - $REDIS_HOME/redis-1/data:/data
      - $REDIS_HOME/redis-1/logs:/var/log/redis 
      - /etc/localtime:/etc/localtime:ro
    privileged: true
    networks:
      - canary-net
  redis-2:
    image: redis:latest
    container_name: redis-2
    command: redis-server /etc/redis/redis.conf --replicaof redis-1 6379
    ports:
      - "6380:6379"
    volumes:
      - $REDIS_HOME/redis-2/conf:/etc/redis
      - $REDIS_HOME/redis-2/data:/data
      - $REDIS_HOME/redis-2/logs:/var/log/redis 
      - /etc/localtime:/etc/localtime:ro
    privileged: true
    networks:
      - canary-net
    depends_on:
      - redis-1
  redis-3:
    image: redis:latest
    container_name: redis-3
    command: redis-server /etc/redis/redis.conf --replicaof redis-1 6379
    ports:
      - "6381:6379"
    volumes:
      - $REDIS_HOME/redis-3/conf:/etc/redis
      - $REDIS_HOME/redis-3/data:/data
      - $REDIS_HOME/redis-3/logs:/var/log/redis 
      - /etc/localtime:/etc/localtime:ro
    privileged: true
    networks:
      - canary-net
    depends_on:
      - redis-1
      - redis-2
 
networks:
  canary-net:
    external: true
```

开启主从复制的几种方式：

- `redis.cnf` 配置 `replicaof <ip> <port>`

  ```powershell
  replicaof 172.17.0.2 6379
  ```

- `docker run -itd` 命令追加 `--replicaof <hostname/ip> <port>`

  ```powershell
  $ docker run -itd --name redis-2 -p 6379:6379 redis redis-server /etc/redis/redis.conf --replicaof redis-1 6379
  ```

- `docker-compose.yaml` 配置 `--replicaof <hostname/ip> <port>`

  ```yaml
  version: '3.8'
  services:
    redis-2:
      image: redis:latest
      container_name: redis-2
      command: redis-server /etc/redis/redis.conf --replicaof redis-1 6379
      ports:
        - "6380:6379"
  ```

- `redis-cli ` 执行 `replicaof <ip> <port>`

  ``` powershell
  $ redis-cli -h 127.0.0.1 -p 6380 replicaof 172.17.0.2 6379
  OK
  ```

**说明：在 `Redis` 中，`salveof` 和 `replicaof` 是用于配置主从复制关系的命令，它们的作用是相同的，只是在 `Redis 6.0` 之后，将 `slaveof` 命令更名为 `replicaof` ，因为它更加清晰和直观，反映了从节点是主节点的复制节点的特性。如果您使用的是较旧的 `Redis` 版本，可能还会看到 `slaveof` 命令，但为了保持一致性和准确性，建议使用 `replicaof` 命令。**



##### 拓展：

`docker-compose` 方式会在 `redis.conf` 自动追加一些配置

```powershell
······
# 省略了其他配置

latency-tracking-info-percentiles 50 99 99.9
save 3600 1
save 300 100
save 60 10000
user default on nopass sanitize-payload ~* &* +@all
```

这个文本片段看起来是在描述一些Redis的配置或指令，逐个解释一下：

1. `latency-tracking-info-percentiles 50 99 99.9`: 这是在配置 `Redis` 实例对命令执行时延的跟踪信息百分位数。在这种情况下，Redis将会记录并报告各种操作的延迟，涵盖了 `50%`，`99%` 和 `99.9%` 的情况。

2. `save 3600 1`: 这是设置Redis进行自动快照（snapshot）持久化的规则。在这里，`Redis` 会在至少1个键变化并且距上一个快照已经过去 `3600秒`（1小时）后自动进行快照持久化。

3. `save 300 100`: 这是另一个设置自动快照的规则。在这里，`Redis` 会在至少 `100个` 键变化并且距上一个快照已经过去 `300秒` 后自动进行快照持久化。

4. `save 60 10000`: 这也是设置自动快照的规则。在这里，`Redis` 会在至少 `10000个` 键变化并且距上一个快照已经过去 `60秒` 后自动进行快照持久化。

5. `user default on nopass sanitize-payload ~* &* +@all`: 看起来是在描述一个用户或者访问控制规则。这个规则可能是关于一个名为 `default` 的用户，启用了某些功能，禁用了密码保护，并对特定类型的数据进行了处理。`~* &* +@all ` 可能是一些正则表达式或者标识符，用于匹配操作的数据。

总的来说，这些内容是关于 Redis 实例的一些配置参数和规则的描述，涵盖了延迟跟踪、自动快照持久化和用户访问控制等方面。



### 3、哨兵模式

| 名称       | IP         | 宿主端口 | 端口  | 备注             |
| ---------- | ---------- | -------- | ----- | ---------------- |
| sentinel-1 | 172.17.0.5 | 26379    | 26379 | 默认监测 redis-1 |
| sentinel-2 | 172.17.0.6 | 26380    | 26379 | 默认监测 redis-1 |
| sentinel-3 | 172.17.0.7 | 26381    | 26379 | 默认监测 redis-1 |

##### sentinel

- sentinel-1

  下载配置

  ``` powershell
  # 下载 sentinel.conf
  $ wget http://download.redis.io/redis-stable/sentinel.conf
  ```

  查看主机IP

  ``` powershell
  # 查看 redis-master 主机ip, 假设IP为 172.17.0.2
  $ docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' redis-1
  172.17.0.2
  ```

  修改配置

  ``` powershell
  # 哨兵监测 master 节点的ip，替换为 172.17.0.2
  sentinel monitor mymaster 172.17.0.2 6379 2
  # 日志
  logfile "/var/log/redis/sentinel.log"
  ```

  将修改后的配置分别 `copy` 到 `redis-1`，`redis-2`，`redis-3` 的 `conf`

  ``` powershell
  ##### 利用 MacOS pbcopy 和 pbpaste 处理，其他系统请逐一拷贝
  # 拷贝进剪切板
  $ pbcopy < sentinel.conf
  # 粘贴文件到 redis-1/conf/sentinel.conf，redis-2/conf/sentinel.conf，redis3/conf/sentinel.conf
  $ pbpaste > $REDIS_HOME/redis{-1, -2, -3}/conf/sentinel.conf
  ```

  启动

  ``` powershell
  $ docker run \
  -itd \
  --name sentinel-1 \
  -p 26379:26379 \
  -v $REDIS_HOME/redis-1/conf/sentinel.conf:/etc/redis/sentinel.conf \
  -v $REDIS_HOME/redis-1/logs:/var/log/redis \
  -v /etc/localtime:/etc/localtime:ro \
  --restart no \
  --privileged=true \
  --network canary-net \
  redis redis-sentinel /etc/redis/sentinel.conf
  ```

- sentinel-2
	 ``` powershell
  $ docker run \
  -itd \
  --name sentinel-2 \
  -p 26380:26379 \
  -v $REDIS_HOME/redis-2/conf/sentinel.conf:/etc/redis/sentinel.conf \
-v $REDIS_HOME/redis-2/logs:/var/log/redis \
  -v /etc/localtime:/etc/localtime:ro \
  --restart no \
  --privileged=true \
  --network canary-net \
redis redis-sentinel /etc/redis/sentinel.conf
	```
	
- sentinel-3
	
  ``` powershell
  $ docker run \
  -itd \
  --name sentinel-3 \
  -p 26381:26379 \
-v $REDIS_HOME/redis-3/conf/sentinel.conf:/etc/redis/sentinel.conf \
  -v $REDIS_HOME/redis-3/logs:/var/log/redis \
  -v /etc/localtime:/etc/localtime:ro \
  --restart no \
  --privileged=true \
  --network canary-net \
  redis redis-sentinel /etc/redis/sentinel.conf
  ```

##### 测试

``` powershell
# 分别验证 sentinel-1，sentinel-2, sentinel-3，返回 PONG 说明启动正常
$ redis-cli 127.0.0.1 -p 6379 ping
PONG
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

##### docker-compose

```yaml
version: '3.8'

services:
  redis-1:
    image: redis:latest
    container_name: redis-1
    command: redis-server /etc/redis/redis.conf
    ports:
      - "6379:6379"
    volumes:
      - $REDIS_HOME/redis-1/conf:/etc/redis
      - $REDIS_HOME/redis-1/data:/data
      - $REDIS_HOME/redis-1/logs:/var/log/redis
      - /etc/localtime:/etc/localtime:ro
    privileged: true
    networks:
      - canary-net
  redis-2:
    image: redis:latest
    container_name: redis-2
    command: redis-server /etc/redis/redis.conf --slaveof redis-1 6379
    ports:
      - "6380:6379"
    volumes:
      - $REDIS_HOME/redis-2/conf:/etc/redis
      - $REDIS_HOME/redis-2/data:/data
      - $REDIS_HOME/redis-2/logs:/var/log/redis
      - /etc/localtime:/etc/localtime:ro
    privileged: true
    networks:
      - canary-net
    depends_on:
      - redis-1
  redis-3:
    image: redis:latest
    container_name: redis-3
    command: redis-server /etc/redis/redis.conf --slaveof redis-1 6379
    ports:
      - "6381:6379"
    volumes:
      - $REDIS_HOME/redis-3/conf:/etc/redis
      - $REDIS_HOME/redis-3/data:/data
      - $REDIS_HOME/redis-3/logs:/var/log/redis
      - /etc/localtime:/etc/localtime:ro
    privileged: true
    networks:
      - canary-net
    depends_on:
      - redis-1
      - redis-2
  sentinel-1:
    image: redis:latest
    container_name: sentinel-1
    # command: redis-sentinel /etc/redis/sentinel.conf --sentinel monitor mymaster 172.17.0.2 6379 2
    command: redis-sentinel /etc/redis/sentinel.conf
    ports:
      - "26379:26379"
    volumes:
      - $REDIS_HOME/redis-1/conf:/etc/redis
      - $REDIS_HOME/redis-1/data:/data
      - $REDIS_HOME/redis-1/logs:/var/log/redis
      - /etc/localtime:/etc/localtime:ro
    privileged: true
    networks:
      - canary-net
    depends_on:
      - redis-1
      - redis-2
      - redis-3
  sentinel-2:
    image: redis:latest
    container_name: sentinel-2
    command: redis-sentinel /etc/redis/sentinel.conf
    ports:
      - "26380:26379"
    volumes:
      - $REDIS_HOME/redis-2/conf:/etc/redis
      - $REDIS_HOME/redis-2/data:/data
      - $REDIS_HOME/redis-2/logs:/var/log/redis
      - /etc/localtime:/etc/localtime:ro
    privileged: true
    networks:
      - canary-net
    depends_on:
      - redis-1
      - redis-2
      - redis-3
      - sentinel-1
  sentinel-3:
    image: redis:latest
    container_name: sentinel-3
    command: redis-sentinel /etc/redis/sentinel.conf
    ports:
      - "26381:26379"
    volumes:
      - $REDIS_HOME/redis-3/conf:/etc/redis
      - $REDIS_HOME/redis-3/data:/data
      - $REDIS_HOME/redis-3/logs:/var/log/redis
      - /etc/localtime:/etc/localtime:ro
    privileged: true
    networks:
      - canary-net
    depends_on:
      - redis-1
      - redis-2
      - redis-3
      - sentinel-1
      - sentinel-2
 
networks:
  canary-net:
    external: true
```

开启哨兵监测节点几种方式：

- `sentinel.conf` 配置 `sentinel monitor <master-group-name> <ip> <port> <quorum>`

  ```powershell
  sentinel monitor mymaster 172.17.0.2 6379 2
  ```

- `docker run -itd` 命令追加 `--sentinel monitor <master-group-name> <ip> <port> <quorum>`

  ```powershell
  $ docker run -itd --name sentinel-1 -p 6379:6379 redis redis-sentinel /etc/redis/sentinel.conf --sentinel monitor mymaster 172.17.0.2 6379 2
  ```

- `docker-compose.yaml` 配置 `--sentinel monitor <master-group-name> <ip> <port> <quorum>`

  ```yaml
  version: '3.8'
  services:
    sentinel-1:
      image: redis:latest
      container_name: sentinel-3
      command: redis-sentinel /etc/redis/sentinel.conf --sentinel monitor mymaster 172.17.0.2 6379 2
      ports:
        - "26379:26379"
  ```

- `redis-cli ` 执行 `sentinel remove <master-group-name>` 和 `--sentinel monitor <master-group-name> <ip> <port> <quorum>`

  ``` powershell
  # 删除旧监测节点
  $ redis-cli -h 127.0.0.1 -p 26379 sentinel remove mymaster
  OK
  # 增加新监测节点
  $ redis-cli -h 127.0.0.1 -p 26379 sentinel monitor mymaster 172.17.0.2 6379 2
  OK
  ```

**注意：使用 `docker run` 和 `docker-compose` 追加 `--sentinel monitor <master-group-name> <ip> <port> <quorum>` 指令，需要删除配置文件的配置，否则可能会因冲突导致 `sentinel` 无法启动**。



##### 拓展：

`docker-compose` 方式会在 `sentinel.conf` 自动追加一些配置

```powershell
······
# 省略了其他配置

latency-tracking-info-percentiles 50 99 99.9
user default on nopass ~* &* +@all
sentinel myid 30cff15eb85b3a07f1eda5b29e7e600c66d14e53

sentinel current-epoch 0

sentinel monitor mymaster 172.24.0.7 6379 2
sentinel config-epoch mymaster 0
sentinel leader-epoch mymaster 0

sentinel known-replica mymaster 172.24.0.9 6379

sentinel known-replica mymaster 172.24.0.8 6379

sentinel known-sentinel mymaster 172.24.0.12 26379 25841504a84073fe84f6a1e5583f2485576e3fa0

sentinel known-sentinel mymaster 172.24.0.11 26379 3bec2a84beecd981d67e50b7226d47ce23a4ea7e
```

这些是关于 `Redis Sentinel` 的配置信息。让我们逐个解释一下这些内容：

1. `latency-tracking-info-percentiles 50 99 99.9`: 这条指令用于配置 `Redis Sentinel` 跟踪监控命令执行的延迟情况的百分位数，其中包括了 `50%`，`99%`，`99.9%` 这三个百分位数。

2. `user default on nopass ~* &* +@all`: 这条指令可能是对用户访问控制的配置，其中设置了一个名为 `default` 的用户，并开启了相关功能。`~* &* +@all ` 是用于匹配操作的一些规则。

3. `sentinel myid 30cff15eb85b3a07f1eda5b29e7e600c66d14e53`: 这个指令设置了 `Sentinel` 进程的唯一标识符（ID）为`30cff15eb85b3a07f1eda5b29e7e600c66d14e53`。

4. `sentinel current-epoch 0`: 这个指令设置了当前的 `Sentinel` 实例的 epoch（纪元）为 0。

5. `sentinel monitor mymaster 172.24.0.7 6379 2`: 这个指令是用于让 `Sentinel` 监视名为 `mymaster` 的主节点，`IP` 地址为`172.24.0.7`，端口为 `6379`，并设置 `最小投票数量 `为 2。

6. 其余的 `sentinel known-replica` 和 `sentinel known-sentinel` 指令则用于告知 `Sentine`l 已知的从节点和其他 `Sentinel` 的信息，包括它们的 IP 地址、端口和一些标识符等。

这些配置信息反映了 Redis Sentinel 系统的监控和故障转移配置。通过这些配置，Sentinel 可以监控主节点和从节点的状态，并根据需要执行故障转移操作。希望这个解释对您有所帮助。如果您有任何进一步的问题，请随时提出。



##### SpringBoot 配置哨兵

查看 `sentinel-1`，`sentinel-2`，`sentinel-3` 的 `IP`，下面两条指令效果相同

``` powershell
$ docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $(docker ps -q --filter "name=sentinel")
172.24.0.12
172.24.0.11
172.24.0.10
```

```powershell
$ docker ps -q --filter "name=sentinel" | xargs docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'
172.24.0.12
172.24.0.11
172.24.0.10
```

修改 application-cluster.yaml

``` yaml
spring:
  data:
    redis:
      password:
      database: 0
      # 连接超时时长（毫秒）
      timeout: 6000ms
      lettuce:
        pool:
          # 连接池最大连接数（使用负值表示没有限制）
          max-active: 100
          # 连接池最大阻塞等待时间（使用负值表示没有限制）
          max-wait: -1ms
          # 连接池中的最大空闲连接
          max-idle: 10
          # 连接池中的最小空闲连接
          min-idle: 5
      sentinel:
        master: mymaster
        nodes: 172.17.0.5:26379,172.17.0.6:26379,172.17.0.7:26379
```

注意：请务必保证 `springboot` 和 `redis` 哨兵网络可达情况。当使用 `IDEA` 调试哨兵集群的时候，由于是 `docker` 自已定网络部署，所以导致 `springboot` 在获取 `sentinel` 的 `master` 的 `IP` 时获取到了虚拟 `IP`，导致连接失败。所以把 `spring boot` 项目以 `docker` 方式部署。



###### springboot 项目布署

``` powershell
FROM openjdk:17-jdk

LABEL image.author="zhaohongliang"

WORKDIR /app/canary

COPY ./target/canary-0.0.1-SNAPSHOT.jar /app/canary/canary-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "canary-0.0.1-SNAPSHOT.jar"]

CMD ["-Dspring.profiles.active=standalone"]
```

构建镜像

``` powershell
$ docker build -t canary:0.0.1 .
```

启动

``` powershell
$ docker run \
-itd \
--name canary \
-p 8080:8080 \
-v $PROJECT_HOME/target:/opt/canary \
-v /etc/localtime:/etc/localtime:ro \
--restart no \
--privileged=true \
--network canary-net \
canary:0.0.1 --spring.profiles.active=cluster
```



