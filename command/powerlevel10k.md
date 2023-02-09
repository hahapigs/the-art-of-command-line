### Mac OS X

#### homebrew 安装

``` powershell
$ brew install romkatv/powerlevel10k/powerlevel10k
$ echo "source $(brew --prefix)/opt/powerlevel10k/powerlevel10k.zsh-theme" >>~/.zshrc
```

#### 手动安装

``` powershell
$ git clone --depth=1 https://github.com/romkatv/powerlevel10k.git ~/powerlevel10k
$ echo 'source ~/powerlevel10k/powerlevel10k.zsh-theme' >>~/.zshrc

# 国内用户可以使用 gitee.com 上的官方镜像加速下载
$ git clone --depth=1 https://gitee.com/romkatv/powerlevel10k.git ~/powerlevel10k
$ cho 'source ~/powerlevel10k/powerlevel10k.zsh-theme' >>~/.zshrc
```

#### oh-my-zsh 安装

``` powershell
$ git clone --depth=1 https://github.com/romkatv/powerlevel10k.git ${ZSH_CUSTOM:-$HOME/.oh-my-zsh/custom}/themes/powerlevel10k

# 国内用户可以使用 gitee.com 上的官方镜像加速下载
$ git clone --depth=1 https://gitee.com/romkatv/powerlevel10k.git ${ZSH_CUSTOM:-$HOME/.oh-my-zsh/custom}/themes/powerlevel10k
```

```tex
Set ZSH_THEME="powerlevel10k/powerlevel10k" in ~/.zshrc
```

#### antigen 安装

``` text
Add antigen theme romkatv/powerlevel10k to ~/.zshrc. Make sure you have antigen apply somewhere after it.
```

#### Zplug 安装

``` tex
Add zplug romkatv/powerlevel10k, as:theme, depth:1 to ~/.zshrc.
```





