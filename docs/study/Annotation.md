
---

# Spring Boot テスト用アノテーションまとめ

## 1. `@SpringBootTest`
### **役割**
Spring Boot アプリケーションを **フル起動** してテストを実行する。

### **特徴**
- アプリケーションコンテキストを丸ごとロード  
- Service / Repository / Component / Config など全部読み込む  
- Web サーバー（Tomcat）も起動可能  
- 最も本番に近い統合テストができる  
- ただし重い

### **使う場面**
- DB・外部サービス・設定を含めた **統合テスト** をしたいとき  
- Mock ではなく実際の Bean を使いたいとき

---

## 2. `@Import(TestAwsConfig.class)`
### **役割**
テスト用の設定クラスを **追加 or 差し替え** する。

### **特徴**
- 本番の設定を使わず、テスト用の Bean を注入できる  
- 設定が変わるとコンテキストキャッシュが効かず、別コンテキストとして起動される  
- 外部サービス（AWS など）のモック化に便利

### **使う場面**
- テスト専用の Config を読み込みたいとき  
- 本番の外部サービス設定を避けたいとき

---

## 3. `@ActiveProfiles("test")`
### **役割**
Spring の **プロファイルを切り替える**。

### **特徴**
- `application-test.yml` が読み込まれる  
- DB や外部サービスの接続先をテスト用に変更できる  
- プロファイルが違うとコンテキストも別扱いになる

### **使う場面**
- テスト用の設定ファイルを使いたいとき  
- 本番とテストで接続先を変えたいとき

---

# 3 つの関係性（まとめ）
| アノテーション | 役割 | テストへの影響 |
|----------------|------|----------------|
| `@SpringBootTest` | アプリ全体を起動 | 最も重いが本番に近い |
| `@Import` | 設定の差し替え | 設定が違うと別コンテキスト |
| `@ActiveProfiles` | プロファイル切替 | 設定ファイルが変わる |

---

# 3 人チームとしてのイメージ
- **@SpringBootTest** → 「アプリを起動する隊長」  
- **@Import** → 「設定を差し替える職人」  
- **@ActiveProfiles** → 「どの環境で動かすか決める司令塔」

この 3 つが揃って **テスト用アプリケーションコンテキスト** を構築する。

---

# @AutoConfigureMockMvc の基本まとめ

## 1. これは何をするアノテーションか
`@AutoConfigureMockMvc` は **MockMvc を自動で使えるように設定してくれるアノテーション**。

MockMvc を使うには本来、

- DispatcherServlet  
- HandlerMapping  
- HandlerAdapter  
- ExceptionResolver  
- MessageConverter  
- Filter  
- Controller の Bean  
- WebMvcConfigurer  

など、WebMVC の内部構造をテスト用に組み立てる必要がある。

これを **全部 Spring Boot が自動で準備してくれる** のがこのアノテーション。

---

## 2. どんなときに使うのか
### ✔ アプリ全体を起動して Controller をテストしたいとき
```java
@SpringBootTest
@AutoConfigureMockMvc
class SampleTest { ... }
```

### ✔ WebMvcTest では足りないとき
`@WebMvcTest` は Controller 層だけを読み込むが、  
Service や Repository も使いたい場合は `@SpringBootTest` + `@AutoConfigureMockMvc` が必要。

---

## 3. 何をしてくれるのか（役割）
- MockMvc の Bean を自動生成  
- WebMVC の内部構造をテスト用に構築  
- Filter や MessageConverter も自動設定  
- Controller を HTTP リクエスト風に叩けるようにする  

つまり、テスト側はこう書くだけで使える。

```java
@Autowired
MockMvc mockMvc;
```

---

## 4. よくある組み合わせ

### ① Controller だけテストしたい  
```java
@WebMvcTest
```
→ MockMvc が自動で使える（@AutoConfigureMockMvc 不要）

### ② アプリ全体を起動して MockMvc を使いたい  
```java
@SpringBootTest
@AutoConfigureMockMvc
```
→ この組み合わせが必要

---

## 5. 注意点
- @SpringBootTest とセットで使うとアプリ全体が起動するため重い  
- WebMvcTest では Service や Repository は読み込まれない  
- MockMvc は “HTTP リクエストを模したテスト” をするためのツール  

---

## 6. まとめ
- **MockMvc を使うための準備を全部やってくれる便利屋**  
- **魔法ではなく、Spring の AutoConfiguration が裏で頑張っている**  
- **Controller のテストを効率化するためのアノテーション**  
- **使い方はシンプルで、理解しておけば現場で困らない**

---

# @WebMvcTest の基本まとめ

## 1. これは何をするアノテーションか
`@WebMvcTest` は **Spring MVC（Controller 層）だけを読み込んでテストするためのアノテーション**。

アプリ全体を起動する `@SpringBootTest` と違い、  
**Controller に関係する部分だけを最小限で起動する** のが特徴。

---

## 2. 読み込まれるもの・読み込まれないもの

### ✔ 読み込まれる（有効になる）
- Controller  
- ControllerAdvice  
- Jackson（JSON 変換）  
- Validation（@Valid）  
- Filter（必要最低限）  
- WebMvcConfigurer  
- ArgumentResolver（PathVariable, RequestParam など）

### ✘ 読み込まれない（自動では使えない）
- Service  
- Repository  
- Component  
- 外部 API クライアント  
- DB 接続  
- Security 設定（必要なら追加設定が必要）

つまり、**Controller 以外は基本的にモック化が必要**。

---

## 3. どんなときに使うのか
### ✔ Controller の動作だけをテストしたいとき
- HTTP リクエストのパラメータ  
- バリデーション  
- JSON の入出力  
- レスポンスコード  
- エラーハンドリング  

こういう “Web 層の挙動” をテストするのに最適。

### ✔ アプリ全体を起動したくないとき
`@SpringBootTest` より **圧倒的に軽い**。

---

## 4. よくある使い方

```java
@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Test
    void testGetCart() throws Exception {
        mockMvc.perform(get("/cart/1"))
               .andExpect(status().isOk());
    }
}
```

### ポイント
- `@MockBean` を使って Service をモック化する  
- MockMvc は自動で使える（@AutoConfigureMockMvc は不要）

---

## 5. @SpringBootTest との違い（比較表）

| 項目 | @WebMvcTest | @SpringBootTest |
|------|-------------|------------------|
| 起動範囲 | Controller だけ | アプリ全体 |
| MockMvc | 自動で使える | @AutoConfigureMockMvc が必要 |
| Service / Repository | 読み込まれない（モック必須） | 実物が使える |
| 起動速度 | 速い | 重い |
| 用途 | Web 層の単体テスト | 統合テスト |

---

## 6. 注意点
- Service や Repository は **必ず @MockBean で差し込む必要がある**  
- Security を使っている場合、追加設定が必要なことがある  
- 外部サービスの呼び出しはモック化しないと動かない  
- Controller のみをテストするためのアノテーションなので、  
  **ビジネスロジックのテストには向かない**

---

## 7. まとめ
- **Controller 層だけを軽量にテストできるアノテーション**  
- **MockMvc が自動で使える**  
- **Service や Repository は読み込まれないためモック化が必要**  
- **Web 層のテストに最適で、起動が速い**

---
