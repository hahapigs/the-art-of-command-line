### [GitHub](https://github.com/athityakumar/colorls)

#### Mac OS X 安装
```powershell

# step1 mac 首先安装ruby
$ brew install ruby
# archlinux 执行如下
# sudo pacman -S ruby

# 如果安装数据源问题，可以更换清华大学源，并移除默认源
$ gem sources -add https://mirrors.tuna.tsinghua.edu.cn.rubygems/ --remove https://rubygems.org/
# 检查是否配置成功
$ gem sources -l

# step2  安装
$ gem install colorls

# step3  如果使用出现问题，请尝试
$ rbenv rehash
$ rehash

# step 4  在 ~/.zshrc 
$ vim ~/.zshrc
```
在 ~/.zshrc  添加如下代码
```
plugins=(git autojump z zsh-autosuggestions zsh-syntax-highlighting)

source $ZSH/oh-my-zsh.sh 
source ~/.bash_profile

source $(dirname $(gem which colorls))/tab_complete.sh
```
 然后查找 gem which colorls 目录
```powershell
$ gem which colorls
/home/zhl/.local/share/gem/ruby/3.0.0/gems/colorls-1.4.4/lib/colorls.rb
```
编辑 sudo vim /etc/profile, 添加 bin 目录
```
append_path '/usr/local/sbin'
append_path '/usr/local/bin'
append_path '/usr/bin'
append_path '/home/zhl/.local/share/gem/ruby/3.0.0/bin'
```
然后映射命令
```
alias ls="colorls"
```
重启终端，或者执行如下命令
```powershell
$ source /etc/profile
$ source ~/.zshrc
```
执行命令，效果如图
```powershell
$ ls
$ ll
$ ll -a 
```
 ![image.png](https://cdn.nlark.com/yuque/0/2021/png/725923/1619527718707-9f7a78bb-e224-4c7e-82d5-8c0e20b500e9.png#height=1080&id=u5c502ec9&margin=%5Bobject%20Object%5D&name=image.png&originHeight=1080&originWidth=1920&originalType=binary&ratio=1&size=443261&status=done&style=none&width=1920)

