---
title: "Java 21's pattern matching could actually convince me to touch Java again"
date: 2023-09-14T21:00:00+05:30
categories:
- JVM
- pattern matching
- functional programming
- Java 21
- Algebraic types
keywords:
- Java updates
- Java 21
- Java records
- Java sealed types
- Java Switch pattern matching
- sealed types
- pattern matching for switch
- record patterns
- sealed classes
- switch pattern matching
description: Algebraic data types in Java.
slug: java-pattern-matching
---

{{< note-alert >}}
Whatever notation is used in this article does not represent how the math is usually presented. If you study this further or have already studied this subject, there may be mistakes, or terms I've used incorrectly. Please point out such mistakes in the comments, and I will update the article ASAP. Also, note that I'm borrowing the type theory notation from Wikipedia.
{{< /note-alert >}}

Java 21 will be released on September 19, 2023, supporting record patterns in switch blocks and expressions. Such syntax is monumental (At least, in Java land). It marks the point where Java could be considered to properly support functional programming patterns in ways similar to Kotlin, Rust, or C#. And it marks the first point where I can say, as a Kotlin developer, that I feel jealous.

## A brief history of recent Java versions.

Java has evolved rapidly in the past 10 years (As of 2023). Java 9 was the last "slow" release, as all subsequent releases happened 6 months apart. Below is a table showing Java updates over the past decade and the major syntactic changes/additions made in each version (Most changes are omitted to stay on topic). <br/>

