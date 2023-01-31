### Mac OS X 

#### homebrew 安装

``` powershell
$ brew install autojump
$ echo "[ -f /usr/local/etc/profile.d/autojump.sh ] && . /usr/local/etc/profile.d/autojump.sh" >>  ~/.zshrc
```

### ArchLinux or ManjaroLinux

#### pacman

``` powershell
$ sudo pacman -S autojump
$ echo "[ -f /usr/local/etc/profile.d/autojump.sh ] && . /usr/local/etc/profile.d/autojump.sh" >>  ~/.zshrc
```

#### 手动安装

``` powershell
$ git clone git://github.com/wting/autojump.git
$ cd autojump
$ ./install.py or ./uninstall.py
```

