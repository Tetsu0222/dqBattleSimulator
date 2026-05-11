# static_importとは？

## 結論
**JUnit 5（Jupiter）のアサーションメソッドを、クラス名なしで使えるようにするための static import 宣言。**

---

## 詳細説明

JUnit 5 のアサーション（`assertEquals` や `assertTrue` など）は  
`org.junit.jupiter.api.Assertions` クラスにまとめられている。

通常の import だと、クラス名を毎回書く必要がある：

```java
import org.junit.jupiter.api.Assertions;

Assertions.assertEquals(3, actual);
```

しかし static import を使うと、クラス名を省略できる：

```java
import static org.junit.jupiter.api.Assertions.*;

assertEquals(3, actual);
```