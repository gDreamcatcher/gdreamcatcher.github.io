package main

import (
	"fmt"
	"reflect"
	"time"
	"unsafe"
)

func channelDemo1() {
	ch := make(chan int, 1)
	close(ch)
	// Get from a closed channel, value: 0
	fmt.Println(fmt.Sprintf("Get from a closed channel, value: %d", <-ch))
	// set to a closed channel
	// panic: send on closed channel
	ch <- 1 // wrong
}

func channelDemo2() {
	ch := make(chan int, 1)
	close(ch)
	count := 0
	go func() {
		for {
			select {
			case v, ok := <-ch:
				if !ok {
					ch = nil
				}
				fmt.Println(fmt.Sprintf("Get from a closed channel, value: %d", v))
			}
			count++
			if count > 10 {
				break
			}
		}
	}()
	time.Sleep(10 * time.Second)
}

func channelDemo3() {
	var ch chan int
	// ch <- 1 // fatal error: all goroutines are asleep - deadlock! goroutine 1 [chan send (nil chan)]:
	<-ch // fatal error: all goroutines are asleep - deadlock! goroutine 1 [chan receive (nil chan)]:
}

func channelDemo4() {
	time.NewTicker(1 * time.Second)
	time.NewTimer(1 * time.Second)
}

type Data struct {
	name string
}

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

func main() {
	// channelDemo3()
	// func() {
	// 	fmt.Println("func no name")
	// }()
	// structDemo()
	stringToBytes()
}
