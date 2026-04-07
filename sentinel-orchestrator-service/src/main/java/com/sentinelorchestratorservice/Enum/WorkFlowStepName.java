package com.sentinelorchestratorservice.Enum;

public enum WorkFlowStepName {
    PAYMENT,
    INVENTORY,
    NOTIFICATION;

    public int number(){
       return switch(this){
            case PAYMENT -> 0;
            case INVENTORY -> 1;
            case NOTIFICATION -> 2;
        };

    }

}
