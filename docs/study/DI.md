# DIコンテナとは

Spring Boot の DI コンテナは、アプリ内の Bean をスキャンして依存関係を解析し、必要なオブジェクトを自動で生成・注入します。
これによりクラス間の結合度が下がり、テスト容易性や拡張性が向上します。

また、DI コンテナが Bean のライフサイクルを一元管理することで、AOP を適用するための Proxy を生成できるようになり、トランザクション管理やキャッシュなどの横断的関心事をメソッド呼び出しに挟み込めるようになります。

---

## 概要

DI コンテナは **「Factory + Container + Assembler」** の複合体です。

| 役割 | 名称 | 説明 |
| --- | --- | --- |
| 工場 | Factory | Bean を生成 |
| 倉庫 | Container | Bean を保存 |
| 配線係 | Assembler | 解析と注入 |

---

## DIコンテナの動き

### 1. Bean の候補を集める（スキャン）

- 指定パッケージ配下のクラスを全部走査する
- `@Service` などのスキャン対象の Bean 情報を収集する
- **このタイミングではインスタンスは生成しない**

### 2. Bean の設計図を作る（BeanDefinition）

- スキャンしたクラスは `BeanDefinition` という "設計図" に変換される
  - Bean のクラス
  - スコープ（`singleton` / `prototype`）
  - 依存するコンストラクタの引数 など

### 3. 依存関係を解析して、生成順序を決める

- `BeanDefinition` を元に、Spring は **依存関係のグラフ（DAG）** を作る
- Spring はこの依存関係を解析して、以下を判断する
  - どの Bean を先に作るべきか
  - 循環依存がないか
  - コンストラクタ注入かフィールド注入か

### 4. Bean を生成する（コンストラクタ呼び出し）

- Spring は **リフレクション** を使ってコンストラクタを呼び出す
- 依存される側 → 依存する側 で生成

```
例：Repository → Service → Controller
```

### 5. 依存を注入する（コンストラクタ or フィールド or setter）

Bean を作るとき、必要な依存を DI コンテナが自動で渡す。

```java
public UserService(UserRepository repo)
```

1. `UserRepository` の Bean を先に作る
2. `UserService` のコンストラクタに渡す

### 6. ライフサイクルコールバックを実行する

Bean が生成されたら、Spring は以下の順でライフサイクルを実行する。

**初期化時**

1. `@PostConstruct`
2. `InitializingBean.afterPropertiesSet()`
3. `initMethod`（`@Bean` の `initMethod` 属性）

**破棄時**

1. `@PreDestroy`
2. `DisposableBean.destroy()`
3. `destroyMethod`

### 7. 必要ならプロキシを作る（AOP, @Transactional）

例えば `@Transactional` が付いていると、Spring はそのクラスの **代理オブジェクト（Proxy）** を作る。
つまり、実際に DI コンテナに登録されるのは、実体そのものではない。
この Proxy がトランザクション開始、メソッド呼び出し、コミット or ロールバックを挟み込む。

---

## DIコンテナに任せず、newを使用するケース

1. **DTO / VO / Entity / Record**（ただのデータの箱）
2. **コレクションやユーティリティ的な一時オブジェクト**
3. **設計上「DI コンテナに管理させない」ことが正しいもの**
   - Java 標準ライブラリ（`LocalDateTime`, `Pattern`, `Random` など）
   - 計算用の一時オブジェクト
   - ループ内で使う軽量オブジェクト
4. **外部ライブラリの Builder / Factory が返すオブジェクト**
5. **DI コンテナに依存させたくない「純粋なドメインオブジェクト」**
   - DDD でいう Entity や ValueObject は `new` が自然

---

## DIコンテナに任せるべきケース

1. **依存を持つクラス**（Service, Repository, Component）
2. **外部リソースを扱うクラス**
   - `DataSource`、`HttpClient`、`KafkaProducer`、`RedisClient`、`ThreadPoolTaskExecutor`
   - これらは Spring がライフサイクル管理しないと危険
3. **AOP の対象にしたいクラス**
4. **シングルトンであるべきクラス**

---

## ライフサイクルコールバック

