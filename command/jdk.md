#### Mac OS X 安装 jdk

```powershell
# 查看 jdk 版本
$ brew search temurin
# 查看 tap list
$ brew tap
# 安装 cask-versions
$ brew tap homebrew/cask-versions

# 找到需要的 temurin 版本 jdk，安装
$ brew install temurin8
$ brew install temurin11
$ brew install temurin17
$ brew install temurin18

# 查看已安装 jdk 版本
$  /usr/libexec/java_home -V
# 查看已安装 jdk 目录
$ ls /Library/Java/JavaVirtualMachines
```

