# yellow

yellow is a dynamically typed toy language.

#### Dynamic Variable bindings
```
import "std:system";
var a = 10;
var b = 20;
System.print(a + b);          // 30

var a = "Hello";
var b = "World";
System.print( a + ", " + b);   // Hello, World
```

#### Lexical Scoping
```
import "std:system";
var a = "a - outer";
var b = "b - outer";

{
  System.print(b);                          // b-outer
  var b = "b - inner";
  System.print(b);                          // b-inner
  {
    System.print(a);                        // a-outer
    var a = "a - inner inner";
    System.print(a);                        // a-inner inner
  }
}
System.print(a);                            // a-outer
System.print(b);                            // b-outer
```

#### Recursion
```
import "std:system";
// Fibonacci

fun fibonacci(n) {
    if (n <= 1) return n;

    return fibonacci(n - 1) + fibonacci(n - 2);
}

for(var i = 1; i < 20; i = i + 1) {
    System.print(fibonacci(i));
}
```

#### Closures
```
import "std:system";
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

System.print(counter());    // 1
System.print(counter());    // 2
System.print(counter());    // 3
System.print(counter());    // 4

```
#### Classes
```
import "std:system";
class Animal {
  init(mammal) {
    this.mammal = mammal;
  }

  info() {
    System.print(this.mamma)l;
  }
}


class Cat < Animal {
  init(name) {
    super.init(true);
    this.name = name;
  }

  info() {
    System.print("name: " + this.name);
    super.info();
  }
}


var d = Cat("Pepper");
System.print(d);                  // <instance <class Cat>>
d.info();                 // name: Pepper
                          // true

System.print(d.name);             // Pepper
System.print(d.mammal);           // true
```

#### Standard Library
```
import "std:system";
import "std:list";

// Show the string prompt and capture the entered value
var num = System.input("Enter a number: ");

System.print(num) 

var l = List();

l.append(10);
l.append("Hello");
l.append(num);

l.set(1, "Another Hello");

l.print();      

// list iteration
for(var i = 0; i < l.len(); i = i + 1) {
    System.println(l.get(i));
}

// clear the list
while(l.len() > 0) {
    l.delete(0);
}
```

