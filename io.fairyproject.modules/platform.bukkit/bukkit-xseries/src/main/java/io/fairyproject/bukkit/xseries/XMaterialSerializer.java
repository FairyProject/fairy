package io.fairyproject.bukkit.xseries;

import com.cryptomorin.xseries.XMaterial;
import io.fairyproject.ObjectSerializer;
import io.fairyproject.container.object.Obj;

@Obj
public class XMaterialSerializer implements ObjectSerializer<XMaterial, String> {
    @Override
    public String serialize(XMaterial input) {
        return input.name();
    }

    @Override
    public XMaterial deserialize(String output) {
        return XMaterial.valueOf(output.toUpperCase());
    }

    @Override
    public Class<XMaterial> inputClass() {
        return XMaterial.class;
    }

    @Override
    public Class<String> outputClass() {
        return String.class;
    }
}
