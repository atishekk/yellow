# yellow

yellow is a dynamically typed toy language.

#### Dynamic Variable bindings
```
var a = 10;
var b = 20;
print a + b;          // 30

var a = "Hello";
var b = "World";
print a + ", " + b;   // Hello, World
```

#### Lexical Scoping
```
var a = "a - outer";
var b = "b - outer";

{
  print b;                          // b-outer
  var b = "b - inner";
  print b;                          // b-inner
  {
    print a;                        // a-outer
    var a = "a - inner inner";
    print a;                        // a-inner inner
  }
}
print a;                            // a-outer
print b;                            // b-outer
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

print counter();    // 1
print counter();    // 2
print counter();    // 3
print counter();    // 4

```
#### Classes
```
class Animal {
  init(mammal) {
    this.mammal = mammal;
  }

  info() {
    print this.mammal;
  }
}


class Cat < Animal {
  init(name) {
    super.init(true);
    this.name = name;
  }

  info() {
    print "name: " + this.name;
    super.info();
  }
}


var d = Cat("Pepper");
print d;                  // <instance <class Cat>>
d.info();                 // name: Pepper
                          // true

print d.name;             // Pepper
print d.mammal;           // true
```

#### Standard Library
```
  TODO()
```

