### [GitHub](https://github.com/tnorthcutt/lsd)

#### Mac OS X 安装
```powershell
$ brew install lsd
$ lsd
$ vim ~/.bash_profile
```
#### Arch Linux 安装
```powershell
$ sudo pacman -S lsd
```
在 ~/.bash_profile 添加如下代码
```
# vim ~/.bash_profile 添加如下命令

[[ -f ~/.bashrc ]] && . ~/.bashrc
alias ls = 'lsd'
```
最后执行
```powershell
$ source ~/.bash_profile
```
如果安装了 oh-my-zsh ，则在 ~/.zshrc 中添加 source ~/.bash_profile，注意：此命令一定要在 source $ZSH/oh-my-zsh.sh 之后

![image.png](https://cdn.nlark.com/yuque/0/2021/png/725923/1619528528448-ab3cda88-c7e3-48b8-bce6-2ae049ddf91d.png#align=left&display=inline&height=1080&id=u6862fd20&margin=%5Bobject%20Object%5D&name=image.png&originHeight=1080&originWidth=1920&size=436134&status=done&style=none&width=1920)




