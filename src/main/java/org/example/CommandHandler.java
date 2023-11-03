package org.example;

import org.example.api.Commands;
import org.example.domain.DomainModel;

public class CommandHandler implements Commands {

    DomainModel domainModel;

    public CommandHandler(DomainModel domainModel) {
        this.domainModel = domainModel;
    }

    @Override
    public void createItem(String id) {
        domainModel.createItem(id);
    }

    @Override
    public void createItem(String id, int[] position, int value) {
        domainModel.createItem(id, position, value);
    }

    @Override
    public void deleteItem(String id) {
        domainModel.deleteItem(id);
    }

    @Override
    public void moveItem(String id, int[] vector) {
        domainModel.moveItem(id, vector);
    }

    @Override
    public void changeValue(String id, int newValue) {
        domainModel.changeValue(id, newValue);
    }
}
