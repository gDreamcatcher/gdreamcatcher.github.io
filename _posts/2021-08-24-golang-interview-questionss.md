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

# 1. 面试题
## 1.1. 基础问题
### 1.1.1. channel
#### 1.1.1.1. channel关闭之后，继续读写会怎么样？
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

#### 1.1.1.2. channel关闭之后，for + select 会怎样？
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

#### 1.1.1.3. 读写未初始化的通道会出现什么问题？
答：channel如果未初始化的话，那么值为nil，那么无论读写都会报错。看实例：
```go
func channelDemo3() {
	var ch chan int
	// ch <- 1 // fatal error: all goroutines are asleep - deadlock! goroutine 1 [chan send (nil chan)]:
	<-ch // fatal error: all goroutines are asleep - deadlock! goroutine 1 [chan receive (nil chan)]:
}
```

### 1.1.2. 集合
#### 1.1.2.1. map中的值如果是结构体会怎么样
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

#### 1.1.2.2. nil切片和空切片的区别
nil切片和空切片指向的地址不一样。nil空切片引用数组指针地址为0(无指向任何实际地址)，空切片的引用数组指针地址是有的，且固定为一个值

#### 1.1.2.3. 拷贝大切片一定比小切片代价大吗？
并不是，所有切片的大小相同；三个字段（一个 uintptr，两个int）。切片中的第一个字是指向切片底层数组的指针，这是切片的存储空间，第二个字段是切片的长度，第三个字段是容量。将一个 slice 变量分配给另一个变量只会复制三个机器字。所以 拷贝大切片跟小切片的代价应该是一样的。

### 1.1.3. 字符串
#### 1.1.3.1. 字符串转字节数组需要拷贝吗？
字符串转字节数组需要拷贝 (准确的说，golang里只要有类型强转换的都会发生拷贝)，那么如何在不拷贝的情况下做到string转[]byte呢？

```go
func stringToBytes(s string) []byte {
	b := *(*[]byte)(unsafe.Pointer(
		&struct {
			string
			Cap int
		}{s, len(s)}))
	return b
}

func bytesToString(b []byte) string {
	s := *(*string)(unsafe.Pointer(&b))
	return s
}
```

# 2. Go modules

