package main.java.com.helloworld.engine;

public class KaldiEngineFactory implements EngineFactory{
    @Override
    public String getName() {
        return "basic";
    }

    @Override
    public Engine createEngine() {
        //We do not want the engine instance to be reassigned (precautionary).
        final KaldiEngine engine = new KaldiEngine();
        //TODO Do we really want to initialize the engine here?
        //engine.init(null);

        return engine;
    }
}