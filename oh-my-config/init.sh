

# instarll brew
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)"

# curl
brew install curl

# weget
brew install wget

# zsh
brew install zsh


# install oh-my-zsh
# curl
# sh -c "$(curl -fsSL https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"
# 国内源
sh -c "$(curl -fsSL https://gitee.com/mirrors/oh-my-zsh/raw/master/tools/install.sh)"

# wget
# sh -c "$(wget -O- https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"
# sh -c "$(wget -O- https://gitee.com/pocmon/mirrors/raw/master/tools/install.sh)"

# fetch
# sh -c "$(fetch -o - https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"

# install powerlevel10k
# 推荐 homebrew、oh-my-zsh 、antigen 、手动方式安装
brew install romkatv/powerlevel10k/powerlevel10k
echo "source $(brew --prefix)/opt/powerlevel10k/powerlevel10k.zsh-theme" >> ~/.zshrc

# lsd (colorls在mac上安装需要先安装ruby，推荐安装lsd)
brew install lsd

# autojump
brew install autojump
echo "[ -f /usr/local/etc/profile.d/autojump.sh  ] && . /usr/local/etc/profile.d/autojump.sh" >>  ~/.zshrc

# z (autojump和z安装一种就可以)
brew install z
echo "[ -f /usr/local/etc/profile.d/z.sh  ] && . /usr/local/etc/profile.d/z.sh" >>  ~/.zshrc

# mtr
brew install mtr

# httpie
brew isntall httpie

# jq
brew install jq

# fzf
brew install fzf

## cask
brew install --cask fig

# 禁止生成.DS_Store
defaults write com.apple.desktopservices DSDontWriteNetworkStores -bool TRUE

# 恢复生成.DS_Store
# defaults delete com.apple.desktopservices DSDontWriteNetworkStores

# 删除当前目录的.DS_Store
# find . -name '.DS_Store' -type f -delete
# 删除所有的.DS_Store
# sudo find / -name '.DS_Store' --depth -exec rm {};

# 管理java环境
brew install jenv

brew untap homebrew/cask-versions
brew untap caskroom/versions
brew tap homebrew/cask-versions

# openJDK 8
brew install temurin8
# openJDK 11
brew install temurin11
# openJDK 17
brew install temurin17

# maven
brew install maven@3.5

# mysql
brew install mysql


# mycli
brew install mycli

brew install --cask iterm2

brew install --cask alfred

brew install --cask the-unarchiver

brew install --cask iina

brew install --cask ssh-tunnel-manager












