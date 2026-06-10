# Skill: Java Naming Conventions

Always use **full, descriptive variable names** — never single-letter or abbreviated names. Prefer longer names over ambiguous short ones, even in loops or lambdas.

## Rule

Be consistent: if a type is `RoadUser`, the local variable must be `roadUser`. The pattern is the **camelCase form of the type name**.

## Examples

| Type | ✅ Variable name | ❌ Avoid |
|------|-----------------|---------|
| `RoadUser` | `roadUser` | `ru`, `user`, `r` |
| `ManoeuvreSession` | `manoeuvreSession` | `ms`, `session`, `s` |
| `JsonParser` | `parser` (acceptable: well-known type) | `jp`, `p` |
| `List<WayPoint>` | `wayPoints` | `wps`, `list`, `l` |
| `String` (in loop) | `fieldName` | `f`, `fn` |
| `int` (index) | `i` is acceptable in classic `for` loops; prefer `index` elsewhere | `idx`, `x` |
| `McmType` (enum loop) | `mcmType` | `type`, `t`, `mt` |

## Lambdas

```java
// ✅ Good
roadUsers.forEach(roadUser -> process(roadUser));

// ❌ Bad
roadUsers.forEach(r -> process(r));
```

