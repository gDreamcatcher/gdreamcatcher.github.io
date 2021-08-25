# go语言基础

## 输入输出

### IO

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



### ioutil库

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



## 文本

### strings

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

### bytes

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

### strconv--字符串和基本数据类型之间的转换

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

### 正则表达式

https://docs.studygolang.com/pkg/regexp/syntax/

### unicode



## context

 函数

```
WithCancel, WithDeadline, WithTimeout, or WithValue
```

原则：

- 不能在结构体里定义context，应该传递给每个需要他的函数，第一个参数，命名ctx
- 不要传递一个nil的context
- Use context Values only for request-scoped data that transits processes and APIs, not for passing optional parameters to functions.
- The same Context may be passed to functions running in different goroutines; Contexts are safe for simultaneous use by multiple goroutines.

## TCP编程

TCP/IP即传输控制协议，是一种面向连接的可靠的基于字节流的传输层通信协议， Go语言利用goroutine实现并发非常方便和高效，所以我们可以建立一次链接就创建一个goroutine去处理。

![socket图解](http://www.topgoer.com/static/6.1/3.png)





流程控制

select

select 语句类似于switch语句，但是select会随机执行可运行的case。如果没有case可运行，他将阻塞直到所有case可运行

