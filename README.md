# yellow

yellow is a dynamically typed toy language.

#### Dynamic Variable bindings
```
import "std:system";
var a = 10;
var b = 20;
System.println(a + b);          // 30

var a = "Hello";
var b = "World";
System.println( a + ", " + b);   // Hello, World
```

#### Lexical Scoping
```
import "std:system";
var a = "a - outer";
var b = "b - outer";

{
  System.println(b);                          // b-outer
  var b = "b - inner";
  System.println(b);                          // b-inner
  {
    System.println(a);                        // a-outer
    var a = "a - inner inner";
    System.println(a);                        // a-inner inner
  }
}
System.println(a);                            // a-outer
System.println(b);                            // b-outer
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
    System.println(fibonacci(i));
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

System.println(counter());    // 1
System.println(counter());    // 2
System.println(counter());    // 3
System.println(counter());    // 4

```
#### Classes
```
import "std:system";
class Animal {
  init(mammal) {
    this.mammal = mammal;
  }

  info() {
    System.println(this.mamma)l;
  }
}


class Cat < Animal {
  init(name) {
    super.init(true);
    this.name = name;
  }

  info() {
    System.println("name: " + this.name);
    super.info();
  }
}


var d = Cat("Pepper");
System.println(d);                  // <instance <class Cat>>
d.info();                 // name: Pepper
                          // true

System.println(d.name);             // Pepper
System.println(d.mammal);           // true
```

#### Standard Library
```
import "std:system";
import "std:list";

// Show the string prompt and capture the entered value
var num = System.input("Enter a number: ");

System.println(num);

var l = List();

l.append(10);
l.append("Hello");
l.append(num);

l.set(1, "Another Hello");

l.print();      

// list iteration
for(var i = 0; i < l.len(); i = i + 1) {
    System.printlnln(l.get(i));  
}

// clear the list
while(l.len() > 0) {
    l.delete(0);
}

import "std:map";

// HashMap - Only strings as keys :(

var m = Map();
m.set("int", 10);
System.println(m.get("int"));   // 10
System.println(m.get("key"));   // nil

m.set("list", List());
var l = m.get("list");    
l.append("Hello");    
l.append(20);
System.println(m.get("list").get(1));     // 20
System.println(m.get("list").get(0));     // Hello

```

