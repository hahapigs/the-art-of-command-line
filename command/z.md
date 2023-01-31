### Mac OS X

#### Homebrew 安装

``` powershell
$ brew install z
$ echo "[ -f /usr/local/etc/profile.d/z.sh ] && . /usr/local/etc/profile.d/z.sh" >>  ~/.zshrc
```

#### ArchLinux or ManjaroLinux

```powershell
$ sudo pacman -S z
$ echo "[ -f /usr/local/etc/profile.d/z.sh ] && . /usr/local/etc/profile.d/z.sh" >>  ~/.zshrc
```

#### 手动安装

``` powershell
$ cd ~
$ curl -o .z.sh https://raw.githubusercintent.com/rupa/z/master/z.sh
$ echo "[ -f /usr/local/etc/profile.d/z.sh ] && . /usr/local/etc/profile.d/z.sh" >>  ~/.zshrc
```



