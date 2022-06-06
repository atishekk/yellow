# yellow

yellow is a dynamically typed toy language.

#### Dynamic Variable bindings
```
var a = 10;
var b = 20;
print a + b;

var a = "Hello";
var b = "World";
print a + ", " + b;
```

#### Lexical Scoping
```
var a = "a - outer";
var b = "b - outer";

{
  print b;
  var b = "b - inner";
  print b;
  {
    print a;
    var a = "a - inner inner";
    print a;
  }
}
print a;
print b;
```

#### Recursion
```
// Fibonacci

fun fibonacci(n) {
    if (n <= 1) return n;

    return fibonacci(n - 1) + fibonacci(n - 2);
}

for(var i = 1; i < 20; i = i + 1) {
    print fibonacci(i);
}
```

#### Closures
```
// Counter

fun makeCounter() {
    var count = 0;

    fun counter() {
        count = count + 1;
        return count;
    }

    return counter;
}

var counter = makeCounter();

print counter();
print counter();
print counter();
print counter();

```
#### Classes
```
  TODO()
```

#### Standard Library
```
  TODO()
```

