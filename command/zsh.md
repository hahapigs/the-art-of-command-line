### Mac OS X

mac 系统已经内置了 zsh，如果不需要最新的特性，可以不安装新版本。

安装前检查一下是否安装，或者已经安装的版本

``` powershell
$ zsh --version
```

#### homebrew 安装

``` powershell
$ brew install zsh
```

#### cul 安装

``` powershell
$ sh -C "$(curl -fsSL https://raw.github.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"
```

#### wget 安装

``` powershell
$ sh -C "$(wget https://raw.github.com/ohmyzsh/ohmyzsh/master/tools/install.sh -O -)"
```

#### 查看当前 shell

``` powershell
$ echo $SHELL
```

#### 查看已安装 Shell

``` powershell
$ cat /etc/shells
```

#### 切换 shell

```powershell
$ chsh -s /bin/bin
```

也可以手动切换

``` powershell
# 切换 zsh, 在 /etc/shells 文件中追加一行 /bin/zsh
$ echo '/bin/zsh' >> /etc/shells
```

注意：设置默认 shell，需要重启终端才能生效