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
      // ...
    }
  }

  // The type is Record.
  var result = switch (r) {
    case A(
      B(
        char a
      )
    ) -> String.valueOf(a); // Destructuring!
    case A(
      SomeOtherRecord(
        // ...
      )
    ) -> {
      // handle it.
      yield null;
    }
    case B(char b) -> String.valueOf(b);
    default -> "r does not match any pattern";
  };
        
  System.out.println(result.toString());
}
