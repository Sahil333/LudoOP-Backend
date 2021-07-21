package com.op.ludo.model;

import com.leansoft.bigqueue.BigQueueImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Component
public class LobbyPlayerQueue {
    @Autowired
    ApplicationContext applicationContext;

    private BigQueueImpl queue = null;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeQueue() throws IOException {
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

    public String dequeue() throws IOException {
        String front = null;
        if(!queue.isEmpty()){
            front = new String(queue.dequeue());
        }
        return front;
    }

    public void gc() throws IOException {
        if(queue != null) queue.gc();
    }
}
