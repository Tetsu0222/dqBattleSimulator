
---

# Mockito 基礎まとめ

## 📌 Mockitoとは
**Java の単体テストで「モック（偽物のオブジェクト）」を作るためのライブラリ。**  
依存オブジェクトを置き換えて、テスト対象のロジックだけを純粋に検証できる。

---

## 📦 1. 依存オブジェクトをモック化する

```java
Party party = mock(Party.class);
```

- `new Party()` の代わりに「偽物」を作る
- コンストラクタを呼ばない
- 状態を持たない（必要な振る舞いだけ設定する）

---

## 🔧 2. メソッドの戻り値を指定する

```java
when(party.getSurvival()).thenReturn(1);
```

- `getSurvival()` を呼ぶと **必ず 1 を返す**
- 実装がどうであれ、テスト用の振る舞いを強制できる

---

## 🎭 3. 複数の戻り値を順番に返す

```java
when(party.getHp()).thenReturn(10, 5, 0);
```

呼び出し順に  
1回目 → 10  
2回目 → 5  
3回目 → 0  

---

## 🧪 4. 例外を投げさせる

```java
when(service.run()).thenThrow(new RuntimeException("error"));
```

例外処理のテストに使う。

---

## 🔍 5. メソッドが呼ばれたか検証する（verify）

```java
verify(party).getSurvival();
```

- `getSurvival()` が **1回** 呼ばれたか確認

回数を指定する場合：

```java
verify(party, times(2)).attack();
verify(party, never()).heal();
```

---

## 🧰 6. 引数を柔軟に扱う（Matchers）

```java
when(repo.findById(anyInt())).thenReturn(data);
```

よく使うマッチャ：

| Matcher | 意味 |
|--------|------|
| `anyInt()` | どんな int でもOK |
| `anyString()` | どんな String でもOK |
| `any()` | どんな型でもOK |
| `eq(x)` | x と一致 |

---

## 🧩 7. @Mock と @InjectMocks（JUnit5）

```java
@ExtendWith(MockitoExtension.class)
class SampleTest {

    @Mock
    Party party;

    @InjectMocks
    BattleProgressService service;

}
```

- `@Mock` → モックを自動生成
- `@InjectMocks` → モックを注入してテスト対象を作る

---

## 🧵 8. doReturn / doThrow の使いどころ

`when(...).thenReturn(...)` が使えないケース（finalメソッドなど）で使う。

```java
doReturn(10).when(party).getHp();
```

例外版：

```java
doThrow(new RuntimeException()).when(service).run();
```

---

## 🧪 9. 実際のテスト例（あなたのコードに近い形）

```java
@Test
void testDeadPartyAndNextAlive() {
    Battle battle = mock(Battle.class);

    Party dead = mock(Party.class);
    Party alive = mock(Party.class);

    when(dead.getSurvival()).thenReturn(0);
    when(alive.getSurvival()).thenReturn(1);

    when(battle.getPartyMap()).thenReturn(Map.of(
            1, dead,
            2, alive
    ));

    Queue<Integer> turnqueue = new LinkedList<>();
    turnqueue.add(2);

    BattleProgressService service = new BattleProgressService();

    boolean result = service.turnAction(battle, 1, turnqueue);

    assertTrue(result);
}
```

---

## 🎯 まとめ

- Mockitoは「依存を偽物にしてテストを簡単にする」ためのライブラリ
- `mock()` で偽物を作る
- `when(...).thenReturn(...)` で振る舞いを定義
- `verify()` で呼び出しを検証
- `@Mock` / `@InjectMocks` でテストをスッキリ書ける

---
