#!/bin/bash

export DOCKER_HOME="$HOME/DockerData"
export REDIS_HOME="$DOCKER_HOME/redis"

SENTINEL_PORTS=("26379" "26380" "26381")

# 布署方式
function askAboutDeploy() {
    # labels A, B, C, D
    options=("A" "B" "C" "D")
    # 架构
    architecture=("standalone" "master-slave" "sentinel" "cluster")

    # Prompt the user to choose from A, B, C, D
    echo "部署模式："
    for index in ${!options[@]}; do
        echo "${options[index]}. ${architecture[index]}"
        option=${options[index]}
        eval "${option}=${architecture[index]}"
    done
    
    flag=0
    while [ $flag -ne 1 ]; do
        # User selection
        read -p "Enter your choice (A、B、C  or D): " choice

        # Check user choice and display the selected line
        case $choice in
            A | a)
                standalone
                flag=1
            ;;
            B | b)
                masterSlave
                flag=1
            ;;
            C | c)
                sentinel
                flag=1
            ;;
            D | d)
                cluster
                flag=1
            ;;
            *) echo "Invalid choice";;
        esac
    done
}

function standalone() {
    mkdir -p $REDIS_HOME/redis-1/conf
    cp redis.conf $REDIS_HOME/redis-1/conf/
    docker-compose up -d redis-1
}

function masterSlave() {
    mkdir -p $REDIS_HOME/redis-{1..3}/conf
    cp redis.conf $REDIS_HOME/redis-1/conf/
    cp redis.conf $REDIS_HOME/redis-2/conf/
    cp redis.conf $REDIS_HOME/redis-3/conf/
    docker-compose up -d redis-1 redis-2 redis-3
}

function sentinel() {
    copyConfig ${#SENTINEL_PORTS[@]}
    docker-compose up -d
    sentinelMontior ${SENTINEL_PORTS[@]}
}

function cluster() {
    echo "cluster"
}

# 拷贝配置
function copyConfig {
    local max=$1
    for ((i=1; i<=$max; i++)) do
        mkdir -p $REDIS_HOME/redis-$i/conf
        cp redis.conf $REDIS_HOME/redis-$i/conf/
        cp sentinel.conf $REDIS_HOME/redis-$i/conf/
    done
}

# 设置哨兵监测节点
function sentinelMontior {
    local sentinel_ports=(${@})
    master_ip=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' redis-1)
    for port in ${sentinel_ports[@]}; do
        redis-cli -h 127.0.0.1 -p $port sentinel remove mymaster &> /dev/null
        redis-cli -h 127.0.0.1 -p $port sentinel monitor mymaster $master_ip 6379 2 &> /dev/null
    done
}

function main() {
    askAboutDeploy
}

main
