

``` powershell
# 查看计算机处理器的可用核心数
$ sysctl -n hw.ncpu

# 查看计算机处理器的型号、核心数和线程数
$ sysctl -n machdep.cpu.brand_string

# 查看计算机物理cpu处理数量
$ sysctl -n hw.physicalcpu

# 查看计算机虚拟逻辑CPU处理器数量
$ sysctl -n hw.logicalcpu
```



``` powershell
# 将文件内容拷贝到剪切板
$ pbcopy < path/to/file
$ cat path/to/file | pbcopy
# 查找后缀 ".md" 文件，把查找结果拷贝到剪切板
$ find . -name "*.png" | pbcopy

# 将剪切板的内容拷贝进文件
$ pbpaste > path/to/file
# 剪切板内容过滤关键词
$ pbpaste | grep foo
```

