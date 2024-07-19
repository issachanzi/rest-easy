# Manifesto of Rest Easy

## **No boilerplate**
Rest Easy should require the absolute minimal code to implement basic functionality.

The following code should be enough to implement a fully functional basic REST API.
```
public class TodoItem extends EasyModel {
  public String text;
  public boolean done;

  public static void main (String [] args) {
    new RestEasy.start ();
  }
}
```

