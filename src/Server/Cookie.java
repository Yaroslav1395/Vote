package Server;

import com.sun.net.httpserver.HttpExchange;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Cookie<V> {
  private final String name;
  private final V value;
  private Integer maxAge;
  private boolean httpOnly;

  public Cookie(String name, V value, Integer maxAge) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(value);
    this.name = name.strip();
    this.value = value;
    this.maxAge = maxAge;
    this.httpOnly = true;
  }
  
  public static <V> Cookie make(String name, V value, Integer maxAge) {
    return new Cookie<>(name, value, maxAge);
  }

  public void setMaxAge(Integer maxAgeInSeconds) {
    this.maxAge = maxAgeInSeconds;
  }

  public void setHttpOnly(boolean httpOnly) {
    this.httpOnly = httpOnly;
  }

  private V getValue() { return value; }
  private Integer getMaxAge() { return maxAge; }
  private String getName() { return name; }
  private boolean isHttpOnly() { return httpOnly; }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    String encodedName = URLEncoder.encode(getName().strip(), StandardCharsets.UTF_8);
    String stringValue = getValue().toString();
    String encodedValue = URLEncoder.encode(stringValue, StandardCharsets.UTF_8);
    stringBuilder.append(String.format("%s=%s", encodedName, encodedValue.strip()));
    if(getMaxAge() != null){
      stringBuilder.append(String.format("; Max-Age=%s", getMaxAge()));
    }
    if(isHttpOnly()){
      stringBuilder.append("; HttpOnly");
    }
    return stringBuilder.toString();
  }
  public static Map<String, String> parse(String cookiesString){
    return Utils.parseUrlEncoded(cookiesString, "; ");
  }
  public static String getCookies(HttpExchange exchange){
    return exchange.getRequestHeaders().getOrDefault("Cookie", List.of("")).get(0);
  }
  public static void setCookie(HttpExchange exchange, Cookie cookie) {
    exchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
  }

}