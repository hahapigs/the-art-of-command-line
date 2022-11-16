
<a name="4Cr0v"></a>
## 安装
<a name="5zuL6"></a>
### 安装方式

<a name="Myqpy"></a>
#### 官网安装
```powershell
# 安装
$ /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)"
# 卸载
$ /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/uninstall)"
```
说明：官网给出的安装方式，不适合国内用户，由于网络资源被墙和一些质量问题，经常出现** port 443: Connection refused **的情况，用户可以通过更换网络为「手机热点」进行安装。

<a name="pepJa"></a>
#### 使用国内源安装
```powershell
# 安装
$ /bin/zsh -c "$(curl -fsSL https://gitee.com/cunkai/HomebrewCN/raw/master/Homebrew.sh)"
# 卸载
$ /bin/zsh -c "$(curl -fsSL https://gitee.com/cunkai/HomebrewCN/raw/master/HomebrewUninstall.sh)"
```

<a name="g6uBV"></a>
#### 手动替换镜像源安装
1、获取 install 文件
```powershell
# 终端命令获取
$ curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install >> brew_install
```
说明：还可以通过浏览器访问 [链接](https://raw.githubusercontent.com/Homebrew/install/master/install) 地址获取，复制粘贴到 brew_install 文件。如果 wifi 访问不到，可以通过手机热点访问。

2、更改 install 文件替换成国内地址

注释如下代码
```powershell
# BREW_REPO = "https://github.com/Homebrew/brew".freeze
# CORE_TAP_REPO = "https://github.com/Homebrew/homebrew-core".freeze
```
修改为如下代码
```powershell
BREW_REPO = "git://mirrors.ustc.edu.cn/brew.git".freeze
CORE_TAP_REPO = "git://mirrors.ustc.edu.cn/homebrew-core.git".freeze
```
3、执行安装
```powershell
$ /usr/bin/ruby ~/brew_install
```

<a name="WbZuq"></a>
#### 修改host地址后安装
1、安装 Xcode 和 Command Line Tools for Xcode 
Xcode 和 Command Line Tools for Xcode 可以通过 AppStore 安装，也可以通过终端安装，或者通过苹果 [developer](https://developer.apple.com/download/more/) 官网安装。
```powershell
# 删除已有的 commandLineTools
$ sudo rm-rf/Library/Developer/CommandLineTools
# 安装 Command Line Tools for Xcode
$ xcode-select --install
# 或者直接更新
$ softwareupdate --all --install --force
```
2、查阅真实IP
登录 [ipaddress](https://www.ipaddress.com/) 查询 _raw.githubusercontent.com _真实IP
![image.png](https://cdn.nlark.com/yuque/0/2020/png/725923/1599892918585-ff164af2-8e34-4327-b413-3868e6966114.png#align=left&display=inline&height=173&margin=%5Bobject%20Object%5D&name=image.png&originHeight=346&originWidth=848&size=31962&status=done&style=none&width=424)
3、修改 hosts 文件
```powershell
# 使用管理员权限打开 hosts文件
$ sudo vim /private/etc/hosts
# 追加地址
199.232.68.133 raw.githubusercontent.com
```
4、执行安装
```powershell
$ /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)"
```

<a name="cc514ef6"></a>
### 查看当前 Homebrew 镜像源

<a name="afUxq"></a>
#### brew.git 镜像源
```powershell
$ git -C "$(brew --repo)" remote -v
# 目录
$ cd "$(brew --repo)"
```
<a name="XQNJ0"></a>
#### homebrew-core.git 镜像源
```powershell
$ git -C "$(brew --repo homebrew/core)" remote -v
# 目录
$ cd "$(brew --repo)"/Library/Taps/homebrew/homebrew-core
```
<a name="BHqXk"></a>
#### homebrew-cask.git 镜像源
```powershell
$ git -C "$(brew --repo homebrew/cask)" remote -v
# 目录
$ cd "$(brew --repo)"/Library/Taps/homebrew/homebrew-cask
```
<a name="C5wCW"></a>
#### bottles 镜像源
```powershell
# 在 ~/.bash_profile 或 ~/.zshrc 文件中查看
HOMEBREW_BOTTLE_DOMAIN=https://mirrors.ustc.edu.cn/homebrew-bottles/
```

<a name="Goosj"></a>
### 国内镜像源地址

<a name="Wf1nq"></a>
#### 科大：[[https://mirrors.ustc.edu.cn] ](https://link.zhihu.com/?target=https%3A//links.jianshu.com/go%3Fto%3Dhttps%253A%252F%252Fmirrors.ustc.edu.cn)
<a name="7f040817"></a>
#### 阿里：[[https://mirrors.aliyun.com/homebrew/] ](https://link.zhihu.com/?target=https%3A//links.jianshu.com/go%3Fto%3Dhttps%253A%252F%252Fmirrors.aliyun.com%252Fhomebrew%252F)

<a name="3uQJl"></a>
### 替换 科大/阿里 镜像源
```powershell
$ git -C "$(brew --repo)" remote set-url origin https://mirrors.ustc.edu.cn/brew.git
$ git -C "$(brew --repo homebrew/core)" remote set-url origin https://mirrors.ustc.edu.cn/homebrew-core.git
$ git -C "$(brew --repo homebrew/cask)" remote set-url origin https://mirrors.ustc.edu.cn/homebrew-cask.git

if [ $SHELL = “/bin/bash” ] # 如果你的是bash
then 
    echo ‘export HOMEBREW_BOTTLE_DOMAIN=https://mirrors.ustc.edu.cn/homebrew-bottles/' >> ~/.bash_profile
    source ~/.bash_profile
elif [ $SHELL = “/bin/zsh” ] # 如果用的shell 是zsh 的话
then
    echo ‘export HOMEBREW_BOTTLE_DOMAIN=https://mirrors.ustc.edu.cn/homebrew-bottles/' >> ~/.zshrc
    source ~/.zshrc
fi

$ brew update
```

<a name="w1DbN"></a>
### 恢复镜像源
```powershell
$ git -C "$(brew --repo)" remote set-url origin https://github.com/Homebrew/brew.git
$ git -C "$(brew --repo homebrew/core)" remote set-url origin https://github.com/Homebrew/homebrew-core.git
$ git -C "$(brew --repo homebrew/cask)" remote set-url origin https://github.com/Homebrew/homebrew-cask.git

# 找到 ~/.bash_profile 或者 ~/.zshrc 中的HOMEBREW_BOTTLE_DOMAIN 一行删除

$ brew update

# 如果不行的话可以依次尝试以下命令
$ brew doctor
$ brew update-reset
$ brew update
```

<a name="UvvbD"></a>
## 使用
```powershell
$ brew update 											# 更新 homebrew
$ brew upgrade      								# 更新所有安装过的软件
$ brew upgrade openjdk  						# 更新指定软件
$ brew upgrade --cask alfred 				# 更新指定的 cahomebrew/cask 软件
$ brew install opnejdk							# 安装指定软件
$ brew install --cask alfred				# 装指定的 homebrew/cask 软件
$ brew uninstall openjdk						# 卸载指定的软件
$ brew uninstall --cask alfred			# 卸载指定的 homebrew/cask 软件
$ brew reinstall openjdk						# 重新安装指定的软件
$ brew reinstall --cask alfred			# 重新安装指定的 homebrew/cask 软件
$ brew search dash 		# 查找软件
$ brew info dash			# 查看软件安装信息
$ brew deps openjdk 	# 列出软件包的依赖关系
$ brew list 					# 列出已安装的软件
$ brew list --cask		# 列出已装的 homebrew/cask 软件
$ brew outdated 			# 列出可以更新的软件包
$ brew outdated --cask 		# 列出可以更新的 homebrew/cask 软件包
$ brew doctor					# 监测
$ brew services list	# 列出已安装的服务
$ brew services start mysql			# 启动指定的 mysql 服务
$ brew services stop mysql 			# 停止指定的 mysql 服务
$ brew services restart mysql 	# 重启指定的 mysql 服务
$ brew tap						# 查看软件仓库
```
```powershell
# 目前已安装的仓库
$ brew tap
homebrew/cask
homebrew/cask-fonts
homebrew/cask-versions
homebrew/core
homebrew/services
pivotal/tap
possatti/possatti
```

<a name="4JKgg"></a>
## 问题：
<a name="M0sPW"></a>
### 1、To retry an incomplete download, remove the file above.
```powershell
$ brew upgrade --cask eudic
==> Upgrading 1 outdated package:
eudic 2020-04-27,3.9.6 -> 3.9.9,2020-08-07
==> Upgrading eudic
==> Downloading https://static.frdic.com/pkg/eudicmac.dmg?v=2020-08-07
######################################################################## 100.0%
==> Verifying SHA-256 checksum for Cask 'eudic'.
==> Note: Running `brew update` may fix SHA-256 checksum errors.
==> Purging files for version 3.9.9,2020-08-07 of Cask eudic
Error: Checksum for Cask 'eudic' does not match.
Expected: ddd1c890d2affdf7067785f56c7572b80842ac2fde317f663d1f6d6739d46348
  Actual: 2c5af4bdeb6b918c3007b97189669f1fdaabd8929730a4bb6df3e3d367475cfe
    File: /Users/zhaohongliang/Library/Caches/Homebrew/downloads/c821d55c4a678722feffe6f38bb87cfcf2be46bc98c9e9f18f8bb8e09537a1bc--eudicmac.dmg
To retry an incomplete download, remove the file above.
If the issue persists, visit:
  https://github.com/Homebrew/homebrew-cask/blob/HEAD/doc/reporting_bugs/checksum_does_not_match_error.md

```
![image.png](https://cdn.nlark.com/yuque/0/2020/png/725923/1599883268308-b760da3e-b6c3-4eba-ba75-69020dcca425.png#align=left&display=inline&height=297&margin=%5Bobject%20Object%5D&name=image.png&originHeight=594&originWidth=1614&size=155435&status=done&style=none&width=807)
<a name="RDBL5"></a>
##### 解决：
```powershell
# 清理并更新 homebrew
$ brew cleanup && brew update
# 根据提示，删除文件
$ rm -rf /Users/zhao/Library/Caches/Homebrew/downloads/c821d55c4a678722feffe6f38bb87cfcf2be46bc98c9e9f18f8bb8e09537a1bc--eudicmac.dmg
# 重新安装或更新
$ brew upgrade --cask eudic

# 重新安装结果如下
Updating Homebrew...
==> Upgrading 1 outdated package:
eudic 2020-04-27,3.9.6 -> 3.9.9,2020-08-07
==> Upgrading eudic
==> Downloading https://static.frdic.com/pkg/eudicmac.dmg?v=2020-08-07
######################################################################## 100.0%
==> Verifying SHA-256 checksum for Cask 'eudic'.
==> Backing App 'Eudic.app' up to '/usr/local/Caskroom/eudic/2020-04-27,3.9.6/Eudic.app'.
==> Removing App '/Applications/Eudic.app'.
==> Moving App 'Eudic.app' to '/Applications/Eudic.app'.
==> Purging files for version 2020-04-27,3.9.6 of Cask eudic
🍺  eudic was successfully upgraded!
```
<a name="YhrPK"></a>
### 2、dowload 地址失效，或安装指定地址的App，如何解决？
<a name="s7G8W"></a>
##### 解决：
```powershell
# 安装 firefox 
$ brew install --cask firefox
==> Downloading https://download-installer.cdn.mozilla.net/pub/firefox/releases/82.0/mac/en-US/Firefox%2082.0.dmg 
###########################################					59.9%^C

# 不想安装 en-US 链接的版本，可以使用 brew edit 修改 firefox.rb 文件安装
$ brew cask edit firefox

## Atom 编辑器打开 firefox.rb 找到如下， 
## 去除 「英文」 language "en", default: true do, 
## 设置 「中文」 language "zh", default: true do

	language "en" do
    sha256 "5b651f2fa3c2d267c23184d29b6e6237af290575168e416ecef23128d94e8d5b"
    "en-US"
  end
	...... # 省略部分代码
  language "zh", default: true do
    sha256 "a276e07f110c02f9bb459bff3c9c4150f60176995ce1a29a4155f1b59eaf8b2f"
    "zh-CN"
  end
  
# 重新安装
$ brew install --cask firefox
==> Downloading https://download-installer.cdn.mozilla.net/pub/firefox/releases/82.0/mac/zh-CN/Firefox%2082.0.dmg
######################################################################## 100.0%
==> Verifying SHA-256 checksum for Cask 'firefox'.
==> Installing Cask firefox
==> Moving App 'Firefox.app' to '/Applications/Firefox.app'.
🍺  firefox was successfully installed!
```


