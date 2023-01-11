#### Linux 安装 node

```powershell
# 下载需要安装的 node 包，也可以使用 wget 方式下载

# 解压文件到指定目录，当前是到 /opt 目录
$  tar -xvf node-v19.3.0-linux-x64.tar.xz -C /opt

# 重命名文件夹
$ mv node-v19.3.0-linux-x64 node-v19.3.0

# 安装 yarn
$ npm install -g yarn

# 创建软连接
$ ln -s /opt/node-v19.3.0/bin/npm /usr/local/bin/npm
$ ln -s /opt/node-v19.3.0/bin/node /usr/local/bin/node
$ ln -s /opt/node-v19.3.0/bin/yarn /usr/local/bin/yarn

$ npm --version
$ node --version
$ yarn --version
 
```

