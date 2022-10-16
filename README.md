# Pangu-Spigot
Pangu @Bridge Support for Spigot Server

### Server to Client
Client.java
```java
// Spigot Server
interface Client {
    Client PROXY = BridgeManager.INSTANCE.createProxy(Client.class);
    
    @Bridge("PanguSpigot.Test")
    void test(Player player, String message);
}
```

Test.java
```java
// Forge Client (W/ Pangu)
public class Test {
    @Bridge(value = "PanguSpigot.Test", side = Side.CLIENT)
    public static test(String message) {
        System.out.println("Received Message From Server: " + message);
    }
}
```


### Client to Server
Server.java
```java
// Spigot Server
public enum Server {
    INSTANCE;
    
    public void register() {
        BridgeManager.INSTANCE.register(this);
    }
    
    @Bridge("PanguSpigot.Test")
    public void test(Player player, String message) {
        System.out.println("Received Message From Player " + player.getName() + ": " + message);
    }
}
```

Test.java
```java
// Forge Client (W/ Pangu)
public class Test {
  @Bridge(value = "PanguSpigot.Test")
  public static test(String message) {
  }
  
  @BindKeyPress(Keyboard.KEY_P)
  public static onKeyPress() {
      test("Press P")
  }
}
```
