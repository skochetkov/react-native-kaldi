package main.java.com.helloworld.engine;

public class KaldiEngine implements Engine {

    @Override
    public void init() {
        //TODO
    }

    @Override
    public void onResourceCreated(Resource resource) {
        //TODO
    }

    @Override
    public void run() {
        Runnable daemon = () -> {

        };
        new Thread(daemon).start();
    }

    @Override
    public void destroy() {
        //TODO
    }
}