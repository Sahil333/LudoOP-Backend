package com.op.ludo.model;

import com.leansoft.bigqueue.BigQueueImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LobbyPlayerQueque {
    @Autowired
    ApplicationContext applicationContext;

    private BigQueueImpl queue = null;

    @EventListener(ApplicationReadyEvent.class)
    public void intializeQueue() throws IOException {
        String queueDir = System.getProperty("user.home");
        String queueName = "ludo-queue";
        try{
            queue = new BigQueueImpl(queueDir, queueName);
        } catch (IOException e){
            SpringApplication.exit(applicationContext);
        }
        while(!isQueueEmpty()){
            queue.dequeue();
        }
    }

    public Boolean isQueueEmpty(){
        if(queue != null && !queue.isEmpty()){
            return false;
        }
        return true;
    }

    public Long getQueueSize() {
        if(queue != null) {
            return queue.size();
        }
        return 0l;
    }

    public void insertInQueue(String data) throws IOException {
        queue.enqueue(data.getBytes());
    }

    public String peekQueue() throws IOException {
        return new String(queue.peek());
    }

    public void dequeueQueue() throws IOException {
        if(!queue.isEmpty()){
            queue.dequeue();
            queue.gc();
        }
    }
}
