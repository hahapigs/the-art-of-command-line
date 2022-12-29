#### Mac OS X 安装

```powershell
# 安装
$ brew install jenv
# 查看版本
$ brew info jenv
# 增加配置到环境变量中
$ echo 'PATH="$PATH:$HOME/.jenv/bin"' >> ~/.bash_profile
$ echo 'eval "$(jenv init -)"' >> ~/.zshrc
$ source ~/.bash_profile
# 验证
$ jenv doctor
# 帮助
$ jenv --help


# 查看已安装 jdk 支持
$ /usr/libexec/java_home -V
# 查看已安装 jdk 目录结构
$ ls /Library/Java/JavaVirtualMachines
# 添加 jdk 支持
$ jenv add /Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home
$ jenv add /Library/Java/JavaVirtualMachines/temurin-11.jdk/Contents/Home
$ jenv add /Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
# 查看已添加 jdk 支持版本
$ jenv versions
# 查看当前 jdk 版本
$ jenv verion

# 删除 jdk 支持
$ jenv remove oracle64-11.0.16
$ jenv remove 11.0.16

# 设置当前项目 jdk 版本
$ jenv local 1.8
$ jenv local 11.0
# 设置全局 jdk 版本
$ jenv global 1.8
$ jenv global 11.0
# 设置 shell jdk 版本
$ jenv shell 1.8

# 查看 jenv 插件
$ jenv plugins
# 查看 jenv 已开启的插件
$ jenv plugins --enabled
# 开启插件，maven，如果不开启此插件，会导致 mvn 命令失效
$ jenv enable-plugin maven
# 开启插件，export，开启对 $JAVA_HOME 的支持
$ jenv enbale-plugin export
# 关闭插件
$ jenv disable-plugin maven

```

