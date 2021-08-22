---
layout:     post
title:      "Java Agent技术"
subtitle:   "Java动态调试技术原理"
author:     "gdream"
header-img: "img/post-bg-2021.jpg"
mathjax: true
catalog: true
tags:
    - Java
---

> Java语言因为其面向对象、跨语言性和自动垃圾回收等特性深受广大程序员的喜欢，Java语言通过JVMTI, (JVM Tool Interface) 提供了非常丰富的接口，帮助开发人员获取JVM运行时的状态以及应用执行的控制权。

# JVMTI概述
JVMTI是开发和监控工具使用的编程接口，它提供了两个方法：检查java进程的状态和控制Java程序的执行。
JVMTI 旨在为需要访问 VM 状态的所有工具提供 VM 接口，包括但不限于：分析、调试、监控、线程分析和覆盖率分析工具。
JVMTI 不保证在所有的java虚拟机中都可以用。
JVMTI 是一个双向接口， JVMTI的客户端，又称agent，可以注册监听JVM的事件， 通过事件通知或其他方法，agent可以控制应用程序或查询应用的程序的信息。
Agent与JVM在同一个进程里直接通信