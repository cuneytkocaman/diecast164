package com.cuneyt.diecast164.assistantclass;

import java.util.UUID;

public class RandomId {
    public String randomUUID(){
        UUID uuid = UUID.randomUUID(); // Random string oluşturuldu. Firebase için. Java guid ile oluşturuldu.
        String uniqeId = uuid.toString(); // Her oyuncuya ait bir benzersiz ID değişkene arandı.

        return uniqeId;
    }
}
