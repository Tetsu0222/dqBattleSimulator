
---

# Java の `record` とは

**不変（イミュータブル）なデータを持つクラスを簡潔に定義するための構文。**  
Java 16 で正式導入された機能で、DTO や戻り値の入れ物として最適。

---

## 🎯 record の目的

- **値を保持するだけのクラス（データキャリア）を簡潔に書く**
- **イミュータブル**なオブジェクトを簡単に作れる
- ボイラープレート（getter, equals, hashCode, toString）を自動生成

---

## 🛠 自動生成されるもの

| 自動生成されるもの | 内容 |
|------------------|------|
| **コンストラクタ** | `new PartyBuildResult(set, list)` |
| **アクセサメソッド** | `partySet()` / `nameList()`（`get` は付かない） |
| **equals()** | フィールドの値で比較 |
| **hashCode()** | フィールドベース |
| **toString()** | `PartyBuildResult[partySet=..., nameList=...]` |

---

## 📌 サンプルコード

### record 版（1 行で完結）

```java
public record PartyBuildResult(Set<AllyData> partySet, List<String> nameList) {}
```

### 従来のクラスで書くと…

```java
public final class PartyBuildResult {
    private final Set<AllyData> partySet;
    private final List<String> nameList;

    public PartyBuildResult(Set<AllyData> partySet, List<String> nameList) {
        this.partySet = partySet;
        this.nameList = nameList;
    }

    public Set<AllyData> getPartySet() { return partySet; }
    public List<String> getNameList() { return nameList; }

    // equals, hashCode, toString も全部書く必要がある
}
```

---

## 🔍 record の特徴と注意点

- **アクセサ名は `getXxx()` ではなく `xxx()`**
  - 呼び出し例：`result.partySet()`
- **フィールドはすべて `final`（再代入不可）**
- **継承不可（暗黙的に `final`）**
- **Lombok の `@Value` に近い感覚**
- **DTO・レスポンス・戻り値の入れ物に最適**

---

## 🧭 まとめ

`record` は「値を保持するだけのクラス」を**最小限のコードで安全に**定義できる仕組み。  
Java でイミュータブルなデータ構造を扱うなら、まず record を選ぶのが自然。

---
