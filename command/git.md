<a name="VlAWV"></a>
### 查看分支
```powershell
# 查看本地分支
$ git branch
# 查看远程分支
$ git branch -r
# 查看本地和远程所有分支
$ git branch -a
# 模糊查找分支
$ git branch | grep 'master'
```
<a name="pmny8"></a>
### 创建分支
```powershell
# 创建 dev_2020-09-27 分支
$ git checkout -b dev_2020-09-27
```
<a name="ALtHQ"></a>
### 删除分支
```powershell
# 删除本地分支
$ git branch -D dev
# 删除远程分支
$ git push origin --delete dev
$ git push --delete origin dev
$ git push origin -d dev
$ git push -d origin dev
# 清理本地无效分支 (远程已删除分支，但本地没有删除分支)
$ git fetch -p
```
<a name="xgeTg"></a>
### 切换分支
```powershell
# switch 切换
$ git switch dev
# checkout 切换
$ git checkout master
```
<a name="AGUAD"></a>
### 合并分支
```powershell
# 切换到 master 分支
$ git switch master
# 合并 dev 分支, 不提交
$ git merge dev --no-commit
```
<a name="NEfyL"></a>
### 本地暂存
```powershell
# 当前修改临时暂存
$ git stash
# 接收最近一次临时暂存的修改
$ git stash apply
# 临时暂存清单
$ git stash list
# 清除暂存清单
$ git stash clear
# 接收最近一次暂存修改，并清除最后一次修改记录
$ git stash pop
# 查看临时暂存清单提交
$ git show stash@{0}
```
<a name="UOofL"></a>
### 提交记录
```powershell
# 查看所有提交记录
$ git log
# 查看本地提交记录
$ git reflog
# 用带参数的 git log 查看分支的合并情况
$ git log --pretty=oneline
$ git log --graph --pretty=oneline --abbrev-commit
```
![image.png](https://cdn.nlark.com/yuque/0/2021/png/725923/1609920521348-a8a0bd71-8470-4eda-b7ef-6c9166a75289.png#crop=0&crop=0&crop=1&crop=1&height=237&id=WHaXZ&margin=%5Bobject%20Object%5D&name=image.png&originHeight=474&originWidth=1254&originalType=binary&ratio=1&rotation=0&showTitle=false&size=85505&status=done&style=none&title=&width=627)
<a name="E1HKC"></a>
#### 说明：
–graph 图形
–pretty=oneline 减少数据
–abbrev-commit 头部数据减少

<a name="wBRac"></a>
### 远程地址
```powershell
# 查看当前的远程分支
$ git remote -v
# 替换远程地址
$ git remote set-url origin http://gitlab.chinahx.net.cn/service/advertservice.git
# 查看是否替换成功
$ git remote -v
```
<a name="NtlGE"></a>
### 版本回退
```powershell
# 回退到 HEAD 的上一个版本
$ git reset --hard HEAD^
# 回退到指定版本
$ git reset --hard d6d35313106b710f6d448fbbb4ba29c267bde14e
# 强推
$ git push -f
```
<a name="sMQYj"></a>
### 修改提交记录
```powershell
# 修改最近一次commit的信息，按a或o进入编辑，然后:wq保存退出
$ git commit --amend

# 修改最近两个或以上的commit信息，对应的pick改成e或者edit，然后:wq保存退出
$ git rebase -i HEAD~2
# 修改commit信息，按a或o进入编辑，然后:wq保存退出
$ git commit --amend
# 改好之后，用如下命令完成rebase
$ git rebase --continue
$ 提交到远程
$ git push -u origin master
```
<a name="R4hBL"></a>
### 把某次提交记录重新提交到新的分支
```powershell
# 打开当前分支，比如我是dev，先查看提交记录的日志
$ git log
# 找到你要重新的提交到其他分支的记录并复制 commit 日志
# 切换到要提交的新分支并拉取最新的代码
$ git checkout master
$ git checkout -b dev
# 提交到新的分支
$ git cherry-pick a223402db5bbb8c3f93144a0a988669b9350fd42
# 如果没有冲突，直接push
$ git push
# 如果有冲突，先解决冲突, add后，再cherry-pick，再push
$ git add 
$ git cherry-pick --continue
$ git push
```
