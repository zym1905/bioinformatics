# Python 环境变量

---

### 整体介绍
Python是一个脚本语言，主要依赖其丰富的开发包，从而方便的实现机器学习，数据分析画图等诸多应用。开发环境搭建主要从2个层面来讲，首先是Python环境和其库的安装工具，其次是开发IDE。虽然对于高手来说可以自己setup一个比较自我定制的场景，但是这过于复杂，这里主要介绍Anaconda和PyCharm两个工具。
### Anaconda工具
	Anaconda一个管理Python，R等数据分析语言环境管理和包管理的工具，详细可以参考：https://www.jianshu.com/p/62f155eb6ac5，这里仅仅将重点列出来：

####1. 安装
安装对应版本Anaconda，可以在官网选择对应系统的版本，建议Python选择3，因为2后续就会逐渐不维护了。官网下载地址：https://www.anaconda.com/distribution/。

####2. 环境管理
 
首先创建环境：
```
conda create --name <env_name> <package_names>

例子：
conda create -n python3 python=3.7
```
激活和取消激活创建的环境：
```
conda activate <env_name>
conda deactivate <env_name>

例子：
conda activate python3
conda deactivate python3
```
####3. 包安装
首先要配置好源，这里国内建议使用清华的镜像。首先编辑~/.condarc文件，换成以下内容。
```
channels:
  - defaults
show_channel_urls: true
channel_alias: https://mirrors.tuna.tsinghua.edu.cn/anaconda
default_channels:
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/main
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/free
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/r
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/pro
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/msys2
custom_channels:
  conda-forge: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
  msys2: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
  bioconda: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
  menpo: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
  pytorch: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
  simpleitk: https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud
```
    需要说明的是国外的情况不同，不过一般主要是用bioconda，所以可以参考：https://bioconda.github.io/user/install.html的说明进行源的添加。
然后清理索引缓存，保证后续使用新的源：
```
conda clean -i
```
后面便可以搜索和安装包了：
```
conda search <[channel::]package_name>
conda install <[channel::]package_name>

注意：channel的部分是可选的，不过很多生物包都只能在bioconda，有时候需要添加
```

### pycharm IDE工具
#### 安装
安装参考官网，这里暂时不多做说明。

