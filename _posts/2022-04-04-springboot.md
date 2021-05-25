---
layout:     post
title:      "spring boot 介绍"
author:     "gdream"
header-img: "img/post-bg-2021.jpg"
catalog: true
tags:
    - else
---

# 配置类
相比于使用配置文件，springboot更倾向于使用配置类的方式

If a bean has more than one constructor, you will need to mark the one you want Spring to use with @Autowired:
```java
@Service
public class MyAccountService implements AccountService {

    private final RiskAssessor riskAssessor;

    private final PrintStream out;

    @Autowired
    public MyAccountService(RiskAssessor riskAssessor) {
        this.riskAssessor = riskAssessor;
        this.out = System.out;
    }

    public MyAccountService(RiskAssessor riskAssessor, PrintStream out) {
        this.riskAssessor = riskAssessor;
        this.out = out;
    }

    // ...

}
```

# JDK8新特性
## stream
> https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#package.description

Example: Here we use widgets, a Collection<Widget>, as a source for a stream, and then perform a filter-map-reduce on the stream to obtain the sum of the weights of the red widgets. 
```java
int sum = widgets.stream()
                .filter(b -> b.getColor() == RED)
                .mapToInt(b -> b.getWeight())
                .sum();
```

`stream`和`Collection`的不同：
- 没有存储。`stream`不是存储元素的数据结构；相反，它通过计算操作的管道从数据结构、数组、生成器函数或 I/O 通道等源传递元素。
- 函数型操作。对`stream`的操作不会修改源数据。
- 懒惰操作。许多`stream`操作，例如过滤、映射或重复删除，可以延迟实现，从而为优化提供机会。例如上面的filter和mapToInt不会立马执行，而是会等到调用sum后才会执行。
- 无界。但`stream`可以是无限大小。短路操作，例如limit(n)or findFirst()可以允许对无限流的计算在有限时间内完成。
- `Consumable`。流的元素在流的生命周期中只被访问一次。

`stream`的获取方法：
- From a Collection via the stream() and parallelStream() methods;
- From an array via Arrays.stream(Object[]);
- From static factory methods on the stream classes, such as Stream.of(Object[]), IntStream.range(int, int) or Stream.iterate(Object, UnaryOperator);
- The lines of a file can be obtained from BufferedReader.lines();
- Streams of file paths can be obtained from methods in Files;
- Streams of random numbers can be obtained from Random.ints();
- Numerous other stream-bearing methods in the JDK, including BitSet.stream(), Pattern.splitAsStream(java.lang.CharSequence), and JarFile.stream().