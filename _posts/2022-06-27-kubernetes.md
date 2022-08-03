---
layout:     post
title:      "常用 kubernetes 命令"
author:     "gdream"
header-img: "img/post-bg-2021.jpg"
catalog: true
tags:
    - k8s
---


# 常用 kubernetes 命令

## Work with app
### exec
在容器内执行一条命令，使用方式如下：
```bash
$ kubectl exec (POD | TYPE/NAME) [-c CONTAINER] [flags] -- COMMAND [args...]
# 在没有指定pod时，会自动选择资源下的第一个pod
# 在没有指定container时，会自动选择有kubectl.kubernetes.io/default-container注释的容器，找不到该注释时选择第一个容器
kubectl exec mypod -- date
kubectl exec mypod -c ruby-container -- date
kubectl exec mypod -c ruby-container -i -t -- bash -il
kubectl exec mypod -i -t -- ls -t /usr
kubectl exec deploy/mydeployment -- date
kubectl exec svc/myservice -- date
```
**注意** `COMMAND`不要加引号，例如使用`kubectl exec mypod -i -t -- ls -t /usr`，而不是 `kubectl exec mypod -i -t -- "ls -t /usr"`

### port-forward
将一个或多个端口转发到pod
```bash
$ kubectl port-forward TYPE/NAME [options] [LOCAL_PORT:]REMOTE_PORT [...[LOCAL_PORT_N:]REMOTE_PORT_N]
# 将本地的8888端口转发到mypod的5000端口， --address 0.0.0.0表示所有IP都支持
$ kubectl port-forward --address 0.0.0.0 pod/mypod 8888:5000
```