普大喜奔的是，从 Go 1.11 版本开始，官方已内置了更为强大的 [Go modules](https://golang.org/cmd/go/#hdr-Modules__module_versions__and_more) 来一统多年来 Go 包依赖管理混乱的局面(Go 官方之前推出的 [dep](https://github.com/golang/dep) 工具也几乎胎死腹中)，并且将在 1.13 版本中正式默认开启。

目前已受到社区的看好和强烈推荐，建议新项目采用 Go modules。



## 2.1. go mod命令

| command  | usage                                              |
| -------- | -------------------------------------------------- |
| download | 下载依赖的module到本地cache                        |
| edit     | 编辑go.mod                                         |
| graph    | 打印模块依赖图                                     |
| init     | 在当前文件夹下初始化一个新的module，创建go.mod文件 |
| vendor   | 将依赖复制到vendor下                               |
| verify   | 依赖校验                                           |
| why      | 解释为什么需要依赖                                 |
| tidy     | 增加丢失的module，去掉未使用的module               |





## 2.2. govendor子命令

| 子命令  | 功能                                                                   |
| ------- | ---------------------------------------------------------------------- |
| init    | 创建 `vendor` 目录和 `vendor.json` 文件                                |
| list    | 列出&过滤依赖包及其状态                                                |
| add     | 从 `$GOPATH` 复制包到项目 `vendor` 目录                                |
| update  | 从 `$GOPATH` 更新依赖包到项目 `vendor` 目录                            |
| remove  | 从 `vendor` 目录移除依赖的包                                           |
| status  | 列出所有缺失、过期和修改过的包                                         |
| fetch   | 从远程仓库添加或更新包到项目 `vendor` 目录(不会存储到 `$GOPATH`)       |
| sync    | 根据 `vendor.json` 拉取相匹配的包到 `vendor` 目录                      |
| migrate | 从其他基于 `vendor` 实现的包管理工具中一键迁移                         |
| get     | 与 `go get` 类似，将包下载到 `$GOPATH`，再将依赖包复制到 `vendor` 目录 |
| license | 列出所有依赖包的 LICENSE                                               |
| shell   | 可一次性运行多个 `govendor` 命令                                       |

## 2.3. govendor状态参数

| 状态      | 缩写 | 含义                                                 |
| --------- | ---- | ---------------------------------------------------- |
| +local    | l    | 本地包，即项目内部编写的包                           |
| +external | e    | 外部包，即在 `GOPATH` 中、却不在项目 `vendor` 目录   |
| +vendor   | v    | 已在 `vendor` 目录下的包                             |
| +std      | s    | 标准库里的包                                         |
| +excluded | x    | 明确被排除的外部包                                   |
| +unused   | u    | 未使用的包，即在 `vendor` 目录下，但项目中并未引用到 |
| +missing  | m    | 被引用了但却找不到的包                               |
| +program  | p    | 主程序包，即可被编译为执行文件的包                   |
| +outside  |      | 相当于状态为 `+external +missing`                    |
| +all      |      | 所有包                                               |


# 3. go语言基础

## 3.1. 输入输出

### 3.1.1. IO

最常用的接口，**go 语言接口建议以er结尾**

```go
// Reader 接口
type Reader interface {
	Read(p []byte) (n int, err error)
}
// Writer接口
type Writer interface {
	Write(p []byte) (n int, err error)
}
```



```go
// 这两个接口可以实现并发读写操作
type ReadAt interface{
  ReadAt(p []byte, offset int)(int, error) // 从指定偏移量读取数据
}
type WriteAt interface{
	WriteAt(p []byte, offset int)(int, error) // 从指定偏移量写入数据
}

// 这两个接口可以将reader和writer互相切换
type ReaderFrom interface {
    ReadFrom(r Reader) (n int64, err error)
}
type WriterTo interface {
    WriteTo(w Writer) (n int64, err error)
}

// Seeker 接口 Seek 设置下一次 Read 或 Write 的偏移量为 offset，它的解释取决于 whence：
// 0 表示相对于文件的起始处，1 表示相对于当前的偏移，而 2 表示相对于其结尾处。
// Seek 返回新的偏移量和一个错误，如果有的话。
const (
  SeekStart   = 0 // seek relative to the origin of the file
  SeekCurrent = 1 // seek relative to the current offset
  SeekEnd     = 2 // seek relative to the end
)
type Seeker interface {
    Seek(offset int64, whence int) (ret int64, err error)
}

// Closer接口 close之前应该检查一下
type Closer interface {
    Close() error
}
```



### 3.1.2. ioutil库

```go
// 一次读取io.Reader中的所有数据,正常返回byte数组和nil,
func ReadAll(r io.Reader) ([]byte, error)
func ReadFile(filename string) ([]byte, error)
func WriteFile(filename string, data []byte, perm os.FileMode) error
// 第一个参数如果为空，表明在系统默认的临时目录（ os.TempDir ）中创建临时目录；第二个参数指定临时目录名的前缀，该函数返回临时目录的路径。该函数生成的临时目录如果已经存在，会重试10000次。
func TempDir(dir, pattern string) (name string, err error) 
// 创建的临时文件
func TempFile(dir, pattern string) (f *os.File, err error)
// 删除临时文件和文件夹
defer func() {
  f.Close()
  os.Remove(f.Name())
}()
```



## 3.2. 文本

### 3.2.1. strings

```go
Compare(a,b  string) int  //用于比较两个字符串的大小，如果两个字符串相等，返回为 0。如果 a 小于 b ，返回 -1 ，反之返回 1 
EqualFold(s, t string) bool // 忽略大小写判读字符串是否相等
Contains(s, substr string) bool  // 子串substr是否包含在s中
ContainsAny(s, char string) bool // chars 中任何一个 Unicode 代码点在 s 中，返回 true
Count(s, sep string) int // 查找子串出现的次数
Split(s, sep string) {}string  // 字符串分割
SplitAfter(s, sep string) []string // 保留分隔符分割字符串
SplitN(s, sep string) []string  // 字符串分割 保留前N个
Fields(s string) []string  // 等同于FieldsFunc(s, unicode.IsSpace)
func FieldsFunc(s string, f func(rune) bool) []string
// s 中是否以 prefix 开始
func HasPrefix(s, prefix string) bool
// s 中是否以 suffix 结尾
func HasSuffix(s, suffix string) bool
// 在 s 中查找 sep 的第一次出现，返回第一次出现的索引
func Index(s, sep string) int
func Join(a []string, sep string) string
func Repeat(s string, count int) string
func Map(mapping func(rune) rune, s string) string
// 用 new 替换 s 中的 old，一共替换 n 个。
// 如果 n < 0，则不限制替换次数，即全部替换
func Replace(s, old, new string, n int) string
// 该函数内部直接调用了函数 Replace(s, old, new , -1)
func ReplaceAll(s, old, new string) string
func ToLower(s string) string
func ToLowerSpecial(c unicode.SpecialCase, s string) string
func ToUpper(s string) string
func ToUpperSpecial(c unicode.SpecialCase, s string) string
func Title(s string) string
func ToTitle(s string) string
func ToTitleSpecial(c unicode.SpecialCase, s string) string
// 将 s 左侧和右侧中匹配 cutset 中的任一字符的字符去掉
func Trim(s string, cutset string) string
// 将 s 左侧的匹配 cutset 中的任一字符的字符去掉
func TrimLeft(s string, cutset string) string
// 将 s 右侧的匹配 cutset 中的任一字符的字符去掉
func TrimRight(s string, cutset string) string
// 如果 s 的前缀为 prefix 则返回去掉前缀后的 string , 否则 s 没有变化。
func TrimPrefix(s, prefix string) string
// 如果 s 的后缀为 suffix 则返回去掉后缀后的 string , 否则 s 没有变化。
func TrimSuffix(s, suffix string) string
// 将 s 左侧和右侧的间隔符去掉。常见间隔符包括：'\t', '\n', '\v', '\f', '\r', ' ', U+0085 (NEL)
func TrimSpace(s string) string
// 将 s 左侧和右侧的匹配 f 的字符去掉
func TrimFunc(s string, f func(rune) bool) string
// 将 s 左侧的匹配 f 的字符去掉
func TrimLeftFunc(s string, f func(rune) bool) string
// 将 s 右侧的匹配 f 的字符去掉
func TrimRightFunc(s string, f func(rune) bool) string
```

### 3.2.2. bytes

```go
// 子 slice subslice 在 b 中，返回 true
func Contains(b, subslice []byte) bool
// slice sep 在 s 中出现的次数（无重叠）
func Count(s, sep []byte) int
// 将 []byte 转换为 []rune
func Runes(s []byte) []rune
func NewReader(b []byte) *Reader
type Buffer struct {
    buf      []byte
    off      int   
    lastRead readOp 
}
func NewBuffer(buf []byte) *Buffer
func (b *Buffer) WriteString(s string) (n int, err error)
func NewBufferString(s string) *Buffer
func (b *Buffer) ReadFrom(r io.Reader) (n int64, err error)
func (b *Buffer) WriteTo(w io.Writer) (n int64, err error)
func StringBuffer() (s string) {
    hello := "hello"
    world := "world"
    buf := bytes.NewBuffer([]bytes{""})
    for i := 0; i < 1000; i++ {
        buffer.WriteString(hello)
        buffer.WriteString(",")
        buffer.WriteString(world)
        s = buffer.String()
    }
    return s
}
```

### 3.2.3. strconv--字符串和基本数据类型之间的转换

**字符串转为整形**

```go
func ParseInt(s string, base int, bitSize int) (i int64, err error)
func ParseUint(s string, base int, bitSize int) (n uint64, err error)
func Atoi(s string) (i int, err error)
```

**整形转为字符串**

```go
func FormatUint(i uint64, base int) string //无符号整形转字符串
func FormatInt(i int64, base int) string   //有符号整形字符串
func Aota(i int) string //
```

**字符串转浮点数**

```go
func ParseFloat(s string, bitSize int) (f float64, err error)
func FormatFloat(f float64, fmt byte, prec, bitSize int) string
func AppendFloat(dst []byte, f float64, fmt byte, prec int, bitSize int)

```

### 3.2.4. 正则表达式

https://docs.studygolang.com/pkg/regexp/syntax/

### 3.2.5. unicode



## 3.3. context

 函数

```
WithCancel, WithDeadline, WithTimeout, or WithValue
```

原则：

- 不能在结构体里定义context，应该传递给每个需要他的函数，第一个参数，命名ctx
- 不要传递一个nil的context
- Use context Values only for request-scoped data that transits processes and APIs, not for passing optional parameters to functions.
- The same Context may be passed to functions running in different goroutines; Contexts are safe for simultaneous use by multiple goroutines.

## 3.4. TCP编程

TCP/IP即传输控制协议，是一种面向连接的可靠的基于字节流的传输层通信协议， Go语言利用goroutine实现并发非常方便和高效，所以我们可以建立一次链接就创建一个goroutine去处理。

![socket图解](http://www.topgoer.com/static/6.1/3.png)

流程控制

select

select 语句类似于switch语句，但是select会随机执行可运行的case。如果没有case可运行，他将阻塞直到所有case可运行



## RPC
[Go RPC 开发指南](https://books.studygolang.com/go-rpc-programming-guide/ )

[rpcx](https://rpcx.io/)

特性：

- > 简单易用 

- > 高性能  性能远高于Dubbo、Motan、Thrift等框架，是grpc性能的两倍

- > 交叉平台 支持多平台部署 多语言调用

- > 服务发现 除了直连外还支持Zookeeper、Etcd、Consul、mDNS等注册中心

- > 服务治理 支持Failover、Failfast、Failtry、Backup等失败模式，支持随机、轮询、权重、网络质量，一致性哈希，地理位置等路由算法

benchmark：

> 测试环境[CPU,Memory, Go version, OS]和测试结果[TPS，Latency：mean time， Latency：middle time]



启动server

```go
addr := flag.String("addr", "localhost:8972", "server address")
func Server(){
  // 生成一个server
	s := server.NewServer()
  // 注册
	s.Register(new(Arith), "")
	s.Serve("tcp", *addr)
}
```

**创建server对象**

**注册**

```go
//注册的函数有四个,分为两类: 注册对象中的符合要求的所有方法, 注册某一个方法
// 注册对象中的符合要求的所有方法，调用的时候通过[类型名或指定的name]+方法名 调用 
Register(rcvr interface{}, metadata string) error; // 自动将类型的名字注册
RegisterName(name string, rcvr interface{}, metadata string) error //修改注册名为指定的name
//注册某一个方法,调用的时候通过servicePath + [方法名或指定的name] 调用 
RegisterFunction(servicePath string, fn interface{}, metadata string) error
RegisterFunctionName(servicePath string, name string, fn interface{}, metadata string) error
```

符合要求的方法：

1. method.PkgPath == ""
2. 函数参数为4个<!--其实是三个，第一个必须是context, 最后一个必须是指针-->
3. 函数有一个返回值，必须是error类型

Register方法将类型中符合要求的方法都注册到service.method集合中，并把第二个参数和第三个参数的类型注册到typePools.pools中，如果这个类型没有方法将返回错误。最后将这个类型的名字注册到server.serviceMap中。



启动client

```go
func Client(){
	option := client.DefaultOption
	option.SerializeType = protocol.ProtoBuffer

	d := client.NewPeer2PeerDiscovery("tcp@"+*addr, "")
	xclient := client.NewXClient("Arith", client.Failfast, client.RandomSelect, d, option)
	defer xclient.Close()

	args := &pb.ProtoArgs{A: 10, B: 20}
	reply := &pb.ProtoReply{}
	if err := xclient.Call(context.Background(), "Muls", args, reply); err != nil{
		fmt.Println(err)
	}
	log.Printf("%d * %d = %d", args.A, args.B, reply.C)
}
```

启动httpclient

```go
func HttpClient() {
	args := &pb.ProtoArgs{A: 10, B: 20}
	reply := HttpToTcp(args)
	log.Printf("%d * %d = %d", args.A, args.B, reply.C)
}

func HttpToTcp(args *pb.ProtoArgs) *pb.ProtoReply {
	reply := &pb.ProtoReply{}

	cc := codec.MsgpackCodec{}
	data, _ := cc.Encode(args)
	req, err := http.NewRequest("POST", "http://127.0.0.1:8972/", bytes.NewReader(data))
	if err != nil {
		log.Fatal("failed to create request: ", err)
	}
	h := req.Header
	h.Set(gateway.XMessageID, "10000")
	h.Set(gateway.XMessageType, "0")
	h.Set(gateway.XSerializeType, "3")
	h.Set(gateway.XServicePath, "Arith")
	h.Set(gateway.XServiceMethod, "Mul")

	res, err := http.DefaultClient.Do(req)
	if err != nil {
		log.Fatal("failed to read response: ", err)
	}
	defer res.Body.Close()
	replyData, err := ioutil.ReadAll(res.Body)
	err = cc.Decode(replyData, reply)
	if err != nil {
		log.Fatal("failed to decode reply: ", err)
	}

	return reply
}
```



通过http访问tcp接口

```go
func HttpServer(){
	r := gin.Default()
	r.GET("/mul", CallMul)
	r.Run(":8080")
}

func CallMul(ctx *gin.Context){
	a, err := strconv.Atoi(ctx.Query("a"))
	if err != nil {
		panic(err)
	}
	b, err := strconv.Atoi(ctx.Query("b"))
	if err != nil {
		panic(err)
	}
	args := &pb.ProtoArgs{A: int32(a), B: int32(b)}
	reply := HttpToTcp(args)
	log.Printf("%d * %d = %d", args.A, args.B, reply.C)
	ctx.JSON(http.StatusOK, gin.H{"status": "ok", "reply": reply.C})
}
```



### 设置超时

服务端设置超时

```go
s := server.NewServer(server.WithReadTimeout(10 * time.Second), server.WithWriteTimeout(10*time.Second))
```

client设置超时

```go
// client的Option可以设置超时
type Option struct {
    //ConnectTimeout sets timeout for dialing
    ConnectTimeout time.Duration
    // ReadTimeout sets readdeadline for underlying net.Conns
    ReadTimeout time.Duration
    // WriteTimeout sets writedeadline for underlying net.Conns
    WriteTimeout time.Duration
}
// context.Context

```



### 插件

Metrics

```go
imoprt"github.com/rcrowley/go-metrics"
```

限流

```go
import (
	"net"
	"time"

	"github.com/juju/ratelimit"
)

// RateLimitingPlugin can limit connecting per unit time
type RateLimitingPlugin struct {
	FillInterval time.Duration
	Capacity     int64
	bucket       *ratelimit.Bucket
}

// NewRateLimitingPlugin creates a new RateLimitingPlugin
func NewRateLimitingPlugin(fillInterval time.Duration, capacity int64) *RateLimitingPlugin {
	tb := ratelimit.NewBucket(fillInterval, capacity)

	return &RateLimitingPlugin{
		FillInterval: fillInterval,
		Capacity:     capacity,
		bucket:       tb}
}

// HandleConnAccept can limit connecting rate
func (plugin *RateLimitingPlugin) HandleConnAccept(conn net.Conn) (net.Conn, bool) {
	return conn, plugin.bucket.TakeAvailable(1) > 0
}
```

