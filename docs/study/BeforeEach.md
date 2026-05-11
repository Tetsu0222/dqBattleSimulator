
---

# JUnit の `@BeforeEach` を徹底解説

## 🎯 結論（まず押さえるべきポイント）
- **@BeforeEach は各テストメソッドの実行前に必ず呼ばれる初期化処理**  
- テスト間の状態を独立させるために使う  
- 典型的には **オブジェクト生成・共通セットアップ・モック準備** などを行う  
- JUnit5（Jupiter）でのアノテーション名は `@BeforeEach`  
  - JUnit4 の `@Before` とほぼ同じ役割

---

## 📌 1. `@BeforeEach` とは何か
`@BeforeEach` は **JUnit5** におけるライフサイクルアノテーションの一つで、  
**各テストメソッドの実行前に毎回実行されるメソッド** に付ける。

```java
@BeforeEach
void setUp() {
    // テストごとに実行される初期化処理
}
```

### 役割
- テストケース間の **独立性** を保つ  
- テストの **可読性向上**（重複コードを排除）  
- テストの **信頼性向上**（状態が汚染されない）

---

## 📌 2. 実行タイミング
テストクラス内の **各テストメソッドの直前** に呼ばれる。

```
@BeforeEach → @Test1 → @BeforeEach → @Test2 → @BeforeEach → @Test3 …
```

つまり、テストが 10 個あれば `@BeforeEach` も 10 回呼ばれる。

---

## 📌 3. 具体例（最もよくある使い方）

### 例：テスト対象クラスのインスタンス生成
```java
class CalculatorTest {

    Calculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Calculator();
    }

    @Test
    void testAdd() {
        assertEquals(5, calculator.add(2, 3));
    }

    @Test
    void testSubtract() {
        assertEquals(1, calculator.subtract(3, 2));
    }
}
```

### ポイント
- `calculator` は毎回新しく生成される  
- テスト間で状態が混ざらない  
- テストコードがスッキリする

---

## 📌 4. `@BeforeEach` と `@BeforeAll` の違い

| アノテーション | 実行タイミング | static 必須 | 主な用途 |
|----------------|----------------|-------------|----------|
| **@BeforeEach** | 各テスト前 | 不要 | 毎回初期化が必要なもの |
| **@BeforeAll** | テストクラス開始時に一度だけ | 必須 | 重い初期化（DB接続など） |

---

## 📌 5. モック（Mockito）と組み合わせる例

```java
class UserServiceTest {

    @Mock
    UserRepository repository;

    @InjectMocks
    UserService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindUser() {
        when(repository.findById(1)).thenReturn(new User(1, "Tetsuya"));
        User user = service.findUser(1);
        assertEquals("Tetsuya", user.getName());
    }
}
```

---

## 📌 6. よくある質問（FAQ）

### Q. `@BeforeEach` の中で例外が出たら？
→ そのテストは **失敗扱い** になり、テストメソッドは実行されない。

### Q. private メソッドにしてもいい？
→ **OK**。JUnit はリフレクションで呼び出すため問題なし。

### Q. 複数の `@BeforeEach` を書いたら？
→ **すべて実行される**。  
実行順は **メソッドの宣言順**（クラス内の記述順）。

---

## 📌 7. まとめ（重要ポイントだけ再掲）
- **各テストの前に毎回実行される初期化処理**  
- テストの独立性を保つために必須  
- オブジェクト生成・モック初期化・共通セットアップに最適  
- JUnit4 の `@Before` とほぼ同じ役割  

---
