# Sample

This is for SmallDemo user.

## Getting Started

### Step 1. Clone SmallDemo (下载源码)
    > cd [你要放SmallDemo的目录]
    > git clone https://github.com/tgzzl/SmallDemo.git

> 强烈建议使用git命令行，方便更新维护。Windows用户入口：[Git for Windows][git-win]<br/>
> 后续更新可以使用命令：git pull origin master
  
### Step 2. Import SmallDemo project (导入示例工程)
打开Android Studio，File->New->Import Project... 选择**SmallDemo**文件夹，导入。

![SmallDemo sample][ic-sample]

* Sample `示例工程`
  * app `宿主工程`
  * app.\* `包含Activity/Fragment的组件`
  * lib.\* `公共库组件`
  * web.\* `本地网页组件`
  * sign `签名文件`

> 顺便说下，这些app.\*跟web.\*可以从工具栏的![▶️][as-run]按钮单独运行。<br/>
> 其中app.home无法单独运行是因为它只包含一个Fragment，没有Launcher Activity。

## License
Apache License 2.0

[git-win]: http://git-scm.com/downloads
[as-run]: http://developer.android.com/images/tools/as-run.png
[ic-sample]: http://code.wequick.net/assets/images/small-sample.png
