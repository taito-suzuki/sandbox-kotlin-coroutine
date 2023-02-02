# Coroutine勉強用

## プログラム実行方法

```shell
./gradlew run -Pmain=com.example.app.Main001Kt
```

## [Coroutine](https://en.wikipedia.org/wiki/Coroutine)

Coroutineという言葉は、文脈によってさまざまな意味を持つので注意。

### 元祖Coroutine

Coroutineは、元々は、[協調マルチタスキング](https://en.wikipedia.org/wiki/Cooperative_multitasking)を実現するために考えられた、概念上の関数だった（[参考](https://en.wikipedia.org/wiki/Coroutine)）。
マルチタスキングをいい感じにやるためにはどうしたらいいんだろう？の答えの一つが、上記のようなCoroutineを使った協調マルチタスキングだった。

### 各種プログラミング言語におけるCoroutine

上記の「概念」は、さまざまなプログラミング言語で実装された。

Coroutineとは、単に、中断（suspend）と再開（resume）ができる関数のこと。それ以上でも以下でもない。

### KotlinのCoroutine

Coroutineとは、Kotlinで実装されているCoroutineを指す。
もしくは、Coroutineライブラリを使って書かれている平行処理を、なんとなく指していたり。
わりとふんわりとしている印象。



[ソースコード](https://github.com/Kotlin/kotlinx.coroutines)。