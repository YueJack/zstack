package org.zstack.sdk;

import org.zstack.sdk.ResourceStackInventory;

public class UpdateResourceStackResult {
    public ResourceStackInventory inventory;
    public void setInventory(ResourceStackInventory inventory) {
        this.inventory = inventory;
    }
    public ResourceStackInventory getInventory() {
        return this.inventory;
    }

}
