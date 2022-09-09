### 一、替换

##### 1、语法
```powershell
:[range]s/source/target/[option]
```
##### 2、range
range标示要替换的范围，想要全局替换的话，可以使用一个百分号
```powershell
:%s/xxx/xxxx
```
另外，小数点标示当前行，$ 表示最后一行，数字表示范围
```powershell
:1,.s			替换第一行到当前行
:.,$s     替换当前行到最后一行
:1,$s			替换第一行到最后一行，相当于 :%s
```
##### 3、source与target
表示源字符串与目标字符串
```powershell
:1,.s/123/456
```
表示把第一行到当前行的首次出现的123替换成456,注意是首次出现,如果要替换某一行全部源字符串需要在后面加上
```powershell
/g
```
另外,对于一些特殊字符比如小数点,斜杠,双引号等需要转义,方式是使用反斜杠,在需要转义的字符面前加一个反斜杠 如把
```powershell
"123//"
```
替换为
```powershell
'123\\'
```
命令如下:
```powershell
:s/\"123\/\/\"/\'123\\\\\'/g
```
因为
```powershell
"123//" 中
"     转义为      \"
/     转义为      \/
'123\\' 中
'     转义为      \'
\     转义为      \\
```
##### 4、option
选项如下:
```powershell
/g  全局替换
/c  确认
/p  替换结果逐行显示
```
注意选项的组合结果是
```powershell
/cg
/pc
```
这样的形式,而不是
```powershell
/c/g
/g/p
```
##### 总结
全局替换的话,使用
```powershell
:%s/source/target/g
```
局部替换的话,使用
```powershell
:n,ms/source/target
```
n,m为行数,表示要替换的范围.。注意如果字符串需要转义要加上反斜杠。

### 二、批量操作
##### 批量操作插入

1. ctrl + v 进去块选择模式，位移选择需要编辑的行
1. 按大写 I 进入首行插入模式
1. 输入需要插入的字符串
1. 按两次 “Esc”
##### 批量操作删除

1. ctrl + v 进去块选择模式，位移选择需要编辑的行
1. 按 d 删除
##### 替换命令批量操作
```powershell
# 第1-6行从行首插入#
:1,6s/^/#/g
# 所有行行首插入#
:%s/^/#/g
# 所有行行尾插入#
:%s/$/#/g
# 所有行行尾去除#
:%s/#$//g
```

### 三、剪切到内容到vim之外的系统剪切板
```powershell
"+nyy		# n代表行数
```

#### NerdTree 插件
```basic
let mapleader = ","			" 定义 <leader> 快捷键
nnoremap <silent> <leader>n :NERDTreeToggle<cr>		" ,n 打开窗口，或者使用命令模式：NERDTree 打开
```