### 要点

- GC がやってくれるのは **ヒープ上のオブジェクトの解放だけ** だが、アプリはメモリ以外のリソースも使用している
- Spring はアプリ側が外部リソースを安全に開放できるようにしている

---

## AOP

本来の業務ロジックとは別に存在する **"横断的な処理"** を、コードに書き散らさず一箇所にまとめて適用する仕組み。

> **横断的関心事とは**
> どのクラスにも必要だけど、ビジネスロジックとは関係ない処理のこと。
> 例：ログ出力、トランザクション管理、認証・認可、キャッシュ、リトライ、メトリクス収集、例外ハンドリングなど

### Spring が AOP を使っている代表例

| アノテーション | 役割 |
| --- | --- |
| `@Transactional` | メソッドの前後にトランザクション開始やコミット or ロールバックを挟み込む |
| `@Async` | メソッド呼び出しを別スレッドに切り替える |
| `@Scheduled` | メソッドを定期実行する |

### finalクラスやprivateメソッドにはAOPが効かない

AOP が効かない理由は **Proxy がオーバーライドできないから**。

- `final` クラス → 継承できない
- `final` メソッド → オーバーライドできない
- `private` メソッド → Proxy から呼び出せない（外から見えない）

Spring AOP は 2 種類の Proxy を使う。

#### JDK Dynamic Proxy（インターフェースベース）

- インターフェースを実装した Proxy を作る
- メソッド呼び出しは Proxy が受け取る
- 本物の実装クラスに委譲する

> この場合、クラスの継承は関係ないけど、`private` メソッドは Proxy の外側なので横取りできない。

#### CGLIB（クラス継承ベース）

- 対象クラスを **継承したサブクラス** を生成
- メソッドをオーバーライドして横取りする
- `final` クラス・`final` メソッド・`private` メソッドはオーバーライドできないため効かない

### 自己呼び出し問題（self-invocation）

メソッド内で自己別メソッドを呼び出すと `this.inner()` になり、Proxy を経由しない。
だから AOP の横取りが発動しない。

### AOPのタイミング

| アノテーション | タイミング |
| --- | --- |
| `@Before` | メソッド実行前 |
| `@After` | メソッド実行後（成否問わず） |
| `@AfterReturning` | 正常終了時 |
| `@AfterThrowing` | 例外時 |
| `@Around` | 前後両方（最強・最自由） |

> `@Transactional` は内部的に `@Around` 相当の動きをしている（前で開始、後で commit、例外で rollback）。

---

## Bean

Spring が管理対象として認識し、ライフサイクル（生成・初期化・破棄）を制御するオブジェクト。
通常 `@Component` や `@Service` などのアノテーション、または `@Configuration` 内の `@Bean` メソッドで定義する。

---

## Q&A

### Q. コンストラクタインジェクションとフィールドインジェクション、どちらを使うべきですか？

**A. コンストラクタインジェクション一択です。**

1. `final` にできるのでイミュータブル
2. 依存が必須であることが型レベルで保証される
3. テストでモックを渡すときに Spring コンテナなしでインスタンス化できる

> `@Autowired` をフィールドに付ける書き方は今は非推奨です。

### Q. 同じインターフェースのBeanが2つあったらどうなりますか？

**A. `NoUniqueBeanDefinitionException` が起きます。**

- 解決策
  - `@Primary` でデフォルトを指定する
  - `@Qualifier("beanName")` で明示的に指定する

### Q. Bean のスコープって何がありますか？

**A.** デフォルトは `singleton` で、アプリ全体で 1 インスタンス。
他に以下がある。

- `prototype`（毎回新規）
- `request`（Web アプリ）
- `session`（Web アプリ）

---

## 要点（今後の学習トピック）

- 解決策は `@Primary` でデフォルトを指定するか、`@Qualifier("beanName")` で明示的に指定する
- `finalize()` がなぜ危険なのか
- Spring のシャットダウンフックの仕組み
- HikariCP や ExecutorService の終了処理の内部
- `@PreDestroy` が呼ばれないケース（実はある）
- Web アプリの「優雅なシャットダウン」の仕組み
