package co.com.julianr0223;

import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args) {
        var vertx = Vertx.vertx();

        vertx.setPeriodic(2000, ignore -> System.out.println("Running "));
    }
}
