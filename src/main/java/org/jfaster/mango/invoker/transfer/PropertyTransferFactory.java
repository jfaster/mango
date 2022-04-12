package org.jfaster.mango.invoker.transfer;

import org.jfaster.mango.invoker.PropertyTransfer;
import org.jfaster.mango.invoker.transfer.enums.EnumToIntegerTransfer;
import org.jfaster.mango.invoker.transfer.enums.EnumToStringTransfer;
import org.jfaster.mango.invoker.transfer.json.ObjectToFastjsonTransfer;
import org.jfaster.mango.invoker.transfer.json.ObjectToGsonTransfer;

public class PropertyTransferFactory {

    public PropertyTransfer makeInstanceFromFactory(String type) {
        if (type.equals("ObjectToFastjsonTransfer")) {
            return new ObjectToFastjsonTransfer();
        } else if (type.equals("ObjectToGsonTransfer")) {
            return new ObjectToGsonTransfer();
        } else if (type.equals("IntegerListToStringTransfer")) {
            return new IntegerListToStringTransfer();
        } else if (type.equals("LongListToStringTransfer")) {
            return new LongListToStringTransfer();
        } else if (type.equals("StringListToStringTransfer")) {
            return new StringListToStringTransfer();
        } else if (type.equals("EnumToIntegerTransfer")) {
            return new EnumToIntegerTransfer();
        } else if (type.equals("EnumToStringTransfer")) {
            return new EnumToStringTransfer();
        } else return
                null;
    }
}
