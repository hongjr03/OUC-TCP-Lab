# OUC 计算机网络 TCP 实验

<img width="380" alt="image" src="https://github.com/user-attachments/assets/65e42855-c503-4521-ac7c-48e4ab9e5829" />

本仓库为2024秋洪锋老师计算机网络课程的期末TCP课程设计，基于提供的 [RDT 1.0](https://github.com/hongjr03/OUC-TCP-Lab/tree/RDT1.0-initial)
代码迭代实现了以下分支的协议。需要注意的是由于才疏学浅，实现的内容并不完全和协议相符，仅供参考，如有问题欢迎发 Issue 告知！

## 实验报告

[实验报告](实验报告.pdf) 使用 [Typst](https://github.com/typst/typst)
和模版 [Typst-Assignment-Template](https://github.com/hongjr03/Typst-Assignment-Template) 撰写。

## 分支概览

- [RDT 1.0](https://github.com/hongjr03/OUC-TCP-Lab/tree/RDT1.0-initial)
- [RDT 2.x](https://github.com/hongjr03/OUC-TCP-Lab/tree/RDT2.x)
- [RDT 3.0](https://github.com/hongjr03/OUC-TCP-Lab/tree/RDT3.0)
- [Select Response](https://github.com/hongjr03/OUC-TCP-Lab/tree/Select-Response)
- [Go Back N](https://github.com/hongjr03/OUC-TCP-Lab/tree/Go-Back-N)
- [TCP](https://github.com/hongjr03/OUC-TCP-Lab/tree/TCP)
- [TCP Tahoe](https://github.com/hongjr03/OUC-TCP-Lab/tree/TCP-Tahoe)
- [TCP Reno](https://github.com/hongjr03/OUC-TCP-Lab/tree/TCP-Reno)

## Tahoe 和 Reno 的说明

SenderWindow 和 SenderWindowViz 的实现是一样的，只是 SenderWindowViz 记录了不同时刻的窗口大小，用于绘制窗口大小的变化图。对于cwnd慢开始和拥塞控制的处理有误，原因是每收到1个ACK并不意味着发出了1个包，这里需要修改。

## Star History

<a href="https://star-history.com/#hongjr03/OUC-TCP-Lab&Date">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=hongjr03/OUC-TCP-Lab&type=Date&theme=dark" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=hongjr03/OUC-TCP-Lab&type=Date" />
   <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=hongjr03/OUC-TCP-Lab&type=Date" />
 </picture>
</a>
