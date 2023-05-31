

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





