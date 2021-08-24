---
layout:     post
title:      "golang面试题"
subtitle:   "总结的一些go语言中经常用错的地方"
date:       2021-08-24 12:00:00
author:     "gdream"
header-img: "img/post-business.jpeg"
catalog: true
tags:
    - golang
    - 面试
---

## 1. 基础问题
### 1.1. channel
#### 1.1.1. channel关闭之后，继续读写会怎么样？
可读不可写， 读取关闭的通道会直接返回通道类型的默认值，往关闭的通道写数据会panic
```go
func channelDemo() {
	ch := make(chan int, 1)
	close(ch)
	fmt.Println(fmt.Sprintf("Get from a closed channel, value: %d", <-ch)) // Get from a closed channel, value: 0
	// set to a closed channel
	ch <- 1 // panic: send on closed channel
}
```

#### 1.1.2. channel关闭之后，for + select 会怎样？
由于通道关闭，所以select每次都可以从通道中获取默认值，而且不会阻塞，例如：
```go
func channelDemo2() {
	ch := make(chan int, 1)
	close(ch)
	count := 0
	for {
		select {
		case v := <-ch:
			fmt.Println(fmt.Sprintf("Get from a closed channel, value: %d", v))
		default:
			fmt.Println("no data")
		}
		count++
		if count > 10 {
			break
		}
	}
}
```
output:
```
Get from a closed channel, value: 0
Get from a closed channel, value: 0
Get from a closed channel, value: 0
Get from a closed channel, value: 0
Get from a closed channel, value: 0
Get from a closed channel, value: 0
Get from a closed channel, value: 0
Get from a closed channel, value: 0
Get from a closed channel, value: 0
Get from a closed channel, value: 0
Get from a closed channel, value: 0
```
那么如何避免这种情况呢， 其实从通道里取数据的时候是有两个参数的`v, ok := <-ch`，我们可以通过ok去判断通道是否已经关闭，如果已经关闭就将其置为nil，这样下次就不会继续进入这个case了，代码如下：
```go
    select {
		case v, ok := <-ch:
			if !ok {
				ch = nil
			}
			fmt.Println(fmt.Sprintf("Get from a closed channel, value: %d", v))
        default:
			fmt.Println("no data")
		}
```
**注意:** 上例中如果select中如果没有default，那么就会发生：fatal error: all goroutines are asleep - deadlock!
所以如果检测到通道已经关闭，要么退出for循环，要么添加一个default的case。 **注意:** select中的break不能退出for循环

#### 1.1.3. 读写未初始化的通道会出现什么问题？
答：channel如果未初始化的话，那么值为nil，那么无论读写都会报错。看实例：
```go
func channelDemo3() {
	var ch chan int
	// ch <- 1 // fatal error: all goroutines are asleep - deadlock! goroutine 1 [chan send (nil chan)]:
	<-ch // fatal error: all goroutines are asleep - deadlock! goroutine 1 [chan receive (nil chan)]:
}
```

### 1.2. map
#### 1.2.1. map中的值如果是结构体会怎么样
```go
func structDemo() {
	m := map[string]Data{"x": {name: "one"}}
	// m["x"].name = "two"  // cannot assign to struct field m["x"].name in map
	d := m["x"]
	d.name = "two"
	fmt.Println(m) // map[x:{one}]

	mp := map[string]*Data{"x": {name: "one"}}
	mp["x"].name = "two"
	fmt.Println(mp["x"].name) // two
	dp := mp["x"]
	dp.name = "three"
	fmt.Println(mp["x"].name) // three
}
```