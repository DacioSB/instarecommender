# Understanding Recommendation Algorithms in `instarecommender`

This document provides a technical explanation of the various recommendation algorithms implemented in this project. These algorithms are used to suggest new users to follow, based on the social graph structure.

## The Example Graph

To illustrate how each algorithm works, let's use a simple, consistent social graph as an example.

- **Users**: Alice, Bob, Charlie, David, Eve, Frank
- **Follows**:
  - `Alice` -> `Bob`
  - `Alice` -> `Charlie`
  - `Bob` -> `David`
  - `Charlie` -> `David`
  - `Charlie` -> `Eve`
  - `Frank` -> `David`

**Goal**: Recommend a new user for `Alice` to follow.
- Alice already follows `Bob` and `Charlie`.
- The potential candidates are `David` and `Eve`, as they are "friends of friends."

---

## 1. Common Neighbors

### Concept
This is one of the simplest and most intuitive methods for recommendation. It suggests users who share the most common connections with the target user. The core idea is that if Alice and David are both followed by many of the same people, they might share similar interests, making David a good recommendation for Alice.

In this project's implementation, we identify candidates by looking at who Alice's friends (`Bob`, `Charlie`) are following. Then, for each candidate, we count how many of the users Alice follows are also following that candidate.

### Mathematical Formula
The score is simply the number of common neighbors between two nodes, `u` and `v`. Let `N(u)` be the set of nodes that user `u` follows.

```
Score(u, v) = |N(u) ∩ N(v)|
```
*Note: The implementation in this project calculates the intersection between the users `Alice` follows (`N(Alice)`) and the users that *follow* the candidate (`Followers(Candidate)`).*

### Example: Recommending for Alice
- **Candidate: `David`**
  - Alice's follows: `{Bob, Charlie}`
  - David's followers: `{Bob, Charlie, Frank}`
  - Common followers: `{Bob, Charlie}`
  - **Score**: 2

- **Candidate: `Eve`**
  - Alice's follows: `{Bob, Charlie}`
  - Eve's followers: `{Charlie}`
  - Common followers: `{Charlie}`
  - **Score**: 1

**Conclusion**: `David` is the better recommendation because he shares more common connections with Alice.

---

## 2. Jaccard Index

### Concept
The Jaccard Index is similar to Common Neighbors but normalizes the score. Instead of just counting common neighbors, it divides the number of common neighbors by the total number of unique neighbors between the two users. This helps to reduce the "rich get richer" problem, where very popular users are always recommended simply because they have many connections.

### Mathematical Formula
The Jaccard Index is the size of the intersection of two sets divided by the size of their union.

```
Jaccard(u, v) = |N(u) ∩ N(v)| / |N(u) ∪ N(v)|
```

### Example: Recommending for Alice
- **Candidate: `David`**
  - Intersection (`Alice`'s follows ∩ `David`'s followers): `{Bob, Charlie}`. Size = 2.
  - Union (`Alice`'s follows ∪ `David`'s followers): `{Bob, Charlie, Frank}`. Size = 3.
  - **Score**: 2 / 3 = 0.67

- **Candidate: `Eve`**
  - Intersection (`Alice`'s follows ∩ `Eve`'s followers): `{Charlie}`. Size = 1.
  - Union (`Alice`'s follows ∪ `Eve`'s followers): `{Bob, Charlie}`. Size = 2.
  - **Score**: 1 / 2 = 0.5

**Conclusion**: `David` still has a higher score and is the better recommendation.

---

## 3. Adamic-Adar Index

### Concept
Adamic-Adar refines the common neighbors approach by giving more weight to common neighbors who are themselves less popular. The intuition is that sharing a niche or less-connected friend is a stronger signal of similarity than sharing a highly popular, well-connected friend. For example, if Alice and David are the only two people who follow a specific artist, that connection is more significant than if they both follow a global celebrity.

It calculates the score by summing the inverse logarithmic degree of each common neighbor.

### Mathematical Formula
The Adamic-Adar index between users `u` and `v` is defined as the sum over their common neighbors `z`, where `D(z)` is the degree (number of connections) of `z`.

```
AA(u, v) = Σ (1 / log(|D(z)|))  for z in N(u) ∩ N(v)
```
*Note: The implementation in the code calculates this score for every path from the user to the candidate, effectively weighting paths through less popular nodes higher.*

### Example: Recommending for Alice
Let's calculate the "following" count for each common neighbor:
- Following count for `Bob`: 1 (`David`)
- Following count for `Charlie`: 2 (`David`, `Eve`)

- **Candidate: `David`**
  - Common Neighbors: `{Bob, Charlie}`
  - Score from `Bob`: `1 / log(1)` -> The code handles `log(1)` as 0, so this path adds 0.
  - Score from `Charlie`: `1 / log(2)` ≈ 1 / 0.693 ≈ 1.44
  - **Total Score**: 1.44

- **Candidate: `Eve`**
  - Common Neighbors: `{Charlie}`
  - Score from `Charlie`: `1 / log(2)` ≈ 1.44
  - **Total Score**: 1.44

**Conclusion**: In this specific simplified case, the scores are equal. In a larger, more complex graph, the scores would diverge more clearly, favoring candidates connected through less popular intermediaries.

---

## 4. PageRank

### Concept
PageRank is fundamentally different from the previous algorithms. It doesn't measure the similarity between two specific users. Instead, it measures the overall *importance* or *influence* of every user in the entire network. A user is considered important if they are followed by other important users.

The recommendation based on PageRank is therefore not personalized in the same way. It suggests users who are globally popular or authoritative within the network, regardless of the target user's specific connections. It's a way of finding "celebrities" or "influencers" in the graph.

### Example: Recommending for Alice
1. The PageRank algorithm is run on the entire graph, assigning a score to every user (`Alice`, `Bob`, `Charlie`, `David`, `Eve`, `Frank`).
2. `David` would likely have the highest PageRank score among the candidates because he is followed by three people (`Bob`, `Charlie`, `Frank`).
3. `Eve` would have a lower score as she is only followed by one person.
4. The final recommendation list for `Alice` would be all users she doesn't currently follow, sorted by their global PageRank score.

**Conclusion**: PageRank recommends globally influential users, which may or may not align with a user's specific, local interests.

---

## Is Adamic-Adar the same as PageRank?

**No, they are fundamentally different.**

| Feature | Adamic-Adar | PageRank |
| :--- | :--- | :--- |
| **Purpose** | Measures **similarity** between two specific nodes. | Measures the global **importance** or **influence** of a single node. |
| **Scope** | **Local**. It only considers the shared neighbors between two nodes. | **Global**. It requires iterating over the entire graph to calculate scores. |
| **Output** | A similarity score (e.g., `Score(Alice, David)`). | An importance score for each node (e.g., `Score(David)`). |
| **Recommendation Type** | **Personalized**. Recommendations are based on the user's unique position in the graph. | **Generalized**. Recommends globally "popular" nodes to everyone. |

In short, Adamic-Adar is for finding "soulmates" based on shared niche connections, while PageRank is for finding "celebrities."

## Implementation Notes

This project provides two sets of implementations for these algorithms:
1.  **In-Memory**: These recommenders (`JaccardRecommender`, `CommonNeighborsRecommender`, etc.) operate on a graph held in the application's memory using the `JGraphT` library. This is suitable for smaller graphs.
2.  **Neo4j**: These recommenders (`Neo4jJaccardRecommender`, etc.) offload the computation to a Neo4j database using the Graph Data Science (GDS) library. This is a much more scalable approach suitable for large-scale production graphs.
