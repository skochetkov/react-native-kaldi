package main.java.com.helloworld.engine;

public interface EngineFactory {

    String getName();

    /**
     * We do not want to expose the logic for creating engine, it is up to specific engine factory
     */
    Engine createEngine();
}