| Java Version | Release Date | Major Syntax Related Features                                                                                                                                                                                                                                                                                                        |
| :----------- | :----------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Java 8       | Mar 18, 2014 | <ul><li>[Lambda expressions](https://openjdk.org/jeps/126)</li></ul>                                                                                                                                                                                                                                                                 |
| Java 9       | Sep 21, 2017 | <ul><li>[Modules(Project Jigsaw)](https://openjdk.org/projects/jigsaw/spec/)</li></ul>                                                                                                                                                                                                                                               |
| Java 10      | Mar 20, 2018 | <ul><li>[`var` for local variables](https://openjdk.java.net/jeps/286)</li></ul>                                                                                                                                                                                                                                                     |
| Java 11      | Sep 25, 2018 | No syntactic changes                                                                                                                                                                                                                                                                                                                 |
| Java 12      | Mar 19, 2019 | <ul><li>[Switch expressions (Preveiw 1)](https://openjdk.java.net/jeps/325)</li></ul>                                                                                                                                                                                                                                                |
| Java 13      | Sep 17, 2019 | <ul><li>[Switch expressions (Preview 2)](https://openjdk.java.net/jeps/354)</li> <li>[Text blocks (Preview 1)](https://openjdk.java.net/jeps/355)</li></ul>                                                                                                                                                                          |
| Java 14      | Mar 17, 2020 | <ul><li>[Switch expressions (Stable release)](https://openjdk.java.net/jeps/361)</li> <li>[`instanceof` pattern matching with `if` (Preview 1)](https://openjdk.java.net/jeps/305)</li> <li>[Records (Preview 1)](https://openjdk.java.net/jeps/359)</li> <li>[Text blocks (Preview 2)](https://openjdk.java.net/jeps/368)</li></ul> |
| Java 15      | Sep 15, 2020 | <ul><li>[Sealed classes (Preview 1)](https://openjdk.java.net/jeps/360)</li> <li>[`instanceof` pattern matching with `if` (Preview 2)](https://openjdk.java.net/jeps/375)</li> <li>[Text blocks (Stable release)](https://openjdk.java.net/jeps/378)</li> <li>[Records (Preview 2)](https://openjdk.java.net/jeps/384)</li></ul>     |
| Java 16      | Mar 16, 2021 | <ul><li>[`instanceof` pattern matching with `if` (Stable release)](https://openjdk.java.net/jeps/394)</li> <li>[Records (Stable release)](https://openjdk.java.net/jeps/395)</li> <li>[Sealed classes (Preview 2)](https://openjdk.java.net/jeps/397)</li></ul>                                                                      |
| Java 17      | Sep 14, 2021 | <ul><li>[Switch pattern matching (Preview 1)](https://openjdk.java.net/jeps/406)</li> <li>[Sealed classes (Stable release)](https://openjdk.java.net/jeps/409)</li></ul>                                                                                                                                                             |
| Java 18      | Mar 22, 2022 | <ul><li>[Switch pattern matching (Preview 2)](https://openjdk.java.net/jeps/420)</li></ul>                                                                                                                                                                                                                                           |
| Java 19      | Sep 20, 2022 | <ul><li>[Record patterns (Preview 1)](https://openjdk.java.net/jeps/405)</li> <li>[Switch pattern matching (Preview 3)](https://openjdk.java.net/jeps/427)</li></ul>                                                                                                                                                                 |
| Java 20      | Mar 21, 2023 | <ul><li>[Record patterns (Preview 2)](https://openjdk.java.net/jeps/432)</li> <li>[Switch pattern matching (Preview 4)](https://openjdk.java.net/jeps/433)</li></ul>                                                                                                                                                                 |
| Java 21      | Sep 19, 2023 | <ul><li>[Record patterns (Stable release)](https://openjdk.java.net/jeps/440)</li> <li>[Switch pattern matching (Stable release)](https://openjdk.java.net/jeps/441)</li></ul>                                                                                                                                                       |

There are a few notable releases here. 

Java 14 stabilised switch expressions, 16 records and `instanceof` pattern matching, 17 sealed classes, and now, 21 will stabilise record patterns and switch pattern matching.

This set of changes allows Java to express one of the foundations of functional programming that the language never could before - Algebraic data types, along with idiomatic ways of using them. 

Algebraic data types are a concept born from *Type theory*, which is a branch of Set theory that focuses specifically on questions like "Is an *Apple* a *Fruit*?" and other such whimsical conundrums math teachers like to pose to hapless students the world over.

## A *very* minimal introduction to some terms from type theory

Type theory has quite a lot of meat to it, and most of it isn't relevant to this article.

So, instead of explaining type theory, I will talk about a few specific kinds of types that it would be useful to know about.

### The Bottom, or Empty Type (`⊥`)

This type describes the set of all values which *can't be computed*[^turing]. This set is usually empty for any normal programming language (Ø).

[^turing]: When I say can't be computed, I mean it in the [Turing-complete](https://en.wikipedia.org/wiki/Turing_completeness) sense.

No object can be cast to bottom since it is an empty set.

An example of such a type is Kotlin's `Nothing`. It is considered an error for an instance of `Nothing` to exist. The way Kotlin prevents `Nothing` from being instantiated is [by setting its constructor to be private](https://github.com/JetBrains/kotlin/blob/7a7d392b3470b38d42f80c896b7270678d0f95c3/core/builtins/native/kotlin/Nothing.kt#L23).

The Java version of this type is [`Void`](https://docs.oracle.com/javase/8/docs/api/java/lang/Void.html), the wrapper class for the `void` primitive type. It is, again, impossible to construct a `Void` instance because its constructor is private. However, `Void` cannot be considered a true bottom type because a `Void` variable can still hold `null`, which means it is technically a unit type[^thanks] (more on that below). In that sense, the primitive `void` does better. There's absolutely no way to use it as a variable type, so you can't even have an empty `void` variable.

[^thanks]: Thanks to [`u/iconoklast`](https://old.reddit.com/r/programming/comments/16jkxfa/java_21_makes_me_actually_like_java_again/k0u8znc/) for pointing this out.
### The Top Type (`⊤`)

This type represents every value of every type - the universal set of values, `U`. In Kotlin, this type is named `Any`. And it may be tempting to think `Object` (the Java equivalent of `Any`) is a top type, except for the fact that primitives exist. Primitives are wholly separate from Java's object model, and interact with objects in rather non-standard ways. Because of this, Java technically does not have a top type the way other languages do. 

Meanwhile, `C` does as it do and overloads `void` by using `void *` to represent a pointer that can refer to a value of any type instead. How droll!

Every object can be cast to top since `U` contains every value that exists.

There isn't much to say about top other than that a variable of this type can hold anything. Including a value of the bottom type. Good luck finding that value, though.

### The Unit Type (`()`)

This type has only one value. There is only one instance of that one value, and it is impossible to create more of it.

Java's `void` primitive technically works like this. When a method returns `void`, you can treat it as if it implicitly returns the sole instance of the `void` type under the hood (This is *not* how the JVM handles `void`). Java deviates from the theoretical norm in that `void` can never be passed into a method as a parameter.

{{< note-alert >}}
The truth is, Java just mashes the bottom and the unit types together to give us `void`.
{{< /note-alert >}}

You can technically simulate the unit type in Java by declaring a new class that is final and has no fields other than a static instance value. You would then be able to treat that instance as the sole unit type value.

In fact, this is precisely how Kotlin defines its own `Unit`. If you navigate to the definition of `Unit`, you'll see how simply it is defined; [it's just an `object`](https://github.com/JetBrains/kotlin/blob/7a7d392b3470b38d42f80c896b7270678d0f95c3/core/builtins/src/kotlin/Unit.kt#L22)! 

Unlike Java, Kotlin allows you to use `Unit` anywhere, including as a parameter to a method. The following snippet is thus legal:

```kotlin
fun identity(param1: Unit): Unit = param1

val result = identity(param1 = Unit) // just returns the Unit instance again.
```

### The Boolean Type

We're now in familiar territory.

The boolean type has two valid values, `true` and `false` (Or whatever other names you want to use). Indeed, you don't even need to use your language's native boolean type to represent this type; you can do just as well by using a nullable instance of a unit type. If the variable is non-null, it's `true`, and if it is `null`, `false`. This is of course, a useless waste of time fit only for those interested in obfuscating their source code.

So far, we've looked at some basic examples of "rules" used to define types in type theory. Let's move onto the heart of the matter and discuss sum and product types, and how Java 21 allows us to represent them via records and sealed classes.

### The Product type

Product types are composed of two or more constituent types. In general, a product type is a list of two or more types grouped together. A product type's *arity*, or *degree*, is the number of constituent types within it.

If you'd like a nice, concrete example of a product type, look no further than the humble C `struct`:

```c
struct some_type {
    int val1; // Type 1
    char *val2; // Type 2
    double val3; // Type 3
    int val4; // Type 4
};
```

In the struct above, `some_type` is a product type composed of four different types: `int`, `char *`, `double`, and `int` again. Notice that we're repeating `int` here. How do we figure out which `int` is which when we perform operations on `some_type`? This may seem obvious, but it's a problem in math, because you have to construct all the building blocks and concepts you use from scratch!

In this case, we already have the tools to make this work. We associate each type with the name given to it in the struct (Duh). Mathematically speaking, a product type is not merely a list of types but a list of *ordered pairs*, where each ordered pair consists of a type and a name associated with that type.

For example, we can represent the first value of `some_type` as the ordered pair `(int, "val1")`. That way, it's impossible to mix up the two `int` components; they've got different names!

#### But what about tuples like in Python or Rust?

You can just think of those as product types where the "name" is the index of the component type in the tuple.

```python
some_tuple = (1, '2', True, 5)

int_1 = some_tuple[0] # (int, 0)
str_2 = some_tuple[1] # (str, 1)

...
```

#### Why do we call them product types anyway?

In set theory, the word product usually refers to the *Cartesian* product of two sets. 

{{< note-alert >}}
The Cartesian product of two sets is a set of ordered pairs made from every possible combination of every element from both sets. 

Thinking about it in simple math terms, the number of elements in the Cartesian product `C` of two sets `A` and `B` is the *product* of the number of elements in `A` and the number of elements in `B`.
{{< /note-alert >}}

You can use set theory notation to express the product of two types `A` and `B` as `C = A × B`. This product operation is not commutative; `A × B` is *not* the same as `B × A`. If you think about it for a bit, you'll see why: you'd be switching around the order of the declared components! The example I just talked about only uses two component types: `A` and `B`. How would we represent `some_type`, for example? The answer is to chain multiple product operations together, like so:

```
some_type = int × char* × double × int
```

The set of values within a product type could be expressed like this (Please leave your complaints about my (ab)use of math symbols in the comments):

```
C = A × B = {(a, b) | a ∈ A, b ∈ B}
```

You could represent the set of all values in `some_type` like this:

```
some_type = { (val1, val2, val3, val4) | val1 ∈ int, val2 ∈ char*, val3 ∈ double, val4 ∈ int }
```

#### Alright, we've established what product types are. What's this got to do with Java?

When Java 16 was released, the record class feature was stabilised. Record classes are a great example of what a product type is. All fields are final; you can't inherit from these classes either. All of a record's state is set at the time of its construction[^mutablerec] and once a record is created, that's how it'll look for the rest of its lifetime, all 200 milliseconds of it. 

[^mutablerec]: Provided you don't have a mutable data type within your record.

This contrasts with normal Java classes, which are all over the place. You can have public and private state, there's hidden state via inheritance that you don't think about until it pops up like some haunted animatronic on caffeine to jumpscare you with weird bugs, and you can have mutable fields, static fields, and all sorts of other distracting things that yadda yadda yadda... (*you get the idea*). 

The problem with normal Java types is that it is impossible to generalise what a type's components are. And that matters when all you want is to efficiently process data; you have to navigate a maze of potentially nonstandard getters to even get at your data in the first place, let alone munge it.

Now, Java never did support destructuring like JavaScript or Rust do. But even if Java had supported it, the spec would still probably restrict that feature to records. Let's ask ourselves a few questions to better understand why. 

{{< note-alert >}}
Destructuring is a feature certain languages (very famously, JavaScript) have which allows you to take a complex value, and, to borrow a PHPism, ***EXPLODE*** it into its components as a list of completely independent variables. Read more about this feature [here](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Destructuring_assignment).
{{< /note-alert >}}

#### How would you even destructure a normal Java class anyway? 

A Java class's internal state includes all its fields, both public and private. However, allowing the extraction of private fields by destructuring doesn't seem like a good idea; we all know how mad old Uncle Bob gets about breaking encapsulation. So fine, we have to exclude private state.

#### What about public state, then?

Let's first think about this: How do Java objects expose public state? Sure, you can define a field as public, and if you want to prevent undue modification, make the field final. But there is another extremely common approach to this as well. Most Java objects set every field to be private and make all fields accessible only through accessor methods for reading and writing.

Unfortunately, there are no *language enforced* conventions for defining accessors; you could give the getter for `foo` the name `getBar`, and it'll still work fine, except for the fact that it would confuse anybody trying to access `bar` and not `foo'. 

Sure, you can use frameworks like Lombok to take away the complexity and uncertainty by slapping a few annotations on your POJO classes, but that doesn't change the underlying fact that normal classes in Java are *very difficult to statically reason about* due to how many "variables" contribute to defining the state of a class. 

I suspect this is one of the problems that prevented the Java Language Specification authors from adding pattern matching to all classes right off the bat.[^future]

[^future]: JEP-441 does mention this as [something to be taken up in the future](https://openjdk.org/jeps/441#Future-work). 

To fix this, they created records, an entirely different class hierarchy. There is already a precedent; Java 5 introduced enums, which inherit from `java.lang.Enum`. Similarly, all records inherit from `java.lang.Record`. 

#### So how do records do what normal classes don't?

Records solve this problem by restricting how they can be defined and rigidly defining the set of properties they can have. 

Specifically:

- Records are implicitly final classes and can't be inherited from. 
  - No more illegitimate child classes birthed of a tryst with an entirely different concubine library.
- Records cannot extend any class but `java.lang.Record`. 
  - This avoids the pitfall of inherited state polluting the record's code.
- A record's components cannot have any visibility modifiers.
- A record's components are always final and immutable.
  - This does not extend the immutability to the contents of each record component, however.
  - Only the references of a record's components are treated as immutable.
- When you declare a record and do not define getter methods, getters will be defined using very specific syntax.
  - This syntax is very regular; Java just uses the name of the field as the getter's name.
  - For field `a`, the getter would be `a()`.
  - Java will use your definition if you manually define a getter that matches the naming conventions. Otherwise, Java will automatically create a getter method that correctly follows the conventions. The nonstandard getter won't make much of a difference.
- The fields that back a record's components are always implicitly private and are accessed only through getters.

(There is a little more to it, but this seems like a good stopping point.)

These properties of records guarantee that any new language feature Java brings out that uses records such as pattern matching will always work, because the language spec itself guarantees the behaviour and structure of records.

#### Sweet. Tell me what I can do with them.

Pattern matching. 

#### Continue...

It's quite a drag to have to write extremely nested code when you have a lot of conditions based on the types of your data. This problem will come into perspective when I introduce sum types further into the article. Pattern matching is a way to statically (Meaning at compile time, as you write the code) verify that certain patterns are present in the data you are processing.

Take a look at the example below. Note that the data within `A` is a `Record` instance, and could contain *any* record type. We first try printing `r`'s contents with normal Java if statements, before doing it with switch pattern matching.

```java
record A(Record inner) {}
record B(char b) {}
record SomeOtherRecord() {}

Record eitherAorB() {
  boolean cond1 = ((int)(Math.random() * 100) % 2 == 0);
  boolean cond2 = ((int)(Math.random() * 100) % 2 == 0);
    return cond1 ? new A(cond2 ? new A(null) : new B('e')) : new B('f'); // returns either A or B.
}

void main() {
  var r = eitherAorB();

  String oldJavaResult = "";

  if (r instanceof A) {
    var inner = ((A)r).inner(); // We have to cast it...
    if (inner instanceof B) {
      oldJavaResult = String.valueOf(((B)inner).b());
    } else if (inner instanceof SomeOtherRecord) {
      oldJavaResult = null;
    }
  } else if (r instanceof B) {
    oldJavaResult = String.valueOf((B)r.b());
  } else {
    oldJavaResult = "r does not match any pattern";
  }

  System.out.println("With the old method: \"" + oldJavaResult + "\"");

  // The type is Record.
  var result = switch (r) {
    case A(B(char a)) -> String.valueOf(a); // Destructuring!
    case A(SomeOtherRecord(/* ... */)) -> {
      // handle it.
      yield null;
    }
    case B(char b) -> String.valueOf(b);
    default -> "r does not match any pattern";
  };
  
  System.out.println(result.toString());
}
```

The switch block is clearly better structured than the if-else ladder above. Switch patterns are very powerful when you want to quickly and easily extract deeply nested data without waffling around with `instanceof` checks and cumbersome type casts. If you ever get a chance to work with Java 21 (May you be blessed with management that isn't allergic to new Java versions), I'm sure you'll appreciate this feature.

If you'd like to try this yourself, here's how. Install Java 21 (There's a handy [`jdk21-jetbrains-bin`](https://aur.archlinux.org/packages/jdk21-jetbrains-bin) package available in the AUR if anybody wants to immediately try it. I use Arch, btw). Copy this code into a `main.java` and run it with:

```sh
java --enable-preview --source 21 main.java
```

This also showcases another nice new preview feature, [unnamed main methods](https://openjdk.org/jeps/445).

In the previous example, we were able to use pattern matching to switch over different record types. Now, let's set that aside for a second and talk about how we manage choices in Java.

## Choices, choices...

What do you think of when you need a restricted set of alternatives to choose from? Java enums fit that bill; they are composed of a set of static variants and cannot change the data they contain inside.

```java
public enum Color {
  RED(255, 0, 0),
  GREEN(0, 255, 0),
  BLUE(0, 0, 255);

  public final int red;
  public final int green;
  public final int blue;

  Color(int red, int green, int blue) {
    this.red = red;
    this.green = green;
    this.blue = blue;
  }
}
```

The enum above defines three colours, red, green, and blue, with set values for the various fields, and it isn't possible to change the colour values within without messing up every single place where this enum is used (The values are final in the code above, but imagine if they weren't). 

Now, imagine a different problem. You want different colour representations such as RGB, HSL, and CMYK. Maybe just make an enum for it?

```java
public enum ColorRepresentation {
  RGB,
  HSL,
  YUV,
  CMYK
}
```

This gives us a nice, restricted set of values we can choose from. But it's cumbersome to use; if you want to have multiple colour values for different representations, you'd need to store the actual colour data separately and keep a `ColorRepresentation` enum value within to help figure out what is actually going on...

```java
class Color {
  public final ColourRepresentation repr;
  public final Number val1;
  public final Number val2;
  public final Number val3;
}
```

Obviously, this is NOT how anybody who knows Java would design the `Color` class. A much better way of implementing multiple colour representations without sacrificing readability is to use polymorphism! 

```java
public abstract class Color {}

public class RGB extends Color {
    private int red;
    private int green;
    private int blue;

    public RGB(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public String toString() {
        return "RGB Color: (" + red + ", " + green + ", " + blue + ")";
    }
}

public class CMYK extends Color {
    private double cyan;
    private double magenta;
    private double yellow;
    private double black;

    public CMYK(double cyan, double magenta, double yellow, double black) {
        this.cyan = cyan;
        this.magenta = magenta;
        this.yellow = yellow;
        this.black = black;
    }

    @Override
    public String toString() {
        return "CMYK Color: (" + cyan + "%, " + magenta + "%, " + yellow + "%, " + black + "%)";
    }
}

public class YUV extends Color {
    private int y;
    private int u;
    private int v;

    public YUV(int y, int u, int v) {
        this.y = y;
        this.u = u;
        this.v = v;
    }

    @Override
    public String toString() {
        return "YUV Color: (Y=" + y + ", U=" + u + ", V=" + v + ")";
    }
}

public class HSL extends Color {
    private double hue;
    private double saturation;
    private double lightness;

    public HSL(double hue, double saturation, double lightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.lightness = lightness;
    }

    @Override
    public String toString() {
        return "HSL Color: (H=" + hue + ", S=" + saturation + "%, L=" + lightness + "%)";
    }
}
```

Now, given a `Color` instance, you'd just need to check if it is an `instanceof` your desired colour representation, and you'd be able to access data from that representation. But this implementation has a flaw. How do we restrict what a colour is in our class hierarchy? Any user of your library could create a new `RYB` class that inherits from `Color`, or from `RGB`, for example. This becomes a problem when your library does not expect any new variants of `Color` to exist or if it does not expect specific `Color` variants to change their behaviour. Unless the API is designed to be extensible, creating new representations could cause crashes in the best case (So you have a chance of knowing what went wrong) or in the worst case, subtle bugs that affect code far away from the problem's source.

To fix this, we could do a few things:

1. Make all variants `final`. 
   - While this helps, it doesn't preclude the creation of new variants directly from `Color`, since it's impossible to make `Color` itself final.
2. Ensure that all internal logic always has a default case for unrecognised variants. 
   - This will reduce the extensibility of the library, but if that is not a goal, it will help. 
   - But this will also be a lot more error-prone; if even one part of the logic forgets to account for the bad case, there'll be problems.
3. Or, we could use sealed classes or interfaces and kill two birds with one stone.


### Sum types

Java 17's sealed classes enable design patterns based on the concept of *Sum types*. Where a product type's range of values is the product of the value ranges of its constituent types, a sum type's range of values is the *sum*. Well, that was pretty obvious from the name... But what does it mean for the range of values to be a sum?

Sum types encode that a type can be *any one* of its constituents at a single time. They are also known as tagged union types because, in type theory, they are usually represented as a type whose range of values is the union set of its components, where each component type is "tagged" with a label.

You could express a sum type like this if you were to use my pseudo-type-theory notation:

```
T = A + B + C
```

The set of values that are in T could be expressed with this logical predicate:

```
T = { x | x ∈ A ⋃ B ⋃ C }
```

You may be reminded of `C`'s `unions` when you hear the term union types. 

```c
union MyUnion {
    int intValue;
    double doubleValue;
    char charValue;
};
```

`MyUnion` is composed of three component types, and `C` allows you to treat a value of `MyUnion` as a container for any of these three types:

```c
union MyUnion myUnion;
myUnion.intValue = 42;
printf("Integer value: %d\n", myUnion.intValue);
myUnion.doubleValue = 3.14159;
printf("Double value: %lf\n", myUnion.doubleValue);
```

Note that the value of the union is overwritten in the second assignment of `doubleValue`. If you were to print `myUnion.intValue` after the second assignment, you'd see gibberish; that's actually just the bytes of `doubleValue` cut in half and interpreted as an integer.

This exposes the most significant flaw `C` unions have; there's no built-in way to know which variant is contained within a union value without external information. Thus, we need an external *discriminant* to determine what's inside. Java's polymorphism can do that for us with `instanceof`. But Java's class hierarchy is too open; there's no way to restrict the number of variants of a Java "union".

What we need are *tagged unions*, which is exactly what sealed types allow us to represent. The `sealed` modifier exists to make it clear that you can't extend a sealed class beyond the classes allowed to inherit from it. This allows the developer to control how users interact with their library's API.

```java
public sealed class Color permits RGB, CMYK, YUV, HSL {
    // Common properties or methods for all color representations
}

final class RGB extends Color {
    private final int red;
    private final int green;
    private final int blue;

    public RGB(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    // Additional methods or properties specific to RGB
}

final class CMYK extends Color {
    private final double cyan;
    private final double magenta;
    private final double yellow;
    private final double black;

    public CMYK(double cyan, double magenta, double yellow, double black) {
        this.cyan = cyan;
        this.magenta = magenta;
        this.yellow = yellow;
        this.black = black;
    }

    // Additional methods or properties specific to CMYK
}

final class YUV extends Color {
    private final int y;
    private final int u;
    private final int v;

    public YUV(int y, int u, int v) {
        this.y = y;
        this.u = u;
        this.v = v;
    }

    // Additional methods or properties specific to YUV
}

final class HSL extends Color {
    private final double hue;
    private final double saturation;
    private final double lightness;

    public HSL(double hue, double saturation, double lightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.lightness = lightness;
    }

    // Additional methods or properties specific to HSL
}
```

Note the syntax of the sealed class `Color`. There is a `sealed` modifier and a `permits` clause with the names of all the subclasses of `Color`. `permits` is used to specify which classes get to inherit from a particular class and is used to prevent any unwanted inheritance. Note also that each implementation of `Color` is marked as `final`, so you only get the four colour representations you see here; you can't make your own.

This class hierarchy is locked down. No new classes can inherit from `Color`, whether they are located in the same package or not.

If your class hierarchy starts with a `sealed` class, you must mark all inheritors (direct or indirect) as `sealed`, `non-sealed`, or `final`. If an inheriting class doesn't have these modifiers, it is a compile error.

Here's what each of these modifiers mean:

- `sealed` - The class cannot be inherited unless the inheritor's name is mentioned after `permits`. 
- `non-sealed` - The class can be inherited normally. Helpful in controlling the scope of custom behaviours.
- `final` - The class is a "leaf" in the inheritance tree; you can't extend it anymore.

These modifiers can be used to control exactly how the classes of an API can behave and how they are used. This avoids situations where the only thing stopping everything from exploding are rules that can only be enforced by developers agreeing to not do the wrong thing. 

Imagine we're writing code to handle colours based on their format. We already have a nice, restricted way of ensuring there'll be no funny business. But how would we structure the code that handles the sealed class instances?

Earlier Java versions wouldn't give us much of a choice; the best we can manage is an if-else ladder:

```java
Color color = new RGB(255, 0, 0);

if (color instanceof RGB) {
  RGB rgb = (RGB) color;
  // ...
} else if (color instanceof CMYK) {
  CMYK cmyk = (CMYK) color;
  // ...
} else if (color instanceof YUV) {
  YUV yuv = (YUV) color;
  // ...
} else if (color instanceof HSL) {
  HSL hsl = (HSL) color;
  // ...
} else {
  System.out.println("Unknown color type");
}
```

We could do a bit better if we're on Java 16+ by using `if` pattern matching:

```java
if (color instanceof RGB rgb) {
  // ...
} else if (color instanceof CMYK cmyk) {
  // ...
} else if (color instanceof YUV yuv) {
  // ...
} else if (color instanceof HSL hsl) {
  // ...
} else {
  System.out.println("Unknown color type");
}
```

But wouldn't it be nice to switch on a colour variable and extract the contents at the same time as well the way Rust can?

Rust would let you do this, for example:

```rust
let color = Color::RGB(255, 0, 0);

match color {
  Color::RGB(red, green, blue) => {
    // ...
  }
  Color::CMYK(cyan, magenta, yellow, black) => {
    // ...
  }
  Color::YUV(y, u, v) => {
    // ...
  }
  Color::HSL(hue, saturation, lightness) => {
    // ...
  }
}
```

Note that these colour values are being destructured in the `match` block above. How would we get that done with sealed classes? Switch pattern matching with destructuring only works on records, and records can't inherit from any class but `Record`... 

The solution is to just use sealed interfaces. They work in exactly the same way as sealed classes, except even records and enums can implement them.

```java
public sealed interface Color permits RGB, CMYK, YUV, HSL {
    // Common properties or methods for all color representations
    String getDescription();
}

record RGB(int red, int green, int blue) implements Color {
    public String getDescription() {
        return "RGB Color: (" + red + ", " + green + ", " + blue + ")";
    }
}

record CMYK(double cyan, double magenta, double yellow, double black) implements Color {
    public String getDescription() {
        return "CMYK Color: (" + cyan + "%, " + magenta + "%, " + yellow + "%, " + black + "%)";
    }
}

record YUV(int y, int u, int v) implements Color {
    public String getDescription() {
        return "YUV Color: (Y=" + y + ", U=" + u + ", V=" + v + ")";
    }
}

record HSL(double hue, double saturation, double lightness) implements Color {
    public String getDescription() {
        return "HSL Color: (H=" + hue + ", S=" + saturation + "%, L=" + lightness + "%)";
    }
}
```

And here's how you can neatly pattern-match and extract the data out of a `Color` instance, which is nice if all you want to do is get the data out of the class without calling any methods on it:

```java
Color color = new RGB(255, 0, 0);

switch (color) {
  case RGB(int red, int green, int blue) -> {
    // red, green and blue are in scope here.
  }
  case CMYK(double cyan, double magenta, double yellow, double black) -> {
    // ...
  }
  case YUV(int y, int u, int v) -> {
    // ...
  }
  case HSL hsl -> {
    // you can also leave the value intact and directly use the pattern matched value.
    System.out.println(hsl.getDescription());
  }
  case null -> {
    System.out.println("How did color become null?!");
  }
}
```

{{< note-alert >}}
Java 21 allows you to catch the `null` case within `switch` blocks and expressions now, so you don't need to precheck for `null` before you get to a switch. 
{{< /note-alert >}}

You may notice we aren't using a default case here. Java would have normally raised an error stating that all cases haven't been covered. However, because `Color` is a sealed class, Java can tell that every case has been handled.

### Guards! Guards! 

Did I mention we've got guard clauses? Guard clauses! You can attach additional conditions to switch arms that have to be true for the arm to be executed! Guard clauses allow the succinct expression of more complex conditions within switch statements and expressions. No more extremely nested `if` conditions in your switches.

Consider a situation where we need to special-case RGB colours where `red > 200` is true. Before, we had to put that condition inside an `if` condition within the corresponding case's body:

```java
switch (color) {
  case RGB(int red, int green, int blue) -> {
    if (red > 200) {
      System.out.println("Very red.");
    } else {
      System.out.println("Not that red...");  
    }
  }
  // ...
}
```

That isn't the ugliest thing ever, but it still means you end up nesting your code a bit more in the long run. It's generally easier to parse code that stretches out vertically than horizontally since there are fewer scopes to keep track of.

Java 21 allows us to integrate that condition within the case label with the `when` keyword.

```java
switch (color) {
  // A guarded destructuring case.
  case RGB(int red, int green, int blue) when red > 200  -> {
    System.out.println("Very red.");
  }
  // You can use guards with direct pattern matching as well.
  case RGB rgb when rgb.green > 100 -> {
    System.out.println("Sort of green...");
    // ...  
  }
  // this case is needed to preserve exhaustivity.
  case RGB rgb -> {
    System.out.println("Not that red...");
    // ...  
  }
  // ...
}
```

{{< note-alert >}}
Java will still eagerly match whichever case evaluates to true first, so make sure to put more specific cases (guarded or not) first, followed by less specific ones.
{{< /note-alert >}}

## Ugh, exceptions (ft. an example from JEP 441)

We have a new class of exceptions to deal with now. Specifically, [`java.lang.MatchException`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/MatchException.html)

What happens when a pattern match goes wrong? Consider the case of a bad record getter implementation:

```java
record R(int i) {
    public int i() {    // bad (but legal) accessor method for i
        return i / 0;
    }
}

static void exampleAnR(R r) {
    switch(r) {
        case R(var i): System.out.println(i); // i's accessor always throws!
    }
}
```

The switch block above will throw a `MatchException` because when i's getter is called, an `ArithmeticException` is thrown.  

[JEP 441](https://openjdk.org/jeps/441) states:
> (A record accessor method which always throws an exception is highly irregular, and an exhaustive pattern switch which throws a MatchException is highly unusual.)

Exhaustive switch blocks will throw if none of the specified variants can match the selector. Regarding this, the JEP states:
> (An exhaustive switch over an enum fails to match only if the enum class is changed after the switch has been compiled, which is highly unusual.)

A `MatchException` will also be thrown if a guard clause raises an exception when executed.

```java
static void example(Object obj) {
    switch (obj) {
        case R r when (r.i / 0 == 1): System.out. println("It's an R!");
        default: break;
    }
}
```

The example here is very easy to spot; we can easily statically determine that there's a divide-by-zero error here. However, the waters would be muddier if the dividend were to be a dynamic, possibly `0` value.

As my late grandfather (A doctor) always said:
> Divide by zero, not even once.

## Conclusion

In this article, we've looked at a bunch of things that Java 21 allows us to do (I haven't covered certain things like how generics interact with switch patterns, however). In the next one, I'll show you some interesting quirks and a few practical examples of how we can leverage these functional building blocks to improve how we write Java code.
