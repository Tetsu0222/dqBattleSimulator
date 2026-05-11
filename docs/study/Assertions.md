
---

# JUnit 5 アサーション完全ガイド

JUnit 5（JUnit Jupiter）は、テストの可読性と表現力を高めるために、豊富なアサーション API を提供している。  
このガイドでは、主要なアサーションの使い方を体系的にまとめる。

---

## 1. 基本アサーション

### `assertEquals`
```java
assertEquals(expected, actual);
assertEquals(expected, actual, "メッセージ");
```

### `assertNotEquals`
```java
assertNotEquals(unexpected, actual);
```

### `assertTrue` / `assertFalse`
```java
assertTrue(value > 0);
assertFalse(list.isEmpty());
```

### `assertNull` / `assertNotNull`
```java
assertNull(result);
assertNotNull(obj);
```

### `assertSame` / `assertNotSame`
```java
assertSame(expectedRef, actualRef);
assertNotSame(obj1, obj2);
```

---

## 2. 例外アサーション

### `assertThrows`
指定した例外が発生することを検証する。

```java
Exception e = assertThrows(IllegalArgumentException.class, () -> {
    someMethod(null);
});
assertEquals("invalid", e.getMessage());
```

### `assertDoesNotThrow`
例外が発生しないことを検証。

```java
assertDoesNotThrow(() -> someMethod("ok"));
```

---

## 3. グループ化アサーション（`assertAll`）

複数のアサーションをまとめて実行し、すべての失敗を報告する。

```java
assertAll(
    () -> assertEquals("Taro", user.getName()),
    () -> assertTrue(user.getAge() > 18),
    () -> assertNotNull(user.getEmail())
);
```

---

## 4. 遅延メッセージ（ラムダ式）

JUnit 5 では、アサーションメッセージをラムダで遅延評価できる。

```java
assertTrue(value > 0, () -> "value should be positive but was " + value);
```

---

## 5. 配列・Iterable のアサーション

### `assertArrayEquals`
```java
assertArrayEquals(new int[]{1, 2, 3}, actualArray);
```

### `assertIterableEquals`
```java
assertIterableEquals(List.of("a", "b"), actualList);
```

---

## 6. タイムアウトアサーション

### `assertTimeout`
処理が指定時間内に完了することを検証。

```java
assertTimeout(Duration.ofMillis(500), () -> {
    Thread.sleep(100);
});
```

### `assertTimeoutPreemptively`
指定時間を超えたら強制終了（注意：スレッド安全性に注意）

```java
assertTimeoutPreemptively(Duration.ofMillis(500), () -> {
    Thread.sleep(100);
});
```

---

## 7. カスタムメッセージのベストプラクティス

- 失敗時に原因が明確になるメッセージを書く  
- 計算コストの高いメッセージはラムダで遅延評価  
- 期待値と実際値を明確に示す

例：

```java
assertEquals(expected, actual, () -> "Expected: " + expected + ", but was: " + actual);
```

---

## 8. よくあるパターン集

### オブジェクトのフィールドをまとめて検証
```java
assertAll("User properties",
    () -> assertEquals("Taro", user.getName()),
    () -> assertEquals(20, user.getAge()),
    () -> assertTrue(user.isActive())
);
```

### 例外のメッセージまで検証
```java
IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.run());
assertEquals("Invalid state", ex.getMessage());
```

### コレクションの中身を検証
```java
assertIterableEquals(List.of(1, 2, 3), actualList);
```

---

## 9. JUnit 4 からの移行ポイント

| JUnit 4 | JUnit 5 |
|--------|---------|
| `org.junit.Assert` | `org.junit.jupiter.api.Assertions` |
| `@Test(expected=...)` | `assertThrows` |
| `@Test(timeout=...)` | `assertTimeout` |
| メッセージは先頭 | メッセージは末尾（ラムダ可） |

---

## 10. まとめ

- JUnit 5 のアサーションは **表現力が高く、柔軟で、読みやすい**  
- `assertAll` や `assertThrows` など、JUnit 4 にはない強力な機能が多数  
- 遅延メッセージによりパフォーマンスも向上  
- テストの可読性と保守性が大幅にアップする

---
