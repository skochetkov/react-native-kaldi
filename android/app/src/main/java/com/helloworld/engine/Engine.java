package main.java.com.helloworld.engine;

public interface Engine {
    void init();

    void onResourceCreated(Resource resource);

    void run();

    void destroy();
}
